========================================================================
             GUESS THE WORD - CONSOLE AMMINISTRATORE (SERVER)
========================================================================

Per scopi di test e valutazione, la tabella degli utenti del database SQLite 
viene automaticamente popolata con un account amministratore predefinito se 
il database è vuoto.

Credenziali dell'Amministratore per il Login:
- Username: admin
- Password: admin

Dettagli Tecnici:
- Nel database SQLite (SQLite db/database.db), la password dell'amministratore 
  viene registrata come hash SHA-256
- Questo account possiede il ruolo 'admin' (mentre i normali utenti giocatori 
  posseggono il ruolo 'giocatore'), garantendo così l'accesso alla console 
  di amministrazione del server.
========================================================================
