package core.net.packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Class representing communication packet used
 * by client and server.
 *
 * @author ivatolm
 */
public class Packet {

    // Type of packet
    private PacketType type;

    // Data of packet
    private byte[] data;

    /**
     * Constructs new {@code Packet} with provided arguments.
     *
     * @param type type of the packet
     * @param bytes bytes of the packet
     */
    public Packet(PacketType type, byte[] bytes) {
        this.type = type;
        this.data = bytes;
    }

    /**
     * Constructs new {@code Packet} with provided arguments.
     *
     * @param type type of the packet
     * @param data data of the packet
     *
     * @throws IOException if cannot serialize object
     */
    public Packet(PacketType type, Object data) throws IOException {
        this.type = type;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(data);
            out.flush();
            this.data = bos.toByteArray();
        } finally {
            bos.close();
        }
    }

    /**
     * @return type of the packet
     */
    public PacketType getType() {
        return this.type;
    }

    /**
     * @return bytes of the packet
     */
    public byte[] getBytes() {
        return this.data;
    }

    /**
     * @return data of the packet
     * @throws IOException if cannot deserialize data
     */
    public Object getData() throws IOException {
        Object result = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(this.data);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            try {
                result = in.readObject();
            } catch (ClassNotFoundException e) {
                System.err.println("Cannot deserialize data. Class not found.");
                throw new IOException("Failed to deserialize data.");
            }
        } finally {
            in.close();
        }

        return result;
    }

}
