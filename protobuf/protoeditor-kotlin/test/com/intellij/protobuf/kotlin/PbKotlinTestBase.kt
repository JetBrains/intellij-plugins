package com.intellij.protobuf.kotlin

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.protobuf.TestUtils
import com.intellij.protobuf.gencodeutils.GotoExpectationMarker
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

abstract class PbKotlinTestBase : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return TestUtils.getTestHomeDirectory() + "/protoeditor-kotlin/testData/"
    }

    protected fun addProjectFiles() {
        addProto()
        addExtensionsProto()
        addOuterClassProto()
        addProtobufRuntimeStubs()
        addGeneratedJavaStubs()
        addGeneratedOuterClassJavaStubs()
        addGeneratedKotlinStubs()
        VfsRootAccess.allowRootAccess(myFixture.module, TestUtils.getTestHomeDirectory())
    }

    protected fun addClashingNameProjectFiles() {
        addClashingNameProtos()
        addProtobufRuntimeStubs()
        addClashingNameGeneratedJavaStubs()
        addClashingNameGeneratedKotlinStubs()
        VfsRootAccess.allowRootAccess(myFixture.module, TestUtils.getTestHomeDirectory())
    }

    protected fun addProjectFilesWithKotlinGeneratedSourceCommentsOnly() {
        addProto()
        addGeneratedKotlinStubs(includeSourceComments = true)
        VfsRootAccess.allowRootAccess(myFixture.module, TestUtils.getTestHomeDirectory())
    }

    protected fun addRealisticGeneratedKotlinProjectFiles() {
        myFixture.copyFileToProject(
            "generated/protoc-3.24.4/input/realistic_api.proto",
            "src/main/proto/realistic_api.proto"
        )
        myFixture.copyFileToProject(
            "generated/protoc-3.24.4/kotlin/demo/realistic/RealisticApiKt.kt",
            "src/main/kotlin/demo/realistic/RealisticApiKt.kt"
        )
        myFixture.copyFileToProject(
            "generated/protoc-3.24.4/kotlin/demo/realistic/RealisticUserKt.kt",
            "src/main/kotlin/demo/realistic/RealisticUserKt.kt"
        )
        addProtobufRuntimeStubs()
        addKotlinDslRuntimeStubs()
        addRealisticGeneratedJavaStubs()
        VfsRootAccess.allowRootAccess(myFixture.module, TestUtils.getTestHomeDirectory())
    }

    protected fun addNamingCollisionGeneratedKotlinProjectFiles() {
        myFixture.copyFileToProject(
            "generated/protoc-3.24.4/input/naming_collisions.proto",
            "src/main/proto/naming_collisions.proto"
        )
        myFixture.copyFileToProject(
            "generated/protoc-3.24.4/kotlin/demo/naming/NamingCollisionsKt.kt",
            "src/main/kotlin/demo/naming/NamingCollisionsKt.kt"
        )
        myFixture.copyFileToProject(
            "generated/protoc-3.24.4/kotlin/demo/naming/NamingCollisionMessageKt.kt",
            "src/main/kotlin/demo/naming/NamingCollisionMessageKt.kt"
        )
        addProtobufRuntimeStubs()
        addKotlinDslRuntimeStubs()
        addNamingCollisionGeneratedJavaStubs()
        VfsRootAccess.allowRootAccess(myFixture.module, TestUtils.getTestHomeDirectory())
    }

    protected fun addLiteRuntimeGeneratedKotlinProjectFiles() {
        myFixture.copyFileToProject(
            "generated/protoc-3.24.4/input/lite_runtime.proto",
            "src/main/proto/lite_runtime.proto"
        )
        myFixture.copyFileToProject(
            "generated/protoc-3.24.4/kotlin/demo/lite/LiteRuntimeKt.kt",
            "src/main/kotlin/demo/lite/LiteRuntimeKt.kt"
        )
        myFixture.copyFileToProject(
            "generated/protoc-3.24.4/kotlin/demo/lite/LiteUserKt.kt",
            "src/main/kotlin/demo/lite/LiteUserKt.kt"
        )
        addProtobufRuntimeStubs()
        addKotlinDslRuntimeStubs()
        addLiteRuntimeStubs()
        addLiteRuntimeGeneratedJavaStubs()
        VfsRootAccess.allowRootAccess(myFixture.module, TestUtils.getTestHomeDirectory())
    }

    protected fun addGrpcKotlinProjectFiles() {
        myFixture.copyFileToProject(
            "generated/grpc-kotlin-1.5.0/input/grpc_service.proto",
            "src/main/proto/grpc_service.proto"
        )
        myFixture.copyFileToProject(
            "generated/grpc-kotlin-1.5.0/kotlin/demo/grpc/GrpcServiceGrpcKt.kt",
            "src/main/kotlin/demo/grpc/GrpcServiceGrpcKt.kt"
        )
        addGrpcRuntimeStubs()
        addGrpcGeneratedJavaStubs()
        VfsRootAccess.allowRootAccess(myFixture.module, TestUtils.getTestHomeDirectory())
    }

    protected fun addProto2GroupGeneratedKotlinProjectFiles() {
        myFixture.copyFileToProject(
            "generated/protoc-3.24.4/input/proto2_groups.proto",
            "src/main/proto/proto2_groups.proto"
        )
        myFixture.copyFileToProject(
            "generated/protoc-3.24.4/kotlin/demo/groups/Proto2GroupsKt.kt",
            "src/main/kotlin/demo/groups/Proto2GroupsKt.kt"
        )
        myFixture.copyFileToProject(
            "generated/protoc-3.24.4/kotlin/demo/groups/GroupContainerKt.kt",
            "src/main/kotlin/demo/groups/GroupContainerKt.kt"
        )
        addProtobufRuntimeStubs()
        addKotlinDslRuntimeStubs()
        addProto2GroupGeneratedJavaStubs()
        VfsRootAccess.allowRootAccess(myFixture.module, TestUtils.getTestHomeDirectory())
    }

    protected fun addGrpcKotlinServiceImplementation() {
        myFixture.addFileToProject(
            "src/main/kotlin/demo/grpc/GreeterService.kt",
            """
        package demo.grpc

        import kotlinx.coroutines.flow.Flow

        class GreeterService : GreeterGrpcKt.GreeterCoroutineImplBase() {
          override suspend fun sayHello(request: HelloRequest): HelloReply = HelloReply()
          override fun listReplies(request: HelloRequest): Flow<HelloReply> = object : Flow<HelloReply> {}
          override suspend fun collectReplies(requests: Flow<HelloRequest>): HelloReply = HelloReply()
          override fun chat(requests: Flow<HelloRequest>): Flow<HelloReply> = object : Flow<HelloReply> {}
        }
      """.trimIndent()
        )
    }

    protected fun configureUser(testFile: String) {
        val kotlinText = VfsUtil.loadText(myFixture.copyFileToProject("users/$testFile"))
        myFixture.configureByText("Main.kt", kotlinText)
    }

    protected fun testExpectations(
        expectationParser: (PsiFile) -> List<GotoExpectationMarker>,
        action: (GotoExpectationMarker, Int) -> Unit,
    ) {
        val file = checkNotNull(myFixture.file) { "Test file is not configured in myFixture. Use configureUser(...)" }
        val expectations = expectationParser(file)
        val errors = mutableListOf<Throwable>()

        val cleanText = myFixture.file.text
        val caretOffsets = myFixture.editor.caretModel.allCarets.map { it.offset }
        myFixture.editor.caretModel.removeSecondaryCarets()

        var lastStamp = myFixture.editor.document.modificationStamp

        for (expectation in expectations) {
            for (caretOffset in caretOffsets.filter { expectation.textRange.contains(it) }) {
                if (myFixture.editor.document.modificationStamp != lastStamp) {
                    myFixture.configureByText("Main.kt", cleanText)
                    lastStamp = myFixture.editor.document.modificationStamp
                }

                val lineNumber = myFixture.editor.document.getLineNumber(caretOffset) + 1
                myFixture.editor.caretModel.moveToOffset(caretOffset)

                try {
                    action(expectation, lineNumber)
                }
                catch (e: Throwable) {
                    errors.add(e)
                }
            }
        }

        if (errors.isNotEmpty()) {
            val message = errors.joinToString("\n") { it.message ?: it.toString() }
            throw AssertionError(
                "Failed with ${errors.size} error(s). " +
                    "You can set a breakpoint using the line number provided by the failure message below:\n" +
                    message
            ).apply {
                errors.forEach { addSuppressed(it) }
            }
        }
    }

    private fun addProto() {
        myFixture.addFileToProject(
            "src/main/proto/demo/user.proto",
            """
        syntax = "proto3";

        package demo;

        option java_package = "demo.proto";
        option java_multiple_files = true;

        message User {
          string id = 1;
          optional string name = 2;
          repeated Role roles = 3;
          Address address = 4;

          oneof test_oneof {
            string string_choice = 5;
          }

          map<string, int32> labels = 6;
          repeated Address previous_addresses = 7;
          optional string when = 8;
          string line2value = 9;

          message Profile {
            string display_name = 1;

            enum Visibility {
              VISIBILITY_UNSPECIFIED = 0;
              PUBLIC = 1;
            }
          }

          message When {
            string value = 1;
          }
        }

        message Address {
          string city = 1;
          string street = 2;
        }

        enum Role {
          ROLE_UNSPECIFIED = 0;
          ADMIN = 1;
          PLAYER = 2;
        }

        message CreateUserRequest {
          User user = 1;
        }

        message Legacy_message {
          string code = 1;
        }

        message When {}

        message HTTP2_response {}
      """.trimIndent()
        )
    }

    private fun addExtensionsProto() {
        myFixture.addFileToProject(
            "src/main/proto/demo/extensions.proto",
            """
        syntax = "proto2";

        package demo;

        option java_package = "demo.proto";
        option java_multiple_files = true;

        message ExtendableUser {
          extensions 100 to max;
        }
      """.trimIndent()
        )
    }

    private fun addOuterClassProto() {
        myFixture.addFileToProject(
            "src/main/proto/demo/outer.proto",
            """
        syntax = "proto3";

        package demo;

        option java_package = "demo.proto";
        option java_outer_classname = "OuterProto";

        message OuterUser {
          string name = 1;
        }

        enum OuterRole {
          OUTER_ROLE_UNSPECIFIED = 0;
          OUTER_ADMIN = 1;
        }
      """.trimIndent()
        )
    }

    private fun addProtobufRuntimeStubs() {
        addJavaFile(
            "com/google/protobuf/GeneratedMessageV3.java",
            """
        package com.google.protobuf;

        public class GeneratedMessageV3 {
        }
      """
        )

        addJavaFile(
            "com/google/protobuf/ProtocolMessageEnum.java",
            """
        package com.google.protobuf;

        public interface ProtocolMessageEnum {
        }
      """
        )

        addJavaFile(
            "com/google/protobuf/MessageOrBuilder.java",
            """
        package com.google.protobuf;

        public interface MessageOrBuilder {
        }
      """
        )

        addJavaFile(
            "com/google/protobuf/MessageLite.java",
            """
        package com.google.protobuf;

        public interface MessageLite {
        }
      """
        )

        addJavaFile(
            "com/google/protobuf/ByteString.java",
            """
        package com.google.protobuf;

        public final class ByteString {
          public static final ByteString EMPTY = new ByteString();
        }
      """
        )

        addJavaFile(
            "com/google/protobuf/ExtensionLite.java",
            """
        package com.google.protobuf;

        public class ExtensionLite<ContainingType, Type> {
          public boolean isRepeated() {
            return false;
          }
        }
      """
        )
    }

    private fun addRealisticGeneratedJavaStubs() {
        addJavaFile(
            "demo/realistic/RealisticUserOrBuilder.java",
            """
        package demo.realistic;

        public interface RealisticUserOrBuilder extends com.google.protobuf.MessageOrBuilder {
          boolean hasProfile();
          RealisticUser.Profile getProfile();
        }
      """
        )

        addJavaFile(
            "demo/realistic/RealisticUser.java",
            """
        package demo.realistic;

        import com.google.protobuf.ExtensionLite;
        import java.util.List;
        import java.util.Map;

        public final class RealisticUser extends com.google.protobuf.GeneratedMessageV3
            implements RealisticUserOrBuilder, com.google.protobuf.MessageLite {
          public static Builder newBuilder() { return new Builder(); }
          public Builder toBuilder() { return new Builder(); }
          public boolean hasProfile() { return false; }
          public Profile getProfile() { return new Profile(); }

          public enum ContactCase { EMAIL, PHONE, CONTACT_NOT_SET }

          public static final class Builder extends com.google.protobuf.GeneratedMessageV3
              implements RealisticUserOrBuilder {
            public RealisticUser build() { return new RealisticUser(); }
            public String getName() { return ""; }
            public Builder setName(String value) { return this; }
            public Builder clearName() { return this; }
            public boolean hasName() { return false; }
            public String getWhen() { return ""; }
            public Builder setWhen(String value) { return this; }
            public Builder clearWhen() { return this; }
            public boolean hasWhen() { return false; }
            public List<String> getAliasesList() { return List.of(); }
            public Builder addAliases(String value) { return this; }
            public Builder addAllAliases(Iterable<String> values) { return this; }
            public Builder setAliases(int index, String value) { return this; }
            public Builder clearAliases() { return this; }
            public Map<String, Integer> getLabelsMap() { return Map.of(); }
            public Builder putLabels(String key, int value) { return this; }
            public Builder removeLabels(String key) { return this; }
            public Builder putAllLabels(Map<String, Integer> values) { return this; }
            public Builder clearLabels() { return this; }
            public String getEmail() { return ""; }
            public Builder setEmail(String value) { return this; }
            public Builder clearEmail() { return this; }
            public boolean hasEmail() { return false; }
            public long getPhone() { return 0; }
            public Builder setPhone(long value) { return this; }
            public Builder clearPhone() { return this; }
            public boolean hasPhone() { return false; }
            public Profile getProfile() { return new Profile(); }
            public Builder setProfile(Profile value) { return this; }
            public Builder clearProfile() { return this; }
            public boolean hasProfile() { return false; }
            public ContactCase getContactCase() { return ContactCase.CONTACT_NOT_SET; }
            public Builder clearContact() { return this; }
            public <T> T getExtension(ExtensionLite<RealisticUser, T> extension) { return null; }
            public boolean hasExtension(ExtensionLite<RealisticUser, ?> extension) { return false; }
            public Builder clearExtension(ExtensionLite<RealisticUser, ?> extension) { return this; }
            public <T> Builder setExtension(ExtensionLite<RealisticUser, T> extension, T value) { return this; }
            public <E> Builder addExtension(
                ExtensionLite<RealisticUser, List<E>> extension,
                E value
            ) { return this; }
            public <E> Builder setExtension(
                ExtensionLite<RealisticUser, List<E>> extension,
                int index,
                E value
            ) { return this; }
          }

          public static final class Profile extends com.google.protobuf.GeneratedMessageV3
              implements com.google.protobuf.MessageLite {
            public static Builder newBuilder() { return new Builder(); }
            public Builder toBuilder() { return new Builder(); }

            public static final class Builder extends com.google.protobuf.GeneratedMessageV3 {
              public Profile build() { return new Profile(); }
              public String getDisplayName() { return ""; }
              public Builder setDisplayName(String value) { return this; }
              public Builder clearDisplayName() { return this; }
              public boolean hasDisplayName() { return false; }
            }
          }
        }
      """
        )
    }

    private fun addNamingCollisionGeneratedJavaStubs() {
        addJavaFile(
            "demo/naming/NamingCollisionMessage.java",
            """
        package demo.naming;

        import java.util.List;
        import java.util.Map;

        public final class NamingCollisionMessage extends com.google.protobuf.GeneratedMessageV3
            implements com.google.protobuf.MessageLite {
          public static Builder newBuilder() { return new Builder(); }
          public Builder toBuilder() { return new Builder(); }

          public static final class Builder extends com.google.protobuf.GeneratedMessageV3 {
            public NamingCollisionMessage build() { return new NamingCollisionMessage(); }
            public String getName() { return ""; }
            public Builder setName(String value) { return this; }
            public Builder clearName() { return this; }
            public boolean hasName() { return false; }
            public String getHasName() { return ""; }
            public Builder setHasName(String value) { return this; }
            public Builder clearHasName() { return this; }
            public boolean hasHasName() { return false; }
            public String getClearName() { return ""; }
            public Builder setClearName(String value) { return this; }
            public Builder clearClearName() { return this; }
            public boolean hasClearName() { return false; }
            public List<String> getAliases4List() { return List.of(); }
            public Builder addAliases4(String value) { return this; }
            public Builder addAllAliases4(Iterable<String> values) { return this; }
            public Builder setAliases4(int index, String value) { return this; }
            public Builder clearAliases4() { return this; }
            public int getAliasesCount5() { return 0; }
            public Builder setAliasesCount5(int value) { return this; }
            public Builder clearAliasesCount5() { return this; }
            public boolean hasAliasesCount5() { return false; }
            public String getAliasesList6() { return ""; }
            public Builder setAliasesList6(String value) { return this; }
            public Builder clearAliasesList6() { return this; }
            public boolean hasAliasesList6() { return false; }
            public Map<String, Integer> getLabels7Map() { return Map.of(); }
            public Builder putLabels7(String key, int value) { return this; }
            public Builder removeLabels7(String key) { return this; }
            public Builder putAllLabels7(Map<String, Integer> values) { return this; }
            public Builder clearLabels7() { return this; }
            public int getLabelsCount8() { return 0; }
            public Builder setLabelsCount8(int value) { return this; }
            public Builder clearLabelsCount8() { return this; }
            public boolean hasLabelsCount8() { return false; }
          }
        }
      """.trimIndent()
        )
    }

    private fun addLiteRuntimeStubs() {
        addJavaFile(
            "com/google/protobuf/GeneratedMessageLite.java",
            """
        package com.google.protobuf;

        public abstract class GeneratedMessageLite<
            MessageType extends GeneratedMessageLite<MessageType, BuilderType>,
            BuilderType extends GeneratedMessageLite.Builder<MessageType, BuilderType>> {
          public abstract static class Builder<
              MessageType extends GeneratedMessageLite<MessageType, BuilderType>,
              BuilderType extends GeneratedMessageLite.Builder<MessageType, BuilderType>> {}
        }
      """.trimIndent()
        )
        addJavaFile(
            "com/google/protobuf/MessageLiteOrBuilder.java",
            """
        package com.google.protobuf;

        public interface MessageLiteOrBuilder {}
      """.trimIndent()
        )
    }

    private fun addLiteRuntimeGeneratedJavaStubs() {
        addJavaFile(
            "demo/lite/LiteUserOrBuilder.java",
            """
        package demo.lite;

        public interface LiteUserOrBuilder extends com.google.protobuf.MessageLiteOrBuilder {
          boolean hasProfile();
          LiteUser.Profile getProfile();
        }
      """.trimIndent()
        )

        addJavaFile(
            "demo/lite/LiteUser.java",
            """
        package demo.lite;

        import java.util.List;
        import java.util.Map;

        public final class LiteUser
            extends com.google.protobuf.GeneratedMessageLite<LiteUser, LiteUser.Builder>
            implements LiteUserOrBuilder, com.google.protobuf.MessageLite {
          public static Builder newBuilder() { return new Builder(); }
          public Builder toBuilder() { return new Builder(); }
          public boolean hasProfile() { return false; }
          public Profile getProfile() { return new Profile(); }

          public enum ContactCase { EMAIL, PHONE, CONTACT_NOT_SET }

          public static final class Builder
              extends com.google.protobuf.GeneratedMessageLite.Builder<LiteUser, Builder>
              implements LiteUserOrBuilder {
            public LiteUser build() { return new LiteUser(); }
            public String getName() { return ""; }
            public Builder setName(String value) { return this; }
            public Builder clearName() { return this; }
            public boolean hasName() { return false; }
            public List<String> getAliasesList() { return List.of(); }
            public Builder addAliases(String value) { return this; }
            public Builder addAllAliases(Iterable<String> values) { return this; }
            public Builder setAliases(int index, String value) { return this; }
            public Builder clearAliases() { return this; }
            public Map<String, Integer> getLabelsMap() { return Map.of(); }
            public Builder putLabels(String key, int value) { return this; }
            public Builder removeLabels(String key) { return this; }
            public Builder putAllLabels(Map<String, Integer> values) { return this; }
            public Builder clearLabels() { return this; }
            public String getEmail() { return ""; }
            public Builder setEmail(String value) { return this; }
            public Builder clearEmail() { return this; }
            public boolean hasEmail() { return false; }
            public long getPhone() { return 0; }
            public Builder setPhone(long value) { return this; }
            public Builder clearPhone() { return this; }
            public boolean hasPhone() { return false; }
            public Profile getProfile() { return new Profile(); }
            public Builder setProfile(Profile value) { return this; }
            public Builder clearProfile() { return this; }
            public boolean hasProfile() { return false; }
            public ContactCase getContactCase() { return ContactCase.CONTACT_NOT_SET; }
            public Builder clearContact() { return this; }
          }

          public static final class Profile
              extends com.google.protobuf.GeneratedMessageLite<Profile, Profile.Builder>
              implements com.google.protobuf.MessageLite {
            public static Builder newBuilder() { return new Builder(); }
            public Builder toBuilder() { return new Builder(); }

            public static final class Builder
                extends com.google.protobuf.GeneratedMessageLite.Builder<Profile, Builder> {
              public Profile build() { return new Profile(); }
              public String getDisplayName() { return ""; }
              public Builder setDisplayName(String value) { return this; }
              public Builder clearDisplayName() { return this; }
            }
          }
        }
      """.trimIndent()
        )
    }

    private fun addGrpcRuntimeStubs() {
        addJavaFile(
            "io/grpc/BindableService.java",
            """
        package io.grpc;

        public interface BindableService {}
      """.trimIndent()
        )
        addJavaFile(
            "io/grpc/CallOptions.java",
            """
        package io.grpc;

        public class CallOptions {
          public static final CallOptions DEFAULT = new CallOptions();
        }
      """.trimIndent()
        )
        addJavaFile("io/grpc/Channel.java", "package io.grpc; public interface Channel {}")
        addJavaFile("io/grpc/Metadata.java", "package io.grpc; public class Metadata {}")
        addJavaFile(
            "io/grpc/MethodDescriptor.java",
            "package io.grpc; public class MethodDescriptor<RequestT, ResponseT> {}"
        )
        addJavaFile("io/grpc/ServiceDescriptor.java", "package io.grpc; public class ServiceDescriptor {}")
        addJavaFile(
            "io/grpc/ServerServiceDefinition.java",
            """
        package io.grpc;

        public class ServerServiceDefinition {
          public static Builder builder(ServiceDescriptor descriptor) { return new Builder(); }

          public static class Builder {
            public Builder addMethod(Object method) { return this; }
            public ServerServiceDefinition build() { return new ServerServiceDefinition(); }
          }
        }
      """.trimIndent()
        )
        addJavaFile(
            "io/grpc/Status.java",
            """
        package io.grpc;

        public class Status {
          public static final Status UNIMPLEMENTED = new Status();
          public Status withDescription(String description) { return this; }
        }
      """.trimIndent()
        )
        addJavaFile(
            "io/grpc/StatusException.java",
            """
        package io.grpc;

        public class StatusException extends RuntimeException {
          public StatusException(Status status) {}
        }
      """.trimIndent()
        )
        addJavaFile(
            "io/grpc/kotlin/AbstractCoroutineServerImpl.java",
            """
        package io.grpc.kotlin;

        public abstract class AbstractCoroutineServerImpl implements io.grpc.BindableService {
          protected final Object context = new Object();
          protected AbstractCoroutineServerImpl(Object coroutineContext) {}
          public abstract io.grpc.ServerServiceDefinition bindService();
        }
      """.trimIndent()
        )
        addJavaFile(
            "io/grpc/kotlin/AbstractCoroutineStub.java",
            """
        package io.grpc.kotlin;

        public abstract class AbstractCoroutineStub<T extends AbstractCoroutineStub<T>> {
          protected final io.grpc.Channel channel;
          protected final io.grpc.CallOptions callOptions;
          protected AbstractCoroutineStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            this.channel = channel;
            this.callOptions = callOptions;
          }
          protected abstract T build(io.grpc.Channel channel, io.grpc.CallOptions callOptions);
        }
      """.trimIndent()
        )
        addJavaFile(
            "io/grpc/kotlin/StubFor.java",
            """
        package io.grpc.kotlin;

        public @interface StubFor {
          Class<?> value();
        }
      """.trimIndent()
        )
        addJavaFile(
            "kotlinx/coroutines/flow/Flow.java",
            "package kotlinx.coroutines.flow; public interface Flow<T> {}"
        )
    }

    private fun addProto2GroupGeneratedJavaStubs() {
        addJavaFile(
            "demo/groups/GroupContainerOrBuilder.java",
            """
        package demo.groups;

        public interface GroupContainerOrBuilder extends com.google.protobuf.MessageOrBuilder {
          boolean hasSingleGroupField();
          GroupContainer.SingleGroupField getSingleGroupField();
        }
      """.trimIndent()
        )

        addJavaFile(
            "demo/groups/GroupContainer.java",
            """
        package demo.groups;

        public final class GroupContainer extends com.google.protobuf.GeneratedMessageV3
            implements GroupContainerOrBuilder {
          public static Builder newBuilder() { return new Builder(); }
          public Builder toBuilder() { return new Builder(); }
          public boolean hasSingleGroupField() { return false; }
          public SingleGroupField getSingleGroupField() { return new SingleGroupField(); }

          public static final class Builder extends com.google.protobuf.GeneratedMessageV3
              implements GroupContainerOrBuilder {
            public GroupContainer build() { return new GroupContainer(); }
            public boolean hasSingleGroupField() { return false; }
            public SingleGroupField getSingleGroupField() { return new SingleGroupField(); }
            public Builder setSingleGroupField(SingleGroupField value) { return this; }
            public Builder clearSingleGroupField() { return this; }
          }

          public static final class SingleGroupField extends com.google.protobuf.GeneratedMessageV3 {
            public static Builder newBuilder() { return new Builder(); }
            public Builder toBuilder() { return new Builder(); }
            public boolean hasInGroup() { return false; }
            public int getInGroup() { return 0; }

            public static final class Builder extends com.google.protobuf.GeneratedMessageV3 {
              public SingleGroupField build() { return new SingleGroupField(); }
              public boolean hasInGroup() { return false; }
              public int getInGroup() { return 0; }
              public Builder setInGroup(int value) { return this; }
              public Builder clearInGroup() { return this; }
            }
          }
        }
      """.trimIndent()
        )
    }

    private fun addGrpcGeneratedJavaStubs() {
        addJavaFile(
            "demo/grpc/HelloRequest.java",
            "package demo.grpc; public class HelloRequest {}"
        )
        addJavaFile(
            "demo/grpc/HelloReply.java",
            "package demo.grpc; public class HelloReply {}"
        )
        addJavaFile(
            "demo/grpc/GreeterGrpc.java",
            """
        package demo.grpc;

        public final class GreeterGrpc {
          public static final String SERVICE_NAME = "grpcdemo.Greeter";
          public static io.grpc.ServiceDescriptor getServiceDescriptor() { return new io.grpc.ServiceDescriptor(); }
          public static io.grpc.MethodDescriptor<HelloRequest, HelloReply> getSayHelloMethod() { return new io.grpc.MethodDescriptor<>(); }
          public static io.grpc.MethodDescriptor<HelloRequest, HelloReply> getListRepliesMethod() { return new io.grpc.MethodDescriptor<>(); }
          public static io.grpc.MethodDescriptor<HelloRequest, HelloReply> getCollectRepliesMethod() { return new io.grpc.MethodDescriptor<>(); }
          public static io.grpc.MethodDescriptor<HelloRequest, HelloReply> getChatMethod() { return new io.grpc.MethodDescriptor<>(); }

          public abstract static class GreeterImplBase implements io.grpc.BindableService {}
        }
      """.trimIndent()
        )
    }

    private fun addGeneratedJavaStubs() {
        addJavaFile(
            "demo/proto/ExtendableUser.java",
            """
        package demo.proto;

        public final class ExtendableUser extends com.google.protobuf.GeneratedMessageV3 {
          public static Builder newBuilder() {
            return new Builder();
          }

          public static final class Builder extends com.google.protobuf.GeneratedMessageV3 {
            public ExtendableUser build() {
              return new ExtendableUser();
            }
          }
        }
      """
        )

        addJavaFile(
            "demo/proto/User.java",
            """
        package demo.proto;

        public final class User extends com.google.protobuf.GeneratedMessageV3 {
          public static final int NAME_FIELD_NUMBER = 2;
          public static final int ROLES_FIELD_NUMBER = 3;
          public static final int LABELS_FIELD_NUMBER = 6;
          public static final int WHEN_FIELD_NUMBER = 8;

          public static Builder newBuilder() {
            return new Builder();
          }

          public String getId() {
            return "";
          }

          public String getName() {
            return "";
          }

          public com.google.protobuf.ByteString getNameBytes() {
            return com.google.protobuf.ByteString.EMPTY;
          }

          public boolean hasName() {
            return true;
          }

          public String getWhen() {
            return "";
          }

          public boolean hasWhen() {
            return true;
          }

          public Address getAddress() {
            return Address.getDefaultInstance();
          }

          public AddressOrBuilder getAddressOrBuilder() {
            return null;
          }

          public java.util.List<? extends AddressOrBuilder> getPreviousAddressesOrBuilderList() {
            return java.util.Collections.emptyList();
          }

          public AddressOrBuilder getPreviousAddressesOrBuilder(int index) {
            return null;
          }

          public int getRolesCount() {
            return 0;
          }

          public java.util.List<Role> getRolesList() {
            return java.util.Collections.emptyList();
          }

          public Role getRoles(int index) {
            return Role.ROLE_UNSPECIFIED;
          }

          public boolean hasAddress() {
            return true;
          }

          public TestOneofCase getTestOneofCase() {
            return TestOneofCase.TESTONEOF_NOT_SET;
          }

          public int getLabelsCount() {
            return 0;
          }

          public java.util.Map<String, Integer> getLabelsMap() {
            return java.util.Collections.emptyMap();
          }

          public boolean containsLabels(String key) {
            return false;
          }

          public int getLabelsOrDefault(String key, int defaultValue) {
            return defaultValue;
          }

          public int getLabelsOrThrow(String key) {
            return 0;
          }

          public static User getDefaultInstance() {
            return new User();
          }

          public enum TestOneofCase implements com.google.protobuf.ProtocolMessageEnum {
            STRING_CHOICE,
            TESTONEOF_NOT_SET;

            public static TestOneofCase forNumber(int value) {
              return TESTONEOF_NOT_SET;
            }
          }

          public static final class Builder extends com.google.protobuf.GeneratedMessageV3 {
            public Builder setId(String value) {
              return this;
            }

            public Builder setName(String value) {
              return this;
            }

            public com.google.protobuf.ByteString getNameBytes() {
              return com.google.protobuf.ByteString.EMPTY;
            }

            public Builder setNameBytes(com.google.protobuf.ByteString value) {
              return this;
            }

            public boolean hasName() {
              return true;
            }

            public Builder clearName() {
              return this;
            }

            public Builder setWhen(String value) {
              return this;
            }

            public Builder setLine2Value(String value) {
              return this;
            }

            public boolean hasWhen() {
              return true;
            }

            public Builder clearWhen() {
              return this;
            }

            public Builder addRoles(Role value) {
              return this;
            }

            public Builder addAllRoles(java.lang.Iterable<? extends Role> values) {
              return this;
            }

            public Builder setRoles(int index, Role value) {
              return this;
            }

            public int getRolesCount() {
              return 0;
            }

            public java.util.List<Role> getRolesList() {
              return java.util.Collections.emptyList();
            }

            public Role getRoles(int index) {
              return Role.ROLE_UNSPECIFIED;
            }

            public Builder clearRoles() {
              return this;
            }

            public Builder setAddress(Address value) {
              return this;
            }

            public AddressOrBuilder getAddressOrBuilder() {
              return null;
            }

            public java.util.List<? extends AddressOrBuilder> getPreviousAddressesOrBuilderList() {
              return java.util.Collections.emptyList();
            }

            public AddressOrBuilder getPreviousAddressesOrBuilder(int index) {
              return null;
            }

            public boolean hasAddress() {
              return true;
            }

            public Builder clearAddress() {
              return this;
            }

            public Builder setStringChoice(String value) {
              return this;
            }

            public boolean hasStringChoice() {
              return true;
            }

            public Builder setStringChoiceBytes(com.google.protobuf.ByteString value) {
              return this;
            }

            public TestOneofCase getTestOneofCase() {
              return TestOneofCase.TESTONEOF_NOT_SET;
            }

            public Builder clearTestOneof() {
              return this;
            }

            public Builder putLabels(String key, int value) {
              return this;
            }

            public Builder putAllLabels(java.util.Map<String, Integer> values) {
              return this;
            }

            public Builder removeLabels(String key) {
              return this;
            }

            public User build() {
              return new User();
            }
          }

          public static final class Profile extends com.google.protobuf.GeneratedMessageV3 {
            public static Builder newBuilder() {
              return new Builder();
            }

            public static Profile getDefaultInstance() {
              return new Profile();
            }

            public String getDisplayName() {
              return "";
            }

            public enum Visibility implements com.google.protobuf.ProtocolMessageEnum {
              VISIBILITY_UNSPECIFIED,
              PUBLIC
            }

            public static final class Builder extends com.google.protobuf.GeneratedMessageV3 {
              public Builder setDisplayName(String value) {
                return this;
              }

              public Profile build() {
                return new Profile();
              }
            }
          }

          public interface ProfileOrBuilder extends com.google.protobuf.MessageOrBuilder {
            String getDisplayName();
          }

          public static final class When extends com.google.protobuf.GeneratedMessageV3 {
            public static Builder newBuilder() {
              return new Builder();
            }

            public static final class Builder extends com.google.protobuf.GeneratedMessageV3 {
              public Builder setValue(String value) {
                return this;
              }

              public When build() {
                return new When();
              }
            }
          }
        }
      """
        )

        addJavaFile(
            "demo/proto/UserOrBuilder.java",
            """
        package demo.proto;

        public interface UserOrBuilder extends com.google.protobuf.MessageOrBuilder {
          String getName();
          com.google.protobuf.ByteString getNameBytes();
          boolean hasName();
          Address getAddress();
          AddressOrBuilder getAddressOrBuilder();
          java.util.List<? extends AddressOrBuilder> getPreviousAddressesOrBuilderList();
          AddressOrBuilder getPreviousAddressesOrBuilder(int index);
        }
      """
        )

        addJavaFile(
            "demo/proto/Address.java",
            """
        package demo.proto;

        public final class Address extends com.google.protobuf.GeneratedMessageV3 {
          public static Builder newBuilder() {
            return new Builder();
          }

          public static Address getDefaultInstance() {
            return new Address();
          }

          public String getCity() {
            return "";
          }

          public String getStreet() {
            return "";
          }

          public static final class Builder extends com.google.protobuf.GeneratedMessageV3 {
            public Builder setCity(String value) {
              return this;
            }

            public Builder setStreet(String value) {
              return this;
            }

            public Address build() {
              return new Address();
            }
          }
        }
      """
        )

        addJavaFile(
            "demo/proto/AddressOrBuilder.java",
            """
        package demo.proto;

        public interface AddressOrBuilder extends com.google.protobuf.MessageOrBuilder {
          String getCity();
          String getStreet();
        }
      """
        )

        addJavaFile(
            "demo/proto/Role.java",
            """
        package demo.proto;

        public enum Role implements com.google.protobuf.ProtocolMessageEnum {
          ROLE_UNSPECIFIED,
          ADMIN,
          PLAYER;

          public static Role forNumber(int value) {
            return ROLE_UNSPECIFIED;
          }
        }
      """
        )

        addJavaFile(
            "demo/proto/CreateUserRequest.java",
            """
        package demo.proto;

        public final class CreateUserRequest extends com.google.protobuf.GeneratedMessageV3 {
          public static Builder newBuilder() {
            return new Builder();
          }

          public User getUser() {
            return User.getDefaultInstance();
          }

          public static final class Builder extends com.google.protobuf.GeneratedMessageV3 {
            public Builder setUser(User value) {
              return this;
            }

            public CreateUserRequest build() {
              return new CreateUserRequest();
            }
          }
        }
      """
        )

        addJavaFile(
            "demo/proto/Legacy_message.java",
            """
        package demo.proto;

        public final class Legacy_message extends com.google.protobuf.GeneratedMessageV3 {
          public static Builder newBuilder() {
            return new Builder();
          }

          public static final class Builder extends com.google.protobuf.GeneratedMessageV3 {
            public Legacy_message build() {
              return new Legacy_message();
            }
          }
        }
      """
        )

        addJavaFile(
            "demo/proto/When.java",
            """
        package demo.proto;

        public final class When extends com.google.protobuf.GeneratedMessageV3 {
          public static Builder newBuilder() {
            return new Builder();
          }

          public static final class Builder extends com.google.protobuf.GeneratedMessageV3 {
            public When build() {
              return new When();
            }
          }
        }
      """
        )

        addJavaFile(
            "demo/proto/HTTP2_response.java",
            """
        package demo.proto;

        public final class HTTP2_response extends com.google.protobuf.GeneratedMessageV3 {
          public static Builder newBuilder() {
            return new Builder();
          }

          public static final class Builder extends com.google.protobuf.GeneratedMessageV3 {
            public HTTP2_response build() {
              return new HTTP2_response();
            }
          }
        }
      """
        )
    }

    private fun addGeneratedOuterClassJavaStubs() {
        addJavaFile(
            "demo/proto/OuterProto.java",
            """
        package demo.proto;

        public final class OuterProto {
          public static final class OuterUser extends com.google.protobuf.GeneratedMessageV3 {
            public static Builder newBuilder() {
              return new Builder();
            }

            public static OuterUser getDefaultInstance() {
              return new OuterUser();
            }

            public String getName() {
              return "";
            }

            public static final class Builder extends com.google.protobuf.GeneratedMessageV3 {
              public Builder setName(String value) {
                return this;
              }

              public OuterUser build() {
                return new OuterUser();
              }
            }
          }

          public enum OuterRole implements com.google.protobuf.ProtocolMessageEnum {
            OUTER_ROLE_UNSPECIFIED,
            OUTER_ADMIN
          }
        }
      """
        )
    }

    private fun addClashingNameGeneratedJavaStubs() {
        addJavaFile(
            "demo/clash/message/ClashingMessageOuterClass.java",
            """
        package demo.clash.message;

        public final class ClashingMessageOuterClass {
          public static final class ClashingMessage extends com.google.protobuf.GeneratedMessageV3 {
            public static Builder newBuilder() {
              return new Builder();
            }

            public static final class Builder extends com.google.protobuf.GeneratedMessageV3 {
              public Builder setFoo(int value) {
                return this;
              }

              public ClashingMessage build() {
                return new ClashingMessage();
              }
            }
          }
        }
      """
        )

        addJavaFile(
            "demo/clash/nested/ClashingNestedMessageOuterClass.java",
            """
        package demo.clash.nested;

        public final class ClashingNestedMessageOuterClass {
          public static final class Foo extends com.google.protobuf.GeneratedMessageV3 {
            public static final class Bar extends com.google.protobuf.GeneratedMessageV3 {
              public static final class ClashingNestedMessage extends com.google.protobuf.GeneratedMessageV3 {
                public static Builder newBuilder() {
                  return new Builder();
                }

                public static final class Builder extends com.google.protobuf.GeneratedMessageV3 {
                  public Builder setFoo(int value) {
                    return this;
                  }

                  public ClashingNestedMessage build() {
                    return new ClashingNestedMessage();
                  }
                }
              }
            }
          }
        }
      """
        )

        addJavaFile(
            "demo/clash/enumcase/ClashingEnumOuterClass.java",
            """
        package demo.clash.enumcase;

        public final class ClashingEnumOuterClass {
          public enum ClashingEnum implements com.google.protobuf.ProtocolMessageEnum {
            ZERO,
            ONE
          }
        }
      """
        )

        addJavaFile(
            "demo/clash/field/NotClashingField.java",
            """
        package demo.clash.field;

        public final class NotClashingField {
          public static final class Foo extends com.google.protobuf.GeneratedMessageV3 {
            public static Builder newBuilder() {
              return new Builder();
            }

            public int getNotClashingField() {
              return 0;
            }

            public static final class Builder extends com.google.protobuf.GeneratedMessageV3 {
              public Builder setNotClashingField(int value) {
                return this;
              }

              public Foo build() {
                return new Foo();
              }
            }
          }
        }
      """
        )
    }

    private fun addClashingNameGeneratedKotlinStubs() {
        myFixture.addFileToProject(
            "src/main/kotlin/demo/clash/message/ClashingMessageKt.kt",
            """
        package demo.clash.message

        object ClashingMessageKt {
          class Dsl {
            var foo: Int = 0
          }
        }

        fun clashingMessage(block: ClashingMessageKt.Dsl.() -> Unit): ClashingMessageOuterClass.ClashingMessage {
          val dsl = ClashingMessageKt.Dsl()
          dsl.block()
          return ClashingMessageOuterClass.ClashingMessage.newBuilder().build()
        }
      """.trimIndent()
        )

        myFixture.addFileToProject(
            "src/main/kotlin/demo/clash/nested/FooKt.kt",
            """
        package demo.clash.nested

        object FooKt {
          object BarKt {
            object ClashingNestedMessageKt {
              class Dsl {
                var foo: Int = 0
              }
            }

            fun clashingNestedMessage(
              block: FooKt.BarKt.ClashingNestedMessageKt.Dsl.() -> Unit
            ): ClashingNestedMessageOuterClass.Foo.Bar.ClashingNestedMessage {
              val dsl = FooKt.BarKt.ClashingNestedMessageKt.Dsl()
              dsl.block()
              return ClashingNestedMessageOuterClass.Foo.Bar.ClashingNestedMessage.newBuilder().build()
            }
          }
        }
      """.trimIndent()
        )

        myFixture.addFileToProject(
            "src/main/kotlin/demo/clash/field/FooKt.kt",
            """
        package demo.clash.field

        object FooKt {
          class Dsl {
            var notClashingField: Int = 0
          }
        }

        fun foo(block: FooKt.Dsl.() -> Unit): NotClashingField.Foo {
          val dsl = FooKt.Dsl()
          dsl.block()
          return NotClashingField.Foo.newBuilder().build()
        }
      """.trimIndent()
        )
    }

    private fun addKotlinDslRuntimeStubs() {
        myFixture.addFileToProject(
            "src/main/kotlin/com/google/protobuf/kotlin/DslCollections.kt",
            """
        package com.google.protobuf.kotlin

        @RequiresOptIn
        annotation class OnlyForUseByGeneratedProtoCode

        @DslMarker
        annotation class ProtoDslMarker

        open class DslProxy

        class DslList<E, P>(values: List<E> = emptyList())

        class DslMap<K, V, P>(values: Map<K, V> = emptyMap())

        class ExtensionList<E, M>(
          internal val extension: com.google.protobuf.ExtensionLite<M, List<E>>,
          values: List<E>,
        )
      """.trimIndent()
        )
    }

    private fun addGeneratedKotlinStubs(includeSourceComments: Boolean = false) {
        addKotlinDslRuntimeStubs()

        myFixture.addFileToProject(
            "src/main/kotlin/demo/proto/ExtendableUserKt.kt",
            """
        ${sourceComment("demo/extensions.proto", includeSourceComments)}
        package demo.proto

        object ExtendableUserKt {
          class Dsl {
            operator fun <T> get(extension: com.google.protobuf.ExtensionLite<ExtendableUser, T>): T = TODO()
            @JvmName("-getRepeatedExtension")
            operator fun <E> get(
              extension: com.google.protobuf.ExtensionLite<ExtendableUser, List<E>>
            ): com.google.protobuf.kotlin.ExtensionList<E, ExtendableUser> = TODO()
            operator fun contains(extension: com.google.protobuf.ExtensionLite<ExtendableUser, *>): Boolean = false
            fun clear(extension: com.google.protobuf.ExtensionLite<ExtendableUser, *>) {}
            fun <T> setExtension(extension: com.google.protobuf.ExtensionLite<ExtendableUser, T>, value: T) {}
            operator fun <T> set(extension: com.google.protobuf.ExtensionLite<ExtendableUser, T>, value: T) {}
            fun <E> com.google.protobuf.kotlin.ExtensionList<E, ExtendableUser>.add(value: E) {}
            operator fun <E> com.google.protobuf.kotlin.ExtensionList<E, ExtendableUser>.plusAssign(value: E) {}
            operator fun <E> com.google.protobuf.kotlin.ExtensionList<E, ExtendableUser>.plusAssign(values: Iterable<E>) {}
            fun <E> com.google.protobuf.kotlin.ExtensionList<E, ExtendableUser>.addAll(values: Iterable<E>) {}
            operator fun <E> com.google.protobuf.kotlin.ExtensionList<E, ExtendableUser>.set(index: Int, value: E) {}
            fun com.google.protobuf.kotlin.ExtensionList<*, ExtendableUser>.clear() {}
          }
        }

        fun extendableUser(block: ExtendableUserKt.Dsl.() -> Unit): ExtendableUser {
          val dsl = ExtendableUserKt.Dsl()
          dsl.block()
          return ExtendableUser.newBuilder().build()
        }
      """.trimIndent()
        )

        myFixture.addFileToProject(
            "src/main/kotlin/demo/proto/UserKt.kt",
            """
        ${sourceComment("demo/user.proto", includeSourceComments)}
        package demo.proto

        object UserKt {
          class Dsl {
            var id: String = ""
            var name: String = ""
            fun hasName(): Boolean = false
            fun clearName() {}
            var when_: String = ""
            fun hasWhen_(): Boolean = false
            fun clearWhen_() {}
            var line2Value: String = ""
            class RolesProxy
            val roles: com.google.protobuf.kotlin.DslList<Role, RolesProxy> = com.google.protobuf.kotlin.DslList()
            fun com.google.protobuf.kotlin.DslList<Role, RolesProxy>.add(value: Role) {}
            operator fun com.google.protobuf.kotlin.DslList<Role, RolesProxy>.plusAssign(value: Role) {}
            operator fun com.google.protobuf.kotlin.DslList<Role, RolesProxy>.plusAssign(values: Iterable<Role>) {}
            fun com.google.protobuf.kotlin.DslList<Role, RolesProxy>.addAll(values: Iterable<Role>) {}
            operator fun com.google.protobuf.kotlin.DslList<Role, RolesProxy>.set(index: Int, value: Role) {}
            fun com.google.protobuf.kotlin.DslList<Role, RolesProxy>.clear() {}
            var address: Address = Address.getDefaultInstance()
            var stringChoice: String = ""
            val testOneofCase: User.TestOneofCase = User.TestOneofCase.TESTONEOF_NOT_SET
            fun clearTestOneof() {}
            class LabelsProxy
            val labels: com.google.protobuf.kotlin.DslMap<String, Int, LabelsProxy> = com.google.protobuf.kotlin.DslMap()
            fun com.google.protobuf.kotlin.DslMap<String, Int, LabelsProxy>.put(key: String, value: Int) {}
            operator fun com.google.protobuf.kotlin.DslMap<String, Int, LabelsProxy>.set(key: String, value: Int) {}
            fun com.google.protobuf.kotlin.DslMap<String, Int, LabelsProxy>.remove(key: String) {}
            fun com.google.protobuf.kotlin.DslMap<String, Int, LabelsProxy>.putAll(values: Map<String, Int>) {}
            fun com.google.protobuf.kotlin.DslMap<String, Int, LabelsProxy>.clear() {}
          }

          object ProfileKt {
            class Dsl {
              var displayName: String = ""
            }
          }

          fun profile(block: UserKt.ProfileKt.Dsl.() -> Unit): User.Profile {
            val dsl = UserKt.ProfileKt.Dsl()
            dsl.block()
            return User.Profile.newBuilder().build()
          }

          object WhenKt {
            class Dsl {
              var value: String = ""
            }
          }

          fun when_(block: UserKt.WhenKt.Dsl.() -> Unit): User.When {
            val dsl = UserKt.WhenKt.Dsl()
            dsl.block()
            return User.When.newBuilder().build()
          }
        }

        fun user(block: UserKt.Dsl.() -> Unit): User {
          val dsl = UserKt.Dsl()
          dsl.block()
          return User.newBuilder().build()
        }

        fun User.copy(block: UserKt.Dsl.() -> Unit): User {
          val dsl = UserKt.Dsl()
          dsl.block()
          return User.newBuilder().build()
        }
      """.trimIndent()
        )

        myFixture.addFileToProject(
            "src/main/kotlin/demo/proto/AddressKt.kt",
            """
        ${sourceComment("demo/user.proto", includeSourceComments)}
        package demo.proto

        object AddressKt {
          class Dsl {
            var city: String = ""
            var street: String = ""
          }
        }

        fun address(block: AddressKt.Dsl.() -> Unit): Address {
          val dsl = AddressKt.Dsl()
          dsl.block()
          return Address.newBuilder().build()
        }
      """.trimIndent()
        )

        myFixture.addFileToProject(
            "src/main/kotlin/demo/proto/CreateUserRequestKt.kt",
            """
        ${sourceComment("demo/user.proto", includeSourceComments)}
        package demo.proto

        object CreateUserRequestKt {
          class Dsl {
            var user: User = User.getDefaultInstance()
          }
        }

        fun createUserRequest(block: CreateUserRequestKt.Dsl.() -> Unit): CreateUserRequest {
          val dsl = CreateUserRequestKt.Dsl()
          dsl.block()
          return CreateUserRequest.newBuilder().build()
        }
      """.trimIndent()
        )

        myFixture.addFileToProject(
            "src/main/kotlin/demo/proto/Legacy_messageKt.kt",
            """
        ${sourceComment("demo/user.proto", includeSourceComments)}
        package demo.proto

        object Legacy_messageKt {
          class Dsl
        }

        fun legacyMessage(block: Legacy_messageKt.Dsl.() -> Unit): Legacy_message {
          val dsl = Legacy_messageKt.Dsl()
          dsl.block()
          return Legacy_message.newBuilder().build()
        }
      """.trimIndent()
        )

        myFixture.addFileToProject(
            "src/main/kotlin/demo/proto/WhenKt.kt",
            """
        ${sourceComment("demo/user.proto", includeSourceComments)}
        package demo.proto

        object WhenKt {
          class Dsl
        }

        fun when_(block: WhenKt.Dsl.() -> Unit): When {
          val dsl = WhenKt.Dsl()
          dsl.block()
          return When.newBuilder().build()
        }
      """.trimIndent()
        )

        myFixture.addFileToProject(
            "src/main/kotlin/demo/proto/HTTP2_responseKt.kt",
            """
        ${sourceComment("demo/user.proto", includeSourceComments)}
        package demo.proto

        object HTTP2_responseKt {
          class Dsl
        }

        fun hTTP2Response(block: HTTP2_responseKt.Dsl.() -> Unit): HTTP2_response {
          val dsl = HTTP2_responseKt.Dsl()
          dsl.block()
          return HTTP2_response.newBuilder().build()
        }
      """.trimIndent()
        )

        myFixture.addFileToProject(
            "src/main/kotlin/demo/proto/OuterUserKt.kt",
            """
        ${sourceComment("demo/outer.proto", includeSourceComments)}
        package demo.proto

        object OuterUserKt {
          class Dsl {
            var name: String = ""
          }
        }

        fun outerUser(block: OuterUserKt.Dsl.() -> Unit): OuterProto.OuterUser {
          val dsl = OuterUserKt.Dsl()
          dsl.block()
          return OuterProto.OuterUser.newBuilder().build()
        }
      """.trimIndent()
        )
    }

    private fun addClashingNameProtos() {
        myFixture.addFileToProject(
            "src/main/proto/clash/message/clashing_message.proto",
            """
        syntax = "proto3";

        package clash.message;

        option java_package = "demo.clash.message";

        message NothingHere {}

        message ClashingMessage {
          int32 foo = 1;
        }
      """.trimIndent()
        )

        myFixture.addFileToProject(
            "src/main/proto/clash/nested/clashing_nested_message.proto",
            """
        syntax = "proto3";

        package clash.nested;

        option java_package = "demo.clash.nested";

        message NothingHere {}

        message Foo {
          message Bar {
            message ClashingNestedMessage {
              int32 foo = 1;
            }
          }
        }
      """.trimIndent()
        )

        myFixture.addFileToProject(
            "src/main/proto/clash/enumcase/clashing_enum.proto",
            """
        syntax = "proto3";

        package clash.enumcase;

        option java_package = "demo.clash.enumcase";

        message NothingHere {}

        enum ClashingEnum {
          ZERO = 0;
          ONE = 1;
        }
      """.trimIndent()
        )

        myFixture.addFileToProject(
            "src/main/proto/clash/field/not_clashing_field.proto",
            """
        syntax = "proto3";

        package clash.field;

        option java_package = "demo.clash.field";

        message NothingHere {}

        message Foo {
          int32 NotClashingField = 1;
        }
      """.trimIndent()
        )
    }

    private fun sourceComment(source: String, include: Boolean): String {
        if (!include) {
            return ""
        }

        return """
        // Generated by the protocol buffer compiler.  DO NOT EDIT!
        // source: $source
        """.trimIndent()
    }

    private fun addJavaFile(path: String, text: String) {
        myFixture.addFileToProject(
            "src/main/java/$path",
            text.trimIndent()
        )
    }
}
