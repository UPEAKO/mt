package wp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wp.bean.ResponseBean;
import wp.ddns.Detection;
import wp.wrap.DetectionWrap;

@Service
public class DDNSService {

    private final static Logger logger = LoggerFactory.getLogger(DDNSService.class);

    private Detection detection;

    @Autowired
    public DDNSService(Detection detection) {
        this.detection = detection;
        logger.warn("detection hashcode[{}]", this.detection.hashCode());
    }

    public ResponseBean getDDNSStatus() {
        logger.debug("step into");
        DetectionWrap detectionWrap = new DetectionWrap();

        detectionWrap.setIpUrl(detection.getIpUrl());
        detectionWrap.setOldIP(detection.getOldIP());
        detectionWrap.setDelayTime(detection.getDelayTime());
        detectionWrap.setErrorTimes(Detection.getErrorTimes());
        detectionWrap.setHasInitialIp(detection.isHasInitialIp());
        detectionWrap.setIpUrlOverTLS(detection.isIpUrlOverTLS());
        detectionWrap.setInitDelayTime(detection.getInitDelayTime());
        detectionWrap.setNumOfDelayTime(detection.getNumOfDelayTime());
        detectionWrap.setSucceedMissionTimes(Detection.getSucceedMissionTimes());

        return new ResponseBean(200, "ddns status",detectionWrap);
    }
}
