# Leggi QR

Un’app Android per **leggere, creare e conservare QR e codici a barre**, con interfaccia chiara blu, QR neri e funzionamento offline. Nessun account, pubblicità o permesso Internet.

[English](README.en.md) · [Scarica APK e sorgenti](https://github.com/devfaxpro/leggi-qr/releases/tag/v2.1.1) · [Guida al codice](docs/GUIDA-AL-CODICE.md) · [Compilazione](BUILDING.md)

![Icona di Leggi QR](docs/icon.png)

## Download e installazione

- **[Leggi QR 2.1.1 — APK firmato](https://github.com/devfaxpro/leggi-qr/releases/download/v2.1.1/Leggi-QR-2.1.1.apk)** per Android 7.0 e successivi.
- **[Sorgenti corrispondenti all’APK](https://github.com/devfaxpro/leggi-qr/releases/download/v2.1.1/Leggi-QR-2.1.1-AndroidStudio.zip)**, completi di progetto Android Studio, generatori, traduzioni, test e copia sorgente di desugar_jdk_libs.
- **[Impronte SHA-256](https://github.com/devfaxpro/leggi-qr/releases/download/v2.1.1/SHA256-Leggi-QR-2.1.1.txt)** per controllare i file scaricati.

Scarica l’APK sul telefono, aprilo e autorizza l’installazione dalla sorgente scelta quando Android lo richiede. L’AAB è destinato alla distribuzione tramite Google Play e non si installa direttamente come un APK.

L’identificatore `app.qrall.scanner` e la firma delle versioni precedenti sono conservati. La serie 2.x usa lo stesso archivio locale; il precedente prototipo abbandonato non viene migrato.

## Funzioni

- Scanner con torcia, zoom, messa a fuoco al tocco, cambio fotocamera e lettura multipla fino a 50 codici.
- Importazione da immagini e ricezione di immagini o testo condivisi da altre app.
- Creazione di QR per link, testo, Wi-Fi, contatti, email, SMS, telefono, posizione ed eventi.
- Generazione Code 128, EAN-13, EAN-8, Code 39, ITF, PDF417, Aztec e Data Matrix.
- Cronologia facoltativa, preferiti, ricerca, filtri, rinomina e esportazione JSON.
- Esportazione PNG e SVG, copia e condivisione esplicita dei contenuti.
- 40 lingue con selezione automatica dal sistema e cambio manuale dal globo. [Elenco completo](LANGUAGES.txt).
- Supporto delle lingue RTL, icona adattiva e variante monocromatica Android.

## Codice leggibile e commentato

La versione 2.1.1 documenta in italiano responsabilità, metodi e passaggi della logica, comprese validazioni, gestione degli errori e uso dei thread. La [guida passo passo](docs/GUIDA-AL-CODICE.md) spiega da quali file iniziare. Le regole per mantenere questa impostazione sono in [AGENTS.md](AGENTS.md).

## Privacy

La lettura e la generazione avvengono sul dispositivo. I fotogrammi non vengono archiviati. La cronologia rimane nei dati dell’app e può essere cancellata; le esportazioni restano nella posizione scelta. L’app non legge la posizione del telefono. Link, dialer, contatti e altre app vengono aperti solo dopo una conferma. Le app esterne seguono le proprie impostazioni e possono usare Internet.

Il JSON esportato contiene il testo dei codici, comprese eventuali password Wi-Fi: il dialogo di esportazione lo segnala prima del salvataggio.

## Verifiche e limiti

La release comprende 36 test JUnit/Robolectric, verifica delle 145 chiavi in ciascuna delle 40 lingue, controlli della firma e rendering Android in più scritture. Gli esiti della build distribuita sono nella release. I rendering non sostituiscono prove della fotocamera su dispositivi fisici. La revisione editoriale madrelingua delle traduzioni non è stata eseguita.

## Licenza

Copyright © 2026 **devfaxpro**. Il codice originale di Leggi QR è distribuito sotto **GNU GPL versione 3 soltanto**, identificatore `GPL-3.0-only`. Puoi usarlo, studiarlo, modificarlo e ridistribuirlo secondo [LICENSE](LICENSE). È fornito senza garanzie.

Le dipendenze e il Gradle wrapper mantengono le rispettive licenze e attribuzioni: vedere [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md). Il testo GPL e gli avvisi delle librerie sono consultabili anche nell’app, dalle impostazioni.
