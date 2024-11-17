package org.jetbrains.idea.perforce.perforce.connections

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.SystemProperties
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.idea.perforce.perforce.PerforceSettings
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.io.FileReader
import javax.xml.parsers.SAXParserFactory

@ApiStatus.Experimental
interface P4ConnectionParametersProvider {
  companion object{
    @JvmField
    val EP_NAME = ExtensionPointName.create<P4ConnectionParametersProvider>("Perforce.P4ConnectionParametersProvider");
  }
  @RequiresBackgroundThread
  fun getConnectionParameters(project: Project): Set<P4ConnectionParameters>
}

internal class SettingsConnectionParametersProvider : P4ConnectionParametersProvider {
  override fun getConnectionParameters(project: Project): Set<P4ConnectionParameters> {
    val settings = PerforceSettings.getSettings(project)

    return if (!settings.isConnectionSettingsDefined()) emptySet()
    else setOf(P4ConnectionParameters().apply {
      client = settings.client
      server = settings.port
      user = settings.user
      if (!settings.isNoneCharset) {
        charset = settings.CHARSET
      }
    })
  }

  private fun PerforceSettings.isConnectionSettingsDefined(): Boolean {
    return !useP4CONFIG &&
           (!port.isNullOrBlank() || !client.isNullOrBlank() || !user.isNullOrBlank())
  }
}

internal class EnvConnectionParametersProvider : P4ConnectionParametersProvider {
  override fun getConnectionParameters(project: Project): Set<P4ConnectionParameters> {
    val defaultParams = P4EnvHelper.getConfigHelper(project).defaultParams

    return if (defaultParams.hasProblems()) emptySet() else setOf(defaultParams)
  }
}

@ApiStatus.Internal
class HelixClientConnectionParametersProvider(private val configFiles: ConfigFiles = DefaultConfigFiles()) : P4ConnectionParametersProvider {

  companion object {
    private val LOG = logger<HelixClientConnectionParametersProvider>()
    private const val CONNECTIONMAP_FILE_NAME = "connectionmap.xml"
  }

  interface ConfigFiles {
    fun getClientConfig(): File
    fun getAdminConfig(): File
  }

  class DefaultConfigFiles : ConfigFiles {
    private fun getUserHomeDir() = FileUtil.toSystemIndependentName(FileUtil.toCanonicalPath(SystemProperties.getUserHome(), true))
    override fun getClientConfig() = "${getUserHomeDir()}/.p4qt/$CONNECTIONMAP_FILE_NAME".let(::File)
    override fun getAdminConfig() = "${getUserHomeDir()}/.p4admin/$CONNECTIONMAP_FILE_NAME".let(::File)
  }

  override fun getConnectionParameters(project: Project): Set<P4ConnectionParameters> {
    val result = linkedSetOf<P4ConnectionParameters>()
    val clientConfig = configFiles.getClientConfig()
    if (clientConfig.exists()) {
      result.addAll(clientConfig.parseConnectionParameters())
    }
    val adminConfig = configFiles.getAdminConfig()
    if (adminConfig.exists()) {
      result.addAll(adminConfig.parseConnectionParameters())
    }

    return result.toSet()
  }

  private fun File.parseConnectionParameters(): List<P4ConnectionParameters> {
    val factory = SAXParserFactory.newInstance()
    val saxParser = factory.newSAXParser()
    val handler = ConnectionMapParseHandler()
    try {
      FileReader(this).use { reader -> saxParser.parse(InputSource(reader), handler) }
    }
    catch (e: Exception) {
      LOG.warn("Unable to parse $this", e)
    }
    return handler.getConnectionMapList()?.connectionMap ?: emptyList()
  }

  private class ConnectionMapParseHandler : DefaultHandler() {
    private var connectionMapList: ConnectionMapList? = null
    private var tempConnectionMapList = mutableListOf<P4ConnectionParameters>()
    private var tempConnectionMap: P4ConnectionParameters? = null
    private var tempValue: String? = null

    fun getConnectionMapList(): ConnectionMapList? {
      return connectionMapList
    }

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
      when (qName) {
        "ConnectionMapList" -> connectionMapList = ConnectionMapList(tempConnectionMapList)
        "ConnectionMap" -> tempConnectionMap = P4ConnectionParameters().apply { isNoConfigFound = true }
      }
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
      when (qName) {
        "User" -> tempConnectionMap?.let { it.user = tempValue }
        "P4Port" -> tempConnectionMap?.let { it.server = tempValue }
        "ConnectionMap" -> tempConnectionMap?.let { tempConnectionMaps -> tempConnectionMapList.add(tempConnectionMaps) }
      }
      tempValue = ""
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
      tempValue = ch?.let { String(it, start, length) }
    }
  }

  private data class ConnectionMapList(val connectionMap: List<P4ConnectionParameters>?)
}
