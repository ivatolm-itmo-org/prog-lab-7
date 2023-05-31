package core.models.user;

import lombok.Getter;
import lombok.Setter;

/**
 * Data structure for User.
 *
 * @author ivatolm
 */
public class User {

    /** Username of the user */
    @Setter
    @Getter
    private String username;

    /** Password of the user */
    @Setter
    @Getter
    private String password;

    /**
     * Constructs dummy-instance of the class.
     */
    public User() {}

}
