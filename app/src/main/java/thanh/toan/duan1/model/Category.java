package thanh.toan.duan1.model;

import java.time.OffsetDateTime;

public class Category {
    private String id;
    private String name;
    private String description;
    private String image;
    private OffsetDateTime createdAt;
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

    public String getImage() {
        return image;
    }

    public void setImage(String value) {
        this.image = value;
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
