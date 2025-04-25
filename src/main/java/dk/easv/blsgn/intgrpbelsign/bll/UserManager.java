package dk.easv.blsgn.intgrpbelsign.bll;

import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.dal.web.UserDAO;
import javafx.collections.ObservableList;

public class UserManager {
    private final UserDAO userDAO = new UserDAO();
    public ObservableList<User> getAllUsers() {
        return userDAO.getAllUsers();
    }
}
