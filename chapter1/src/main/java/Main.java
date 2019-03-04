import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author qzz
 */
public class Main {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("application.xml");
    }
}
