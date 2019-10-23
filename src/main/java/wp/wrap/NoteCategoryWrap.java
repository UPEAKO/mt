package wp.wrap;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * 使用jackson序列化需要get&set方法
 */
public class NoteCategoryWrap {
    private Integer id;

    private String title;

    // key:categoryId
    // value:categoryName
    // 自顶上下查找树形结构目录，最后插入title&id
    private ArrayList<CategoryWrap> categoriesChain;

    public NoteCategoryWrap(){}

    public NoteCategoryWrap(Integer id, String title, ArrayList<CategoryWrap> categoriesChain) {
        this.id = id;
        this.title = title;
        this.categoriesChain = categoriesChain;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<CategoryWrap> getCategoriesChain() {
        return categoriesChain;
    }

    public void setCategoriesChain(ArrayList<CategoryWrap> categoriesChain) {
        this.categoriesChain = categoriesChain;
    }
}
