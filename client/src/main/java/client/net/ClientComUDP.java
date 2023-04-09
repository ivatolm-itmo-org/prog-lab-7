package client.net;

import core.net.Com;
import core.net.packet.Packet;

public class ClientComUDP implements Com {

    // Server ip
    private String ip;

    // Server port
    private Integer port;

    /**
     * Constructs new {@code ClientComUDP} with provided arguments.
     * 
     * @param ip server ip
     * @param port server port
     */
    public ClientComUDP(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * Implements {@code isAlive} method of {@code Com}.
     */
    @Override
    public boolean isAlive() {
        throw new UnsupportedOperationException("Unimplemented method 'isAlive'");
    }

    /**
     * Implements {@code send} method of {@code Com}.
     */
    @Override
    public void send(Packet command) {
        throw new UnsupportedOperationException("Unimplemented method 'send'");
    }

    /**
     * Implements {@code receive} method of {@code Com}.
     */
    @Override
    public Packet receive() {
        throw new UnsupportedOperationException("Unimplemented method 'receive'");
    }
   
}
