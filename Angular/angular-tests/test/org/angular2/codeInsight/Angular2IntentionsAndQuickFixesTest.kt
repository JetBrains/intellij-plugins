// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.ide.IdeBundle
import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList.AccessType
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil.accessTypeToKeyword
import com.intellij.polySymbols.testFramework.checkListByFile
import com.intellij.polySymbols.testFramework.renderLookupItems
import com.intellij.refactoring.BaseRefactoringProcessor
import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule.ANGULAR_CDK_14_2_0
import org.angular2.Angular2TestModule.ANGULAR_COMMON_16_2_8
import org.angular2.Angular2TestModule.ANGULAR_CORE_16_2_8
import org.angular2.Angular2TestModule.ANGULAR_CORE_18_2_1
import org.angular2.Angular2TestModule.RXJS_7_8_1
import org.angular2.Angular2TsConfigFile
import org.angular2.SkipTsGoFork
import org.angular2.TestTsGoFork
import org.angular2.TestTsNode
import org.angular2.lang.Angular2Bundle
import org.junit.Test
import org.intellij.idea.lang.javascript.intention.JSIntentionBundle
import org.junit.Ignore

@TestTsNode
@TestTsGoFork
class Angular2IntentionsAndQuickFixesTest : Angular2TestCase("intentionsAndQuickFixes") {

  @Test
  fun testBooleanTransformAttr() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-input-transformer.std.name", "booleanAttribute"),
           ANGULAR_CORE_16_2_8)

  @Test
  @SkipTsGoFork
  @Ignore("Caused by WEB-78250")
  fun testBooleanTransformBinding() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-input-transformer.std.name", "booleanAttribute"),
           ANGULAR_CORE_16_2_8)

  @Test
  fun testNumberTransformAttr() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-input-transformer.std.name", "numberAttribute"),
           ANGULAR_CORE_16_2_8)

  @Test
  @SkipTsGoFork
  @Ignore("Caused by WEB-78250")
  fun testNumberTransformBinding() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-input-transformer.std.name", "numberAttribute"),
           ANGULAR_CORE_16_2_8)

  @Test
  fun testCustomTransformAttr() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-input-transformer.family"),
           ANGULAR_CORE_16_2_8)

  @Test
  fun testCustomTransformBinding() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-input-transformer.family"),
           ANGULAR_CORE_16_2_8)

  @Test
  fun testCustomTransformBindingWithAlias() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-input-transformer.family"),
           ANGULAR_CORE_16_2_8)

  @Test
  fun testCreateSignalFromUsage() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-signal.name", "fooSig"),
           ANGULAR_CORE_16_2_8, checkIntentionPreview = false)

  @Test
  fun testNoCreateSignalFromUsage() =
    checkNoIntention(Angular2Bundle.message("angular.quickfix.template.create-signal.name", "fooSig"),
                     ANGULAR_CORE_16_2_8)

  @Test
  @SkipTsGoFork
  fun testCreateObservablePropertyFromUsage() =
    doTest(JavaScriptBundle.message("javascript.create.field.intention.name", "foo"),
           ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, RXJS_7_8_1, checkCodeCompletion = true, checkIntentionPreview = false)

  @Test
  fun testCreateObservableMethodFromUsage() =
    doTest(JavaScriptBundle.message("javascript.create.method.intention.name", "foo"),
           ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, RXJS_7_8_1, checkIntentionPreview = false)

  @Test
  fun testCreateComponentOutputFromUsage() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-output.name", "emitter"),
           ANGULAR_CORE_16_2_8, checkIntentionPreview = false)

  @Test
  fun testNoCreateComponentOutputFromUsage() =
    checkNoIntention(Angular2Bundle.message("angular.quickfix.template.create-output.name", "emitter"),
                     ANGULAR_CORE_16_2_8)

  @Test
  fun testNoJSImportForPipe() =
    checkNoIntention(IdeBundle.message("quickfix.text.insert.0", "\"import {async} from 'rxjs'\""),
                     ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, RXJS_7_8_1)

  @Test
  @SkipTsGoFork
  fun testBasicFieldCreation() {
    doTest(JavaScriptBundle.message("javascript.create.field.intention.name", "foo"),
           dir = true, configureFileName = "template.html", checkIntentionPreview = false)
  }

  @Test
  @SkipTsGoFork
  fun testFieldCreationWithExportDefault() {
    doTest(JavaScriptBundle.message("javascript.create.field.intention.name", "foo"),
           dir = true, configureFileName = "template.html", checkIntentionPreview = false)
  }

  @Test
  @SkipTsGoFork
  fun testThisQualifiedFieldCreation() {
    doTest(JavaScriptBundle.message("javascript.create.field.intention.name", "foo"),
           dir = true, configureFileName = "template.html", checkIntentionPreview = false)
  }

  @Test
  @SkipTsGoFork
  fun testQualifiedFieldCreation() {
    doTest(JavaScriptBundle.message("javascript.create.field.intention.name", "foo"),
           dir = true, configureFileName = "template.html", checkIntentionPreview = false)
  }

  @Test
  fun testBasicMethodCreation() {
    doTest(JavaScriptBundle.message("javascript.create.method.intention.name", "foo"),
           dir = true, configureFileName = "template.html", checkIntentionPreview = false)
  }

  @Test
  fun testMethodCreationWithExportDefault() {
    doTest(JavaScriptBundle.message("javascript.create.method.intention.name", "foo"),
           dir = true, configureFileName = "template.html", checkIntentionPreview = false)
  }

  @Test
  fun testThisQualifiedMethodCreation() {
    doTest(JavaScriptBundle.message("javascript.create.method.intention.name", "foo"),
           dir = true, configureFileName = "template.html", checkIntentionPreview = false)
  }

  @Test
  @SkipTsGoFork
  fun testQualifiedMethodCreation() {
    doTest(JavaScriptBundle.message("javascript.create.method.intention.name", "foo"),
           dir = true, configureFileName = "template.html", checkIntentionPreview = false)
  }

  @Test
  fun testComputeConstantInTemplate() {
    doTest(JSIntentionBundle.message("string.join-concatenated-string-literals.display-name"), extension = "html",
           checkIntentionPreview = false)
  }

  @Test
  fun testFlipConditionalInTemplate() {
    doTest(JSIntentionBundle.message("conditional.flip-conditional.display-name"), extension = "html", checkIntentionPreview = false)
  }

  @Test
  fun testDeMorgansLawInTemplate() {
    doTest(JSIntentionBundle.message("bool.de-morgans-law.display-name.ANDAND"), extension = "html", checkIntentionPreview = false)
  }

  @Test
  fun testCreateComponentInputBasic() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-input.name", "foo"), ANGULAR_CORE_16_2_8, checkIntentionPreview = false)

  @Test
  fun testCreateDirectiveInputAsSelector() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-input.name", "test"), ANGULAR_CORE_16_2_8,
           checkIntentionPreview = false)

  @Test
  fun testCreateDirectiveInputWithDash() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-input.name", "foo-bar"), ANGULAR_CORE_16_2_8,
           checkIntentionPreview = false)

  @Test
  fun testNoCreateLibDirectiveInput() =
    checkNoIntention(Angular2Bundle.message("angular.quickfix.template.create-input.name", "foo"),
                     ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, ANGULAR_CDK_14_2_0)

  @Test
  fun testCreateDirectiveOutputBasic() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-output.name", "foo"), ANGULAR_CORE_16_2_8,
           checkIntentionPreview = false)

  @Test
  fun testCreateDirectiveOutputWithDash() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-output.name", "foo-bar"), ANGULAR_CORE_16_2_8,
           checkIntentionPreview = false)

  @Test
  fun testNoCreateLibDirectiveOutput() =
    checkNoIntention(Angular2Bundle.message("angular.quickfix.template.create-input.name", "foo"),
                     ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, ANGULAR_CDK_14_2_0)

  @Test
  fun testExtractLetVariable() =
    doTest(Angular2Bundle.message("angular.intention.introduce.let.variable.name"), ANGULAR_CORE_18_2_1,
           extension = "html", checkIntentionPreview = false)

  @Test
  fun testExtractComponentTemplate() =
    doTest(Angular2Bundle.message("angular.intention.extract.component.template.name"), ANGULAR_CORE_18_2_1,
           dir = true)

  @Test
  fun testInlineComponentTemplate() =
    doTest(Angular2Bundle.message("angular.intention.inline.component.template.name"), ANGULAR_CORE_18_2_1,
           dir = true)

  @Test
  fun testRemoveUnusedBlockLetVariable() =
    doTest(JavaScriptBundle.message("js.unused.symbol.remove", "constant 'letUnused'"), ANGULAR_CORE_18_2_1,
           checkIntentionPreview = false)

  @Test
  fun testRemoveUnusedNgTemplateLetVariable() =
    doTest(JavaScriptBundle.message("js.unused.symbol.remove", "constant 'ngTemplateUnused'"), ANGULAR_CORE_18_2_1,
           checkIntentionPreview = false)

  @Test
  fun testRemoveUnusedBlockParameterVariable1() =
    doTest(JavaScriptBundle.message("js.unused.symbol.remove", "constant 'first'"), ANGULAR_CORE_18_2_1,
           checkIntentionPreview = false)

  @Test
  fun testRemoveUnusedBlockParameterVariable2() =
    doTest(JavaScriptBundle.message("js.unused.symbol.remove", "constant 'first'"), ANGULAR_CORE_18_2_1,
           checkIntentionPreview = false)

  @Test
  fun testRemoveUnusedTemplateBindingVariable1() =
    doTest(JavaScriptBundle.message("js.unused.symbol.remove", "constant 'forUnused'"), ANGULAR_CORE_18_2_1,
           checkIntentionPreview = false)

  @Test
  fun testRemoveUnusedTemplateBindingVariable2() =
    doTest(JavaScriptBundle.message("js.unused.symbol.remove", "constant 'ifUnused'"), ANGULAR_CORE_18_2_1,
           checkIntentionPreview = false)

  @Test
  fun testChangeVisibilityToProtectedInTemplate() =
    doTest(JavaScriptBundle
             .message("typescript.fix.change.member.access", accessTypeToKeyword(AccessType.PROTECTED, DialectOptionHolder.TS)),
           ANGULAR_CORE_18_2_1, checkIntentionPreview = false)

  @Test
  fun testChangeVisibilityToPrivateInTemplate() =
    try {
      doTest(JavaScriptBundle
               .message("typescript.fix.change.member.access", accessTypeToKeyword(AccessType.PRIVATE, DialectOptionHolder.TS)),
             ANGULAR_CORE_18_2_1, checkIntentionPreview = false)
      throw AssertionError("Intention action should report conflicts")
    } catch (e: BaseRefactoringProcessor.ConflictsInTestsException) {
      assert(e.messages.count() == 1) { "Intention action should report one conflict"}
    }

  @Test
  fun testChangeVisibilityToSharpPrivateInTemplate() = try {
    doTest(JavaScriptBundle.message("js.fix.change.member.access.to.sharp"),
           ANGULAR_CORE_18_2_1, checkIntentionPreview = false)
    throw AssertionError("Intention action should report conflicts")
  } catch (e: BaseRefactoringProcessor.ConflictsInTestsException) {
    assert(e.messages.count() == 1) { "Intention action should report one conflict"}
  }

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
  }

  private fun checkNoIntention(
    intentionName: String,
    vararg modules: WebFrameworkTestModule,
  ) {
    doConfiguredTest(*modules,
                     configurators = listOf(Angular2TsConfigFile())
    ) {
      try {
        findSingleIntention(intentionName)
        throw AssertionError("Intention action $intentionName is present")
      }
      catch (e: AssertionError) {
        if (e.message.let { it != null && !it.startsWith("\"" + intentionName) })
          throw e
      }
    }
  }

  private fun doTest(
    intentionName: String,
    vararg modules: WebFrameworkTestModule,
    dir: Boolean = false,
    extension: String = defaultExtension,
    configureFileName: String = "$testName.$extension",
    checkIntentionPreview: Boolean = true,
    checkCodeCompletion: Boolean = false,
  ) {
    doConfiguredTest(*modules, dir = dir,
                     checkResult = true,
                     configureFileName = configureFileName,
                     configurators = listOf(Angular2TsConfigFile())
    ) {
      if (checkCodeCompletion)
        TemplateManagerImpl.setTemplateTesting(testRootDisposable)
      if (checkIntentionPreview) {
        checkPreviewAndLaunchAction(findSingleIntention(intentionName))
      }
      else {
        launchAction(findSingleIntention(intentionName))
      }
      if (checkCodeCompletion) {
        checkListByFile(renderLookupItems(false, false),
                        if (dir) "${testName}/items.txt" else "${testName}.items.txt", false)
        type("\n")
      }
    }
  }

}