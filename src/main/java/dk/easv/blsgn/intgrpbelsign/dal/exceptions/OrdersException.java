package dk.easv.blsgn.intgrpbelsign.dal.exceptions;

public class OrdersException extends RuntimeException {
    public OrdersException(String message) {
        super(message);
    }
    public OrdersException(Exception e) {
        super(e);
    }

}