package wp.ddns.iputil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class IPCN extends AbstractIP {

    private final static Logger LOGGER = LoggerFactory.getLogger(IPCN.class);

    private final static String IPURL = "https://www.ip.cn/";

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

        Elements bodys = doc.getElementsByTag("body");
        Elements scripts = bodys.first().getElementsByTag("script");
        String textWrapIP = scripts.first().html();
        Pattern pattern = Pattern.compile(".*>([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})<.*");
        Matcher matcher = pattern.matcher(textWrapIP);
        if (matcher.find()) {
            // group(0)-->the raw string,group(1)-->the match string in the first brackets
            String ip = matcher.group(1);
            LOGGER.info("current ip[{}]",ip);
            return ip;
        } else {
            throw new Exception("get ip with regex from textWrapIP fail");
        }
    }
}
