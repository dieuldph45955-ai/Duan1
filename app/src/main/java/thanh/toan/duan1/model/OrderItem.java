package thanh.toan.duan1.model;

import java.util.Map;

public class OrderItem {
    private Object product;
    private Long quantity;
    private String id;

    public Object getProduct() {
        return product;
    }

    public void setProduct(Object product) {
        this.product = product;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductName() {
        if (product == null) return "Sản phẩm không tồn tại";
        
        if (product instanceof String) {
            return "Sản phẩm (Đang cập nhật)";
        }
        
        if (product instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) product;
            Object name = map.get("name");
            return name != null ? name.toString() : "Sản phẩm không tên";
        }
        
        // Trường hợp Gson parse được thành Product (nếu cấu hình custom adapter, nhưng ở đây default là Map)
        if (product instanceof Product) {
            return ((Product) product).getName();
        }

        return "Sản phẩm";
    }
}
