package guesstheword_client.network;

/**
 * Definisce gli eventi locali di rete rilevati dal client per separare
 * la logica del protocollo applicativo di rete da quella degli stati della connessione.
 *
 * @author Sabrina Soriano
 */
public enum ClientNetworkEvent {
    TIMEOUT,
    CONNECTION_LOST,
    SERVER_SHUTDOWN
}
