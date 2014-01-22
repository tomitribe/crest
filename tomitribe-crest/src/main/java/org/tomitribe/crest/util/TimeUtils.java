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
package org.tomitribe.crest.util;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class TimeUtils {

    /**
     * Converts time to a human readable format within the specified range
     *
     * @param duration the time in milliseconds to be converted
     * @param min      the lowest time unit of interest
     * @param max      the highest time unit of interest
     */
    public static String formatMillis(long duration, TimeUnit min, TimeUnit max) {
        return format(duration, MILLISECONDS, min, max);
    }

    /**
     * Converts time to a human readable format within the specified range
     *
     * @param duration the time in milliseconds to be converted
     * @param min      the lowest time unit of interest
     * @param max      the highest time unit of interest
     */
    public static String formatNanos(long duration, TimeUnit min, TimeUnit max) {
        return format(duration, NANOSECONDS, min, max);
    }

    /**
     * Converts time to a human readable format within the specified range
     *
     * @param duration the time in milliseconds to be converted
     * @param min      the lowest time unit of interest
     */
    public static String formatNanos(long duration, TimeUnit min) {
        return format(duration, NANOSECONDS, min, max());
    }

    public static String format(long duration, final TimeUnit sourceUnit, TimeUnit min) {
        return format(duration, sourceUnit, min, max());
    }

    public static String format(long duration, final TimeUnit sourceUnit) {
        return format(duration, sourceUnit, min(), max());
    }

    private static TimeUnit max() {
        final TimeUnit[] values = TimeUnit.values();
        return values[values.length - 1];
    }

    private static TimeUnit min() {
        return TimeUnit.values()[0];
    }

    /**
     * Converts time to a human readable format within the specified range
     *
     * @param duration the time to be converted
     * @param sourceUnit the unit representing this time
     * @param min      the lowest time unit of interest
     * @param max      the highest time unit of interest
     */
    public static String format(long duration, final TimeUnit sourceUnit, TimeUnit min, TimeUnit max) {
        StringBuilder res = new StringBuilder();

        TimeUnit current = max;

        while (duration > 0) {
            long temp = current.convert(duration, sourceUnit);

            if (temp > 0) {

                duration -= sourceUnit.convert(temp, current);

                res.append(temp).append(" ").append(current.name().toLowerCase());

                if (temp < 2) res.deleteCharAt(res.length() - 1);

                res.append(", ");
            }

            if (current == min) break;

            current = TimeUnit.values()[current.ordinal() - 1];
        }

        // we never got a hit, the time is lower than we care about
        if (res.lastIndexOf(", ") < 0) return "0 " + min.name().toLowerCase();

        // yank trailing  ", "
        res.deleteCharAt(res.length() - 1);
        res.deleteCharAt(res.length() - 1);

        //  convert last ", " to " and"
        int i = res.lastIndexOf(", ");
        if (i > 0) {
            res.deleteCharAt(i);
            res.insert(i, " and");
        }

        return res.toString();
    }

    /**
     * Converts time to a human readable format within the specified range
     *
     * @param duration the time in milliseconds to be converted
     * @param max      the highest time unit of interest
     */
    public static String formatHighest(long duration, TimeUnit max) {
        TimeUnit[] units = TimeUnit.values();

        StringBuilder res = new StringBuilder();

        TimeUnit current = max;

        while (duration > 0) {
            long temp = current.convert(duration, MILLISECONDS);

            if (temp > 0) {

                duration -= current.toMillis(temp);

                res.append(temp).append(" ").append(current.name().toLowerCase());

                if (temp < 2) res.deleteCharAt(res.length() - 1);

                break;
            }

            if (current == MILLISECONDS) break;

            current = units[(current.ordinal() - 1)];
        }

        // we never got a hit, the time is lower than we care about
        return res.toString();
    }

    public static String abbreviate(String time) {
        time = time.replaceAll(" days", "d");
        time = time.replaceAll(" day", "d");
        time = time.replaceAll(" hours", "hr");
        time = time.replaceAll(" hour", "hr");
        time = time.replaceAll(" minutes", "m");
        time = time.replaceAll(" minute", "m");
        time = time.replaceAll(" seconds", "s");
        time = time.replaceAll(" second", "s");
        time = time.replaceAll(" milliseconds", "ms");
        time = time.replaceAll(" millisecond", "ms");
        return time;
    }

    public static String daysAndMinutes(long duration) {
        return formatMillis(duration, MINUTES, DAYS);
    }

    public static String hoursAndMinutes(long duration) {
        return formatMillis(duration, MINUTES, HOURS);
    }

    public static String hoursAndSeconds(long duration) {
        return formatMillis(duration, SECONDS, HOURS);
    }
}
