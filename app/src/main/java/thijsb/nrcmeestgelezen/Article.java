package thijsb.nrcmeestgelezen;

import java.io.Serializable;

public class Article implements Serializable {
    private String title;
    private String path;
    private String image;
    private Integer position;

    public Article(String title, String path, String image, Integer position) {
        this.title = title;
        this.path = path;
        this.image = image;
        this.position = position;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
