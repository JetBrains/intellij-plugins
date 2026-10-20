package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.lang.javascript.psi.ecma6.TypeScriptVariable
import com.intellij.lang.typescript.library.TypeScriptLibraryProvider
import com.intellij.lang.typescript.tsconfig.TypeScriptConfig
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class AstroResolveScopeTest : AstroCodeInsightTestCase("codeInsight/resolveScope") {
  @Test
  fun testBasic() {
    TypeScriptTestUtil.setTsConfigOptions(project, astro.testRootDisposable, TypeScriptConfig.LanguageTarget.ES6)
    val mainFile = myFixture.configureByFile("${astro.testName}.astro")

    val bundledLibs = TypeScriptLibraryProvider.getService(project).allBundledLibraries

    val olderLib = bundledLibs.single { it.name.contains("lib.es5.d.ts") }
    val newerLib = bundledLibs.single { it.name.contains("lib.es2017.d.ts") }

    // The resolve scope needs a progress job.
    ReadAction.nonBlocking<Unit> {
      val fooVar = PsiTreeUtil.findChildOfType(mainFile, TypeScriptVariable::class.java)!!

      assertTrue(fooVar.resolveScope.contains(olderLib), "Scope must contain ES5 lib")
      assertFalse(fooVar.resolveScope.contains(newerLib), "Scope must not contain too new lib")
    }.executeSynchronously()
  }
}
