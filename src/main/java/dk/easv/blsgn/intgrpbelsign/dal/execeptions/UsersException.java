package dk.easv.blsgn.intgrpbelsign.dal.execeptions;

public class UsersException extends RuntimeException {
    public UsersException(String message) {
        super(message);
    }
    public UsersException(Exception e) {
        super(e);
    }
}
