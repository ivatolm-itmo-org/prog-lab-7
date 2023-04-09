package server.net;

import core.net.Com;
import core.net.packet.Packet;

public class ServerComUDP implements Com {

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
