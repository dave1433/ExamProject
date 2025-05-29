package dk.easv.blsgn.intgrpbelsign.dal.web;

import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.be.ImageWithMeta;
import dk.easv.blsgn.intgrpbelsign.dal.exceptions.OrdersException;

import java.util.List;

public interface IOrderDAO {

        List<ImageWithMeta> getAllImagesWithStatus(int orderId, int itemId) throws OrdersException;

        void saveImage(int orderId, int itemId, byte[] imageBytes, String viewType) throws OrdersException;

        List<Order> getAllOrders()throws OrdersException;

        void updateImageStatus(int imageId, String status)throws OrdersException;

        void deleteImage(int imageId) throws OrdersException;

}


