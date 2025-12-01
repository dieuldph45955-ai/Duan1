package thanh.toan.duan1.model;

import java.util.Map;
import java.util.Objects;
import thanh.toan.duan1.model.Product;

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

        // Nếu chỉ là id chuỗi
        if (product instanceof String) {
            return "Sản phẩm (Đang cập nhật)";
        }

        // Nếu Gson parse thành Product object
        if (product instanceof Product) {
            String name = ((Product) product).getName();
            return name != null ? name : "Sản phẩm không tên";
        }

        // Nếu Gson parse thành Map (thông thường khi server trả object)
        if (product instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) product;
            // trường hợp product: { product: { name: ... } } hoặc trực tiếp { name: ... }
            Object nested = map.get("product");
            if (nested instanceof Map) {
                Object name = ((Map<?, ?>) nested).get("name");
                if (name != null) return name.toString();
            }
            Object name = map.get("name");
            if (name != null) return name.toString();

            // thử các khóa khác
            Object title = map.get("title");
            if (title != null) return title.toString();

            return "Sản phẩm không tên";
        }

        // Fallback
        return Objects.toString(product, "Sản phẩm");
    }
}
