package wp.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Date;

@Component
public class JWTUtil {

    private final static Logger logger = LoggerFactory.getLogger(JWTUtil.class);

    // 过期时间6小时
    private static long EXPIRE_TIME = 6*60*60*1000;

    @Value("${wp.util.expireTime}")
    public void setExpireTime(long expireTime) {
        logger.debug("default expire time[{}]", EXPIRE_TIME);
        EXPIRE_TIME = expireTime;
        logger.warn("expire time from configure file[{}] was set", EXPIRE_TIME );
    }

    public static boolean verify(String token, String secret) {
        logger.debug("step into");
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (Exception exception) {
            logger.warn("verify token[{}] fail",token);
            return false;
        }
    }

    public static String getUsername(String token) {
        logger.debug("step into");
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("username").asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    public static int getUserId(String token) {
        logger.debug("step into");
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("userId").asInt();
        } catch (JWTDecodeException e) {
            logger.error("getUserId from token[{}] fail",token);
            throw e;
        }
    }

    public static String sign(String username, Integer userId ,String secret) {
        logger.debug("step into");
        try {
            Date date = new Date(System.currentTimeMillis()+EXPIRE_TIME);
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withClaim("username", username)
                    .withClaim("userId", userId)
                    .withExpiresAt(date)
                    .sign(algorithm);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
