package server.balancer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for managing load on handler-workers.
 *
 * @author ivatolm
 */
public class Balancer {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger("LoadBalancer");

    // Executor with workers
    private ExecutorService executorService;

    /**
     * Constructs new {@code LoadBalancer} with provided arguments.
     */
    public Balancer(Integer workersNum) {
        this.executorService = Executors.newFixedThreadPool(workersNum);
    }

    /**
     * Execute processing for incoming task.
     */
    public void process(BalancerTask task) {
        this.executorService.execute(task);

        logger.trace("Task was submitted to executor service.");
    }

    /**
     * Closes balancer.
     */
    public void close() {
        this.executorService.shutdown();
    }

}
