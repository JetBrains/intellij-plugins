// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css

import com.intellij.testFramework.UsefulTestCase
import com.intellij.util.containers.ContainerUtil
import com.intellij.webSymbols.moveToOffsetBySignature
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angularjs.AngularTestUtil

class CssCompletionTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "css/completion"
  }

  fun testLocalCssCompletionHtml() {
    myFixture.configureByFiles("local-stylesheet-ext.html", "local-stylesheet-ext.ts", "local-stylesheet-ext.css", "package.json")
    myFixture.moveToOffsetBySignature("class=\"<caret>\"")
    myFixture.completeBasic()
    assertEquals(mutableListOf("", "local-class-ext", "local-class-int"), ContainerUtil.sorted(myFixture.getLookupElementStrings()!!))
    myFixture.type('\n')
    myFixture.moveToOffsetBySignature("id=\"<caret>\"")
    myFixture.completeBasic()
    assertEquals(mutableListOf("local-id-ext", "local-id-int"), ContainerUtil.sorted(myFixture.getLookupElementStrings()!!))
  }

  fun testLocalCssCompletionLocalCss() {
    myFixture.configureByFiles("local-stylesheet-ext.ts", "local-stylesheet-ext.css", "local-stylesheet-ext.html", "package.json")
    myFixture.moveToOffsetBySignature(".<caret> {")
    myFixture.completeBasic()
    assertEquals(mutableListOf("class-in-html", "local-class-ext"), ContainerUtil.sorted(myFixture.getLookupElementStrings()!!))
    myFixture.type('\n')
    myFixture.moveToOffsetBySignature("#<caret> {")
    myFixture.completeBasic()
    assertEquals(mutableListOf("id-in-html", "local-id-ext"), ContainerUtil.sorted(myFixture.getLookupElementStrings()!!))
  }

  fun testPreprocessorIncludePaths() {
    myFixture.addFileToProject("angular.json",
                               "{\"projects\":{\"foo\":{\"root\":\"src\",\"architect\":{\"build\":{\"builder\":\"z\"," +
                               "\"options\":{\"stylePreprocessorOptions\":{\"includePaths\":[\"src/foo\"]}}}}}}}")
    myFixture.addFileToProject("src/_var1.scss", "")
    myFixture.addFileToProject("src/foo/_var2.scss", "")
    myFixture.addFileToProject("src/foo/bar/_var3.scss", "")
    myFixture.addFileToProject("src/foobar/main.scss", "@import '<caret>';")
    myFixture.configureFromTempProjectFile("src/foobar/main.scss")
    myFixture.completeBasic()

    //todo remove src context
    UsefulTestCase.assertSameElements(myFixture.getLookupElementStrings()!!, "~foo", "~foobar", "bar", "var2", "src")
  }

  fun testBaseURLPriority() {
    myFixture.addFileToProject("tsconfig.json", "{\"compilerOptions\": {\"baseUrl\": \"./src/foo\"}}")
    myFixture.addFileToProject("angular.json",
                               "{\"projects\":{\"foo\":{\"root\":\"src\",\"architect\":{\"build\":{\"builder\":\"z\"," +
                               "\"options\":{\"tsConfig\":\"tsconfig.json\",\"stylePreprocessorOptions\":{\"includePaths\":[\"src/foo\"]}}}}}}}")
    myFixture.addFileToProject("src/_var1.scss", "")
    myFixture.addFileToProject("src/foo/_var2.scss", "")
    myFixture.addFileToProject("src/foo/bar/_var3.scss", "")
    myFixture.addFileToProject("src/foobar/main.scss", "@import '<caret>';")
    myFixture.configureFromTempProjectFile("src/foobar/main.scss")
    myFixture.completeBasic()

    //todo remove src context
    UsefulTestCase.assertSameElements(myFixture.getLookupElementStrings()!!, "~bar", "bar", "var2", "src")
  }

  fun testLegacyPreprocessorIncludePaths() {
    myFixture.addFileToProject(".angular-cli.json",
                               "{ \"apps\": [{\"root\": \"src\", \"stylePreprocessorOptions\": {\"includePaths\": [\"foo\"]}}]}")
    myFixture.addFileToProject("src/_var1.scss", "")
    myFixture.addFileToProject("src/foo/_var2.scss", "")
    myFixture.addFileToProject("src/foo/bar/_var3.scss", "")
    myFixture.configureByText("main.scss", "@import '<caret>';")
    myFixture.completeBasic()
    UsefulTestCase.assertSameElements(myFixture.getLookupElementStrings()!!, "src", "~foo", "bar", "var2")
  }
}
