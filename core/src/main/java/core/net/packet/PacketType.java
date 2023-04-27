package core.net.packet;

/**
 * Enum of types of packets that communicators work with.
 *
 * @author ivatolm
 */
public enum PacketType {
    IdValidation,
    NewCommands,
    ScriptRequest,
    OutputResponse
}
