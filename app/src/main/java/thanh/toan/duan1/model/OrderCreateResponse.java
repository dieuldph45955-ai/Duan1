package thanh.toan.duan1.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderCreateResponse {
    @SerializedName("_id")
    private String id;
    
    private Object user;
    private List<ItemResponse> items;
    private Long totalPrice;
    private String status;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static class ItemResponse {
        private Object product; // Nhận cả String ID hoặc Object
        private int quantity;
        
        public Object getProduct() {
            return product;
        }
    }
}
