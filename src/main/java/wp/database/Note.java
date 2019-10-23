package wp.database;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
//@IdClass(wp.database.NotePK.class)
@Table(name = "notes_tb")
public class Note {
    @Id
    // 联合主键不能使用下列注解
    // https://stackoverflow.com/questions/46740624/org-springframework-orm-jpa-jpasystemexception-could-not-set-field-value-post
    //@GeneratedValue(strategy= GenerationType.AUTO)
    //@GeneratedValue(strategy= GenerationType.IDENTITY)
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "note_id")
    private Integer id;

    //@Id
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "note_title")
    private String title;

    @Column(name = "note_content")
    private String content;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "create_time")
    private Timestamp createTime;

    public Note(){}

    public Note(Integer userId, String title, String content, Integer categoryId, Timestamp createTime) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
        this.createTime = createTime;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Integer getCategoryId() {
        return categoryId;
    }
}
