/*
 * Leggi QR — Copyright (C) 2026 devfaxpro
 * SPDX-License-Identifier: GPL-3.0-only
 * Ridistribuzione e modifica consentite secondo la GNU GPL versione 3.
 * Software fornito senza garanzie; il testo completo è nel file LICENSE.
 */
package app.qrall.scanner;

import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import androidx.core.graphics.Insets;
import androidx.core.view.*;
import com.google.zxing.common.BitMatrix;

/**
 * Componenti grafici condivisi del tema chiaro blu: spaziatura, testi, pulsanti, icone e bitmap.
 */
public final class Soft {
  public static final int BG = 0xffF5F8FC,
      INK = 0xff23466D,
      MUTED = 0xff596D84,
      BLUE = 0xff386BA5,
      BLUE_LIGHT = 0xffE7F0FA,
      BLUE_MIST = 0xffE0ECFA,
      BLUE_ICE = 0xffEBF4FC,
      LINE = 0xffDAE4F0;

  /** Converte unità logiche dp in pixel, arrotondando secondo la densità del dispositivo. */
  public static int dp(Context c, float n) {
    return (int) (n * c.getResources().getDisplayMetrics().density + .5f);
  }

  /**
   * Risolve una chiave applicativa tramite riferimenti R verificati in compilazione, poi legge la
   * traduzione nella lingua corrente.
   */
  public static String t(Context c, String key) {
    return c.getString(Strings.id(key));
  }

  /**
   * Crea uno sfondo con colore, angoli arrotondati e bordo facoltativo coerenti con il tema blu.
   */
  public static GradientDrawable shape(Context c, int color, int radius, boolean border) {
    GradientDrawable d = new GradientDrawable();
    d.setColor(color);
    d.setCornerRadius(dp(c, radius));
    if (border) d.setStroke(dp(c, 1), LINE);
    return d;
  }

  /**
   * Aggiunge al componente un riscontro visivo al tocco, conservando il colore e la forma dello
   * sfondo.
   */
  public static void ripple(View v, int color, int radius) {
    v.setBackground(
        new RippleDrawable(
            ColorStateList.valueOf(0x20386BA5), shape(v.getContext(), color, radius, false), null));
  }

  /** Crea un contenitore verticale per ordinare i componenti dall’alto verso il basso. */
  public static LinearLayout column(Context c) {
    LinearLayout l = new LinearLayout(c);
    l.setOrientation(LinearLayout.VERTICAL);
    return l;
  }

  /**
   * Crea un contenitore orizzontale con allineamento verticale centrale; Android risolve la
   * direzione secondo la lingua.
   */
  public static LinearLayout row(Context c) {
    LinearLayout l = new LinearLayout(c);
    l.setGravity(Gravity.CENTER_VERTICAL);
    return l;
  }

  /** Applica la stessa spaziatura interna sui quattro lati, convertendola da dp in pixel. */
  public static void pad(View v, int a) {
    v.setPadding(
        dp(v.getContext(), a), dp(v.getContext(), a), dp(v.getContext(), a), dp(v.getContext(), a));
  }

  /**
   * Configura dimensione, colore e peso del testo. Mantiene il margine del font per non tagliare
   * accenti o segni delle scritture complesse.
   */
  public static TextView text(Context c, String value, int size, int color, boolean bold) {
    TextView t = new TextView(c);
    t.setText(value);
    t.setTextSize(size);
    t.setTextColor(color);
    t.setFontFeatureSettings("kern");
    if (bold) t.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    // Il padding del font protegge diacritici e segni di arabo, hindi, tamil e altre scritture.
    t.setIncludeFontPadding(true);
    t.setLineSpacing(dp(c, 3), 1);
    return t;
  }

  /** Crea un testo nello stile comune dopo aver risolto la sua chiave di traduzione. */
  public static TextView label(Context c, String key, int size, int color, boolean bold) {
    return text(c, t(c, key), size, color, bold);
  }

  /**
   * Inserisce una spaziatura verticale esplicita per rendere leggibile la composizione dei layout.
   */
  public static void gap(LinearLayout parent, int h) {
    Space s = new Space(parent.getContext());
    parent.addView(s, new LinearLayout.LayoutParams(1, dp(parent.getContext(), h)));
  }

  /**
   * Crea una scheda con spaziatura interna e angoli morbidi; le schede bianche ricevono anche un
   * bordo leggero.
   */
  public static LinearLayout card(Context c, int color) {
    LinearLayout l = column(c);
    pad(l, 20);
    l.setBackground(shape(c, color, 26, color == Color.WHITE));
    return l;
  }

  /**
   * Compone icona facoltativa e testo, applica area tattile minima e descrizione accessibile, poi
   * collega l’azione al tocco.
   */
  public static LinearLayout button(
      Context c, String label, String icon, int color, int foreground, Runnable click) {
    LinearLayout b = row(c);
    b.setGravity(Gravity.CENTER);
    b.setMinimumHeight(dp(c, 52));
    b.setPadding(dp(c, 16), dp(c, 12), dp(c, 16), dp(c, 12));
    ripple(b, color, 18);
    if (icon != null) {
      Icon i = new Icon(c, icon, foreground);
      b.addView(i, new LinearLayout.LayoutParams(dp(c, 22), dp(c, 22)));
      Space s = new Space(c);
      b.addView(s, new LinearLayout.LayoutParams(dp(c, 9), 1));
    }
    TextView title = text(c, label, 14, foreground, true);
    b.addView(title);
    // L’intero pulsante ha un’unica etichetta comprensibile ai servizi di accessibilità.
    b.setContentDescription(label);
    b.setFocusable(true);
    b.setOnClickListener(v -> click.run());
    return b;
  }

  /**
   * Centra un’icona dentro un’area tattile con etichetta accessibile, usata per globo, Indietro e
   * chiusura.
   */
  public static FrameLayout iconButton(Context c, String icon, String description, Runnable click) {
    FrameLayout f = new FrameLayout(c);
    ripple(f, Color.WHITE, 18);
    f.setContentDescription(description);
    f.setFocusable(true);
    Icon i = new Icon(c, icon, INK);
    FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(dp(c, 23), dp(c, 23), Gravity.CENTER);
    f.addView(i, p);
    f.setOnClickListener(v -> click.run());
    return f;
  }

  /**
   * Avvolge una colonna in una vista scorrevole che riempie lo spazio disponibile senza nascondere
   * i controlli oltre lo schermo.
   */
  public static ScrollView scroll(Context c, LinearLayout child) {
    ScrollView s = new ScrollView(c);
    s.setFillViewport(true);
    s.setVerticalScrollBarEnabled(false);
    s.setClipToPadding(false);
    s.addView(child);
    return s;
  }

  /**
   * Disegna dietro le barre di sistema e applica gli inset reali, compresi quelli della tastiera.
   * Le icone delle barre restano scure sul tema chiaro.
   */
  public static void edge(android.app.Activity a, View root) {
    WindowCompat.setDecorFitsSystemWindows(a.getWindow(), false);
    a.getWindow().setStatusBarColor(Color.TRANSPARENT);
    a.getWindow().setNavigationBarColor(Color.TRANSPARENT);
    WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(a.getWindow(), root);
    controller.setAppearanceLightStatusBars(true);
    controller.setAppearanceLightNavigationBars(true);
    ViewCompat.setOnApplyWindowInsetsListener(
        root,
        (v, w) -> {
          // Unisce barre di sistema e tastiera: il contenuto non deve finire sotto questi elementi.
          Insets i =
              w.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
          v.setPadding(i.left, i.top, i.right, i.bottom);
          return w;
        });
  }

  /**
   * Trasforma la matrice in pixel: moduli attivi nel colore scelto e sfondo bianco, senza
   * interpolazioni che riducano la leggibilità del codice.
   */
  public static Bitmap bitmap(BitMatrix matrix, int color) {
    int w = matrix.getWidth(), h = matrix.getHeight();
    int[] pixels = new int[w * h];
    for (int y = 0; y < h; y++)
      for (int x = 0; x < w; x++) pixels[y * w + x] = matrix.get(x, y) ? color : Color.WHITE;
    return Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888);
  }

  /** Simbolo vettoriale disegnato dal codice, condiviso dai pulsanti dell’interfaccia. */
  public static final class Icon extends View {
    private final String name;
    private final Paint p = new Paint(3);
    private final int color;

    /**
     * Memorizza nome e colore dell’icona. L’etichetta accessibile appartiene al pulsante che la
     * contiene, evitando annunci duplicati.
     */
    public Icon(Context c, String name, int color) {
      super(c);
      this.name = name;
      this.color = color;
      setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    /**
     * Disegna i simboli su una griglia logica 24×24. Specchia solo le frecce direzionali nelle
     * lingue RTL; gli altri simboli mantengono il proprio orientamento.
     */
    @Override
    protected void onDraw(Canvas original) {
      super.onDraw(original);
      Canvas c = original;
      c.save();
      // Le coordinate delle icone restano indipendenti dalla densità e dalle dimensioni del
      // pulsante.
      c.scale(getWidth() / 24f, getHeight() / 24f);
      if (getLayoutDirection() == LAYOUT_DIRECTION_RTL
          && (name.equals("arrow") || name.equals("back"))) c.scale(-1, 1, 12, 12);
      p.setColor(color);
      p.setStrokeWidth(1.65f);
      p.setStrokeCap(Paint.Cap.ROUND);
      p.setStrokeJoin(Paint.Join.ROUND);
      p.setStyle(Paint.Style.STROKE);
      // Ogni caso descrive un simbolo con forme semplici, senza bitmap da riscalare.
      switch (name) {
        case "scan":
          path(
              c,
              "M3 8 L3 4 L7 4 M17 4 L21 4 L21 8 M21 16 L21 20 L17 20 M7 20 L3 20 L3 16 M7 12 L17"
                  + " 12");
          break;
        case "globe":
          c.drawCircle(12, 12, 9, p);
          c.drawOval(8, 3, 16, 21, p);
          path(c, "M3 12 L21 12 M5 7 L19 7 M5 17 L19 17");
          break;
        case "plus":
          path(c, "M12 5 L12 19 M5 12 L19 12");
          break;
        case "image":
          c.drawRoundRect(3, 4, 21, 20, 3, 3, p);
          c.drawCircle(8, 9, 1.5f, p);
          path(c, "M4 18 L10 12 L14 16 L17 13 L21 17");
          break;
        case "collection":
          c.drawRoundRect(4, 6, 20, 21, 3, 3, p);
          path(c, "M8 3 L16 3 M8 11 L16 11 M8 15 L13 15");
          break;
        case "heart":
          {
            Path h = new Path();
            h.moveTo(12, 20);
            h.cubicTo(0, 12, 2, 4, 7, 4);
            h.cubicTo(10, 4, 11, 6, 12, 7);
            h.cubicTo(13, 6, 14, 4, 17, 4);
            h.cubicTo(22, 4, 24, 12, 12, 20);
            c.drawPath(h, p);
            break;
          }
        case "settings":
          c.drawCircle(12, 12, 8, p);
          c.drawCircle(12, 12, 3, p);
          path(c, "M12 2 L12 4 M12 20 L12 22 M2 12 L4 12 M20 12 L22 12");
          break;
        case "arrow":
          path(c, "M5 12 L19 12 M14 7 L19 12 L14 17");
          break;
        case "back":
          path(c, "M19 12 L5 12 M10 7 L5 12 L10 17");
          break;
        case "close":
          path(c, "M6 6 L18 18 M6 18 L18 6");
          break;
        case "url":
          c.save();
          c.rotate(-40, 12, 12);
          c.drawRoundRect(3, 8, 13, 16, 4, 4, p);
          c.drawRoundRect(11, 8, 21, 16, 4, 4, p);
          c.restore();
          break;
        case "wifi":
          c.drawArc(2, 4, 22, 24, 225, 90, false, p);
          c.drawArc(6, 9, 18, 21, 225, 90, false, p);
          c.drawCircle(12, 18, 1, p);
          break;
        case "contact":
          c.drawCircle(12, 8, 4, p);
          c.drawArc(5, 14, 19, 26, 180, 180, false, p);
          break;
        case "email":
          c.drawRoundRect(3, 5, 21, 19, 3, 3, p);
          path(c, "M4 7 L12 13 L20 7");
          break;
        case "sms":
          c.drawRoundRect(3, 3, 21, 18, 4, 4, p);
          path(c, "M7 18 L7 22 L12 18 M7 8 L17 8 M7 12 L14 12");
          break;
        case "phone":
          path(
              c, "M5 3 L9 3 L10 8 L7 10 L14 17 L16 14 L21 15 L21 19 L18 21 L14 20 L4 10 L3 6 L5 3");
          break;
        case "location":
          c.drawCircle(12, 10, 3, p);
          {
            Path h = new Path();
            h.moveTo(12, 22);
            h.cubicTo(-6, 4, 8, 0, 12, 2);
            h.cubicTo(27, 0, 22, 15, 12, 22);
            c.drawPath(h, p);
          }
          break;
        case "event":
          c.drawRoundRect(3, 5, 21, 21, 3, 3, p);
          path(c, "M7 3 L7 7 M17 3 L17 7 M3 10 L21 10 M8 14 L10 14 M14 14 L16 14");
          break;
        case "barcode":
          path(c, "M3 4 L3 20 M6 4 L6 20 M10 4 L10 20 M12 4 L12 20 M17 4 L17 20 M21 4 L21 20");
          break;
        case "flash":
          path(c, "M14 2 L5 14 L11 14 L10 22 L19 10 L13 10 L14 2");
          break;
        case "swap":
          path(c, "M4 8 L20 8 L16 4 M20 16 L4 16 L8 20");
          break;
        case "copy":
          c.drawRoundRect(8, 8, 21, 21, 2, 2, p);
          path(c, "M16 5 L16 3 L3 3 L3 16 L5 16");
          break;
        case "share":
          c.drawCircle(18, 4, 3, p);
          c.drawCircle(5, 12, 3, p);
          c.drawCircle(18, 20, 3, p);
          path(c, "M8 10 L15 6 M8 14 L15 18");
          break;
        case "download":
          path(c, "M12 3 L12 16 M7 11 L12 16 L17 11 M4 17 L4 21 L20 21 L20 17");
          break;
        case "check":
          path(c, "M4 12 L9 17 L20 6");
          break;
        case "shield":
          path(c, "M12 2 L21 6 L20 15 L12 22 L4 15 L3 6 L12 2 M8 11 L11 14 L16 9");
          break;
        case "trash":
          path(c, "M3 6 L21 6 M9 3 L15 3 M5 6 L6 21 L18 21 L19 6 M9 10 L9 17 M15 10 L15 17");
          break;
        case "edit":
          path(c, "M4 16 L4 21 L9 21 L21 9 L16 4 L4 16 M13 7 L18 12");
          break;
        case "search":
          c.drawCircle(10, 10, 6, p);
          path(c, "M15 15 L21 21");
          break;
        default:
          path(c, "M4 6 L20 6 M4 12 L20 12 M4 18 L14 18");
      }
      c.restore();
    }

    /**
     * Interpreta il piccolo formato interno di segmenti M/L per costruire le icone. I tracciati
     * sono costanti del codice e non provengono da contenuti scansionati.
     */
    private void path(Canvas c, String source) {
      String[] a = source.replaceAll("([ML])", " $1 ").trim().split("\\s+");
      Path path = new Path();
      for (int i = 0; i < a.length; ) {
        String op = a[i++];
        float x = Float.parseFloat(a[i++]), y = Float.parseFloat(a[i++]);
        if (op.equals("M")) path.moveTo(x, y);
        else path.lineTo(x, y);
      }
      c.drawPath(path, p);
    }
  }
}
