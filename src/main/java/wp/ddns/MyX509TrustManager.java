package wp.ddns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class MyX509TrustManager implements X509TrustManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(MyX509TrustManager.class);

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        // TODO Auto-generated method stub
        LOGGER.debug("step into");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        // TODO Auto-generated method stub
        LOGGER.debug("step into");
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        LOGGER.debug("step into");
        // TODO Auto-generated method stub
        return null;
    }

}
