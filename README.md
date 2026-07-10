# GuessTheWord_G1

**GuessTheWord_G1** è un'applicazione accademica client-server scritta in **Java 8** e basata sul framework **JavaFX** per l'interfaccia grafica e **SQLite** per la persistenza dei dati. Il gioco consiste in una sfida in tempo reale tra due utenti connessi che devono indovinare una parola nascosta cifrata (tramite Cifrario di Cesare) nel minor tempo possibile.

---

## 👥 Autori e Ruoli
Il progetto è stato sviluppato dal team composto da:
* **Carmine Muollo** — *Senior Java Architect & Database Engineer*
* **William Menza** — *Frontend UI/UX Designer & Client Controller Lead*
* **Sabrina Soriano** — *Network Protocol Specialist & Server Connection Developer*
* **Davide Andrea Odierna** — *Gameplay Concurrency Engineer & Game Session Lead*

* **Versione attuale**: `2.0.0`
* **Release Tag riferito**: `v2.0.0`

---

## 🔑 Credenziali per il Testing
Per agevolare le operazioni di test da parte del docente, sono pre-registrati all'interno del database i seguenti account:

### 🎮 Account Giocatori (Client)
1. **Utente 1**:
   - **Username**: `User1`
   - **Password**: `User123`
2. **Utente 2**:
   - **Username**: `User2`
   - **Password**: `User456`

### 🛠️ Account Amministratore (Server)
- **Username**: `admin`
- **Password**: `admin`

---

## 🏗️ Architettura del Progetto

Il progetto è suddiviso in due moduli principali:
1. **GuessTheWord_Server**: 
   - Gestisce le sessioni di gioco attive (`GameSession`), l'accoppiamento dei giocatori in lobby in base alla difficoltà scelta, l'analisi asincrona dei documenti di testo (tramite JavaFX Service e Task in background) per determinare le parole chiave da proporre come sfide, e la persistenza dei dati sul database SQLite.
2. **GuessTheWord_Client**: 
   - Consente agli utenti di autenticarsi, registrarsi, scegliere la difficoltà, attendere un avversario, giocare in tempo reale visualizzando il progresso e consultare lo storico personale dei propri match.

---

## ⚡ Caratteristiche del Refactoring (Versione 2.0.0)

Nelle fasi di evoluzione verso la versione `2.0.0`, sono state implementate importanti ottimizzazioni architetturali:
* **Ottimizzazione del Driver Database**: Il driver SQLite viene ora caricato in un blocco di inizializzazione statico thread-safe una sola volta all'avvio, abbattendo l'overhead di connessione.
* **Sicurezza DDL/DML**: Rimosse le vulnerabilità di SQL Injection parametrizzando le query per gli inserimenti iniziali.
* **Refactoring DRY/SOLID**: Centralizzati i mapping dei ResultSet del database nel layer DAO ed aggregate le query statistiche per ridurre i round-trip sul DB.
* **Classifica Reattiva e Thread-Safe**: La classifica globale della dashboard amministrativa si aggiorna in tempo reale grazie a una `ObservableList` condivisa globale. Gli aggiornamenti scatenati dai thread socket di gioco in background al momento di una vittoria vengono inoltrati al thread grafico in modo sicuro tramite `Platform.runLater()`.
* **Notifiche Non Bloccanti e Zero-Freeze**: Sostituiti tutti gli Alert grafici bloccanti del server con chiamate non bloccanti `alert.show()` e implementato un flag di guardia per prevenire alert multipli sovrapposti.
* **Riconnessione Dinamica del Client**: Rimosso il ritardo di 2 secondi sulle disconnessioni dei client per prevenire socket appese. All'atto della disconnessione, il client viene reindirizzato istantaneamente alla schermata di Login, dove un thread demone controlla ogni 3 secondi lo stato della rete e notifica reattivamente l'avvenuto ripristino in colore verde (*"Server ripristinato!"*).

---

## 🛠️ Come Compilare e Avviare il Progetto

### Prerequisiti
* **Java Development Kit (JDK) 8** installato (il percorso consigliato per la compilazione è `C:\Program Files\Java\jdk-1.8`).

### 1. Compilazione
Per compilare entrambi i progetti da riga di comando (eseguire dalla root del rispettivo modulo `GuessTheWord_Server` e `GuessTheWord_Client`):

**Compilazione Server**:
```powershell
powershell -Command "& 'C:\Program Files\Java\jdk-1.8\bin\javac' -encoding UTF-8 -cp 'lib/*' -d build (Get-ChildItem -Path src, test -Filter *.java -Recurse | ForEach-Object { $_.FullName })"
```

**Compilazione Client**:
```powershell
powershell -Command "& 'C:\Program Files\Java\jdk-1.8\bin\javac' -encoding UTF-8 -cp 'lib/*' -d build (Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { $_.FullName })"
```

*Nota: Ricordarsi di copiare i file di risorse (.fxml, .css, .properties) all'interno delle rispettive cartelle `build` prima di mandare in esecuzione.*

### 2. Esecuzione Test Unitari (Server)
```powershell
# Esegui Test Analisi
powershell -Command "& 'C:\Program Files\Java\jdk-1.8\bin\java' -cp 'lib/*;build' org.junit.runner.JUnitCore guesstheword_server.analysis.TestDocumentAnalyzer"

# Esegui Test Caricamento Schermate FXML
powershell -Command "& 'C:\Program Files\Java\jdk-1.8\bin\java' -cp 'lib/*;build' org.junit.runner.JUnitCore guesstheword_server.controller.TestControllersLoading"
```