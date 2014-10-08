package $groupId;


import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.crest.Main;

/**
 * Unit test for simple App.
 */
public class AppTest extends Assert {

    @Test
    public void testApp() throws Exception {
        final Main main = new Main(App.class);

        assertEquals("Hello, World!", main.exec("hello"));
        assertEquals("Hello, Wisconsin!", main.exec("hello", "--name=Wisconsin"));
        assertEquals("Hola, Ecuador!", main.exec("hello", "--name=Ecuador", "--language=ES"));
    }
}
