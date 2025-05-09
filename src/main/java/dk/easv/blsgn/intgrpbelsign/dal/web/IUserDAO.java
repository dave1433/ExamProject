package dk.easv.blsgn.intgrpbelsign.dal.web;

import dk.easv.blsgn.intgrpbelsign.be.Role;
import dk.easv.blsgn.intgrpbelsign.be.User;
import javafx.collections.ObservableList;

import java.util.List;

public interface IUserDAO {

    /**
     * Retrieves all users from the database.
     *
     * @return An ObservableList of User objects.
     */
    ObservableList<User> getAllUsers();
    boolean addUser(User user);
    boolean editUser(User user);
    boolean doesUserNameExist(String username);
    User getUserByUsername(String username);
    List<Role> getAllRoles();

}
