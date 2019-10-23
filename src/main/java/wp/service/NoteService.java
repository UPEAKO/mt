package wp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wp.bean.ResponseBean;
import wp.database.Category;
import wp.database.CategoryRepository;
import wp.database.Note;
import wp.database.NoteRepository;
import wp.exception.BadParamException;
import wp.exception.DeleteException;
import wp.exception.NoteAlreadyExistException;
import wp.exception.NoteNotExistException;
import wp.shiro.MyRealm;
import wp.wrap.AddWrap;
import wp.wrap.CategoryWrap;
import wp.wrap.NoteCategoryWrap;
import wp.wrap.NoteWrap;

import java.sql.Timestamp;
import java.util.*;

/**
 * delete or update 可能抛弃部分category
 * 不影响分类，只是无用数据冗余，但减少查询及响应时间
 * 被抛弃的category在下次新建同名分类时直接被启用
 */
@Service
public class NoteService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private NoteRepository noteRepository;

    private CategoryRepository categoryRepository;

    private MyRealm myRealm;

    @Autowired
    public NoteService(NoteRepository noteRepository, CategoryRepository categoryRepository, MyRealm myRealm) {
        this.noteRepository = noteRepository;
        this.categoryRepository = categoryRepository;
        this.myRealm = myRealm;
    }

    /**
     * 1.获取当前user下的所有note_id,note_title,category_id from notes_tb  (List<Object[]>形式)
     * 2.获取当前user下的所有category_id,category_parent_id,category_name from categories_tb  (List<Object[]>形式)
     * 3.查找组建categoriesChain
     * @return bean
     */
    public ResponseBean getNoteList() {
        logger.info("myRealm'hashcode: {}", myRealm.hashCode());
        Integer userId = myRealm.getUser().getId();
        // note_id, note_title, category_id
        List<Object[]> notes = noteRepository.findNotes(userId);
        // category_id, category_parent_id, category_name
        List<Object[]> categories = categoryRepository.findCategories(userId);
        List<NoteCategoryWrap> responseData = new ArrayList<>();
        for (Object[] objects : notes) {
            Integer noteId = (Integer) objects[0];
            String noteTitle = (String) objects[1];
            Integer categoryId = (Integer) objects[2];
            //通过categoryId向上溯源到id==0
            ArrayList<CategoryWrap> categoriesChain = new ArrayList<>();
            while (categoryId != 0) {
                int categoriesLocation = findParentCategory(categoryId,categories);
                String categoryName = (String) categories.get(categoriesLocation)[2];
                categoriesChain.add(new CategoryWrap(categoryId, categoryName));
                categoryId = (Integer) categories.get(categoriesLocation)[1];
            }
            responseData.add(new NoteCategoryWrap(noteId,noteTitle,categoriesChain));
        }
        return new ResponseBean(200, "get list success", responseData);
    }

    /**
     * 通过sql日志可知jpa删除会先执行查询，delete函数返回删除的记录数（Integer)
     * @param id noteId
     * @return bean
     */
    public ResponseBean deleteNoteById(Integer id) {
        Integer userId = myRealm.getUser().getId();
        Note note = noteRepository.findNoteByIdAndUserId(id,userId);
        if (note == null) {
            throw new BadParamException();
        }
        Integer deleteNum = noteRepository.deleteNoteByIdAndUserId(id,userId);
        if (deleteNum != 1) {
            throw new DeleteException();
        }
        return new ResponseBean(200,"delete note success", deleteNum);
    }

    /**
     *
     * @param id noteId
     * @param addWrap 上传json数据
     * @return getList相同数据类型NoteCategoryWrap
     */
    public ResponseBean updateNoteById(Integer id, AddWrap addWrap) {
        Integer userId = myRealm.getUser().getId();
        String title = addWrap.getTitle();
        if (title == null || title.isEmpty()) {
            throw new BadParamException();
        }
        String[] categories = addWrap.getCategories();
        if (categories == null || categories.length == 0) {
            throw new BadParamException();
        }
        Note note = noteRepository.findNoteByIdAndUserId(id, userId);
        if (note == null) {
            throw new BadParamException();
        }
        // 修改目录树
        ArrayList<CategoryWrap> categoryWraps = new ArrayList<>(3);
        // 首先顺序遍历查询category,不存在则添加category
        Integer parentId = 0;
        for (String category : categories) {
            Category categorySearch = categoryRepository.findCategoryByCategoryAndUserId(category,userId);
            if (categorySearch == null) {
                categorySearch = new Category();
                categorySearch.setCategory(category);
                categorySearch.setCreateTime(new Timestamp(System.currentTimeMillis()));
                categorySearch.setParentId(parentId);
                categorySearch.setUserId(userId);
                categorySearch = categoryRepository.save(categorySearch);
            }
            // 由于fastJson序列化时只能保证数组的顺序，故未用LinkedList添加到队首（同getList统一顺序格式）
            categoryWraps.add(0,new CategoryWrap(categorySearch.getId(),categorySearch.getCategory()));
            parentId = categorySearch.getId();
        }
        // 将最后的parentId值作为note_tb中的category_id
        note.setTitle(title);
        note.setContent(addWrap.getContent());
        note.setCategoryId(parentId);
        note = noteRepository.save(note);
        return new ResponseBean(200,"update note success",
                new NoteCategoryWrap(note.getId(),title,categoryWraps));
    }

    public ResponseBean addNote(AddWrap addWrap) {
        Integer userId = myRealm.getUser().getId();
        String title = addWrap.getTitle();
        if (title == null || title.isEmpty()) {
            throw new BadParamException();
        }
        String[] categories = addWrap.getCategories();
        if (categories == null || categories.length == 0) {
            throw new BadParamException();
        }
        Note note = noteRepository.findNoteByTitleAndUserId(title, userId);
        if (note != null) {
            throw new NoteAlreadyExistException();
        }
        ArrayList<CategoryWrap> categoryWraps = new ArrayList<>(3);
        // 首先顺序遍历查询category,不存在则添加category
        Integer parentId = 0;
        for (String category : categories) {
            Category categorySearch = categoryRepository.findCategoryByCategoryAndUserId(category,userId);
            if (categorySearch == null) {
                categorySearch = new Category();
                categorySearch.setCategory(category);
                categorySearch.setCreateTime(new Timestamp(System.currentTimeMillis()));
                categorySearch.setParentId(parentId);
                categorySearch.setUserId(userId);
                logger.info("查询间隔");
                categorySearch = categoryRepository.save(categorySearch);
                // 注意此处category id 事先未设定,存储返回结果仍为null,与note.java中联合主键注解矛盾
                //categorySearch = categoryRepository.findCategoryByCategoryAndUserId(category,userId);
            }
            // 由于fastJson序列化时只能保证数组的顺序，故未用LinkedList添加到队首（同getList统一顺序格式）
            categoryWraps.add(0,new CategoryWrap(categorySearch.getId(),categorySearch.getCategory()));
            parentId = categorySearch.getId();
        }
        // 将最后的parentId值作为note_tb中的category_id
        note = new Note();
        note.setUserId(userId);
        note.setTitle(addWrap.getTitle());
        note.setContent(addWrap.getContent());
        note.setCategoryId(parentId);
        note.setCreateTime(new Timestamp(System.currentTimeMillis()));
        note = noteRepository.save(note);
        // 注意此处note id 事先未设定,存储返回结果仍为null,与note.java中联合主键注解矛盾
        //note = noteRepository.findNoteByTitleAndUserId(addWrap.getTitle(), userId);
        return new ResponseBean(200, "add success",
                new NoteCategoryWrap(note.getId(),note.getTitle(),categoryWraps));
    }

    public ResponseBean getNoteById(Integer id) {
        List<Object[]> note = noteRepository.findNote(id, myRealm.getUser().getId());
        if (note == null) {
            logger.info("该note不存在，note_id: {}", id);
            throw new NoteNotExistException();
        }
        return new ResponseBean(200, "get note success by id",
                new NoteWrap((Integer) note.get(0)[0], (String) note.get(0)[1], (String) note.get(0)[2]));
    }

    private int findParentCategory(Integer categoryId, List<Object[]> categories) {
        for (int i = 0; i < categories.size(); i++) {
            if (categoryId.equals(categories.get(i)[0])) {
                return i;
            }
        }
        return -1;
    }
}
