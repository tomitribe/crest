package $package;

import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;

/**
 * The commands of this CLI.  Every method annotated with Command becomes a
 * subcommand of the executable, discovered at build time by the
 * crest-maven-plugin descriptor goal.  There is no main method anywhere in
 * this project; org.tomitribe.crest.Main is the entry point.
 */
public class Cli {

    /**
     * Greet someone or something by name.
     *
     * Prints a one-line greeting composed from the language option and the
     * name given on the command line.  This javadoc is the command's
     * documentation: the first sentence appears in the command listing and
     * the whole comment renders as a man page via 'help greet'.  Keep the
     * text plain - inline javadoc tags such as code or link render literally.
     *
     * @param language the language to greet in: EN, ES or FR
     * @param name the person, place or thing to greet, e.g. 'World'
     */
    @Command
    public String greet(@Option("language") @Default("EN") final Language language, final Name name) {
        return String.format("%s, %s!", language.getSalutation(), name);
    }

    public enum Language {
        EN("Hello"),
        ES("Hola"),
        FR("Bonjour");

        private final String salutation;

        Language(final String salutation) {
            this.salutation = salutation;
        }

        public String getSalutation() {
            return salutation;
        }
    }

}
