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

import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;

import java.io.File;
import java.net.URI;
import java.util.regex.Pattern;

public class JavadocHelpTest {


    public static class Rsync {

        /**
         *
         * @param recursive This tells rsync to copy directories recursively.  See also --dirs (-d).
         * @param links When symlinks are encountered, recreate the symlink on the destination.
         * @param perms  This option causes the receiving rsync to set the destination permissions
         *               to be the same as the source permissions. (See also the --chmod option for
         *               a way to modify what rsync considers to be the source permissions.)
         * @param owner  This option causes rsync to set the owner of the destination file to be the
         *               same as the source file, but only if the receiving rsync is being run as the
         *               super-user (see also the --super option to force rsync to attempt super-user
         *               activities). Without this option, the owner is set to the invoking user on
         *               the receiving side.
         *
         *               The preservation of ownership will associate matching names by default, but
         *               may fall back to using the ID number in some circumstances (see also the
         *               --numeric-ids option for a full discussion).
         * @param group This option causes rsync to set the group of the destination file to be the
         *             same as the source file. If the receiving program is not running as the super-user
         *             (or if --no-super was specified), only groups that the invoking user on the receiving
         *              side is a member of will be preserved. Without this option, the group is set to the
         *              default group of the invok- ing user on the receiving side.
         *
         *              The preservation of group information will associate matching names by default, but
         *              may fall back to using the ID number in some circumstances (see also the --numeric-ids
         *              option for a full discussion).
         * @param devices This option causes rsync to transfer
         *                character and block device files to the remote system to recreate
         *                these devices. This option has no effect if the receiving rsync is
         *                not run as the super-user and --super is not specified.
         * @param specials This option causes rsync to transfer special files such as named sockets and fifos.
         * @param times This tells rsync to transfer modification times along with the files
         *             and update them on the remote system. Note that if this option is not used,
         *              the optimization that excludes files that have not been modified cannot be effective; in other
         *              words, a missing -t or -a will cause the next transfer to behave as if it
         *              used -I, causing all files to be updated (though the rsync algorithm will make the
         *              update fairly efficient if the files haven't actually changed, you're much better
         *              off using -t).
         * @param exclude This option is a simplified form of the --filter option that defaults to an
         *                exclude rule and does not allow the full rule- parsing syntax of normal filter rules.
         *
         *                See the FILTER RULES section for detailed information on this option.
         *                
         * @param excludeFrom
         * 
         * This option is related to the --exclude option, but it specifies a FILE that contains
         * exclude patterns (one per line). Blank lines in the file and lines starting with ';' or
         * '#' are ignored. If FILE is -, the list will be read from standard input.
         * 
         * @param include  This option is a simplified form of the --filter option that defaults to an include
         *                 rule and does not allow the full rule-parsing syntax of normal filter rules.
         *
         *               See the FILTER RULES section for detailed information on this option.
         * @param includeFrom This option is related to the --include option, but it specifies a FILE t
         *                    hat contains include patterns (one per line). Blank lines in the file
         *                    and lines starting with ';' or '#' are ignored. If FILE is -, the list will be
         *                    read from standard input.
         * @param progress
         * This  option  tells  rsync to print information showing the progress of the transfer. This gives a bored user something to
         * watch.  Implies --verbose if it wasn't already specified.
         *
         * While rsync is transferring a regular file, it updates a progress line that looks like this:
         *
         *       782448  63%  110.64kB/s    0:00:04
         *
         * In this example, the receiver has reconstructed 782448 bytes or 63% of the sender's file, which is being reconstructed  at
         * a  rate  of 110.64 kilobytes per second, and the transfer will finish in 4 seconds if the current rate is maintained until
         * the end.
         *
         * These statistics can be misleading if the incremental transfer algorithm is in use.  For example,  if  the  sender's  file
         * consists  of  the @param is ignored  basis  file  followed  by  additional  data, the reported rate will probably drop dramatically when the
         * receiver gets to the literal data, and the transfer will probably take much longer to finish than the  receiver  estimated
         * as it was finishing the matched part of the file.
         *
         * When the file transfer finishes, rsync replaces the progress line with a summary line that looks like this:
         *
         *      1238099 100%  146.38kB/s    0:00:08  (xfer#5, to-check=169/396)
         *
         * In  this  example,  the  file  was 1238099 bytes long in total, the average rate of transfer for the whole file was 146.38
         * kilobytes per second over the 8 seconds that it took to complete, it was the 5th transfer of a  regular  file  during  the
         * current  rsync  session,  and  there  are  169 more files for the receiver to check (to see if they are up-to-date or not)
         * remaining out of the 396 total files in the file-list.
         *         @param sources one or more sources to sync to the destination.  A source can be a file or URI such
         *                as [USER@]HOST:SRC or rsync://[USER@]HOST[:PORT]/SRC
         *@param dest the destination file being updated.  Acceptable forms include DEST, [USER@]HOST:DEST or
         *             rsync://[USER@]HOST[:PORT]/DEST
         */
        @Command
        public void rsync(@Option("recursive") final boolean recursive,
                          @Option("links") final boolean links,
                          @Option("perms") final boolean perms,
                          @Option("owner") final boolean owner,
                          @Option("group") final boolean group,
                          @Option("devices") final boolean devices,
                          @Option("specials") final boolean specials,
                          @Option("times") final boolean times,
                          @Option("exclude") final Pattern exclude,
                          @Option("exclude-from") final File excludeFrom,
                          @Option("include") final Pattern include,
                          @Option("include-from") final File includeFrom,
                          @Option({"progress", "p"}) @Default("true") final boolean progress,
                          final URI[] sources,
                          final URI dest
        ) {

        }
    }

}
