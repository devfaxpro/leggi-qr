/*
 * Leggi QR — Copyright (C) 2026 devfaxpro
 * SPDX-License-Identifier: GPL-3.0-only
 * Ridistribuzione e modifica consentite secondo la GNU GPL versione 3.
 * Software fornito senza garanzie; il testo completo è nel file LICENSE.
 */
package app.qrall.scanner;

import com.google.zxing.BarcodeFormat;
import java.net.URLEncoder;
import java.time.*;
import java.time.format.*;
import java.util.*;

/** Costruzione e validazione del testo dei codici, separata dall’interfaccia e dalla fotocamera. */
public final class Payloads {
  public static final String[] TYPES = {
    "url", "text", "wifi", "contact", "email", "sms", "phone", "location", "event", "barcode"
  };

  /**
   * Restituisce le chiavi dei campi richiesti dal tipo scelto. L’ordine deve coincidere con gli
   * indici usati da build e con il modulo mostrato all’utente.
   */
  public static String[] fields(String type) {
    // Le chiavi dei campi saranno tradotte dalla UI; qui rimangono identificatori stabili.
    return switch (type) {
      case "url" -> new String[] {"url_field"};
      case "wifi" -> new String[] {"network", "password"};
      case "contact" -> new String[] {"name", "phone_field", "email_field", "company", "website"};
      case "email" -> new String[] {"email_field", "subject", "message"};
      case "sms" -> new String[] {"phone_field", "message"};
      case "phone" -> new String[] {"phone_field"};
      case "location" -> new String[] {"latitude", "longitude"};
      case "event" -> new String[] {"event_title", "start", "end", "place", "message"};
      default -> new String[] {"content"};
    };
  }

  /**
   * 1. Rimuove spazi esterni e controlla i campi obbligatori. 2. Valida i dati specifici del tipo.
   * 3. Produce il testo interoperabile da codificare nel QR; non apre app né esegue azioni esterne.
   */
  public static String build(String type, String[] f, String security, boolean hidden) {
    // Passo 1: normalizza gli spazi esterni prima di verificare il campo principale.
    for (int i = 0; i < f.length; i++) f[i] = f[i].trim();
    if (f[0].isEmpty()) throw new IllegalArgumentException("required");
    return switch (type) {
      case "url" -> web(f[0]);
      case "wifi" -> {
        // Per una rete protetta la password è obbligatoria; per una rete aperta può essere vuota.
        if (!security.equals("nopass") && f[1].isEmpty())
          throw new IllegalArgumentException("required");
        yield "WIFI:T:"
            + security
            + ";S:"
            + wifi(f[0])
            + ";P:"
            + wifi(f[1])
            + ";H:"
            + hidden
            + ";;";
      }
      case "contact" ->
          "BEGIN:VCARD\r\nVERSION:3.0\r\nFN:"
              + vcard(f[0])
              + "\r\nTEL:"
              + vcard(f[1])
              + "\r\nEMAIL:"
              + vcard(f[2])
              + "\r\nORG:"
              + vcard(f[3])
              + "\r\nURL:"
              + vcard(f[4])
              + "\r\nEND:VCARD";
      case "email" -> {
        if (!f[0].matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
          throw new IllegalArgumentException("invalid");
        yield "mailto:" + f[0] + "?subject=" + url(f[1]) + "&body=" + url(f[2]);
      }
      case "sms" -> "SMSTO:" + phone(f[0]) + ":" + f[1];
      case "phone" -> "tel:" + phone(f[0]);
      case "location" -> {
        // Rifiuta anche NaN e infinito: il solo confronto con i limiti non basta per NaN.
        double lat = Double.parseDouble(f[0]), lon = Double.parseDouble(f[1]);
        if (!Double.isFinite(lat)
            || !Double.isFinite(lon)
            || Math.abs(lat) > 90
            || Math.abs(lon) > 180) throw new IllegalArgumentException("invalid");
        yield "geo:" + f[0] + "," + f[1];
      }
      case "event" -> {
        // Passo 2: il calendario usa parsing rigoroso, così date impossibili non vengono corrette
        // implicitamente.
        DateTimeFormatter local =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);
        LocalDateTime start = LocalDateTime.parse(f[1], local),
            end = LocalDateTime.parse(f[2], local);
        if (!end.isAfter(start)) throw new IllegalArgumentException("invalid");
        // Passo 3: converte l’ora locale in UTC nel contenuto VEVENT.
        DateTimeFormatter stamp =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
        yield "BEGIN:VEVENT\r\nSUMMARY:"
            + vcard(f[0])
            + "\r\nDTSTART:"
            + stamp.format(start.atZone(ZoneId.systemDefault()))
            + "\r\nDTEND:"
            + stamp.format(end.atZone(ZoneId.systemDefault()))
            + "\r\nLOCATION:"
            + vcard(f[3])
            + "\r\nDESCRIPTION:"
            + vcard(f[4])
            + "\r\nEND:VEVENT";
      }
      default -> f[0];
    };
  }

  /**
   * Accetta soltanto HTTP/HTTPS, aggiungendo HTTPS se manca lo schema. Rifiuta host assente e
   * credenziali nell’URL, per evitare collegamenti ambigui o schemi eseguibili.
   */
  public static String web(String raw) {
    // Non trasformare schemi espliciti non web in collegamenti apparentemente validi.
    if (raw.matches("(?i)^[a-z][a-z0-9+.-]*:.*") && !raw.matches("(?i)^https?://.*"))
      throw new IllegalArgumentException("invalid");
    String s = raw.matches("(?i)^https?://.*") ? raw : "https://" + raw;
    try {
      java.net.URI u = new java.net.URI(s);
      // Richiedi un host reale ed escludi la parte utente: evita ambiguità come
      // sito-buono@altro-sito.
      if (u.getHost() == null
          || u.getRawUserInfo() != null
          || !Set.of("http", "https").contains(u.getScheme().toLowerCase(Locale.ROOT)))
        throw new Exception();
    } catch (Exception e) {
      throw new IllegalArgumentException("invalid");
    }
    return s;
  }

  /**
   * Controlla lunghezza e caratteri ammessi nel numero. La validazione prepara un contenuto
   * telefonico, senza effettuare chiamate.
   */
  private static String phone(String s) {
    if (!s.matches("[+0-9() .-]{3,40}")) throw new IllegalArgumentException("invalid");
    return s;
  }

  /**
   * Protegge i separatori del formato Wi-Fi. Il backslash viene sostituito per primo, così i
   * caratteri di escape aggiunti dopo non vengono raddoppiati.
   */
  public static String wifi(String s) {
    return s.replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(":", "\\:")
        .replace(",", "\\,")
        .replace("\"", "\\\"");
  }

  /**
   * Protegge separatori e interruzioni di riga per i campi vCard e calendario; il testo dell’utente
   * rimane all’interno del proprio campo.
   */
  private static String vcard(String s) {
    return s.replace("\\", "\\\\")
        .replace("\r", "")
        .replace("\n", "\\n")
        .replace(";", "\\;")
        .replace(",", "\\,");
  }

  /**
   * Codifica un valore come parametro URL in UTF-8. L’assenza di UTF-8 sarebbe un errore della
   * piattaforma, non un dato utente da correggere.
   */
  private static String url(String s) {
    try {
      return URLEncoder.encode(s, "UTF-8");
    } catch (java.io.UnsupportedEncodingException impossible) {
      throw new AssertionError(impossible);
    }
  }

  /**
   * Mappa la posizione del selettore nel formato ZXing. Per tutti i tipi diversi da barcode
   * restituisce QR_CODE; mantenere l’ordine allineato al menu.
   */
  public static BarcodeFormat format(String type, int position) {
    return !type.equals("barcode")
        ? BarcodeFormat.QR_CODE
        : new BarcodeFormat[] {
              BarcodeFormat.CODE_128,
              BarcodeFormat.EAN_13,
              BarcodeFormat.EAN_8,
              BarcodeFormat.CODE_39,
              BarcodeFormat.ITF,
              BarcodeFormat.PDF_417,
              BarcodeFormat.AZTEC,
              BarcodeFormat.DATA_MATRIX
            }
            [position];
  }
}
