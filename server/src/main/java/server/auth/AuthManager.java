package server.auth;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.Random;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import core.command.Command;
import core.command.CommandType;
import core.event.Event;
import core.event.EventType;
import core.models.user.User;
import server.database.HibernateUtil;

/**
 * Class for validating token and request type for being permitted.
 *
 * @author ivatolm
 */
public class AuthManager {

    public static boolean auth(Event event, String token) {
        if (event.getType() == EventType.LoginValidation ||
            event.getType() == EventType.Close)
        {
            return true;
        }

        if (event.getToken() != null &&
            event.getToken().equals(token))
        {
            return true;
        }

        if (event.getType() == EventType.NewCommands) {
            @SuppressWarnings("unchecked")
            LinkedList<Command> commands = (LinkedList<Command>) event.getData();
            if (commands.size() != 1) {
                return false;
            }

            Command cmd = commands.getFirst();
            if (cmd.getType() == CommandType.HELP ||
                cmd.getType() == CommandType.REGISTER)
            {
                return true;
            }
        }

        return false;
    }

    public static String login(String username, String password) {
        User user;
        try {
            SessionFactory factory = HibernateUtil.getSessionFactory();
            Session session = factory.openSession();
            Criteria criteria = session.createCriteria(User.class)
                .add(Restrictions.idEq(username));

            user = (User) criteria.uniqueResult();

            session.close();
        } catch (HibernateException e) {
            System.err.println("Error occured while committing transaction: " + e);
            return null;
        }

        if (user == null) {
            return "";
        }

        if (user.getPassword().equals(getCryptoHash(password))) {
            Random random = new Random();
            return getCryptoHash("" + random.nextInt());
        }

        return "";
    }

    public static String getCryptoHash(String x) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD2");
            byte[] message = md.digest(x.getBytes());
            BigInteger no = new BigInteger(1, message);
            String hash = no.toString(16);

            while (hash.length() < 32) {
                hash = "0" + hash;
            }

            return hash;

        } catch (NoSuchAlgorithmException e) {
            System.err.println("Algorithm not found: " + e);
            return "";
        }
    }

}
