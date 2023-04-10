package org.tomitribe.crest.table;

import org.junit.Test;
import org.tomitribe.crest.api.table.Border;
import org.tomitribe.util.PrintString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
public class FormattingTest {

    @Test
    public void asTableHappyPath() throws IOException {
        final Options options = new Options();
        options.setFields("director.id director.name writer.id writer.name");
        options.setHeader(true);
        options.setBorder(Border.asciiCompact);

        final List<Movie> movies = new ArrayList<>();
        movies.add(new Movie(new Person(123L, "Jon Favreau"), new Person(435L, "Dave Filoni")));

        final PrintString out = new PrintString();
        new TableInterceptor.TableOutput(movies, options).write(out);

        assertEquals(" director.id   director.name   writer.id   writer.name \n" +
                "------------- --------------- ----------- -------------\n" +
                "         123   Jon Favreau           435   Dave Filoni \n", out.toString());
    }

    @Test
    public void asTableNullValues() throws IOException {
        final Options options = new Options();
        options.setFields("director.id director.name writer.id writer.name");
        options.setHeader(true);
        options.setBorder(Border.asciiCompact);

        final List<Movie> movies = new ArrayList<>();
        movies.add(new Movie(null, new Person(null, "Dave Filoni")));

        final PrintString out = new PrintString();
        new TableInterceptor.TableOutput(movies, options).write(out);

        assertEquals(" director.id   director.name   writer.id   writer.name \n" +
                "------------- --------------- ----------- -------------\n" +
                "                                           Dave Filoni \n", out.toString());
    }

    @Test
    public void caseInsensitiveFields() throws IOException {
        final Options options = new Options();
        options.setFields("Director.Id director.nAme wriTer.iD writer.namE");
        options.setHeader(true);
        options.setBorder(Border.asciiCompact);

        final List<Movie> movies = new ArrayList<>();
        movies.add(new Movie(new Person(123L, "Jon Favreau"), new Person(435L, "Dave Filoni")));

        final PrintString out = new PrintString();
        new TableInterceptor.TableOutput(movies, options).write(out);

        assertEquals(" Director.Id   director.nAme   wriTer.iD   writer.namE \n" +
                "------------- --------------- ----------- -------------\n" +
                "         123   Jon Favreau           435   Dave Filoni \n", out.toString());
    }

    public static class Movie {
        private final Person director;
        private final Person writer;

        public Movie(final Person director, final Person writer) {
            this.director = director;
            this.writer = writer;
        }

        public Person getDirector() {
            return director;
        }

        public Person getWriter() {
            return writer;
        }
    }

    public static class Person {
        final Long id;
        final String name;

        public Person(final Long id, final String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}