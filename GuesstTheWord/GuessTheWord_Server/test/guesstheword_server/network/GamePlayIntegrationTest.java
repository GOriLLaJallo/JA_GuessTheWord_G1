package guesstheword_server.network;

import guesstheword_server.game.CaesarCipher;
import guesstheword_server.protocol.MessageProtocol;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.net.*;

public class GamePlayIntegrationTest {

    private static int port = 5700; // porta diversa per ogni test, evita conflitti

    private ServerSocket startServerWithHandlers(int numClients) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Runnable acceptAndRun = () -> {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new ClientHandler(clientSocket).run();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            for (int i = 0; i < numClients; i++) {
                new Thread(acceptAndRun).start();
            }
            return serverSocket;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------------------------------------------------
    // 1. TEST REGISTRAZIONE
    // ---------------------------------------------------------
    @Test
    public void testRegistrazione() throws Exception {
        System.out.println("\n===== TEST REGISTRAZIONE =====");
        ServerSocket serverSocket = startServerWithHandlers(1);
        int myPort = port++;

        Socket client = new Socket("localhost", myPort);
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        String nuovoUsername = "utenteTest_" + System.currentTimeMillis();

        // 1a. Registrazione nuovo utente -> AUTH_OK
        System.out.println("[FASE 1a] Registrazione nuovo utente: " + nuovoUsername);
        out.println(MessageProtocol.build(MessageProtocol.AUTH_REGISTER, nuovoUsername, "pass123"));
        String[] resp1 = MessageProtocol.parse(in.readLine());
        System.out.println("[RISPOSTA] " + String.join(":", resp1));
        assertEquals(MessageProtocol.AUTH_OK, resp1[0]);
        assertEquals(nuovoUsername, resp1[1]);

        client.close();
        serverSocket.close();
        System.out.println("===== FINE TEST REGISTRAZIONE (utente nuovo OK) =====\n");
    }

    @Test
    public void testRegistrazioneUsernameDuplicato() throws Exception {
        System.out.println("\n===== TEST REGISTRAZIONE - USERNAME DUPLICATO =====");
        ServerSocket serverSocket = startServerWithHandlers(2);
        int myPort = port++;

        String username = "utenteDuplicato_" + System.currentTimeMillis();

        // Prima registrazione
        Socket client1 = new Socket("localhost", myPort);
        PrintWriter out1 = new PrintWriter(client1.getOutputStream(), true);
        BufferedReader in1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));

        System.out.println("[FASE 1] Prima registrazione di: " + username);
        out1.println(MessageProtocol.build(MessageProtocol.AUTH_REGISTER, username, "pass123"));
        String[] resp1 = MessageProtocol.parse(in1.readLine());
        System.out.println("[RISPOSTA 1] " + String.join(":", resp1));
        assertEquals(MessageProtocol.AUTH_OK, resp1[0]);

        // Seconda registrazione con stesso username
        Socket client2 = new Socket("localhost", myPort);
        PrintWriter out2 = new PrintWriter(client2.getOutputStream(), true);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));

        System.out.println("[FASE 2] Seconda registrazione con stesso username: " + username);
        out2.println(MessageProtocol.build(MessageProtocol.AUTH_REGISTER, username, "altraPass"));
        String[] resp2 = MessageProtocol.parse(in2.readLine());
        System.out.println("[RISPOSTA 2] " + String.join(":", resp2));
        assertEquals(MessageProtocol.AUTH_FAIL, resp2[0]);

        client1.close();
        client2.close();
        serverSocket.close();
        System.out.println("===== FINE TEST USERNAME DUPLICATO =====\n");
    }

    
    // ---------------------------------------------------------
    // 2. TEST PAIRING CON DIFFICOLTA' DIVERSE (nessun pairing)
    // ---------------------------------------------------------
    @Test
    public void testNessunPairingConDifficoltaDiverse() throws Exception {
        System.out.println("\n===== TEST PAIRING - DIFFICOLTA' DIVERSE (no match) =====");
        ServerSocket serverSocket = startServerWithHandlers(2);
        int myPort = port++;

        Socket client1 = new Socket("localhost", myPort);
        Socket client2 = new Socket("localhost", myPort);

        PrintWriter out1 = new PrintWriter(client1.getOutputStream(), true);
        BufferedReader in1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));

        PrintWriter out2 = new PrintWriter(client2.getOutputStream(), true);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));

        // Timeout breve per non far bloccare il test
        client1.setSoTimeout(2000);
        client2.setSoTimeout(2000);

        System.out.println("[FASE 1] client1 -> WAITING:EASY, client2 -> WAITING:HARD");
        out1.println(MessageProtocol.build(MessageProtocol.WAITING, "EASY"));
        out2.println(MessageProtocol.build(MessageProtocol.WAITING, "HARD"));

        // Entrambi devono ricevere solo il WAITING di conferma
        String[] wait1 = MessageProtocol.parse(in1.readLine());
        String[] wait2 = MessageProtocol.parse(in2.readLine());
        System.out.println("[RISPOSTA client1] " + String.join(":", wait1));
        System.out.println("[RISPOSTA client2] " + String.join(":", wait2));
        assertEquals(MessageProtocol.WAITING, wait1[0]);
        assertEquals(MessageProtocol.WAITING, wait2[0]);

        // Nessun secondo messaggio deve arrivare entro il timeout (no OPPONENT_FOUND)
        System.out.println("[FASE 2] Verifico che NON arrivi nulla (no pairing)...");
        try {
            String unexpected = in1.readLine();
            fail("Non doveva arrivare nessun messaggio, invece: " + unexpected);
        } catch (SocketTimeoutException e) {
            System.out.println("[OK] Nessun messaggio ricevuto da client1, come previsto (timeout).");
        }

        try {
            String unexpected = in2.readLine();
            fail("Non doveva arrivare nessun messaggio, invece: " + unexpected);
        } catch (SocketTimeoutException e) {
            System.out.println("[OK] Nessun messaggio ricevuto da client2, come previsto (timeout).");
        }

        client1.close();
        client2.close();
        serverSocket.close();
        System.out.println("===== FINE TEST DIFFICOLTA' DIVERSE =====\n");
    }

    // ---------------------------------------------------------
    // 3. TEST REQ_HISTORY SENZA LOGIN
    // ---------------------------------------------------------
    @Test
    public void testHistorySenzaLogin() throws Exception {
        System.out.println("\n===== TEST REQ_HISTORY SENZA LOGIN =====");
        ServerSocket serverSocket = startServerWithHandlers(1);
        int myPort = port++;

        Socket client = new Socket("localhost", myPort);
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        System.out.println("[FASE 1] Invio REQ_HISTORY senza essere autenticato");
        out.println(MessageProtocol.build(MessageProtocol.REQ_HISTORY));
        String[] resp = MessageProtocol.parse(in.readLine());
        System.out.println("[RISPOSTA] " + String.join(":", resp));

        assertEquals(MessageProtocol.AUTH_FAIL, resp[0]);
        assertEquals("Non autenticato.", resp[1]);

        client.close();
        serverSocket.close();
        System.out.println("===== FINE TEST REQ_HISTORY SENZA LOGIN =====\n");
    }

    // ---------------------------------------------------------
    // 4. TEST GAME_ANSWER - RISPOSTA CORRETTA / ERRATA
    // ---------------------------------------------------------
    @Test
    public void testRispostaCorrettaVince() throws Exception {
        System.out.println("\n===== TEST GAME_ANSWER - RISPOSTA CORRETTA =====");
        ServerSocket serverSocket = startServerWithHandlers(2);
        int myPort = port++;

        Socket client1 = new Socket("localhost", myPort);
        Socket client2 = new Socket("localhost", myPort);

        PrintWriter out1 = new PrintWriter(client1.getOutputStream(), true);
        BufferedReader in1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));

        PrintWriter out2 = new PrintWriter(client2.getOutputStream(), true);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));

        System.out.println("[FASE 1] Entrambi i client mandano WAITING:MEDIUM");
        out1.println(MessageProtocol.build(MessageProtocol.WAITING, "MEDIUM"));
        out2.println(MessageProtocol.build(MessageProtocol.WAITING, "MEDIUM"));

        String[] w1 = MessageProtocol.parse(in1.readLine());
        String[] w2 = MessageProtocol.parse(in2.readLine());
        System.out.println("[RISPOSTA client1] " + String.join(":", w1));
        System.out.println("[RISPOSTA client2] " + String.join(":", w2));
        assertEquals(MessageProtocol.WAITING, w1[0]);
        assertEquals(MessageProtocol.WAITING, w2[0]);

        System.out.println("[FASE 2] Attendo OPPONENT_FOUND per entrambi");
        String[] of1 = MessageProtocol.parse(in1.readLine());
        String[] of2 = MessageProtocol.parse(in2.readLine());
        System.out.println("[RISPOSTA client1] " + String.join(":", of1));
        System.out.println("[RISPOSTA client2] " + String.join(":", of2));
        assertEquals(MessageProtocol.OPPONENT_FOUND, of1[0]);
        assertEquals(MessageProtocol.OPPONENT_FOUND, of2[0]);

        System.out.println("[FASE 3] Attendo GAME_START per entrambi");
        String[] start1 = MessageProtocol.parse(in1.readLine());
        String[] start2 = MessageProtocol.parse(in2.readLine());
        System.out.println("[GAME_START client1] " + String.join(":", start1));
        System.out.println("[GAME_START client2] " + String.join(":", start2));
        assertEquals(MessageProtocol.GAME_START, start1[0]);
        assertEquals(MessageProtocol.GAME_START, start2[0]);

        String testoCifrato = start1[1];
        int shift = Integer.parseInt(start1[2]);
        String parolaCorretta = CaesarCipher.decrypt(testoCifrato, shift);

        System.out.println("[FASE 4] Testo cifrato: " + testoCifrato + " | Shift: " + shift);
        System.out.println("[FASE 4] Parola decifrata: " + parolaCorretta);

        System.out.println("[FASE 5] client1 risponde correttamente: " + parolaCorretta);
        out1.println(MessageProtocol.build(MessageProtocol.GAME_ANSWER, parolaCorretta));

        String[] win = MessageProtocol.parse(in1.readLine());
        System.out.println("[RISPOSTA client1] " + String.join(":", win));
        assertEquals(MessageProtocol.GAME_WIN, win[0]);

        String[] lose = MessageProtocol.parse(in2.readLine());
        System.out.println("[RISPOSTA client2] " + String.join(":", lose));
        assertEquals(MessageProtocol.GAME_LOSE, lose[0]);

        client1.close();
        client2.close();
        serverSocket.close();
        System.out.println("===== FINE TEST RISPOSTA CORRETTA =====\n");
    }

    @Test
    public void testRispostaErrataNonTermina() throws Exception {
        System.out.println("\n===== TEST GAME_ANSWER - RISPOSTA ERRATA =====");
        ServerSocket serverSocket = startServerWithHandlers(2);
        int myPort = port++;

        Socket client1 = new Socket("localhost", myPort);
        Socket client2 = new Socket("localhost", myPort);

        PrintWriter out1 = new PrintWriter(client1.getOutputStream(), true);
        BufferedReader in1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));

        PrintWriter out2 = new PrintWriter(client2.getOutputStream(), true);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));

        System.out.println("[FASE 1] Entrambi i client mandano WAITING:MEDIUM");
        out1.println(MessageProtocol.build(MessageProtocol.WAITING, "MEDIUM"));
        out2.println(MessageProtocol.build(MessageProtocol.WAITING, "MEDIUM"));

        in1.readLine(); // WAITING
        in2.readLine(); // WAITING

        System.out.println("[FASE 2] Attendo OPPONENT_FOUND");
        in1.readLine(); // OPPONENT_FOUND
        in2.readLine(); // OPPONENT_FOUND

        System.out.println("[FASE 3] Attendo GAME_START");
        in1.readLine(); // GAME_START
        in2.readLine(); // GAME_START

        System.out.println("[FASE 4] client1 risponde con parola SICURAMENTE errata");
        out1.println(MessageProtocol.build(MessageProtocol.GAME_ANSWER, "PAROLA_SICURAMENTE_ERRATA_XYZ"));

        String[] resp = MessageProtocol.parse(in1.readLine());
        System.out.println("[RISPOSTA client1] " + String.join(":", resp));

        // Da ClientHandler.handleAnswer -> GameSession.handleAnswer:
        // risposta errata -> AUTH_FAIL:"Risposta errata. Riprova!"
        assertEquals(MessageProtocol.AUTH_FAIL, resp[0]);
        assertEquals("Risposta errata. Riprova!", resp[1]);

        System.out.println("[OK] La sessione non è terminata, client2 non riceve GAME_LOSE/GAME_WIN immediato.");

        client1.close();
        client2.close();
        serverSocket.close();
        System.out.println("===== FINE TEST RISPOSTA ERRATA =====\n");
    }
}