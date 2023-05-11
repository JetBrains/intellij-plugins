package com.intellij.lang.javascript.frameworks.nextjs

import com.intellij.lang.javascript.JSDaemonAnalyzerLightTestCase
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection

/**
 * Needs to be run with the classpath intellij.idea.ultimate.tests.main
 */
class NextJsHighlightTest : JSDaemonAnalyzerLightTestCase() {
  override fun getTestDataPath(): String = NextJsTestUtil.getTestDataPath() + "highlight_nextjs/"

  override fun getBasePath(): String = throw IllegalStateException()


  override fun getExtension(): String = "js"

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection())
  }

  fun testUnusedGlobalInspection() {
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

  fun testNextjsProject() {
    myFixture.enableInspections(JSUnusedLocalSymbolsInspection())
    val dir = getTestName(true)
    myFixture.copyDirectoryToProject(dir, "");
    myFixture.testHighlightingAllFiles(
      true, false, true,
      "app/page.tsx",
      "app/route.js",
      "otherDir/otherPage.tsx",
      "otherDir/otherRoute.js"
    )
  }
}