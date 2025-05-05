package dk.easv.blsgn.intgrpbelsign.be;

public class Item {
    private int id;
    private String itemId;     // This can be something like "ITM-001"
    private String itemName;

    // Optional: Link to order ID if needed
    private int orderId;

    public Item(int id, String itemName, int orderId) {
        this.id = id;
        this.itemName = itemName;
        this.orderId = orderId;
    }

    // Getters
    public int getId() {
        return id;
    }


    public String getItemName() {
        return itemName;
    }

    public int getOrderId() {
        return orderId;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }


    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
}