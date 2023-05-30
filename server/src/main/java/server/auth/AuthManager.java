package server.auth;

import java.util.LinkedList;

import core.command.Command;
import core.command.CommandType;
import core.event.Event;
import core.event.EventType;

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
        if (username.equals("ivatolm") && password.equals("passwd")) {
            return "-t-o-k-e-n-";
        } else {
            return "";
        }
    }

}
