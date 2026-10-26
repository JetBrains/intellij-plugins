package org.jetbrains.qodana

import com.intellij.openapi.Disposable
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightModuleFixture
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.junit5.fixture.testFixture
import org.jetbrains.annotations.TestOnly

/**
 * The test data directory of the Qodana tests, relative to the home path.
 */
const val QODANA_TEST_DATA_BASE_PATH: String = "/contrib/qodana/core/test-data"

/**
 * Creates a [QodanaPluginLightFixture] on a new module of [projectFixture].
 * It is the JUnit 5 replacement of the JUnit 3 base `QodanaPluginLightTestBase`.
 * Declare the result as an instance property, so that each test gets its own fixture.
 * Declare [projectFixture] as a static field, so that the tests of the class share the project.
 * [basePath] replaces an override of `QodanaPluginLightTestBase.getBasePath()`. It is relative to the home path.
 */
@TestOnly
fun qodanaPluginLightFixture(
  projectFixture: TestFixture<Project>,
  basePath: String = QODANA_TEST_DATA_BASE_PATH,
): TestFixture<QodanaPluginLightFixture> = testFixture("qodanaPluginLightFixture") { context ->
  val fixture = codeInsightModuleFixture(projectFixture).init()
  fixture.testDataPath = IdeaTestExecutionPolicy.getHomePathWithPolicy().trimEnd('/') + "/" + basePath.trimStart('/')
  val result = QodanaPluginLightFixture(fixture, context.extensionContext.requiredTestMethod.name, QodanaPluginTestImpl())
  result.init({ fixture }, { fixture.testRootDisposable })
  initialized(result) {
    result.tearDownQodanaTest()
  }
}

/**
 * The state and the helpers of `QodanaPluginLightTestBase` for a JUnit 5 test.
 * The [QodanaPluginTest] members and extensions work on this fixture, for example `reinstansiateService` and `assertNoNotifications`.
 * Call a member from any thread, unless its KDoc says otherwise.
 */
@TestOnly
class QodanaPluginLightFixture internal constructor(
  val fixture: CodeInsightTestFixture,
  /** The name of the test method. It replaces `TestCase.getName()`. */
  val testMethodName: String,
  impl: QodanaPluginTestImpl,
) : QodanaPluginTest by impl {
  val project: Project get() = fixture.project

  val module: Module get() = fixture.module

  /** The disposable of the test. The fixture disposes it after the `@AfterEach` methods. */
  val testRootDisposable: Disposable get() = fixture.testRootDisposable

  /** Returns the test name as `UsefulTestCase.getTestName` does. */
  fun getTestName(lowercaseFirstLetter: Boolean): String = PlatformTestUtil.getTestName(testMethodName, lowercaseFirstLetter)
}
