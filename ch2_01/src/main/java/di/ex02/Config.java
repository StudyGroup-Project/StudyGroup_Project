package di.ex01;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    @Bean
    SamsungTV tv() {return new SamsungTV();}

    @Bean
    LgTV lgtv() {return new LgTV();}
}
