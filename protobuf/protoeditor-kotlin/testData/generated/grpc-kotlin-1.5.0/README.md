# Recorded gRPC Kotlin output

`kotlin/demo/grpc/GrpcServiceGrpcKt.kt` was generated once with:

- `com.google.protobuf:protoc:3.24.4:exe:windows-x86_64` (`libprotoc 24.4`);
- `io.grpc:protoc-gen-grpc-kotlin:1.5.0:jdk8@jar`.

The gRPC Kotlin generator artifact SHA-256 is
`DA938E9047A7973D53916EEE33A1465ADCE1E72DCE75127474B8808388023026`.

From this directory, the equivalent generation command is:

```text
protoc --proto_path=input --plugin=protoc-gen-grpckt=<launcher-for-protoc-gen-grpc-kotlin-1.5.0-jdk8.jar> --grpckt_out=kotlin input/grpc_service.proto
```

The checked-in Kotlin file is an unmodified golden fixture. Tests must not
download or invoke either generator at runtime.
