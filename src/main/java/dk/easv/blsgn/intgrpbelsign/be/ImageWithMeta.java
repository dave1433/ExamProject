package dk.easv.blsgn.intgrpbelsign.be;

public class ImageWithMeta {
    private int id;
    private byte[] imageData;
    private String status;
    private String viewType;


    public ImageWithMeta(int id, byte[] imageData, String status , String viewType) {
        this.id = id;
        this.imageData = imageData;
        this.status = status;
        this.viewType = viewType;

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

    public String getViewType() {return viewType;}

    public void setId(int id) {
        this.id = id;
    }
}
