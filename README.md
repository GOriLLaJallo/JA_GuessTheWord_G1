# GuessTheWord_G1

GuessTheWord_G1 e' un'applicazione client-server scritta in Java 8 che si basa su JavaFX per l'interfaccia grafica e SQLite per la persistenza dei dati. Il gioco consiste in una sfida in tempo reale tra due utenti connessi che devono indovinare una parola nascosta cifrata nel minor tempo possibile.

---

## Autori e Classi Associate

### Carmine Muollo
* guesstheword_client.model.GameState
* guesstheword_client.model.MatchRecord
* guesstheword_client.network.ClientNetworkEvent
* guesstheword_server.ServerApp
* guesstheword_server.analysis.AnalysisResult
* guesstheword_server.analysis.AnalysisService
* guesstheword_server.controller.AdminDashboardViewController
* guesstheword_server.controller.AdminLoginViewController
* guesstheword_server.controller.AdminMainViewController
* guesstheword_server.controller.LeaderBoardViewController
* guesstheword_server.db.ChallengeDAO
* guesstheword_server.db.DatabaseManager
* guesstheword_server.db.ResultDAO
* guesstheword_server.db.UserDAO
* guesstheword_server.exception.DataAccessException
* guesstheword_server.model.Challenge
* guesstheword_server.model.GameResult
* guesstheword_server.model.LeaderboardEntry
* guesstheword_server.model.User
* guesstheword_server.model.UserStatsDTO
* guesstheword_server.network.ClientRegistry
* guesstheword_server.service.AuthService
* guesstheword_server.service.HistoryService
* guesstheword_server.service.LeaderboardService
* guesstheword_server.service.MatchPersistenceService
* guesstheword_server.utils.HashUtil
* guesstheword_server.analysis.TestDocumentAnalyzer (Test)
* guesstheword_server.controller.TestControllersLoading (Test)

### William Menza
* guesstheword_client.ClientApp
* guesstheword_client.controller.DifficultyViewController
* guesstheword_client.controller.GameViewController
* guesstheword_client.controller.HistoryViewController
* guesstheword_client.controller.LoginViewController
* guesstheword_client.controller.WaitingRoomViewController
* guesstheword_client.service.AuthService
* guesstheword_client.service.GameService
* guesstheword_client.service.HistoryService
* guesstheword_client.utils.HashUtil
* guesstheword_client.utils.SceneManager

### Sabrina Soriano
* guesstheword_client.network.ListenerTask
* guesstheword_client.network.ServerConnection
* guesstheword_server.game.Difficulty
* guesstheword_server.game.GameManager (in collaborazione)
* guesstheword_server.network.ClientHandler
* guesstheword_server.network.GameServer
* guesstheword_server.protocol.MessageProtocol

### Davide Andrea Odierna
* guesstheword_server.game.GameSession
* guesstheword_server.game.GameManager (in collaborazione)

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