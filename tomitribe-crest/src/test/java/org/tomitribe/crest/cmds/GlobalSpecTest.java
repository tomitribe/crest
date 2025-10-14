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
package org.tomitribe.crest.cmds;

import org.junit.Test;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.GlobalOptions;
import org.tomitribe.crest.api.Option;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class GlobalSpecTest {

    @Test
    public void single() throws Exception {
        final GlobalSpec globalSpec = GlobalSpec.builder()
                .optionsClass(Color.class)
                .build();

        final List<Object> objects = globalSpec.parse("--red=12", "--green=34", "--blue=56");

        assertEquals("Color{blue=56, red=12, green=34}", objects.get(0).toString());
        assertEquals(1, objects.size());
    }

    @Test
    public void multiple() throws Exception {
        final GlobalSpec globalSpec = GlobalSpec.builder()
                .optionsClass(Color.class)
                .optionsClass(Config.class)
                .build();

        final List<Object> objects = globalSpec.parse("--red=12", "--green=34", "--blue=56", "--name=jcool", "--env=DEV");

        assertEquals("Color{blue=56, red=12, green=34}", objects.get(0).toString());
        assertEquals("Config{environment=DEV, name='jcool'}", objects.get(1).toString());
        assertEquals(2, objects.size());
    }

    @Test
    public void defaultsPartial() throws Exception {
        final GlobalSpec globalSpec = GlobalSpec.builder()
                .optionsClass(Color.class)
                .optionsClass(Config.class)
                .build();

        final List<Object> objects = globalSpec.parse("--red=12");

        assertEquals("Color{blue=0, red=12, green=165}", objects.get(0).toString());
        assertEquals("Config{environment=null, name='null'}", objects.get(1).toString());
        assertEquals(2, objects.size());
    }

    @Test
    public void defaultsFull() throws Exception {
        final GlobalSpec globalSpec = GlobalSpec.builder()
                .optionsClass(Color.class)
                .optionsClass(Config.class)
                .build();

        final List<Object> objects = globalSpec.parse();

        assertEquals("Color{blue=0, red=255, green=165}", objects.get(0).toString());
        assertEquals("Config{environment=null, name='null'}", objects.get(1).toString());
        assertEquals(2, objects.size());
    }

    @Test
    public void nullable() throws Exception {
        final GlobalSpec globalSpec = GlobalSpec.builder()
                .optionsClass(HSB.class)
                .optionsClass(Config.class)
                .build();

        final List<Object> objects = globalSpec.parse();

        assertNull(objects.get(0));
        assertEquals("Config{environment=null, name='null'}", objects.get(1).toString());
        assertEquals(2, objects.size());
    }


    @GlobalOptions
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

        @Override
        public String toString() {
            return "Color{" +
                    "blue=" + blue +
                    ", red=" + red +
                    ", green=" + green +
                    '}';
        }
    }

    @GlobalOptions(nillable = true)
    public static class HSB {
        private int hue;
        private float saturation;
        private float brightness;

        public HSB(@Default("1.0") @Option("brightness") final float brightness,
                   @Default("1.0") @Option("saturation") final float saturation,
                   @Default("39") @Option("hue") final int hue) {
            this.brightness = brightness;
            this.saturation = saturation;
            this.hue = hue;
        }

        public float getSaturation() {
            return saturation;
        }

        public float getBrightness() {
            return brightness;
        }

        public int getHue() {
            return hue;
        }

        @Override
        public String toString() {
            return "HSB{" +
                    "hue=" + hue +
                    ", saturation=" + saturation +
                    ", brightness=" + brightness +
                    '}';
        }
    }

    @GlobalOptions
    public static class Config {
        private String name;
        private Environment environment;

        public Config(@Option("env") final Environment environment, @Option("name") final String name) {
            this.environment = environment;
            this.name = name;
        }

        enum Environment {
            PROD, DEV;
        }

        @Override
        public String toString() {
            return "Config{" +
                    "environment=" + environment +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

}