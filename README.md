# Workshop Registration System – EX2 Maven Evolution

## One-time setup: install the API jars

The API jars and the tester are not on Maven Central, so they have to be installed into the
local repository (`~/.m2`) once on every computer. The jars are in the `lib` folder.
Run these commands from the root folder of the project:

```bash
mvn install:install-file -Dfile=lib/model-2.0.jar -DgroupId=patmal.workshops -DartifactId=model -Dversion=2.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=lib/logic-2.0.jar -DgroupId=patmal.workshops -DartifactId=logic -Dversion=2.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=lib/workshop-tester-2.0.jar -DgroupId=patmal.workshops -DartifactId=workshop-tester -Dversion=2.0 -Dpackaging=jar -DgeneratePom=true
```

`-DgeneratePom=true` is needed because each jar contains a pom of its own whose parent
(`patmal.workshops:workshops-aggregator`) does not exist anywhere. Without the flag Maven installs
that pom and warns that it is invalid. With the flag it writes a simple pom with exactly the
coordinates above.

## Build

```bash
mvn clean install
```
