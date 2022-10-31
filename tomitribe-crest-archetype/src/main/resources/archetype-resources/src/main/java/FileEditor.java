package $groupId;

import org.tomitribe.crest.api.Editor;
import org.tomitribe.util.editor.AbstractConverter;

import java.io.File;

@Editor(File.class)
public class FileEditor extends AbstractConverter {
    @Override
    protected Object toObjectImpl(final String s) {
        return new File(s);
    }
}
