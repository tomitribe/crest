package org.tomitribe.crest.table;

import org.junit.Test;
import org.tomitribe.crest.api.table.Border;
import org.tomitribe.util.PrintString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        new TableOutput(movies, options).write(out);

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
        new TableOutput(movies, options).write(out);

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
        new TableOutput(movies, options).write(out);

        assertEquals(" Director.Id   director.nAme   wriTer.iD   writer.namE \n" +
                "------------- --------------- ----------- -------------\n" +
                "         123   Jon Favreau           435   Dave Filoni \n", out.toString());
    }

    @Test
    public void mapsOfMaps() throws Exception {
        final Options options = new Options();
        options.setFields("Director.Id director.nAme wriTer.iD writer.namE");
        options.setHeader(true);
        options.setBorder(Border.asciiCompact);

        final Map<String, Object> director = new HashMap<>();
        director.put("name", "Jon Favreau");
        director.put("id", 123L);

        final Map<String, Object> writer = new HashMap<>();
        writer.put("name", "Dave Filoni");
        writer.put("id", 435L);

        final Map<String, Object> movie = new HashMap<>();
        movie.put("writer", writer);
        movie.put("director", director);

        final List<Map<String, Object>> movies = new ArrayList<>();
        movies.add(movie);

        final PrintString out = new PrintString();
        new TableOutput(movies, options).write(out);

        assertEquals(" Director.Id   director.nAme   wriTer.iD   writer.namE \n" +
                "------------- --------------- ----------- -------------\n" +
                "         123   Jon Favreau           435   Dave Filoni \n", out.toString());
    }

    /**
     * If the datasource is a map, we retain the order in the map (if any)
     */
    @Test
    public void mapsOfMapsKeyOrder() throws Exception {
        final Options options = new Options();
        options.setHeader(true);
        options.setBorder(Border.asciiCompact);

        final Map<String, Object> show = new LinkedHashMap<>();
        show.put("name", "The Mandalorian");
        show.put("directorId", 123L);
        show.put("directorName", "Jon Favreau");
        show.put("writerId", 435L);
        show.put("writerName", "Dave Filoni");

        final List<Map<String, Object>> shows = new ArrayList<>();
        shows.add(show);

        final PrintString out = new PrintString();
        new TableOutput(shows, options).write(out);

        assertEquals("      name         directorId   directorName   writerId   writerName  \n" +
                "----------------- ------------ -------------- ---------- -------------\n" +
                " The Mandalorian          123   Jon Favreau         435   Dave Filoni \n", out.toString());
    }

    /**
     * If the datasource is a map and has dots, they can be escaped
     */
    @Test
    public void escapeDots() throws Exception {
        final Options options = new Options();
        options.setHeader(true);
        options.setBorder(Border.asciiCompact);
        options.setFields("name director\\.id director\\.name writer\\.id writer\\.name");

        final Map<String, Object> show = new LinkedHashMap<>();
        show.put("name", "The Mandalorian");
        show.put("director.id", 123L);
        show.put("director.name", "Jon Favreau");
        show.put("writer.id", 435L);
        show.put("writer.name", "Dave Filoni");

        final List<Map<String, Object>> shows = new ArrayList<>();
        shows.add(show);

        final PrintString out = new PrintString();
        new TableOutput(shows, options).write(out);

        assertEquals("      name         director.id   director.name   writer.id   writer.name \n" +
                "----------------- ------------- --------------- ----------- -------------\n" +
                " The Mandalorian           123   Jon Favreau           435   Dave Filoni \n", out.toString());
    }

    @Test
    public void mapWithDotsAndDefaultHeader() throws Exception {
        final Options options = new Options();
        options.setHeader(true);
        options.setBorder(Border.asciiCompact);

        final Map<String, Object> show = new LinkedHashMap<>();
        show.put("name", "The Mandalorian");
        show.put("director.id", 123L);
        show.put("director.name", "Jon Favreau");
        show.put("writer.id", 435L);
        show.put("writer.name", "Dave Filoni");

        final List<Map<String, Object>> shows = new ArrayList<>();
        shows.add(show);

        final PrintString out = new PrintString();
        new TableOutput(shows, options).write(out);

        assertEquals("      name         director.id   director.name   writer.id   writer.name \n" +
                "----------------- ------------- --------------- ----------- -------------\n" +
                " The Mandalorian           123   Jon Favreau           435   Dave Filoni \n", out.toString());
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