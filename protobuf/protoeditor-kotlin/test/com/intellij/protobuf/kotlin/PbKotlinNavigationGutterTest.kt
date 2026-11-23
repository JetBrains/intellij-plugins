package com.intellij.protobuf.kotlin

import com.google.common.truth.Truth.assertWithMessage
import com.intellij.protobuf.ProtoeditorCoreIcons
import com.intellij.protobuf.ide.gutter.findImplementations
import com.intellij.protobuf.ide.gutter.findProtoDefinitions
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbSymbol
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.QualifiedName
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

class PbKotlinNavigationGutterTest : PbKotlinTestBase() {
    fun testNavigationFromProtoMessageToKotlinDslFactory() {
        addProjectFiles()

        val protoFile = findProjectFile("src/main/proto/demo/user.proto") as PbFile
        assertMessageImplementation(protoFile, "User", "user", "UserKt.kt")
        assertMessageImplementation(protoFile, "User.Profile", "profile", "UserKt.kt")
        assertMessageImplementation(protoFile, "Address", "address", "AddressKt.kt")
        assertMessageImplementation(
            protoFile,
            "CreateUserRequest",
            "createUserRequest",
            "CreateUserRequestKt.kt"
        )
        assertMessageImplementation(
            protoFile,
            "Legacy_message",
            "legacyMessage",
            "Legacy_messageKt.kt"
        )
        assertMessageImplementation(protoFile, "When", "when_", "WhenKt.kt")
        assertMessageImplementation(protoFile, "HTTP2_response", "hTTP2Response", "HTTP2_responseKt.kt")

        val outerProtoFile = findProjectFile("src/main/proto/demo/outer.proto") as PbFile
        assertMessageImplementation(outerProtoFile, "OuterUser", "outerUser", "OuterUserKt.kt")

        val extensionsProtoFile = findProjectFile("src/main/proto/demo/extensions.proto") as PbFile
        assertMessageImplementation(
            extensionsProtoFile,
            "ExtendableUser",
            "extendableUser",
            "ExtendableUserKt.kt"
        )
    }

    fun testNavigationFromKotlinDslFactoryToProtoMessage() {
        addProjectFiles()

        val userFactory = findKotlinFunction("src/main/kotlin/demo/proto/UserKt.kt", "user")
        val definitions = findProtoDefinitions(userFactory).toList()

        assertProtoDefinitionsContain(definitions, "User")

        val legacyFactory = findKotlinFunction("src/main/kotlin/demo/proto/Legacy_messageKt.kt", "legacyMessage")
        assertProtoDefinitionsContain(findProtoDefinitions(legacyFactory).toList(), "Legacy_message")

        val keywordFactory = findKotlinFunction("src/main/kotlin/demo/proto/WhenKt.kt", "when_")
        assertProtoDefinitionsContain(findProtoDefinitions(keywordFactory).toList(), "When")

        val unusualFactory = findKotlinFunction("src/main/kotlin/demo/proto/HTTP2_responseKt.kt", "hTTP2Response")
        assertProtoDefinitionsContain(findProtoDefinitions(unusualFactory).toList(), "HTTP2_response")
    }

    fun testNavigationFromKotlinDslCopyToProtoMessage() {
        addProjectFiles()

        val copyFunction = findKotlinFunction("src/main/kotlin/demo/proto/UserKt.kt", "copy")
        val definitions = findProtoDefinitions(copyFunction).toList()

        assertProtoDefinitionsContain(definitions, "User")
    }

    fun testUnrelatedCopyFunctionIsNotTreatedAsGeneratedDsl() {
        addProjectFiles()
        myFixture.addFileToProject(
            "src/main/kotlin/demo/UserHelpers.kt",
            """
        package demo

        import demo.proto.User
        import demo.proto.UserKt

        fun User.copy(block: UserKt.Dsl.() -> Unit): User = this
      """.trimIndent()
        )

        val copyFunction = findKotlinFunction("src/main/kotlin/demo/UserHelpers.kt", "copy")

        assertWithMessage("Unrelated user copy function must not navigate to proto")
            .that(findProtoDefinitions(copyFunction).toList())
            .isEmpty()
    }

    fun testNavigationFromNestedKotlinDslFactoryToProtoMessage() {
        addProjectFiles()

        val profileFactory = findKotlinFunction("src/main/kotlin/demo/proto/UserKt.kt", "profile")
        val definitions = findProtoDefinitions(profileFactory).toList()

        assertProtoDefinitionsContain(definitions, "User.Profile")
    }

    fun testNavigationFromOuterClassKotlinDslFactoryToProtoMessage() {
        addProjectFiles()

        val outerUserFactory = findKotlinFunction("src/main/kotlin/demo/proto/OuterUserKt.kt", "outerUser")
        val definitions = findProtoDefinitions(outerUserFactory).toList()

        assertProtoDefinitionsContain(definitions, "OuterUser")
    }

    fun testNavigationFromKotlinDslPropertyToProtoField() {
        addProjectFiles()

        val nameProperty = findKotlinProperty("src/main/kotlin/demo/proto/UserKt.kt", "name")
        val definitions = findProtoDefinitions(nameProperty).toList()

        assertProtoDefinitionsContain(definitions, "User.name")

        val reservedProperty = findKotlinProperty("src/main/kotlin/demo/proto/UserKt.kt", "when_")
        assertProtoDefinitionsContain(findProtoDefinitions(reservedProperty).toList(), "User.when")

        val digitProperty = findKotlinProperty("src/main/kotlin/demo/proto/UserKt.kt", "line2Value")
        assertProtoDefinitionsContain(findProtoDefinitions(digitProperty).toList(), "User.line2value")
    }

    fun testGeneratedKotlinReverseMappingDoesNotReturnDuplicates() {
        addProjectFiles()

        val generatedElements = listOf(
            findKotlinFunction("src/main/kotlin/demo/proto/UserKt.kt", "user"),
            findKotlinProperty("src/main/kotlin/demo/proto/UserKt.kt", "name"),
            findKotlinFunction("src/main/kotlin/demo/proto/UserKt.kt", "hasName"),
            findKotlinFunction(
                "src/main/kotlin/demo/proto/UserKt.kt",
                "plusAssign",
                "RolesProxy",
                "Role"
            ),
            findKotlinProperty("src/main/kotlin/demo/proto/UserKt.kt", "testOneofCase")
        )

        for (generatedElement in generatedElements) {
            val targets = PbKotlinGeneratedCodeSearch
                .protoTargetsForGeneratedKotlinElement(generatedElement)
                .toList()

            assertWithMessage("Expected reverse-mapped targets for ${generatedElement.name} to be unique")
                .that(targets)
                .containsNoDuplicates()
        }
    }

    fun testNavigationFromProtoFieldWithDigitToKotlinDslProperty() {
        addProjectFiles()

        val protoFile = findProjectFile("src/main/proto/demo/user.proto") as PbFile
        val field = findProtoSymbol(protoFile, "User.line2value")
        val implementations = findImplementations(field).toList()

        assertWithMessage("Expected proto User.line2value to navigate to Kotlin DSL line2Value")
            .that(implementations.any { element ->
                element is KtProperty && element.name == "line2Value"
            })
            .isTrue()
    }

    fun testNavigationFromKotlinDslAccessorFunctionsToProtoField() {
        addProjectFiles()

        val hasNameFunction = findKotlinFunction("src/main/kotlin/demo/proto/UserKt.kt", "hasName")
        assertProtoDefinitionsContain(findProtoDefinitions(hasNameFunction).toList(), "User.name")

        val clearNameFunction = findKotlinFunction("src/main/kotlin/demo/proto/UserKt.kt", "clearName")
        assertProtoDefinitionsContain(findProtoDefinitions(clearNameFunction).toList(), "User.name")

        val hasReservedFunction = findKotlinFunction("src/main/kotlin/demo/proto/UserKt.kt", "hasWhen_")
        assertProtoDefinitionsContain(findProtoDefinitions(hasReservedFunction).toList(), "User.when")

        val clearReservedFunction = findKotlinFunction("src/main/kotlin/demo/proto/UserKt.kt", "clearWhen_")
        assertProtoDefinitionsContain(findProtoDefinitions(clearReservedFunction).toList(), "User.when")
    }

    fun testNavigationFromKotlinDslCollectionOperatorsToProtoField() {
        addProjectFiles()

        for (functionName in listOf("add", "plusAssign", "addAll", "set", "clear")) {
            val function = findKotlinFunction(
                "src/main/kotlin/demo/proto/UserKt.kt",
                functionName,
                "RolesProxy",
                valueParameterType = "Role".takeIf { functionName == "plusAssign" }
            )
            assertProtoDefinitionsContain(findProtoDefinitions(function).toList(), "User.roles")
        }

        val iterablePlusAssign = findKotlinFunction(
            "src/main/kotlin/demo/proto/UserKt.kt",
            "plusAssign",
            "RolesProxy",
            "Iterable<Role>"
        )
        assertProtoDefinitionsContain(findProtoDefinitions(iterablePlusAssign).toList(), "User.roles")

        for (functionName in listOf("put", "set", "remove", "putAll", "clear")) {
            val function = findKotlinFunction(
                "src/main/kotlin/demo/proto/UserKt.kt",
                functionName,
                "LabelsProxy"
            )
            assertProtoDefinitionsContain(findProtoDefinitions(function).toList(), "User.labels")
        }
    }

    fun testNavigationFromProtoFieldToKotlinDslCollectionOperators() {
        addProjectFiles()

        val protoFile = findProjectFile("src/main/proto/demo/user.proto") as PbFile
        assertFieldImplementation(protoFile, "User.roles", "plusAssign", "RolesProxy")
        assertFieldImplementation(protoFile, "User.labels", "set", "LabelsProxy")
    }

    fun testNavigationBetweenExtendableMessageAndKotlinDslExtensionHelpers() {
        addProjectFiles()

        val setExtension = findKotlinFunction("src/main/kotlin/demo/proto/ExtendableUserKt.kt", "setExtension")
        assertProtoDefinitionsContain(findProtoDefinitions(setExtension).toList(), "ExtendableUser")

        val plusAssign = findKotlinFunction(
            "src/main/kotlin/demo/proto/ExtendableUserKt.kt",
            "plusAssign",
            "ExtensionList",
            "E"
        )
        assertProtoDefinitionsContain(findProtoDefinitions(plusAssign).toList(), "ExtendableUser")

        val iterablePlusAssign = findKotlinFunction(
            "src/main/kotlin/demo/proto/ExtendableUserKt.kt",
            "plusAssign",
            "ExtensionList",
            "Iterable<E>"
        )
        assertProtoDefinitionsContain(findProtoDefinitions(iterablePlusAssign).toList(), "ExtendableUser")

        val protoFile = findProjectFile("src/main/proto/demo/extensions.proto") as PbFile
        val message = findProtoSymbol(protoFile, "ExtendableUser")
        val implementations = findImplementations(message).toList()

        assertWithMessage("Expected extendable proto message to navigate to its Kotlin DSL extension helpers")
            .that(
                implementations.any { element ->
                    element is KtNamedFunction && element.name == "setExtension"
                } && implementations.any { element ->
                    element is KtNamedFunction &&
                        element.name == "plusAssign" &&
                        element.receiverTypeReference?.text?.contains("ExtensionList") == true
                }
            )
            .isTrue()
    }

    fun testNavigationBetweenKotlinDslOneofHelpersAndProtoOneof() {
        addProjectFiles()

        val caseProperty = findKotlinProperty("src/main/kotlin/demo/proto/UserKt.kt", "testOneofCase")
        assertProtoDefinitionsContain(findProtoDefinitions(caseProperty).toList(), "User.test_oneof")

        val clearFunction = findKotlinFunction("src/main/kotlin/demo/proto/UserKt.kt", "clearTestOneof")
        assertProtoDefinitionsContain(findProtoDefinitions(clearFunction).toList(), "User.test_oneof")

        val protoFile = findProjectFile("src/main/proto/demo/user.proto") as PbFile
        val oneof = findProtoSymbol(protoFile, "User.test_oneof")
        val implementations = findImplementations(oneof).toList()

        assertWithMessage("Expected proto oneof to navigate to its generated Kotlin DSL helpers")
            .that(
                implementations.any { element -> element is KtProperty && element.name == "testOneofCase" } &&
                    implementations.any { element -> element is KtNamedFunction && element.name == "clearTestOneof" }
            )
            .isTrue()
    }

    fun testNavigationFromNestedKotlinDslPropertyToProtoField() {
        addProjectFiles()

        val displayNameProperty = findKotlinProperty("src/main/kotlin/demo/proto/UserKt.kt", "displayName")
        val definitions = findProtoDefinitions(displayNameProperty).toList()

        assertProtoDefinitionsContain(definitions, "User.Profile.display_name")
    }

    fun testNavigationFromOuterClassKotlinDslPropertyToProtoField() {
        addProjectFiles()

        val nameProperty = findKotlinProperty("src/main/kotlin/demo/proto/OuterUserKt.kt", "name")
        val definitions = findProtoDefinitions(nameProperty).toList()

        assertProtoDefinitionsContain(definitions, "OuterUser.name")
    }

    fun testNavigationFromKotlinGeneratedSourceCommentsToProto() {
        addProjectFilesWithKotlinGeneratedSourceCommentsOnly()

        val userFactory = findKotlinFunction("src/main/kotlin/demo/proto/UserKt.kt", "user")
        assertProtoDefinitionsContain(findProtoDefinitions(userFactory).toList(), "User")

        val nameProperty = findKotlinProperty("src/main/kotlin/demo/proto/UserKt.kt", "name")
        assertProtoDefinitionsContain(findProtoDefinitions(nameProperty).toList(), "User.name")

        val copyFunction = findKotlinFunction("src/main/kotlin/demo/proto/UserKt.kt", "copy")
        assertProtoDefinitionsContain(findProtoDefinitions(copyFunction).toList(), "User")

        val hasNameFunction = findKotlinFunction("src/main/kotlin/demo/proto/UserKt.kt", "hasName")
        assertProtoDefinitionsContain(findProtoDefinitions(hasNameFunction).toList(), "User.name")

        val clearNameFunction = findKotlinFunction("src/main/kotlin/demo/proto/UserKt.kt", "clearName")
        assertProtoDefinitionsContain(findProtoDefinitions(clearNameFunction).toList(), "User.name")

        val addressFactory = findKotlinFunction("src/main/kotlin/demo/proto/AddressKt.kt", "address")
        assertProtoDefinitionsContain(findProtoDefinitions(addressFactory).toList(), "Address")

        val nestedKeywordFactory = findKotlinFunction("src/main/kotlin/demo/proto/UserKt.kt", "when_")
        assertProtoDefinitionsContain(findProtoDefinitions(nestedKeywordFactory).toList(), "User.When")

        val cityProperty = findKotlinProperty("src/main/kotlin/demo/proto/AddressKt.kt", "city")
        assertProtoDefinitionsContain(findProtoDefinitions(cityProperty).toList(), "Address.city")
    }

    fun testNavigationBetweenRealisticOrNullPropertiesAndProtoField() {
        addRealisticGeneratedKotlinProjectFiles()

        val generatedFile = "src/main/kotlin/demo/realistic/RealisticUserKt.kt"
        val dslProperty = findKotlinProperty(generatedFile, "profileOrNull", "RealisticUserKt.Dsl")
        val dslDefinitions = findProtoDefinitions(dslProperty).toList()
        assertProtoDefinitionsContain(dslDefinitions, "RealisticUser.profile", "realistic")

        val orBuilderProperty = findKotlinProperty(generatedFile, "profileOrNull", "RealisticUserOrBuilder")
        val orBuilderDefinitions = findProtoDefinitions(orBuilderProperty).toList()
        assertProtoDefinitionsContain(orBuilderDefinitions, "RealisticUser.profile", "realistic")

        val protoFile = findProjectFile("src/main/proto/realistic_api.proto") as PbFile
        val field = findProtoSymbol(protoFile, "RealisticUser.profile")
        val implementations = findImplementations(field).filterIsInstance<KtProperty>().toList()

        assertWithMessage("Expected proto field to navigate to both generated profileOrNull properties")
            .that(
                implementations.any { property ->
                    property.name == "profileOrNull" &&
                        property.receiverTypeReference?.text?.contains("RealisticUserKt.Dsl") == true
                } && implementations.any { property ->
                    property.name == "profileOrNull" &&
                        property.receiverTypeReference?.text?.contains("RealisticUserOrBuilder") == true
                }
            )
            .isTrue()
    }

    fun testNavigationBetweenLiteRuntimeKotlinDslAndProto() {
        addLiteRuntimeGeneratedKotlinProjectFiles()

        val generatedFile = "src/main/kotlin/demo/lite/LiteUserKt.kt"
        val liteUserFactory = findKotlinFunction(generatedFile, "liteUser")
        assertProtoDefinitionsContain(findProtoDefinitions(liteUserFactory).toList(), "LiteUser", "lite")

        val profileFactory = findKotlinFunction(generatedFile, "profile")
        assertProtoDefinitionsContain(
            findProtoDefinitions(profileFactory).toList(),
            "LiteUser.Profile",
            "lite"
        )

        val nameProperty = findKotlinProperty(generatedFile, "name")
        assertProtoDefinitionsContain(
            findProtoDefinitions(nameProperty).toList(),
            "LiteUser.name",
            "lite"
        )
        val aliasesAdd = findKotlinFunction(generatedFile, "add", "AliasesProxy")
        assertProtoDefinitionsContain(
            findProtoDefinitions(aliasesAdd).toList(),
            "LiteUser.aliases",
            "lite"
        )
        val labelsSet = findKotlinFunction(generatedFile, "set", "LabelsProxy")
        assertProtoDefinitionsContain(
            findProtoDefinitions(labelsSet).toList(),
            "LiteUser.labels",
            "lite"
        )
        val profileOrNull = findKotlinProperty(generatedFile, "profileOrNull", "LiteUserOrBuilder")
        assertProtoDefinitionsContain(
            findProtoDefinitions(profileOrNull).toList(),
            "LiteUser.profile",
            "lite"
        )

        val protoFile = findProjectFile("src/main/proto/lite_runtime.proto") as PbFile
        assertMessageImplementation(protoFile, "LiteUser", "liteUser", "LiteUserKt.kt")
        assertMessageImplementation(protoFile, "LiteUser.Profile", "profile", "LiteUserKt.kt")

        val profileField = findProtoSymbol(protoFile, "LiteUser.profile")
        assertWithMessage("Expected lite profile field to navigate to profileOrNull")
            .that(findImplementations(profileField).any { element ->
                element is KtProperty &&
                    element.name == "profileOrNull" &&
                    element.receiverTypeReference?.text?.contains("LiteUserOrBuilder") == true
            })
            .isTrue()
    }

    fun testNavigationBetweenGrpcKotlinServiceImplementationAndProto() {
        addGrpcKotlinProjectFiles()
        addGrpcKotlinServiceImplementation()
        myFixture.addFileToProject(
            "src/main/proto/other_grpc_service.proto",
            """
        syntax = "proto3";
        package other;

        service Greeter {
          rpc SayHello(OtherRequest) returns (OtherReply);
        }

        message OtherRequest {}
        message OtherReply {}
      """.trimIndent()
        )

        val protoFile = findProjectFile("src/main/proto/grpc_service.proto") as PbFile
        val service = findProtoSymbol(protoFile, "Greeter")
        val serviceImplementations = findImplementations(service).toList()
        assertWithMessage("Expected proto service to navigate to Kotlin coroutine implementation")
            .that(serviceImplementations.any { element ->
                element is KtClassOrObject && element.name == "GreeterService"
            })
            .isTrue()

        val implementationFile = "src/main/kotlin/demo/grpc/GreeterService.kt"
        val implementationClass = findKotlinClass(implementationFile, "GreeterService")
        val serviceDefinitions = findProtoDefinitions(implementationClass).toList()
        assertProtoDefinitionsContain(serviceDefinitions, "Greeter", "grpcdemo")
        assertWithMessage("Expected gRPC SERVICE_NAME to disambiguate same-named proto services")
            .that(serviceDefinitions.filterIsInstance<PbSymbol>().map { definition ->
                definition.qualifiedName?.toString()
            })
            .containsExactly("grpcdemo.Greeter")

        val methodNames = mapOf(
            "SayHello" to "sayHello",
            "ListReplies" to "listReplies",
            "CollectReplies" to "collectReplies",
            "Chat" to "chat",
        )
        for ((protoMethodName, kotlinMethodName) in methodNames) {
            val method = findProtoSymbol(protoFile, "Greeter.$protoMethodName")
            val methodImplementations = findImplementations(method).toList()
            assertWithMessage("Expected proto rpc $protoMethodName to navigate to Kotlin coroutine override")
                .that(methodImplementations.any { element ->
                    element is KtNamedFunction && element.name == kotlinMethodName
                })
                .isTrue()

            val implementationMethod = findKotlinFunction(implementationFile, kotlinMethodName)
            assertProtoDefinitionsContain(
                findProtoDefinitions(implementationMethod).toList(),
                "Greeter.$protoMethodName",
                "grpcdemo"
            )
        }

        myFixture.configureFromExistingVirtualFile(
            myFixture.findFileInTempDir(implementationFile)
                ?: error("No project file found at $implementationFile")
        )
        val configuredImplementation = findKotlinClass(implementationFile, "GreeterService")
        myFixture.editor.caretModel.moveToOffset(configuredImplementation.nameIdentifier!!.textOffset)
        assertWithMessage("Expected Kotlin gRPC implementation class to have a proto declaration gutter")
            .that(myFixture.findGuttersAtCaret().any { gutter ->
                gutter.icon == ProtoeditorCoreIcons.GoToDeclaration
            })
            .isTrue()
    }

    fun testNavigationBetweenGrpcKotlinClientStubAndProto() {
        addGrpcKotlinProjectFiles()
        myFixture.addFileToProject(
            "src/main/proto/other_grpc_client_service.proto",
            """
        syntax = "proto3";
        package other;

        service Greeter {
          rpc SayHello(OtherRequest) returns (OtherReply);
        }

        message OtherRequest {}
        message OtherReply {}
      """.trimIndent()
        )

        val generatedFile = "src/main/kotlin/demo/grpc/GrpcServiceGrpcKt.kt"
        val protoFile = findProjectFile("src/main/proto/grpc_service.proto") as PbFile
        val service = findProtoSymbol(protoFile, "Greeter")
        val stubClass = findKotlinClass(generatedFile, "GreeterCoroutineStub")

        assertWithMessage("Expected proto service to navigate to generated Kotlin coroutine stub")
            .that(findImplementations(service).any { element -> element == stubClass })
            .isTrue()
        val serviceDefinitions = findProtoDefinitions(stubClass).toList()
        assertProtoDefinitionsContain(serviceDefinitions, "Greeter", "grpcdemo")
        assertWithMessage("Expected client stub SERVICE_NAME to disambiguate same-named proto services")
            .that(serviceDefinitions.filterIsInstance<PbSymbol>().map { definition ->
                definition.qualifiedName?.toString()
            })
            .containsExactly("grpcdemo.Greeter")

        val methodNames = mapOf(
            "SayHello" to "sayHello",
            "ListReplies" to "listReplies",
            "CollectReplies" to "collectReplies",
            "Chat" to "chat",
        )
        for ((protoMethodName, kotlinMethodName) in methodNames) {
            val protoMethod = findProtoSymbol(protoFile, "Greeter.$protoMethodName")
            val stubMethod = findKotlinFunction(
                generatedFile,
                kotlinMethodName,
                containingClassName = "GreeterCoroutineStub"
            )

            assertWithMessage("Expected proto rpc $protoMethodName to navigate to coroutine stub method")
                .that(findImplementations(protoMethod).any { element -> element == stubMethod })
                .isTrue()
            assertProtoDefinitionsContain(
                findProtoDefinitions(stubMethod).toList(),
                "Greeter.$protoMethodName",
                "grpcdemo"
            )
        }

        myFixture.configureFromExistingVirtualFile(
            myFixture.findFileInTempDir(generatedFile)
                ?: error("No project file found at $generatedFile")
        )
        val configuredStub = findKotlinClass(generatedFile, "GreeterCoroutineStub")
        myFixture.editor.caretModel.moveToOffset(configuredStub.nameIdentifier!!.textOffset)
        assertWithMessage("Expected Kotlin gRPC client stub class to have a proto declaration gutter")
            .that(myFixture.findGuttersAtCaret().any { gutter ->
                gutter.icon == ProtoeditorCoreIcons.GoToDeclaration
            })
            .isTrue()
    }

    fun testNavigationBetweenNamingCollisionFieldsAndGeneratedKotlinDeclarations() {
        addNamingCollisionGeneratedKotlinProjectFiles()

        val generatedFile = "src/main/kotlin/demo/naming/NamingCollisionMessageKt.kt"
        val propertyExpectations = mapOf(
            "hasName" to "NamingCollisionMessage.has_name",
            "clearName" to "NamingCollisionMessage.clear_name",
            "aliases4" to "NamingCollisionMessage.aliases",
            "aliasesCount5" to "NamingCollisionMessage.aliases_count",
            "aliasesList6" to "NamingCollisionMessage.aliases_list",
            "labels7" to "NamingCollisionMessage.labels",
            "labelsCount8" to "NamingCollisionMessage.labels_count",
        )
        for ((propertyName, fieldName) in propertyExpectations) {
            val property = findKotlinProperty(generatedFile, propertyName)
            assertProtoDefinitionsContain(findProtoDefinitions(property).toList(), fieldName, "naming")
        }

        val hasNameFunction = findKotlinFunction(generatedFile, "hasName")
        assertProtoDefinitionsContain(
            findProtoDefinitions(hasNameFunction).toList(),
            "NamingCollisionMessage.name",
            "naming"
        )
        val clearNameFunction = findKotlinFunction(generatedFile, "clearName")
        assertProtoDefinitionsContain(
            findProtoDefinitions(clearNameFunction).toList(),
            "NamingCollisionMessage.name",
            "naming"
        )

        val aliasesAdd = findKotlinFunction(generatedFile, "add", "Aliases4Proxy")
        assertProtoDefinitionsContain(
            findProtoDefinitions(aliasesAdd).toList(),
            "NamingCollisionMessage.aliases",
            "naming"
        )

        val protoFile = findProjectFile("src/main/proto/naming_collisions.proto") as PbFile
        val aliases = findProtoSymbol(protoFile, "NamingCollisionMessage.aliases")
        val aliasesImplementations = findImplementations(aliases).toList()
        assertWithMessage("Expected proto aliases to navigate to suffixed Kotlin property and helper")
            .that(
                aliasesImplementations.any { element ->
                    element is KtProperty && element.name == "aliases4"
                } && aliasesImplementations.any { element ->
                    element is KtNamedFunction &&
                        element.name == "add" &&
                        element.receiverTypeReference?.text?.contains("Aliases4Proxy") == true
                }
            )
            .isTrue()

        val labelsCount = findProtoSymbol(protoFile, "NamingCollisionMessage.labels_count")
        assertWithMessage("Expected proto labels_count to navigate to labelsCount8")
            .that(findImplementations(labelsCount).any { element ->
                element is KtProperty && element.name == "labelsCount8"
            })
            .isTrue()
    }

    fun testNavigationBetweenProto2GroupAndGeneratedKotlinDeclarations() {
        addProto2GroupGeneratedKotlinProjectFiles()

        val generatedFile = "src/main/kotlin/demo/groups/GroupContainerKt.kt"
        val protoFile = findProjectFile("src/main/proto/proto2_groups.proto") as PbFile

        val groupFactory = findKotlinFunction(generatedFile, "singleGroupField")
        assertProtoDefinitionsContain(
            findProtoDefinitions(groupFactory).toList(),
            "GroupContainer.SingleGroupField",
            "groups"
        )
        val groupCopy = findKotlinFunction(
            generatedFile,
            "copy",
            receiverTypeMarker = "SingleGroupField"
        )
        assertProtoDefinitionsContain(
            findProtoDefinitions(groupCopy).toList(),
            "GroupContainer.SingleGroupField",
            "groups"
        )

        val groupProperty = findKotlinProperty(generatedFile, "singleGroupField")
        assertProtoDefinitionsContain(
            findProtoDefinitions(groupProperty).toList(),
            "GroupContainer.singlegroupfield",
            "groups"
        )
        for (receiverType in listOf("GroupContainerKt.Dsl", "GroupContainerOrBuilder")) {
            val orNullProperty = findKotlinProperty(
                generatedFile,
                "singleGroupFieldOrNull",
                receiverType
            )
            assertProtoDefinitionsContain(
                findProtoDefinitions(orNullProperty).toList(),
                "GroupContainer.singlegroupfield",
                "groups"
            )
        }

        for (accessorName in listOf("hasSingleGroupField", "clearSingleGroupField")) {
            val accessor = findKotlinFunction(generatedFile, accessorName)
            assertProtoDefinitionsContain(
                findProtoDefinitions(accessor).toList(),
                "GroupContainer.singlegroupfield",
                "groups"
            )
        }

        val innerProperty = findKotlinProperty(generatedFile, "inGroup")
        assertProtoDefinitionsContain(
            findProtoDefinitions(innerProperty).toList(),
            "GroupContainer.SingleGroupField.in_group",
            "groups"
        )

        assertMessageImplementation(
            protoFile,
            "GroupContainer.SingleGroupField",
            "singleGroupField",
            "GroupContainerKt.kt"
        )

        val generatedGroupField = findProtoSymbol(protoFile, "GroupContainer.singlegroupfield")
        assertWithMessage("Expected generated group field to navigate to Kotlin DSL property and accessors")
            .that(findImplementations(generatedGroupField).any { element ->
                element is KtProperty && element.name == "singleGroupField"
            })
            .isTrue()

        val innerField = findProtoSymbol(protoFile, "GroupContainer.SingleGroupField.in_group")
        assertWithMessage("Expected group member field to navigate to Kotlin DSL property")
            .that(findImplementations(innerField).any { element ->
                element is KtProperty && element.name == "inGroup"
            })
            .isTrue()
    }

    private fun findProtoSymbol(protoFile: PbFile, localDottedName: String): PbSymbol {
        val qualifiedName = protoFile.packageQualifiedName
            .append(QualifiedName.fromDottedString(localDottedName))
        val symbols = protoFile.localQualifiedSymbolMap[qualifiedName].orEmpty()

        assertWithMessage("Find symbol: $localDottedName")
            .that(symbols)
            .hasSize(1)

        return symbols.single() as PbSymbol
    }

    private fun findProjectFile(path: String): PsiFile {
        val virtualFile = myFixture.findFileInTempDir(path)
            ?: error("No project file found at $path")
        return PsiManager.getInstance(project).findFile(virtualFile)
            ?: error("No PSI file found at $path")
    }

    private fun findKotlinFunction(
        path: String,
        name: String,
        receiverTypeMarker: String? = null,
        valueParameterType: String? = null,
        containingClassName: String? = null,
    ): KtNamedFunction {
        val ktFile = findProjectFile(path) as KtFile
        return PsiTreeUtil.findChildrenOfType(ktFile, KtNamedFunction::class.java)
            .singleOrNull { function ->
                function.name == name &&
                    (receiverTypeMarker == null ||
                        function.receiverTypeReference?.text?.contains(receiverTypeMarker) == true) &&
                    (valueParameterType == null ||
                        function.valueParameters.any { parameter ->
                            parameter.typeReference?.text == valueParameterType
                        }) &&
                    (containingClassName == null ||
                        PsiTreeUtil.getParentOfType(
                            function,
                            KtClassOrObject::class.java,
                            true
                        )?.name == containingClassName)
            }
            ?: error("No Kotlin function '$name' found in $path")
    }

    private fun findKotlinClass(path: String, name: String): KtClassOrObject {
        val ktFile = findProjectFile(path) as KtFile
        return PsiTreeUtil.findChildrenOfType(ktFile, KtClassOrObject::class.java)
            .singleOrNull { classOrObject -> classOrObject.name == name }
            ?: error("No Kotlin class '$name' found in $path")
    }

    private fun findKotlinProperty(
        path: String,
        name: String,
        receiverTypeMarker: String? = null,
    ): KtProperty {
        val ktFile = findProjectFile(path) as KtFile
        return PsiTreeUtil.findChildrenOfType(ktFile, KtProperty::class.java)
            .singleOrNull { property ->
                property.name == name &&
                    (receiverTypeMarker == null ||
                        property.receiverTypeReference?.text?.contains(receiverTypeMarker) == true)
            }
            ?: error("No Kotlin property '$name' found in $path")
    }

    private fun assertMessageImplementation(
        protoFile: PbFile,
        messageName: String,
        factoryName: String,
        fileName: String
    ) {
        val message = findProtoSymbol(protoFile, messageName)
        val implementations = findImplementations(message).toList()

        assertWithMessage("Expected implementations of proto $messageName to contain no duplicates")
            .that(implementations)
            .containsNoDuplicates()
        assertWithMessage("Expected proto $messageName to navigate to generated Kotlin DSL factory")
            .that(implementations.any { element ->
                element is KtNamedFunction &&
                    element.name == factoryName &&
                    element.containingFile.name == fileName
            })
            .isTrue()
    }

    private fun assertFieldImplementation(
        protoFile: PbFile,
        fieldName: String,
        functionName: String,
        receiverTypeMarker: String
    ) {
        val field = findProtoSymbol(protoFile, fieldName)
        val implementations = findImplementations(field).toList()

        assertWithMessage("Expected implementations of proto $fieldName to contain no duplicates")
            .that(implementations)
            .containsNoDuplicates()
        assertWithMessage("Expected proto $fieldName to navigate to Kotlin DSL $functionName")
            .that(implementations.any { element ->
                element is KtNamedFunction &&
                    element.name == functionName &&
                    element.receiverTypeReference?.text?.contains(receiverTypeMarker) == true
            })
            .isTrue()
    }

    private fun assertProtoDefinitionsContain(
        definitions: List<*>,
        expectedLocalDottedName: String,
        protoPackage: String = "demo",
    ) {
        assertWithMessage("Expected proto definitions to contain no duplicates")
            .that(definitions)
            .containsNoDuplicates()
        assertWithMessage("Expected proto definitions to contain $expectedLocalDottedName")
            .that(definitions.any { definition ->
                (definition as? PbSymbol)?.qualifiedName?.toString() == "$protoPackage.$expectedLocalDottedName"
            })
            .isTrue()
    }
}
