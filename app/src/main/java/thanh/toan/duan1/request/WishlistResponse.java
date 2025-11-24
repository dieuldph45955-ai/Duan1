package thanh.toan.duan1.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import thanh.toan.duan1.model.Product;

public class WishlistResponse {
    private boolean success;
    @SerializedName("data")
    private DataClass data;

    public static class DataClass {
        @SerializedName("userId")
        private String userId;

        @SerializedName("wishlist")
        private List<Product> wishlist;

        @SerializedName("count")
        private int count;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public List<Product> getWishlist() {
            return wishlist;
        }

        public void setWishlist(List<Product> wishlist) {
            this.wishlist = wishlist;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public DataClass getData() {
        return data;
    }

    public void setData(DataClass data) {
        this.data = data;
    }
}