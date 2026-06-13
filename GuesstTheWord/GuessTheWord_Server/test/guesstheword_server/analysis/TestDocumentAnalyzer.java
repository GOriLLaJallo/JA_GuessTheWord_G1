package guesstheword_server.analysis;

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
}
