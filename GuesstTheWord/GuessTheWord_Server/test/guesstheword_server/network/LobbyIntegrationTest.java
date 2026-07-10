package guesstheword_server.network;

import guesstheword_server.protocol.MessageProtocol;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.net.*;

public class LobbyIntegrationTest {

    private static final int TEST_PORT = 5600;

    @Test
    public void testPairingDueGiocatori() throws Exception {
        ServerSocket serverSocket = new ServerSocket(TEST_PORT);

        Runnable acceptAndRun = () -> {
            try {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        new Thread(acceptAndRun).start();
        new Thread(acceptAndRun).start();

        Socket client1 = new Socket("localhost", TEST_PORT);
        Socket client2 = new Socket("localhost", TEST_PORT);

        PrintWriter out1 = new PrintWriter(client1.getOutputStream(), true);
        BufferedReader in1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));

        PrintWriter out2 = new PrintWriter(client2.getOutputStream(), true);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));

        out1.println(MessageProtocol.build(MessageProtocol.WAITING, "MEDIUM"));
        out2.println(MessageProtocol.build(MessageProtocol.WAITING, "MEDIUM"));

        assertEquals(MessageProtocol.WAITING, MessageProtocol.parse(in1.readLine())[0]);
        assertEquals(MessageProtocol.WAITING, MessageProtocol.parse(in2.readLine())[0]);

        assertEquals(MessageProtocol.OPPONENT_FOUND, MessageProtocol.parse(in1.readLine())[0]);
        assertEquals(MessageProtocol.OPPONENT_FOUND, MessageProtocol.parse(in2.readLine())[0]);

        String[] start1 = MessageProtocol.parse(in1.readLine());
        String[] start2 = MessageProtocol.parse(in2.readLine());
        assertEquals(MessageProtocol.GAME_START, start1[0]);
        assertEquals(MessageProtocol.GAME_START, start2[0]);

        client1.close();
        client2.close();
        serverSocket.close();
    }
}