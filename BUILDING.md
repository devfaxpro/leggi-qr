# Compilare Leggi QR

## 1. Preparare il progetto

Apri la cartella principale in Android Studio. Servono JDK 17 o 21, Android SDK Platform 36 e Build Tools 36.0.0. Il wrapper incluso scarica Gradle 8.13 e il progetto usa Android Gradle Plugin 8.13.2. Al primo avvio servono connessione e dipendenze dai repository Google/Maven Central; le funzioni dell’app distribuita restano offline.

Puoi clonare il repository o estrarre l’archivio sorgenti della release. Il file `local.properties`, se creato da Android Studio, deve contenere il percorso del tuo SDK e rimanere locale. In alternativa configura `ANDROID_HOME` secondo la tua installazione. Non usare percorsi del computer di un altro sviluppatore.

## 2. Eseguire le prove e creare un APK di sviluppo

Da PowerShell, nella cartella del progetto:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

Su macOS/Linux:

```sh
./gradlew testDebugUnitTest assembleDebug
```

L’APK si trova in `app/build/outputs/apk/debug/app-debug.apk`. Il pacchetto di sviluppo termina con `.debug`, così può convivere con quello pubblico. I test sono in `app/src/test`; i rendering generati sono in `app/build/test-render`. `--offline` funziona soltanto quando tutte le dipendenze necessarie sono già nella cache.

## 3. Creare una release con la propria firma

Configura nell’ambiente locale `ANDROID_KEYFILE`, `ANDROID_KEY_ALIAS`, `ANDROID_STORE_PASSWORD` e `ANDROID_KEY_PASSWORD`, usando una tua chiave. Android Studio offre anche **Build → Generate Signed App Bundle or APK**. Il repository non contiene chiavi o credenziali del proprietario.

```powershell
.\gradlew.bat testDebugUnitTest lintVitalAnalyzeRelease assembleRelease bundleRelease
```

I risultati sono `app/build/outputs/apk/release/app-release.apk` e `app/build/outputs/bundle/release/app-release.aab`. Una copia modificata firmata con una chiave diversa non può aggiornare l’APK ufficiale: usa un application ID distinto o disinstalla la copia precedente dopo aver esportato i dati che vuoi conservare.

Per installare un APK di sviluppo su un dispositivo già collegato e autorizzato:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 4. Modificare le traduzioni

I cataloghi delle prime dieci lingue sono in `tools/generate_translations.py`; gli altri sono in `tools/locales/*.tsv`, separati dal carattere `|`. I codici e nomi nativi sono in `tools/generate_locales.py`. Usa Python 3, senza librerie aggiuntive:

```sh
python tools/generate_translations.py
```

Il generatore legge la versione da Gradle, verifica chiavi e celle, scrive le risorse Android e rigenera `Strings.java`, `LanguageCatalog.java` e la configurazione delle lingue. Le frasi devono mantenere il significato dei messaggi originali; non basta tradurre i pulsanti. I file generati contengono commenti che indicano quale sorgente modificare.

## 5. Dipendenze e licenze

Le versioni delle librerie sono dichiarate in `app/build.gradle`; l’elenco risolto della base distribuita è in `third-party/dependencies.txt`. Il Gradle wrapper è un componente di terze parti con il proprio avviso Apache.

L’archivio sorgenti della release include anche `third-party/desugar-jdk-libs-2.1.5-source.zip`. Questo archivio di terze parti è escluso dall’albero Git per evitare di duplicare un file voluminoso, ma è distribuito insieme ai sorgenti corrispondenti all’APK. Non è necessario per il normale download delle dipendenze effettuato da Gradle.

## 6. Controllare prima di distribuire

Esegui i test, controlla il contenuto dell’APK e la firma con gli strumenti Android SDK, verifica le lingue e prova fotocamera, permessi, importazione e condivisione su telefoni reali. Distribuisci insieme al binario i sorgenti corrispondenti e gli avvisi delle dipendenze. La build della release è stata verificata con JDK 21; non si dichiara la riproducibilità byte per byte del file firmato su ogni ambiente.
