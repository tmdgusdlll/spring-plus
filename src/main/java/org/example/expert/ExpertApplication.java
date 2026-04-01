package org.example.expert;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.expert.config.DotenvInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class ExpertApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        boolean osEnvSet = System.getenv("SPRING_PROFILES_ACTIVE") != null;
        boolean jvmArgSet = System.getProperty("spring.profiles.active") != null;

        if (!osEnvSet && !jvmArgSet) {
            String profile = dotenv.get("SPRING_PROFILES_ACTIVE", "local");
            System.setProperty("spring.profiles.active", profile);
        }
        SpringApplication app = new SpringApplication(ExpertApplication.class);
        app.addInitializers(new DotenvInitializer());
        app.run(args);
    }
}
