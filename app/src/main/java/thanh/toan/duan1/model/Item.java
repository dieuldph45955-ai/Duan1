package thanh.toan.duan1.model;

public class Item {
    private Product product;
    private Long quantity;
    private String id;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product value) {
        this.product = value;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long value) {
        this.quantity = value;
    }

    public String getID() {
        return id;
    }

    public void setID(String value) {
        this.id = value;
    }
}
