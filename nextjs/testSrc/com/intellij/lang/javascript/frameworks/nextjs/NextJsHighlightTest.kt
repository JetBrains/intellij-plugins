package com.intellij.lang.javascript.frameworks.nextjs

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection
import com.intellij.lang.javascript.JSDaemonAnalyzerFixture.Companion.jsDaemonAnalyzerFixture
import com.intellij.lang.javascript.JSFixtureOptions
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection
import com.intellij.lang.javascript.jsProjectFixture
import com.intellij.openapi.application.EDT
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Needs to be run with the classpath intellij.idea.ultimate.tests.main
 */
@TestApplication
class NextJsHighlightTest {
  private val projectFixture = jsProjectFixture()
  private val js by jsDaemonAnalyzerFixture(projectFixture, JSFixtureOptions(NextJsTestUtil.getTestDataPath() + "highlight/", "js"))

  @BeforeEach
  fun setUp() {
    js.fixture.enableInspections(JSUnusedGlobalSymbolsInspection())
  }

  @Test
  fun testUnusedGlobalInspection() {
    js.fixture.addFileToProject("package.json", "{\"dependencies\": {\"next\": \"*\"}}")
    js.fixture.addFileToProject("pages/smth/component1.js", "export default function Test1() {return <div></div>}")
    js.fixture.testHighlighting("pages/smth/component1.js")
  }

  @Test
  fun testUnusedStaticMethods() {
    js.fixture.addFileToProject("pages/smth/component1.js",
                               "export async function getStaticProps(context) {\n" +
                               "  return {\n" +
                               "    props: {}, // will be passed to the page component as props\n" +
                               "  }\n" +
                               "}")
    addPackageJsonWithDependencyOnNext()
    js.fixture.testHighlighting("pages/smth/component1.js")
  }

  @Test
  fun testUnusedStaticVariable() {
    js.fixture.addFileToProject("pages/smth/component1.js",
                               "export const getStaticProps = (context) => {\n" +
                               "  return {\n" +
                               "    props: {}, // will be passed to the page component as props\n" +
                               "  }\n" +
                               "}")
    addPackageJsonWithDependencyOnNext()
    js.fixture.testHighlighting("pages/smth/component1.js")
  }

  private fun addPackageJsonWithDependencyOnNext() {
    js.fixture.addFileToProject("package.json", "{\"dependencies\": {\"next\": \"*\"}}")
  }

  @Test
  fun testGroupAndSlotResolving() {
    js.fixture.enableInspections(HtmlUnknownTargetInspection())
    val dir = js.getTestName(true)
    js.fixture.copyDirectoryToProject(dir, "")
    js.fixture.testHighlighting(true, false, true, "app/usage/usage.tsx")
  }

  @Test
  fun testNextjsProject() {
    js.fixture.enableInspections(JSUnusedLocalSymbolsInspection())
    val dir = js.getTestName(true)
    js.fixture.copyDirectoryToProject(dir, "")
    timeoutRunBlocking {
      withContext(Dispatchers.EDT) {
        js.fixture.testHighlightingAllFiles(
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
  }
}