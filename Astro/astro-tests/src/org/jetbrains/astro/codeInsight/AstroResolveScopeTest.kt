package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.lang.javascript.psi.ecma6.TypeScriptVariable
import com.intellij.lang.typescript.library.TypeScriptLibraryProvider
import com.intellij.lang.typescript.tsconfig.TypeScriptConfig
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.junit.Assert

class AstroResolveScopeTest : AstroCodeInsightTestCase("codeInsight/resolveScope") {
  fun testBasic() {
    TypeScriptTestUtil.setTsConfigOptions(project, testRootDisposable, TypeScriptConfig.LanguageTarget.ES6)
    val mainFile = myFixture.configureByFile("$testName.astro")

    val bundledLibs = TypeScriptLibraryProvider.getService(project).allBundledLibraries

    val olderLib = bundledLibs.single { it.name.contains("lib.es5.d.ts") }
    val newerLib = bundledLibs.single { it.name.contains("lib.es2017.d.ts") }

    val fooVar = PsiTreeUtil.findChildOfType(mainFile, TypeScriptVariable::class.java)!!

    Assert.assertTrue("Scope must contain ES5 lib", fooVar.resolveScope.contains(olderLib))
    Assert.assertFalse("Scope must not contain too new lib", fooVar.resolveScope.contains(newerLib))
  }
}
