// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
@file:JvmName("AngularCliConfigLoader")

package org.angular2.cli

import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
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

private val ANGULAR_CLI_CONFIG_KEY = Key.create<CachedValue<AngularCliConfig>>("ANGULAR_CLI_CONFIG_KEY")
private val LOG = Logger.getInstance("#org.angularjs.cli.AngularCliConfigLoader")

fun load(project: Project, context: VirtualFile): AngularCliConfig {
  val angularCliFolder = AngularCliUtil.findAngularCliFolder(project, context)
                         ?: return AngularCliEmptyConfig()
  val angularCliJson = AngularCliUtil.findCliJson(angularCliFolder) ?: return AngularCliEmptyConfig()
  try {
    return CachedValuesManager.getManager(project).getCachedValue(
      PsiManager.getInstance(project).findFile(angularCliJson)!!, ANGULAR_CLI_CONFIG_KEY,
      {
        val cachedDocument = FileDocumentManager.getInstance().getCachedDocument(angularCliJson)
        CachedValueProvider.Result.create(
          AngularCliJsonFileConfig(
            angularCliJson, cachedDocument?.charsSequence ?: VfsUtilCore.loadText(angularCliJson)),
          cachedDocument ?: angularCliJson)
      }, false)
  }
  catch (e: Exception) {
    LOG.info(e)
  }
  return AngularCliEmptyConfig()
}

interface AngularCliConfig {
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

  init {
    val ngCliConfig = GsonBuilder().setLenient().create().fromJson(CharSequenceReader(text), AngularCli::class.java)
    val allProjects = ContainerUtil.concat(ngCliConfig.apps, ngCliConfig.projects.values.toList())
    myRootPaths = allProjects.mapNotNull { it.rootPath }.fold(ArrayList()) { acc, root -> acc.add(root); acc; }
    myStylePreprocessorIncludePaths = ContainerUtil.concat(
      allProjects.mapNotNull { it.stylePreprocessorOptions?.includePaths },
      allProjects.mapNotNull { it.targets?.build?.options?.stylePreprocessorOptions?.includePaths }
    ).fold(ArrayList()) { acc, list -> acc.addAll(list); acc; }
    myKarmaConfigPath = allProjects.mapNotNull { it.targets?.test?.options?.karmaConfig }.firstOrNull()
    myProtractorConfigPath = allProjects.mapNotNull { it.targets?.e2e?.options?.protractorConfig }.firstOrNull()
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
  @SerializedName("apps")
  @Expose
  val apps: List<AngularCliProject> = ArrayList()

  @SerializedName("projects")
  @Expose
  val projects: Map<String, AngularCliProject> = HashMap()
}

private class AngularCliProject {
  @SerializedName("root")
  @Expose
  val rootPath: String? = null

  @SerializedName("stylePreprocessorOptions")
  @Expose
  val stylePreprocessorOptions: AngularCliStylePreprocessorOptions? = null

  @SerializedName("targets", alternate = ["architect"])
  @Expose
  val targets: AngularCliTargets? = null

}

private class AngularCliTargets {
  @SerializedName("build")
  @Expose
  val build: AngularCliBuild? = null

  @SerializedName("test")
  @Expose
  val test: AngularCliTest? = null

  @SerializedName("e2e")
  @Expose
  val e2e: AngularCliE2E? = null
}

private class AngularCliE2E {
  @SerializedName("options")
  @Expose
  val options: AngularCliE2EOptions? = null
}

private class AngularCliE2EOptions {
  @SerializedName("protractorConfig")
  @Expose
  val protractorConfig: String? = null
}

private class AngularCliTest {
  @SerializedName("options")
  @Expose
  val options: AngularCliTestOptions? = null
}

private class AngularCliTestOptions {
  @SerializedName("karmaConfig")
  @Expose
  val karmaConfig: String? = null
}

private class AngularCliBuild {
  @SerializedName("options")
  @Expose
  val options: AngularCliBuildOptions? = null
}

private class AngularCliBuildOptions {
  @SerializedName("stylePreprocessorOptions")
  @Expose
  val stylePreprocessorOptions: AngularCliStylePreprocessorOptions? = null
}

private class AngularCliStylePreprocessorOptions {
  @SerializedName("includePaths")
  @Expose
  val includePaths: List<String> = ArrayList()
}

