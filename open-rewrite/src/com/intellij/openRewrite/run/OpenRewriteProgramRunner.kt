package com.intellij.openRewrite.run

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.AsyncProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.executeState
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.java.library.JavaLibraryUtil
import com.intellij.java.library.MavenCoordinates
import com.intellij.openRewrite.OpenRewriteBundle
import com.intellij.openRewrite.recipe.OpenRewriteRecipeDescriptor
import com.intellij.openRewrite.recipe.OpenRewriteRecipeService
import com.intellij.openRewrite.recipe.OpenRewriteType
import com.intellij.openRewrite.run.before.OpenRewriteInstallBeforeRunTask
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.PsiManager
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.containers.addIfNotNull
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileIndex
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.asPromise
import org.jetbrains.concurrency.resolvedPromise
import org.jetbrains.yaml.psi.YAMLDocument
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar
import org.jetbrains.yaml.psi.YAMLSequence
import java.util.concurrent.Callable
import kotlinx.coroutines.CancellationException

internal class OpenRewriteProgramRunner : AsyncProgramRunner<RunnerSettings>() {
  @Throws(ExecutionException::class)
  override fun execute(environment: ExecutionEnvironment, state: RunProfileState): Promise<RunContentDescriptor?> {
    val configuration = environment.runProfile as? OpenRewriteRunConfiguration
                        ?: throw ExecutionException(OpenRewriteBundle.message("open.rewrite.run.configuration.invalid.type"))
    if (state !is OpenRewriteState) {
      throw ExecutionException(OpenRewriteBundle.message("open.rewrite.run.configuration.invalid.state"))
    }

    val reload = OpenRewriteRecipeService.getInstance(environment.project).reload()?.asPromise() ?: resolvedPromise<Any?>()
    val descriptor = AsyncPromise<RunContentDescriptor?>()
    reload.onProcessed {
      ReadAction.nonBlocking(Callable<Void> {
        try {
          setRecipeArtifactIds(configuration, state)
        }
        catch (e: IndexNotReadyException) {
          // Fallback to artifacts provided by OpenRewrite library contributors.
          val serviceArtifacts = OpenRewriteRecipeService.getInstance(environment.project).recipeArtifacts()
          state.setRecipeArtifacts(serviceArtifacts + getBeforeRunTaskArtifacts(configuration))
        }
        catch (e: CancellationException) {
          throw e
        }
        catch (e: Exception) {
          descriptor.setError(e)
          throw e
        }
        return@Callable null
      })
        .expireWith(environment)
        .expireWith(OpenRewriteRecipeService.getInstance(environment.project))
        .finishOnUiThread(ApplicationManager.getApplication().defaultModalityState) {
          try {
            descriptor.setResult(executeState(state, environment, this))
          }
          catch (e: Exception) {
            descriptor.setError(e)
          }
        }
        .submit(AppExecutorUtil.getAppExecutorService())
    }
    return descriptor
  }

  override fun getRunnerId(): String = "OpenRewriteProgramRunner"

  override fun canRun(executorId: String, profile: RunProfile): Boolean =
    executorId == DefaultRunExecutor.EXECUTOR_ID && profile is OpenRewriteRunConfiguration

  private fun setRecipeArtifactIds(configuration: OpenRewriteRunConfiguration, state: OpenRewriteState) {
    val activeRecipes = splitConfigurationValue(configuration.activeRecipes)
    if (activeRecipes.isEmpty()) {
      state.setRecipeArtifacts(emptyList())
      return
    }

    val project = configuration.project
    val configFile = findConfigFile(configuration.getExpandedWorkingDirectory(), configuration.getExpandedConfigLocation())?.let {
      PsiManager.getInstance(project).findFile(it)
    }

    val recipeService = OpenRewriteRecipeService.getInstance(project)
    val artifacts = HashSet<String>()
    var openRewriteLibraryAdded = false

    val processedRecipes = HashSet<String>()
    val recipes = ArrayDeque(activeRecipes)

    while (recipes.isNotEmpty()) {
      val recipe = recipes.removeFirst()
      if (!processedRecipes.add(recipe)) continue

      val descriptor = recipeService.findDescriptor(recipe, configFile, OpenRewriteType.RECIPE) ?: continue
      val descriptorFile = descriptor.declaration.file ?: continue

      if (descriptorFile == configFile) {
        // Process sub recipes from local yaml recipe file.
        val subRecipes = getSubRecipes(descriptor)
        for (subRecipe in subRecipes) {
          if (!processedRecipes.contains(subRecipe)) {
            recipes.add(subRecipe)
          }
        }
      }
      else {
        val virtualFile = descriptorFile.originalFile.virtualFile ?: continue

        val orderEntries = ProjectFileIndex.getInstance(project).getOrderEntriesForFile(virtualFile)
        if (orderEntries.isEmpty()) {
          if (!openRewriteLibraryAdded) {
            val root = WorkspaceFileIndex.getInstance(project).findFileSet(virtualFile, true, false, false, true, true, true, false)?.root
            if (root != null && recipeService.getRoots().contains(root)) {
              artifacts.addAll(recipeService.recipeArtifacts())
              openRewriteLibraryAdded = true
            }
          }
        }
        else {
          val recipeArtifacts = orderEntries.filterIsInstance<LibraryOrderEntry>().mapNotNull {
            val library = it.library ?: return@mapNotNull null
            JavaLibraryUtil.getMavenCoordinates(library)?.toArtifactString()
          }
          artifacts.addAll(recipeArtifacts)
        }
      }
    }

    artifacts.addAll(getBeforeRunTaskArtifacts(configuration))

    state.setRecipeArtifacts(artifacts)
  }

  private fun getBeforeRunTaskArtifacts(configuration: OpenRewriteRunConfiguration): List<String> {
    return configuration.beforeRunTasks.filterIsInstance<OpenRewriteInstallBeforeRunTask>().map { it.getCoordinates().toArtifactString() }
  }

  private fun MavenCoordinates.toArtifactString(): String = "$groupId:$artifactId:$version"

  private fun getSubRecipes(descriptor: OpenRewriteRecipeDescriptor): Collection<String> {
    val mapping = (descriptor.declaration.retrieve() as? YAMLDocument)?.topLevelValue as? YAMLMapping ?: return emptyList()
    val sequences = ArrayList<YAMLSequence>()
    sequences.addIfNotNull(mapping.getKeyValueByKey(OpenRewriteType.RECIPE.listKey)?.value as? YAMLSequence)
    for (key in OpenRewriteType.RECIPE.additionalListKeys) {
      sequences.addIfNotNull(mapping.getKeyValueByKey(key)?.value as? YAMLSequence)
    }
    if (sequences.isEmpty()) return emptyList()

    val subRecipes = ArrayList<String>()
    for (sequence in sequences) {
      for (item in sequence.items) {
        val value = item.value
        if (value is YAMLScalar) {
          subRecipes.add(value.textValue)
        }
        else if (value is YAMLMapping) {
          for (keyValue in value.keyValues) {
            subRecipes.add(keyValue.keyText)
          }
        }
      }
    }
    return subRecipes
  }
}