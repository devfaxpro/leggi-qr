/*
 * Leggi QR — Copyright (C) 2026 devfaxpro
 * SPDX-License-Identifier: GPL-3.0-only
 * Ridistribuzione e modifica consentite secondo la GNU GPL versione 3.
 * Software fornito senza garanzie; il testo completo è nel file LICENSE.
 */
package app.qrall.scanner;

import android.graphics.*;
import android.view.*;
import java.io.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.*;

/**
 * Rendering nativi Android per confrontare layout e scritture. Le immagini non provengono da un
 * dispositivo fisico.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, qualifiers = "it-rIT-w393dp-h852dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class RenderTest {
  /**
   * Produce schermate arabe su display piccolo e verifica direzione RTL e limiti del selettore
   * Wi-Fi.
   */
  @Test
  @Config(qualifiers = "ar-rEG-ldrtl-w320dp-h568dp-xhdpi")
  public void renderArabicSmallPhone() throws Exception {
    try (ActivityController<MainActivity> c =
        Robolectric.buildActivity(MainActivity.class).setup()) {
      render(c.get(), "home-ar-small", 640, 1136);
      org.junit.Assert.assertEquals(
          View.LAYOUT_DIRECTION_RTL,
          c.get().findViewById(android.R.id.content).getLayoutDirection());
      c.get().showTab(1);
      render(c.get(), "create-ar-small", 640, 1136);
      c.get().showForm("wifi", null);
      render(c.get(), "wifi-ar-small", 640, 1136);
      checkSpinnerBounds(c.get().findViewById(android.R.id.content));
    }
  }

  /** Controlla che l’opzione selezionata resti dentro il selettore, anche nell’interfaccia RTL. */
  private void checkSpinnerBounds(View view) {
    if (view instanceof android.widget.Spinner spinner) {
      org.junit.Assert.assertNotNull(spinner.getSelectedView());
      View selected = spinner.getSelectedView();
      org.junit.Assert.assertTrue(
          "Spinner selected item outside visible bounds: "
              + selected.getLeft()
              + ","
              + selected.getRight()
              + " width="
              + spinner.getWidth(),
          selected.getLeft() >= 0 && selected.getRight() <= spinner.getWidth());
      org.junit.Assert.assertTrue(selected.getWidth() > 0);
    } else if (view instanceof ViewGroup group) {
      for (int i = 0; i < group.getChildCount(); i++) checkSpinnerBounds(group.getChildAt(i));
    }
  }

  /**
   * Produce home e impostazioni in hindi per controllare segni del font e spaziatura su display
   * piccolo.
   */
  @Test
  @Config(qualifiers = "hi-rIN-w320dp-h568dp-xhdpi")
  public void renderHindiSmallPhone() throws Exception {
    try (ActivityController<MainActivity> c =
        Robolectric.buildActivity(MainActivity.class).setup()) {
      render(c.get(), "home-hi-small", 640, 1136);
      c.get().showTab(3);
      render(c.get(), "settings-hi-small", 640, 1136);
    }
  }

  /** Produce home e creazione in tamil per verificare i testi lunghi su display piccolo. */
  @Test
  @Config(qualifiers = "ta-rIN-w320dp-h568dp-xhdpi")
  public void renderTamilSmallPhone() throws Exception {
    try (ActivityController<MainActivity> c =
        Robolectric.buildActivity(MainActivity.class).setup()) {
      render(c.get(), "home-ta-small", 640, 1136);
      c.get().showTab(1);
      render(c.get(), "create-ta-small", 640, 1136);
    }
  }

  /**
   * Produce la home in thailandese per controllare le scritture con segni sopra e sotto la riga.
   */
  @Test
  @Config(qualifiers = "th-rTH-w393dp-h852dp-xhdpi")
  public void renderThai() throws Exception {
    try (ActivityController<MainActivity> c =
        Robolectric.buildActivity(MainActivity.class).setup()) {
      render(c.get(), "home-th", 786, 1704);
    }
  }

  /**
   * Renderizza le schermate principali italiane usando i componenti Android reali in Robolectric.
   */
  @Test
  public void renderMainScreens() throws Exception {
    RuntimeEnvironment.getApplication().deleteDatabase("qrall-soft.db");
    try (ActivityController<MainActivity> c =
        Robolectric.buildActivity(MainActivity.class).setup()) {
      MainActivity a = c.get();
      render(a, "home", 786, 1704);
      a.showTab(1);
      render(a, "create", 786, 1704);
      a.showForm("wifi", null);
      render(a, "wifi", 786, 1704);
      a.showDetail(new Store.Item("https://example.org", "QR_CODE", true));
      render(a, "detail", 786, 1704);
      a.showTab(3);
      render(a, "settings", 786, 1704);
      a.showTab(0);
    }
  }

  /**
   * Produce la home italiana nel formato compatto, per verificare la raggiungibilità dei controlli.
   */
  @Test
  @Config(qualifiers = "it-rIT-w320dp-h568dp-xhdpi")
  public void renderSmallPhone() throws Exception {
    try (ActivityController<MainActivity> c =
        Robolectric.buildActivity(MainActivity.class).setup()) {
      render(c.get(), "home-small", 640, 1136);
    }
  }

  /** Produce home e creazione in tedesco, utile per controllare etichette più lunghe. */
  @Test
  @Config(qualifiers = "de-rDE-w393dp-h852dp-xhdpi")
  public void renderGerman() throws Exception {
    try (ActivityController<MainActivity> c =
        Robolectric.buildActivity(MainActivity.class).setup()) {
      render(c.get(), "home-de", 786, 1704);
      c.get().showTab(1);
      render(c.get(), "create-de", 786, 1704);
    }
  }

  /** Produce la home in giapponese per controllare composizione e font CJK. */
  @Test
  @Config(qualifiers = "ja-rJP-w393dp-h852dp-xhdpi")
  public void renderJapanese() throws Exception {
    try (ActivityController<MainActivity> c =
        Robolectric.buildActivity(MainActivity.class).setup()) {
      render(c.get(), "home-ja", 786, 1704);
    }
  }

  /**
   * Disegna l’icona e la grafica di presentazione dalle risorse native effettivamente usate dal
   * progetto.
   */
  @Test
  public void renderStoreArt() throws Exception {
    try (ActivityController<MainActivity> c =
        Robolectric.buildActivity(MainActivity.class).setup()) {
      Bitmap icon = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
      android.graphics.drawable.Drawable drawable = c.get().getDrawable(R.drawable.ic_launcher);
      drawable.setBounds(0, 0, 512, 512);
      drawable.draw(new Canvas(icon));
      write(icon, "icon-512");
      Bitmap banner = Bitmap.createBitmap(1024, 500, Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(banner);
      canvas.drawColor(Soft.BG);
      Paint paint = new Paint(3);
      paint.setColor(Soft.BLUE_LIGHT);
      canvas.drawCircle(870, 200, 330, paint);
      paint.setColor(Soft.INK);
      paint.setTypeface(Typeface.create("sans-serif-medium", 0));
      paint.setTextSize(84);
      canvas.drawText(c.get().getString(R.string.app_name), 64, 160, paint);
      paint.setTextSize(34);
      canvas.drawText("Scansiona. Crea. Conserva.", 64, 230, paint);
      paint.setTypeface(Typeface.create("sans-serif", 0));
      paint.setTextSize(22);
      paint.setColor(Soft.MUTED);
      canvas.drawText("Il tuo mondo, in un piccolo codice.", 64, 285, paint);
      drawable.setBounds(688, 104, 936, 352);
      drawable.draw(canvas);
      write(banner, "feature-1024x500");
    }
  }

  /**
   * Salva un bitmap di verifica come PNG nella cartella build/test-render e ne libera la memoria.
   */
  private void write(Bitmap bitmap, String name) throws Exception {
    File file = new File("build/test-render/" + name + ".png");
    file.getParentFile().mkdirs();
    try (FileOutputStream out = new FileOutputStream(file)) {
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
    }
    bitmap.recycle();
  }

  /**
   * Misura e dispone la gerarchia Android alle dimensioni richieste, poi la disegna in un PNG per
   * il controllo visivo.
   */
  private void render(MainActivity a, String name, int width, int height) throws Exception {
    View root = a.findViewById(android.R.id.content);
    root.measure(
        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
    root.layout(0, 0, width, height);
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    root.draw(new Canvas(bitmap));
    File file = new File("build/test-render/" + name + ".png");
    file.getParentFile().mkdirs();
    try (FileOutputStream out = new FileOutputStream(file)) {
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
    }
    bitmap.recycle();
  }
}
