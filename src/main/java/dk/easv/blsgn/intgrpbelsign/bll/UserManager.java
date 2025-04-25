package dk.easv.blsgn.intgrpbelsign.bll;

import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.dal.UserDAO;

import java.util.List;

public class UserManager {
    private final UserDAO userDAO = new UserDAO();

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }
}
