package wp.ddns;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// TODO  似乎未正常执行，添加log调试原因

@Service
public class Detection {

    private final static Logger LOGGER = LoggerFactory.getLogger(Detection.class);

    @Value("${wp.ddns.updateBaseUrl}")
    private String updateBaseUrl;

    @Value("${wp.ddns.baseListUrl}")
    private String baseListUrl;

    // 从域名服务商获取的ip
    private String oldIP = "127.0.0.1";

    // 是否已经初始化oldIP
    private boolean hasInitialIp = false;

    // 自定义ip服务器url
    @Value("${wp.ddns.ipUrl}")
    private String ipUrl;

    // 是否ip服务器使用TLS加密,区别请求方法
    @Value("${wp.ddns.isIpUrlOverTLS}")
    private boolean isIpUrlOverTLS;

    @Value("${wp.ddns.initDelayTime}")
    private int initDelayTime;

    @Value("${wp.ddns.delayTime}")
    private int delayTime;

    @Value("${wp.ddns.numOfDelayTime}")
    private int numOfDelayTime;

    private static int succeedMissionTimes = 0;

    private static int errorTimes = 0;

    // ip改变，更新域名服务商处ip解析
    private boolean update(String newIP) throws Exception {
        LOGGER.debug("step into");
        String result = RequestUtil.httpsRequest(baseListUrl,"GET",null);
        if (result.isEmpty()) {
            throw new Exception("get nothing from " + baseListUrl);
        }
        LOGGER.warn("raw xml source from namesilo: [{}]", result);
        Document document = DocumentHelper.parseText(result);
        Node NCode = document.selectSingleNode("/namesilo/reply/code");
        String code = NCode.getText();
        if (!code.equals("300")) {
            throw new Exception("the code in xml resource from namesilo is not 300");
        }

        List<Node> recordIds = document.selectNodes("/namesilo/reply/resource_record/record_id");
        String recordId1 = recordIds.get(0).getText();
        String recordId2 = recordIds.get(1).getText();
        LOGGER.info("recordId1[{}],recordId2[{}]",recordId1,recordId2);

        String updateUrl1 = updateBaseUrl + "rrhost=www&rrid=" + recordId1 + "&rrvalue=" + newIP;
        String result1 = RequestUtil.httpsRequest(updateUrl1,"GET",null);
        if (result1.isEmpty())  {
            throw new Exception("get empty xml resource from namesilo");
        }
        LOGGER.warn("raw xml source from namesilo: [{}]", result1);
        document = DocumentHelper.parseText(result1);
        NCode = document.selectSingleNode("/namesilo/reply/code");
        code = NCode.getText();
        if (!code.equals("300"))  {
            throw new Exception("the code in xml resource from namesilo is not 300");
        }

        String updateUrl2 = updateBaseUrl + "rrid=" + recordId2 + "&rrvalue=" + newIP;
        String result2 = RequestUtil.httpsRequest(updateUrl2,"GET",null);
        if (result2.isEmpty())  {
            throw new Exception("get empty xml resource from namesilo");
        }
        LOGGER.warn("raw xml source from namesilo: [{}]", result2);
        document = DocumentHelper.parseText(result2);
        NCode = document.selectSingleNode("/namesilo/reply/code");
        code = NCode.getText();
        if (!code.equals("300"))  {
            throw new Exception("the code in xml resource from namesilo is not 300");
        }

        return true;
    }

    private String getCurrentIP() throws Exception {
        LOGGER.debug("step into");
        // 是否已经成功获取oldIP
        if (!hasInitialIp) {
            String resultOldIP = RequestUtil.httpsRequest(baseListUrl,"GET",null);
            if (resultOldIP.isEmpty()) {
                throw new Exception("get nothing from " + baseListUrl);
            }
            else {
                // 输出getCurrentIP原始数据
                LOGGER.warn("raw xml source from namesilo: [{}]", resultOldIP);
                Document document = DocumentHelper.parseText(resultOldIP);
                LOGGER.debug("start parse xml resource at getCurrentIP() function");
                Node NCode = document.selectSingleNode("/namesilo/reply/code");
                LOGGER.debug("selectSingleNode at getCurrentIP function");
                String code = NCode.getText();
                LOGGER.debug("current code at xml[{}]", code);
                if (code.equals("300")) {
                    List<Node> NIPs = document.selectNodes("/namesilo/reply/resource_record/value");
                    oldIP = NIPs.get(0).getText();
                    LOGGER.warn("current host's IP from namesilo:[{}]",oldIP);
                    String anotherIP = NIPs.get(1).getText();
                    LOGGER.warn("current sub host's IP from namesilo[{}]",anotherIP);
                    if (!oldIP.equals(anotherIP)) {
                        throw new Exception("main host ip not equals with sub host ip");
                    }
                    hasInitialIp = true;
                } else {
                    throw new Exception("the code in xml resource from is not 300");
                }
            }
        }
        // 从自定义服务器获取currentIP
        String result;
        if (!isIpUrlOverTLS) {
            LOGGER.debug("get IP from [{}] not over TLS", ipUrl);
            result = RequestUtil.httpRequest(ipUrl,"GET",null);
            // FIXME 长度判别改为正则表达式判别IP
            if (result.isEmpty() || result.length() < 7) {
                throw new Exception("get wrong ip from ip server not over TLS");
            }
            return result;
        } else {
            LOGGER.debug("get IP from [{}] not TLS", ipUrl);
            result = RequestUtil.httpsRequest(ipUrl,"GET",null);
            if (result.isEmpty() || result.length() < 7) {
                throw new Exception("get wrong ip from ip server over TLS");
            }
            return result;
        }
    }

    public void detectionIpChange() {
        LOGGER.debug("step into");
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);
        scheduledExecutorService.scheduleWithFixedDelay(() ->{
            // 捕获一切异常，出错该轮任务抛弃，等待下一轮任务
            try {
                String currentIP = getCurrentIP();
                if (!currentIP.equals(oldIP)) {
                    if (update(currentIP)) {
                        oldIP = currentIP;
                        LOGGER.warn("oldIP change to:[{}]",oldIP);
                    } else {
                        throw new Exception("update IP fail");
                    }
                }
                if (succeedMissionTimes == Integer.MAX_VALUE) {
                    succeedMissionTimes = 0;
                }
                succeedMissionTimes++;
                if (succeedMissionTimes % numOfDelayTime == 0) {
                    LOGGER.warn("the number[{}] successful schedule mission,current IP[{}],wait next mission", succeedMissionTimes, currentIP);
                }
                LOGGER.info("the number[{}] successful schedule mission,current IP[{}],wait next mission", succeedMissionTimes, currentIP);
            } catch (Exception e) {
                if (errorTimes == Integer.MAX_VALUE) {
                    errorTimes = 0;
                }
                errorTimes++;
                LOGGER.error("this schedule mission fail,wait next mission!!!Exception message[{}]",e.getMessage());
                LOGGER.error("stackTrace", e);
            }
        },initDelayTime,delayTime, TimeUnit.MINUTES);
        LOGGER.warn("this detection hashcode[{}]",this.hashCode());
        LOGGER.warn("baseListUlr[{}]",baseListUrl);
        LOGGER.warn("updateBaseUrl[{}]", updateBaseUrl);
        LOGGER.warn("detection IP change start!!!");
        LOGGER.warn("{} is over TLS[{}], initDealy[{}], delay[{}]-->TimeUnit.MINUTES", ipUrl, isIpUrlOverTLS, initDelayTime, delayTime);
    }

    public String getOldIP() {
        return oldIP;
    }

    public boolean isHasInitialIp() {
        return hasInitialIp;
    }

    public String getIpUrl() {
        return ipUrl;
    }

    public boolean isIpUrlOverTLS() {
        return isIpUrlOverTLS;
    }

    public int getInitDelayTime() {
        return initDelayTime;
    }

    public int getDelayTime() {
        return delayTime;
    }

    public int getNumOfDelayTime() {
        return numOfDelayTime;
    }

    public static int getSucceedMissionTimes() {
        return succeedMissionTimes;
    }

    public static int getErrorTimes() {
        return errorTimes;
    }
}
