package wp.database;

import java.io.Serializable;

public class CategoryPK implements Serializable {
    private Integer id;

    private Integer userId;

    public CategoryPK(){}

    public CategoryPK(Integer id, Integer userId) {
        this.id = id;
        this.userId = userId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (userId == null ? 0 : userId.hashCode());
        result = PRIME * result + (id == null ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CategoryPK categoryPK = (CategoryPK) obj;
        if (userId == null) {
            if (categoryPK.userId != null) {
                return false;
            }
        } else if (!userId.equals(categoryPK.userId)) {
            return false;
        }
        if (id == null) {
            return categoryPK.id == null;
        } else {
            return id.equals(categoryPK.id);
        }
    }
}
