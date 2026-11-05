Leggi QR 2.1.1 — Android, progetto nuovo indipendente

Interfaccia nativa chiara in blu e azzurro: pulsanti, navigazione, schede, moduli e impostazioni coordinati. QR nero nell’icona e nell’illustrazione della home; nero anche come colore iniziale dei codici generati, con quattro colori personalizzabili. Nessun codice applicativo del precedente progetto Binary Eye è utilizzato. CameraX gestisce la fotocamera; ZXing Core legge e genera i codici offline.

FUNZIONI
- Scansione QR e codici a barre da fotocamera; torcia, zoom, messa a fuoco al tocco, camera frontale/posteriore.
- Scansione multipla (fino a 50 codici distinti per sessione), importazione di immagini, riconoscimento di più codici nella stessa immagine e codici invertiti.
- Ricezione di immagini e testo dalla condivisione di altre app. Il contenuto non viene aperto automaticamente.
- Creazione: link, testo Unicode, Wi-Fi (WPA/WPA2, WPA3 SAE, WEP, aperta; rete nascosta), vCard, email, SMS, telefono, posizione, evento con data/ora guidate.
- Generazione barcode: Code 128, EAN-13, EAN-8, Code 39, ITF, PDF417, Aztec, Data Matrix.
- Cronologia locale opzionale, preferiti, ricerca, filtri, nomi personalizzati, eliminazione, esportazione JSON.
- Quattro colori scuri leggibili su bianco, copia, condivisione contenuti/immagini, PNG 1600px, SVG vettoriale.
- Azioni contestuali con conferma: browser, composizione messaggi, dialer, contatti, calendario, mappa e impostazioni Wi-Fi. Per il Wi-Fi copia la password e apre le impostazioni: la scelta della rete è manuale.
- Lingua automatica del sistema, globo per scegliere una delle 40 lingue principali o tornare al sistema.
- Scorciatoie Scan/Crea dall'icona (Android 7.1+); icona B con cornice blu, QR nero e sfondo azzurro; versioni adattiva e monocromatica.

COMPILAZIONE
Aprire questa cartella in Android Studio. JDK 17 o 21, Android SDK 36, Gradle 8.13, Android Gradle Plugin 8.13.2. Minimo Android 7.0 (API 24), target Android 16 (API 36).
Gradle wrapper incluso. Eseguire gradlew testDebugUnitTest lintDebug assembleDebug.
Per un bundle di pubblicazione: gradlew bundleRelease. La firma release usa esclusivamente variabili d'ambiente ANDROID_KEYFILE, ANDROID_KEY_ALIAS, ANDROID_STORE_PASSWORD, ANDROID_KEY_PASSWORD. Non inserire segreti nel codice.
Il parametro facoltativo QRALL_DEBUG_KEYFILE serve soltanto a specificare una chiave di sviluppo personalizzata.

Application ID: app.qrall.scanner. VersionCode 7; versionName 2.1.1. L’app si chiamava Qr all nelle versioni precedenti. Il pacchetto mantiene l'identificatore e la firma delle precedenti consegne, così può aggiornarle. I dati della versione 2.0.0 vengono conservati. La cronologia del vecchio progetto iniziale non viene migrata. Nessun dato esterno viene cancellato.

STRUTTURA
MainActivity.java: navigazione, creazione, dettaglio, cronologia, import/export, lingua.
ScannerActivity.java / CameraFrames.java: ciclo di vita CameraX e lettura luminanza.
Codes.java: generazione, SVG, scansione; Payloads.java: formati QR e validazioni.
Store.java: SQLite; Soft.java: componenti grafici; Strings.java: risorse con riferimenti verificati in compilazione.
Le 145 stringhe sono tradotte in 40 lingue, elencate in LANGUAGES.txt. Traduzioni in app/src/main/res/values*/strings.xml. Il selettore conserva le dieci lingue richieste in testa, poi le altre per nome nativo. Nessun duplicato regionale. Il sistema sceglie la risorsa disponibile più adatta, con inglese come fallback. Sono incluse le lingue RTL.
Per rigenerare i cataloghi: python tools/generate_translations.py (Python 3, solo libreria standard). La verifica del generatore blocca chiavi mancanti, duplicate o vuote. Non è stata eseguita una revisione editoriale madrelingua delle traduzioni.
Licenze di terze parti in app/src/main/assets/licenses.txt, accessibili anche nell'app.

VERIFICA
Suite JUnit/Robolectric inclusa: codici multilingue, vari formati, rotazione/inversione, scansione multipla, Wi-Fi, email, URL, coordinate, eventi, SVG, dati camera con stride, persistenza e preferiti, ricerca, ripristino form, condivisione esterna, permessi e rendering Android.
I rendering automatici sono riproduzioni dell'interfaccia Android. Non sostituiscono una prova della fotocamera su un telefono reale.

PRIVACY E LIMITI
Nessun permesso Internet, account, pubblicità o analytics. Nessuna sincronizzazione cloud né servizio di QR dinamici: i codici sono statici e funzionano offline. Le altre app usate su richiesta dell'utente possono usare Internet. Cronologia e preferiti rimangono nei dati locali dell'app. I file esportati appartengono all'utente e restano nella destinazione scelta.
La robustezza su fotocamere reali, tutte le condizioni di luce e tutti i modelli Android richiede il collaudo su dispositivi. Le traduzioni sono complete, ma una revisione editoriale madrelingua prima del lancio è consigliata.

LICENZA E LEGGIBILITÀ
Codice applicativo GPL-3.0-only. Vedere LICENSE e THIRD_PARTY_NOTICES.md. Commenti italiani sulle responsabilità e sui passaggi dei metodi; guida in docs/GUIDA-AL-CODICE.md. Le licenze dei componenti di terze parti restano quelle originali.
