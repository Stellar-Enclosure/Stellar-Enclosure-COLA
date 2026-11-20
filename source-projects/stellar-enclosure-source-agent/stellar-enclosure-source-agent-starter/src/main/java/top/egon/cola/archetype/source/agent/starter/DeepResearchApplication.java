package top.egon.cola.archetype.source.agent.starter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/** Boot entry point for the generated Deep Research Agent application. */
@SpringBootApplication(scanBasePackages = "top.egon.cola.archetype.source.agent")
// The data access interfaces live in Infrastructure, outside the base package the automatic
// scanning of the starter covers, so each repository package is registered here.
@MapperScan(basePackages = "top.egon.cola.archetype.source.agent.infrastructure.knowledge.dao")
public class DeepResearchApplication {

    /** Retains the SDK qualifier while reusing the Boot-managed Jackson 2 mapper. */
    @Bean(name = "nativeJacksonObjectMapperAlias")
    public static BeanFactoryPostProcessor nativeJacksonObjectMapperAlias() {
        return beanFactory -> {
            if (beanFactory.containsBean("jackson2ObjectMapper") && !beanFactory.containsBean("jacksonObjectMapper")) {
                beanFactory.registerAlias("jackson2ObjectMapper", "jacksonObjectMapper");
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(DeepResearchApplication.class, args);
    }
}
