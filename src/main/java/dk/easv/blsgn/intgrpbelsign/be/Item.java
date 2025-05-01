package dk.easv.blsgn.intgrpbelsign.be;

public class Item {
    private int id;
    private String itemId;     // This can be something like "ITM-001"
    private String itemName;

    // Optional: Link to order ID if needed
    private int orderId;

    public Item(int id, String itemId, String itemName, int orderId) {
        this.id = id;
        this.itemId = itemId;
        this.itemName = itemName;
        this.orderId = orderId;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getItemId() {
        return itemId;
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

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
}