# Deeper Command Groups — Working Document

## Goal
Allow arbitrary depth of command group nesting so users can express CLIs like `tool quote line-item add <args-and-options>`. Today Crest supports exactly one level of grouping (`group subcommand`). The end result is multi-level groups with a clean, backward-compatible annotation model.

## Decisions Made
| # | Decision | Rationale | Alternatives Considered |
|---|----------|-----------|------------------------|
| 1 | Express nesting via space-separated `@Command("quote line-item")` on the class | Reads like the CLI invocation. No annotation API change needed (`value` stays `String`). Backward compatible — existing single-word values are a natural subset. Leaves `String[]` available for future command aliases (consistent with `@Option` alias pattern). | See rejected alternatives below |
| 2 | Do not distinguish sub-commands from sub-groups in help listings (for now) | Current behavior already treats them uniformly. May revisit as a separate, isolated change. | Showing groups differently (e.g., with a `>` marker or "Groups:" section) |
| 3 | Description conflicts at deeper levels follow existing behavior | No reason to invent new rules — iterate owners, first non-empty description wins, applied recursively at every level. Consistent, already understood. | New conflict resolution rules |
| 4 | Intermediate groups are auto-created (`mkdir -p` style) | JAX-RS model: `@Command("one two")` on class + `@Command("three four")` on method = path `one two three four`. All intermediate tokens become groups. No placeholder classes needed. If a class later declares that intermediate path, it merges in naturally with its description. | Requiring explicit declarations for every intermediate group |
| 5 | Method-level `@Command` value is a path, not just a name (concatenated with class path) | Follows JAX-RS `@Path` concatenation model. Class path + method path = full command path. All tokens except the last are groups; the last is the leaf command. Fully backward compatible — single-word values work identically. | Method values remain single names only |
| 6 | CmdMethod-vs-CmdGroup name collision is an error (for now) | Today this is silently broken (last-write-wins at top level, ClassCastException inside groups). Fail fast with a clear error message. The `git remote` / `git remote add` pattern (a group that is also a command) is a real use case but is a separate feature — "default commands on groups." We can relax the error to enable that pattern later. | Allow collision (needs default command design), silently overwrite (current broken behavior) |

## Usage Scenarios

### Scenario A: Separate classes (most common)
```java
@Command(value = "quote", description = "Quote management")
public class QuoteCommands {
    @Command(description = "Create a quote")
    public void create(...) { }
    @Command(description = "Remove a quote")
    public void remove(...) { }
}

@Command(value = "quote line-item", description = "Manage line items")
public class QuoteLineItemCommands {
    @Command(description = "Create a line item")
    public void create(...) { }
    @Command(description = "Delete a line item")
    public void delete(...) { }
    @Command(description = "List line items")
    public void list(...) { }
}
```

### Scenario B: Single class with deeper method paths
```java
@Command(value = "quote", description = "Quote management")
public class QuoteCommands {
    @Command(description = "Create a quote")
    public void create(...) { }

    @Command("line-item create")
    public void lineItemCreate(...) { }

    @Command("line-item delete")
    public void lineItemDelete(...) { }
}
```

### Scenario C: Mixed — both contribute to the same tree
Scenarios A and B can coexist. Merge logic handles it uniformly.

### Resulting tree (all scenarios):
```
quote                     → CmdGroup ("Quote management")
├── create                → CmdMethod
├── remove                → CmdMethod
└── line-item             → CmdGroup ("Manage line items" if Scenario A, no desc if B-only)
    ├── create            → CmdMethod
    ├── delete            → CmdMethod
    └── list              → CmdMethod (Scenario A only)
```

## Test Plan

### 1. Basic depth — class-level @Command with spaces (Decision 1)

| Test | What it proves |
|------|---------------|
| `twoLevelExec` — `@Command("config setting")` class with `add` method, exec `"config", "setting", "add", args` | Basic two-level nesting works |
| `threeLevelExec` — `@Command("app server config")` class with `set` method | Three levels works, no artificial depth limit |
| `singleWordUnchanged` — existing `@Command("git")` style still works identically | Backward compatibility |

### 2. Method-level path concatenation (Decision 5)

| Test | What it proves |
|------|---------------|
| `methodPathConcatenation` — `@Command("config")` class + `@Command("setting add")` method, exec `"config", "setting", "add", args` | Class path + method path concatenation works |
| `singleWordMethodUnchanged` — `@Command("config")` class + `@Command("set")` method | Existing single-word method values unchanged |
| `classAndMethodBothMultiWord` — `@Command("app server")` class + `@Command("config set")` method → four levels | Full JAX-RS style concatenation |

### 3. Intermediate group auto-creation — mkdir -p (Decision 4)

| Test | What it proves |
|------|---------------|
| `intermediateGroupCreated` — `@Command("config setting")` with no separate `@Command("config")` class. Exec `"config", "setting", "add"` works | Intermediate `config` group auto-created |
| `intermediateGroupHasNoDescription` — help on the auto-created `config` group shows no description | Auto-created groups have null description |
| `intermediateGroupGetsDescriptionFromLaterClass` — register `@Command("config setting")` first, then `@Command(value = "config", description = "Configuration")` | Description merges into auto-created group |

### 4. Merging at depth (Decision 3)

| Test | What it proves |
|------|---------------|
| `mergeAtDepthSeparateClasses` — two classes both `@Command("config setting")`, different methods. All methods accessible | Merge works at depth > 1 |
| `mergeIntermediateAndDeep` — `@Command("config")` class with `set` method + `@Command("config setting")` class with `add` method. Both `"config", "set"` and `"config", "setting", "add"` work | Sibling commands and sub-groups coexist |
| `descriptionConflictAtDepth` — two classes contribute `@Command(value = "config setting", description = "...")` with different descriptions. First non-empty wins | Existing description resolution at depth |

### 5. CmdMethod-vs-CmdGroup collision is an error (Decision 6)

| Test | What it proves |
|------|---------------|
| `collisionMethodAndGroupSameName` — `@Command("config")` class has method `@Command("setting")` AND separate `@Command("config setting")` class. Expect error at build time | Collision detected, clear error message |

### 6. Help output (Decision 2)

| Test | What it proves |
|------|---------------|
| `helpListsDeepGroups` — `help` shows top-level listing including `config` with description | Top-level help unchanged |
| `helpOnIntermediateGroup` — `help config` lists both direct sub-commands (`set`, `remove`) and sub-groups (`setting`) | Help at intermediate level works |
| `helpOnDeepSubCommand` — `help config setting add` shows the leaf command's man page | Help drills through multiple levels |

### 7. Error cases

| Test | What it proves |
|------|---------------|
| `missingSubCommandAtDepth` — exec `"config", "setting"` with no further args | Same "Missing sub-command" behavior at every level |
| `noSuchSubCommandAtDepth` — exec `"config", "setting", "bogus"` | Same "No such sub-command" behavior at every level |

## Future Work (identified but out of scope)
- **Default commands on groups**: The `git remote` / `git remote add` pattern where a name is both a command and a group. Requires its own design session for the annotation model and dispatch semantics.
- **Distinguishing groups from commands in help listings**: May want a visual distinction (e.g., `>` marker, separate "Groups:" section) but should be a separate, isolated change.

## Rejected Alternatives
| Alternative | Why Rejected |
|-------------|-------------|
| `@Command({"quote", "line-item"})` — String array for path | Uses up the array semantic on `value`. `@Option` uses arrays for aliases (`@Option({"v", "verbose"})`). If we later want command aliases (`@Command({"rm", "remove"})`), we'd have eliminated the ability to do it consistently with `@Option`. Also requires changing `value` from `String` to `String[]`, which is a breaking API change. |
| Nested inner classes | Java inner classes are awkward. Hard to support multiple classes contributing to the same deep group via merging. Couples the Java structure to the command structure. |
| Dotted/slashed names (`@Command("quote.line-item")`) | Separator character is arbitrary and doesn't match the CLI invocation. Dot collides with Java naming conventions; slash looks like a file path. |
| Allow CmdMethod-vs-CmdGroup collision silently | Currently broken (last-write-wins or ClassCastException). Needs intentional design as "default commands on groups" feature. |

## Action Items
| # | Item | Dependencies | Issue |
|---|------|-------------|-------|
