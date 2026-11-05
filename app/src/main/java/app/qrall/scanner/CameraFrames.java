/*
 * Leggi QR — Copyright (C) 2026 devfaxpro
 * SPDX-License-Identifier: GPL-3.0-only
 * Ridistribuzione e modifica consentite secondo la GNU GPL versione 3.
 * Software fornito senza garanzie; il testo completo è nel file LICENSE.
 */
package app.qrall.scanner;

import java.nio.ByteBuffer;

/** Adattamento dei buffer CameraX: elimina il padding e prepara i pixel per il decoder. */
final class CameraFrames {
  /** Classe di utilità: non servono istanze per leggere il piano di luminanza. */
  private CameraFrames() {}

  /**
   * Estrae i campioni Y del fotogramma in una matrice compatta. Rispetta posizione iniziale,
   * distanza fra righe e distanza fra pixel: i buffer della fotocamera possono contenere spazi di
   * allineamento.
   */
  static byte[] luminance(
      ByteBuffer buffer, int width, int height, int rowStride, int pixelStride) {
    byte[] data = new byte[width * height];
    // Passo 1: il piano può iniziare dopo un offset nel buffer, non necessariamente da zero.
    int origin = buffer.position();
    // Passo 2: copia solo i pixel utili, saltando il padding fra righe e fra campioni.
    for (int y = 0; y < height; y++)
      for (int x = 0; x < width; x++)
        data[y * width + x] = buffer.get(origin + y * rowStride + x * pixelStride);
    return data;
  }
}
