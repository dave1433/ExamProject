package dk.easv.blsgn.intgrpbelsign.be;

public class ImageWithMeta {
    private int id;
    private byte[] imageData;
    private String status;


    public ImageWithMeta(int id, byte[] imageData, String status) {
        this.id = id;
        this.imageData = imageData;
        this.status = status;

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

}
