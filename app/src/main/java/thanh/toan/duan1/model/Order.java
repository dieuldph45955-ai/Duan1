// Order.java

package thanh.toan.duan1.model;

import java.util.Date;
import java.util.List;

public class Order {
    private String id;
    private User user;
    private List<Item> items;
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

    public User getUser() {
        return user;
    }

    public void setUser(User value) {
        this.user = value;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> value) {
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
}

