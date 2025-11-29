package thanh.toan.duan1.request;

import java.util.List;
import java.util.Map;

public class OrderRequest {
    private List<OrderItemRequest> items;
    private String shippingAddress;
    private String phone;
    private String paymentMethod;
    private Boolean paid; // optional: indicate payment success
    private Map<String, Object> paymentResult; // optional: details from payment gateway

    // existing constructor (kept for compatibility)
    public OrderRequest(List<OrderItemRequest> items, String shippingAddress, String phone, String paymentMethod) {
        this.items = items;
        this.shippingAddress = shippingAddress;
        this.phone = phone;
        this.paymentMethod = paymentMethod;
        this.paid = null;
        this.paymentResult = null;
    }

    // new constructor with paid flag
    public OrderRequest(List<OrderItemRequest> items, String shippingAddress, String phone, String paymentMethod, Boolean paid) {
        this.items = items;
        this.shippingAddress = shippingAddress;
        this.phone = phone;
        this.paymentMethod = paymentMethod;
        this.paid = paid;
        this.paymentResult = null;
    }

    // new constructor with paymentResult
    public OrderRequest(List<OrderItemRequest> items, String shippingAddress, String phone, String paymentMethod, Boolean paid, Map<String, Object> paymentResult) {
        this.items = items;
        this.shippingAddress = shippingAddress;
        this.phone = phone;
        this.paymentMethod = paymentMethod;
        this.paid = paid;
        this.paymentResult = paymentResult;
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
