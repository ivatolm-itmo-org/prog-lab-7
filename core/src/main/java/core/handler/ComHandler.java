package core.handler;

/**
 * Class for handling network communication logic of the application.
 *
 * @author ivatolm
 */
public abstract class ComHandler<S extends Enum<?>> extends Handler<ChannelType, S> {

    /**
     * Constructs new {@code ComHandler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     * @param initState initial state of FSM
     */
    protected ComHandler(HandlerChannels inputChannels,
                         HandlerChannels outputChannels,
                         S initState) {
        super(inputChannels, outputChannels, initState);
    }

}
