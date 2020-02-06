package wp.ddns.iputil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class IP {

    private final static Logger LOGGER = LoggerFactory.getLogger(IP.class);

    private static ArrayList<AbstractIP> list = new ArrayList<>(3);

    static {
        list.add(new IP138());
        list.add(new IPIP());
        list.add(new IPCN());
    }

    public static String getCurrentIP() throws Exception{
        int succeedTime = 0;
        HashMap<String,Integer> judge = new HashMap<>(1);
        for (AbstractIP abstractIP : list) {
            try {
                String ip = abstractIP.getCurrentIP();
                abstractIP.setIP(ip);
                abstractIP.addSucceedTime();
                if (judge.get(ip) == null) {
                    judge.put(ip,1);
                } else {
                    int size = judge.get(ip);
                    judge.put(ip,++size);
                }
                succeedTime++;
            } catch (Exception e) {
                abstractIP.addFailTime();
                // 错误超过阈值，删除，但必须保留一个
                if ((double)abstractIP.getFailTime() / (double)abstractIP.getSucceedTime() > 1.1 && list.size() > 1) {
                    list.remove(abstractIP);
                }
                LOGGER.error("get current ip from one server fail",e);
            }
        }

        if (succeedTime == 0 || judge.isEmpty()) {
            throw new Exception("get current ip from all server fail");
        }

        int maxIPNum = 0;
        String result = "";
        // 选取ip众数
        for (HashMap.Entry<String,Integer> entry : judge.entrySet()) {
            if (entry.getValue() > maxIPNum) {
                maxIPNum = entry.getValue();
                result = entry.getKey();
            }
        }
        Pattern pattern = Pattern.compile("^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$");
        Matcher matcher = pattern.matcher(result);
        if (!matcher.matches()) {
            throw new Exception("the final result of current ip's schema wrong!!!");
        }
        // 非众数ip错误值加1;
        for (AbstractIP abstractIP : list) {
            if (!abstractIP.getIP().equals(result)) {
                abstractIP.addFailTime();
                LOGGER.error("ip from all server not the same, different class Name[{}]",abstractIP.getClass());
            }
        }
        LOGGER.info("the final ip from open server[{}]",result);
        return result;
    }
}
