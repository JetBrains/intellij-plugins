package com.intellij.aws.cloudformation

import com.intellij.ide.DataManager
import com.intellij.ide.plugins.PluginManager
import com.intellij.idea.IdeaLogger
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.ex.ApplicationInfoEx
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.Consumer
import com.intellij.util.io.HttpRequests
import com.intellij.util.text.DateFormatUtil
import java.awt.Component
import java.net.HttpURLConnection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID


class CloudFormationReportSubmitter : ErrorReportSubmitter() {
  val LOG = Logger.getInstance(CloudFormationReportSubmitter::class.java)
  val ENDPOINT = "https://classiq.diverse.org.ru/dav_feedback"

  override fun getReportActionText(): String = "Report to plugin developers"

  fun getExceptionText(event: IdeaLoggingEvent): String {
    val throwable = event.throwable

    return (if (throwable.javaClass != Throwable::class.java) (throwable.javaClass.simpleName + ": ") else "") +
        event.message +
        (if (throwable.message != event.message && throwable.message != null) (" (${throwable.message})") else "") + "\n" + StringUtil.getThrowableText(throwable)
  }

  private fun buildReportText(events: List<IdeaLoggingEvent>): String {
    val builder = StringBuilder()

    val appInfo = ApplicationInfoEx.getInstanceEx()
    val appNamesInfo = ApplicationNamesInfo.getInstance()
    val application = ApplicationManager.getApplication()

    // Product coordinates
    builder.appendln("JetBrains ${appNamesInfo.fullProductName} ${appInfo.majorVersion}.${appInfo.minorVersion} Build ${appInfo.apiVersion}")

    // Plugin coordinates
    val pluginId = PluginManager.getPluginByClassName(CloudFormationReportSubmitter::class.java.name)
    val plugin = PluginManager.getPlugin(pluginId)
    if (plugin != null) {
      builder.appendln("Plugin Name: ${plugin.name}")
      builder.appendln("Plugin Version: ${plugin.version}")
    }

    // Stacktrace
    events.forEach {
      builder.appendln("----------------------")
      builder.appendln(getExceptionText(it))
    }

    builder.appendln("----------------------")

    // Additional info
    builder.appendln()
    builder.appendln("last.action = ${IdeaLogger.ourLastActionId}")

    builder.appendln()
    builder.appendln("app.eap = ${appInfo.isEAP}")
    builder.appendln("app.internal = ${application.isInternal}")
    builder.appendln("app.build = ${appInfo.apiVersion}")
    builder.appendln("app.version.major = ${appInfo.majorVersion}")
    builder.appendln("app.version.minor = ${appInfo.minorVersion}")
    builder.appendln("app.build.date = ${DateFormatUtil.getIso8601Format().format(appInfo.buildDate.time)}")
    builder.appendln("app.build.date.release = ${DateFormatUtil.getIso8601Format().format(appInfo.majorReleaseBuildDate.time)}")
    builder.appendln("app.build.date.release = ${DateFormatUtil.getIso8601Format().format(appInfo.majorReleaseBuildDate.time)}")
    builder.appendln("app.compilation.timestamp = ${IdeaLogger.getOurCompilationTimestamp()}")
    builder.appendln("app.product.code = ${appInfo.build.productCode}")

    builder.appendln()
    builder.appendln("os.name = ${SystemInfo.getOsNameAndVersion()}")
    builder.appendln("java.version = ${SystemInfo.JAVA_VERSION}")

    return builder.toString()
  }

  override fun submit(events: Array<out IdeaLoggingEvent>, additionalInfo: String?, parentComponent: Component, consumer: Consumer<SubmittedReportInfo>): Boolean {
    val dataContext = DataManager.getInstance().getDataContext(parentComponent)
    val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return false

    val now = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm")

    val reportId = now.format(formatter) + "-" + UUID.randomUUID().toString()

    val reportedText = buildReportText(events.toList())
    val reportBytes = reportedText.toByteArray()

    val task = object : Task.Backgroundable(project, "Submitting exceptions", true, ALWAYS_BACKGROUND) {
      override fun run(indicator: ProgressIndicator) {
        try {
          HttpRequests.request("$ENDPOINT/$reportId")
              .tuner {
                val con = it as HttpURLConnection

                con.requestMethod = "PUT"
                con.doOutput = true
                con.useCaches = false
                con.allowUserInteraction = false
                con.setRequestProperty("Content-Type", "text/xml")
                con.setFixedLengthStreamingMode(reportBytes.size)
              }.connect {
                val os = it.connection.outputStream
                os.write(reportBytes)
                os.close()

                val response = it.connection.inputStream.readBytes()
                try {
                  LOG.info("Reporting endpoint answered: ${response.toString(Charsets.UTF_8)}")
                } catch(t: Throwable) {
                  LOG.error(t)
                }
              }

          val reportInfo = SubmittedReportInfo(null, "Submitted an Issue", SubmittedReportInfo.SubmissionStatus.NEW_ISSUE)
          consumer.consume(reportInfo)
        } catch (t: Throwable) {
          try {
            LOG.error("Submitting feedback error", t)
          } catch (t: Throwable) {
            LOG.error(t)
          }
        }
      }
    }

    ProgressManager.getInstance().run(task)

    return true
  }
}