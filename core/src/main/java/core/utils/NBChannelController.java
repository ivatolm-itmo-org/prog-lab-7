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
        int readCnt = 0;

        ByteBuffer lengthBuffer = ByteBuffer.allocate(LENGTH_FIELD_SIZE);
        while (readCnt != LENGTH_FIELD_SIZE) {
            readCnt += channel.read(lengthBuffer);
        }

        int length = SerializationUtils.deserialize(lengthBuffer.array());

        readCnt = 0;
        ByteBuffer objectBuffer = ByteBuffer.allocate(length);
        while (readCnt != length) {
            readCnt += channel.read(objectBuffer);
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
        int writeCnt;

        byte[] objectBytes = SerializationUtils.serialize(object);
        byte[] objectLengthBytes = SerializationUtils.serialize(objectBytes.length);

        writeCnt = channel.write(ByteBuffer.wrap(objectLengthBytes));
        if (writeCnt != LENGTH_FIELD_SIZE) {
            throw new IOException(
                "Cannot write object to channel: " + writeCnt + " != " + LENGTH_FIELD_SIZE
            );
        }

        writeCnt = channel.write(ByteBuffer.wrap(objectBytes));
        if (writeCnt != objectBytes.length) {
            throw new IOException(
                "Cannot write object to channel: " + writeCnt + " != " + objectBytes.length
            );
        }
    }

}
