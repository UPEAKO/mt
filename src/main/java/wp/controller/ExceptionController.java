package wp.controller;

import org.apache.shiro.ShiroException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import wp.bean.ResponseBean;
import wp.exception.BadParamException;
import wp.exception.NoteAlreadyExistException;
import wp.exception.NoteNotExistException;
import wp.exception.UnauthorizedException;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ExceptionController {

    private final static Logger logger = LoggerFactory.getLogger(ExceptionController.class);

    // 捕捉shiro的异常
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(ShiroException.class)
    public ResponseBean handle401(ShiroException e) {
        logger.debug("step into");
        logger.warn(e.getMessage());
        return new ResponseBean(401, e.getMessage() != null ? e.getMessage() : "shiro exception", null);
    }

    // 捕捉UnauthorizedException
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseBean handle401(UnauthorizedException e) {
        logger.debug("step into");
        logger.warn("Unauthorized");
        return new ResponseBean(401, e.getMessage() != null ? e.getMessage() : "Unauthorized", null);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoteNotExistException.class)
    public ResponseBean handle404(NoteNotExistException e) {
        logger.debug("step into");
        logger.warn("note not exist");
        return new ResponseBean(404, e.getMessage() != null ? e.getMessage() : "note not exist", null);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(NoteAlreadyExistException.class)
    public ResponseBean handle409(NoteAlreadyExistException e) {
        logger.debug("step into");
        logger.warn("note already exist");
        return new ResponseBean(409, e.getMessage() != null ? e.getMessage() : "note already exist", null);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BadParamException.class})
    public ResponseBean handle400(BadParamException e) {
        logger.debug("step into");
        logger.warn("wrong param");
        return new ResponseBean(400, e.getMessage() != null ? e.getMessage() : "wrong param", null);
    }

    // 捕捉其他所有异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseBean globalException(HttpServletRequest request, Throwable ex) {
        logger.debug("step into");
        logger.warn("other Exception");
        return new ResponseBean(getStatus(request).value(), ex.getMessage(), "other Exception");
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        logger.debug("step into");
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.valueOf(statusCode);
    }
}

