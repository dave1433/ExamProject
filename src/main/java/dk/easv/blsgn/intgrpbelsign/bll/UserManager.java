package dk.easv.blsgn.intgrpbelsign.bll;

import dk.easv.blsgn.intgrpbelsign.be.Role;
import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.dal.web.UserDAO;
import dk.easv.blsgn.intgrpbelsign.utils.PasswordUtils;
import javafx.collections.ObservableList;

import javax.security.sasl.AuthenticationException;
import java.sql.SQLException;
import java.util.List;

import static dk.easv.blsgn.intgrpbelsign.utils.PasswordUtils.hashPassword;

public class UserManager {
    private final UserDAO userDAO = new UserDAO();
    public ObservableList<User> getAllUsers() {
        return userDAO.getAllUsers();
    }
    /**
     * Validates user credentials
     * @param username The username to validate
     * @param password The plain text password to validate
     * @return User object if credentials are valid, null otherwise
     * @throws AuthenticationException if there's an error during authentication
     */
    public User validateUser(String username, String password) throws AuthenticationException {
        // First get the user by username to retrieve stored hash
        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            return null;
        }
        
        // Then verify the password
        if (PasswordUtils.checkPassword(password, user.getPassword_hash())) {
            return user;
        }
        return null;
    }

    public boolean doesUserNameExist(String username) {
        return userDAO.doesUserNameExist(username);
    }

    public boolean addUser(User user, String password) {
        user.setPassword_hash(hashPassword(password));
        return userDAO.addUser(user);
    }

    public boolean editUser(User user, String password) {
        user.setPassword_hash(hashPassword(password));
        return userDAO.editUser(user);
    }

    public List<Role> getAllRoles() {
        return userDAO.getAllRoles();
    }
}