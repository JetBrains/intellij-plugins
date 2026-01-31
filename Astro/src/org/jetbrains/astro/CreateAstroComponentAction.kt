// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys.PROJECT
import com.intellij.openapi.actionSystem.PlatformDataKeys.PSI_ELEMENT
import com.intellij.openapi.actionSystem.PlatformDataKeys.PSI_FILE
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import org.jetbrains.annotations.Nls
import org.jetbrains.astro.context.hasAstroFiles
import org.jetbrains.astro.context.isAstroProject

class CreateAstroComponentAction : CreateFileFromTemplateAction(), DumbAware {

  companion object {
    const val ASTRO_TEMPLATE_NAME: String = "Astro Component"

    @Nls
    private val name = AstroBundle.message("action.CreateAstroComponent.text")
  }

  override fun isAvailable(dataContext: DataContext): Boolean {
    return super.isAvailable(dataContext)
           && (PROJECT.getData(dataContext)?.let { hasAstroFiles(it) } == true
               || (PSI_ELEMENT.getData(dataContext) ?: PSI_FILE.getData(dataContext))?.let { isAstroProject(it) } == true)
  }

  override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
    builder
      .setTitle(AstroBundle.message("astro.create.component.action.dialog.title", name))
      .addKind(name, AstroIcons.Astro, ASTRO_TEMPLATE_NAME)
  }

  override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String =
    AstroBundle.message("astro.create.component.action.name", name, newName)

}
