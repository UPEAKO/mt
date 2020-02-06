package wp.service;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
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
import wp.util.JWTUtil;
import wp.wrap.AddWrap;
import wp.wrap.CategoryWrap;
import wp.wrap.NoteCategoryWrap;
import wp.wrap.NoteWrap;

import java.sql.Timestamp;
import java.util.*;


@Service
public class NoteService {

    private final static Logger logger = LoggerFactory.getLogger(NoteService.class);

    private NoteRepository noteRepository;

    private CategoryRepository categoryRepository;

    @Autowired
    public NoteService(NoteRepository noteRepository, CategoryRepository categoryRepository) {
        this.noteRepository = noteRepository;
        this.categoryRepository = categoryRepository;
    }

    public ResponseBean getNoteList(String user,String searchInfo) {
        logger.debug("step into");

        if (!searchInfo.isEmpty()) {
            return searchNotes(searchInfo);
        }

        Integer userId = getCurrentUserId();
        List<Object[]> notes = noteRepository.findNotes(userId);
        List<Object[]> categories = categoryRepository.findCategories(userId);
        List<NoteCategoryWrap> responseData = new ArrayList<>();
        for (Object[] objects : notes) {
            Integer noteId = (Integer) objects[0];
            String noteTitle = (String) objects[1];
            Integer categoryId = (Integer) objects[2];
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

    public ResponseBean deleteNoteById(Integer id,String user) {
        logger.debug("step into");
        Integer userId = getCurrentUserId();
        Note note = noteRepository.findNoteByIdAndUserId(id,userId);
        if (note == null) {
            throw new BadParamException("note note exist");
        }
        // delete 返回删除的记录数
        Integer deleteNum = noteRepository.deleteNoteByIdAndUserId(id,userId);
        if (deleteNum != 1) {
            logger.warn("delete note num[{}] isn't 1", deleteNum);
            throw new DeleteException("delete note[" + note.getTitle() + "] fail");
        }
        return new ResponseBean(200,"delete note success", deleteNum);
    }

    public ResponseBean updateNoteById(Integer id, AddWrap addWrap,String user) {
        logger.debug("step into");
        Integer userId = getCurrentUserId();
        String title = addWrap.getTitle();
        if (title == null || title.isEmpty()) {
            throw new BadParamException("title not exist,update fail");
        }
        String[] categories = addWrap.getCategories();
        if (categories == null || categories.length == 0) {
            throw new BadParamException("category is empty,update fail");
        }
        Note note = noteRepository.findNoteByIdAndUserId(id, userId);
        if (note == null) {
            throw new BadParamException("note not exist,update fail");
        }
        ArrayList<CategoryWrap> categoryWraps = new ArrayList<>(3);
        Integer parentId = 0;
        // 当某级目录不存在时，之后所有子目录均要新建,新建标志newSubCategory
        boolean newSubCategory = false;
        for (String category : categories) {
            Category categorySearch = categoryRepository.findCategoryByCategoryAndParentIdAndUserId(category,parentId,userId);
            if (categorySearch == null || newSubCategory) {
                newSubCategory = true;
                categorySearch = new Category();
                categorySearch.setCategory(category);
                categorySearch.setCreateTime(new Timestamp(System.currentTimeMillis()));
                categorySearch.setParentId(parentId);
                categorySearch.setUserId(userId);
                categorySearch = categoryRepository.save(categorySearch);
                logger.warn("save {} into categories_tb", category);
            }
            // json序列化时只能保证数组的顺序
            categoryWraps.add(0,new CategoryWrap(categorySearch.getId(),categorySearch.getCategory()));
            parentId = categorySearch.getId();
        }
        note.setTitle(title);
        note.setContent(addWrap.getContent());
        note.setCategoryId(parentId);
        note = noteRepository.save(note);
        return new ResponseBean(200,"update note success",
                new NoteCategoryWrap(note.getId(),title,categoryWraps));
    }

    public ResponseBean addNote(AddWrap addWrap,String user) {
        logger.debug("step into");
        Integer userId = getCurrentUserId();
        String title = addWrap.getTitle();
        if (title == null || title.isEmpty()) {
            throw new BadParamException("title is empty,add fail");
        }
        String[] categories = addWrap.getCategories();
        if (categories == null || categories.length == 0) {
            throw new BadParamException("category is empty,add fail");
        }
        ArrayList<CategoryWrap> categoryWraps = new ArrayList<>(3);
        // 首先顺序遍历查询category,不存在则添加category
        Integer parentId = 0;
        // 当某级目录不存在时，之后所有子目录均要新建,新建标志newSubCategory
        boolean newSubCategory = false;
        for (String category : categories) {
            Category categorySearch = categoryRepository.findCategoryByCategoryAndParentIdAndUserId(category,parentId,userId);
            if (categorySearch == null || newSubCategory) {
                newSubCategory = true;
                categorySearch = new Category();
                categorySearch.setCategory(category);
                categorySearch.setCreateTime(new Timestamp(System.currentTimeMillis()));
                categorySearch.setParentId(parentId);
                categorySearch.setUserId(userId);
                categorySearch = categoryRepository.save(categorySearch);
                logger.info("save {} into categories_tb", category);
            }
            categoryWraps.add(0,new CategoryWrap(categorySearch.getId(),categorySearch.getCategory()));
            parentId = categorySearch.getId();
        }

        Note note = null;
        //当未新建子文件夹时，最后的子目录下无同名note才能新建
        if (!newSubCategory) {
            note = noteRepository.findNotesByTitleAndCategoryIdAndUserId(title,parentId,userId);
            if (note != null) {
                throw new NoteAlreadyExistException("note already exist in the same path!!!");
            }
        }
        note = new Note();
        note.setUserId(userId);
        note.setTitle(addWrap.getTitle());
        note.setContent(addWrap.getContent());
        note.setCategoryId(parentId);
        note.setCreateTime(new Timestamp(System.currentTimeMillis()));
        note = noteRepository.save(note);
        return new ResponseBean(200, "add success",
                new NoteCategoryWrap(note.getId(),note.getTitle(),categoryWraps));
    }

    public ResponseBean getNoteById(Integer id,String user) {
        logger.debug("step into");
        List<Object[]> note = noteRepository.findNote(id, getCurrentUserId());
        if (note == null) {
            logger.warn("note (with id {}) not exist", id);
            throw new NoteNotExistException("note[" + id +"] not exist");
        }
        return new ResponseBean(200, "get note success by id",
                new NoteWrap((Integer) note.get(0)[0], (String) note.get(0)[1], (String) note.get(0)[2]));
    }

    private ResponseBean searchNotes(String searchInfo) {
        logger.debug("step into");
        logger.debug("searchInfo[{}]",searchInfo);
        // TODO 分页查询，标题内容分开查询
        List<Note> notes = noteRepository.findNotesBySearchInfo(getCurrentUserId(),searchInfo);
        if (notes == null || notes.isEmpty()) {
            return new ResponseBean(404, "matching nothing with " + searchInfo, null);
        }
        return new ResponseBean(200,"get search result succeed", notes);
    }

    private int findParentCategory(Integer categoryId, List<Object[]> categories) {
        for (int i = 0; i < categories.size(); i++) {
            if (categoryId.equals(categories.get(i)[0])) {
                return i;
            }
        }
        return -1;
    }

    private int getCurrentUserId() {
        Subject subject = SecurityUtils.getSubject();
        String token = subject.getPrincipal().toString();
        return JWTUtil.getUserId(token);
    }
}
