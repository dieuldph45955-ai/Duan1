package thanh.toan.duan1.model;


import java.time.OffsetDateTime;
import java.util.List;

public class User {
    private String id;
    private String username;
    private String email;
    private Boolean isActive;
    private Boolean isAdmin;
    private List<Object> wishlist;
    private OffsetDateTime createdAt;
    private Long v;

    public String getID() {
        return id;
    }

    public void setID(String value) {
        this.id = value;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String value) {
        this.username = value;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        this.email = value;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean value) {
        this.isActive = value;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean value) {
        this.isAdmin = value;
    }

    public List<Object> getWishlist() {
        return wishlist;
    }

    public void setWishlist(List<Object> value) {
        this.wishlist = value;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime value) {
        this.createdAt = value;
    }

    public Long getV() {
        return v;
    }

    public void setV(Long value) {
        this.v = value;
    }
}
