package dk.easv.blsgn.intgrpbelsign.dal.web;

import dk.easv.blsgn.intgrpbelsign.be.Role;
import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.dal.exceptions.UsersException;
import javafx.collections.ObservableList;

import java.util.List;

public interface IUserDAO {
    List<User> getAllUsers() throws UsersException;

    boolean addUser(User user) throws UsersException;

    boolean editUser(User user) throws UsersException;

    boolean doesUserNameExist(String username) throws UsersException;

    User getUserByUsername(String username) throws UsersException;

    List<Role> getAllRoles() throws UsersException;

    byte[] getSignatureByUserId(int userId) throws UsersException;

    void incrementUsageCount(int userId) throws UsersException;

}