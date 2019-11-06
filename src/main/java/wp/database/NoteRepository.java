package wp.database;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface NoteRepository extends CrudRepository<Note, Integer> {

    String findNotes = "select note_id, note_title, category_id from notes_tb where user_id = ?1";

    String findNote = "select note_id, note_title, note_content from notes_tb where note_id = ?1 and user_id = ?2";

    String findNotesBySearchInfo = "select * from notes_tb where user_id = ?1 and (note_title like %?2% or note_content like %?2%)";

    @Query(nativeQuery = true, value = findNotes)
    List<Object[]> findNotes(Integer userId);

    @Query(nativeQuery = true, value = findNote)
    List<Object[]> findNote(Integer noteId, Integer userId);

    @Query(nativeQuery = true, value = findNotesBySearchInfo)
    List<Note> findNotesBySearchInfo(Integer userId,String info);

    Note findNoteByTitleAndUserId(String title, Integer userId);

    Note findNoteByIdAndUserId(Integer id, Integer userId);

    @Transactional
    Integer deleteNoteByIdAndUserId(Integer id, Integer userId);
}
