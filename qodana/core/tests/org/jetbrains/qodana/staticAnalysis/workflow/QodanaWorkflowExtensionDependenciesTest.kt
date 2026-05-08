package org.jetbrains.qodana.staticAnalysis.workflow

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.ExtensionTestUtil
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.QodanaPluginLightTestBase
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import java.nio.file.Paths
import kotlin.reflect.jvm.javaMethod

class QodanaWorkflowExtensionDependenciesTest : QodanaPluginLightTestBase() {
  override fun runInDispatchThread() = false

  fun testImplementsMethodOnlyForEffectiveCallbackOverrides() {
    val afterConfiguration = QodanaWorkflowExtension::afterConfiguration.javaMethod!!
    val beforeProjectOpened = QodanaWorkflowExtension::beforeProjectOpened.javaMethod!!

    assertThat(DefaultWorkflowExtension().implementsMethod(afterConfiguration)).isFalse()
    assertThat(AfterConfigurationWorkflowExtension().implementsMethod(afterConfiguration)).isTrue()
    assertThat(AfterConfigurationWorkflowExtension().implementsMethod(beforeProjectOpened)).isFalse()
    assertThat(InheritedAfterConfigurationWorkflowExtension().implementsMethod(afterConfiguration)).isTrue()
  }

  fun testBeforeProjectOpenedSchedulingIgnoresCyclesFromOtherPhases() {
    runBlocking {
      val executed = ArrayList<String>()
      val capabilityA = QodanaWorkflowCapability("test.after.phase.a")
      val capabilityB = QodanaWorkflowCapability("test.after.phase.b")

      withWorkflowExtensions(
        LoggingBeforeProjectOpenedExtension(executed, "before"),
        AfterConfigurationExtensionWithDependencies(capabilityA, setOf(capabilityB)),
        AfterConfigurationExtensionWithDependencies(capabilityB, setOf(capabilityA)),
      ) {
        callQodanaWorkflowExtensions(QodanaWorkflowExtension::beforeProjectOpened, config())
      }

      assertThat(executed).containsExactly("before")
    }
  }

  fun testBeforeProjectOpenedDependenciesAreResolvedWithinTheSamePhase() {
    runBlocking {
      val executed = ArrayList<String>()
      val capability = QodanaWorkflowCapability("test.before.phase")

      withWorkflowExtensions(
        BeforeProjectOpenedProviderExtension(executed, capability),
        BeforeProjectOpenedDependentExtension(executed, capability),
      ) {
        callQodanaWorkflowExtensions(QodanaWorkflowExtension::beforeProjectOpened, config())
      }

      assertThat(executed).containsExactly("provider", "dependent")
    }
  }

  fun testBeforeProjectOpenedStillFailsOnSamePhaseDependencyCycle() {
    val capabilityA = QodanaWorkflowCapability("test.before.cycle.a")
    val capabilityB = QodanaWorkflowCapability("test.before.cycle.b")

    assertThatThrownBy {
      runBlocking {
        withWorkflowExtensions(
          BeforeProjectOpenedExtensionWithDependencies(capabilityA, setOf(capabilityB)),
          BeforeProjectOpenedExtensionWithDependencies(capabilityB, setOf(capabilityA)),
        ) {
          callQodanaWorkflowExtensions(QodanaWorkflowExtension::beforeProjectOpened, config())
        }
      }
    }.hasMessageContaining("Qodana workflow capability dependencies cannot be resolved")
  }

  private fun config(): QodanaConfig {
    return QodanaConfig.fromYaml(Paths.get("").toAbsolutePath(), Paths.get("unused"))
  }

  private suspend fun withWorkflowExtensions(
    vararg extensions: QodanaWorkflowExtension,
    action: suspend () -> Unit,
  ) {
    val disposable: Disposable = Disposer.newDisposable(testRootDisposable)
    try {
      ExtensionTestUtil.maskExtensions(QodanaWorkflowExtension.EP_NAME, extensions.toList(), disposable)
      action()
    }
    finally {
      Disposer.dispose(disposable)
    }
  }

  private class DefaultWorkflowExtension : QodanaWorkflowExtension

  private class AfterConfigurationWorkflowExtension : QodanaWorkflowExtension {
    override suspend fun afterConfiguration(config: QodanaConfig, project: Project) {
    }
  }

  private open class BaseAfterConfigurationWorkflowExtension : QodanaWorkflowExtension {
    override suspend fun afterConfiguration(config: QodanaConfig, project: Project) {
    }
  }

  private class InheritedAfterConfigurationWorkflowExtension : BaseAfterConfigurationWorkflowExtension()

  private class LoggingBeforeProjectOpenedExtension(
    private val executed: MutableList<String>,
    private val name: String,
  ) : QodanaWorkflowExtension {
    override suspend fun beforeProjectOpened(config: QodanaConfig) {
      executed.add(name)
    }
  }

  private class BeforeProjectOpenedProviderExtension(
    private val executed: MutableList<String>,
    private val capability: QodanaWorkflowCapability,
  ) : QodanaWorkflowExtension {
    override val provides: Set<QodanaWorkflowCapability>
      get() = setOf(capability)

    override suspend fun beforeProjectOpened(config: QodanaConfig) {
      executed.add("provider")
    }
  }

  private class BeforeProjectOpenedDependentExtension(
    private val executed: MutableList<String>,
    private val capability: QodanaWorkflowCapability,
  ) : QodanaWorkflowExtension {
    override val dependsOn: Set<QodanaWorkflowCapability>
      get() = setOf(capability)

    override suspend fun beforeProjectOpened(config: QodanaConfig) {
      executed.add("dependent")
    }
  }

  private class BeforeProjectOpenedExtensionWithDependencies(
    private val providedCapability: QodanaWorkflowCapability,
    private val dependencies: Set<QodanaWorkflowCapability>,
  ) : QodanaWorkflowExtension {
    override val provides: Set<QodanaWorkflowCapability>
      get() = setOf(providedCapability)

    override val dependsOn: Set<QodanaWorkflowCapability>
      get() = dependencies

    override suspend fun beforeProjectOpened(config: QodanaConfig) {
    }
  }

  private class AfterConfigurationExtensionWithDependencies(
    private val providedCapability: QodanaWorkflowCapability,
    private val dependencies: Set<QodanaWorkflowCapability>,
  ) : QodanaWorkflowExtension {
    override val provides: Set<QodanaWorkflowCapability>
      get() = setOf(providedCapability)

    override val dependsOn: Set<QodanaWorkflowCapability>
      get() = dependencies

    override suspend fun afterConfiguration(config: QodanaConfig, project: Project) {
    }
  }
}
