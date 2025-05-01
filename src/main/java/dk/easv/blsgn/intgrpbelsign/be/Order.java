package dk.easv.blsgn.intgrpbelsign.be;


import java.util.List;

public class Order {
    private int id;
    private String orderNumber;
    private List<Item> items;

    public Order(int id, String orderNumber, List<Item> items) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.items = items;
    }

    // Getters
    public int getID() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public List<Item> getItems() {
        return items;
    }

    // Setters
    public void setID(int id) {
        this.id = id;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}

