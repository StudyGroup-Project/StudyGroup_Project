package di.ex01;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TVUser {
    public static void main(String[] args){
        ApplicationContext factory = new AnnotationConfigApplicationContext(Config.class);
        TV tv = (TV)factory.getBean("lgtv");

        tv.powerOn();
        tv.volumeUp();
        tv.volumeDown();
        tv.powerOff();
    }
}
