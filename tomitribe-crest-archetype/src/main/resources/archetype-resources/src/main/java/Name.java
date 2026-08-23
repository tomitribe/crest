package $package;

/**
 * The name to greet, e.g. 'World'.  Typed positional arguments name
 * themselves in the usage line - 'greet [options] Name' rather than
 * 'greet [options] String' - and the single-String constructor is the
 * natural home for validation.
 */
public class Name {

    private final String value;

    public Name(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Name may not be blank");
        }
        this.value = value;
    }

    public String get() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
