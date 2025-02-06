// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.ide.IdeBundle
import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.webSymbols.testFramework.checkListByFile
import com.intellij.webSymbols.testFramework.renderLookupItems
import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule.*
import org.angular2.Angular2TsConfigFile
import org.angular2.lang.Angular2Bundle
import org.intellij.idea.lang.javascript.intention.JSIntentionBundle

class Angular2IntentionsAndQuickFixesTest : Angular2TestCase("intentionsAndQuickFixes", true) {

  fun testBooleanTransformAttr() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-input-transformer.std.name", "booleanAttribute"),
           ANGULAR_CORE_16_2_8)

  fun testBooleanTransformBinding() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-input-transformer.std.name", "booleanAttribute"),
           ANGULAR_CORE_16_2_8)

  fun testNumberTransformAttr() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-input-transformer.std.name", "numberAttribute"),
           ANGULAR_CORE_16_2_8)

  fun testNumberTransformBinding() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-input-transformer.std.name", "numberAttribute"),
           ANGULAR_CORE_16_2_8)

  fun testCustomTransformAttr() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-input-transformer.family"),
           ANGULAR_CORE_16_2_8)

  fun testCustomTransformBinding() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-input-transformer.family"),
           ANGULAR_CORE_16_2_8)

  fun testCustomTransformBindingWithAlias() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-input-transformer.family"),
           ANGULAR_CORE_16_2_8)

  fun testCreateSignalFromUsage() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-signal.name", "fooSig"),
           ANGULAR_CORE_16_2_8, checkIntentionPreview = false)

  fun testNoCreateSignalFromUsage() =
    checkNoIntention(Angular2Bundle.message("angular.quickfix.template.create-signal.name", "fooSig"),
                     ANGULAR_CORE_16_2_8)

  fun testCreateObservablePropertyFromUsage() =
    doTest(JavaScriptBundle.message("javascript.create.field.intention.name", "foo"),
           ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, RXJS_7_8_1, checkCodeCompletion = true, checkIntentionPreview = false)

  fun testCreateObservableMethodFromUsage() =
    doTest(JavaScriptBundle.message("javascript.create.method.intention.name", "foo"),
           ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, RXJS_7_8_1, checkIntentionPreview = false)

  fun testCreateComponentOutputFromUsage() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-output.name", "emitter"),
           ANGULAR_CORE_16_2_8, checkIntentionPreview = false)

  fun testNoCreateComponentOutputFromUsage() =
    checkNoIntention(Angular2Bundle.message("angular.quickfix.template.create-output.name", "emitter"),
                     ANGULAR_CORE_16_2_8)

  fun testNoJSImportForPipe() =
    checkNoIntention(IdeBundle.message("quickfix.text.insert.0", "\"import {async} from 'rxjs'\""),
                     ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, RXJS_7_8_1)

  fun testBasicFieldCreation() {
    doTest(JavaScriptBundle.message("javascript.create.field.intention.name", "foo"),
           dir = true, configureFileName = "template.html", checkIntentionPreview = false)
  }

  fun testThisQualifiedFieldCreation() {
    doTest(JavaScriptBundle.message("javascript.create.field.intention.name", "foo"),
           dir = true, configureFileName = "template.html", checkIntentionPreview = false)
  }

  fun testQualifiedFieldCreation() {
    doTest(JavaScriptBundle.message("javascript.create.field.intention.name", "foo"),
           dir = true, configureFileName = "template.html", checkIntentionPreview = false)
  }

  fun testBasicMethodCreation() {
    doTest(JavaScriptBundle.message("javascript.create.method.intention.name", "foo"),
           dir = true, configureFileName = "template.html", checkIntentionPreview = false)
  }

  fun testThisQualifiedMethodCreation() {
    doTest(JavaScriptBundle.message("javascript.create.method.intention.name", "foo"),
           dir = true, configureFileName = "template.html", checkIntentionPreview = false)
  }

  fun testQualifiedMethodCreation() {
    doTest(JavaScriptBundle.message("javascript.create.method.intention.name", "foo"),
           dir = true, configureFileName = "template.html", checkIntentionPreview = false)
  }

  fun testComputeConstantInTemplate() {
    doTest(JSIntentionBundle.message("string.join-concatenated-string-literals.display-name"), extension = "html",
           checkIntentionPreview = false)
  }

  fun testFlipConditionalInTemplate() {
    doTest(JSIntentionBundle.message("conditional.flip-conditional.display-name"), extension = "html", checkIntentionPreview = false)
  }

  fun testDeMorgansLawInTemplate() {
    doTest(JSIntentionBundle.message("bool.de-morgans-law.display-name.ANDAND"), extension = "html", checkIntentionPreview = false)
  }

  fun testCreateComponentInputBasic() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-input.name", "foo"), ANGULAR_CORE_16_2_8, checkIntentionPreview = false)

  fun testCreateDirectiveInputAsSelector() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-input.name", "test"), ANGULAR_CORE_16_2_8,
           checkIntentionPreview = false)

  fun testCreateDirectiveInputWithDash() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-input.name", "foo-bar"), ANGULAR_CORE_16_2_8,
           checkIntentionPreview = false)

  fun testNoCreateLibDirectiveInput() =
    checkNoIntention(Angular2Bundle.message("angular.quickfix.template.create-input.name", "foo"),
                     ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, ANGULAR_CDK_14_2_0)

  fun testCreateDirectiveOutputBasic() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-output.name", "foo"), ANGULAR_CORE_16_2_8,
           checkIntentionPreview = false)

  fun testCreateDirectiveOutputWithDash() =
    doTest(Angular2Bundle.message("angular.quickfix.template.create-output.name", "foo-bar"), ANGULAR_CORE_16_2_8,
           checkIntentionPreview = false)

  fun testNoCreateLibDirectiveOutput() =
    checkNoIntention(Angular2Bundle.message("angular.quickfix.template.create-input.name", "foo"),
                     ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, ANGULAR_CDK_14_2_0)

  fun testExtractLetVariable() =
    doTest(Angular2Bundle.message("angular.intention.introduce.let.variable.name"), ANGULAR_CORE_18_2_1,
           extension = "html", checkIntentionPreview = false)

  fun testExtractComponentTemplate() =
    doTest(Angular2Bundle.message("angular.intention.extract.component.template.name"), ANGULAR_CORE_18_2_1,
           dir = true)

  fun testInlineComponentTemplate() =
    doTest(Angular2Bundle.message("angular.intention.inline.component.template.name"), ANGULAR_CORE_18_2_1,
           dir = true)

  fun testRemoveUnusedBlockLetVariable() =
    doTest(JavaScriptBundle.message("js.unused.symbol.remove", "constant 'letUnused'"),  ANGULAR_CORE_18_2_1,
           checkIntentionPreview = false)

  fun testRemoveUnusedNgTemplateLetVariable() =
    doTest(JavaScriptBundle.message("js.unused.symbol.remove", "constant 'ngTemplateUnused'"),  ANGULAR_CORE_18_2_1,
           checkIntentionPreview = false)

  fun testRemoveUnusedBlockParameterVariable1() =
    doTest(JavaScriptBundle.message("js.unused.symbol.remove", "constant 'first'"),  ANGULAR_CORE_18_2_1,
           checkIntentionPreview = false)

  fun testRemoveUnusedBlockParameterVariable2() =
    doTest(JavaScriptBundle.message("js.unused.symbol.remove", "constant 'first'"),  ANGULAR_CORE_18_2_1,
           checkIntentionPreview = false)

  fun testRemoveUnusedTemplateBindingVariable1() =
    doTest(JavaScriptBundle.message("js.unused.symbol.remove", "constant 'forUnused'"),  ANGULAR_CORE_18_2_1,
           checkIntentionPreview = false)

  fun testRemoveUnusedTemplateBindingVariable2() =
    doTest(JavaScriptBundle.message("js.unused.symbol.remove", "constant 'ifUnused'"),  ANGULAR_CORE_18_2_1,
           checkIntentionPreview = false)

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
  }

  private fun checkNoIntention(intentionName: String,
                               vararg modules: WebFrameworkTestModule) {
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

  private fun doTest(intentionName: String,
                     vararg modules: WebFrameworkTestModule,
                     dir: Boolean = false,
                     extension: String = defaultExtension,
                     configureFileName: String = "$testName.$extension",
                     checkIntentionPreview: Boolean = true,
                     checkCodeCompletion: Boolean = false
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