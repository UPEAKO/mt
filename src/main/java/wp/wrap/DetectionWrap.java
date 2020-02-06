package wp.wrap;

public class DetectionWrap {

    private String oldIP;

    private boolean hasInitialIp;

    private String ipUrl;

    private boolean isIpUrlOverTLS;

    private int initDelayTime;

    private int delayTime;

    private int numOfDelayTime;

    private int succeedMissionTimes;

    private int errorTimes;

    public DetectionWrap(){}

    public DetectionWrap(String oldIP, boolean hasInitialIp, String ipUrl, boolean isIpUrlOverTLS, int initDelayTime, int delayTime, int numOfDelayTime, int succeedMissionTimes, int errorTimes) {
        this.oldIP = oldIP;
        this.hasInitialIp = hasInitialIp;
        this.ipUrl = ipUrl;
        this.isIpUrlOverTLS = isIpUrlOverTLS;
        this.initDelayTime = initDelayTime;
        this.delayTime = delayTime;
        this.numOfDelayTime = numOfDelayTime;
        this.succeedMissionTimes = succeedMissionTimes;
        this.errorTimes = errorTimes;
    }

    public String getOldIP() {
        return oldIP;
    }

    public void setOldIP(String oldIP) {
        this.oldIP = oldIP;
    }

    public boolean isHasInitialIp() {
        return hasInitialIp;
    }

    public void setHasInitialIp(boolean hasInitialIp) {
        this.hasInitialIp = hasInitialIp;
    }

    public String getIpUrl() {
        return ipUrl;
    }

    public void setIpUrl(String ipUrl) {
        this.ipUrl = ipUrl;
    }

    public boolean isIpUrlOverTLS() {
        return isIpUrlOverTLS;
    }

    public void setIpUrlOverTLS(boolean ipUrlOverTLS) {
        isIpUrlOverTLS = ipUrlOverTLS;
    }

    public int getInitDelayTime() {
        return initDelayTime;
    }

    public void setInitDelayTime(int initDelayTime) {
        this.initDelayTime = initDelayTime;
    }

    public int getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    public int getNumOfDelayTime() {
        return numOfDelayTime;
    }

    public void setNumOfDelayTime(int numOfDelayTime) {
        this.numOfDelayTime = numOfDelayTime;
    }

    public int getSucceedMissionTimes() {
        return succeedMissionTimes;
    }

    public void setSucceedMissionTimes(int succeedMissionTimes) {
        this.succeedMissionTimes = succeedMissionTimes;
    }

    public int getErrorTimes() {
        return errorTimes;
    }

    public void setErrorTimes(int errorTimes) {
        this.errorTimes = errorTimes;
    }
}
