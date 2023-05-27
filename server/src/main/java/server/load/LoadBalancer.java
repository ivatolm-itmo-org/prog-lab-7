package server.load;

import java.nio.channels.SelectableChannel;
import java.util.HashMap;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.handler.ChannelType;
import core.handler.Handler;

/**
 * Class for managing load on handler-workers.
 *
 * @author ivatolm
 */
public class LoadBalancer {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger("LoadBalancer");

    // Number of workers
    private Integer workersNum;

    // Workers data
    private HashMap<Integer, Worker> workers;
    private HashMap<Integer, Thread> workerThreads;

    private Random random;

    /**
     * Constructs new {@code LoadBalancer} with provided arguments.
     *
     * @param workersNum number of workers to start
     */
    public LoadBalancer(Integer workersNum) {
        this.workersNum = workersNum;

        this.workers = new HashMap<>();
        this.workerThreads = new HashMap<>();

        this.random = new Random();

        for (int i = 0; i < this.workersNum; i++) {
            Worker worker = new Worker();

            Thread workerThread = new Thread(worker);
            workerThread.start();

            this.workers.put(i, worker);
            this.workerThreads.put(i, workerThread);

            System.out.println("Worker " + i + " started.");
        }
    }

    /**
     * Execute processing for incoming handler.
     *
     * @param handler handler for processing
     * @param channelType type of the channel with request
     * @param channel channel with request
     */
    public void process(Handler<ChannelType, ?> handler, ChannelType channelType, SelectableChannel channel) {
        Integer id = this.nextWorkerId();
        this.workers.get(id).addRequest(handler, channelType, channel);

        logger.trace("Sent request to worker " + id);
    }

    /**
     * Closes all workers.
     */
    public void close() {
        for (Integer key : workers.keySet()) {
            this.workers.get(key).close();

            try {
                this.workerThreads.get(key).join();
            } catch (InterruptedException e) {
                System.err.println("Cannot join worker thread.");
            }
        }
    }

    /**
     * Returns id of the next worker.
     * Tries to spread load accross all the workers.
     *
     * @return id of next worker to execute
     */
    private Integer nextWorkerId() {
        return random.nextInt(this.workersNum);
    }

}
