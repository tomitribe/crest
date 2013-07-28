/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.tomitribe.crest.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @version $Revision$ $Date$
 */
public final class Pipe implements Runnable {

    private final InputStream in;
    private final OutputStream out;

    public Pipe(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    public static void pipe(Process process) {
        pipe(process.getInputStream(), System.out);
        pipe(process.getErrorStream(), System.err);
        //        pipe(System.in, process.getOutputStream());
    }

    public static Future<Pipe> pipe(InputStream in, OutputStream out) {
        final Pipe target = new Pipe(in, out);

        final FutureTask<Pipe> task = new FutureTask<Pipe>(target, target);
        final Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        return task;
    }

    public void run() {
        try {
            int i = -1;

            byte[] buf = new byte[1024];

            while ((i = in.read(buf)) != -1) {
                out.write(buf, 0, i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
