/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.crest;

import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.cmds.CmdGroup;
import org.tomitribe.crest.cmds.processors.Commands;
import org.tomitribe.util.PrintString;

import java.util.Map;

/**
 * Tests for multi-level command group nesting via space-separated
 * {@code @Command} values.  For example, {@code @Command("config setting")}
 * on a class produces the command path {@code config setting <subcommand>}.
 *
 * <p>The design follows the JAX-RS {@code @Path} concatenation model:
 * the class-level path and method-level path are concatenated, with
 * every token except the last becoming a command group and the last
 * token becoming the leaf command.
 */
public class DeepCommandGroupsTest extends Assert {

    // ---------------------------------------------------------------
    // 1. Basic depth — class-level @Command with spaces (Decision 1)
    // ---------------------------------------------------------------

    /**
     * A class annotated {@code @Command("config setting")} should produce
     * a two-level group hierarchy.  Executing {@code config setting add}
     * dispatches through the {@code config} group, into the {@code setting}
     * group, and finally invokes the {@code add} command method.
     */
    @Test
    public void twoLevelExec() throws Exception {
        final Main main = Main.builder()
                .command(ConfigSettingCommands.class)
                .build();

        assertEquals("add:color:blue", main.exec("config", "setting", "add", "--name=color", "--value=blue"));
    }

    /**
     * A class annotated {@code @Command("app server config")} should produce
     * a three-level group hierarchy with no artificial depth limit.
     * Executing {@code app server config set} dispatches through all
     * three intermediate groups before invoking the leaf command.
     */
    @Test
    public void threeLevelExec() throws Exception {
        final Main main = Main.builder()
                .command(AppServerConfigCommands.class)
                .build();

        assertEquals("set:port:8080", main.exec("app", "server", "config", "set", "--key=port", "--value=8080"));
    }

    /**
     * Existing single-word {@code @Command("git")} usage must continue
     * to work identically.  This verifies backward compatibility with
     * the original one-level grouping behavior.
     */
    @Test
    public void singleWordUnchanged() throws Exception {
        final Main main = Main.builder()
                .command(GitCommands.class)
                .build();

        assertEquals("push:origin", main.exec("git", "push", "origin"));
        assertEquals("pull:origin", main.exec("git", "pull", "origin"));
    }

    // ---------------------------------------------------------------
    // 2. Method-level path concatenation (Decision 5)
    // ---------------------------------------------------------------

    /**
     * A class annotated {@code @Command("config")} with a method annotated
     * {@code @Command("setting add")} should concatenate the paths, producing
     * the command {@code config setting add}.  The class contributes the
     * first path segment; the method contributes the remaining segments
     * following the JAX-RS {@code @Path} concatenation model.
     */
    @Test
    public void methodPathConcatenation() throws Exception {
        final Main main = Main.builder()
                .command(ConfigWithMethodPath.class)
                .build();

        assertEquals("setting-add:timeout:30", main.exec("config", "setting", "add", "--name=timeout", "--value=30"));
    }

    /**
     * Existing single-word method {@code @Command} values inside a
     * single-word class group must continue to work identically.
     * This verifies backward compatibility for the common case of
     * {@code @Command("config")} on the class and {@code @Command("set")}
     * on the method, producing {@code config set}.
     */
    @Test
    public void singleWordMethodUnchanged() throws Exception {
        final Main main = Main.builder()
                .command(ConfigTopLevel.class)
                .build();

        assertEquals("set:port:8080", main.exec("config", "set", "--key=port", "--value=8080"));
    }

    /**
     * Both the class-level and method-level {@code @Command} values
     * can be multi-word.  {@code @Command("app server")} on the class
     * and {@code @Command("config set")} on the method should concatenate
     * into the four-level path {@code app server config set}.
     */
    @Test
    public void classAndMethodBothMultiWord() throws Exception {
        final Main main = Main.builder()
                .command(AppServerWithMethodPath.class)
                .build();

        assertEquals("set:port:8080", main.exec("app", "server", "config", "set", "--key=port", "--value=8080"));
    }

    // ---------------------------------------------------------------
    // 3. Intermediate group auto-creation — mkdir -p (Decision 4)
    // ---------------------------------------------------------------

    /**
     * When {@code @Command("config setting")} is declared but no class
     * explicitly declares {@code @Command("config")}, the {@code config}
     * group should be auto-created as an intermediate group (mkdir -p
     * style) so that dispatching through it works.
     */
    @Test
    public void intermediateGroupCreated() throws Exception {
        final Main main = Main.builder()
                .command(ConfigSettingCommands.class)
                .build();

        // config was auto-created as an intermediate group
        assertEquals("add:color:blue", main.exec("config", "setting", "add", "--name=color", "--value=blue"));
    }

    /**
     * An auto-created intermediate group should have no description
     * since no class explicitly declared it.  The {@code config} group
     * here exists only because {@code ConfigSettingCommands} declared
     * {@code @Command("config setting")}, implying its existence.
     */
    @Test
    public void intermediateGroupHasNoDescription() {
        final Map<String, Cmd> commands = Commands.get(ConfigSettingCommands.class);
        final Cmd config = commands.get("config");

        assertNotNull(config);
        assertTrue(config instanceof CmdGroup);
        assertNull(config.getDescription());
    }

    /**
     * An auto-created intermediate group should acquire a description
     * when a class later merges into it with an explicit description.
     * Here, {@code ConfigSettingCommands} causes {@code config} to be
     * auto-created without a description; then {@code ConfigTopLevel}
     * declares {@code @Command(value = "config", description = "Configuration")}
     * which merges in and provides the description.
     */
    @Test
    public void intermediateGroupGetsDescriptionFromLaterClass() {
        final PrintString out = new PrintString();
        final Main display = Main.builder()
                .command(ConfigSettingCommands.class)
                .command(ConfigTopLevel.class)
                .out(out)
                .build();

        display.run("help");

        final String output = out.toString();
        assertTrue(output.contains("Configuration"));
    }

    // ---------------------------------------------------------------
    // 4. Merging at depth (Decision 3)
    // ---------------------------------------------------------------

    /**
     * Two separate classes both declaring {@code @Command("config setting")}
     * should merge their sub-commands into the same {@code setting} group.
     * {@code ConfigSettingCommands} contributes {@code add} and
     * {@code ConfigSettingExtra} contributes {@code remove}; both must
     * be accessible under {@code config setting}.
     */
    @Test
    public void mergeAtDepthSeparateClasses() throws Exception {
        final Main main = Main.builder()
                .command(ConfigSettingCommands.class)
                .command(ConfigSettingExtra.class)
                .build();

        assertEquals("add:color:blue", main.exec("config", "setting", "add", "--name=color", "--value=blue"));
        assertEquals("remove:color", main.exec("config", "setting", "remove", "--name=color"));
    }

    /**
     * A class declaring {@code @Command("config")} with direct sub-commands
     * and a separate class declaring {@code @Command("config setting")} must
     * coexist.  The {@code config} group should contain both its own direct
     * sub-command {@code set} and the nested {@code setting} sub-group.
     */
    @Test
    public void mergeIntermediateAndDeep() throws Exception {
        final Main main = Main.builder()
                .command(ConfigTopLevel.class)
                .command(ConfigSettingCommands.class)
                .build();

        // Direct sub-command of config
        assertEquals("set:port:8080", main.exec("config", "set", "--key=port", "--value=8080"));
        // Deep sub-command via sub-group
        assertEquals("add:color:blue", main.exec("config", "setting", "add", "--name=color", "--value=blue"));
    }

    /**
     * When two classes both declare {@code @Command("config setting")} with
     * different descriptions, the first non-empty description wins — the
     * same resolution rule used for single-level groups.  Here,
     * {@code ConfigSettingCommands} provides "Manage settings" and
     * {@code ConfigSettingExtra} provides "Extra settings"; the parent
     * group listing should show "Manage settings".
     */
    @Test
    public void descriptionConflictAtDepth() {
        final PrintString out = new PrintString();
        final Main display = Main.builder()
                .command(ConfigSettingCommands.class)
                .command(ConfigSettingExtra.class)
                .out(out)
                .build();

        // The description shows up in the parent group's listing
        display.run("help", "config");

        // Both classes provide descriptions; first non-empty wins
        final String output = out.toString();
        assertTrue(output.contains("Manage settings"));
    }

    // ---------------------------------------------------------------
    // 5. CmdMethod-vs-CmdGroup collision is an error (Decision 6)
    // ---------------------------------------------------------------

    /**
     * If a class has a method named {@code setting} (producing a CmdMethod)
     * and a separate class declares {@code @Command("config setting")}
     * (producing a CmdGroup), both wanting the name "setting" inside the
     * {@code config} group, the framework must throw an error rather than
     * silently overwriting.  A name cannot be both a leaf command and a
     * group containing sub-commands.
     */
    @Test
    public void collisionMethodAndGroupSameName() {
        try {
            Main.builder()
                    .command(ConfigWithSettingMethod.class)
                    .command(ConfigSettingCommands.class)
                    .build();
            fail("Expected an error for CmdMethod vs CmdGroup collision on 'setting'");
        } catch (final Exception e) {
            assertTrue(e.getMessage() != null && e.getMessage().contains("setting"));
        }
    }

    // ---------------------------------------------------------------
    // 6. Help output (Decision 2)
    // ---------------------------------------------------------------

    /**
     * The top-level {@code help} listing should show the root group
     * {@code config} with its description, regardless of whether
     * deeper sub-groups exist beneath it.
     */
    @Test
    public void helpListsDeepGroups() {
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(ConfigTopLevel.class)
                .command(ConfigSettingCommands.class)
                .out(out)
                .build();

        main.run("help");

        final String output = out.toString();
        assertTrue(output.contains("config"));
        assertTrue(output.contains("Configuration"));
    }

    /**
     * Running {@code help config} should list both direct sub-commands
     * (like {@code set} from ConfigTopLevel) and nested sub-groups
     * (like {@code setting} from ConfigSettingCommands) uniformly in
     * the same listing.  Groups and commands are not distinguished
     * visually.
     */
    @Test
    public void helpOnIntermediateGroup() {
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(ConfigTopLevel.class)
                .command(ConfigSettingCommands.class)
                .out(out)
                .build();

        main.run("help", "config");

        final String output = out.toString();
        assertTrue(output.contains("set"));
        assertTrue(output.contains("setting"));
    }

    /**
     * Running {@code help config setting add} should drill through
     * the {@code config} and {@code setting} groups and display the
     * full help for the {@code add} leaf command, including its
     * {@code --name} and {@code --value} options.
     */
    @Test
    public void helpOnDeepSubCommand() {
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(ConfigSettingCommands.class)
                .out(out)
                .build();

        main.run("help", "config", "setting", "add");

        final String output = out.toString();
        assertTrue(output.contains("--name"));
        assertTrue(output.contains("--value"));
    }

    // ---------------------------------------------------------------
    // 7. Error cases
    // ---------------------------------------------------------------

    /**
     * Executing {@code config setting} with no further arguments should
     * produce a "Missing sub-command" error, the same behavior as a
     * single-level group when no sub-command is provided.  This verifies
     * that error handling works at every nesting level.
     */
    @Test
    public void missingSubCommandAtDepth() throws Exception {
        final Main main = Main.builder()
                .command(ConfigSettingCommands.class)
                .build();

        try {
            main.exec("config", "setting");
            fail("Expected missing sub-command error");
        } catch (final Exception e) {
            // Expected — "Missing sub-command" at the setting level
        }
    }

    /**
     * Executing {@code config setting bogus} where {@code bogus} is not
     * a registered sub-command should produce a "No such sub-command"
     * error, the same behavior as a single-level group with an unknown
     * sub-command.
     */
    @Test
    public void noSuchSubCommandAtDepth() throws Exception {
        final Main main = Main.builder()
                .command(ConfigSettingCommands.class)
                .build();

        try {
            main.exec("config", "setting", "bogus");
            fail("Expected no such sub-command error");
        } catch (final Exception e) {
            // Expected — "No such sub-command: bogus"
        }
    }

    // ---------------------------------------------------------------
    // Command classes
    // ---------------------------------------------------------------

    @Command(value = "config setting", description = "Manage settings")
    public static class ConfigSettingCommands {

        @Command(description = "Add a setting")
        public String add(@Option("name") final String name,
                          @Option("value") final String value) {
            return "add:" + name + ":" + value;
        }
    }

    @Command(value = "config setting", description = "Extra settings")
    public static class ConfigSettingExtra {

        @Command(description = "Remove a setting")
        public String remove(@Option("name") final String name) {
            return "remove:" + name;
        }
    }

    @Command(value = "app server config")
    public static class AppServerConfigCommands {

        @Command(description = "Set a config value")
        public String set(@Option("key") final String key,
                          @Option("value") final String value) {
            return "set:" + key + ":" + value;
        }
    }

    @Command("git")
    public static class GitCommands {

        @Command
        public String push(final String remote) {
            return "push:" + remote;
        }

        @Command
        public String pull(final String remote) {
            return "pull:" + remote;
        }
    }

    @Command(value = "config", description = "Configuration")
    public static class ConfigTopLevel {

        @Command(description = "Set a value")
        public String set(@Option("key") final String key,
                          @Option("value") final String value) {
            return "set:" + key + ":" + value;
        }
    }

    @Command("config")
    public static class ConfigWithMethodPath {

        @Command("setting add")
        public String settingAdd(@Option("name") final String name,
                                 @Option("value") final String value) {
            return "setting-add:" + name + ":" + value;
        }
    }

    @Command("app server")
    public static class AppServerWithMethodPath {

        @Command("config set")
        public String configSet(@Option("key") final String key,
                                @Option("value") final String value) {
            return "set:" + key + ":" + value;
        }
    }

    @Command("config")
    public static class ConfigWithSettingMethod {

        @Command(description = "Show settings")
        public String setting() {
            return "settings";
        }
    }
}
