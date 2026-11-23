# Recorded protobuf Kotlin output

These files were generated once with the Maven Central artifact
`com.google.protobuf:protoc:3.24.4:exe:windows-x86_64` (`libprotoc 24.4`).

Artifact SHA-1: `8112e008198ace6daf3eaef51f3a50a77e495d33`.

From this directory, the equivalent generation command is:

```text
protoc --proto_path=input --kotlin_out=kotlin input/realistic_api.proto
protoc --proto_path=input --kotlin_out=kotlin input/naming_collisions.proto
protoc --proto_path=input --kotlin_out=kotlin input/lite_runtime.proto
protoc --proto_path=input --kotlin_out=kotlin input/proto2_groups.proto
```

The checked-in Kotlin files are an unmodified golden fixture. Tests must not
download or invoke `protoc` at runtime.
