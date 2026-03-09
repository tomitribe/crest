---
title: "Border Styles"
description: "All available table border styles in Crest, from ASCII and Unicode to CSV and markup formats."
weight: 2
---

Crest provides 15 border styles through the `Border` enum. The default is `asciiCompact`.

## Available Styles

| Enum Value                  | Description                            |
|-----------------------------|----------------------------------------|
| `asciiCompact`              | Compact ASCII borders (default)        |
| `asciiDots`                 | Dotted ASCII borders                   |
| `asciiSeparated`            | ASCII with row separators              |
| `githubMarkdown`            | GitHub-flavored Markdown table         |
| `mysqlStyle`                | MySQL client output style              |
| `unicodeDouble`             | Double-line Unicode box drawing        |
| `unicodeSingle`             | Single-line Unicode box drawing        |
| `unicodeSingleSeparated`    | Single-line Unicode with row separators|
| `whitespaceCompact`         | Compact whitespace-only alignment      |
| `whitespaceSeparated`       | Whitespace with blank line separators  |
| `tsv`                       | Tab-separated values                   |
| `csv`                       | Comma-separated values                 |
| `reStructuredTextGrid`      | reStructuredText grid table            |
| `reStructuredTextSimple`    | reStructuredText simple table          |
| `redditMarkdown`            | Reddit-flavored Markdown table         |

## Example Output

Given a table with columns `name`, `version`, and `status`, here is how several popular styles render.

### asciiCompact (default)

```
 name       version   status
---------- --------- --------
 tomcat     9.0.1     active
 jetty      11.0.2    active
 netty      4.1.85    sunset
```

### asciiSeparated

```
 name       version   status
---------- --------- --------
 tomcat     9.0.1     active
---------- --------- --------
 jetty      11.0.2    active
---------- --------- --------
 netty      4.1.85    sunset
---------- --------- --------
```

### githubMarkdown

```
| name   | version | status |
|--------|---------|--------|
| tomcat | 9.0.1   | active |
| jetty  | 11.0.2  | active |
| netty  | 4.1.85  | sunset |
```

### mysqlStyle

```
+--------+---------+--------+
| name   | version | status |
+--------+---------+--------+
| tomcat | 9.0.1   | active |
| jetty  | 11.0.2  | active |
| netty  | 4.1.85  | sunset |
+--------+---------+--------+
```

### unicodeSingle

```
┌────────┬─────────┬────────┐
│ name   │ version │ status │
├────────┼─────────┼────────┤
│ tomcat │ 9.0.1   │ active │
│ jetty  │ 11.0.2  │ active │
│ netty  │ 4.1.85  │ sunset │
└────────┴─────────┴────────┘
```

### unicodeDouble

```
╔════════╦═════════╦════════╗
║ name   ║ version ║ status ║
╠════════╬═════════╬════════╣
║ tomcat ║ 9.0.1   ║ active ║
║ jetty  ║ 11.0.2  ║ active ║
║ netty  ║ 4.1.85  ║ sunset ║
╚════════╩═════════╩════════╝
```

## Usage

Set the border in the `@Table` annotation:

```java
@Command
@Table(fields = "name version status", border = Border.githubMarkdown)
public List<Package> list() {
    return packageService.findAll();
}
```

Or override at runtime with the `--table-border` flag:

```bash
myapp list --table-border=unicodeSingle
```

For data interchange, `tsv` and `csv` produce plain delimited output suitable for piping into other tools:

```bash
myapp list --table-border=csv > packages.csv
```
