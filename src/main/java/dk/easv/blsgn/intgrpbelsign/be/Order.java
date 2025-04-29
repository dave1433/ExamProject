package dk.easv.blsgn.intgrpbelsign.be;


    public class Order {
        private int order_number;
        private int item_number;
        private String image;

        public Order(int orderNumber, int itemNumber, String image) {
            this.order_number = orderNumber;
            this.item_number= itemNumber;
            this.image = image;
        }

        public int getOrderNumber() {
            return order_number;
        }

        public int getOrder_number() {
            return order_number;
        }

        public void setOrder_number(int order_number) {
            this.order_number = order_number;
        }

        public int getItem_number() {
            return item_number;
        }

        public void setItem_number(int item_number) {
            this.item_number = item_number;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public int getItemNumber() {
            return item_number;
        }

        public String getImage() {
            return image;
        }
    }

