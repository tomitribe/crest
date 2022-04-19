/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static java.nio.charset.StandardCharsets.UTF_8

// enables to conditionally include some content

Path projectPath = Paths.get(request.outputDirectory, request.artifactId)
Properties projectProperties = request.properties
String packageName = projectProperties.getProperty('package')

Properties crestProperties = new Properties()
Thread.currentThread().getContextClassLoader()
        .getResourceAsStream('META-INF/maven/org.tomitribe/tomitribe-crest-archetype/pom.properties')
        .withStream {
            crestProperties.load(it)
        }
String crestVersion = crestProperties.getProperty('version')

Path pom = projectPath.resolve('pom.xml')

boolean useArthur = Boolean.parseBoolean(projectProperties.getProperty('enableArthur'))
boolean useBeanValidation = Boolean.parseBoolean(projectProperties.getProperty('enableBeanValidation'))

if (useArthur) {
    Files.write(
            pom,
            new String(Files.readAllBytes(pom), UTF_8)
                    .replace(
                            '</project>',
                            """
  <profiles>
    <profile>
      <id>native</id>
      <build>
        <plugins>
          <plugin>
            <groupId>org.apache.geronimo.arthur</groupId>
            <artifactId>arthur-maven-plugin</artifactId>
            <version>1.0.5</version>
            <configuration>
              <main>org.tomitribe.crest.Main</main>
              <graalVersion>22.0.0.2.r11</graalVersion>
              <enableAllSecurityServices>false</enableAllSecurityServices>
              <graalExtensions>
                <graalExtension>org.tomitribe:tomitribe-crest-arthur-extension:${crestVersion}</graalExtension>
              </graalExtensions>
              <extensionProperties>
                <tomitribe.crest.command.includes>${packageName}</tomitribe.crest.command.includes>
              </extensionProperties>
            </configuration>
          </plugin>
        </plugins>
      </build>
    </profile>
  </profiles>
</project>""").getBytes(UTF_8))
}
if (useBeanValidation) {
    Files.write(
            pom,
            new String(Files.readAllBytes(pom), UTF_8)
                    .replace(
                            '  </dependencies>',
                            """

    <!-- to get bean validation (optional) -->
    <dependency>
      <groupId>org.apache.geronimo.specs</groupId>
      <artifactId>geronimo-validation_2.0_spec</artifactId>
      <version>1.0</version>
    </dependency>
    <dependency>
      <groupId>org.apache.bval</groupId>
      <artifactId>bval-jsr</artifactId>
      <version>1.1.0</version>
    </dependency>
    <dependency> <!-- force because transitive one (bval-jsr) is too old for recent JVM -->
      <groupId>org.apache.commons</groupId>
      <artifactId>commons-lang3</artifactId>
      <version>3.12.0</version>
    </dependency>
  </dependencies>""").getBytes(UTF_8))
} else {
    Files.delete(projectPath.resolve("src/main/java/${packageName.replace('.', '/')}/IsFile.java"))
    Path app = projectPath.resolve("src/main/java/${packageName.replace('.', '/')}/App.java")
    Files.write(
            app,
            new String(Files.readAllBytes(app), UTF_8)
                    .replace('@IsFile ', '')
                    .getBytes(UTF_8))
}
