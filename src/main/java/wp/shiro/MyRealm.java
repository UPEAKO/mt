package wp.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wp.database.User;
import wp.database.UserRepository;
import wp.util.JWTUtil;

import java.util.ArrayList;

@Service
public class MyRealm extends AuthorizingRealm {

    private final static Logger logger = LoggerFactory.getLogger(MyRealm.class);

    private UserRepository userRepository;

    private User user;

    public User getUser() {
        logger.debug("function[getUser]");
        return user;
    }

    @Autowired
    public MyRealm(UserRepository userRepository) {
        logger.debug("function[MyRealm]");
        logger.info("create MyRealm,hashcode[{}]",this.hashCode());
        this.userRepository = userRepository;
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        logger.debug("function[supports]");
        return token instanceof JWTToken;
    }

    /**
     * 只有当需要检测用户权限的时候才会调用此方法，例如checkRole,checkPermission
     */
    // FIXME doGetAuthorizationInfo每次请求都重复执行4次
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        logger.debug("function[doGetAuthorizationInfo]");
        String username = JWTUtil.getUsername(principals.toString());
        if (!user.getName().equals(username)) {
            throw new UnauthorizedException();
        }
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        simpleAuthorizationInfo.addRole(user.getRole());
        ArrayList<String> list = new ArrayList<>();
        list.add(user.getPermission());
        simpleAuthorizationInfo.addStringPermissions(list);
        return simpleAuthorizationInfo;
    }

    /**
     * 默认使用此方法进行用户名正确与否验证，错误抛出异常即可。
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
        logger.debug("function[doGetAuthenticationInfo]");
        String token = (String) auth.getCredentials();
        String username = JWTUtil.getUsername(token);
        if (username == null) {
            throw new AuthenticationException("token invalid");
        }
        user = userRepository.findUserByName(username);
        logger.info("myRealm'hashcode[{}]", this.hashCode());
        if (user == null) {
            throw new AuthenticationException("User didn't existed!");
        }
        if (! JWTUtil.verify(token, user.getName(), user.getPassword())) {
            throw new AuthenticationException("Username or password error");
        }

        return new SimpleAuthenticationInfo(token, token, "my_realm");
    }
}
