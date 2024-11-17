package org.jetbrains.qodana.jvm.java

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ui.configuration.SdkLookup
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.PlatformUtils
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import kotlin.time.Duration.Companion.minutes

private val LOG = logger<QodanaConfigJdkService>()
private val QODANA_JDK_TIMEOUT = 5.minutes

@Service
class QodanaConfigJdkService {
  val deferredSdk: CompletableDeferred<Sdk?> = CompletableDeferred()

  init {
    if (!PlatformUtils.isQodana()) {
      deferredSdk.complete(null)
    }
  }

  fun configureJdk(config: QodanaConfig) {
    if (deferredSdk.isCompleted) { return }

    val jdkName = config.jvm.projectJDK
    if (jdkName.isNullOrEmpty()) {
      deferredSdk.complete(null)
      return
    }
    setupSdk(jdkName)
  }

  suspend fun getJdk() : Sdk? {
    return withTimeout(QODANA_JDK_TIMEOUT) {
      deferredSdk.await()
    }
  }

  private fun setupSdk(jdkName: String) {
    LOG.info("Setting up JDK '$jdkName' from Qodana config")
    SdkLookup
      .newLookupBuilder()
      .withSdkName(jdkName)
      .withSdkType(JavaSdk.getInstance())
      .onSdkResolved { sdk ->
        if (sdk == null) {
          deferredSdk.completeExceptionally(
            QodanaException("Can't find locally or download required in config JDK '$jdkName'. Check that you specified " +
                            "supported version or mounted JDK compatible with ${SystemInfo.OS_NAME}/${SystemInfo.OS_ARCH}"))
          LOG.info("Setting up JDK '$jdkName' from Qodana config completed with exception")
        }
        else {
          deferredSdk.complete(sdk)
          LOG.info("Setting up JDK '$jdkName' from Qodana config completed")
        }
      }.executeLookup()
  }
}