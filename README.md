# GuessTheWord_G1

GuessTheWord_G1 e' un'applicazione client-server scritta in Java 8 che si basa su JavaFX per l'interfaccia grafica e SQLite per la persistenza dei dati. Il gioco consiste in una sfida in tempo reale tra due utenti connessi che devono indovinare una parola nascosta cifrata nel minor tempo possibile.

---

## Autori e Classi Associate

Nota: Ciascun autore di un Controller si e' occupato anche dello sviluppo e del design della rispettiva vista FXML (es. l'autore di AdminDashboardViewController e' autore anche di AdminDashboardView.fxml e così via).

### William Menza (13 classi)
* ClientApp.java
* DifficultyViewController.java (con relativa vista: DifficultyView.fxml)
* GameViewController.java (con relativa vista: GameView.fxml)
* HistoryViewController.java (con relativa vista: HistoryView.fxml)
* LoginViewController.java (con relativa vista: LoginView.fxml)
* WaitingRoomViewController.java (con relativa vista: WaitingRoomView.fxml)
* GameState.java
* MatchRecord.java
* AuthService.java (Client)
* GameService.java
* HistoryService.java (Client)
* HashUtil.java (Client)
* SceneManager.java

### Davide Andrea Odierna (6 classi)
* AnalysisResult.java
* AnalysisService.java
* DocumentAnalyzer.java
* GameManager.java (in collaborazione con Sabrina Soriano)
* GameSession.java (in collaborazione con Sabrina Soriano)
* ChallengePreparator.java

### Sabrina Soriano (9 classi)
* ClientNetworkEvent.java
* ListenerTask.java
* MessageProtocol.java
* ServerConnection.java
* CaesarCipher.java
* Difficulty.java
* ClientRegistry.java
* GameServer.java
* ClientHandler.java

### Carmine Muollo (21 classi)
* ServerApp.java
* AdminDashboardViewController.java (con relativa vista: AdminDashboardView.fxml)
* AdminLoginViewController.java (con relativa vista: AdminLoginView.fxml)
* AdminMainViewController.java (con relativa vista: AdminMainView.fxml)
* LeaderBoardViewController.java (con relativa vista: LeaderBoardView.fxml)
* ChallengeDAO.java
* ResultDAO.java
* UserDAO.java
* DatabaseManager.java
* DataAccessException.java
* Challenge.java
* GameResult.java
* User.java
* LeaderboardEntry.java
* UserStatsDTO.java
* AuthService.java (Server)
* HistoryService.java (Server)
* LeaderboardService.java
* MatchPersistenceService.java
* TestDocumentAnalyzer.java (Test)
* TestControllersLoading.java (Test)

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