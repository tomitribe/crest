---
title: "Example"
description: "A real Crest command shown end-to-end: source javadoc and rendered terminal output."
weight: 6
---

[Best Practices]({{< ref "best-practices" >}}) names the conventions. This page shows them applied to a real command -- source javadoc above, rendered terminal output below.

The command is `distribe job create`, which schedules a CLI command to run on a cron-like trigger. It's worth showing because it exercises most of Crest's help features: ALL-CAPS section headings, bullets with backtick-emphasized tokens, preformatted command examples, and `@param` descriptions.

## Source

```java
/**
 * Schedule a distribe command to be executed in cron-like fashion.
 *
 * NAMES
 *
 * The name of the Schedule will default to the value of the command arguments
 * joined by a "-". However, the name can be explicitly set via `--name`
 *
 * CRON-BASED SCHEDULES
 *
 * A cron expression creates a fine-grained recurring schedule that runs at a
 * specific time of your choosing. EventBridge Scheduler supports configuring
 * cron-based schedules in Universal Coordinated Time (UTC), or in the time
 * zone that you specify when you create your schedule. For example, you can
 * create a cron-based schedule that runs at 8:00 a.m. PST on the first Monday
 * of every month.
 *
 * A cron expression consists of five required fields separated by white space:
 *
 * minutes hours day-of-month month day-of-week year
 *
 * Acceptable values include:
 *
 * - `minute` accepts values `0-59` and wildcards `, - * /`
 * - `hour` accepts values `0-23` and wildcards `, - * /`
 * - `day-of-month` accepts values `1-31` and wildcards `, - * ? / L W`
 * - `month` accepts values `1-12` or `JAN-DEC` and wildcards `, - * /`
 * - `day-of-week` accepts values `1-7` or `SUN-SAT` and wildcards `, - * ? L #`
 * - `year` accepts values `1970-2199` and wildcards `, - * /`
 *
 * EXAMPLES
 *
 * Say you want to list the releases every month and email them to
 * support-team@tomitribe.com:
 *
 *     distribe job create --day-of-month=1 --to=support-team@tomitribe.com s3 releases
 *
 * IDEMPOTENCY
 *
 * This command cannot be run with the same arguments multiple times. Once a
 * schedule is created with a specific name, no other schedules can be created
 * with that name. Use `--name` to set the name explicitly instead of relying
 * on defaults.
 *
 * SYSTEMS
 *
 * - AWS EventBridge (read,write)
 * - AWS Simple Email Service (write)
 *
 * IMPACT
 *
 * This command creates a schedule in AWS EventBridge that triggers the
 * JobsLambda setup as an AWS Lambda.
 *
 * RELATED
 *
 * To list the current schedules
 *
 *     distribe job list
 *
 * To get greater detail on any specific job
 *
 *     distribe job get the-job-name
 *
 * To delete an existing job so it can be redefined
 *
 *     distribe job delete the-job-name
 *
 * @param to       the email addresses where the command will be sent
 * @param name     the unique name of the job
 * @param command  the distribe command to execute, minus the "distribe" prefix
 */
@Command
public String create(
    @Option("to") final List<Email> to,
    @Option("name") String name,
    final Schedule schedule,
    final Config config,
    final String... command) { ... }
```

## Rendered

{{< crest-help >}}
NAME
       create

SYNOPSIS
       job create [options] String...

DESCRIPTION
       Schedule a distribe command to be executed in cron-like fashion.

NAMES

       The name of the Schedule will default to the value of the command
       arguments joined by a "-". However, the name can be explicitly set
       via `--name`

CRON-BASED SCHEDULES

       A cron expression creates a fine-grained recurring schedule that runs
       at a specific time of your choosing. EventBridge Scheduler supports
       configuring cron-based schedules in Universal Coordinated Time (UTC),
       or in the time zone that you specify when you create your schedule.
       For example, you can create a cron-based schedule that runs at 8:00
       a.m. PST on the first Monday of every month.

       A cron expression consists of five required fields separated by white
       space:

       minutes hours day-of-month month day-of-week year

       Acceptable values include:

       o      `minute` accepts values `0-59` and wildcards `, - * /`

       o      `hour` accepts values `0-23` and wildcards `, - * /`

       o      `day-of-month` accepts values `1-31` and wildcards `, - * ? / L W`

       o      `month` accepts values `1-12` or `JAN-DEC` and wildcards `, - * /`

       o      `day-of-week` accepts values `1-7` or `SUN-SAT` and wildcards `, - * ? L #`

       o      `year` accepts values `1970-2199` and wildcards `, - * /`
{{< /crest-help >}}

Notice how the source maps to the rendering:

- ALL-CAPS lines (`NAMES`, `CRON-BASED SCHEDULES`, etc.) become bold section headings
- Backticks around `--name`, `minute`, `0-59`, and `, - * /` make those tokens stand out from prose without using color
- The 4-space-indented `distribe job create ...` block in the source renders verbatim, preserving the command shape
- Bullets are rendered with the `o` glyph used in traditional man pages

The author didn't write a single piece of formatting markup beyond `-` for bullets, four spaces for code blocks, and backticks for emphasis. The rest is just how the prose was structured.
