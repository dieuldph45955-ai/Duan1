package thanh.toan.duan1.request;

import java.util.List;

public class OrderRequest {
    private List<OrderItemRequest> items;
    private String shippingAddress;
    private String phone;
    private String paymentMethod;

    public OrderRequest(List<OrderItemRequest> items, String shippingAddress, String phone, String paymentMethod) {
        this.items = items;
        this.shippingAddress = shippingAddress;
        this.phone = phone;
        this.paymentMethod = paymentMethod;
    }

    public static class OrderItemRequest {
        private String product;
        private int quantity;

        public OrderItemRequest(String product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }
}
