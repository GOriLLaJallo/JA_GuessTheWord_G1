package guesstheword_server.protocol;

import org.junit.Test;
import static org.junit.Assert.*;

public class MessageProtocolTest {

    @Test
    public void testBuildConParametri() {
        String msg = MessageProtocol.build(MessageProtocol.AUTH_LOGIN, "sabrina", "pass123");
        assertEquals("AUTH_LOGIN:sabrina:pass123", msg);
    }

    @Test
    public void testBuildSenzaParametri() {
        String msg = MessageProtocol.build(MessageProtocol.WAITING);
        assertEquals("WAITING", msg);
    }

    @Test
    public void testBuildConDifficolta() {
        String msg = MessageProtocol.build(MessageProtocol.WAITING, "HARD");
        assertEquals("WAITING:HARD", msg);
    }

    @Test
    public void testParseConParametri() {
        String[] parts = MessageProtocol.parse("AUTH_LOGIN:sabrina:pass123");
        assertEquals(3, parts.length);
        assertEquals("AUTH_LOGIN", parts[0]);
        assertEquals("sabrina", parts[1]);
        assertEquals("pass123", parts[2]);
    }

    @Test
    public void testParseSoloComando() {
        String[] parts = MessageProtocol.parse("WAITING");
        assertEquals(1, parts.length);
        assertEquals("WAITING", parts[0]);
    }

    @Test
    public void testBuildEParseRoundTrip() {
        String msg = MessageProtocol.build(MessageProtocol.GAME_START, "ABCDEF", "3", "60");
        String[] parts = MessageProtocol.parse(msg);
        assertEquals(MessageProtocol.GAME_START, parts[0]);
        assertEquals("ABCDEF", parts[1]);
        assertEquals("3", parts[2]);
        assertEquals("60", parts[3]);
    }
}