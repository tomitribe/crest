package $groupId;


import org.apache.xbean.finder.archive.ClassesArchive;
import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.Xbean;

/**
 * Unit test for simple App.
 */
public class AppTest extends Assert {

    @Test
    public void testApp() throws Exception {
        final ClassesArchive entries = new ClassesArchive(App.class);
        final Main main = new Main(new Xbean(entries));

        assertEquals("Hello, World!", main.exec("hello"));
        assertEquals("Hello, Wisconsin!", main.exec("hello", "--name=Wisconsin"));
        assertEquals("Hola, Ecuador!", main.exec("hello", "--name=Ecuador", "--language=ES"));
    }
}
