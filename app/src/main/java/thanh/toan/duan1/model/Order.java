// Order.java

package thanh.toan.duan1.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Order {
    @SerializedName("_id")
    private String id;
    // possible server-side code fields
    private String code;
    @SerializedName("orderCode")
    private String orderCode;
    // user can be either a String id or an object; keep as Object to avoid parse errors
    private Object user;
    private List<OrderItem> items;
    private Long totalPrice;
    private String status;
    private String shippingAddress;
    private String phone;
    private String paymentMethod;
    private Date createdAt;
    private Date updatedAt;
    private Long v;

    public String getID() {
        return id;
    }

    public void setID(String value) {
        this.id = value;
    }

    // New: return server-provided order code if present, else null
    public String getCode() {
        if (code != null && !code.isEmpty()) return code;
        if (orderCode != null && !orderCode.isEmpty()) return orderCode;
        return null;
    }

    public void setCode(String value) { this.code = value; }

    // raw user object (may be String id, Map or User)
    public Object getUserRaw() {
        return user;
    }

    // Setter accepts flexible types
    public void setUser(Object value) {
        this.user = value;
    }

    // Try to extract user id regardless of type
    public String getUserId() {
        if (user == null) return null;
        if (user instanceof String) return (String) user;
        if (user instanceof Map) {
            Map map = (Map) user;
            Object idObj = null;
            if (map.containsKey("_id")) idObj = map.get("_id");
            if (idObj == null && map.containsKey("id")) idObj = map.get("id");
            return idObj != null ? idObj.toString() : null;
        }
        if (user instanceof User) return ((User) user).getId();
        // fallback
        return user.toString();
    }

    // Try to extract user display name
    public String getUserDisplayName() {
        if (user == null) return null;
        if (user instanceof String) return (String) user;
        if (user instanceof Map) {
            Map map = (Map) user;
            Object name = null;
            if (map.containsKey("fullName")) name = map.get("fullName");
            if (name == null && map.containsKey("username")) name = map.get("username");
            if (name == null && map.containsKey("email")) name = map.get("email");
            return name != null ? name.toString() : getUserId();
        }
        if (user instanceof User) {
            User u = (User) user;
            if (u.getFullName() != null) return u.getFullName();
            if (u.getUsername() != null) return u.getUsername();
            if (u.getEmail() != null) return u.getEmail();
            return u.getId();
        }
        return user.toString();
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> value) {
        this.items = value;
    }

    public Long getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Long value) {
        this.totalPrice = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String value) {
        this.shippingAddress = value;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String value) {
        this.phone = value;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String value) {
        this.paymentMethod = value;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date value) {
        this.createdAt = value;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date value) {
        this.updatedAt = value;
    }

    public Long getV() {
        return v;
    }

    public void setV(Long value) {
        this.v = value;
    }
    public String getDisplayCode() {
        if (code != null && !code.isEmpty()) {
            return code.toLowerCase(java.util.Locale.ROOT);
        }
        if (orderCode != null && !orderCode.isEmpty()) {
            return orderCode.toLowerCase(java.util.Locale.ROOT);
        }
        // Fallback: nếu server không gửi code, dùng 8 ký tự đầu của ID in lowercase
        if (id != null && id.length() >= 8) {
            return id.substring(0, 8).toLowerCase(java.util.Locale.ROOT);
        }
        return id != null ? id.toLowerCase(java.util.Locale.ROOT) : "";
    }
}
