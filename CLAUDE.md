# Crest

Annotation-driven CLI framework (`org.tomitribe.crest`).

## Command-line syntax in ALL examples

Crest options take values ONLY as `--foo=bar`. The space-separated form
`--foo bar` is NOT supported — it parses `--foo` as the boolean "true" and
`bar` as a positional argument. Before finishing any edit containing a Crest
command line (docs, README, changelog, tests, error-message recovery text),
scan for `--name value` patterns and convert to `--name=value`. Bare flags
with no value (`--verbose`, `help --all`) are fine.

## Build

- Java 11+ required to build (checkstyle); artifacts target Java 8 — no
  post-Java-8 APIs in main sources.
- Verify with: `mvn -pl tomitribe-crest -am test`
