package wp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wp.bean.ResponseBean;
import wp.database.User;
import wp.database.UserRepository;
import wp.exception.UnauthorizedException;
import wp.util.JWTUtil;

@Service
public class UserService {

    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResponseBean authenticateUser(String userName, String passWord) {
        logger.info("{} {}",userName,passWord);
        User user = userRepository.findUserByName(userName);
        if (user == null) {
            logger.info("user is null");
            throw new UnauthorizedException();
        }
        logger.info("{}   {}",user.getName(),user.getPassword());
        if (passWord.equals(user.getPassword())) {
            return new ResponseBean(200, "login success", JWTUtil.sign(userName, passWord));
        } else {
            throw new UnauthorizedException();
        }
    }
}
