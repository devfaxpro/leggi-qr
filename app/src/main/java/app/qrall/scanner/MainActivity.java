/*
 * Leggi QR — Copyright (C) 2026 devfaxpro
 * SPDX-License-Identifier: GPL-3.0-only
 * Ridistribuzione e modifica consentite secondo la GNU GPL versione 3.
 * Software fornito senza garanzie; il testo completo è nel file LICENSE.
 */
package app.qrall.scanner;

import static app.qrall.scanner.Soft.*;

import android.content.*;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.provider.*;
import android.text.*;
import android.text.InputType;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.*;
import androidx.core.content.FileProvider;
import androidx.core.os.LocaleListCompat;
import com.google.android.material.textfield.*;
import com.google.zxing.*;
import com.google.zxing.client.result.*;
import com.google.zxing.common.BitMatrix;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;

/**
 * Schermata principale: coordina le schede, i moduli, la raccolta e gli scambi espliciti con altre
 * app.
 */
public final class MainActivity extends AppCompatActivity {
  public static final String[] LANGUAGE_TAGS = LanguageCatalog.TAGS;
  public static final String[] LANGUAGE_NAMES = LanguageCatalog.NAMES;
  // Palette dei codici; l’indice iniziale 3 corrisponde al nero richiesto.
  private static final int[] INKS = {0xff294C3B, 0xff514473, 0xff674638, Color.BLACK};
  private Store store;
  private SharedPreferences prefs;
  private LinearLayout root, body, header, bottom;
  private Store.Item current;
  private int tab = 0, filter = 0, color = 3;
  private String page = "home", formType = "text", search = "", pendingExport;
  private final List<TextInputEditText> fields = new ArrayList<>();
  private Spinner security, barcode;
  private CheckBox hidden;
  // Importare immagini può essere costoso: questa coda mantiene reattiva l’interfaccia.
  private final ExecutorService worker = Executors.newSingleThreadExecutor();
  private boolean importing = false;
  private final ActivityResultLauncher<Intent> scan =
      registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          r -> {
            if (r.getResultCode() == RESULT_OK && r.getData() != null)
              receiveScan(r.getData().getStringExtra("results"));
          });
  private final ActivityResultLauncher<String[]> photo =
      registerForActivityResult(
          new ActivityResultContracts.OpenDocument(),
          uri -> {
            if (uri != null) importImage(uri);
          });
  private final ActivityResultLauncher<Intent> saveFile =
      registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          r -> {
            if (r.getResultCode() == RESULT_OK
                && r.getData() != null
                && r.getData().getData() != null) writeExport(r.getData().getData());
          });

  /**
   * 1. Inizializza archivio e preferenze. 2. Ricostruisce schermata e dati temporanei dopo una
   * ricreazione. 3. Collega il tasto Indietro e gestisce eventuali contenuti ricevuti da altre app.
   */
  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    prefs = getSharedPreferences("settings", MODE_PRIVATE);
    store = new Store(this);
    if (state != null) {
      tab = state.getInt("tab");
      filter = state.getInt("filter");
      color = state.getInt("color", 3);
      page = state.getString("page", "home");
      formType = state.getString("formType", "text");
      search = state.getString("search", "");
      pendingExport = state.getString("pendingExport");
      String item = state.getString("item");
      if (item != null)
        try {
          current = unpack(new JSONObject(item));
        } catch (Exception ignored) {
          page = "home";
        }
    }
    build();
    installShortcuts();
    if (page.equals("form")) showForm(formType, state);
    else if (page.equals("detail") && current != null) showDetail(current);
    else showTab(tab);
    if (state == null) handleIncoming(getIntent());
    getOnBackPressedDispatcher()
        .addCallback(
            this,
            new OnBackPressedCallback(true) {
              /**
               * Torna dalla pagina di dettaglio o dal modulo alla scheda corrente; dalla home
               * lascia terminare l’attività.
               */
              @Override
              public void handleOnBackPressed() {
                if (page.equals("detail") || page.equals("form")) {
                  showTab(tab);
                } else if (tab != 0) {
                  showTab(0);
                } else {
                  setEnabled(false);
                  getOnBackPressedDispatcher().onBackPressed();
                }
              }
            });
  }

  /**
   * Costruisce la struttura comune: intestazione, contenuto espandibile e navigazione inferiore.
   * Gli inset di sistema mantengono i controlli fuori da barre e tastiera.
   */
  private void build() {
    root = column(this);
    root.setBackgroundColor(BG);
    setContentView(root);
    edge(this, root);
    header = row(this);
    header.setPadding(dp(this, 24), dp(this, 18), dp(this, 24), dp(this, 14));
    root.addView(header);
    body = column(this);
    root.addView(body, new LinearLayout.LayoutParams(-1, 0, 1));
    bottom = row(this);
    bottom.setPadding(dp(this, 12), dp(this, 10), dp(this, 12), dp(this, 10));
    bottom.setBackground(shape(this, Color.WHITE, 0, false));
    root.addView(bottom);
  }

  /**
   * Aggiorna titolo, eventuale pulsante Indietro e globo delle lingue. Il globo rimane accessibile
   * anche nei moduli e nei dettagli.
   */
  private void head(String title, boolean back) {
    header.removeAllViews();
    if (back) {
      header.addView(
          iconButton(this, "back", t(this, "back"), () -> showTab(tab)),
          new LinearLayout.LayoutParams(dp(this, 46), dp(this, 46)));
      Space space = new Space(this);
      header.addView(space, new LinearLayout.LayoutParams(dp(this, 12), 1));
    }
    LinearLayout brand = column(this);
    brand.addView(text(this, title, back ? 22 : 28, INK, true));
    if (!back) {
      gap(brand, 4);
      brand.addView(label(this, "tagline", 12, MUTED, false));
    }
    header.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));
    header.addView(
        iconButton(this, "globe", t(this, "language"), this::showLanguages),
        new LinearLayout.LayoutParams(dp(this, 48), dp(this, 48)));
  }

  /**
   * Azzera lo stato della pagina secondaria, aggiorna intestazione e navigazione, poi costruisce la
   * scheda richiesta: scansione, creazione, raccolta o impostazioni.
   */
  void showTab(int value) {
    hideKeyboard();
    tab = value;
    page = "home";
    current = null;
    fields.clear();
    body.removeAllViews();
    head(getString(R.string.app_name), false);
    navigation();
    switch (tab) {
      case 1:
        creator();
        break;
      case 2:
        collection();
        break;
      case 3:
        settings();
        break;
      default:
        home();
    }
  }

  /**
   * Crea quattro destinazioni con etichette localizzate e stato selezionato accessibile. Un secondo
   * tocco sulla scheda di scansione apre la fotocamera.
   */
  private void navigation() {
    bottom.removeAllViews();
    bottom.setVisibility(View.VISIBLE);
    String[] keys = {"scan", "create", "collection", "settings"};
    String[] icons = {"scan", "plus", "collection", "settings"};
    for (int i = 0; i < 4; i++) {
      final int index = i;
      LinearLayout b = column(this);
      b.setGravity(Gravity.CENTER);
      b.setMinimumHeight(dp(this, 58));
      ripple(b, i == tab ? BLUE_LIGHT : Color.WHITE, 20);
      b.addView(
          new Icon(this, icons[i], i == tab ? BLUE : MUTED),
          new LinearLayout.LayoutParams(dp(this, 24), dp(this, 24)));
      gap(b, 5);
      TextView name = label(this, keys[i], 11, i == tab ? INK : MUTED, i == tab);
      name.setGravity(Gravity.CENTER);
      b.addView(name);
      b.setContentDescription(t(this, keys[i]));
      b.setSelected(i == tab);
      b.setFocusable(true);
      b.setOnClickListener(
          v -> {
            if (index == 0 && tab == 0) launchScanner();
            else showTab(index);
          });
      LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1);
      lp.setMargins(dp(this, 3), 0, dp(this, 3), 0);
      bottom.addView(b, lp);
    }
  }

  /**
   * Sostituisce il contenuto corrente con una colonna scorrevole. Lo scorrimento mantiene
   * raggiungibili i controlli con testi lunghi o schermi piccoli.
   */
  private LinearLayout content() {
    body.removeAllViews();
    LinearLayout c = column(this);
    c.setPadding(dp(this, 24), dp(this, 8), dp(this, 24), dp(this, 28));
    body.addView(scroll(this, c), new LinearLayout.LayoutParams(-1, -1));
    return c;
  }

  /**
   * 1. Mostra la scheda introduttiva blu e un QR dimostrativo nero. 2. Offre scansione e
   * importazione. 3. Aggiunge scorciatoie di creazione e codici recenti.
   */
  private void home() {
    LinearLayout c = content();
    // Su display bassi il titolo compatto lascia spazio al comando principale di scansione.
    boolean compact = getResources().getConfiguration().screenHeightDp < 700;
    LinearLayout hero = card(this, BLUE_LIGHT);
    hero.setPadding(dp(this, 24), dp(this, 22), dp(this, 24), dp(this, 24));
    LinearLayout badge = row(this);
    badge.addView(
        new Icon(this, "shield", BLUE), new LinearLayout.LayoutParams(dp(this, 16), dp(this, 16)));
    TextView offline = label(this, "offline_badge", 11, BLUE, true);
    offline.setPaddingRelative(dp(this, 7), 0, 0, 0);
    badge.addView(offline);
    hero.addView(badge);
    gap(hero, 18);
    hero.addView(label(this, "hero_title", compact ? 22 : 32, INK, true));
    gap(hero, 10);
    hero.addView(label(this, "hero_subtitle", 14, MUTED, false));
    FrameLayout art = new FrameLayout(this);
    art.setMinimumHeight(dp(this, compact ? 90 : 158));
    ImageView qr = new ImageView(this);
    try {
      qr.setImageBitmap(
          bitmap(
              Codes.encode(getString(R.string.app_name), BarcodeFormat.QR_CODE, 256), Color.BLACK));
    } catch (Exception ignored) {
    }
    qr.setRotation(-8);
    pad(qr, 8);
    qr.setBackground(shape(this, Color.WHITE, 22, false));
    FrameLayout.LayoutParams qlp =
        new FrameLayout.LayoutParams(
            dp(this, compact ? 76 : 128), dp(this, compact ? 76 : 128), Gravity.CENTER);
    qlp.topMargin = dp(this, 14);
    qlp.bottomMargin = dp(this, 14);
    art.addView(qr, qlp);
    // L’immagine dimostrativa è decorativa: il pulsante di scansione ha già la propria etichetta.
    qr.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
    hero.addView(art);
    hero.addView(button(this, t(this, "scan_now"), "scan", BLUE, Color.WHITE, this::launchScanner));
    c.addView(hero);
    gap(c, 12);
    c.addView(
        button(
            this,
            t(this, "import_image"),
            "image",
            Color.WHITE,
            INK,
            () -> photo.launch(new String[] {"image/*"})));
    gap(c, 26);
    section(c, "quick_create", "view_all", () -> showTab(1));
    gap(c, 14);
    LinearLayout quick = row(this);
    String[] types = {"url", "wifi", "text", "contact"};
    for (int i = 0; i < types.length; i++) {
      String type = types[i];
      LinearLayout item = column(this);
      item.setGravity(Gravity.CENTER);
      ripple(item, i % 2 == 0 ? BLUE_MIST : BLUE_ICE, 20);
      item.setMinimumHeight(dp(this, 85));
      item.addView(
          new Icon(this, type, INK), new LinearLayout.LayoutParams(dp(this, 24), dp(this, 24)));
      gap(item, 9);
      TextView txt = label(this, type, 11, INK, true);
      txt.setGravity(Gravity.CENTER);
      item.addView(txt);
      item.setContentDescription(t(this, type));
      item.setFocusable(true);
      item.setOnClickListener(
          v -> {
            tab = 1;
            showForm(type, null);
          });
      LinearLayout.LayoutParams qp = new LinearLayout.LayoutParams(0, -2, 1);
      if (i > 0) qp.setMarginStart(dp(this, 8));
      quick.addView(item, qp);
    }
    c.addView(quick);
    gap(c, 26);
    section(c, "recent", "view_all", () -> showTab(2));
    gap(c, 12);
    List<Store.Item> recent = store.list("", 0);
    if (recent.isEmpty()) empty(c, "empty_recent", "empty_recent_hint");
    else for (int i = 0; i < Math.min(3, recent.size()); i++) itemRow(c, recent.get(i));
  }

  /**
   * Compone un titolo di sezione e, quando presente, un’azione laterale. Il titolo usa lo spazio
   * rimanente senza coprire il pulsante.
   */
  private void section(LinearLayout c, String title, String action, Runnable run) {
    LinearLayout r = row(this);
    r.addView(label(this, title, 18, INK, true), new LinearLayout.LayoutParams(0, -2, 1));
    if (action != null) {
      TextView a = label(this, action, 12, BLUE, true);
      a.setGravity(Gravity.CENTER);
      a.setMinHeight(dp(this, 48));
      a.setPaddingRelative(dp(this, 10), 0, 0, 0);
      a.setOnClickListener(v -> run.run());
      a.setFocusable(true);
      r.addView(a);
    }
    c.addView(r);
  }

  /**
   * Mostra titolo e spiegazione quando una raccolta non contiene elementi, così l’utente sa come
   * iniziare.
   */
  private void empty(LinearLayout c, String title, String hint) {
    LinearLayout panel = card(this, Color.WHITE);
    panel.addView(label(this, title, 16, INK, true));
    gap(panel, 7);
    panel.addView(label(this, hint, 13, MUTED, false));
    c.addView(panel);
  }

  /**
   * Elenca i tipi di codice disponibili, con icona e descrizione tradotta; ogni riga apre il modulo
   * corrispondente.
   */
  private void creator() {
    LinearLayout c = content();
    c.addView(label(this, "create_title", 30, INK, true));
    gap(c, 8);
    c.addView(label(this, "create_hint", 14, MUTED, false));
    gap(c, 22);
    String[] hints = {
      "url_hint",
      "text_hint",
      "wifi_hint",
      "contact_hint",
      "email_hint",
      "sms_hint",
      "phone_hint",
      "location_hint",
      "event_hint",
      "barcode_hint"
    };
    for (int i = 0; i < Payloads.TYPES.length; i++) {
      String type = Payloads.TYPES[i];
      LinearLayout tile = row(this);
      pad(tile, 18);
      ripple(tile, Color.WHITE, 22);
      FrameLayout icon = new FrameLayout(this);
      icon.setBackground(
          shape(this, i % 3 == 0 ? BLUE_LIGHT : i % 3 == 1 ? BLUE_MIST : BLUE_ICE, 16, false));
      icon.addView(
          new Icon(this, type, INK),
          new FrameLayout.LayoutParams(dp(this, 24), dp(this, 24), Gravity.CENTER));
      tile.addView(icon, new LinearLayout.LayoutParams(dp(this, 48), dp(this, 48)));
      LinearLayout titles = column(this);
      titles.setPaddingRelative(dp(this, 16), 0, dp(this, 8), 0);
      titles.addView(label(this, type, 16, INK, true));
      gap(titles, 5);
      titles.addView(label(this, hints[i], 12, MUTED, false));
      tile.addView(titles, new LinearLayout.LayoutParams(0, -2, 1));
      tile.addView(
          new Icon(this, "arrow", MUTED),
          new LinearLayout.LayoutParams(dp(this, 20), dp(this, 20)));
      tile.setOnClickListener(v -> showForm(type, null));
      tile.setFocusable(true);
      c.addView(tile);
      gap(c, 10);
    }
  }

  /**
   * 1. Crea i controlli specifici del tipo scelto. 2. Configura tastiera, limiti e campi
   * obbligatori. 3. Ripristina i valori precedenti e collega il comando di generazione.
   */
  void showForm(String type, Bundle state) {
    page = "form";
    formType = type;
    current = null;
    fields.clear();
    head(t(this, type), true);
    bottom.setVisibility(View.GONE);
    LinearLayout c = content();
    c.addView(label(this, "form_hint", 14, MUTED, false));
    gap(c, 20);
    if (type.equals("wifi")) {
      security = spinner(new String[] {"WPA / WPA2", "WPA3 (SAE)", "WEP", t(this, "open_network")});
      c.addView(label(this, "security", 13, MUTED, true));
      gap(c, 8);
      c.addView(security);
      gap(c, 14);
    }
    if (type.equals("barcode")) {
      barcode =
          spinner(
              new String[] {
                "Code 128", "EAN-13", "EAN-8", "Code 39", "ITF", "PDF417", "Aztec", "Data Matrix"
              });
      c.addView(label(this, "format", 13, MUTED, true));
      gap(c, 8);
      c.addView(barcode);
      gap(c, 14);
    }
    String[] keys = Payloads.fields(type);
    for (int i = 0; i < keys.length; i++) {
      String key = keys[i];
      TextInputLayout wrapper = new TextInputLayout(this);
      wrapper.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
      wrapper.setBoxBackgroundColor(Color.WHITE);
      wrapper.setBoxStrokeColor(BLUE);
      wrapper.setBoxCornerRadii(dp(this, 16), dp(this, 16), dp(this, 16), dp(this, 16));
      wrapper.setHint(t(this, key) + (i == 0 ? " *" : ""));
      TextInputEditText edit = new TextInputEditText(wrapper.getContext());
      edit.setBackground(null);
      edit.setTextSize(16);
      edit.setTextColor(INK);
      edit.setMinHeight(dp(this, 58));
      edit.setFilters(new InputFilter[] {new InputFilter.LengthFilter(3000)});
      int input = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
      if (key.equals("url_field") || key.equals("website"))
        input = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI;
      if (key.equals("email_field"))
        input = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
      if (key.equals("phone_field")) input = InputType.TYPE_CLASS_PHONE;
      if (key.equals("latitude") || key.equals("longitude"))
        input =
            InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_DECIMAL
                | InputType.TYPE_NUMBER_FLAG_SIGNED;
      if (key.equals("password")) {
        input = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
        wrapper.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
      }
      if (key.equals("content") || key.equals("message")) {
        input |= InputType.TYPE_TEXT_FLAG_MULTI_LINE;
        edit.setMinLines(3);
        edit.setGravity(Gravity.TOP);
      }
      edit.setInputType(input);
      edit.setId(View.generateViewId());
      edit.setSaveEnabled(false);
      wrapper.addView(edit);
      if (key.equals("start") || key.equals("end")) {
        wrapper.setHelperText(t(this, "date_hint"));
        edit.setFocusable(false);
        edit.setOnClickListener(v -> pickDate(edit));
      }
      c.addView(wrapper);
      gap(c, 14);
      fields.add(edit);
    }
    if (type.equals("wifi")) {
      hidden = new CheckBox(this);
      hidden.setText(t(this, "hidden_network"));
      hidden.setTextColor(INK);
      c.addView(hidden);
    }
    if (type.equals("barcode")) {
      c.addView(label(this, "barcode_help", 12, MUTED, false));
      gap(c, 14);
    }
    gap(c, 10);
    c.addView(button(this, t(this, "generate"), "plus", BLUE, Color.WHITE, this::generate));
    gap(c, 14);
    c.addView(label(this, "private_hint", 12, MUTED, false));
    if (state != null) {
      ArrayList<String> restored = state.getStringArrayList("formValues");
      if (restored != null)
        for (int i = 0; i < Math.min(fields.size(), restored.size()); i++)
          fields.get(i).setText(restored.get(i));
      if (type.equals("wifi")) {
        security.setSelection(state.getInt("security"));
        hidden.setChecked(state.getBoolean("hidden"));
      }
      if (type.equals("barcode")) barcode.setSelection(state.getInt("barcode"));
    }
  }

  /**
   * Apre in sequenza data e ora, conservando il valore già presente quando valido. Scrive nel campo
   * il formato stabile richiesto dalla validazione degli eventi.
   */
  private void pickDate(EditText edit) {
    Calendar now = Calendar.getInstance();
    new android.app.DatePickerDialog(
            this,
            (picker, year, month, day) ->
                new android.app.TimePickerDialog(
                        this,
                        (clock, hour, minute) ->
                            edit.setText(
                                String.format(
                                    Locale.ROOT,
                                    "%04d-%02d-%02d %02d:%02d",
                                    year,
                                    month + 1,
                                    day,
                                    hour,
                                    minute)),
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        android.text.format.DateFormat.is24HourFormat(this))
                    .show(),
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH))
        .show();
  }

  /**
   * Crea un selettore con testo e freccia espliciti. L’allineamento relativo funziona anche con
   * arabo ed ebraico e non dipende dal layout predefinito di Android.
   */
  private Spinner spinner(String[] values) {
    Spinner s = new Spinner(this);
    ArrayAdapter<String> a =
        new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, values) {
          /**
           * Prepara la riga del selettore con stile leggibile; la freccia compare solo nel valore
           * selezionato, non nelle opzioni del menu.
           */
          private TextView item(int position, boolean selected) {
            TextView title = text(MainActivity.this, getItem(position), 16, INK, false);
            title.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            title.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            title.setPaddingRelative(
                dp(MainActivity.this, 16),
                dp(MainActivity.this, 10),
                dp(MainActivity.this, 16),
                dp(MainActivity.this, 10));
            title.setMinHeight(dp(MainActivity.this, 52));
            if (selected)
              title.setCompoundDrawablesRelativeWithIntrinsicBounds(
                  null, null, getDrawable(R.drawable.ic_dropdown), null);
            return title;
          }

          /** Restituisce la riga visibile quando il menu è chiuso. */
          @Override
          public View getView(int position, View convertView, ViewGroup parent) {
            return item(position, true);
          }

          /** Restituisce una singola opzione del menu aperto. */
          @Override
          public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return item(position, false);
          }
        };
    s.setAdapter(a);
    s.setMinimumHeight(dp(this, 52));
    s.setBackground(shape(this, Color.WHITE, 16, true));
    return s;
  }

  /**
   * 1. Legge i campi e costruisce il contenuto. 2. Verifica che ZXing possa codificarlo. 3. Salva
   * se la cronologia è attiva e mostra il risultato; gli errori diventano un messaggio localizzato.
   */
  private void generate() {
    // Passo 1: acquisisci una copia dei valori nell’ordine stabilito da Payloads.fields.
    String[] values = new String[fields.size()];
    for (int i = 0; i < values.length; i++) values[i] = String.valueOf(fields.get(i).getText());
    try {
      String secure =
          formType.equals("wifi")
              ? new String[] {"WPA", "SAE", "WEP", "nopass"}[security.getSelectedItemPosition()]
              : "";
      // Passo 2: valida i dati prima di creare anteprima o registrazione in cronologia.
      String payload =
          Payloads.build(formType, values, secure, formType.equals("wifi") && hidden.isChecked());
      BarcodeFormat format =
          Payloads.format(
              formType, formType.equals("barcode") ? barcode.getSelectedItemPosition() : 0);
      // Una prova di codifica intercetta lunghezza e checksum non supportati prima del salvataggio.
      Codes.encode(payload, format, 600);
      Store.Item item = new Store.Item(payload, format.name(), true);
      if (prefs.getBoolean("history", true)) store.save(item);
      hideKeyboard();
      showDetail(item);
    } catch (Exception e) {
      toast(t(this, "invalid_input"));
    }
  }

  /**
   * Costruisce ricerca, filtro e lista dei codici. Gli ascoltatori aggiornano solo i risultati
   * della raccolta quando cambiano testo o categoria.
   */
  private void collection() {
    LinearLayout c = content();
    c.addView(label(this, "collection_title", 30, INK, true));
    gap(c, 8);
    c.addView(label(this, "collection_hint", 14, MUTED, false));
    gap(c, 18);
    EditText query = new EditText(this);
    query.setSingleLine(true);
    query.setTextSize(15);
    query.setTextColor(INK);
    query.setHintTextColor(MUTED);
    query.setHint(t(this, "search"));
    pad(query, 16);
    query.setBackground(shape(this, Color.WHITE, 18, true));
    query.setText(search);
    c.addView(query);
    gap(c, 12);
    Spinner filterSpinner =
        spinner(
            new String[] {
              t(this, "all"), t(this, "favorites"), t(this, "created"), t(this, "scanned")
            });
    filterSpinner.setSelection(filter);
    c.addView(filterSpinner);
    gap(c, 16);
    LinearLayout rows = column(this);
    c.addView(rows);
    Runnable reload =
        () -> {
          rows.removeAllViews();
          List<Store.Item> items = store.list(search, filter);
          if (items.isEmpty()) empty(rows, "empty_collection", "empty_collection_hint");
          else for (Store.Item i : items) itemRow(rows, i);
        };
    query.addTextChangedListener(
        new TextWatcher() {
          /**
           * Callback richiesta dall’interfaccia: nessuna operazione prima della modifica del testo.
           */
          public void beforeTextChanged(CharSequence s, int st, int count, int after) {}

          /** Conserva la query aggiornata e ricarica la lista con il filtro corrente. */
          public void onTextChanged(CharSequence s, int st, int before, int count) {
            search = s.toString();
            reload.run();
          }

          /**
           * Callback richiesta dall’interfaccia: l’aggiornamento è già gestito durante la modifica.
           */
          public void afterTextChanged(Editable e) {}
        });
    filterSpinner.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          /** Aggiorna il filtro scelto nel menu e ricarica gli elementi visibili. */
          public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
            filter = pos;
            reload.run();
          }

          /** Non cambia il filtro se Android segnala una selezione assente. */
          public void onNothingSelected(AdapterView<?> p) {}
        });
    reload.run();
  }

  /**
   * Classifica il contenuto riconosciuto da ZXing per scegliere icona, etichetta e azione proposta.
   */
  private String kind(Store.Item i) {
    try {
      ParsedResult p = parse(i);
      return switch (p.getType()) {
        case URI -> "url";
        case WIFI -> "wifi";
        case ADDRESSBOOK -> "contact";
        case EMAIL_ADDRESS -> "email";
        case SMS -> "sms";
        case TEL -> "phone";
        case GEO -> "location";
        case CALENDAR -> "event";
        default -> i.barcodeFormat() == BarcodeFormat.QR_CODE ? "text" : "barcode";
      };
    } catch (Exception e) {
      return "text";
    }
  }

  /**
   * Interpreta il testo salvato senza eseguire il suo contenuto; il risultato strutturato alimenta
   * le sole anteprime e azioni di conferma.
   */
  private ParsedResult parse(Store.Item i) {
    return ResultParser.parseResult(new Result(i.text, null, null, i.barcodeFormat()));
  }

  /**
   * Crea una riga della raccolta con nome, tipo e data. I dati Wi-Fi non espongono la password nel
   * riepilogo; il tocco apre il dettaglio.
   */
  private void itemRow(LinearLayout c, Store.Item item) {
    LinearLayout r = row(this);
    pad(r, 16);
    ripple(r, Color.WHITE, 20);
    r.addView(
        new Icon(this, item.favorite ? "heart" : kind(item), BLUE),
        new LinearLayout.LayoutParams(dp(this, 24), dp(this, 24)));
    LinearLayout labels = column(this);
    labels.setPaddingRelative(dp(this, 14), 0, dp(this, 8), 0);
    TextView title =
        text(this, item.title.isEmpty() ? t(this, kind(item)) : item.title, 15, INK, true);
    title.setMaxLines(1);
    title.setEllipsize(TextUtils.TruncateAt.END);
    labels.addView(title);
    gap(labels, 5);
    String summary =
        kind(item).equals("wifi")
            ? t(this, "wifi_hint")
            : item.text.replace('\n', ' ').replace('\r', ' ');
    TextView sub = text(this, summary, 12, MUTED, false);
    sub.setMaxLines(1);
    sub.setEllipsize(TextUtils.TruncateAt.END);
    labels.addView(sub);
    gap(labels, 5);
    labels.addView(
        text(
            this,
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                .format(new Date(item.time)),
            10,
            MUTED,
            false));
    r.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
    r.addView(
        new Icon(this, "arrow", MUTED), new LinearLayout.LayoutParams(dp(this, 18), dp(this, 18)));
    r.setOnClickListener(v -> showDetail(item));
    r.setFocusable(true);
    c.addView(r);
    gap(c, 9);
  }

  /**
   * 1. Genera l’anteprima del codice. 2. Mostra contenuto e azione disponibile. 3. Collega
   * preferiti, colore, condivisione ed esportazione; la cancellazione richiede conferma.
   */
  void showDetail(Store.Item item) {
    page = "detail";
    current = item;
    head(t(this, item.created ? "your_code" : "result"), true);
    bottom.setVisibility(View.GONE);
    LinearLayout c = content();
    LinearLayout card = card(this, Color.WHITE);
    TextView type =
        text(this, item.title.isEmpty() ? t(this, kind(item)) : item.title, 24, INK, true);
    card.addView(type);
    gap(card, 6);
    card.addView(text(this, item.format.replace('_', ' '), 11, MUTED, true));
    gap(card, 12);
    try {
      ImageView image = new ImageView(this);
      image.setImageBitmap(bitmap(Codes.encode(item.text, item.barcodeFormat(), 700), INKS[color]));
      image.setAdjustViewBounds(true);
      image.setScaleType(ImageView.ScaleType.FIT_CENTER);
      image.setContentDescription(t(this, "code_preview"));
      card.addView(
          image,
          new LinearLayout.LayoutParams(
              -1, dp(this, item.barcodeFormat() == BarcodeFormat.QR_CODE ? 240 : 170)));
    } catch (Exception e) {
      card.addView(label(this, "preview_unavailable", 13, MUTED, false));
    }
    gap(card, 12);
    String display = parse(item).getDisplayResult();
    TextView value = text(this, display, 15, INK, false);
    value.setTextIsSelectable(true);
    value.setMaxLines(7);
    card.addView(value);
    gap(card, 8);
    TextView full = label(this, "show_content", 12, BLUE, true);
    full.setMinHeight(dp(this, 48));
    full.setGravity(Gravity.CENTER_VERTICAL);
    full.setOnClickListener(
        v ->
            new AlertDialog.Builder(this)
                .setTitle(t(this, "content"))
                .setMessage(item.text)
                .setPositiveButton(t(this, "close"), null)
                .show());
    card.addView(full);
    c.addView(card);
    gap(c, 14);
    // Le azioni vengono soltanto preparate: il contenuto letto non viene eseguito da solo.
    Intent action = actionFor(item);
    if (action != null) {
      c.addView(
          button(
              this,
              t(this, actionLabel(item)),
              kind(item),
              BLUE,
              Color.WHITE,
              () -> confirmAction(item, action)));
      gap(c, 10);
    }
    LinearLayout main = row(this);
    main.addView(
        button(this, t(this, "copy"), "copy", BLUE_LIGHT, INK, () -> copy(item.text)),
        new LinearLayout.LayoutParams(0, -2, 1));
    Space sp = new Space(this);
    main.addView(sp, new LinearLayout.LayoutParams(dp(this, 10), 1));
    main.addView(
        button(
            this,
            t(this, item.favorite ? "favorited" : "favorite"),
            "heart",
            BLUE_MIST,
            INK,
            () -> {
              store.favorite(item, !item.favorite);
              showDetail(item);
            }),
        new LinearLayout.LayoutParams(0, -2, 1));
    c.addView(main);
    gap(c, 20);
    c.addView(label(this, "color", 14, INK, true));
    gap(c, 10);
    LinearLayout colors = row(this);
    String[] names = {"sage", "lavender", "cocoa", "ink"};
    for (int i = 0; i < INKS.length; i++) {
      final int at = i;
      FrameLayout swatch = new FrameLayout(this);
      swatch.setBackground(shape(this, INKS[i], 18, false));
      if (color == i)
        swatch.addView(
            new Icon(this, "check", Color.WHITE),
            new FrameLayout.LayoutParams(dp(this, 24), dp(this, 24), Gravity.CENTER));
      swatch.setContentDescription(t(this, names[i]));
      swatch.setSelected(color == i);
      swatch.setFocusable(true);
      swatch.setOnClickListener(
          v -> {
            color = at;
            showDetail(item);
          });
      LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(0, dp(this, 48), 1);
      if (i > 0) pp.setMarginStart(dp(this, 12));
      colors.addView(swatch, pp);
    }
    c.addView(colors);
    gap(c, 20);
    c.addView(
        button(this, t(this, "share_image"), "share", Color.WHITE, INK, () -> export(true, false)));
    gap(c, 9);
    c.addView(
        button(
            this, t(this, "save_png"), "download", Color.WHITE, INK, () -> export(false, false)));
    gap(c, 9);
    c.addView(
        button(this, t(this, "save_svg"), "download", Color.WHITE, INK, () -> export(false, true)));
    gap(c, 9);
    c.addView(
        button(
            this,
            t(this, "share_text"),
            "text",
            Color.WHITE,
            INK,
            () -> {
              Intent send =
                  new Intent(Intent.ACTION_SEND)
                      .setType("text/plain")
                      .putExtra(Intent.EXTRA_TEXT, item.text);
              safeStart(Intent.createChooser(send, t(this, "share_text")));
            }));
    gap(c, 9);
    c.addView(button(this, t(this, "rename"), "edit", Color.WHITE, INK, () -> rename(item)));
    if (item.id >= 0) {
      gap(c, 9);
      c.addView(
          button(
              this,
              t(this, "delete"),
              "trash",
              BLUE_ICE,
              INK,
              () ->
                  new AlertDialog.Builder(this)
                      .setTitle(t(this, "delete_confirm"))
                      .setPositiveButton(
                          t(this, "delete"),
                          (d, w) -> {
                            store.delete(item.id);
                            showTab(tab);
                          })
                      .setNegativeButton(t(this, "cancel"), null)
                      .show()));
    }
  }

  /**
   * Prepara un Intent compatibile con il contenuto, senza avviarlo. Consente solo link HTTP/HTTPS
   * senza credenziali e usa il dialer per i numeri, evitando chiamate automatiche.
   */
  Intent actionFor(Store.Item item) {
    ParsedResult p = parse(item);
    try {
      // Un QR può contenere testo arbitrario: la proposta di apertura applica una lista di schemi
      // web ammessi.
      if (p instanceof URIParsedResult uri) {
        String s = uri.getURI();
        Uri parsed = Uri.parse(s);
        if (!Set.of("http", "https")
                .contains(String.valueOf(parsed.getScheme()).toLowerCase(Locale.ROOT))
            || parsed.getHost() == null
            || parsed.getUserInfo() != null) return null;
        return new Intent(Intent.ACTION_VIEW, parsed).addCategory(Intent.CATEGORY_BROWSABLE);
      }
      if (p instanceof TelParsedResult tel)
        // ACTION_DIAL richiede alla persona di effettuare la chiamata nell’app telefono.
        return new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tel.getNumber()));
      if (p instanceof EmailAddressParsedResult mail) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, mail.getTos());
        intent.putExtra(Intent.EXTRA_SUBJECT, mail.getSubject());
        intent.putExtra(Intent.EXTRA_TEXT, mail.getBody());
        return intent;
      }
      if (p instanceof SMSParsedResult sms) {
        String[] numbers = sms.getNumbers();
        if (numbers == null || numbers.length == 0) return null;
        return new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + numbers[0]))
            .putExtra("sms_body", sms.getBody());
      }
      if (p instanceof GeoParsedResult geo)
        return new Intent(Intent.ACTION_VIEW, Uri.parse(geo.getGeoURI()));
      if (p instanceof WifiParsedResult) return new Intent(Settings.ACTION_WIFI_SETTINGS);
      if (p instanceof AddressBookParsedResult contact) {
        Intent i = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
        if (contact.getNames() != null && contact.getNames().length > 0)
          i.putExtra(ContactsContract.Intents.Insert.NAME, contact.getNames()[0]);
        if (contact.getPhoneNumbers() != null && contact.getPhoneNumbers().length > 0)
          i.putExtra(ContactsContract.Intents.Insert.PHONE, contact.getPhoneNumbers()[0]);
        if (contact.getEmails() != null && contact.getEmails().length > 0)
          i.putExtra(ContactsContract.Intents.Insert.EMAIL, contact.getEmails()[0]);
        i.putExtra(ContactsContract.Intents.Insert.COMPANY, contact.getOrg());
        return i;
      }
      if (p instanceof CalendarParsedResult event) {
        Intent i =
            new Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, event.getSummary())
                .putExtra(CalendarContract.Events.EVENT_LOCATION, event.getLocation())
                .putExtra(CalendarContract.Events.DESCRIPTION, event.getDescription())
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.getStartTimestamp())
                .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, event.isStartAllDay());
        if (event.getEndTimestamp() > 0)
          i.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.getEndTimestamp());
        return i;
      }
    } catch (Exception ignored) {
    }
    return null;
  }

  /** Sceglie la chiave tradotta del pulsante esterno in base al contenuto del codice. */
  private String actionLabel(Store.Item i) {
    return switch (kind(i)) {
      case "url" -> "open_link";
      case "wifi" -> "connect_wifi";
      case "contact" -> "add_contact";
      case "event" -> "add_event";
      case "location" -> "open_map";
      case "phone" -> "dial";
      case "email", "sms" -> "compose";
      default -> "open_link";
    };
  }

  /**
   * Mostra cosa verrà aperto e attende il consenso nel dialogo. Per il Wi-Fi copia la password
   * soltanto alla conferma e poi apre le impostazioni di rete.
   */
  private void confirmAction(Store.Item item, Intent action) {
    String message =
        kind(item).equals("wifi")
            ? t(this, "wifi_connect_hint")
            : t(this, "open_confirm") + "\n\n" + parse(item).getDisplayResult();
    new AlertDialog.Builder(this)
        .setTitle(t(this, actionLabel(item)))
        .setMessage(message)
        .setPositiveButton(
            t(this, "continue_action"),
            (d, w) -> {
              if (parse(item) instanceof WifiParsedResult wifi
                  && wifi.getPassword() != null
                  && !wifi.getPassword().isEmpty()) copy(wifi.getPassword());
              safeStart(action);
            })
        .setNegativeButton(t(this, "cancel"), null)
        .show();
  }

  /**
   * Chiede un titolo fino a 80 caratteri; salva soltanto alla conferma e ridisegna il dettaglio con
   * il nuovo nome.
   */
  private void rename(Store.Item item) {
    EditText input = new EditText(this);
    input.setSingleLine(true);
    input.setText(item.title);
    input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(80)});
    pad(input, 20);
    new AlertDialog.Builder(this)
        .setTitle(t(this, "rename"))
        .setView(input)
        .setPositiveButton(
            t(this, "save"),
            (d, w) -> {
              store.rename(item, input.getText().toString().trim());
              showDetail(item);
            })
        .setNegativeButton(t(this, "cancel"), null)
        .show();
  }

  /**
   * 1. Inserisce la scelta di seguire il sistema. 2. Mostra i nomi nativi e seleziona la lingua
   * corrente. 3. Delega persistenza e ricreazione ad AppCompat; una lista vuota ripristina il
   * sistema.
   */
  void showLanguages() {
    String[] options = new String[LANGUAGE_NAMES.length + 1];
    // La voce zero non impone una lingua: ripristina il comportamento automatico del sistema.
    options[0] = t(this, "system_language");
    System.arraycopy(LANGUAGE_NAMES, 0, options, 1, LANGUAGE_NAMES.length);
    String selected = AppCompatDelegate.getApplicationLocales().toLanguageTags();
    int checked = 0;
    for (int i = 0; i < LANGUAGE_TAGS.length; i++)
      if (LANGUAGE_TAGS[i].equalsIgnoreCase(selected)) checked = i + 1;
    new AlertDialog.Builder(this)
        .setTitle(t(this, "language"))
        .setSingleChoiceItems(
            options,
            checked,
            (d, w) -> {
              d.dismiss();
              // Lista vuota = sistema. AppCompat gestisce la preferenza anche sulle versioni
              // Android precedenti alla 13.
              AppCompatDelegate.setApplicationLocales(
                  LocaleListCompat.forLanguageTags(w == 0 ? "" : LANGUAGE_TAGS[w - 1]));
            })
        .setNegativeButton(t(this, "cancel"), null)
        .show();
  }

  /**
   * Costruisce preferenze, spiegazione della privacy, esportazione e cancellazione della raccolta,
   * informazioni sulla versione e accesso alle licenze.
   */
  private void settings() {
    LinearLayout c = content();
    c.addView(label(this, "settings_title", 30, INK, true));
    gap(c, 20);
    c.addView(button(this, t(this, "language"), "globe", Color.WHITE, INK, this::showLanguages));
    gap(c, 16);
    toggle(c, "history", "history_setting", "history_hint", true);
    gap(c, 10);
    toggle(c, "vibrate", "vibrate_setting", "vibrate_hint", true);
    gap(c, 22);
    LinearLayout privacy = card(this, BLUE_LIGHT);
    privacy.addView(
        new Icon(this, "shield", BLUE), new LinearLayout.LayoutParams(dp(this, 28), dp(this, 28)));
    gap(privacy, 12);
    privacy.addView(label(this, "privacy_title", 20, INK, true));
    gap(privacy, 10);
    privacy.addView(label(this, "privacy_body", 14, MUTED, false));
    c.addView(privacy);
    gap(c, 16);
    c.addView(
        button(this, t(this, "export_history"), "download", Color.WHITE, INK, this::exportHistory));
    gap(c, 10);
    c.addView(
        button(
            this,
            t(this, "clear_history"),
            "trash",
            BLUE_ICE,
            INK,
            () ->
                new AlertDialog.Builder(this)
                    .setTitle(t(this, "clear_history"))
                    .setMessage(t(this, "clear_confirm"))
                    .setPositiveButton(
                        t(this, "delete"),
                        (d, w) -> {
                          store.clear();
                          toast(t(this, "deleted"));
                        })
                    .setNegativeButton(t(this, "cancel"), null)
                    .show()));
    gap(c, 22);
    c.addView(label(this, "about", 16, INK, true));
    gap(c, 8);
    c.addView(label(this, "about_body", 13, MUTED, false));
    gap(c, 12);
    TextView licenses = label(this, "licenses", 13, BLUE, true);
    licenses.setMinHeight(dp(this, 48));
    licenses.setGravity(Gravity.CENTER_VERTICAL);
    licenses.setOnClickListener(v -> showLicenses());
    c.addView(licenses);
  }

  /**
   * Collega un interruttore a una preferenza persistente e aggiunge una spiegazione localizzata
   * sotto il controllo.
   */
  private void toggle(LinearLayout c, String key, String title, String hint, boolean defaultValue) {
    LinearLayout card = card(this, Color.WHITE);
    androidx.appcompat.widget.SwitchCompat toggle =
        new androidx.appcompat.widget.SwitchCompat(this);
    toggle.setText(t(this, title));
    toggle.setTextSize(15);
    toggle.setTextColor(INK);
    toggle.setChecked(prefs.getBoolean(key, defaultValue));
    toggle.setOnCheckedChangeListener((b, value) -> prefs.edit().putBoolean(key, value).apply());
    card.addView(toggle);
    gap(card, 7);
    card.addView(label(this, hint, 12, MUTED, false));
    c.addView(card);
  }

  /**
   * Legge gli avvisi distribuiti nell’APK e li mostra in una finestra scorrevole con testo
   * selezionabile. Un errore di lettura non arresta l’app.
   */
  private void showLicenses() {
    try (InputStream stream = getAssets().open("licenses.txt")) {
      String text = new String(readAll(stream), StandardCharsets.UTF_8);
      TextView view = Soft.text(this, text, 12, INK, false);
      pad(view, 20);
      view.setTextIsSelectable(true);
      ScrollView scroll = new ScrollView(this);
      scroll.addView(view);
      new AlertDialog.Builder(this)
          .setTitle(t(this, "licenses"))
          .setView(scroll)
          .setPositiveButton(t(this, "close"), null)
          .show();
    } catch (Exception e) {
      toast(t(this, "unavailable"));
    }
  }

  /**
   * Installa le scorciatoie localizzate sui launcher Android compatibili. Rispetta il limite di
   * aggiornamento del sistema e ignora gli stati transitori non disponibili.
   */
  private void installShortcuts() {
    if (Build.VERSION.SDK_INT < 25) return;
    android.content.pm.ShortcutManager manager =
        getSystemService(android.content.pm.ShortcutManager.class);
    if (manager == null || manager.isRateLimitingActive()) return;
    try {
      java.util.List<android.content.pm.ShortcutInfo> items = new ArrayList<>();
      for (String key : new String[] {"scan", "create"})
        items.add(
            new android.content.pm.ShortcutInfo.Builder(this, key)
                .setShortLabel(t(this, key))
                .setIcon(
                    android.graphics.drawable.Icon.createWithResource(this, R.drawable.ic_launcher))
                .setIntent(new Intent(this, MainActivity.class).setAction("app.qrall." + key))
                .build());
      manager.setDynamicShortcuts(items);
    } catch (IllegalStateException ignored) {
    }
  }

  /**
   * Distingue scorciatoie, immagini condivise e testo. Accetta immagini tramite URI content e
   * limita il testo a 10.000 caratteri; presenta il contenuto senza aprirlo automaticamente.
   */
  private void handleIncoming(Intent intent) {
    if ("app.qrall.scan".equals(intent.getAction())) {
      launchScanner();
      return;
    }
    if ("app.qrall.create".equals(intent.getAction())) {
      showTab(1);
      return;
    }
    // Ignora gli Intent che non appartengono ai flussi di condivisione previsti.
    if (!Intent.ACTION_SEND.equals(intent.getAction())) return;
    if (intent.getType() != null && intent.getType().startsWith("image/")) {
      Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
      if (uri != null && "content".equals(uri.getScheme())) importImage(uri);
    } else if ("text/plain".equals(intent.getType())) {
      String text = intent.getStringExtra(Intent.EXTRA_TEXT);
      if (text != null && !text.isBlank() && text.length() <= 10000) {
        Store.Item item = new Store.Item(text, "QR_CODE", false);
        if (prefs.getBoolean("history", true)) store.save(item);
        showDetail(item);
      }
    }
  }

  /**
   * Avvia la fotocamera tramite il contratto Activity Result, che riporterà i risultati alla
   * schermata principale.
   */
  private void launchScanner() {
    scan.launch(new Intent(this, ScannerActivity.class));
  }

  /**
   * Decodifica l’elenco JSON restituito dallo scanner, applica la preferenza sulla cronologia e
   * passa i risultati alla schermata di scelta.
   */
  private void receiveScan(String json) {
    try {
      JSONArray array = new JSONArray(json);
      List<Store.Item> items = new ArrayList<>();
      for (int i = 0; i < array.length(); i++) {
        JSONObject j = array.getJSONObject(i);
        Store.Item item = new Store.Item(j.getString("text"), j.getString("format"), false);
        if (prefs.getBoolean("history", true)) store.save(item);
        items.add(item);
      }
      displayResults(items);
    } catch (Exception e) {
      toast(t(this, "no_code"));
    }
  }

  /**
   * Mostra direttamente un unico risultato, propone una scelta quando sono diversi e segnala
   * l’assenza di codici. Il riepilogo Wi-Fi non rivela la password.
   */
  private void displayResults(List<Store.Item> items) {
    if (items.isEmpty()) {
      toast(t(this, "no_code"));
      return;
    }
    if (items.size() == 1) {
      showDetail(items.get(0));
      return;
    }
    String[] labels = new String[items.size()];
    for (int i = 0; i < items.size(); i++) {
      Store.Item item = items.get(i);
      labels[i] =
          t(this, kind(item))
              + " · "
              + (kind(item).equals("wifi")
                  ? t(this, "wifi_hint")
                  : item.text.substring(0, Math.min(item.text.length(), 60)));
    }
    new AlertDialog.Builder(this)
        .setTitle(t(this, "choose_code"))
        .setItems(labels, (d, w) -> showDetail(items.get(w)))
        .setNegativeButton(t(this, "close"), null)
        .show();
  }

  /**
   * 1. Legge le dimensioni senza caricare la foto intera. 2. Riduce le immagini grandi e analizza i
   * pixel su un thread di lavoro. 3. Riporta i risultati sul thread UI solo se l’attività è ancora
   * valida.
   */
  private void importImage(Uri uri) {
    // Evita di avviare due importazioni contemporanee dalla stessa schermata.
    if (importing) return;
    importing = true;
    toast(t(this, "reading_image"));
    worker.execute(
        () -> {
          List<Store.Item> items = new ArrayList<>();
          try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            // Passo 1: leggi solo le dimensioni; non allocare subito la foto a piena risoluzione.
            bounds.inJustDecodeBounds = true;
            try (InputStream s = getContentResolver().openInputStream(uri)) {
              BitmapFactory.decodeStream(s, null, bounds);
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw new IOException();
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 1;
            // Passo 2: limita entrambi i lati per contenere la memoria necessaria alla decodifica.
            while (bounds.outWidth / opts.inSampleSize > 2400
                || bounds.outHeight / opts.inSampleSize > 2400) opts.inSampleSize *= 2;
            Bitmap bitmap;
            try (InputStream s = getContentResolver().openInputStream(uri)) {
              bitmap = BitmapFactory.decodeStream(s, null, opts);
            }
            if (bitmap == null) throw new IOException();
            int width = bitmap.getWidth(), height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            bitmap.recycle();
            // Passo 3: esegui la lettura offline e prepara gli elementi senza toccare ancora i
            // widget.
            for (Result r : Codes.decodePixels(pixels, width, height))
              items.add(new Store.Item(r.getText(), r.getBarcodeFormat().name(), false));
            runOnUiThread(
                () -> {
                  importing = false;
                  // L’importazione può terminare dopo la chiusura della schermata: in quel caso non
                  // aggiornare la UI.
                  if (isDestroyed() || isFinishing()) return;
                  for (Store.Item item : items)
                    if (prefs.getBoolean("history", true)) store.save(item);
                  displayResults(items);
                });
            // Anche un’immagine danneggiata o troppo onerosa deve riportare la UI in stato
            // utilizzabile.
          } catch (Exception | OutOfMemoryError e) {
            runOnUiThread(
                () -> {
                  importing = false;
                  if (!isDestroyed()) toast(t(this, "image_error"));
                });
          }
        });
  }

  /**
   * Genera PNG o SVG nella cache privata. Per condividere concede un URI temporaneo tramite
   * FileProvider; per salvare avvia il selettore documenti di Android.
   */
  private void export(boolean share, boolean svg) {
    if (current == null) return;
    try {
      File folder = new File(getCacheDir(), "exports");
      folder.mkdirs();
      String extension = svg ? "svg" : "png";
      File out = new File(folder, "leggi-qr-" + System.currentTimeMillis() + "." + extension);
      BitMatrix matrix =
          Codes.encode(
              current.text,
              current.barcodeFormat(),
              svg ? (current.barcodeFormat() == BarcodeFormat.QR_CODE ? 0 : 600) : 1600);
      try (OutputStream stream = new FileOutputStream(out)) {
        if (svg) stream.write(Codes.svg(matrix, INKS[color]).getBytes(StandardCharsets.UTF_8));
        else {
          Bitmap image = bitmap(matrix, INKS[color]);
          image.compress(Bitmap.CompressFormat.PNG, 100, stream);
          image.recycle();
        }
      }
      String mime = svg ? "image/svg+xml" : "image/png";
      if (share) {
        // Condividi solo l’URI del file scelto con permesso temporaneo di lettura, non l’intera
        // cache.
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".files", out);
        Intent send =
            new Intent(Intent.ACTION_SEND)
                .setType(mime)
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        send.setClipData(ClipData.newRawUri(getString(R.string.app_name), uri));
        safeStart(Intent.createChooser(send, t(this, "share_image")));
      } else createExport(out, mime);
    } catch (Exception e) {
      toast(t(this, "export_error"));
    }
  }

  /**
   * Serializza la raccolta in JSON e informa che può contenere dati come password Wi-Fi. Il file
   * viene copiato fuori dalla cache solo dopo la conferma e la scelta della destinazione.
   */
  private void exportHistory() {
    try {
      List<Store.Item> items = store.list("", 0);
      JSONArray data = new JSONArray();
      for (Store.Item i : items) data.put(pack(i));
      File folder = new File(getCacheDir(), "exports");
      folder.mkdirs();
      File file = new File(folder, "leggi-qr-history-" + System.currentTimeMillis() + ".json");
      try (OutputStream out = new FileOutputStream(file)) {
        out.write(data.toString(2).getBytes(StandardCharsets.UTF_8));
      }
      new AlertDialog.Builder(this)
          .setTitle(t(this, "export_history"))
          .setMessage(t(this, "export_history_hint"))
          .setPositiveButton(
              t(this, "continue_action"), (d, w) -> createExport(file, "application/json"))
          .setNegativeButton(t(this, "cancel"), null)
          .show();
    } catch (Exception e) {
      toast(t(this, "export_error"));
    }
  }

  /**
   * Ricorda il nome del file temporaneo e apre il selettore Android. Non richiede accesso generale
   * all’archiviazione del telefono.
   */
  private void createExport(File file, String mime) {
    pendingExport = file.getName();
    saveFile.launch(
        // Il selettore Android restituisce l’URI autorizzato della destinazione scelta dalla
        // persona.
        new Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType(mime)
            .putExtra(Intent.EXTRA_TITLE, file.getName()));
  }

  /**
   * Copia il file della cache nella destinazione autorizzata dal selettore. Chiude entrambi i
   * flussi e azzera lo stato pendente solo dopo il completamento.
   */
  private void writeExport(Uri destination) {
    try {
      if (pendingExport == null) throw new IOException();
      File file = new File(new File(getCacheDir(), "exports"), pendingExport);
      try (InputStream in = new FileInputStream(file);
          OutputStream out = getContentResolver().openOutputStream(destination, "w")) {
        if (out == null) throw new IOException();
        // Copia a blocchi per evitare di caricare in memoria tutto il file esportato.
        byte[] buffer = new byte[8192];
        int n;
        while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
      }
      toast(t(this, "saved"));
      pendingExport = null;
    } catch (Exception e) {
      toast(t(this, "export_error"));
    }
  }

  /** Inserisce il testo negli appunti di Android e dà un riscontro localizzato all’utente. */
  private void copy(String text) {
    ((android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE))
        .setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name), text));
    toast(t(this, "copied"));
  }

  /**
   * Avvia un Intent richiesto dall’utente e converte l’assenza di un’app compatibile o di un
   * permesso in un messaggio comprensibile.
   */
  private void safeStart(Intent intent) {
    try {
      startActivity(intent);
    } catch (ActivityNotFoundException | SecurityException e) {
      toast(t(this, "no_handler"));
    }
  }

  /** Mostra un messaggio breve usando il contesto dell’attività corrente. */
  private void toast(String text) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show();
  }

  /**
   * Chiude la tastiera soltanto se una vista ha il focus, per lasciare spazio alla nuova schermata.
   */
  private void hideKeyboard() {
    View focus = getCurrentFocus();
    if (focus != null)
      ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
          .hideSoftInputFromWindow(focus.getWindowToken(), 0);
  }

  /**
   * Legge un flusso in blocchi e restituisce i byte. Il chiamante mantiene la responsabilità di
   * chiudere il flusso originale.
   */
  private static byte[] readAll(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[8192];
    int n;
    while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
    return out.toByteArray();
  }

  /**
   * Converte tutti i dati dell’elemento in JSON per esportazione e salvataggio temporaneo dello
   * stato.
   */
  private static JSONObject pack(Store.Item i) throws JSONException {
    JSONObject j = new JSONObject();
    j.put("id", i.id)
        .put("content", i.text)
        .put("format", i.format)
        .put("title", i.title)
        .put("time", i.time)
        .put("favorite", i.favorite)
        .put("created", i.created);
    return j;
  }

  /**
   * Ripristina un elemento serializzato, incluso id, data, titolo e preferito, senza inserirlo
   * nuovamente nel database.
   */
  private static Store.Item unpack(JSONObject j) throws JSONException {
    Store.Item i =
        new Store.Item(j.getString("content"), j.getString("format"), j.getBoolean("created"));
    i.id = j.getLong("id");
    i.time = j.getLong("time");
    i.title = j.optString("title", "");
    i.favorite = j.getBoolean("favorite");
    return i;
  }

  /**
   * Salva navigazione, filtri, elemento aperto e campi del modulo prima della ricreazione
   * dell’attività, così una rotazione non cancella il lavoro in corso.
   */
  @Override
  protected void onSaveInstanceState(Bundle state) {
    super.onSaveInstanceState(state);
    // Passo 1: conserva navigazione e opzioni indipendenti dalla pagina.
    state.putInt("tab", tab);
    state.putInt("filter", filter);
    state.putInt("color", color);
    state.putString("page", page);
    state.putString("formType", formType);
    state.putString("search", search);
    state.putString("pendingExport", pendingExport);
    if (current != null)
      try {
        state.putString("item", pack(current).toString());
      } catch (Exception ignored) {
      }
    // Passo 2: conserva i campi non ancora generati, inclusi selettori e rete nascosta.
    if (page.equals("form")) {
      ArrayList<String> values = new ArrayList<>();
      for (TextInputEditText f : fields) values.add(String.valueOf(f.getText()));
      state.putStringArrayList("formValues", values);
      if (formType.equals("wifi")) {
        state.putInt("security", security.getSelectedItemPosition());
        state.putBoolean("hidden", hidden.isChecked());
      }
      if (formType.equals("barcode")) state.putInt("barcode", barcode.getSelectedItemPosition());
    }
  }

  /**
   * Chiude database e thread di lavoro quando l’attività viene distrutta, evitando di trattenere
   * risorse della schermata.
   */
  @Override
  protected void onDestroy() {
    worker.shutdown();
    store.close();
    super.onDestroy();
  }
}
