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

import junit.framework.TestCase;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.tomitribe.crest.util.TimeUtils.abbreviate;
import static org.tomitribe.crest.util.TimeUtils.formatMillis;

/**
 * @version $Revision$ $Date$
 */
public class TimeUtilsTest extends TestCase {

    public void testFormatMillis() throws Exception {

        {
            final long time = DAYS.toMillis(1) + HOURS.toMillis(22) + MINUTES.toMillis(33) + SECONDS.toMillis(44) + 555;

            assertEquals("1 day, 22 hours, 33 minutes, 44 seconds and 555 milliseconds", formatMillis(time, MILLISECONDS, DAYS));
            assertEquals("1 day, 22 hours, 33 minutes and 44 seconds", formatMillis(time, SECONDS, DAYS));
            assertEquals("1 day, 22 hours and 33 minutes", formatMillis(time, MINUTES, DAYS));
            assertEquals("1 day and 22 hours", formatMillis(time, HOURS, DAYS));
            assertEquals("1 day", formatMillis(time, DAYS, DAYS));

            assertEquals("46 hours, 33 minutes, 44 seconds and 555 milliseconds", formatMillis(time, MILLISECONDS, HOURS));
            assertEquals("2793 minutes, 44 seconds and 555 milliseconds", formatMillis(time, MILLISECONDS, MINUTES));
            assertEquals("167624 seconds and 555 milliseconds", formatMillis(time, MILLISECONDS, SECONDS));
            assertEquals("167624555 milliseconds", formatMillis(time, MILLISECONDS, MILLISECONDS));
        }

        { // Singular

            final long time = DAYS.toMillis(1) + HOURS.toMillis(1) + MINUTES.toMillis(1) + SECONDS.toMillis(1) + 1;

            assertEquals("1 day, 1 hour, 1 minute, 1 second and 1 millisecond", formatMillis(time, MILLISECONDS, DAYS));
        }

        { // Plural

            final long time = DAYS.toMillis(2) + HOURS.toMillis(2) + MINUTES.toMillis(2) + SECONDS.toMillis(2) + 2;

            assertEquals("2 days, 2 hours, 2 minutes, 2 seconds and 2 milliseconds", formatMillis(time, MILLISECONDS, DAYS));
        }


    }

    public void testFormatHighest() throws Exception {
        final int time = 167624555;
        assertEquals("1 day", TimeUtils.formatHighest(time, DAYS));
        assertEquals("46 hours", TimeUtils.formatHighest(time, HOURS));
        assertEquals("2793 minutes", TimeUtils.formatHighest(time, MINUTES));
        assertEquals("167624 seconds", TimeUtils.formatHighest(time, SECONDS));
        assertEquals("167624555 milliseconds", TimeUtils.formatHighest(time, MILLISECONDS));
    }

    public void testAbbreviate() throws Exception {
        { // Singular

            final long time = DAYS.toMillis(1) + HOURS.toMillis(1) + MINUTES.toMillis(1) + SECONDS.toMillis(1) + 1;

            final String longFormat = formatMillis(time, MILLISECONDS, DAYS);
            final String shortFormat = abbreviate(longFormat);

            assertEquals("1 day, 1 hour, 1 minute, 1 second and 1 millisecond", longFormat);
            assertEquals("1d, 1hr, 1m, 1s and 1ms", shortFormat);
        }

        { // Plural

            final long time = DAYS.toMillis(2) + HOURS.toMillis(2) + MINUTES.toMillis(2) + SECONDS.toMillis(2) + 2;

            final String longFormat = formatMillis(time, MILLISECONDS, DAYS);
            final String shortFormat = abbreviate(longFormat);

            assertEquals("2 days, 2 hours, 2 minutes, 2 seconds and 2 milliseconds", longFormat);
            assertEquals("2d, 2hr, 2m, 2s and 2ms", shortFormat);
        }

    }
}
