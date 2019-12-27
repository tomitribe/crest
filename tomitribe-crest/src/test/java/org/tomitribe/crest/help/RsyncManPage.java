/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.crest.help;

public class RsyncManPage {
    public static Document getDocument() {
        return Document.builder()
                .heading("name")
                .paragraph("rsync - faster, flexible replacement for rcp")
                .heading("synopsis")
                .paragraph("rsync [OPTION]... SRC [SRC]... DEST")
                .paragraph("rsync [OPTION]... SRC [SRC]... [USER@]HOST:DEST")
                .paragraph("rsync [OPTION]... SRC [SRC]... [USER@]HOST::DEST")
                .paragraph("rsync [OPTION]... SRC [SRC]... rsync://[USER@]HOST[:PORT]/DEST")
                .paragraph("rsync [OPTION]... SRC")
                .paragraph("rsync [OPTION]... [USER@]HOST:SRC [DEST]")
                .paragraph("rsync [OPTION]... [USER@]HOST::SRC [DEST]")
                .paragraph("rsync [OPTION]... rsync://[USER@]HOST[:PORT]/SRC [DEST]")
                .heading("DeScRiPTION")
                .paragraph("rsync is a program that behaves in much the same way that rcp does, but has m" +
                        "any more options and uses the rsync remote-update protocol to greatly speed up f" +
                        "ile transfers when the destination file is being updated.")
                .paragraph("The rsync remote-update protocol allows rsync to transfer just the difference" +
                        "s between two sets of files across the network connection, using an efficient ch" +
                        "ecksum-search algorithm described in the technical report that accompanies this " +
                        "package.")
                .paragraph("Some of the additional features of rsync are:")
                .bullet("support for copying links, devices, owners, groups, and permissions")
                .bullet("exclude and exclude-from options similar to GNU tar")
                .bullet("a CVS exclude mode for ignoring the same files that CVS would ignore")
                .bullet("can use any transparent remote shell, including ssh or rsh")
                .bullet("does not require super-user privileges")
                .bullet("pipelining of file transfers to minimize latency costs")
                .bullet("support for anonymous or authenticated rsync daemons (ideal for mirroring)")
                .heading("general")
                .paragraph("Rsync copies files either to or from a remote host, or locally on the current" +
                        " host (it does not support copying files between two remote hosts).")
                .paragraph("There are two different ways for rsync to contact a remote system: using a re" +
                        "mote-shell program as the transport (such as ssh or rsh) or contacting an rsync " +
                        "daemon directly via TCP. The remote-shell transport is used whenever the source " +
                        "or destination path contains a single colon (:) separator after a host specifica" +
                        "tion. Contacting an rsync daemon directly happens when the source or destination" +
                        " path contains a double colon (::) separator after a host specification, OR when" +
                        " an rsync:// URL is specified (see also the \"USING RSYNC-DAEMON FEATURES VIA A " +
                        "REMOTE-SHELL CONNECTION\" section for an exception to this latter rule).")
                .paragraph("As a special case, if a single source arg is specified without a destination," +
                        " the files are listed in an output format similar to \"ls -l\".")
                .paragraph("As expected, if neither the source or destination path specify a remote host," +
                        " the copy occurs locally (see also the --list-only option).")
                .heading("setup")
                .paragraph("See the file README for installation instructions.")
                .paragraph("Once installed, you can use rsync to any machine that you can access via a re" +
                        "mote shell (as well as some that you can access using the rsync daemon-mode prot" +
                        "ocol). For remote transfers, a modern rsync uses ssh for its communications, but" +
                        " it may have been configured to use a different remote shell by default, such as" +
                        " rsh or remsh.")
                .paragraph("You can also specify any remote shell you like, either by using the -e comman" +
                        "d line option, or by setting the RSYNC_RSH environment variable.")
                .paragraph("Note that rsync must be installed on both the source and destination machines.")
                .heading("usage")
                .paragraph("You use rsync in the same way you use rcp. You must specify a source and a de" +
                        "stination, one of which may be remote.")
                .paragraph("Perhaps the best way to explain the syntax is with some examples:")
                .indentedContent("       rsync -t *.c foo:src/")
                .paragraph("This would transfer all files matching the pattern *.c from the current direc" +
                        "tory to the directory src on the machine foo. If any of the files already exist " +
                        "on the remote system then the rsync remote-update protocol is used to update the" +
                        " file by sending only the differences. See the tech report for details.")
                .indentedContent("       rsync -avz foo:src/bar /data/tmp")
                .paragraph("This would recursively transfer all files from the directory src/bar on the m" +
                        "achine foo into the /data/tmp/bar directory on the local machine. The files are " +
                        "transferred in \"archive\" mode, which ensures that symbolic links, devices, att" +
                        "ributes, permissions, ownerships, etc. are preserved in the transfer. Additional" +
                        "ly, compression will be used to reduce the size of data portions of the transfer.")
                .paragraph("       rsync -avz foo:src/bar/ /data/tmp")
                .paragraph("A trailing slash on the source changes this behavior to avoid creating an add" +
                        "itional directory level at the destination. You can think of a trailing / on a s" +
                        "ource as meaning \"copy the contents of this directory\" as opposed to \"copy th" +
                        "e directory by name\", but in both cases the attributes of the containing direct" +
                        "ory are transferred to the containing directory on the destination. In other wor" +
                        "ds, each of the following commands copies the files in the same way, including t" +
                        "heir setting of the attributes of /dest/foo:")
                .indentedContent("       rsync -av /src/foo /dest\n       rsync -av /src/foo/ /dest/foo")
                .paragraph("Note also that host and module references don't require a trailing slash to c" +
                        "opy the contents of the default directory.")
                .build();
    }
}
