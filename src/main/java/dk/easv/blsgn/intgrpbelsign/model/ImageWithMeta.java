package dk.easv.blsgn.intgrpbelsign.model;

public class ImageWithMeta {
    private byte[] imageData;
    private String status;
    private int index;

    public ImageWithMeta(byte[] imageData, String status, int index) {
        this.imageData = imageData;
        this.status = status;
        this.index = index;
    }

    public byte[] getImageData() { return imageData; }
    public String getStatus() { return status; }
    public int getIndex() { return index; }
}
