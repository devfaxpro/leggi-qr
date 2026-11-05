/*
 * Leggi QR — Copyright (C) 2026 devfaxpro
 * SPDX-License-Identifier: GPL-3.0-only
 * Ridistribuzione e modifica consentite secondo la GNU GPL versione 3.
 * Software fornito senza garanzie; il testo completo è nel file LICENSE.
 */
package app.qrall.scanner;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import com.google.zxing.BarcodeFormat;
import java.util.*;

/** Archivio SQLite locale: salvataggio, deduplicazione, ricerca e gestione dei preferiti. */
public final class Store extends SQLiteOpenHelper {
  /** Elemento della raccolta: testo codificato, formato, provenienza e metadati locali. */
  public static final class Item {
    public long id, time;
    public String text, format, title;
    public boolean favorite, created;

    /**
     * Crea un elemento ancora non salvato: id negativo, titolo vuoto e data corrente. Il flag
     * created distingue i codici generati da quelli letti.
     */
    public Item(String text, String format, boolean created) {
      this.text = text;
      this.format = format;
      this.created = created;
      this.time = System.currentTimeMillis();
      this.id = -1;
      this.title = "";
    }

    /**
     * Converte il nome salvato nel formato ZXing; se il dato non è riconosciuto usa QR_CODE per
     * evitare un arresto della schermata.
     */
    public BarcodeFormat barcodeFormat() {
      try {
        return BarcodeFormat.valueOf(format);
      } catch (Exception e) {
        return BarcodeFormat.QR_CODE;
      }
    }
  }

  /**
   * Apre il database locale della serie 2.x. Nome e versione dello schema restano stabili per
   * conservare la raccolta quando si aggiorna l’APK.
   */
  public Store(Context context) {
    super(context, "qrall-soft.db", null, 1);
  }

  /**
   * Crea tabella e indice al primo utilizzo. L’indice sulla data rende efficiente la
   * visualizzazione dei codici più recenti.
   */
  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(
        "CREATE TABLE codes (id INTEGER PRIMARY KEY, content TEXT NOT NULL, format TEXT NOT NULL,"
            + " title TEXT NOT NULL DEFAULT '', time INTEGER NOT NULL, favorite INTEGER NOT NULL"
            + " DEFAULT 0, created INTEGER NOT NULL DEFAULT 0)");
    db.execSQL("CREATE INDEX codes_time ON codes(time DESC)");
  }

  /**
   * Lo schema è ancora alla versione 1: nessuna migrazione è necessaria. Prima di aumentare la
   * versione, implementare qui una migrazione che preservi i dati.
   */
  @Override
  public void onUpgrade(SQLiteDatabase db, int old, int now) {}

  /**
   * 1. Cerca lo stesso contenuto, formato e provenienza. 2. Conserva titolo e preferito
   * dell’elemento già presente. 3. Inserisce o aggiorna usando parametri SQL. Il blocco
   * sincronizzato evita inserimenti concorrenti nella stessa istanza.
   */
  public synchronized Item save(Item item) {
    // Passo 1: cerca un duplicato usando argomenti separati dalla query SQL.
    try (Cursor c =
        getReadableDatabase()
            .query(
                "codes",
                null,
                "content=? AND format=? AND created=?",
                new String[] {item.text, item.format, item.created ? "1" : "0"},
                null,
                null,
                null,
                "1")) {
      if (c.moveToFirst()) {
        Item old = read(c);
        item.id = old.id;
        // Passo 2: non perdere le scelte manuali quando lo stesso codice viene letto di nuovo.
        item.favorite = old.favorite;
        item.title = old.title;
      }
    }
    // Passo 3: prepara i valori della riga, senza concatenare dati utente nel comando SQL.
    ContentValues v = new ContentValues();
    v.put("content", item.text);
    v.put("format", item.format);
    v.put("title", item.title);
    v.put("time", item.time);
    v.put("favorite", item.favorite ? 1 : 0);
    v.put("created", item.created ? 1 : 0);
    if (item.id < 0) item.id = getWritableDatabase().insertOrThrow("codes", null, v);
    else getWritableDatabase().update("codes", v, "id=?", new String[] {"" + item.id});
    return item;
  }

  /**
   * Applica ricerca letterale e filtro: 0 tutti, 1 preferiti, 2 creati, 3 scansionati. Protegge i
   * caratteri speciali di LIKE e restituisce gli elementi dal più recente.
   */
  public List<Item> list(String query, int filter) {
    String where = "(content LIKE ? ESCAPE '\\' OR title LIKE ? ESCAPE '\\')";
    // Percentuale e underscore devono essere cercati come caratteri, non come jolly SQL.
    String q = "%" + query.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    if (filter == 1) where += " AND favorite=1";
    if (filter == 2) where += " AND created=1";
    if (filter == 3) where += " AND created=0";
    List<Item> rows = new ArrayList<>();
    try (Cursor c =
        getReadableDatabase()
            .query("codes", null, where, new String[] {q, q}, null, null, "time DESC", null)) {
      while (c.moveToNext()) rows.add(read(c));
    }
    return rows;
  }

  /**
   * Salva prima gli elementi temporanei, poi aggiorna soltanto il preferito. La scelta esplicita
   * dell’utente funziona anche con cronologia automatica disattivata.
   */
  public void favorite(Item i, boolean value) {
    // Un preferito esplicito deve esistere nell’archivio anche se la cronologia è spenta.
    if (i.id < 0) save(i);
    i.favorite = value;
    ContentValues v = new ContentValues();
    v.put("favorite", value ? 1 : 0);
    getWritableDatabase().update("codes", v, "id=?", new String[] {"" + i.id});
  }

  /**
   * Rende persistente un elemento temporaneo e ne aggiorna il titolo scelto dall’utente, senza
   * cambiare il contenuto codificato.
   */
  public void rename(Item i, String value) {
    if (i.id < 0) save(i);
    i.title = value;
    ContentValues v = new ContentValues();
    v.put("title", value);
    getWritableDatabase().update("codes", v, "id=?", new String[] {"" + i.id});
  }

  /**
   * Elimina dal database soltanto l’elemento identificato dall’id. I file già esportati non sono
   * coinvolti.
   */
  public void delete(long id) {
    getWritableDatabase().delete("codes", "id=?", new String[] {"" + id});
  }

  /**
   * Svuota la raccolta locale, inclusi i preferiti. La conferma dell’utente viene richiesta dalla
   * schermata prima di arrivare qui.
   */
  public void clear() {
    getWritableDatabase().delete("codes", null, null);
  }

  /**
   * Ricostruisce un elemento dai nomi delle colonne, evitando dipendenze dall’ordine fisico dei
   * campi nel risultato SQL.
   */
  private Item read(Cursor c) {
    Item i =
        new Item(
            c.getString(c.getColumnIndexOrThrow("content")),
            c.getString(c.getColumnIndexOrThrow("format")),
            c.getInt(c.getColumnIndexOrThrow("created")) == 1);
    i.id = c.getLong(c.getColumnIndexOrThrow("id"));
    i.time = c.getLong(c.getColumnIndexOrThrow("time"));
    i.title = c.getString(c.getColumnIndexOrThrow("title"));
    i.favorite = c.getInt(c.getColumnIndexOrThrow("favorite")) == 1;
    return i;
  }
}
