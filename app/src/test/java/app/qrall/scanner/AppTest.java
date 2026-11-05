/*
 * Leggi QR — Copyright (C) 2026 devfaxpro
 * SPDX-License-Identifier: GPL-3.0-only
 * Ridistribuzione e modifica consentite secondo la GNU GPL versione 3.
 * Software fornito senza garanzie; il testo completo è nel file LICENSE.
 */
package app.qrall.scanner;

import static org.junit.Assert.*;

import android.content.*;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.*;

/** Prove Android su risorse, dati e flussi dell’app; non sostituiscono un collaudo su telefono. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, qualifiers = "it-rIT-w393dp-h852dp-xhdpi")
public class AppTest {
  /** Prepara dati e preferenze puliti per isolare ogni prova dalle precedenti. */
  @Before
  public void reset() {
    RuntimeEnvironment.getApplication().deleteDatabase("qrall-soft.db");
    RuntimeEnvironment.getApplication().getSharedPreferences("settings", 0).edit().clear().commit();
  }

  /** Verifica che una lettura ripetuta non duplichi il codice e conservi titolo e preferito. */
  @Test
  public void databaseDeduplicatesAndPreservesFavorite() {
    try (Store store = new Store(RuntimeEnvironment.getApplication())) {
      Store.Item a = store.save(new Store.Item("hello", "QR_CODE", false));
      store.favorite(a, true);
      store.rename(a, "A greeting");
      store.save(new Store.Item("hello", "QR_CODE", false));
      assertEquals(1, store.list("", 0).size());
      assertEquals("A greeting", store.list("", 1).get(0).title);
      assertEquals(1, store.list("greeting", 0).size());
      assertTrue(store.list("%", 0).isEmpty());
      store.clear();
      assertTrue(store.list("", 0).isEmpty());
    }
  }

  /** Verifica che provenienze diverse rimangano distinguibili nei filtri della raccolta. */
  @Test
  public void separateCreatedAndScannedFilters() {
    try (Store store = new Store(RuntimeEnvironment.getApplication())) {
      store.save(new Store.Item("same", "QR_CODE", true));
      store.save(new Store.Item("same", "QR_CODE", false));
      assertEquals(1, store.list("", 2).size());
      assertEquals(1, store.list("", 3).size());
      assertEquals(2, store.list("", 0).size());
    }
  }

  /**
   * Controlla che tutte le 40 lingue risolvano un titolo tradotto con una vera interruzione di
   * riga.
   */
  @Test
  public void allFortyLanguagesResolveTranslatedStrings() {
    Context context = RuntimeEnvironment.getApplication();
    Set<String> translations = new HashSet<>();
    for (String tag : MainActivity.LANGUAGE_TAGS) {
      Configuration config = new Configuration(context.getResources().getConfiguration());
      config.setLocale(Locale.forLanguageTag(tag));
      String title = Soft.t(context.createConfigurationContext(config), "hero_title");
      assertFalse(title.contains("\\n"));
      assertTrue(title.contains("\n"));
      translations.add(title);
    }
    assertEquals(40, translations.size());
  }

  /** Controlla ordine delle prime dieci lingue, nomi nativi e assenza di duplicati regionali. */
  @Test
  public void languageNamesAndOrderAreExact() {
    assertArrayEquals(
        new String[] {
          "Italiano",
          "English",
          "Español",
          "Português (Brasil)",
          "Français",
          "Deutsch",
          "简体中文",
          "日本語",
          "한국어",
          "Русский"
        },
        Arrays.copyOf(MainActivity.LANGUAGE_NAMES, 10));
    assertEquals(40, MainActivity.LANGUAGE_NAMES.length);
    assertEquals(40, MainActivity.LANGUAGE_TAGS.length);
    Set<String> languages = new HashSet<>();
    for (String tag : MainActivity.LANGUAGE_TAGS)
      assertTrue(
          "Duplicate regional variant: " + tag,
          languages.add(Locale.forLanguageTag(tag).getLanguage()));
  }

  /** Prova lingue di sistema con regione e una lingua non disponibile, verificando il fallback. */
  @Test
  public void regionalSystemLocalesResolveWithoutDuplicateEntries() {
    Context context = RuntimeEnvironment.getApplication();
    String[][] cases = {
      {"ar-EG", "الإعدادات"},
      {"he-IL", "הגדרות"},
      {"id-ID", "Setelan"},
      {"hi-IN", "सेटिंग"},
      {"pt-PT", "Configurações"},
      {"zh-CN", "设置"},
      {"fil-PH", "Mga setting"},
      {"zu-ZA", "Settings"}
    };
    for (String[] entry : cases) {
      Configuration config = new Configuration(context.getResources().getConfiguration());
      config.setLocale(Locale.forLanguageTag(entry[0]));
      assertEquals(
          entry[0],
          entry[1],
          context.createConfigurationContext(config).getString(R.string.settings));
    }
  }

  /** Simula scelta manuale dal globo e ritorno al sistema attraverso il dialogo effettivo. */
  @Test
  public void globeOffersFortyLanguagesAndCanReturnToSystem() {
    try (ActivityController<MainActivity> c =
        Robolectric.buildActivity(MainActivity.class).setup()) {
      MainActivity a = c.get();
      a.showLanguages();
      androidx.appcompat.app.AlertDialog dialog =
          (androidx.appcompat.app.AlertDialog)
              org.robolectric.shadows.ShadowDialog.getLatestDialog();
      ListView list = dialog.getListView();
      assertEquals(41, list.getCount());
      assertEquals(a.getString(R.string.system_language), list.getAdapter().getItem(0));
      int arabic = Arrays.asList(MainActivity.LANGUAGE_TAGS).indexOf("ar") + 1;
      list.performItemClick(null, arabic, arabic);
      assertEquals(
          "ar", androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().toLanguageTags());
      a.showLanguages();
      dialog =
          (androidx.appcompat.app.AlertDialog)
              org.robolectric.shadows.ShadowDialog.getLatestDialog();
      assertEquals(arabic, dialog.getListView().getCheckedItemPosition());
      dialog.getListView().performItemClick(null, 0, 0);
      assertTrue(androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().isEmpty());
    } finally {
      androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
          androidx.core.os.LocaleListCompat.getEmptyLocaleList());
    }
  }

  /** Verifica che il manifesto non conceda Internet o accesso generale alla memoria esterna. */
  @Test
  public void noInternetOrStoragePermission() {
    Context c = RuntimeEnvironment.getApplication();
    assertEquals(
        android.content.pm.PackageManager.PERMISSION_DENIED,
        c.checkSelfPermission("android.permission.INTERNET"));
    assertEquals(
        android.content.pm.PackageManager.PERMISSION_DENIED,
        c.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE"));
  }

  /**
   * Controlla che schemi eseguibili, file locali e URL con credenziali non ricevano un’azione di
   * apertura.
   */
  @Test
  public void dangerousScannedLinksHaveNoOpenAction() {
    try (ActivityController<MainActivity> controller =
        Robolectric.buildActivity(MainActivity.class).setup()) {
      MainActivity a = controller.get();
      for (String s :
          new String[] {"javascript:alert(1)", "file:///sdcard/x", "https://good.org@bad.org"})
        assertNull(a.actionFor(new Store.Item(s, "QR_CODE", false)));
      Intent safe = a.actionFor(new Store.Item("https://example.org", "QR_CODE", false));
      assertEquals(Intent.ACTION_VIEW, safe.getAction());
    }
  }

  /** Controlla che un codice telefonico prepari il dialer anziché chiamare direttamente. */
  @Test
  public void phoneActionOnlyOpensDialer() {
    try (ActivityController<MainActivity> c =
        Robolectric.buildActivity(MainActivity.class).setup()) {
      Intent i = c.get().actionFor(new Store.Item("tel:+3906123456", "QR_CODE", false));
      assertEquals(Intent.ACTION_DIAL, i.getAction());
    }
  }

  /**
   * Salva lo stato Android e ricrea l’attività per verificare che il testo non ancora generato
   * sopravviva.
   */
  @Test
  public void textFormSurvivesRecreation() {
    try (ActivityController<MainActivity> c =
        Robolectric.buildActivity(MainActivity.class).setup()) {
      MainActivity a = c.get();
      a.showForm("text", null);
      EditText edit = findEdit(a.findViewById(android.R.id.content));
      assertNotNull(edit);
      edit.setText("My unsaved content");
      Bundle state = new Bundle();
      c.saveInstanceState(state);
      try (ActivityController<MainActivity> recreated =
          Robolectric.buildActivity(MainActivity.class)
              .create(state)
              .start()
              .restoreInstanceState(state)
              .resume()
              .visible()) {
        assertEquals(
            "My unsaved content",
            findEdit(recreated.get().findViewById(android.R.id.content)).getText().toString());
      }
    }
  }

  /**
   * Verifica che disattivare la cronologia lasci temporaneo il codice finché la persona non lo
   * aggiunge ai preferiti.
   */
  @Test
  public void historyOffSavesOnlyWhenUserChoosesFavorite() {
    RuntimeEnvironment.getApplication()
        .getSharedPreferences("settings", 0)
        .edit()
        .putBoolean("history", false)
        .commit();
    try (ActivityController<MainActivity> c =
            Robolectric.buildActivity(MainActivity.class).setup();
        Store db = new Store(c.get())) {
      MainActivity a = c.get();
      a.showForm("text", null);
      findEdit(a.findViewById(android.R.id.content)).setText("Private note");
      findAction(a.findViewById(android.R.id.content), Soft.t(a, "generate")).performClick();
      assertEquals(0, db.list("", 0).size());
      findAction(a.findViewById(android.R.id.content), Soft.t(a, "favorite")).performClick();
      assertEquals(1, db.list("", 1).size());
    }
  }

  /**
   * Invia un Intent di condivisione e controlla che il link venga mostrato senza aprire altre app.
   */
  @Test
  public void sharedTextIsDisplayedWithoutOpeningLink() {
    Intent incoming =
        new Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, "https://example.org/shared");
    try (ActivityController<MainActivity> c =
            Robolectric.buildActivity(MainActivity.class, incoming).setup();
        Store db = new Store(c.get())) {
      assertEquals("https://example.org/shared", db.list("", 0).get(0).text);
      assertNotNull(
          findAction(c.get().findViewById(android.R.id.content), Soft.t(c.get(), "open_link")));
      assertNull(Shadows.shadowOf(c.get()).getNextStartedActivity());
    }
  }

  /**
   * Cerca ricorsivamente il controllo con la descrizione accessibile desiderata, senza dipendere
   * dalle coordinate del layout.
   */
  private View findAction(View view, String text) {
    if (text.contentEquals(
        view.getContentDescription() == null ? "" : view.getContentDescription())) return view;
    if (view instanceof ViewGroup g)
      for (int i = 0; i < g.getChildCount(); i++) {
        View found = findAction(g.getChildAt(i), text);
        if (found != null) return found;
      }
    return null;
  }

  /** Trova ricorsivamente il primo campo modificabile del modulo usato dalla prova. */
  private EditText findEdit(View view) {
    if (view instanceof EditText) return (EditText) view;
    if (view instanceof ViewGroup g)
      for (int i = 0; i < g.getChildCount(); i++) {
        EditText found = findEdit(g.getChildAt(i));
        if (found != null) return found;
      }
    return null;
  }
}
