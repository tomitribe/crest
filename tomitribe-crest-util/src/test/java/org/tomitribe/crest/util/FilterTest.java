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
package org.tomitribe.crest.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FilterTest {

    final List<Person> people = new ArrayList<>();

    {
        final Address address1 = new Address("Springfield Retirement Castle", "Springfield", "FX", "06889");
        final Person abraham = new Person("Abraham", "Simpson", 83, null, null, address1);

        final Address address = new Address("742 Evergreen Terrace", "Springfield", "FX", "06889");
        final Person marge = new Person("Marge", "Simpson", 34, null, null, address);
        final Person homer = new Person("Homer", "Simpson", 36, null, abraham, address);
        final Person lisa = new Person("Lisa", "Simpson", 8, marge, homer, address);
        final Person bart = new Person("Bart", "Simpson", 10, marge, homer, address);

        final Address address3 = new Address("744 Evergreen Terrace", "Springfield", "FX", "06889");
        final Person ned = new Person("Ned", "Flanders", 60, null, null, address3);

        final Address address4 = new Address("1000 Mammon Lane", "Springfield", "FX", "06891");
        final Person charles = new Person("Charles Montgomery", "Burns", 104, null, null, address4);

        final Address address5 = new Address("Moe's Tavern, 555 Walnut St", "Springfield", "FX", "06889");
        final Person moe = new Person("Moe", "Szyslak", 48, null, null, address5);

        people.add(abraham);
        people.add(marge);
        people.add(homer);
        people.add(lisa);
        people.add(bart);
        people.add(ned);
        people.add(charles);
        people.add(moe);
    }

    @Test
    public void testEverythingMatches() throws Exception {

        final Filter<Object> filter = Filter.builder()
                .build();

        final String results = apply(filter);

        assertEquals("Person{firstName='Abraham', lastName='Simpson', age=83, mom=null, dad=null, address=Address{street='Springfield Retirement Castle', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Marge', lastName='Simpson', age=34, mom=null, dad=null, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Homer', lastName='Simpson', age=36, mom=null, dad=Abraham, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Lisa', lastName='Simpson', age=8, mom=Marge, dad=Homer, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Bart', lastName='Simpson', age=10, mom=Marge, dad=Homer, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Ned', lastName='Flanders', age=60, mom=null, dad=null, address=Address{street='744 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Charles Montgomery', lastName='Burns', age=104, mom=null, dad=null, address=Address{street='1000 Mammon Lane', city='Springfield', state='FX', zipCode='06891'}}\n" +
                "Person{firstName='Moe', lastName='Szyslak', age=48, mom=null, dad=null, address=Address{street='Moe's Tavern, 555 Walnut St', city='Springfield', state='FX', zipCode='06889'}}", results);
    }

    @Test
    public void include() throws Exception {

        final Filter<Object> filter = Filter.builder()
                .include("Simpson")
                .build();

        final String results = apply(filter);

        assertEquals("" +
                "Person{firstName='Abraham', lastName='Simpson', age=83, mom=null, dad=null, address=Address{street='Springfield Retirement Castle', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Marge', lastName='Simpson', age=34, mom=null, dad=null, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Homer', lastName='Simpson', age=36, mom=null, dad=Abraham, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Lisa', lastName='Simpson', age=8, mom=Marge, dad=Homer, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Bart', lastName='Simpson', age=10, mom=Marge, dad=Homer, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}", results);
    }

    @Test
    public void includeByField() throws Exception {

        final Filter<Object> filter = Filter.builder()
                .include("Simpson")
                .field("lastName")
                .build();

        final String results = apply(filter);

        assertEquals("" +
                "Person{firstName='Abraham', lastName='Simpson', age=83, mom=null, dad=null, address=Address{street='Springfield Retirement Castle', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Marge', lastName='Simpson', age=34, mom=null, dad=null, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Homer', lastName='Simpson', age=36, mom=null, dad=Abraham, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Lisa', lastName='Simpson', age=8, mom=Marge, dad=Homer, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Bart', lastName='Simpson', age=10, mom=Marge, dad=Homer, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}", results);
    }

    @Test
    public void includeByFieldMiss() throws Exception {

        final Filter<Object> filter = Filter.builder()
                .include("Simpson")
                .field("firstName")
                .build();

        final String results = apply(filter);

        assertEquals("", results);
    }

    @Test
    public void includeByFieldCaseInsensitive() throws Exception {

        final Filter<Object> filter = Filter.builder()
                .include("Simpson")
                .field("LaStNaMe")
                .build();

        final String results = apply(filter);

        assertEquals("" +
                "Person{firstName='Abraham', lastName='Simpson', age=83, mom=null, dad=null, address=Address{street='Springfield Retirement Castle', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Marge', lastName='Simpson', age=34, mom=null, dad=null, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Homer', lastName='Simpson', age=36, mom=null, dad=Abraham, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Lisa', lastName='Simpson', age=8, mom=Marge, dad=Homer, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Bart', lastName='Simpson', age=10, mom=Marge, dad=Homer, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}", results);
    }

    @Test
    public void includeByFieldNested() throws Exception {

        final Filter<Object> filter = Filter.builder()
                .include("Simpson")
                .field("dad.lastName")
                .build();

        final String results = apply(filter);

        assertEquals("" +
                "Person{firstName='Homer', lastName='Simpson', age=36, mom=null, dad=Abraham, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Lisa', lastName='Simpson', age=8, mom=Marge, dad=Homer, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Bart', lastName='Simpson', age=10, mom=Marge, dad=Homer, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}", results);
    }

    @Test
    public void includeByFieldNestedCaseInsensitive() throws Exception {

        final Filter<Object> filter = Filter.builder()
                .include("Simpson")
                .field("DaD.LaStNAme")
                .build();

        final String results = apply(filter);

        assertEquals("" +
                "Person{firstName='Homer', lastName='Simpson', age=36, mom=null, dad=Abraham, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Lisa', lastName='Simpson', age=8, mom=Marge, dad=Homer, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Bart', lastName='Simpson', age=10, mom=Marge, dad=Homer, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}", results);
    }

    @Test
    public void includeByFieldNestedDeepCaseInsensitive() throws Exception {

        final Filter<Object> filter = Filter.builder()
                .include("Simpson")
                .field("DaD.DaD.LaStNAme")
                .build();

        final String results = apply(filter);

        assertEquals("" +
                "Person{firstName='Lisa', lastName='Simpson', age=8, mom=Marge, dad=Homer, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Bart', lastName='Simpson', age=10, mom=Marge, dad=Homer, address=Address{street='742 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}", results);
    }

    @Test
    public void exclude() throws Exception {

        final Filter<Object> filter = Filter.builder()
                .exclude("Simpson")
                .build();

        final String results = apply(filter);

        assertEquals("" +
                "Person{firstName='Ned', lastName='Flanders', age=60, mom=null, dad=null, address=Address{street='744 Evergreen Terrace', city='Springfield', state='FX', zipCode='06889'}}\n" +
                "Person{firstName='Charles Montgomery', lastName='Burns', age=104, mom=null, dad=null, address=Address{street='1000 Mammon Lane', city='Springfield', state='FX', zipCode='06891'}}\n" +
                "Person{firstName='Moe', lastName='Szyslak', age=48, mom=null, dad=null, address=Address{street='Moe's Tavern, 555 Walnut St', city='Springfield', state='FX', zipCode='06889'}}" +
                "", results);
    }


    private String apply(final Filter<Object> filter) {
        final String results = people.stream()
                .filter(filter)
                .map(Person::toString)
                .reduce((s, s2) -> s + "\n" + s2)
                .orElse("");
        return results;
    }


    public static class Person {
        private final String firstName;
        private final String lastName;
        private final int age;
        private final Person mom;
        private final Person dad;
        private final Address address;

        public Person(final String firstName, final String lastName, final int age, final Person mom, final Person dad, final Address address) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
            this.mom = mom;
            this.dad = dad;
            this.address = address;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public int getAge() {
            return age;
        }

        public Person getMom() {
            return mom;
        }

        public Person getDad() {
            return dad;
        }

        public Address getAddress() {
            return address;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", age=" + age +
                    ", mom=" + (mom == null ? null : mom.getFirstName()) +
                    ", dad=" + (dad == null ? null : dad.getFirstName()) +
                    ", address=" + address +
                    '}';
        }
    }

    public static class Address {
        private final String street;
        private final String city;
        private final String state;
        private final String zipCode;

        public Address(final String street, final String city, final String state, final String zipCode) {
            this.street = street;
            this.city = city;
            this.state = state;
            this.zipCode = zipCode;
        }

        public String getStreet() {
            return street;
        }

        public String getCity() {
            return city;
        }

        public String getState() {
            return state;
        }

        public String getZipCode() {
            return zipCode;
        }

        @Override
        public String toString() {
            return "Address{" +
                    "street='" + street + '\'' +
                    ", city='" + city + '\'' +
                    ", state='" + state + '\'' +
                    ", zipCode='" + zipCode + '\'' +
                    '}';
        }
    }


}