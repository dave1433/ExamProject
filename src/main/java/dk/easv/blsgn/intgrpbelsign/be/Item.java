package dk.easv.blsgn.intgrpbelsign.be;

public class Item {
    private int id;
    private String itemId; // Optional external ID (e.g. "ITM-001")
    private String itemName;
    private int orderId;

    private String materialsUsed;
    private String approxQuantity;
    private int totalWeight; // in grams

    // Full constructor
    public Item(int id, String itemName, int orderId, String materialsUsed, String approxQuantity, int totalWeight) {
        this.id = id;
        this.itemName = itemName;
        this.orderId = orderId;
        this.materialsUsed = materialsUsed;
        this.approxQuantity = approxQuantity;
        this.totalWeight = totalWeight;
    }

    // Constructor without materials (used if material info is not fetched yet)
    public Item(int id, String itemName, int orderId) {
        this(id, itemName, orderId, "N/A", "N/A", 0);
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

    public String getMaterialsUsed() {
        return materialsUsed;
    }

    public String getApproxQuantity() {
        return approxQuantity;
    }

    public int getTotalWeight() {
        return totalWeight;
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

    public void setMaterialsUsed(String materialsUsed) {
        this.materialsUsed = materialsUsed;
    }

    public void setApproxQuantity(String approxQuantity) {
        this.approxQuantity = approxQuantity;
    }

    public void setTotalWeight(int totalWeight) {
        this.totalWeight = totalWeight;
    }

    private boolean isSubmitted;

    public boolean isSubmitted() {
        return isSubmitted;
    }

    public void setSubmitted(boolean submitted) {
        isSubmitted = submitted;
    }
}
