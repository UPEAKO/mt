package wp.ddns.iputil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class IP138 extends AbstractIP {

    private final static Logger LOGGER = LoggerFactory.getLogger(IP138.class);

    private boolean RENEWIPURL = true;

    private final static String BASEURL = "http://ip138.com";

    private String IPURL;

    @Override
    String getCurrentIP() throws Exception {
        LOGGER.debug("step into");
        LOGGER.debug("current value renewIPUrl[{}]", RENEWIPURL);
        if (RENEWIPURL) {
            getCurrentIPUrl();
        }

        Document doc;
        try {
            doc = Jsoup.connect(IPURL).timeout(10000).get();
        } catch (IOException e) {
            LOGGER.error("internet may error,get html wrap current ip fail!!!");
            throw e;
        }

        String textWrapIP;
        try {
            Elements ps = doc.getElementsByTag("p");
            textWrapIP = ps.first().text();
            LOGGER.debug("current textWrapIP[{}]",textWrapIP);
        } catch (Exception e) {
            LOGGER.error("something wrong when parse html for ip");
            throw e;
        }

        Pattern pattern = Pattern.compile(".*\\[([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})].*");
        Matcher matcher = pattern.matcher(textWrapIP);
        if (matcher.find()) {
            // group(0)-->the raw string,group(1)-->the match string in the first brackets
            String ip = matcher.group(1);
            LOGGER.info("current ip[{}]",ip);
            return ip;
        } else {
            RENEWIPURL = true;
            LOGGER.error("not match this regex,current textWrap[{}]",textWrapIP);
            throw new Exception("get ip with regex from textWrapIP fail");
        }
    }

    private  void getCurrentIPUrl() throws Exception{
        LOGGER.debug("step into");
        Document baseDoc;
        try {
            baseDoc = Jsoup.connect(BASEURL).timeout(10000).get();
        } catch (IOException e) {
            LOGGER.error("internet may error,get baseUrl fail!!!");
            throw e;
        }

        try {
            Elements ipMods = baseDoc.getElementsByClass("module mod-ip");
            Elements iframes = ipMods.first().getElementsByTag("iframe");
            IPURL =  iframes.first().attr("src");
            RENEWIPURL = false;
        } catch (Exception e) {
            LOGGER.error("something wrong when parse html for ipUrl");
            throw e;
        }
    }
}
