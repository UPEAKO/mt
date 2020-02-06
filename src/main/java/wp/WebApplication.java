package wp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import wp.ddns.Detection;

@SpringBootApplication
public class WebApplication {

    private final static Logger LOGGER = LoggerFactory.getLogger(WebApplication.class);

    @Value("${wp.enableDDNS}")
    private boolean enableDDNS;

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

    @Bean
    CommandLineRunner init(Detection detection) {
        return (args) -> {
            if (enableDDNS) {
                detection.detectionIpChange();
                LOGGER.warn("start ddns change detection!!!");
            }
        };
    }
}
