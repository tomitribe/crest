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

import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;

import java.io.File;
import java.net.URI;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

public class JavadocHelpComplexTest {

    @Test
    public void test() throws Exception {
        final TestEnvironment env = TestEnvironment.builder().build();
        new Main(Rsync.class).main(env, new String[]{"help", "rsync"});

        assertEquals("NAME\n" +
                        "       rsync\n" +
                        "\n" +
                        "SYNOPSIS\n" +
                        "       rsync [options] URI... URI\n" +
                        "\n" +
                        "DESCRIPTION\n" +
                        "       rsync  is a program that behaves in much the same way that rcp does, but has many more\n" +
                        "       options and uses the rsync remote-update protocol to greatly speed up  file  transfers\n" +
                        "       when the destination file is being updated.\n" +
                        "\n" +
                        "       The rsync remote-update protocol allows rsync to transfer just the differences between\n" +
                        "       two sets of files across the network connection, using an efficient check-  sum-search\n" +
                        "       algorithm  described  in  the  technical  report  that  accompanies  this  pack-  age.\n" +
                        "\n" +
                        "OPTIONS\n" +
                        "       --recursive\n" +
                        "              This  tells  rsync  to  copy  directories  recursively.   See also --dirs (-d).\n" +
                        "\n" +
                        "       --links\n" +
                        "              When  symlinks  are  encountered,  recreate  the  symlink  on  the destination.\n" +
                        "\n" +
                        "       --perms\n" +
                        "              This option causes the receiving rsync to set the destination permissions to be\n" +
                        "              the same as the source permissions.  (See also the --chmod option for a way  to\n" +
                        "              modify what rsync considers to be the source permissions.)\n" +
                        "\n" +
                        "       --owner\n" +
                        "              This  option  causes  rsync  to set the owner of the destination file to be the\n" +
                        "              same as the source file, but only if the receiving rsync is being  run  as  the\n" +
                        "              super-user  (see  also  the --super option to force rsync to attempt super-user\n" +
                        "              activities).  Without this option, the owner is set to the invoking user on the\n" +
                        "              receiving side.\n" +
                        "       \n" +
                        "              The preservation of ownership will associate matching names by default, but may\n" +
                        "              fall back  to  using  the  ID  number  in  some  circumstances  (see  also  the\n" +
                        "       \n" +
                        "              o      -numeric-ids option for a full discussion).\n" +
                        "\n" +
                        "       --group\n" +
                        "              This  option  causes  rsync  to set the group of the destination file to be the\n" +
                        "              same as the source file.  If the  receiving  program  is  not  running  as  the\n" +
                        "              super-user (or if --no-super was specified), only groups that the invoking user\n" +
                        "              on the receiving side is a member of will be preserved.  Without  this  option,\n" +
                        "              the  group  is set to the default group of the invok- ing user on the receiving\n" +
                        "              side.\n" +
                        "       \n" +
                        "              The preservation of group information will associate matching names by default,\n" +
                        "              but may fall back to using the ID number in some circumstances  (see  also  the\n" +
                        "              --numeric-ids option for a full discussion).\n" +
                        "\n" +
                        "       --devices\n" +
                        "              This  option  causes  rsync to transfer character and block device files to the\n" +
                        "              remote system to recreate these devices.  This option  has  no  effect  if  the\n" +
                        "              receiving  rsync  is  not  run  as the super-user and --super is not specified.\n" +
                        "\n" +
                        "       --specials\n" +
                        "              This  option  causes  rsync to transfer special files such as named sockets and\n" +
                        "              fifos.\n" +
                        "\n" +
                        "       --times\n" +
                        "              This tells rsync to transfer modification times along with the files and update\n" +
                        "              them on the remote system.    Note  that  if  this  option  is  not  used,  the\n" +
                        "              optimization  that  excludes  files  that  have  not  been  modified  cannot be\n" +
                        "              effective; in other words, a missing -t or -a will cause the next  transfer  to\n" +
                        "              behave  as  if  it  used  -I, causing all files to be updated (though the rsync\n" +
                        "              algorithm will make the update fairly efficient if the files  haven't  actually\n" +
                        "              changed, you're much better off using -t).\n" +
                        "\n" +
                        "       --exclude=<Pattern>\n" +
                        "              This  option  is  a  simplified form of the --filter option that defaults to an\n" +
                        "              exclude rule and does not allow the full rule- parsing syntax of normal  filter\n" +
                        "              rules.\n" +
                        "       \n" +
                        "              See  the  FILTER  RULES  section  for  detailed  information  on  this  option.\n" +
                        "\n" +
                        "       --exclude-from=<File>\n" +
                        "              This  option  is  related to the --exclude option, but it specifies a FILE that\n" +
                        "              contains exclude patterns (one per line).  Blank lines in the  file  and  lines\n" +
                        "              starting with ';' or '#' are ignored.  If FILE is -, the list will be read from\n" +
                        "              standard input.\n" +
                        "\n" +
                        "       --include=<Pattern>\n" +
                        "              This  option  is  a  simplified form of the --filter option that defaults to an\n" +
                        "              include rule and does not allow the full rule-parsing syntax of  normal  filter\n" +
                        "              rules.\n" +
                        "       \n" +
                        "              See  the  FILTER  RULES  section  for  detailed  information  on  this  option.\n" +
                        "\n" +
                        "       --include-from=<File>\n" +
                        "              This  option  is related to the --include option, but it specifies a FILE t hat\n" +
                        "              contains include patterns (one per line).  Blank lines in the  file  and  lines\n" +
                        "              starting with ';' or '#' are ignored.  If FILE is -, the list will be read from\n" +
                        "              standard input.\n" +
                        "\n" +
                        "       --no-progress, --no-p\n" +
                        "              This  option  tells  rsync  to  print  information  showing the progress of the\n" +
                        "              transfer.  This gives a bored user something to watch.  Implies --verbose if it\n" +
                        "              wasn't already specified.\n" +
                        "       \n" +
                        "              While  rsync  is  transferring  a regular file, it updates a progress line that\n" +
                        "              looks like this:\n" +
                        "       \n" +
                        "                    782448  63%  110.64kB/s    0:00:04\n" +
                        "       \n" +
                        "              In  this  example,  the  receiver  has reconstructed 782448 bytes or 63% of the\n" +
                        "              sender's file, which is being reconstructed at a rate of 110.64  kilobytes  per\n" +
                        "              second,  and  the  transfer  will  finish  in  4 seconds if the current rate is\n" +
                        "              maintained until the end.\n" +
                        "       \n" +
                        "              These  statistics can be misleading if the incremental transfer algorithm is in\n" +
                        "              use.  For example, if the sender's file consists of the @param is ignored basis\n" +
                        "              file  followed  by  additional  data,  the  reported  rate  will  probably drop\n" +
                        "              dramatically when the receiver gets to the literal data, and the transfer  will\n" +
                        "              probably  take  much  longer  to  finish  than the receiver estimated as it was\n" +
                        "              finishing the matched part of the file.\n" +
                        "       \n" +
                        "              When  the  file  transfer  finishes,  rsync  replaces  the progress line with a\n" +
                        "              summary line that looks like this:\n" +
                        "       \n" +
                        "                   1238099 100%  146.38kB/s    0:00:08  (xfer#5, to-check=169/396)\n" +
                        "       \n" +
                        "              In  this example, the file was 1238099 bytes long in total, the average rate of\n" +
                        "              transfer for the whole file was 146.38 kilobytes per second over the 8  seconds\n" +
                        "              that  it took to complete, it was the 5th transfer of a regular file during the\n" +
                        "              current rsync session, and there are 169 more files for the receiver  to  check\n" +
                        "              (to  see if they are up-to-date or not) remaining out of the 396 total files in\n" +
                        "              the file-list.\n",
                env.getOut().toString());
    }


    public static class Rsync {

        /**
         * rsync is a program that behaves in much the same way that rcp does,
         * but has many more options and uses the rsync remote-update protocol
         * to greatly speed up file transfers when the destination file is
         * being updated.
         *
         * The  rsync  remote-update  protocol  allows  rsync  to transfer just the differences
         * between two sets of files across the network connection, using an  efficient  check-
         * sum-search  algorithm  described in the technical report that accompanies this pack-
         * age.
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
