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
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Consumer;

@Service
public class MyRealm extends AuthorizingRealm {

    private final static Logger logger = LoggerFactory.getLogger(MyRealm.class);

    private HashMap<String,User> cacheUsers = new HashMap<>(1);

    @Autowired
    public MyRealm(UserRepository userRepository) {
        logger.debug("step into");
        Iterable<User> users = userRepository.findAll();
        users.forEach(user -> {
            cacheUsers.put(user.getName(),user);
            logger.warn("put user[{}]  from db to cacheUsers",user.getName());
        });
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        logger.debug("step into");
        return token instanceof JWTToken;
    }

    /**
     * 只有当需要检测用户权限的时候才会调用此方法，例如checkRole,checkPermission
     */
    // FIXME doGetAuthorizationInfo每次请求都重复执行4次
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        logger.debug("step into");
        // FIXME the principal is token,not userName
        String username = JWTUtil.getUsername(principals.toString());
        User user = cacheUsers.get(username);
        if (user == null || !user.getName().equals(username)) {
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
        logger.debug("step into");
        String token = (String) auth.getCredentials();
        String username = JWTUtil.getUsername(token);
        if (username == null) {
            throw new AuthenticationException("token invalid");
        }
        User user = cacheUsers.get(username);
        logger.info("myRealm'hashcode[{}]", this.hashCode());
        if (user == null) {
            throw new AuthenticationException("User didn't existed!");
        }
        logger.debug("current user[userId({}),userName({})]'s hashcode[{}]", user.getId(),user.getName(),user.hashCode());
        if (! JWTUtil.verify(token,user.getPassword())) {
            throw new AuthenticationException("Username or password error");
        }

        return new SimpleAuthenticationInfo(token, token, "my_realm");
    }
}
