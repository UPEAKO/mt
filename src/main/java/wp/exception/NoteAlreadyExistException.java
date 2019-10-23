package wp.exception;

public class NoteAlreadyExistException extends RuntimeException {
    public NoteAlreadyExistException(String msg) {
        super(msg);
    }

    public NoteAlreadyExistException() {
        super();
    }
}
