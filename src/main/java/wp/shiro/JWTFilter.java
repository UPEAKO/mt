package wp.shiro;

import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JWTFilter extends BasicHttpAuthenticationFilter {

    private final static Logger logger = LoggerFactory.getLogger(JWTFilter.class);

    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        logger.debug("step into");
        HttpServletRequest req = (HttpServletRequest) request;
        // FIXME "Authorization"关键字来自于父类默认实现，此处修改为其它"eg:auth"将导致token传到后面为空，具体原因待debug
        String authorization = req.getHeader("Authorization");
        logger.info("isLoginAttempt-->current token[{}]", authorization);
        return authorization != null;
    }
    
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        logger.debug("step into");
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String authorization = httpServletRequest.getHeader("Authorization");
        JWTToken token = new JWTToken(authorization);
        logger.info("executeLogin-->current token[{}]", authorization);
        // 提交给realm进行登入，如果错误抛出异常并被捕获,登录失败
        getSubject(request, response).login(token);
        return true;
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        logger.debug("step into");
        // 无论是否使用controller中是否@RequiresAuthentication，本方法总执行
        if (isLoginAttempt(request, response)) {
            try {
                executeLogin(request, response);
            } catch (Exception e) {
                // FIXME 暂时直接丢弃异常，使之进入controller进而调用相应的注解处理器，最后执行shiro注解处理器会由于MyRealm的doGetAuthenticationInfo未
                // FIXME 返回princle而对缺少principal抛出UnauthenticatedException错误,被hand401捕获
                //return false;
            }
        }
        return true;
    }

    /**
     * 对跨域提供支持
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        logger.debug("step into");
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        // 跨域时会首先发送一个option请求，这里我们给option请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }
}
