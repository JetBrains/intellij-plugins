package org.jetbrains.qodana.inspectionKts.templates

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.parents
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.inspectionKts.INSPECTIONS_KTS_DIRECTORY
import org.jetbrains.qodana.inspectionKts.INSPECTIONS_KTS_EXTENSION

class NewKtsInspectionAction : CreateFileFromTemplateAction(), DumbAware {
  override fun isAvailable(dataContext: DataContext): Boolean {
    if (!super.isAvailable(dataContext)) return false

    val ideView = LangDataKeys.IDE_VIEW.getData(dataContext) ?: return false
    val directories = ideView.directories
    if (directories.isEmpty()) return false

    val isInspectionsKtsDirectory = ideView.directories.all { selectedDir ->
      val inspectionKtsDirectorySelected = selectedDir
        .parents(true)
        .filterIsInstance<PsiDirectory>()
        .any { it.name == INSPECTIONS_KTS_DIRECTORY }
      inspectionKtsDirectorySelected
    }

    return isInspectionsKtsDirectory
  }

  override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
    val templates = InspectionKtsTemplate.Provider.templates()
    templates
      .map { it.uiDescriptor }
      .sortedBy { it.weight }
      .forEach { uiDescriptor ->
        builder.addKind(uiDescriptor.name, uiDescriptor.icon, uiDescriptor.id)
      }
  }

  override fun createFile(name: String?, templateName: String?, dir: PsiDirectory?): PsiFile? {
    if (name == null || templateName == null || dir == null) return null
    val inspectionKtsTemplate = InspectionKtsTemplate.Provider.templates().find { it.uiDescriptor.id == templateName } ?: return null
    val ktsFileType = FileTypeManager.getInstance().getFileTypeByExtension("kts")
    val project = dir.project
    val fullFilename = "$name.${INSPECTIONS_KTS_EXTENSION}"

    val psiFile = WriteCommandAction
      .writeCommandAction(project)
      .withGlobalUndo()
      .compute<PsiFile, Exception> {
        dir.checkCreateFile(fullFilename)
        val psiFile = PsiFileFactory.getInstance(project).createFileFromText(
          fullFilename,
          ktsFileType,
          inspectionKtsTemplate.templateContent.invoke(name)
        )
        dir.add(psiFile)

        psiFile
      }

    project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
      openFileInEditor(dir, fullFilename)
    }

    return psiFile
  }

  override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String  {
    return QodanaBundle.message("action.Qodana.NewInspectionKts.text")
  }

  private suspend fun openFileInEditor(psiDirectory: PsiDirectory, filename: String) {
    val project = psiDirectory.project

    withContext(QodanaDispatchers.Default) {
      psiDirectory.virtualFile.refresh(false, false)
      val virtualFile = psiDirectory.virtualFile.findChild(filename)
      if (virtualFile != null) {
        withContext(QodanaDispatchers.Ui) {
          FileEditorManager.getInstance(project).openFile(virtualFile, true)
        }
      }
    }
  }
}