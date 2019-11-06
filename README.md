### 配置
* 配置文件application.properties中配置log4j2.xml路径
* application.properties在jar同级目录、同级目录config子目录、classpath、classpath下config子目录自动应用
* 直接参数指定 java -jar xxx.jar --spring.config.location=D:\springconfig\ --logging.config=./log4j2.xml
### 调试
* spring find and store 注解 
```java
scanCandidateComponents:432, ClassPathScanningCandidateComponentProvider (org.springframework.context.annotation)
findCandidateComponents:316, ClassPathScanningCandidateComponentProvider (org.springframework.context.annotation)
doScan:275, ClassPathBeanDefinitionScanner (org.springframework.context.annotation)
parse:132, ComponentScanAnnotationParser (org.springframework.context.annotation)
doProcessConfigurationClass:287, ConfigurationClassParser (org.springframework.context.annotation)
processConfigurationClass:242, ConfigurationClassParser (org.springframework.context.annotation)
parse:199, ConfigurationClassParser (org.springframework.context.annotation)
parse:167, ConfigurationClassParser (org.springframework.context.annotation)
processConfigBeanDefinitions:315, ConfigurationClassPostProcessor (org.springframework.context.annotation)
postProcessBeanDefinitionRegistry:232, ConfigurationClassPostProcessor (org.springframework.context.annotation)
invokeBeanDefinitionRegistryPostProcessors:275, PostProcessorRegistrationDelegate (org.springframework.context.support)
invokeBeanFactoryPostProcessors:95, PostProcessorRegistrationDelegate (org.springframework.context.support)
invokeBeanFactoryPostProcessors:705, AbstractApplicationContext (org.springframework.context.support)
refresh:531, AbstractApplicationContext (org.springframework.context.support)
refresh:141, ServletWebServerApplicationContext (org.springframework.boot.web.servlet.context)
refresh:744, SpringApplication (org.springframework.boot)
refreshContext:391, SpringApplication (org.springframework.boot)
run:312, SpringApplication (org.springframework.boot)
run:1215, SpringApplication (org.springframework.boot)
run:1204, SpringApplication (org.springframework.boot)
main:13, WebApplication (wp)
```
