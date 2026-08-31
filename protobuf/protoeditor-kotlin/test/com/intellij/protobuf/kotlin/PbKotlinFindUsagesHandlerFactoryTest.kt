package com.intellij.protobuf.kotlin

import com.google.common.truth.Truth.assertWithMessage
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbSymbol
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.QualifiedName
import com.intellij.usageView.UsageInfo

class PbKotlinFindUsagesHandlerFactoryTest : PbKotlinTestBase() {
    fun testFindUsagesIncludesKotlinJavaApiAndDslUsages() {
        addProjectFiles()

        myFixture.configureByText(
            "Main.kt",
            """
        package demo

        import demo.proto.ExtendableUser
        import demo.proto.Role
        import demo.proto.OuterProto
        import demo.proto.User
        import demo.proto.UserKt
        import demo.proto.copy
        import demo.proto.hTTP2Response
        import demo.proto.legacyMessage
        import demo.proto.outerUser
        import demo.proto.user
        import demo.proto.extendableUser
        import demo.proto.when_
        import com.google.protobuf.ExtensionLite

        fun main() {
          val javaApiUser = User.newBuilder()
            .setName("Ada")
            .setWhen("soon")
            .setLine2Value("two")
            .addRoles(Role.ADMIN)
            .setStringChoice("notes")
            .putLabels("tier", 1)
            .build()

          val javaApiProfile = User.Profile.getDefaultInstance()
          val publicVisibility = User.Profile.Visibility.PUBLIC
          val javaApiOneofCase = User.getDefaultInstance().getTestOneofCase()
          val javaApiOneofType: User.TestOneofCase = User.TestOneofCase.TESTONEOF_NOT_SET
          val javaApiOneofValue = User.TestOneofCase.STRING_CHOICE
          val javaApiClearedOneof = User.newBuilder().clearTestOneof().build()
          val legacy = legacyMessage {}
          val keywordMessage = when_ {}
          val nestedKeywordMessage = UserKt.when_ {}
          val unusualName = hTTP2Response {}

          val copiedUser = javaApiUser.copy {
            name = "Katherine"
            when_ = "later"
            line2Value = "second"
          }

          val dslUser = user {
            name = "Grace"
            if (hasName()) {
              clearName()
            }
            if (hasWhen_()) {
              clearWhen_()
            }
            line2Value = "third"
            roles += Role.PLAYER
            stringChoice = "notes"
            println(testOneofCase)
            clearTestOneof()
            labels["tier"] = 2
          }

          val javaApiOuterUser = OuterProto.OuterUser.newBuilder()
            .setName("Lin")
            .build()

          val dslOuterUser = outerUser {
            name = "Lin"
          }

          val extension = ExtensionLite<ExtendableUser, String>()
          val extendedUser = extendableUser {
            setExtension(extension, "Ada")
            clear(extension)
          }
        }
      """.trimIndent()
        )

        val protoFile = findProjectFile("src/main/proto/demo/user.proto") as PbFile
        val outerProtoFile = findProjectFile("src/main/proto/demo/outer.proto") as PbFile

        assertUsagesContain(
            findUsagesForSymbol(protoFile, "User"),
            "Main.kt" to "User.newBuilder",
            "Main.kt" to "user {",
            "Main.kt" to "copy {"
        )

        assertUsagesContain(
            findUsagesForSymbol(protoFile, "Legacy_message"),
            "Main.kt" to "legacyMessage {"
        )

        assertUsagesContain(
            findUsagesForSymbol(protoFile, "When"),
            "Main.kt" to "when_ {"
        )

        assertUsagesContain(
            findUsagesForSymbol(protoFile, "User.When"),
            "Main.kt" to "when_ {"
        )

        assertUsagesContain(
            findUsagesForSymbol(protoFile, "HTTP2_response"),
            "Main.kt" to "hTTP2Response {"
        )

        assertUsagesContain(
            findUsagesForSymbol(protoFile, "User.name"),
            "Main.kt" to "setName",
            "Main.kt" to "name =",
            "Main.kt" to "hasName()",
            "Main.kt" to "clearName()"
        )

        assertUsagesContain(
            findUsagesForSymbol(protoFile, "User.when"),
            "Main.kt" to "setWhen",
            "Main.kt" to "when_ =",
            "Main.kt" to "hasWhen_()",
            "Main.kt" to "clearWhen_()"
        )

        assertUsagesContain(
            findUsagesForSymbol(protoFile, "User.line2value"),
            "Main.kt" to "setLine2Value",
            "Main.kt" to "line2Value ="
        )

        assertUsagesContain(
            findUsagesForSymbol(protoFile, "User.roles"),
            "Main.kt" to "addRoles",
            "Main.kt" to "roles +="
        )

        assertUsagesContain(
            findUsagesForSymbol(protoFile, "User.Profile"),
            "Main.kt" to "Profile.getDefaultInstance"
        )

        assertUsagesContain(
            findUsagesForSymbol(protoFile, "ADMIN"),
            "Main.kt" to "ADMIN)"
        )

        assertUsagesContain(
            findUsagesForSymbol(protoFile, "PLAYER"),
            "Main.kt" to "PLAYER"
        )

        assertUsagesContain(
            findUsagesForSymbol(protoFile, "User.Profile.PUBLIC"),
            "Main.kt" to "PUBLIC"
        )

        assertUsagesContain(
            findUsagesForSymbol(protoFile, "User.Profile.Visibility"),
            "Main.kt" to "Visibility.PUBLIC"
        )

        assertUsagesContain(
            findUsagesForSymbol(protoFile, "User.string_choice"),
            "Main.kt" to "setStringChoice",
            "Main.kt" to "STRING_CHOICE",
            "Main.kt" to "stringChoice ="
        )

        assertUsagesContain(
            findUsagesForSymbol(protoFile, "User.test_oneof"),
            "Main.kt" to "getTestOneofCase",
            "Main.kt" to "TestOneofCase",
            "Main.kt" to "TESTONEOF_NOT_SET",
            "Main.kt" to "clearTestOneof",
            "Main.kt" to "testOneofCase"
        )

        assertUsagesContain(
            findUsagesForSymbol(protoFile, "User.labels"),
            "Main.kt" to "putLabels",
            "Main.kt" to "labels["
        )

        assertUsagesContain(
            findUsagesForSymbol(outerProtoFile, "OuterUser"),
            "Main.kt" to "OuterUser.newBuilder",
            "Main.kt" to "outerUser {"
        )

        assertUsagesContain(
            findUsagesForSymbol(outerProtoFile, "OuterUser.name"),
            "Main.kt" to "setName",
            "Main.kt" to "name ="
        )

        val extensionsProtoFile = findProjectFile("src/main/proto/demo/extensions.proto") as PbFile
        assertUsagesContain(
            findUsagesForSymbol(extensionsProtoFile, "ExtendableUser"),
            "Main.kt" to "extendableUser {",
            "Main.kt" to "setExtension(extension",
            "Main.kt" to "clear(extension)"
        )
    }

    fun testFindUsagesIncludesRealisticOrNullProperties() {
        addRealisticGeneratedKotlinProjectFiles()

        myFixture.configureByText(
            "Main.kt",
            """
        package demo

        import demo.realistic.*

        fun useNullableProfiles(existing: RealisticUser) {
          println(existing.profileOrNull)
          realisticUser {
            println(profileOrNull)
          }
        }
      """.trimIndent()
        )

        val protoFile = findProjectFile("src/main/proto/realistic_api.proto") as PbFile
        val usageDescriptions = findUsagesForSymbol(protoFile, "RealisticUser.profile")
            .mapNotNull(::describeUsage)
        val orNullUsageCount = usageDescriptions.count { usage ->
            usage.fileName == "Main.kt" && usage.context.startsWith("profileOrNull")
        }

        assertWithMessage("Expected both generated profileOrNull properties to contribute usages")
            .that(orNullUsageCount)
            .isAtLeast(2)
    }

    fun testFindUsagesIncludesLiteRuntimeJavaApiAndKotlinDsl() {
        addLiteRuntimeGeneratedKotlinProjectFiles()

        myFixture.configureByText(
            "Main.kt",
            """
        package demo

        import demo.lite.*

        fun useLiteRuntime(existing: LiteUser) {
          LiteUser.newBuilder()
            .setName("Ada")
            .addAliases("Ada")
            .putLabels("score", 1)
            .setProfile(LiteUser.Profile.newBuilder().setDisplayName("Ada").build())
            .clearContact()
            .build()

          liteUser {
            name = "Grace"
            aliases.add("Grace")
            labels["score"] = 2
            println(contactCase)
            clearContact()
            profile = profile {
              displayName = "Grace"
            }
          }

          println(existing.profileOrNull)
        }
      """.trimIndent()
        )

        val protoFile = findProjectFile("src/main/proto/lite_runtime.proto") as PbFile
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "LiteUser"),
            "Main.kt" to "LiteUser.newBuilder",
            "Main.kt" to "liteUser {"
        )
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "LiteUser.name"),
            "Main.kt" to "setName",
            "Main.kt" to "name ="
        )
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "LiteUser.aliases"),
            "Main.kt" to "addAliases",
            "Main.kt" to "aliases.add"
        )
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "LiteUser.labels"),
            "Main.kt" to "putLabels",
            "Main.kt" to "labels["
        )
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "LiteUser.contact"),
            "Main.kt" to "clearContact",
            "Main.kt" to "contactCase"
        )
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "LiteUser.Profile"),
            "Main.kt" to "Profile.newBuilder"
        )
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "LiteUser.Profile.display_name"),
            "Main.kt" to "setDisplayName"
        )
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "LiteUser.profile"),
            "Main.kt" to "setProfile",
            "Main.kt" to "profile =",
            "Main.kt" to "profileOrNull"
        )
    }

    fun testFindUsagesIncludesKotlinSyntheticPropertyAccessToJavaGetter() {
        addLiteRuntimeGeneratedKotlinProjectFiles()

        myFixture.configureByText(
            "Main.kt",
            """
        package demo

        import demo.lite.LiteUser

        fun useGeneratedJavaProperty(existing: LiteUser) {
          val name = existing.name
        }
      """.trimIndent()
        )

        val protoFile = findProjectFile("src/main/proto/lite_runtime.proto") as PbFile
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "LiteUser.name"),
            "Main.kt" to "name"
        )
    }

    fun testFindUsagesIncludesGrpcKotlinServiceAndMethodCalls() {
        addGrpcKotlinProjectFiles()
        addGrpcKotlinServiceImplementation()

        myFixture.configureByText(
            "Main.kt",
            """
        package demo

        import demo.grpc.GreeterService
        import demo.grpc.GreeterGrpcKt
        import demo.grpc.HelloRequest
        import io.grpc.Channel
        import kotlinx.coroutines.flow.Flow

        suspend fun invokeGrpcService(service: GreeterService, request: HelloRequest) {
          val anotherService = GreeterService()
          service.sayHello(request)
        }

        fun createGrpcClient(channel: Channel): GreeterGrpcKt.GreeterCoroutineStub =
          GreeterGrpcKt.GreeterCoroutineStub(channel)

        suspend fun invokeGrpcClient(
          client: GreeterGrpcKt.GreeterCoroutineStub,
          request: HelloRequest,
          requests: Flow<HelloRequest>,
        ) {
          client.sayHello(request)
          client.listReplies(request)
          client.collectReplies(requests)
          client.chat(requests)
        }
      """.trimIndent()
        )

        val protoFile = findProjectFile("src/main/proto/grpc_service.proto") as PbFile
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "Greeter"),
            "Main.kt" to "GreeterService",
            "Main.kt" to "GreeterCoroutineStub"
        )
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "Greeter.SayHello"),
            "Main.kt" to "sayHello"
        )
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "Greeter.ListReplies"),
            "Main.kt" to "listReplies"
        )
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "Greeter.CollectReplies"),
            "Main.kt" to "collectReplies"
        )
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "Greeter.Chat"),
            "Main.kt" to "chat"
        )
    }

    fun testFindUsagesIncludesRealisticKotlinNamingCollisions() {
        addNamingCollisionGeneratedKotlinProjectFiles()

        myFixture.configureByText(
            "Main.kt",
            """
        package demo

        import demo.naming.namingCollisionMessage

        fun useNamingCollisions() {
          namingCollisionMessage {
            name = "Ada"
            hasName = "property"
            clearName = "property"
            println(hasName())
            clearName()
            aliases4 += "alias"
            aliases4.add("second")
            aliasesCount5 = 2
            aliasesList6 = "list"
            labels7["score"] = 1
            labelsCount8 = 1
          }
        }
      """.trimIndent()
        )

        val protoFile = findProjectFile("src/main/proto/naming_collisions.proto") as PbFile
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "NamingCollisionMessage.name"),
            "Main.kt" to "name =",
            "Main.kt" to "hasName()",
            "Main.kt" to "clearName()"
        )
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "NamingCollisionMessage.has_name"),
            "Main.kt" to "hasName ="
        )
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "NamingCollisionMessage.clear_name"),
            "Main.kt" to "clearName ="
        )
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "NamingCollisionMessage.aliases"),
            "Main.kt" to "aliases4 +=",
            "Main.kt" to "aliases4.add"
        )
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "NamingCollisionMessage.aliases_count"),
            "Main.kt" to "aliasesCount5 ="
        )
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "NamingCollisionMessage.aliases_list"),
            "Main.kt" to "aliasesList6 ="
        )
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "NamingCollisionMessage.labels"),
            "Main.kt" to "labels7["
        )
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "NamingCollisionMessage.labels_count"),
            "Main.kt" to "labelsCount8 ="
        )
    }

    fun testFindUsagesIncludesProto2GroupTypeFieldAndMembers() {
        addProto2GroupGeneratedKotlinProjectFiles()

        myFixture.configureByText(
            "Main.kt",
            """
        package demo

        import demo.groups.GroupContainer
        import demo.groups.GroupContainerKt
        import demo.groups.copy
        import demo.groups.groupContainer
        import demo.groups.singleGroupFieldOrNull

        fun useProto2Group(existing: GroupContainer.SingleGroupField) {
          val javaGroup = GroupContainer.SingleGroupField.newBuilder()
            .setInGroup(1)
            .build()
          GroupContainer.newBuilder()
            .setSingleGroupField(javaGroup)
            .build()

          val kotlinGroup = GroupContainerKt.singleGroupField {
            inGroup = 2
          }
          groupContainer {
            singleGroupField = kotlinGroup
            hasSingleGroupField()
            clearSingleGroupField()
          }

          existing.copy {
            inGroup = 3
            hasInGroup()
            clearInGroup()
          }
          println(GroupContainer.newBuilder().singleGroupFieldOrNull)
        }
      """.trimIndent()
        )

        val protoFile = findProjectFile("src/main/proto/proto2_groups.proto") as PbFile
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "GroupContainer.SingleGroupField"),
            "Main.kt" to "SingleGroupField",
            "Main.kt" to "singleGroupField {",
            "Main.kt" to "copy"
        )
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "GroupContainer.singlegroupfield"),
            "Main.kt" to "setSingleGroupField",
            "Main.kt" to "singleGroupField =",
            "Main.kt" to "hasSingleGroupField",
            "Main.kt" to "singleGroupFieldOrNull"
        )
        assertUsagesContain(
            findUsagesForSymbol(protoFile, "GroupContainer.SingleGroupField.in_group"),
            "Main.kt" to "setInGroup",
            "Main.kt" to "inGroup =",
            "Main.kt" to "hasInGroup"
        )
    }

    fun testFindUsagesIncludesClashingGeneratedNames() {
        addClashingNameProjectFiles()

        myFixture.configureByText(
            "Main.kt",
            """
        package demo

        import demo.clash.field.NotClashingField
        import demo.clash.field.foo as fieldFoo
        import demo.clash.message.ClashingMessageOuterClass
        import demo.clash.message.clashingMessage
        import demo.clash.nested.ClashingNestedMessageOuterClass
        import demo.clash.nested.FooKt

        fun main() {
          val javaMessage = ClashingMessageOuterClass.ClashingMessage.newBuilder()
            .setFoo(1)
            .build()
          val dslMessage = clashingMessage {
            foo = 2
          }

          val javaNestedMessage = ClashingNestedMessageOuterClass.Foo.Bar.ClashingNestedMessage.newBuilder()
            .setFoo(3)
            .build()
          val dslNestedMessage = FooKt.BarKt.clashingNestedMessage {
            foo = 4
          }

          val javaField = NotClashingField.Foo.newBuilder()
            .setNotClashingField(5)
            .build()
          val dslField = fieldFoo {
            notClashingField = 6
          }
        }
      """.trimIndent()
        )

        val messageProto = findProjectFile("src/main/proto/clash/message/clashing_message.proto") as PbFile
        val nestedProto = findProjectFile("src/main/proto/clash/nested/clashing_nested_message.proto") as PbFile
        val fieldProto = findProjectFile("src/main/proto/clash/field/not_clashing_field.proto") as PbFile

        assertUsagesContain(
            findUsagesForSymbol(messageProto, "ClashingMessage"),
            "Main.kt" to "ClashingMessage.newBuilder",
            "Main.kt" to "clashingMessage {"
        )
        assertUsagesContain(
            findUsagesForSymbol(messageProto, "ClashingMessage.foo"),
            "Main.kt" to "setFoo",
            "Main.kt" to "foo ="
        )
        assertUsagesContain(
            findUsagesForSymbol(nestedProto, "Foo.Bar.ClashingNestedMessage"),
            "Main.kt" to "ClashingNestedMessage.newBuilder",
            "Main.kt" to "clashingNestedMessage {"
        )
        assertUsagesContain(
            findUsagesForSymbol(nestedProto, "Foo.Bar.ClashingNestedMessage.foo"),
            "Main.kt" to "setFoo",
            "Main.kt" to "foo ="
        )
        assertUsagesContain(
            findUsagesForSymbol(fieldProto, "Foo.NotClashingField"),
            "Main.kt" to "setNotClashingField",
            "Main.kt" to "notClashingField ="
        )
    }

    private fun findProjectFile(path: String): PsiFile {
        val virtualFile = myFixture.findFileInTempDir(path)
            ?: error("No project file found at $path")
        return PsiManager.getInstance(project).findFile(virtualFile)
            ?: error("No PSI file found at $path")
    }

    private fun findUsagesForSymbol(protoFile: PbFile, localDottedName: String): List<UsageInfo> {
        val qualifiedName = protoFile.packageQualifiedName
            .append(QualifiedName.fromDottedString(localDottedName))
        val symbols = protoFile.localQualifiedSymbolMap[qualifiedName].orEmpty()

        assertWithMessage("Find symbol: $localDottedName")
            .that(symbols)
            .hasSize(1)

        return myFixture.findUsages(symbols.single() as PbSymbol).toList()
    }

    private fun assertUsagesContain(
        usages: List<UsageInfo>,
        vararg expectedFileAndContext: Pair<String, String>
    ) {
        val usageDescriptions = usages.mapNotNull(::describeUsage)

        for ((expectedFile, expectedContext) in expectedFileAndContext) {
            assertWithMessage(
                "Expected usage '$expectedContext' in $expectedFile.\n" +
                    "Actual usages:\n${usageDescriptions.joinToString(separator = "\n")}"
            )
                .that(usageDescriptions.any { usage ->
                    usage.fileName == expectedFile && usage.context.startsWith(expectedContext)
                })
                .isTrue()
        }
    }

    private fun describeUsage(usage: UsageInfo): UsageDescription? {
        val file = usage.file ?: return null
        val text = file.text
        val offset = usage.navigationOffset.takeIf { it in text.indices } ?: return null
        val contextEnd = minOf(text.length, offset + 40)
        return UsageDescription(
            fileName = file.name,
            context = text.substring(offset, contextEnd).lineSequence().firstOrNull().orEmpty()
        )
    }

    private data class UsageDescription(
        val fileName: String,
        val context: String
    )
}
