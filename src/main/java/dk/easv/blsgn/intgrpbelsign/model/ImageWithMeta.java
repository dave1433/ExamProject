package dk.easv.blsgn.intgrpbelsign.model;

public class ImageWithMeta {
    private int id;
    private byte[] imageData;
    private String status;
    private int index;

    public ImageWithMeta(int id, byte[] imageData, String status, int index) {
        this.id = id;
        this.imageData = imageData;
        this.status = status;
        this.index = index;
    }

    public int getId() {
        return id;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public String getStatus() {
        return status;
    }

    public int getIndex() {
        return index;
    }
}
