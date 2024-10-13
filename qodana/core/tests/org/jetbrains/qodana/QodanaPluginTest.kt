package org.jetbrains.qodana

import com.intellij.notification.Notification
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.ComponentManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.QodanaDispatchersProviderTestImpl
import org.jetbrains.qodana.report.ReportDescriptor
import java.nio.file.Path


interface QodanaPluginTest {
  companion object {
    /**
     * Directory in test project with additional data for tests
     * (expected files from checkHighlighting and checkCaretAndSelection are copied there)
     */
    const val ADDITIONAL_DATA_FOR_TEST_DIR = "additionalTestData"
  }

  val scope: CoroutineScope

  val projectDir: VirtualFile?

  val sarifTestReports: SarifTestReportsProvider

  val dispatchersProvider: QodanaDispatchersProviderTestImpl?

  /** Must be called in setUp method */
  fun init(fixtureProvider: () -> CodeInsightTestFixture, testRootDisposableProvider: () -> Disposable)

  fun tearDownQodanaTest()

  /** Copy project data from testdata, set [projectDir] */
  fun copyProjectTestData(projectTestDataPath: String): VirtualFile

  fun loadAdditionalTestDataFile(pathToAdditionalTestDataFile: String): VirtualFile

  fun <T : Any> reinstansiateService(projectOrApplication: ComponentManager, serviceInterface: Class<T>, instance: T)

  suspend fun interceptNotifications(action: suspend () -> Unit): List<Notification>
}

inline fun <reified T : Any> QodanaPluginTest.reinstansiateService(projectOrApplication: ComponentManager, instance: T) =
  reinstansiateService(projectOrApplication, T::class.java, instance)

suspend fun QodanaPluginTest.assertNoNotifications(action: suspend () -> Unit) {
  val notifications = interceptNotifications(action)
  assertThat(notifications).isEmpty()
}

suspend fun QodanaPluginTest.assertSingleNotificationWithMessage(message: String, action: suspend () -> Unit) {
  val notifications = interceptNotifications(action)
  assertThat(notifications.map { it.content }).singleElement().isEqualTo(message)
}

fun QodanaPluginTest.allowExceptions(block: () -> Unit): List<Throwable> {
  dispatchersProvider?.enableExceptionsHandling()
  block()
  dispatchersProvider?.disableExceptionHandling()
  val handledExceptions = dispatchersProvider?.handledExceptions?.toList() ?: emptyList()
  dispatchersProvider?.resetHandledExceptions()
  return handledExceptions
}

fun QodanaPluginTest.assertNoUnhandledExceptions() {
  val exceptionsCount = dispatchersProvider?.exceptionsCount ?: 0
  assertEquals(0, exceptionsCount)
}

suspend fun assertReportIsAvailableSignalsCount(reportDescriptor: ReportDescriptor, expected: Int, block: suspend () -> Unit) {
  coroutineScope {
    var actual = 0
    val job = launch(QodanaDispatchers.Ui) {
      reportDescriptor.isReportAvailableFlow.collect { actual++ }
    }
    block()
    dispatchAllTasksOnUi()
    job.cancelAndJoin()
    BasePlatformTestCase.assertEquals(expected, actual)
  }
}

interface SarifTestReportsProvider {
  val valid1: Path

  val valid2: Path

  val invalidJsonStructure: Path

  val emptyRuns: Path

  val noResults: Path

  val noRuns: Path

  val noTool: Path

  val notExisting: Path

  val validForConverter: Path
}