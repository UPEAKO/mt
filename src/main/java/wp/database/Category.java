package wp.database;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "categories_tb")
public class Category {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "category_parent_id")
    private Integer parentId;

    @Column(name = "category_name")
    private String category;

    @Column(name = "create_time")
    private Timestamp createTime;

    public Category(){}

    public Category(Integer userId, Integer parentId, String category, Timestamp createTime) {
        this.userId = userId;
        this.parentId = parentId;
        this.category = category;
        this.createTime = createTime;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Integer getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
