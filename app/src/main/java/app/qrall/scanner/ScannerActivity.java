/*
 * Leggi QR — Copyright (C) 2026 devfaxpro
 * SPDX-License-Identifier: GPL-3.0-only
 * Ridistribuzione e modifica consentite secondo la GNU GPL versione 3.
 * Software fornito senza garanzie; il testo completo è nel file LICENSE.
 */
package app.qrall.scanner;

import static app.qrall.scanner.Soft.*;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.*;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.Result;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;

/**
 * Sessione di lettura con CameraX. Il thread di analisi legge i frame; il thread UI gestisce
 * controlli e risultati.
 */
public final class ScannerActivity extends AppCompatActivity {
  // Il decoder lavora su un solo thread; i widget vengono aggiornati sul thread principale.
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private PreviewView preview;
  private ProcessCameraProvider provider;
  private androidx.camera.core.Camera camera;
  private boolean front = false, torch = false;
  // Questi flag vengono letti sia dal thread UI sia dal thread che analizza i fotogrammi.
  private volatile boolean finished = false, batch = false;
  private long lastFrame = 0;
  private final LinkedHashMap<String, Result> found = new LinkedHashMap<>();
  private TextView status, zoomLabel;
  private LinearLayout done;
  private SeekBar zoom;
  private final ActivityResultLauncher<String> permission =
      registerForActivityResult(
          new ActivityResultContracts.RequestPermission(),
          granted -> {
            if (granted) startCamera();
            else permissionDenied();
          });

  /**
   * 1. Ripristina l’eventuale sessione multipla. 2. Costruisce i controlli. 3. Avvia CameraX
   * soltanto se il permesso è già concesso, altrimenti lo richiede.
   */
  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    if (state != null) batch = state.getBoolean("batch");
    build();
    // Passo 1: recupera i risultati della sessione, senza conservare i fotogrammi.
    if (state != null) {
      try {
        JSONArray a = new JSONArray(state.getString("found", "[]"));
        for (int i = 0; i < a.length(); i++) {
          JSONObject j = a.getJSONObject(i);
          Result r =
              new Result(
                  j.getString("text"),
                  null,
                  null,
                  com.google.zxing.BarcodeFormat.valueOf(j.getString("format")));
          found.put(r.getBarcodeFormat() + r.getText(), r);
        }
        updateCount();
      } catch (Exception ignored) {
      }
    }
    // Passo 2: il permesso riguarda solo la scansione con camera, non la creazione dei codici.
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        == PackageManager.PERMISSION_GRANTED) startCamera();
    else permission.launch(Manifest.permission.CAMERA);
  }

  /**
   * Compone anteprima, cornice e controlli. Il tocco regola il fuoco; torcia, cambio camera, zoom e
   * scansione multipla agiscono sulla sessione CameraX corrente.
   */
  private void build() {
    LinearLayout root = column(this);
    root.setBackgroundColor(BG);
    setContentView(root);
    edge(this, root);
    LinearLayout head = row(this);
    pad(head, 16);
    head.addView(
        iconButton(this, "close", t(this, "close"), this::finish),
        new LinearLayout.LayoutParams(dp(this, 48), dp(this, 48)));
    TextView title = label(this, "scan", 22, INK, true);
    LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1);
    tp.setMarginStart(dp(this, 16));
    head.addView(title, tp);
    root.addView(head);
    FrameLayout view = new FrameLayout(this);
    preview = new PreviewView(this);
    preview.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
    view.addView(preview, new FrameLayout.LayoutParams(-1, -1));
    view.addView(new FrameOverlay(this), new FrameLayout.LayoutParams(-1, -1));
    root.addView(view, new LinearLayout.LayoutParams(-1, 0, 1));
    // Trasforma il punto toccato nel sistema di coordinate richiesto da CameraX.
    preview.setOnTouchListener(
        (v, event) -> {
          if (event.getAction() == android.view.MotionEvent.ACTION_UP && camera != null) {
            MeteringPoint p =
                preview.getMeteringPointFactory().createPoint(event.getX(), event.getY());
            camera
                .getCameraControl()
                .startFocusAndMetering(
                    new FocusMeteringAction.Builder(p)
                        .setAutoCancelDuration(3, TimeUnit.SECONDS)
                        .build());
            v.performClick();
          }
          return true;
        });
    LinearLayout controls = column(this);
    pad(controls, 20);
    status = label(this, "scan_hint", 14, MUTED, false);
    status.setGravity(Gravity.CENTER);
    controls.addView(status);
    gap(controls, 12);
    LinearLayout zoomRow = row(this);
    zoomLabel = text(this, "1.0×", 14, INK, true);
    zoomRow.addView(zoomLabel, new LinearLayout.LayoutParams(dp(this, 52), -2));
    zoom = new SeekBar(this);
    zoom.setMax(100);
    zoomRow.addView(zoom, new LinearLayout.LayoutParams(0, -2, 1));
    zoom.setContentDescription(t(this, "zoom"));
    zoom.setOnSeekBarChangeListener(
        new SeekBar.OnSeekBarChangeListener() {
          /**
           * Traduce la posizione della barra nel rapporto di zoom supportato dalla camera, fino a
           * 8×. Ignora gli aggiornamenti della barra che non provengono dall’utente.
           */
          public void onProgressChanged(SeekBar b, int p, boolean user) {
            if (camera != null && user) {
              ZoomState z = camera.getCameraInfo().getZoomState().getValue();
              if (z != null) {
                float ratio =
                    z.getMinZoomRatio()
                        + (Math.min(8, z.getMaxZoomRatio()) - z.getMinZoomRatio()) * p / 100f;
                camera.getCameraControl().setZoomRatio(ratio);
                zoomLabel.setText(String.format(Locale.getDefault(), "%.1f×", ratio));
              }
            }
          }

          /**
           * Callback obbligatoria: lo zoom cambia nei singoli aggiornamenti della barra, senza
           * lavoro aggiuntivo all’inizio del trascinamento.
           */
          public void onStartTrackingTouch(SeekBar b) {}

          /**
           * Callback obbligatoria: l’ultimo rapporto di zoom applicato rimane attivo dopo il
           * rilascio.
           */
          public void onStopTrackingTouch(SeekBar b) {}
        });
    controls.addView(zoomRow);
    LinearLayout buttons = row(this);
    buttons.addView(
        button(
            this,
            t(this, "flash"),
            "flash",
            BLUE_LIGHT,
            INK,
            () -> {
              if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                torch = !torch;
                camera.getCameraControl().enableTorch(torch);
                Toast.makeText(this, t(this, torch ? "flash_on" : "flash_off"), Toast.LENGTH_SHORT)
                    .show();
              } else Toast.makeText(this, t(this, "unavailable"), Toast.LENGTH_SHORT).show();
            }),
        new LinearLayout.LayoutParams(0, -2, 1));
    Space sp = new Space(this);
    buttons.addView(sp, new LinearLayout.LayoutParams(dp(this, 12), 1));
    buttons.addView(
        button(
            this,
            t(this, "switch_camera"),
            "swap",
            BLUE_MIST,
            INK,
            () -> {
              front = !front;
              bindCamera();
            }),
        new LinearLayout.LayoutParams(0, -2, 1));
    controls.addView(buttons);
    gap(controls, 10);
    CheckBox multiple = new CheckBox(this);
    multiple.setText(t(this, "batch"));
    multiple.setTextColor(INK);
    multiple.setChecked(batch);
    multiple.setOnCheckedChangeListener(
        (b, c) -> {
          batch = c;
          if (!batch && !found.isEmpty()) returnResults();
        });
    controls.addView(multiple);
    done = button(this, t(this, "done"), "check", BLUE, Color.WHITE, this::returnResults);
    done.setVisibility(View.GONE);
    controls.addView(done);
    root.addView(controls);
  }

  /**
   * Ottiene CameraX in modo asincrono e si collega alla fotocamera solo se la schermata è ancora
   * attiva; gli errori vengono mostrati nell’interfaccia.
   */
  private void startCamera() {
    ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
    future.addListener(
        () -> {
          if (isFinishing() || isDestroyed()) return;
          try {
            provider = future.get();
            bindCamera();
          } catch (Exception e) {
            cameraError();
          }
        },
        ContextCompat.getMainExecutor(this));
  }

  /**
   * 1. Seleziona la camera richiesta con ripiego sull’altra. 2. Collega anteprima e analisi al
   * ciclo di vita. 3. Scarta i frame arretrati e azzera torcia e zoom al cambio camera.
   */
  private void bindCamera() {
    if (provider == null) return;
    try {
      CameraSelector selector =
          front ? CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;
      if (!provider.hasCamera(selector)) {
        front = !front;
        selector = front ? CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;
      }
      // Scollega la vecchia camera prima di associare quella nuova al ciclo di vita.
      provider.unbindAll();
      Preview p = new Preview.Builder().build();
      p.setSurfaceProvider(preview.getSurfaceProvider());
      ImageAnalysis analysis =
          new ImageAnalysis.Builder()
              .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
              .setResolutionSelector(
                  new androidx.camera.core.resolutionselector.ResolutionSelector.Builder()
                      .setResolutionStrategy(
                          new androidx.camera.core.resolutionselector.ResolutionStrategy(
                              new android.util.Size(1280, 720),
                              androidx.camera.core.resolutionselector.ResolutionStrategy
                                  .FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER))
                      .build())
              .build();
      // Le analisi precedenti non si accumulano: viene mantenuto soltanto il frame più recente.
      analysis.setAnalyzer(executor, this::analyze);
      camera = provider.bindToLifecycle(this, selector, p, analysis);
      torch = false;
      zoom.setProgress(0);
      zoomLabel.setText("1.0×");
    } catch (Exception e) {
      cameraError();
    }
  }

  /**
   * Legge un frame al massimo ogni 350 ms, estrae luminanza e cerca codici fuori dal thread UI.
   * Chiude sempre ImageProxy nel finally, anche quando salta il frame o la lettura fallisce.
   */
  private void analyze(ImageProxy image) {
    try {
      long now = SystemClock.elapsedRealtime();
      // Il limite di frequenza riduce lavoro e consumo quando non serve analizzare ogni frame.
      if (finished || now - lastFrame < 350) return;
      lastFrame = now;
      // Per riconoscere i codici basta il piano Y; non serve ricostruire i colori della camera.
      ImageProxy.PlaneProxy plane = image.getPlanes()[0];
      ByteBuffer buffer = plane.getBuffer();
      int width = image.getWidth(),
          height = image.getHeight(),
          row = plane.getRowStride(),
          pixel = plane.getPixelStride();
      byte[] data = CameraFrames.luminance(buffer, width, height, row, pixel);
      List<Result> results = Codes.decode(data, width, height);
      if (!results.isEmpty()) runOnUiThread(() -> accept(results));
    } catch (RuntimeException ignored) {
    } finally {
      // Obbligatorio anche nei percorsi con return o errore: libera il frame per CameraX.
      image.close();
    }
  }

  /**
   * 1. Scarta risultati arrivati dopo la chiusura. 2. Aggiunge solo codici nuovi fino a 50. 3.
   * Vibra se richiesto e conclude oppure aggiorna il contatore della scansione multipla.
   */
  private void accept(List<Result> results) {
    if (finished
        || isFinishing()
        || !getLifecycle().getCurrentState().isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED))
      return;
    // Un duplicato non deve far vibrare di nuovo il telefono né aumentare il contatore.
    boolean fresh = false;
    for (Result r : results) {
      String key = r.getBarcodeFormat() + r.getText();
      if (!found.containsKey(key) && found.size() < 50) {
        found.put(key, r);
        fresh = true;
      }
    }
    if (fresh && getSharedPreferences("settings", MODE_PRIVATE).getBoolean("vibrate", true)) {
      Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
      if (v != null) {
        if (Build.VERSION.SDK_INT >= 26)
          v.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE));
        else v.vibrate(45);
      }
    }
    // La sessione termina al primo risultato in modalità singola oppure al limite di 50.
    if (!batch || found.size() >= 50) returnResults();
    else updateCount();
  }

  /**
   * Aggiorna il numero di codici raccolti e rende disponibile il comando finale solo quando esiste
   * almeno un risultato.
   */
  private void updateCount() {
    status.setText(t(this, "batch_count") + " " + found.size());
    done.setVisibility(found.isEmpty() ? View.GONE : View.VISIBLE);
  }

  /**
   * Converte i risultati in JSON con testo e formato. Lo stesso formato serve al ritorno della
   * scansione e al ripristino dopo una rotazione.
   */
  private String serialize() {
    JSONArray a = new JSONArray();
    for (Result r : found.values()) {
      try {
        JSONObject j = new JSONObject();
        j.put("text", r.getText());
        j.put("format", r.getBarcodeFormat().name());
        a.put(j);
      } catch (JSONException ignored) {
      }
    }
    return a.toString();
  }

  /**
   * Blocca nuove letture, restituisce i codici alla schermata chiamante e chiude lo scanner; non
   * restituisce una sessione vuota.
   */
  private void returnResults() {
    if (found.isEmpty()) return;
    finished = true;
    setResult(RESULT_OK, new Intent().putExtra("results", serialize()));
    finish();
  }

  /**
   * Spiega come autorizzare la camera e offre l’apertura delle impostazioni dell’app. Annullando,
   * chiude lo scanner lasciando disponibili le altre funzioni.
   */
  private void permissionDenied() {
    new AlertDialog.Builder(this)
        .setTitle(t(this, "camera_permission"))
        .setMessage(t(this, "permission_hint"))
        .setPositiveButton(
            t(this, "settings"),
            (d, w) -> {
              startActivity(
                  new Intent(
                      Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                      Uri.parse("package:" + getPackageName())));
              finish();
            })
        .setNegativeButton(t(this, "close"), (d, w) -> finish())
        .setOnCancelListener(d -> finish())
        .show();
  }

  /**
   * Mantiene visibile un messaggio di errore e consente di tornare indietro senza arrestare l’app.
   */
  private void cameraError() {
    status.setText(t(this, "camera_error"));
    Toast.makeText(this, t(this, "camera_error"), Toast.LENGTH_LONG).show();
  }

  /**
   * Conserva modalità multipla e codici già raccolti, senza salvare immagini o fotogrammi della
   * camera.
   */
  @Override
  protected void onSaveInstanceState(Bundle out) {
    super.onSaveInstanceState(out);
    out.putBoolean("batch", batch);
    out.putString("found", serialize());
  }

  /**
   * Segnala subito la fine della sessione e arresta l’esecutore; CameraX rilascia le risorse
   * tramite il ciclo di vita collegato.
   */
  @Override
  protected void onDestroy() {
    finished = true;
    executor.shutdown();
    super.onDestroy();
  }

  /** Guida grafica sopra la camera; non contiene dati dell’immagine e non modifica l’analisi. */
  private static final class FrameOverlay extends View {
    /**
     * Crea la cornice grafica decorativa ed esclude questa vista dalla lettura dei servizi di
     * accessibilità.
     */
    FrameOverlay(Context c) {
      super(c);
      setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    /**
     * Calcola una finestra centrale in proporzione allo schermo, scurisce l’esterno e disegna il
     * bordo. La cornice guida la persona ma non ritaglia l’immagine analizzata.
     */
    @Override
    protected void onDraw(Canvas c) {
      Paint p = new Paint(3);
      float side = Math.min(getWidth() * .76f, getHeight() * .68f),
          left = (getWidth() - side) / 2,
          top = (getHeight() - side) / 2;
      p.setColor(0x55F8F8F3);
      Path shade = new Path();
      shade.addRect(0, 0, getWidth(), getHeight(), Path.Direction.CW);
      shade.addRoundRect(left, top, left + side, top + side, 30, 30, Path.Direction.CCW);
      c.drawPath(shade, p);
      p.setStyle(Paint.Style.STROKE);
      p.setColor(Color.WHITE);
      p.setStrokeWidth(6);
      c.drawRoundRect(left, top, left + side, top + side, 30, 30, p);
    }
  }
}
