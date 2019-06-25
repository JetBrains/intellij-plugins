// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
@file:JvmName("AngularCliConfigLoader")

package org.angular2.cli

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.text.CharSequenceReader
import java.io.IOException


private val ANGULAR_CLI_CONFIG_KEY = Key.create<CachedValue<AngularCliConfig>>("ANGULAR_CLI_CONFIG_KEY")
private val LOG = Logger.getInstance("#org.angularjs.cli.AngularCliConfigLoader")

fun load(project: Project, context: VirtualFile): AngularCliConfig {
  val angularCliJson = AngularCliUtil.findAngularCliFolder(project, context)?.let {
    AngularCliUtil.findCliJson(it)
  } ?: return AngularCliEmptyConfig()
  val psiFile = PsiManager.getInstance(project).findFile(angularCliJson) ?: return AngularCliEmptyConfig()
  return CachedValuesManager.getManager(project).getCachedValue(psiFile, ANGULAR_CLI_CONFIG_KEY, {
    val cachedDocument = FileDocumentManager.getInstance().getCachedDocument(angularCliJson)
    val config =
      try {
        AngularCliJsonFileConfig(
          angularCliJson, cachedDocument?.charsSequence ?: VfsUtilCore.loadText(angularCliJson))
      }
      catch (e: Exception) {
        LOG.warn("Cannot load " + angularCliJson.name + ": " + e.message, e)
        AngularCliEmptyConfig()
      }
    CachedValueProvider.Result.create(config, cachedDocument ?: angularCliJson)
  }, false)
}

interface AngularCliConfig {

  fun getIndexHtmlFile(): VirtualFile?

  fun getGlobalStyleSheets(): Collection<VirtualFile>

  /**
   * @return root folders according to apps -> root in .angular-cli.json; usually it is a single 'src' folder.
   */
  fun getRootDirs(): Collection<VirtualFile>

  /**
   * @return folders that are precessed as root folders by style preprocessor according to apps -> stylePreprocessorOptions -> includePaths in .angular-cli.json
   */
  fun getStylePreprocessorIncludeDirs(): Collection<VirtualFile>

  fun getKarmaConfigFile(): VirtualFile?

  fun getProtractorConfigFile(): VirtualFile?

  fun exists(): Boolean

}

private class AngularCliEmptyConfig : AngularCliConfig {
  override fun getIndexHtmlFile(): VirtualFile? = null

  override fun getGlobalStyleSheets(): Collection<VirtualFile> = emptyList()

  override fun getRootDirs(): Collection<VirtualFile> = emptyList()

  override fun getStylePreprocessorIncludeDirs(): Collection<VirtualFile> = emptyList()

  override fun getKarmaConfigFile(): VirtualFile? = null

  override fun getProtractorConfigFile(): VirtualFile? = null

  override fun exists(): Boolean = false

}

private class AngularCliJsonFileConfig(angularCliJson: VirtualFile, text: CharSequence) : AngularCliConfig {

  private val myAngularCliJson: VirtualFile = angularCliJson
  private val myRootPaths: List<String>
  private val myStylePreprocessorIncludePaths: List<String>
  private val myKarmaConfigPath: String?
  private val myProtractorConfigPath: String?
  private val myIndexHtmlPath: String?
  private val myStyles: List<String>?

  init {
    val mapper = ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    val ngCliConfig = mapper.readValue(CharSequenceReader(text), AngularCli::class.java)
    val allProjects = ContainerUtil.concat(ngCliConfig.apps, ngCliConfig.projects.values.toList())
    myRootPaths = allProjects.mapNotNull { it.rootPath }.fold(ArrayList()) { acc, root -> acc.add(root); acc; }
    myStylePreprocessorIncludePaths = allProjects.mapNotNull {
      (it.targets?.build?.options?.stylePreprocessorOptions ?: it.stylePreprocessorOptions)?.includePaths
    }.fold(ArrayList()) { acc, list -> acc.addAll(list); acc; }
    myKarmaConfigPath = allProjects.mapNotNull { it.targets?.test?.options?.karmaConfig }.firstOrNull()
    myProtractorConfigPath = allProjects.mapNotNull { it.targets?.e2e?.options?.protractorConfig }.firstOrNull()
    myIndexHtmlPath = allProjects.mapNotNull { it.targets?.build?.options?.index ?: it.index }.firstOrNull()
    myStyles = allProjects.mapNotNull { it.targets?.build?.options?.styles ?: it.styles }.firstOrNull()
  }

  private fun resolveFile(filePath: String?): VirtualFile? {
    return myAngularCliJson.parent.findFileByRelativePath(filePath ?: return null)
           ?: getRootDirs().mapNotNull { it.findFileByRelativePath(filePath) }.firstOrNull()
  }

  override fun getIndexHtmlFile(): VirtualFile? {
    return resolveFile(myIndexHtmlPath)
  }

  override fun getGlobalStyleSheets(): Collection<VirtualFile> {
    return (myStyles ?: return emptyList()).mapNotNull { resolveFile(it) }
  }

  override fun getRootDirs(): Collection<VirtualFile> {
    val angularCliFolder = myAngularCliJson.parent
    return myRootPaths.mapNotNull { s -> angularCliFolder.findFileByRelativePath(s) }
  }

  override fun getStylePreprocessorIncludeDirs(): Collection<VirtualFile> {
    val angularCliFolder = myAngularCliJson.parent
    val result = ArrayList<VirtualFile>(myRootPaths.size * myStylePreprocessorIncludePaths.size)
    for (rootPath in myRootPaths) {
      for (includePath in myStylePreprocessorIncludePaths) {
        ContainerUtil.addIfNotNull(result, angularCliFolder.findFileByRelativePath("$rootPath/$includePath"))
      }
    }
    return result
  }

  override fun getKarmaConfigFile(): VirtualFile? {
    return myAngularCliJson.parent.findFileByRelativePath(myKarmaConfigPath ?: return null)
  }

  override fun getProtractorConfigFile(): VirtualFile? {
    return myAngularCliJson.parent.findFileByRelativePath(myProtractorConfigPath ?: return null)
  }

  override fun exists(): Boolean = true

}

private class AngularCli {
  @JsonProperty("apps")
  val apps: List<AngularCliProject> = ArrayList()

  @JsonProperty("projects")
  val projects: Map<String, AngularCliProject> = HashMap()
}

private open class AngularCliBuildOptionsBase {

  @JsonProperty("stylePreprocessorOptions")
  val stylePreprocessorOptions: AngularCliStylePreprocessorOptions? = null

  @JsonProperty("index")
  val index: String? = null

  @JsonProperty("styles")
  @JsonDeserialize(using = StringOrObjectWithInputDeserializer::class)
  val styles: List<String>? = null

}

private class AngularCliProject : AngularCliBuildOptionsBase() {
  @JsonProperty("root")
  val rootPath: String? = null

  @JsonProperty("targets")
  @JsonAlias(value = ["architect"])
  val targets: AngularCliTargets? = null

}

private class AngularCliTargets {
  @JsonProperty("build")
  val build: AngularCliBuild? = null

  @JsonProperty("test")
  val test: AngularCliTest? = null

  @JsonProperty("e2e")
  val e2e: AngularCliE2E? = null
}

private class AngularCliE2E {
  @JsonProperty("options")
  val options: AngularCliE2EOptions? = null
}

private class AngularCliE2EOptions {
  @JsonProperty("protractorConfig")
  val protractorConfig: String? = null
}

private class AngularCliTest {
  @JsonProperty("options")
  val options: AngularCliTestOptions? = null
}

private class AngularCliTestOptions {
  @JsonProperty("karmaConfig")
  val karmaConfig: String? = null
}

private class AngularCliBuild {
  @JsonProperty("options")
  val options: AngularCliBuildOptions? = null
}

private class AngularCliBuildOptions : AngularCliBuildOptionsBase()

private class AngularCliStylePreprocessorOptions {
  @JsonProperty("includePaths")
  val includePaths: List<String> = ArrayList()
}

private class StringOrObjectWithInputDeserializer : JsonDeserializer<List<String>>() {

  @Throws(IOException::class)
  override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): List<String> {
    val files = mutableListOf<String>()
    if (jsonParser.currentToken === JsonToken.START_ARRAY) {
      while (jsonParser.nextToken() !== JsonToken.END_ARRAY) {
        when (jsonParser.currentToken) {
          JsonToken.START_OBJECT -> while (jsonParser.nextToken() !== JsonToken.END_OBJECT) {
            assert(jsonParser.currentToken === JsonToken.FIELD_NAME)
            val propName = jsonParser.currentName
            jsonParser.nextToken()
            if (propName == "input") {
              files.add(jsonParser.valueAsString)
            }
            else {
              jsonParser.skipChildren()
            }
          }
          JsonToken.VALUE_STRING -> files.add(jsonParser.valueAsString)
          else -> deserializationContext.handleUnexpectedToken(String::class.java, jsonParser)
        }
      }
    }
    else {
      deserializationContext.handleUnexpectedToken(List::class.java, jsonParser)
    }
    return files
  }
}
