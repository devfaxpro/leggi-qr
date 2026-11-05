# Licenze e componenti di terze parti

Il codice originale di **Leggi QR** è Copyright © 2026 devfaxpro e usa **GPL-3.0-only**. Questa dichiarazione non sostituisce le licenze delle dipendenze, del wrapper Gradle o dei loro sorgenti inclusi.

| Componente | Versione dichiarata | Avviso e sorgenti |
| --- | --- | --- |
| AndroidX AppCompat | 1.7.1 | Apache-2.0; [Android Open Source Project](https://source.android.com/license) |
| AndroidX CameraX | 1.5.3 | Apache-2.0; [AndroidX](https://android.googlesource.com/platform/frameworks/support/) |
| Material Components Android | 1.13.0 | Apache-2.0; [sorgenti](https://github.com/material-components/material-components-android) |
| ZXing Core | 3.5.4 | Apache-2.0; [sorgenti](https://github.com/zxing/zxing) |
| desugar_jdk_libs | 2.1.5 | Avvisi Apache-2.0 e GPLv2 con Classpath Exception secondo i file di origine; [sorgenti](https://github.com/google/desugar_jdk_libs) |
| Gradle wrapper / distribuzione | 8.13 | Apache-2.0; gli avvisi nei file del wrapper restano invariati |
| JUnit / Robolectric | 4.13.2 / 4.16.1 | Dipendenze dei test: EPL-1.0 / MIT, non incorporate nel codice applicativo dell’APK |

Le dipendenze transitive sono elencate in `third-party/dependencies.txt`. Gli avvisi distribuiti sono in `app/src/main/assets/licenses.txt`, consultabili nell’app, e nei file `third-party/Apache-2.0.txt`, `third-party/desugar-LICENSE.txt` e `third-party/libyuv-LICENSE.txt`.

Il pacchetto completo dei sorgenti della release include la copia sorgente di desugar_jdk_libs 2.1.5. I relativi file conservano le proprie licenze. Il repository non contiene il codice del prototipo Binary Eye abbandonato.

Riferimenti: [testo GNU GPLv3](https://www.gnu.org/licenses/gpl-3.0.html), [istruzioni GNU per applicare la licenza](https://www.gnu.org/licenses/gpl-howto.en.html), [compatibilità Apache-2.0 / GPLv3](https://apache.org/licenses/GPL-compatibility.html).
