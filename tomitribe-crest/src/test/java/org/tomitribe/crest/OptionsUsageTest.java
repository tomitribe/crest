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

import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Options;
import org.tomitribe.util.PrintString;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class OptionsUsageTest {

    @Test
    public void optionHelp() throws Exception {

        final PrintString out = new PrintString();
        final PrintString err = new PrintString();
        final AtomicInteger exit = new AtomicInteger();
        final Main main = Main.builder()
                .command(ColorCommands.class)
                .name("color")
                .version("23.5.6")
                .out(out)
                .err(err)
                .exit(exit::set)
                .build();

        {
            main.run("color", "--h");
            assertEquals(String.format("Unknown options: --h%n" +
                    "%n" +
                    "Usage: color color [options]%n" +
                    "%n" +
                    "Options: %n" +
                    "  --opacity=<float>        default: 1.0%n" +
                    "  --blue=<int>             default: 0%n" +
                    "  --green=<int>            default: 165%n" +
                    "  --red=<int>              default: 255%n" +
                    "%n" +
                    "color 23.5.6%n"), err.toString());
        }
    }


    public static class ColorCommands {
        @Command("color")
        public String color(@Default("1.0") @Option("opacity") final float opacity, final Color color) {
            return String.format("opacity %f, red %d, green %d, blue %d", opacity, color.getRed(), color.getGreen(), color.getBlue());
        }
    }

    @Options
    public static class Color {
        private int red;
        private int green;
        private int blue;

        public Color(@Default("0") @Option("blue") final int blue,
                     @Default("165") @Option("green") final int green,
                     @Default("255") @Option("red") final int red) {
            this.blue = blue;
            this.green = green;
            this.red = red;
        }

        public int getBlue() {
            return blue;
        }

        public int getGreen() {
            return green;
        }

        public int getRed() {
            return red;
        }
    }


}
