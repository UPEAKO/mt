package wp.exception;

public class NoteNotExistException extends RuntimeException {
    public NoteNotExistException(String msg) {
        super(msg);
    }
    public NoteNotExistException() {
        super();
    }
}
