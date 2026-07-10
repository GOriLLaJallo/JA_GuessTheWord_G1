# GuessTheWord_G1

GuessTheWord_G1 e' un'applicazione client-server scritta in Java 8 che si basa su JavaFX per l'interfaccia grafica e SQLite per la persistenza dei dati. Il gioco consiste in una sfida in tempo reale tra due utenti connessi che devono indovinare una parola nascosta cifrata nel minor tempo possibile.

---

## Autori e Classi Associate

William: (13 classi)
ClientApp.java
DifficultyViewController.java
GameViewController.java
HistoryViewController.java
LoginViewController.java
WaitingRoomController.java
GameState.java
MatchRecord.java
AuthService.java
GameService.java
HistoryService.java
HashUtil.java
SceneManager.java

Davide: (6 classi)
AnalysisResult.java
AnalysisService.java
DocumentAnalyzer.java
GameManager.java (con Sabrina)
GameSession.java (con Sabrina)
ChallengePreparator.java

Sabrina: (9 classi)
ClientNetworkEvent.java
ListenerTask.java
MessageProtocol.java
ServerConnection.java
CaesarCipher.java
Difficulty.java
ClientRegistry.java
GameServer.java
ClientHandler

Carmine: (21 classi)
ServerApp.java
AdminDashboardViewController.java
AdminLoginViewController.java
AdminMainViewController.java
LeaderBoardViewController.java
ChallengeDAO.java
ResultDAO.java
UserDAO.java
DatabaseManager.java
DataAccessException.java
Challenge.java
GameResult.java
User.java
LeaderboardEntry.java
UserStatsDTO.java
AuthService.java
HistoryService.java
LeaderboardService.java
MatchPersistenceService.java
TestDocumentAnalyzer.java
TestControllersLoading.java

TOTALE: 49 CLASSI

---

## Credenziali per il Testing (Versione 2.0.0)

Per effettuare il test del progetto da parte del docente, utilizzare le seguenti credenziali pre-configurate nel database:

### Account Giocatori (Client)
1. **Utente 1**:
   - Username: User1
   - Password: User123
2. **Utente 2**:
   - Username: User2
   - Password: User456

### Account Amministratore (Server)
- Username: admin
- Password: admin

---

## Caratteristiche del Refactoring (Versione 2.0.0)

* **Ottimizzazione del Driver Database**: Il driver SQLite viene caricato una sola volta all'avvio in un blocco statico thread-safe, eliminando overhead di connessione ripetuti.
* **Sicurezza Query**: Riscritto l'inserimento dell'amministratore tramite PreparedStatement parametrizzato per prevenire vulnerabilita' di SQL Injection.
* **Refactoring DRY/SOLID**: Centralizzati i mapping dei ResultSet nei DAO ed aggregate le query statistiche per ridurre i tempi di risposta.
* **Classifica Reattiva e Thread-Safe**: La classifica globale dell'interfaccia amministratore si aggiorna dinamicamente all'arrivo di una notifica di vittoria in background tramite Platform.runLater() su una ObservableList condivisa.
* **Notifiche Non Bloccanti e Zero-Freeze**: Sostituiti tutti gli Alert bloccanti (showAndWait) con dialoghi non bloccanti (show) e aggiunto un controllo di guardia per evitare la sovrapposizione di popup di errore multipli.
* **Riconnessione Dinamica del Client**: Eliminato il ritardo di 2 secondi sulle disconnessioni per evitare socket appese. Il client viene riportato subito al login, dove un thread demone tenta la riconnessione automatica ogni 3 secondi, indicando il ripristino in verde ("Server ripristinato!").

---

## Istruzioni per la Compilazione e l'Esecuzione

### Compilazione Server
```powershell
powershell -Command "& 'C:\Program Files\Java\jdk-1.8\bin\javac' -encoding UTF-8 -cp 'lib/*' -d build (Get-ChildItem -Path src, test -Filter *.java -Recurse | ForEach-Object { $_.FullName })"
```

### Compilazione Client
```powershell
powershell -Command "& 'C:\Program Files\Java\jdk-1.8\bin\javac' -encoding UTF-8 -cp 'lib/*' -d build (Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { $_.FullName })"
```

*Nota: Assicurarsi di copiare i file della cartella resources all'interno di build prima dell'avvio.*

### Esecuzione Test Unitari
```powershell
powershell -Command "& 'C:\Program Files\Java\jdk-1.8\bin\java' -cp 'lib/*;build' org.junit.runner.JUnitCore guesstheword_server.analysis.TestDocumentAnalyzer"
powershell -Command "& 'C:\Program Files\Java\jdk-1.8\bin\java' -cp 'lib/*;build' org.junit.runner.JUnitCore guesstheword_server.controller.TestControllersLoading"
```