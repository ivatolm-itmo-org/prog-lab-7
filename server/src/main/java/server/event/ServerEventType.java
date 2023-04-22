package server.event;

/**
 * Enum of types of server events that used in server internally.
 *
 * @author ivatolm
 */
public enum ServerEventType {
    IdValidationReq,
    IdValidationResp,
    NewCommandsReq,
    NewCommandsResp,
    ScritpReq,
    SendDataReq,
}
