package server.database;

import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Utility class for managing interactions with Hibernate.
 *
 * @author ivatolm
 */
public class HibernateUtil {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger("HibernateUtil");

    // XML based configuration
    private static SessionFactory sessionFactory;

    // Flag controlling whether port forwarding is done
    private static boolean portForwardingFlag;

    // Connecting to server by ssh and setting up port forwarding
    public static void setupPortForwarding() {
        logger.debug("Setting up port forwarding for db access...");

        logger.trace("Pulling environment variables...");
        String sshHostVar = System.getenv("SSH_HOST");
        String sshPortVar = System.getenv("SSH_PORT");
        String sshUsernameVar = System.getenv("SSH_USERNAME");
        String sshPasswordVar = System.getenv("SSH_PASSWORD");
        String sshForwardPortVar = System.getenv("SSH_FORWARD_PORT");

        int sshPort = Integer.parseInt(sshPortVar);
        int sshForwardPort = Integer.parseInt(sshForwardPortVar);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");

        JSch jsch = new JSch();
        Session jschSession;
        try {
            jschSession = jsch.getSession(sshUsernameVar, sshHostVar, sshPort);
            logger.trace("Jsch session created.");

            jschSession.setPassword(sshPasswordVar);
            jschSession.setConfig(config);
            jschSession.connect();
            logger.trace("Jsch session connected.");

            jschSession.setPortForwardingL(sshForwardPort, sshHostVar, sshForwardPort);
        } catch (JSchException e) {
            logger.warn("Cannot connect to server by ssh.");
            return;
        }

        logger.debug("Port forwarding was set up.");
        portForwardingFlag = true;
    }

    // Creates session factory from XML configuration
    private static SessionFactory buildSessionFactory() {
        logger.debug("Creating new session factory...");

        try {
        	Configuration configuration = new Configuration();
        	configuration.configure("database/hibernate.cfg.xml");
        	logger.trace("Hibernate Configuration loaded");

        	ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();
        	logger.trace("Hibernate serviceRegistry created");

        	SessionFactory sessionFactory = configuration.buildSessionFactory(serviceRegistry);

            logger.debug("Session factory created.");
            return sessionFactory;

        } catch (Throwable ex) {
            logger.warn("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Returns session factory.
     *
     * @return session factory
     */
	public static SessionFactory getSessionFactory() {
		logger.debug("Session factory requested.");

        if (!portForwardingFlag) {
            logger.error("Port forwarding must be done first.");
            return null;
        }

        if (sessionFactory == null) {
            sessionFactory = buildSessionFactory();
        }

        return sessionFactory;
    }

}
