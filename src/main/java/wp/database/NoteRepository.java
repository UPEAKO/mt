package wp.database;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface NoteRepository extends CrudRepository<Note, Integer> {
    // native sql语句设置自定义查询方式，?1表示第一个参数,可为无需查询的参数设定默认字段

    String findNotes = "select note_id, note_title, category_id from notes_tb where user_id = ?1";

    String findNote = "select note_id, note_title, note_content from notes_tb where note_id = ?1 and user_id = ?2";

    @Query(nativeQuery = true, value = findNotes)
    List<Object[]> findNotes(Integer userId);

    @Query(nativeQuery = true, value = findNote)
    List<Object[]> findNote(Integer noteId, Integer userId);

    Note findNoteByTitleAndUserId(String title, Integer userId);

    Note findNoteByIdAndUserId(Integer id, Integer userId);

    @Transactional
    Integer deleteNoteByIdAndUserId(Integer id, Integer userId);
}
