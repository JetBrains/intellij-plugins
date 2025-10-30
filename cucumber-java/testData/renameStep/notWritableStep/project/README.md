This is a tiny, sample project.

Purpose: to test that an attempt to rename a step, whose definition is in some external library, is correctly handled and rejected.

Build a JAR:

```shell
mvn clean package
```

Move it to the right place:

```shell
cp target/steps-1.0.0.jar ../before/steps-1.0.0.jar
```
