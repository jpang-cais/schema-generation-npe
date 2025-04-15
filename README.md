### NullPointerException with Schema Generation

The repository contains code that demonstrates a breaking change when upgrading between versions 7.8.1 and 7.8.2 of the 
`io.confluent:kafka-json-schema-serializer` artifact.

[SchemaGenerationTest.kt](src/test/kotlin/org/example/SchemaGenerationTest.kt) contains a test that fails with the latest version of the
artifact. Reverting to a previous minor version (7.8.1) resolves the issue.

The underlying issue seems to be moving from `com.kjetland:mbknor-jackson-jsonschema_2.13:1.0.39` to 
`io.yokota:mbknor-jackson-jsonschema-java8:1.0.39.2`.
