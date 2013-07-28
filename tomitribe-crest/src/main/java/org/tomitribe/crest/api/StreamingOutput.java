/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.tomitribe.crest.api;

import java.io.IOException;
import java.io.OutputStream;

public interface StreamingOutput {
    public void write(OutputStream os) throws IOException;
}
