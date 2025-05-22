package dk.easv.blsgn.intgrpbelsign.dal.web;

import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.be.ImageWithMeta;

import java.sql.SQLException;
import java.util.List;

public interface IOrderDAO {

        List<ImageWithMeta> getAllImagesWithStatus(int orderId, int itemId);

        void saveImage(int orderId, int itemId, byte[] imageBytes, String viewType) throws SQLException;

        List<byte[]> getImagesForItem(int orderId, int itemId);

        List<Order> getAllOrders();

        void updateImageStatus(int imageId, String status);

        void deleteImage(int imageId);
    }


