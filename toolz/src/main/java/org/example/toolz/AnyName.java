/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.example.toolz;

import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;

import java.io.File;
import java.net.URI;
import java.util.regex.Pattern;

public class AnyName {

    @Command
    public void rsync(@Option("recursive") boolean recursive,
                      @Option("links") boolean links,
                      @Option("perms") boolean perms,
                      @Option("owner") boolean owner,
                      @Option("group") boolean group,
                      @Option("devices") boolean devices,
                      @Option("specials") boolean specials,
                      @Option("times") boolean times,
                      @Option("exclude") Pattern exclude,
                      @Option("exclude-from") File excludeFrom,
                      @Option("include") Pattern include,
                      @Option("include-from") File includeFrom,
                      @Option("progress") @Default("true") boolean progress,
                      URI[] sources,
                      URI dest) {

        // TODO write the implementation...
    }
}
