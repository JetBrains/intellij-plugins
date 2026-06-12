package com.intellij.openRewrite.recipe

import com.intellij.openRewrite.OpenRewriteBundle
import com.intellij.openRewrite.isRecipe
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.extension.SchemaType
import org.jetbrains.annotations.Nls

internal class OpenRewriteRecipeSchemaProviderFactory : JsonSchemaProviderFactory {
  override fun getProviders(project: Project): List<JsonSchemaFileProvider> = listOf(
    OpenRewriteRecipeSchemaProvider(project),
  )

  private class OpenRewriteRecipeSchemaProvider(private val project: Project) : JsonSchemaFileProvider {
    override fun getSchemaFile(): VirtualFile {
      val resourceUrl = javaClass.classLoader.getResource("schemas/openRewrite.json")!!
      val vfsUrl = VfsUtilCore.convertFromUrl(resourceUrl)
      return VirtualFileManager.getInstance().refreshAndFindFileByUrl(vfsUrl)!!
    }

    override fun isAvailable(file: VirtualFile): Boolean {
      return ReadAction.compute<Boolean, Exception> {
        val psiFile = PsiManager.getInstance(project).findFile(file) ?: return@compute false
        isRecipe(psiFile)
      }
    }

    override fun getName(): @Nls String = OpenRewriteBundle.message("open.rewrite.recipe")

    override fun getSchemaType(): SchemaType = SchemaType.schema
  }
}