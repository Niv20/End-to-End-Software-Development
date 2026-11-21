# Workshop Registration System – EX2 Maven Evolution

## Project structure

```
root                  aggregator + top parent (this folder)
|-- core              aggregator + parent of the implementation modules
|   |-- model-impl    Person, Instructor, Student, Workshop and Team
|   `-- logic-impl    WorkshopSystemImpl and the managers it delegates to
`-- app               workshop-cli: Main, log4j2.xml, students.csv -> workshop-cli.jar
lib                   the API jars and the tester we received (not on Maven Central)
```

All versions are properties in the root pom and managed there in `dependencyManagement` /
`pluginManagement`. The child poms never write a version.

## One-time setup: install the API jars

The API jars and the tester are not on Maven Central, so they have to be installed into the
local repository (`~/.m2`) once on every computer. Run these commands from the root folder:

```bash
mvn install:install-file -Dfile=lib/model-2.0.jar -DgroupId=patmal.workshops -DartifactId=model -Dversion=2.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=lib/logic-2.0.jar -DgroupId=patmal.workshops -DartifactId=logic -Dversion=2.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=lib/workshop-tester-2.0.jar -DgroupId=patmal.workshops -DartifactId=workshop-tester -Dversion=2.0 -Dpackaging=jar -DgeneratePom=true
```

In PowerShell put every `-D...` argument in quotes, for example `"-Dfile=lib/model-2.0.jar"`.

`-DgeneratePom=true` is needed because each jar contains a pom of its own whose parent
(`patmal.workshops:workshops-aggregator`) does not exist anywhere. Without the flag Maven installs
that pom and warns that it is invalid. With the flag it writes a simple pom with exactly the
coordinates above. Such a pom has no dependencies, so every module that uses the `logic` API
lists `model` as well.

## Build

```bash
mvn clean install
```

This builds all the modules in the right order and creates `app/target/workshop-cli.jar`.
In the `verify` phase the build runs the tester against the implementation, and a failing tester
fails the build. To build without it: `mvn clean install -Dexec.skip`.

## Run

```bash
java -jar app/target/workshop-cli.jar
```

The application creates demo data and writes `instructors.json`, `students.json` and `teams.json`
to the current folder. The students are read from `students.csv` inside the jar, or from another
CSV file given as an argument:

```bash
java -jar app/target/workshop-cli.jar my-students.csv
```

The log level of our classes is `info` and can be changed, for example
`java -Dworkshop.log.level=warn -jar app/target/workshop-cli.jar`.

## Run the tester by hand

```bash
java -cp "lib/workshop-tester-2.0.jar:app/target/workshop-cli.jar" patmal.workshops.main.Main
```

(On Windows use `;` instead of `:`.) The expected result is `RESULT: ALL OK`.

## Libraries

| Library | Module | Scope | Used for |
|---|---|---|---|
| log4j-api 2.26.1 | logic-impl, app | compile | logging calls in the code |
| log4j-core 2.26.1 | app | runtime | the logging implementation, needed only when the application runs |
| commons-validator 1.11.0 | logic-impl | compile | e-mail validation (`EmailValidator`) |
| jackson-databind 2.23.0 | logic-impl | compile | JSON export |
| commons-csv 1.14.1 | logic-impl | compile | CSV import |
| workshop-tester 2.0 | app | test | runs in the build, so it is not packed into the jar |

Log4j and Jackson versions come from their BOMs (`log4j-bom`, `jackson-bom`), imported in the
root pom. I chose Log4j 2 because it does not need SLF4J, and Jackson because it is the most
widely used JSON library. commons-validator and commons-csv were recommended in the exercise.
All of them are stable releases from Maven Central.

The uber jar is built with `maven-shade-plugin`, which also sets the main class and
`Multi-Release: true` in the manifest (Log4j has classes for newer Java versions).

## Transitive dependencies

Output of `mvn dependency:tree` for logic-impl:

```
+- commons-validator:commons-validator:jar:1.11.0:compile
|  +- commons-beanutils:commons-beanutils:jar:1.11.0:compile
|  +- commons-digester:commons-digester:jar:2.1:compile
|  +- commons-logging:commons-logging:jar:1.4.0:compile
|  \- commons-collections:commons-collections:jar:3.2.2:compile
+- com.fasterxml.jackson.core:jackson-databind:jar:2.23.0:compile
|  +- com.fasterxml.jackson.core:jackson-annotations:jar:2.23:compile
|  \- com.fasterxml.jackson.core:jackson-core:jar:2.23.0:compile
\- org.apache.commons:commons-csv:jar:1.14.1:compile
   +- commons-io:commons-io:jar:2.20.0:compile
   \- commons-codec:commons-codec:jar:1.19.0:compile
```

- commons-beanutils, commons-digester, commons-logging and commons-collections were brought by
  commons-validator (they are used by its form validation part, not by `EmailValidator`).
- jackson-core (the streaming parser/generator) and jackson-annotations were brought by
  jackson-databind.
- commons-io and commons-codec were brought by commons-csv.
- In the app module, log4j-api also comes with log4j-core.
