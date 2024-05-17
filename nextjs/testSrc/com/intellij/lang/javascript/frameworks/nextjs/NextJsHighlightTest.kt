package com.intellij.lang.javascript.frameworks.nextjs

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection
import com.intellij.lang.javascript.JSDaemonAnalyzerLightTestCase
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection

/**
 * Needs to be run with the classpath intellij.idea.ultimate.tests.main
 */
class NextJsHighlightTest : JSDaemonAnalyzerLightTestCase() {
  override fun getTestDataPath(): String = NextJsTestUtil.getTestDataPath() + "highlight/"

  override fun getBasePath(): String = throw IllegalStateException()


  override fun getExtension(): String = "js"

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection())
  }

  fun testUnusedGlobalInspection() {
    myFixture.addFileToProject("package.json", "{\"dependencies\": {\"next\": \"*\"}}")
    myFixture.addFileToProject("pages/smth/component1.js", "export default function Test1() {return <div></div>}")
    myFixture.testHighlighting("pages/smth/component1.js")
  }

  fun testUnusedStaticMethods() {
    myFixture.addFileToProject("pages/smth/component1.js",
                               "export async function getStaticProps(context) {\n" +
                               "  return {\n" +
                               "    props: {}, // will be passed to the page component as props\n" +
                               "  }\n" +
                               "}")
    addPackageJsonWithDependencyOnNext()
    myFixture.testHighlighting("pages/smth/component1.js")
  }

  fun testUnusedStaticVariable() {
    myFixture.addFileToProject("pages/smth/component1.js",
                               "export const getStaticProps = (context) => {\n" +
                               "  return {\n" +
                               "    props: {}, // will be passed to the page component as props\n" +
                               "  }\n" +
                               "}")
    addPackageJsonWithDependencyOnNext()
    myFixture.testHighlighting("pages/smth/component1.js")
  }

  private fun addPackageJsonWithDependencyOnNext() {
    myFixture.addFileToProject("package.json", "{\"dependencies\": {\"next\": \"*\"}}")
  }

  fun testGroupAndSlotResolving() {
    myFixture.enableInspections(HtmlUnknownTargetInspection())
    val dir = getTestName(true)
    myFixture.copyDirectoryToProject(dir, "")
    myFixture.testHighlighting(true, false, true, "app/usage/usage.tsx")
  }

  fun testNextjsProject() {
    myFixture.enableInspections(JSUnusedLocalSymbolsInspection())
    val dir = getTestName(true)
    myFixture.copyDirectoryToProject(dir, "")
    myFixture.testHighlightingAllFiles(
      true, false, true,
      "app/layout.tsx",
      "app/page.tsx",
      "app/route.ts",
      "otherDir/exportDefault.ts",
      "otherDir/knownFunctionsAndObjects.js",
      "otherDir/middleware.js",
      "otherDir/route.jsx"
    )
  }
}