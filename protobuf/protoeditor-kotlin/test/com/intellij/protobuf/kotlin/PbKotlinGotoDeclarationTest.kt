package com.intellij.protobuf.kotlin

import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction
import com.intellij.protobuf.gencodeutils.GotoExpectationMarker
import com.intellij.psi.util.PsiUtilCore
import com.intellij.psi.util.parentOfType
import org.jetbrains.kotlin.psi.KtReferenceExpression

class PbKotlinGotoDeclarationTest : PbKotlinTestBase() {
    fun testAll() {
        doTest("all_goto_declaration.kt.test")
    }

    fun testAliasImports() {
        doTest("alias_imports.kt.test")
    }

    fun testSamePackage() {
        doTest("same_package.kt.test")
    }

    fun testWildcardImports() {
        doTest("wildcard_imports.kt.test")
    }

    fun testClashingNames() {
        addClashingNameProjectFiles()
        configureUser("clashing_names.kt.test")
        doConfiguredTest()
    }

    fun testKotlinGeneratedSourceCommentFallback() {
        addProjectFilesWithKotlinGeneratedSourceCommentsOnly()
        configureUser("source_comment_fallback.kt.test")
        doConfiguredTest()
    }

    fun testQualifiedNestedKeywordFactory() {
        doTest("qualified_nested_factory.kt.test")
    }

    fun testRealisticGeneratedKotlinApi() {
        addRealisticGeneratedKotlinProjectFiles()
        configureUser("realistic_generated_api.kt.test")
        doConfiguredTest()
    }

    fun testRealisticGeneratedKotlinNamingCollisions() {
        addNamingCollisionGeneratedKotlinProjectFiles()
        configureUser("naming_collisions.kt.test")
        doConfiguredTest()
    }

    fun testRealisticGeneratedKotlinLiteRuntime() {
        addLiteRuntimeGeneratedKotlinProjectFiles()
        configureUser("lite_runtime.kt.test")
        doConfiguredTest()
    }

    fun testGrpcKotlinClientStub() {
        addGrpcKotlinProjectFiles()
        configureUser("grpc_kotlin_client.kt.test")
        doConfiguredTest()
    }

    fun testRealisticProto2Groups() {
        addProto2GroupGeneratedKotlinProjectFiles()
        configureUser("proto2_groups.kt.test")
        doConfiguredTest()
    }

    private fun doTest(testFile: String) {
        addProjectFiles()
        configureUser(testFile)
        doConfiguredTest()
    }

    private fun doConfiguredTest() {
        testExpectations(GotoExpectationMarker::parseExpectations) { expectation, lineNumber ->
            val reference = PsiUtilCore.getElementAtOffset(myFixture.file, myFixture.caretOffset)
                .parentOfType<KtReferenceExpression>()
                ?: error("No Kotlin reference found at line $lineNumber")

            val gotoTargets = GotoDeclarationAction.findAllTargetElements(
                myFixture.project,
                myFixture.editor,
                myFixture.caretOffset,
            )

            expectation.checkGotoTargets(reference.text, gotoTargets, lineNumber)
        }
    }
}
