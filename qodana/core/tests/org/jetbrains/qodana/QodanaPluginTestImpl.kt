package org.jetbrains.qodana

import com.intellij.notification.Notification
import com.intellij.notification.Notifications
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.components.ComponentManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.util.coroutines.childScope
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy
import com.intellij.testFramework.replaceService
import com.intellij.util.application
import kotlinx.coroutines.*
import org.jetbrains.qodana.cloud.api.IjQDCloudClientProvider
import org.jetbrains.qodana.cloud.api.IjQDCloudClientProviderTestImpl
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.QodanaDispatchersProviderTestImpl
import kotlin.io.path.Path
import kotlin.io.path.pathString
import kotlin.time.Duration.Companion.minutes

class QodanaPluginTestImpl : QodanaPluginTest {
  private lateinit var fixtureProvider: () -> CodeInsightTestFixture
  private val myFixture: CodeInsightTestFixture get() = fixtureProvider.invoke()

  private lateinit var testRootDisposableProvider: () -> Disposable
  private val testRootDisposable: Disposable get() = testRootDisposableProvider.invoke()

  private lateinit var qodanaTestDisposable: Disposable

  override var projectDir: VirtualFile? = null

  @OptIn(DelicateCoroutinesApi::class)
  override val scope = GlobalScope.childScope("qodana tests")

  override val sarifTestReports: SarifTestReportsProvider
    get() {
      val pluginTestDataPath = Path(IdeaTestExecutionPolicy.getHomePathWithPolicy(), "/contrib/qodana/core/test-data")
      val sarifDir = pluginTestDataPath.resolve("projectSamples/simpleProjectWithSarifs")

      return object : SarifTestReportsProvider {
        override val valid1 = sarifDir.resolve("report1.sarif.json")
        override val valid2 = sarifDir.resolve("report2.sarif.json")
        override val invalidJsonStructure = sarifDir.resolve("reportInvalidJsonStructure.sarif.json")
        override val emptyRuns = sarifDir.resolve("reportEmptyRuns.sarif.json")
        override val noResults = sarifDir.resolve("reportNoResults.sarif.json")
        override val noRuns = sarifDir.resolve("reportNoRuns.sarif.json")
        override val noTool = sarifDir.resolve("reportNoTool.sarif.json")
        override val notExisting = sarifDir.resolve("notExisting.sarif.json")
        override val validForConverter = sarifDir.resolve("reportValidForConverter.sarif.json")
      }
    }

  override val dispatchersProvider get() = QodanaDispatchers as? QodanaDispatchersProviderTestImpl

  override fun init(
    fixtureProvider: () -> CodeInsightTestFixture,
    testRootDisposableProvider: () -> Disposable
  ) {
    this.fixtureProvider = fixtureProvider
    this.testRootDisposableProvider = testRootDisposableProvider
    this.qodanaTestDisposable = Disposer.newDisposable(testRootDisposable)
    dispatchersProvider?.resetHandledExceptionsState()
    reinstansiateService(application, IjQDCloudClientProvider::class.java, IjQDCloudClientProviderTestImpl())
  }

  override fun tearDownQodanaTest() {
    runBlocking {
      val job = scope.coroutineContext.job
      job.cancel()
      invokeAndWaitIfNeeded {
        dispatchAllTasksOnUi()
      }
      withTimeout(1.minutes) {
        job.join()
      }
    }
    assertNoUnhandledExceptions()
    Disposer.dispose(qodanaTestDisposable)
  }

  override fun copyProjectTestData(projectTestDataPath: String): VirtualFile {
    projectDir = myFixture.copyDirectoryToProject(projectTestDataPath, "")
    return projectDir!!
  }

  override fun loadAdditionalTestDataFile(pathToAdditionalTestDataFile: String): VirtualFile {
    if (projectDir == null) throw IllegalStateException("Project directory must be configured by calling copyProjectTestData")

    val destinationPath = Path(QodanaPluginTest.ADDITIONAL_DATA_FOR_TEST_DIR, pathToAdditionalTestDataFile).pathString
    val alreadyExistingFile = projectDir!!.findFileByRelativePath(destinationPath)
    runWriteActionAndWait { alreadyExistingFile?.delete(null) }

    return myFixture.copyFileToProject(pathToAdditionalTestDataFile, destinationPath)
  }

  override fun <T : Any> reinstansiateService(projectOrApplication: ComponentManager, serviceInterface: Class<T>, instance: T) {
    (instance as? Disposable)?.let { Disposer.register(qodanaTestDisposable, it) }
    projectOrApplication.replaceService(serviceInterface, instance, qodanaTestDisposable)
  }

  override suspend fun interceptNotifications(action: suspend () -> Unit): List<Notification> {
    val collectedNotifications = mutableListOf<Notification>()

    val listenerDisposable = Disposer.newDisposable(qodanaTestDisposable, "notifications listener")
    myFixture.project.messageBus.connect(listenerDisposable).subscribe(Notifications.TOPIC, object : Notifications {
      override fun notify(notification: Notification) {
        collectedNotifications.add(notification)
      }
    })
    action.invoke()
    Disposer.dispose(listenerDisposable)

    return collectedNotifications
  }
}