package dk.easv.blsgn.intgrpbelsign.be;

public class Item {
    private int id;
    private String itemId; // Optional external ID (e.g. "ITM-001")
    private String itemName;
    private int orderId;



    // Full constructor
    public Item(int id, String itemName, int orderId) {
        this.id = id;
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

    private boolean isSubmitted;

    public boolean isSubmitted() {
        return isSubmitted;
    }

    public void setSubmitted(boolean submitted) {
        isSubmitted = submitted;
    }
}
