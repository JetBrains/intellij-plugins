package org.jetbrains.qodana.staticAnalysis

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.util.Disposer
import org.jetbrains.annotations.TestOnly
import kotlin.properties.ReadOnlyProperty

fun qodanaEnv(): QodanaEnv {
  return QodanaEnvService.getInstance().env
}

@TestOnly
fun addQodanaEnvMock(disposable: Disposable, env: QodanaEnvEmpty) {
  (QodanaEnvService.getInstance() as? QodanaEnvServiceTestImpl)?.addEnv(disposable, env) ?: error("Must invoke only in test")
}

@Suppress("PropertyName")
interface QodanaEnv {
  class KeyAndValue(
    val key: String,
    val value: String?
  )

  /** Cloud backend, deprecated */
  val ENDPOINT: KeyAndValue

  /** Cloud frontend for self-hosted */
  val QODANA_ENDPOINT: KeyAndValue

  /** Token for uploading data to cloud */
  val QODANA_TOKEN: KeyAndValue

  /** The branch name of the repository analyzed by Qodana, e.g. `refs/heads/main`. */
  val QODANA_BRANCH: KeyAndValue

  /** The SHA of the current commit analyzed by Qodana, e.g. `f0c8b8f`. */
  val QODANA_REVISION: KeyAndValue

  /** The https URL to the repository analyzed by Qodana, e.g. `https://github.com/JetBrains/qodana-action`. */
  val QODANA_REPO_URL: KeyAndValue

  /** The ssh/etc. URI to the repository analyzed by Qodana, e.g. `ssh://.../qodana-action`. */
  val QODANA_REMOTE_URL: KeyAndValue

  val QODANA_JOB_URL: KeyAndValue
  val QODANA_REPORT_ID: KeyAndValue
  val QODANA_PROJECT_ID: KeyAndValue

  /** environment variable that describes the environment in which Qodana runs, e.g. `teamcity:2022.04:12345`. */
  val QODANA_ENV: KeyAndValue

  val QODANA_DISABLE_COLLECT_CONTEXT: KeyAndValue
}

private class QodanaEnvImpl : QodanaEnv {
  override val ENDPOINT by env()
  override val QODANA_ENDPOINT by env()
  override val QODANA_TOKEN by env()
  override val QODANA_BRANCH by env()
  override val QODANA_REVISION by env()
  override val QODANA_REPO_URL by env()
  override val QODANA_REMOTE_URL by env()
  override val QODANA_JOB_URL by env()
  override val QODANA_REPORT_ID by env()
  override val QODANA_PROJECT_ID by env()
  override val QODANA_ENV by env()
  override val QODANA_DISABLE_COLLECT_CONTEXT by env()

  private fun env(): ReadOnlyProperty<Any?, QodanaEnv.KeyAndValue> = ReadOnlyProperty { _, property ->
    val key = property.name
    return@ReadOnlyProperty QodanaEnv.KeyAndValue(key, System.getenv(key))
  }
}

@TestOnly
abstract class QodanaEnvEmpty : QodanaEnv {
  override val ENDPOINT by empty()
  override val QODANA_ENDPOINT by empty()
  override val QODANA_TOKEN by empty()
  override val QODANA_BRANCH by empty()
  override val QODANA_REVISION by empty()
  override val QODANA_REPO_URL by empty()
  override val QODANA_REMOTE_URL by empty()
  override val QODANA_JOB_URL by empty()
  override val QODANA_REPORT_ID by empty()
  override val QODANA_PROJECT_ID by empty()
  override val QODANA_ENV by empty()
  override val QODANA_DISABLE_COLLECT_CONTEXT by empty()

  protected fun value(value: String): ReadOnlyProperty<Any?, QodanaEnv.KeyAndValue> = ReadOnlyProperty { _, property ->
    val key = property.name
    return@ReadOnlyProperty QodanaEnv.KeyAndValue(key, value)
  }

  private fun empty(): ReadOnlyProperty<Any?, QodanaEnv.KeyAndValue> = ReadOnlyProperty { _, property ->
    val key = property.name
    return@ReadOnlyProperty QodanaEnv.KeyAndValue(key, null)
  }
}

private interface QodanaEnvService {
  companion object {
    fun getInstance(): QodanaEnvService = service()
  }

  val env: QodanaEnv
}

private class QodanaEnvServiceImpl : QodanaEnvService {
  override val env: QodanaEnv = QodanaEnvImpl()
}

private class QodanaEnvServiceTestImpl : QodanaEnvService {
  private val envs = mutableListOf<QodanaEnv>()

  fun addEnv(disposable: Disposable, env: QodanaEnv) {
    envs.add(env)
    Disposer.register(disposable) {
      envs.removeIf { it === env }
    }
  }

  override val env: QodanaEnv
    get() = object : QodanaEnv {
      override val ENDPOINT by latestEnvWithValue { ENDPOINT }
      override val QODANA_ENDPOINT by latestEnvWithValue { QODANA_ENDPOINT }
      override val QODANA_TOKEN by latestEnvWithValue { QODANA_TOKEN }
      override val QODANA_BRANCH by latestEnvWithValue { QODANA_BRANCH }
      override val QODANA_REVISION by latestEnvWithValue { QODANA_REVISION }
      override val QODANA_REPO_URL by latestEnvWithValue { QODANA_REPO_URL }
      override val QODANA_REMOTE_URL by latestEnvWithValue { QODANA_REMOTE_URL }
      override val QODANA_JOB_URL by latestEnvWithValue { QODANA_JOB_URL }
      override val QODANA_REPORT_ID by latestEnvWithValue { QODANA_REPORT_ID }
      override val QODANA_PROJECT_ID by latestEnvWithValue { QODANA_PROJECT_ID }
      override val QODANA_ENV by latestEnvWithValue { QODANA_ENV }
      override val QODANA_DISABLE_COLLECT_CONTEXT by latestEnvWithValue { QODANA_DISABLE_COLLECT_CONTEXT }
    }

  private fun latestEnvWithValue(
    value: QodanaEnv.() -> QodanaEnv.KeyAndValue
  ): ReadOnlyProperty<Any?, QodanaEnv.KeyAndValue> = ReadOnlyProperty { _, property ->
    for (env in envs.asReversed()) {
      val pair = env.value()
      if (pair.value != null) {
        return@ReadOnlyProperty pair
      }
    }
    return@ReadOnlyProperty QodanaEnv.KeyAndValue(property.name, null)
  }
}
