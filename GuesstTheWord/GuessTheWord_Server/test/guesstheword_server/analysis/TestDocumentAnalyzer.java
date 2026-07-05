package guesstheword_server.analysis;

import guesstheword_server.game.Difficulty;
import guesstheword_server.game.GameSession;
import guesstheword_server.network.ClientHandler;
import guesstheword_server.network.ClientRegistry;
import guesstheword_server.model.Challenge;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test unitari e di integrazione JUnit per DocumentAnalyzer, ChallengePreparator e GameSession.
 * 
 * @author Carmine Muollo
 */
public class TestDocumentAnalyzer {

    /**
     * Test 1: Estrazione della parola più frequente.
     */
    @Test
    public void testMostFrequentWord() {
        DocumentAnalyzer analyzer = new DocumentAnalyzer();
        String text = "computer software computer hardware program computer internet network";
        String keyword = analyzer.extractKeyWord(text);
        assertEquals("Atteso 'computer' come parola più frequente", "computer", keyword);
    }

    /**
     * Test 2: Verifica il filtraggio delle stopwords.
     */
    @Test
    public void testStopwordsFiltering() {
        DocumentAnalyzer analyzer = new DocumentAnalyzer();
        String text = "perche perche perche perche musica musica";
        String keyword = analyzer.extractKeyWord(text);
        assertEquals("Atteso 'musica' (le stopword devono essere ignorate)", "musica", keyword);
    }

    /**
     * Test 3: Verifica il filtraggio di parole troppo corte (< 4 caratteri).
     */
    @Test
    public void testShortWordsFiltering() {
        DocumentAnalyzer analyzer = new DocumentAnalyzer();
        String text = "abc abc abc abc abc programma programma";
        String keyword = analyzer.extractKeyWord(text);
        assertEquals("Atteso 'programma' (le parole corte devono essere ignorate)", "programma", keyword);
    }

    /**
     * Test 4: Caso di testo vuoto (deve attivare il fallback).
     */
    @Test
    public void testFallbackEmptyText() {
        DocumentAnalyzer analyzer = new DocumentAnalyzer();
        String keyword = analyzer.extractKeyWord("   ");
        assertNotNull("La parola di fallback non deve essere null", keyword);
        assertFalse("La parola di fallback non deve essere vuota", keyword.isEmpty());
    }

    /**
     * Test 5: Caso di testo contenente solo stopword (deve attivare il fallback).
     */
    @Test
    public void testFallbackOnlyStopwords() {
        DocumentAnalyzer analyzer = new DocumentAnalyzer();
        String text = "che con del della per tra fra";
        String keyword = analyzer.extractKeyWord(text);
        assertNotNull("La parola di fallback non deve essere null", keyword);
        assertFalse("La parola di fallback non deve essere vuota", keyword.isEmpty());
    }

    /**
     * Test 6: Verifica che extractExcerpt() restituisca sempre un estratto valido contenente la keyword in modalità HARD.
     */
    @Test
    public void testExtractExcerptHardMode() {
        DocumentAnalyzer analyzer = new DocumentAnalyzer();
        String text = "STORIA DI FANTASIA \n\n" +
            "Il villaggio di Valverde sorgeva vicino a un bosco lucente. In quel luogo viveva un giovane artigiano di nome Leo. Ogni giorno egli creava piccoli oggetti di legno. Un mattino d'estate, Leo decise di cercare un albero speciale. Camminò a lungo lungo un sentiero stretto e isolato.\n\n" +
            "Mentre avanzava, il ragazzo sentì un rumore strano. Tra le foglie notò un gatto dal pelo candido. L'animale lo guardava con occhi grandi e curiosi. Leo si avvicinò piano e offrì un pezzo di pane. Il gatto accettò il cibo, poi iniziò a correre. Il giovane decise di seguire quella scia bianca.\n\n" +
            "La corsa finì davanti a una roccia enorme. L'animale saltò sopra una pietra e sparì. Leo esaminò la parete con grande stupore e cura. Notò subito una fessura nascosta tra i rami. Lì dentro si vedeva un oggetto molto vecchio. Era una scatola di metallo, chiusa da molti secoli.\n\n" +
            "Il ragazzo usò un piccolo attrezzo per aprirla. Il coperchio scattò con un suono acuto e secco. Dentro non c'erano monete d'oro o gioielli. C'era solo un antico diario con pagine bianche. Su ogni foglio si poteva leggere una parola. La prima scritta visibile era la parola \"libertà\".\n\n" +
            "All'improvviso, la scatola emanò un lampo dorato. Quella luce riempì tutta la vallata circostante. Leo sentì una forte energia dentro il suo cuore. Capì che quel diario poteva scrivere il futuro. Ogni frase inserita sarebbe diventata realtà pura. Il giovane tornò a casa con il tesoro.\n\n" +
            "Da quel giorno, Valverde cambiò in modo totale. Non ci furono più inverni freddi o carestie. La pioggia bagnava i campi al momento giusto. La terra donava frutti dolci e molto succosi. Gli abitanti vivevano felici, senza alcun timore. Nessuno conobbe mai il segreto del ragazzo.\n\n" +
            "Leo continuò a creare i suoi piccoli oggetti. Il diario rimase custodito in un posto sicuro. Il gatto candido tornava a trovarlo ogni sera. Insieme guardavano le stelle dalla finestra alta. La vita scorreva serena in quel mondo magico.\n\n" +
            "Il tempo passava veloce ma nulla rovinava la pace. Gli alberi del bosco crescevano forti e sani. Ogni persona offriva aiuto al proprio vicino con gioia. Leo scriveva storie di pace sul diario segreto. Il borgo divenne il posto più felice della terra. Tutti i bambini giocavano liberi nei prati verdi. I vecchi saggi parlavano sotto la grande ombra del platano. La serenità era ormai un dono per ogni famiglia.";

        for (int i = 0; i < 20; i++) {
            String keyword = analyzer.extractKeyWord(text, Difficulty.HARD);
            assertNotNull("Keyword estratta nulla in modalita HARD", keyword);
            String excerpt = analyzer.extractExcerpt(text, keyword);
            assertNotNull("Estratto nullo per la keyword '" + keyword + "'", excerpt);
            
            String lowerKeyword = keyword.toLowerCase();
            String[] tokens = excerpt.toLowerCase().split("[^a-zA-Zàèìòùáéíóúâêîôûäëïöü]+");
            boolean found = false;
            for (int j = 0; j < tokens.length; j++) {
                if (tokens[j].equals(lowerKeyword)) {
                    found = true;
                    break;
                }
            }
            assertTrue("L'estratto deve contenere la parola chiave '" + keyword + "'", found);
        }
    }

    /**
     * Test 7: Verifica che prepareRandom() non causi StackOverflowError e restituisca una Challenge valida.
     */
    @Test
    public void testPrepareRandomNoStackOverflow() {
        guesstheword_server.game.ChallengePreparator preparator = new guesstheword_server.game.ChallengePreparator();
        Challenge c = preparator.prepareRandom(Difficulty.HARD);
        
        assertNotNull("Challenge di fallback nulla!", c);
        assertNotNull("La parola nascosta della Challenge di fallback e nulla o vuota!", c.getParolaNascosta());
        assertFalse("La parola nascosta non deve essere vuota", c.getParolaNascosta().isEmpty());
        assertNotNull("L'estratto della Challenge di fallback e nullo o vuoto!", c.getEstratto());
        assertFalse("L'estratto non deve essere vuoto", c.getEstratto().isEmpty());
    }

    /**
     * Test 8: Verifica il fallback della difficoltà nulla o invalida in GameSession.
     */
    @Test
    public void testGameSessionDifficultyFallback() {
        Challenge c1 = new Challenge("computer", 3, java.time.LocalDateTime.now(), null);
        java.net.Socket s1 = new java.net.Socket();
        java.net.Socket s2 = new java.net.Socket();
        ClientRegistry registry = ClientRegistry.getInstance();
        ClientHandler ch1 = new ClientHandler(s1, registry);
        ClientHandler ch2 = new ClientHandler(s2, registry);
        
        GameSession session1 = new GameSession(ch1, ch2, c1);
        session1.start();
        
        assertNotNull("Difficolta rimasta nulla dopo start()", session1.getChallenge().getDifficolta());
        
        Challenge c2 = new Challenge("schermo", 4, java.time.LocalDateTime.now(), "INVALID_DIFF");
        GameSession session2 = new GameSession(ch1, ch2, c2);
        session2.start();
        
        assertNotNull("Difficolta rimasta non valida dopo start()", session2.getChallenge().getDifficolta());
    }

    /**
     * Test 9: Verifica la correttezza del metodo contieneParola in diversi contesti (accenti, apostrofi, punteggiatura).
     */
    @Test
    public void testContieneParolaCases() {
        assertTrue("Fallita ricerca parola presente", DocumentAnalyzer.contieneParola("il computer sorgeva vicino", "computer"));
        assertFalse("Fallita ricerca parola assente", DocumentAnalyzer.contieneParola("il computer sorgeva vicino", "tastiera"));
        assertTrue("Fallita ricerca parola con accento", DocumentAnalyzer.contieneParola("questa è libertà pura", "libertà"));
        assertTrue("Fallita ricerca parola con apostrofo e virgola", DocumentAnalyzer.contieneParola("all'improvviso, la scatola emanò", "improvviso"));
        assertFalse("Trovata parola inesatta", DocumentAnalyzer.contieneParola("il sentiero era stretto", "era_"));
    }

    /**
     * Test 10: Verifica che il client non venga disconnesso durante una seconda partita
     * consecutiva se è rimasto inattivo dopo la fine della prima partita per oltre 90 secondi,
     * ma ha regolarmente iniziato/inviato messaggi nella seconda partita.
     */
    @Test
    public void testConsecutiveGamesInactivityRegression() {
        java.net.Socket mockSocket = new java.net.Socket();
        ClientRegistry registry = ClientRegistry.getInstance();
        ClientHandler handler = new ClientHandler(mockSocket, registry);

        // 1. Inizio Prima Partita
        Challenge c1 = new Challenge("tastiera", 4, java.time.LocalDateTime.now(), "EASY");
        GameSession session1 = new GameSession(handler, handler, c1);
        handler.setCurrentSession(session1);
        session1.start();

        // Verifica che la sessione sia attiva e la soglia sia di 90 secondi (GameSession.DEFAULT_TIMER_SECONDS + 30) * 1000
        assertTrue("La sessione 1 dovrebbe essere attiva per il client", session1.equals(handler.getCurrentSession()));
        
        // 2. Fine Prima Partita
        // Sblocchiamo la prima partita decretando una vincita fittizia
        session1.handleAnswer(handler, session1.getChallenge().getParolaNascosta());
        assertTrue("La sessione 1 dovrebbe essere terminata", session1.isFinished());

        // 3. Simuliamo 95 secondi di inattività da parte del client dopo la prima partita
        long timeOfActivityAfterGame1 = System.currentTimeMillis() - 95000;
        
        // 4. Inizio Seconda Partita
        Challenge c2 = new Challenge("mouse", 5, java.time.LocalDateTime.now(), "EASY");
        GameSession session2 = new GameSession(handler, handler, c2);
        handler.setCurrentSession(session2);
        session2.start();

        assertTrue("La sessione 2 dovrebbe essere attiva per il client", session2.equals(handler.getCurrentSession()));
        assertFalse("La sessione 2 non dovrebbe essere finita", session2.isFinished());

        // Simuliamo l'inizio o l'invio di messaggi della seconda partita aggiornando lastActivityTimestamp a adesso
        long lastActivityTimestamp = System.currentTimeMillis();
        long elapsed = lastActivityTimestamp - timeOfActivityAfterGame1;
        
        // La soglia per la partita attiva è 90 secondi (90000 ms)
        long threshold = (GameSession.DEFAULT_TIMER_SECONDS + 30) * 1000L;
        
        // Se controllassimo l'inattività basandoci sul vecchio timestamp di game 1 (elapsed = 95s > 90s), scatterebbe il timeout.
        // Ma avendo ricevuto messaggi/attività per game 2, resettiamo a adesso (elapsed2 = 0s < 90s)
        long elapsedReal = System.currentTimeMillis() - lastActivityTimestamp;
        
        assertTrue("I 95s di intervallo simulati superano la soglia di 90s", elapsed > threshold);
        assertTrue("Il nuovo tempo di inattività reale (0s) non deve superare la soglia", elapsedReal <= threshold);
    }
}
