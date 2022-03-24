package \$groupId;

import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.StreamingOutput;
import org.tomitribe.crest.api.table.Table;
import org.tomitribe.crest.val.Readable;
import org.tomitribe.util.Files;
import org.tomitribe.util.IO;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.Stream;

/**
 *
 */
public class App {

    /**
     * The cat utility reads a file and writes it  to the standard output.  The file operands are processed in command-line
     * order.  Files must be readable by the user and cannot be a directorie.  Unlike the UNIX or Linux version of `cat`
     * the use of a single dash (`-') to read the standard input is not supported, nor is the ability read UNIX domain sockets.
     *
     * In fact, this javadoc is here just to show you that javadoc can be used create nice help that looks like a man page.
     * The formatting is similar to Markdown.  To see how this looks when output, run this command
     *
     *     help cat
     *
     * @param file the path of a file on the local system
     * @return
     */
    @Command
    public StreamingOutput cat(@IsFile @Readable final File file) {
        Files.exists(file);
        Files.readable(file);
        Files.file(file);

        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException {
                IO.copy(file, os);
            }
        };
    }

    /**
     * List files in the specified directory
     * @param directory the path to a directory
     */
    @Command
    @Table(fields = "parent name absolutePath", sort = "name parent")
    public Stream<File> ls(final File directory){
        return Stream.of(directory.listFiles())
                .filter(File::isFile);
    }

    /**
     * Prints hello to the specified name and in one of the supported languages.
     *
     * @param name The name of the person, place or thing
     * @param language Indicates what language in which to say hello
     */
    @Command
    public String hello(@Option("name") @Default("World") String name,
                        @Option("language") @Default("EN") Language language) {
        return String.format("%s, %s!", language.getSalutation(), name);
    }

    public static enum Language {
        EN("Hello"),
        ES("Hola"),
        FR("Bonjour");

        private final String salutation;

        private Language(String salutation) {
            this.salutation = salutation;
        }

        public String getSalutation() {
            return salutation;
        }
    }

}
