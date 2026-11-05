/*
 * Leggi QR — Copyright (C) 2026 devfaxpro
 * SPDX-License-Identifier: GPL-3.0-only
 * Ridistribuzione e modifica consentite secondo la GNU GPL versione 3.
 * Software fornito senza garanzie; il testo completo è nel file LICENSE.
 */
package app.qrall.scanner;

import com.google.zxing.*;
import com.google.zxing.common.*;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.util.*;

/** Motore offline per QR e codici a barre. Non dipende dall’interfaccia Android. */
public final class Codes {
  /** Impedisce di creare istanze: codifica e lettura non hanno stato condiviso. */
  private Codes() {}

  /**
   * 1. Imposta testo UTF-8 e margine libero attorno al codice. 2. Per i QR usa correzione degli
   * errori alta. 3. Delega a ZXing la matrice; lascia propagare gli errori di formato al chiamante.
   */
  public static BitMatrix encode(String content, BarcodeFormat format, int size)
      throws WriterException {
    Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
    // Passo 1: preserva tutte le scritture supportate usando UTF-8.
    hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
    // Il margine libero separa il codice dall’esterno e facilita la rilettura.
    hints.put(EncodeHintType.MARGIN, format == BarcodeFormat.QR_CODE ? 4 : 12);
    if (format == BarcodeFormat.QR_CODE)
      hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
    return new MultiFormatWriter()
        .encode(content, format, size, format == BarcodeFormat.QR_CODE ? size : size / 2, hints);
  }

  /**
   * 1. Crea una superficie bianca con dimensioni della matrice. 2. Raggruppa i pixel scuri contigui
   * in segmenti orizzontali. 3. Scrive solo forme vettoriali: il contenuto del QR non diventa
   * markup eseguibile.
   */
  public static String svg(BitMatrix matrix, int color) {
    StringBuilder out =
        new StringBuilder(
            "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 "
                + matrix.getWidth()
                + " "
                + matrix.getHeight()
                + "\" shape-rendering=\"crispEdges\"><rect width=\"100%\" height=\"100%\""
                + " fill=\"white\"/><path fill=\"#"
                + String.format(Locale.ROOT, "%06x", color & 0xffffff)
                + "\" d=\"");
    // Passo 2: compatta una sequenza di moduli scuri in un solo rettangolo SVG.
    for (int y = 0; y < matrix.getHeight(); y++)
      for (int x = 0; x < matrix.getWidth(); x++) {
        if (!matrix.get(x, y)) continue;
        int start = x;
        while (x + 1 < matrix.getWidth() && matrix.get(x + 1, y)) x++;
        out.append('M')
            .append(start)
            .append(' ')
            .append(y)
            .append('h')
            .append(x - start + 1)
            .append("v1H")
            .append(start)
            .append('z');
      }
    return out.append("\"/></svg>").toString();
  }

  /**
   * Converte i pixel RGB in luminanza, dando più peso al verde, poi usa la stessa lettura offline
   * impiegata dalla fotocamera.
   */
  public static List<Result> decodePixels(int[] pixels, int width, int height) {
    byte[] gray = new byte[pixels.length];
    for (int i = 0; i < pixels.length; i++) {
      int p = pixels[i];
      gray[i] = (byte) ((((p >> 16) & 255) + 2 * ((p >> 8) & 255) + (p & 255)) / 4);
    }
    return decode(gray, width, height);
  }

  /**
   * 1. Cerca più codici nella luminanza originale e invertita. 2. Se non trova risultati, riprova
   * dopo una rotazione di 90 gradi. 3. Elimina i duplicati mantenendo l’ordine di rilevamento.
   */
  public static List<Result> decode(byte[] gray, int width, int height) {
    Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
    hints.put(DecodeHintType.TRY_HARDER, true);
    hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
    // La chiave contiene formato e contenuto: lo stesso testo in due formati resta distinto.
    LinkedHashMap<String, Result> results = new LinkedHashMap<>();
    for (int rotation = 0; rotation < 2; rotation++) {
      LuminanceSource source =
          new PlanarYUVLuminanceSource(gray, width, height, 0, 0, width, height, false);
      // Passo 1: prova sia fondo chiaro sia fondo scuro prima di ruotare.
      for (int inverse = 0; inverse < 2; inverse++) {
        BinaryBitmap bitmap =
            new BinaryBitmap(new HybridBinarizer(inverse == 0 ? source : source.invert()));
        try {
          Result[] found =
              new GenericMultipleBarcodeReader(new MultiFormatReader())
                  .decodeMultiple(bitmap, hints);
          for (Result r : found) results.put(r.getBarcodeFormat() + ":" + r.getText(), r);
        } catch (NotFoundException ignored) {
        }
      }
      if (!results.isEmpty()) break;
      // Passo 2: ruota solo quando nessun tentativo precedente ha prodotto risultati.
      byte[] rotated = new byte[gray.length];
      for (int y = 0; y < height; y++)
        for (int x = 0; x < width; x++) rotated[x * height + height - 1 - y] = gray[y * width + x];
      gray = rotated;
      int swap = width;
      width = height;
      height = swap;
    }
    return new ArrayList<>(results.values());
  }
}
