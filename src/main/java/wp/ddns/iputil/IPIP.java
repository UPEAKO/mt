package wp.ddns.iputil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class IPIP extends AbstractIP{

    private final static Logger LOGGER = LoggerFactory.getLogger(IPIP.class);

    private final static String IPURL = "https://www.ipip.net/ip.html";

    @Override
    String getCurrentIP() throws Exception {
        LOGGER.debug("step into");
        Document doc;
        try {
            doc = Jsoup.connect(IPURL).timeout(10000).get();
        } catch (IOException e) {
            LOGGER.error("internet may error,get html wrap current ip fail!!!");
            throw e;
        }

        try {
            Elements inputs = doc.getElementsByAttributeValue("name", "ip");
            String ip = inputs.first().attr("value");
            Pattern pattern = Pattern.compile("^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$");
            Matcher matcher = pattern.matcher(ip);
            if (!matcher.matches()) {
                throw new Exception("wrong ip schema");
            }
            LOGGER.info("current ip[{}]",ip);
            return ip;
        } catch (Exception e) {
            LOGGER.error("something wrong when parse html for ip");
            throw e;
        }
    }
}
