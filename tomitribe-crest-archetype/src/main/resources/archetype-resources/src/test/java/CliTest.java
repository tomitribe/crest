package $package;

import org.junit.jupiter.api.Test;
import org.tomitribe.crest.Main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Commands are plain methods, so they test without any process launching:
 * Main.exec runs the same path the executable runs.
 */
public class CliTest {

    @Test
    public void greet() throws Exception {
        final Main main = new Main(Cli.class);

        assertEquals("Hello, World!", main.exec("greet", "World"));
        assertEquals("Hola, Ecuador!", main.exec("greet", "--language=ES", "Ecuador"));
    }

    @Test
    public void nameMayNotBeBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Name(" "));
    }
}
