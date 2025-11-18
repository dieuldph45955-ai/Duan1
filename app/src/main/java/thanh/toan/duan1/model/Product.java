package thanh.toan.duan1.model;

import java.util.Date;
import java.util.List;

public class Product {
    private String id;
    private String name;
    private String description;
    private Long price;
    private String category;
    private List<String> images;
    private List<Object> sizes;
    private Long rating;
    private List<String> reviews;
    private Date createdAt;
    private Long v;

    public String getID() {
        return id;
    }

    public void setID(String value) {
        this.id = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long value) {
        this.price = value;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String value) {
        this.category = value;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> value) {
        this.images = value;
    }

    public List<Object> getSizes() {
        return sizes;
    }

    public void setSizes(List<Object> value) {
        this.sizes = value;
    }

    public Long getRating() {
        return rating;
    }

    public void setRating(Long value) {
        this.rating = value;
    }

    public List<String> getReviews() {
        return reviews;
    }

    public void setReviews(List<String> value) {
        this.reviews = value;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date value) {
        this.createdAt = value;
    }

    public Long getV() {
        return v;
    }

    public void setV(Long value) {
        this.v = value;
    }
}
