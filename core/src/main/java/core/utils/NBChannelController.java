package core.utils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.apache.commons.lang3.SerializationUtils;

/**
 * Class providing static methods for reading and writing
 * objects to non-blocking channels.
 *
 * @author ivatolm
 */
public class NBChannelController {

    // Length of field representing size of the object
    private static final int LENGTH_FIELD_SIZE = SerializationUtils.serialize(42).length;

    /**
     * Reads one object from the channel and returns it.
     *
     * @param channel channel to read from
     * @return object read from the channel
     * @throws IOException if failed to read from channel
     */
    public static Serializable read(ReadableByteChannel channel) throws IOException {
        int readCnt;

        ByteBuffer lengthBuffer = ByteBuffer.allocate(LENGTH_FIELD_SIZE);
        readCnt = channel.read(lengthBuffer);
        if (readCnt != LENGTH_FIELD_SIZE) {
            throw new IOException(
                "Cannot read object from channel: " + readCnt + " != " + LENGTH_FIELD_SIZE
            );
        }

        int length = SerializationUtils.deserialize(lengthBuffer.array());

        ByteBuffer objectBuffer = ByteBuffer.allocate(length);
        readCnt = channel.read(objectBuffer);
        if (readCnt != length) {
            throw new IOException(
                "Cannot read object from channel: " + readCnt + " != " + length
            );
        }

        Serializable object = SerializationUtils.deserialize(objectBuffer.array());
        return object;
    }

    /**
     * Writes one object to the channel.
     *
     * @param channel channel to write to
     * @param object object to be written
     * @throws IOException if failed to write to channel
     */
    public static void write(WritableByteChannel channel, Serializable object) throws IOException {
        byte[] objectBytes = SerializationUtils.serialize(object);
        byte[] objectLengthBytes = SerializationUtils.serialize(objectBytes.length);

        ByteBuffer objectLengthBuffer = ByteBuffer.allocate(LENGTH_FIELD_SIZE);
        ByteBuffer.wrap(objectLengthBytes);

        if (objectLengthBuffer.capacity() != LENGTH_FIELD_SIZE) {
            throw new IOException("Cannot write object to channel: " +
                                  objectLengthBuffer.capacity() +
                                  " != " +
                                  LENGTH_FIELD_SIZE
            );
        }

        channel.write(ByteBuffer.wrap(objectLengthBytes));
        channel.write(ByteBuffer.wrap(objectBytes));
    }

}
