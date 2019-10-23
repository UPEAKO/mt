package wp.exception;

public class BadParamException extends RuntimeException{
    public BadParamException(String msg) {
        super(msg);
    }
    public BadParamException() {
        super();
    }
}
