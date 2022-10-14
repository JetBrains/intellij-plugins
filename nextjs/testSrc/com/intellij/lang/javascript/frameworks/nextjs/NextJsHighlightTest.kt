package com.intellij.lang.javascript.frameworks.nextjs

import com.intellij.lang.javascript.JSDaemonAnalyzerLightTestCase
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection

class NextJsHighlightTest : JSDaemonAnalyzerLightTestCase() {
  override fun getBasePath(): String = NextJsTestUtil.getTestDataPath()
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
    myFixture.testHighlighting("pages/smth/component1.js")
  }

  fun testUnusedStaticVariable() {
    myFixture.addFileToProject("pages/smth/component1.js",
                               "export const getStaticProps = (context) => {\n" +
                               "  return {\n" +
                               "    props: {}, // will be passed to the page component as props\n" +
                               "  }\n" +
                               "}")
    myFixture.testHighlighting("pages/smth/component1.js")
  }
}