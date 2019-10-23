package wp.wrap;

public class AddWrap {

    private String title;

    private String content;

    private String[] categories;

    public AddWrap(String title, String content, String[] categories) {
        this.title = title;
        this.content = content;
        this.categories = categories;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String[] getCategories() {
        return categories;
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }
}
