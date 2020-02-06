package wp.ddns.iputil;

abstract class AbstractIP {

    private int succeedTime = 100;

    private int failTime = 100;

    private String IP;

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    void addSucceedTime() {
        succeedTime++;
        if (succeedTime == Integer.MAX_VALUE) {
            succeedTime = 100;
            failTime = 100;
        }
    }

    int getSucceedTime() {
        return succeedTime;
    }

    void addFailTime() {
        failTime++;
        if (failTime == Integer.MAX_VALUE) {
            succeedTime = 100;
            failTime = 100;
        }
    }

    int getFailTime() {
        return failTime;
    }

    abstract String getCurrentIP() throws Exception;
}
