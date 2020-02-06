package wp.database;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CategoryRepository extends CrudRepository<Category, Integer> {
    String findCategories = "select category_id, category_parent_id, category_name " +
            "from categories_tb " +
            "where user_id = ?1";

    @Query(nativeQuery = true,value = findCategories)
    List<Object[]> findCategories(Integer userId);

    Category findCategoryByCategoryAndParentIdAndUserId(String category, Integer parentId, Integer userId);
}
