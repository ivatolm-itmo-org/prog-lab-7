package core.event;

/**
 * Enum of types of events that used in client and server.
 *
 * @author ivatolm
 */
public enum EventType {
    LoginValidation,
    IdValidation,
    NewCommands,
    ScriptRequest,
    OutputResponse,
    ConnectionTimeout,
    PingTimeout,
    Ping,
    AuthError,
    Close
}
