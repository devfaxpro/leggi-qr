/*
 * Leggi QR — Copyright (C) 2026 devfaxpro
 * SPDX-License-Identifier: GPL-3.0-only
 * Ridistribuzione e modifica consentite secondo la GNU GPL versione 3.
 * Software fornito senza garanzie; il testo completo è nel file LICENSE.
 */
package app.qrall.scanner;

import static org.junit.Assert.*;

import com.google.zxing.*;
import com.google.zxing.client.result.*;
import com.google.zxing.common.BitMatrix;
import java.util.*;
import org.junit.Test;

/** Prove del motore offline con dati sintetici, formati diversi e input non validi. */
public class CodesTest {
  /**
   * Simula righe con padding, pixel distanziati e offset iniziale, verificando l’estrazione della
   * luminanza.
   */
  @Test
  public void cameraPlaneStridesAndBufferOffsetAreHandled() {
    byte[] padded = {99, 10, 99, 20, 99, 88, 88, 30, 99, 40};
    java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(padded);
    buffer.position(1);
    assertArrayEquals(new byte[] {10, 20, 30, 40}, CameraFrames.luminance(buffer, 2, 2, 6, 2));
    assertEquals(1, buffer.position());
  }

  /** Trasforma una matrice di prova in luminanza e richiama il decoder applicativo. */
  private List<Result> decode(BitMatrix m, boolean inverted, boolean rotate) {
    int w = m.getWidth(), h = m.getHeight();
    int[] p = new int[w * h];
    for (int y = 0; y < h; y++)
      for (int x = 0; x < w; x++) {
        int at = rotate ? x * h + h - 1 - y : y * w + x;
        p[at] = (m.get(x, y) ^ inverted) ? 0xff294C3B : 0xffffffff;
      }
    return Codes.decodePixels(p, rotate ? h : w, rotate ? w : h);
  }

  /** Genera e rilegge campioni in dieci scritture per verificare il testo Unicode nel QR. */
  @Test
  public void qrRoundTripInTenLanguages() throws Exception {
    for (String text :
        new String[] {
          "Caffè e possibilità",
          "Hello world",
          "¡Hola! ¿Qué tal?",
          "Olá, conexão",
          "À bientôt",
          "Grüße für dich",
          "你好，世界",
          "こんにちは世界",
          "안녕하세요",
          "Привет, мир"
        }) {
      List<Result> found = decode(Codes.encode(text, BarcodeFormat.QR_CODE, 480), false, false);
      assertEquals(text, found.get(0).getText());
    }
  }

  /** Verifica la lettura di un QR con luminosità invertita. */
  @Test
  public void invertedQrIsRecognized() throws Exception {
    assertEquals(
        "invert",
        decode(Codes.encode("invert", BarcodeFormat.QR_CODE, 350), true, false).get(0).getText());
  }

  /** Verifica che il tentativo ruotato riconosca un codice a barre verticale. */
  @Test
  public void rotatedLinearBarcodeIsRecognized() throws Exception {
    assertEquals(
        "QrAll-123",
        decode(Codes.encode("QrAll-123", BarcodeFormat.CODE_128, 600), false, true)
            .get(0)
            .getText());
  }

  /** Genera e rilegge un campione per ogni formato supportato dal menu. */
  @Test
  public void supportedGeneratedFormatsRoundTrip() throws Exception {
    BarcodeFormat[] formats = {
      BarcodeFormat.QR_CODE,
      BarcodeFormat.CODE_128,
      BarcodeFormat.EAN_13,
      BarcodeFormat.EAN_8,
      BarcodeFormat.CODE_39,
      BarcodeFormat.ITF,
      BarcodeFormat.PDF_417,
      BarcodeFormat.AZTEC,
      BarcodeFormat.DATA_MATRIX
    };
    String[] values = {
      "test",
      "CODE128",
      "5901234123457",
      "96385074",
      "HELLO123",
      "123456",
      "Pdf test",
      "Aztec test",
      "Data matrix"
    };
    for (int i = 0; i < formats.length; i++) {
      List<Result> found = decode(Codes.encode(values[i], formats[i], 600), false, false);
      assertFalse(formats[i].name(), found.isEmpty());
      assertEquals(formats[i].name(), values[i], found.get(0).getText());
    }
  }

  /** Compone due QR nella stessa immagine e verifica che entrambi vengano trovati. */
  @Test
  public void multipleCodesInOneImage() throws Exception {
    BitMatrix a = Codes.encode("left", BarcodeFormat.QR_CODE, 300),
        b = Codes.encode("right", BarcodeFormat.QR_CODE, 300),
        joined = new BitMatrix(720, 340);
    for (int y = 0; y < 300; y++)
      for (int x = 0; x < 300; x++) {
        if (a.get(x, y)) joined.set(x + 10, y + 20);
        if (b.get(x, y)) joined.set(x + 400, y + 20);
      }
    List<Result> results = decode(joined, false, false);
    Set<String> texts = new HashSet<>();
    for (Result r : results) texts.add(r.getText());
    assertEquals(Set.of("left", "right"), texts);
  }

  /**
   * Controlla che separatori e backslash di una rete Wi-Fi sopravvivano alla codifica e
   * interpretazione.
   */
  @Test
  public void wifiEscapesRoundTrip() {
    String ssid = "Café;guest:01\\x", pass = "a;:b\\c\"";
    String payload = Payloads.build("wifi", new String[] {ssid, pass}, "WPA", true);
    WifiParsedResult parsed =
        (WifiParsedResult)
            ResultParser.parseResult(new Result(payload, null, null, BarcodeFormat.QR_CODE));
    assertEquals(ssid, parsed.getSsid());
    assertEquals(pass, parsed.getPassword());
    assertTrue(parsed.isHidden());
  }

  /** Controlla l’encoding UTF-8 di oggetto e corpo del messaggio email. */
  @Test
  public void emailUtf8Body() {
    String s =
        Payloads.build(
            "email", new String[] {"a@example.org", "Caffè & tè", "こんにちは + ciao"}, "", false);
    EmailAddressParsedResult p =
        (EmailAddressParsedResult)
            ResultParser.parseResult(new Result(s, null, null, BarcodeFormat.QR_CODE));
    assertEquals("Caffè & tè", p.getSubject());
    assertEquals("こんにちは + ciao", p.getBody());
  }

  /** Verifica l’aggiunta di HTTPS a un indirizzo senza schema. */
  @Test
  public void httpUrlGetsSafeScheme() {
    assertEquals("https://example.org/a?q=1", Payloads.web("example.org/a?q=1"));
  }

  /** Prova URL non ammessi e richiede un errore di validazione per ciascuno. */
  @Test
  public void invalidUrlsAreRejected() {
    for (String s :
        new String[] {
          "javascript:alert(1)",
          "https://trusted.org@evil.org",
          "file:///etc/test",
          "a b",
          "https://"
        }) assertThrows(IllegalArgumentException.class, () -> Payloads.web(s));
  }

  /** Verifica il rifiuto delle coordinate fuori intervallo o non finite. */
  @Test
  public void geoRejectsInvalidCoordinates() {
    for (String lat : new String[] {"91", "NaN", "Infinity"})
      assertThrows(
          IllegalArgumentException.class,
          () -> Payloads.build("location", new String[] {lat, "12"}, "", false));
  }

  /** Controlla che un evento valido produca data iniziale e finale interpretabili. */
  @Test
  public void eventHasValidStartAndEnd() {
    String event =
        Payloads.build(
            "event",
            new String[] {"Cena", "2026-12-31 18:30", "2026-12-31 20:00", "Roma", "Auguri"},
            "",
            false);
    CalendarParsedResult parsed =
        (CalendarParsedResult)
            ResultParser.parseResult(new Result(event, null, null, BarcodeFormat.QR_CODE));
    assertEquals(90 * 60 * 1000, parsed.getEndTimestamp() - parsed.getStartTimestamp());
  }

  /**
   * Prova date impossibili e fine antecedente all’inizio per verificare la validazione rigorosa.
   */
  @Test
  public void eventRejectsInvalidDateAndOrder() {
    assertThrows(
        RuntimeException.class,
        () ->
            Payloads.build(
                "event",
                new String[] {"x", "2026-02-30 10:00", "2026-03-01 10:00", "", ""},
                "",
                false));
    assertThrows(
        RuntimeException.class,
        () ->
            Payloads.build(
                "event",
                new String[] {"x", "2026-12-31 20:00", "2026-12-31 19:00", "", ""},
                "",
                false));
  }

  /** Richiede che la generazione rifiuti una cifra di controllo errata. */
  @Test
  public void barcodeRejectsWrongChecksum() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Codes.encode("5901234123450", BarcodeFormat.EAN_13, 600));
  }

  /**
   * Verifica struttura vettoriale e assenza di immagini incorporate o contenuti eseguibili
   * nell’SVG.
   */
  @Test
  public void svgContainsOnlyVectorShapes() throws Exception {
    String svg = Codes.svg(Codes.encode("<script> & text", BarcodeFormat.QR_CODE, 0), 0xff294c3b);
    assertTrue(svg.contains("#294c3b"));
    assertTrue(svg.endsWith("</svg>"));
    assertFalse(svg.contains("<script>"));
    assertFalse(svg.contains("<image"));
  }
}
