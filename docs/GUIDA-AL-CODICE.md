# Leggere il codice passo passo

Il progetto separa contenuti QR, riconoscimento, dati e interfaccia. I metodi sono documentati in italiano; nei flussi più lunghi i commenti numerati spiegano l’ordine delle operazioni e il motivo dei controlli.

## 1. Parti dal contenuto, senza Android

In `Payloads.java`, `fields` sceglie i campi del modulo e `build` converte i valori nel testo da inserire nel codice. I casi Wi-Fi, vCard, email, posizione ed evento hanno escape e validazioni specifiche. Un evento viene interpretato nell’ora locale e convertito in UTC. Nessuna di queste funzioni apre app esterne.

In `Codes.java`, `encode` trasforma il testo in una matrice di moduli chiari e scuri. `decode` compie il percorso inverso, provando luminosità invertita e rotazione se necessario. `svg` traduce la matrice in forme vettoriali senza inserire il testo originale nel markup.

Leggi poi `CodesTest.java`: gli esempi verificano round trip, più codici nella stessa immagine, scritture Unicode, input non validi e geometria dei buffer camera.

## 2. Segui un elemento nella raccolta

`Store.Item` descrive contenuto, formato, data, titolo, preferito e provenienza. Un id negativo significa che l’elemento è temporaneo. `Store.save` cerca un duplicato e conserva le scelte manuali; `favorite` e `rename` rendono persistente anche un elemento temporaneo quando l’utente lo richiede.

La ricerca usa parametri SQL ed escape dei caratteri speciali di `LIKE`. Il database è locale, con schema versione 1. La versione dell’app può aumentare senza cambiare lo schema: prima di modificarlo serve una migrazione esplicita in `onUpgrade`.

## 3. Leggi la navigazione

`MainActivity.onCreate` apre archivio e preferenze, costruisce la schermata e recupera lo stato Android. `showTab` sceglie una delle quattro sezioni. `head` mantiene il globo disponibile; `Soft.java` fornisce i componenti grafici comuni.

Per seguire la creazione usa questa sequenza: `creator` → `showForm` → `generate` → `Payloads.build` → `Codes.encode` → `showDetail`. La cronologia viene aggiornata solo quando abilitata o quando viene richiesta esplicitamente una conservazione, per esempio con un preferito.

## 4. Segui una scansione

`ScannerActivity` richiede il permesso e collega anteprima e analisi CameraX al ciclo di vita. `analyze` estrae il piano di luminanza tramite `CameraFrames.luminance`, legge i codici e chiude sempre il fotogramma nel blocco `finally`.

La lettura avviene su un thread dedicato; `accept` aggiorna la UI sul thread principale. La mappa dei risultati elimina i duplicati. In modalità multipla raccoglie al massimo 50 codici; in modalità singola restituisce subito il risultato. `receiveScan` nella schermata principale applica le preferenze di salvataggio e apre il dettaglio.

## 5. Importazione, esportazione e azioni esterne

`importImage` legge prima le dimensioni della foto, riduce le immagini molto grandi e lavora fuori dal thread UI. Prima di mostrare l’esito controlla che l’attività sia ancora valida. Gli errori, compresa la memoria insufficiente, ripristinano lo stato dell’importazione.

`export` prepara PNG o SVG nella cache. La condivisione passa da FileProvider con un permesso temporaneo; il salvataggio passa dal selettore documenti Android. `exportHistory` avverte che il JSON può contenere credenziali Wi-Fi e richiede una conferma.

`actionFor` prepara un Intent ma non lo esegue. `confirmAction` mostra il contenuto e `safeStart` gestisce l’eventuale assenza di un’app compatibile. Per i link sono ammessi HTTP e HTTPS senza credenziali; per il telefono si apre il dialer.

## 6. Lingue e stato

`showLanguages` mostra prima l’opzione di sistema, poi i 40 nomi nativi. Una lista locale vuota restituita ad AppCompat significa “segui il sistema”. Android 13 e successivi possono mostrare le lingue anche nelle impostazioni dell’app; AppCompat gestisce le versioni precedenti.

`onSaveInstanceState` conserva campi del modulo, navigazione e selezioni durante la ricreazione dell’attività. Le preferenze persistenti, come cronologia e vibrazione, sono separate dallo stato temporaneo della schermata.

## 7. Come mantenere i commenti utili

- Descrivi responsabilità, ordine delle operazioni e ragioni di una scelta, non soltanto la sintassi.
- Quando cambi il comportamento, aggiorna commenti, guida e prove coinvolte.
- Per file generati modifica il generatore; le rigenerazioni devono mantenere spiegazioni e licenza.
- Conserva le attribuzioni delle dipendenze e non riscrivere i loro avvisi come se fossero codice originale.
- I test su JVM e i rendering non dimostrano il funzionamento di una fotocamera fisica: riporta separatamente ciò che è stato provato sul dispositivo.
