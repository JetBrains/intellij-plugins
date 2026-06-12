package com.intellij.openRewrite.run.editor

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openRewrite.OpenRewriteIcons
import com.intellij.openRewrite.recipe.OpenRewriteRecipeDescriptor
import com.intellij.openRewrite.recipe.OpenRewriteRecipeService
import com.intellij.openRewrite.recipe.OpenRewriteType
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.ui.TextFieldWithAutoCompletionListProvider
import java.util.concurrent.Callable
import java.util.function.Supplier
import kotlin.math.max
import kotlin.math.min

internal class OpenRewriteRecipeCompletionProvider(private val project: Project,
                                                   private val configSupplier: Supplier<Collection<VirtualFile>>,
                                                   private val type: OpenRewriteType) :
  TextFieldWithAutoCompletionListProvider<OpenRewriteRecipeDescriptor>(null) {

  override fun getLookupString(item: OpenRewriteRecipeDescriptor): String = item.name

  override fun createLookupBuilder(item: OpenRewriteRecipeDescriptor): LookupElementBuilder {
    var builder = LookupElementBuilder.create(item.name)
      .withIcon(OpenRewriteIcons.OpenRewrite)
    if (item.displayName != null) {
      builder = builder
        .withLookupString(item.displayName)
        .withTypeText(item.displayName)
    }
    return builder
  }

  override fun getItems(prefix: String?, cached: Boolean, parameters: CompletionParameters): Collection<OpenRewriteRecipeDescriptor> {
    if (prefix == null) return emptyList()

    val text = filterOutCompletingRecipe(parameters.editor.document.text, parameters.offset)
    val activeRecipes = text.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()

    return getDescriptors().filter { it.options.isEmpty() && !activeRecipes.contains(it.name) }
  }

  override fun getPrefix(text: String, offset: Int): String {
    val space = max(text.lastIndexOf(' ', offset - 1), text.lastIndexOf('\n', offset - 1)) + 1
    val comma = text.lastIndexOf(',', offset - 1) + 1
    return text.substring(max(space, comma), offset)
  }

  private fun getDescriptors(): Collection<OpenRewriteRecipeDescriptor> {
    return ReadAction.nonBlocking(Callable {
      val result = ArrayList<OpenRewriteRecipeDescriptor>()
      for (virtualFile in configSupplier.get()) {
        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: continue
        result.addAll(OpenRewriteRecipeService.getInstance(project).getLocalDescriptors(psiFile, type))
      }
      result.addAll(OpenRewriteRecipeService.getInstance(project).getDescriptors(null, type))
      result
    }).executeSynchronously()
  }

  private fun filterOutCompletingRecipe(text: String, offset: Int): String {
    var space = max(text.lastIndexOf(' ', offset - 1), text.lastIndexOf('\n', offset - 1)) + 1
    var comma = text.lastIndexOf(',', offset - 1) + 1
    val before = max(space, comma)
    var result = text.substring(0, before)

    space = min(text.indexOf(' ', offset), text.indexOf('\n', offset))
    comma = text.indexOf(',', offset)
    val after = if (space < 0) {
      comma
    }
    else {
      if (comma < 0) space else min(comma, space)
    }
    if (after >= 0) {
      result += text.substring(after)
    }
    return result
  }
}