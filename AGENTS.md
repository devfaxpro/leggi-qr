# Regole per contribuire a Leggi QR

- Scrivere codice leggibile e ben organizzato. Usare nomi significativi, formattazione uniforme e commenti in italiano su classi, metodi e passaggi non ovvi, incluse validazioni ed errori.
- Nei flussi complessi spiegare i passaggi in ordine. Aggiornare le spiegazioni quando cambia il comportamento; evitare commenti che ripetono inutilmente la sintassi.
- Applicare GPL-3.0-only al codice originale e mantenere le attribuzioni delle dipendenze.
- Conservare application ID, database e compatibilità delle preferenze. Non introdurre cancellazioni di dati per una semplice nuova versione.
- Conservare tema chiaro blu, QR neri, globo delle lingue e funzionamento offline.
- Mantenere tutte le 40 lingue del catalogo, l’ordine delle prime dieci e i nomi nativi. Modificare i generatori per i file generati e controllare le chiavi con `python tools/generate_translations.py`.
- Non pubblicare chiavi, credenziali, `local.properties`, cache o log del computer di sviluppo.
- Eseguire i test pertinenti e verificare firma e metadati dei binari distribuiti. Distinguere i test JVM/rendering dal collaudo su dispositivo fisico.
