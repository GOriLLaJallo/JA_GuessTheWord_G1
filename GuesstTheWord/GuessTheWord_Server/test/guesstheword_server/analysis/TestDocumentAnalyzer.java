package guesstheword_server.analysis;

import guesstheword_server.game.Difficulty;

/**
 * Test standalone per la classe DocumentAnalyzer.
 * Verifica la logica di estrazione della parola chiave,
 * escludendo stopword, parole corte ed applicando il fallback.
 * 
 * @author Carmine Muollo
 */
public class TestDocumentAnalyzer {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("AVVIO TEST UNITARI SU DOCUMENTANALYZER");
        System.out.println("==================================================");

        try {
            testMostFrequentWord();
            testStopwordsFiltering();
            testShortWordsFiltering();
            testFallbackEmptyText();
            testFallbackOnlyStopwords();
            testExtractExcerptHardMode();

            System.out.println("==================================================");
            System.out.println("TUTTI I TEST SONO PASSATI CON SUCCESSO!");
            System.out.println("==================================================");
        } catch (Throwable t) {
            System.err.println("==================================================");
            System.err.println("FALLIMENTO TEST: " + t.getMessage());
            t.printStackTrace();
            System.err.println("==================================================");
            System.exit(1);
        }
    }

    /**
     * Test 1: Estrazione della parola più frequente.
     */
    private static void testMostFrequentWord() {
        System.out.print("Test 1: Estrazione parola frequente... ");
        DocumentAnalyzer analyzer = new DocumentAnalyzer();
        String text = "computer software computer hardware program computer internet network";
        // 'computer' appare 3 volte, le altre 1.
        String keyword = analyzer.extractKeyWord(text);
        
        if (!"computer".equals(keyword)) {
            throw new RuntimeException("Atteso 'computer', ma ricevuto '" + keyword + "'");
        }
        System.out.println("PASSATO (risultato: '" + keyword + "')");
    }

    /**
     * Test 2: Verifica il filtraggio delle stopwords.
     */
    private static void testStopwordsFiltering() {
        System.out.print("Test 2: Filtraggio delle stopwords... ");
        DocumentAnalyzer analyzer = new DocumentAnalyzer();
        // 'perche' (stopword) ripetuto 4 volte, 'musica' (valido) ripetuto 2 volte.
        // Ci si aspetta che 'perche' venga ignorata e venga estratta 'musica'.
        String text = "perche perche perche perche musica musica";
        String keyword = analyzer.extractKeyWord(text);
        
        if (!"musica".equals(keyword)) {
            throw new RuntimeException("Atteso 'musica', ma ricevuto '" + keyword + "'");
        }
        System.out.println("PASSATO (risultato: '" + keyword + "')");
    }

    /**
     * Test 3: Verifica il filtraggio di parole troppo corte (< 4 caratteri).
     */
    private static void testShortWordsFiltering() {
        System.out.print("Test 3: Filtraggio parole corte... ");
        DocumentAnalyzer analyzer = new DocumentAnalyzer();
        // 'abc' (lunghezza 3) ripetuto 5 volte, 'programma' (lunghezza 9) 2 volte.
        // 'abc' deve essere ignorato perché corto.
        String text = "abc abc abc abc abc programma programma";
        String keyword = analyzer.extractKeyWord(text);
        
        if (!"programma".equals(keyword)) {
            throw new RuntimeException("Atteso 'programma', ma ricevuto '" + keyword + "'");
        }
        System.out.println("PASSATO (risultato: '" + keyword + "')");
    }

    /**
     * Test 4: Caso di testo vuoto (deve attivare il fallback).
     */
    private static void testFallbackEmptyText() {
        System.out.print("Test 4: Fallback su testo vuoto... ");
        DocumentAnalyzer analyzer = new DocumentAnalyzer();
        String keyword = analyzer.extractKeyWord("   ");
        
        if (keyword == null || keyword.isEmpty()) {
            throw new RuntimeException("Attesa una parola di fallback valida, ma ricevuto vuoto/null");
        }
        System.out.println("PASSATO (risultato di fallback: '" + keyword + "')");
    }

    /**
     * Test 5: Caso di testo contenente solo stopword (deve attivare il fallback).
     */
    private static void testFallbackOnlyStopwords() {
        System.out.print("Test 5: Fallback su sole stopword... ");
        DocumentAnalyzer analyzer = new DocumentAnalyzer();
        String text = "che con del della per tra fra";
        String keyword = analyzer.extractKeyWord(text);
        
        if (keyword == null || keyword.isEmpty()) {
            throw new RuntimeException("Attesa una parola di fallback valida, ma ricevuto vuoto/null");
        }
        System.out.println("PASSATO (risultato di fallback: '" + keyword + "')");
    }

    /**
     * Test 6: Verifica che extractExcerpt() restituisca sempre un estratto valido contenente la keyword in modalità HARD.
     */
    private static void testExtractExcerptHardMode() {
        System.out.print("Test 6: Estrazione estratto in modalita HARD... ");
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
            if (keyword == null) {
                throw new RuntimeException("Keyword estratta nulla in modalita HARD");
            }
            String excerpt = analyzer.extractExcerpt(text, keyword);
            if (excerpt == null) {
                throw new RuntimeException("Estratto nullo per la keyword '" + keyword + "'");
            }
            
            String lowerKeyword = keyword.toLowerCase();
            String[] tokens = excerpt.toLowerCase().split("[^a-zA-Zàèìòùáéíóúâêîôûäëïöü]+");
            boolean found = false;
            for (int j = 0; j < tokens.length; j++) {
                if (tokens[j].equals(lowerKeyword)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new RuntimeException("L'estratto non contiene la parola chiave '" + keyword + "'. Estratto: " + excerpt);
            }
        }
        System.out.println("PASSATO (Verificati 20 campioni HARD estratti correttamente)");
    }
}
