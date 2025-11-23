package thanh.toan.duan1.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class User {

    @SerializedName("_id")
    private String id;

    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private Boolean isActive;
    private Boolean isAdmin;

    private List<Object> wishlist;

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public List<Object> getWishlist() {
        return wishlist;
    }
}
