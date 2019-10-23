package wp.exception;

public class DeleteException extends RuntimeException {
    public DeleteException() {
        super();
    }
    public DeleteException(String msg) {
        super(msg);
    }
}
