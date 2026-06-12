package com.intellij.openRewrite.run.before

import com.intellij.execution.ExecutionBundle
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchRootType
import com.intellij.openRewrite.OpenRewriteBundle
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.text
import org.jetbrains.annotations.Nullable
import java.awt.event.ActionListener
import javax.swing.JComponent
import javax.swing.event.DocumentEvent

internal class OpenRewriteInstallBeforeRunTaskDialog(
  project: Project,
  title: @NlsContexts.DialogTitle String,
  private val beforeRunTask: OpenRewriteInstallBeforeRunTask,
) : DialogWrapper(project, true) {

  private val content = MyPanel(project)

  init {
    setTitle(title)
    initPanel(beforeRunTask)
    content.scratchFileField.addDocumentListener(object : DocumentAdapter() {
      override fun textChanged(e: DocumentEvent) {
        updateEmptyText()
        initValidation()
      }

      private fun updateEmptyText() {
        val task = OpenRewriteInstallBeforeRunTask()
        task.scratchFileUrl = content.scratchFileField.text
        val id = task.getId()
        content.groupField.emptyText.text = id
        content.artifactField.emptyText.text = id
      }
    })
    init()
  }

  private fun initPanel(beforeRunTask: OpenRewriteInstallBeforeRunTask) {
    content.scratchFileField.text = beforeRunTask.scratchFileUrl ?: ""
    content.groupField.text = beforeRunTask.groupId ?: ""
    val id = beforeRunTask.getId()
    content.groupField.emptyText.text = id
    content.artifactField.text = beforeRunTask.artifactId ?: ""
    content.artifactField.emptyText.text = id
    content.versionField.text = beforeRunTask.version ?: ""
  }

  override fun doOKAction() {
    beforeRunTask.scratchFileUrl = content.scratchFileField.text
    beforeRunTask.groupId = content.groupField.text.takeIf { it.isNotBlank() }
    beforeRunTask.artifactId = content.artifactField.text.takeIf { it.isNotBlank() }
    beforeRunTask.version = content.versionField.text.takeIf { it.isNotBlank() }
    super.doOKAction()
  }

  @Nullable
  override fun createCenterPanel(): JComponent = content.panel

  @Nullable
  override fun getDimensionServiceKey(): String = "openRewrite.OpenRewriteInstallBeforeRunTaskDialog"

  override fun doValidate(): ValidationInfo? {
    val virtualFile = content.getFileFromEditor()
    if (virtualFile == null || !virtualFile.isValid) {
      return ValidationInfo(OpenRewriteBundle.message("open.rewrite.install.before.run.task.file.not.fount"))
    }
    if (virtualFile.fileType != JavaFileType.INSTANCE) {
      return ValidationInfo(OpenRewriteBundle.message("open.rewrite.install.before.run.task.file.not.java"))
    }
    return super.doValidate()
  }

  private class MyPanel(val project: Project) {
    lateinit var scratchFileField: TextFieldWithBrowseButton
    lateinit var groupField: JBTextField
    lateinit var artifactField: JBTextField
    lateinit var versionField: JBTextField

    val panel = panel {
      row(ExecutionBundle.message("path.to.scratch.file")) {
        scratchFileField = cell(TextFieldWithBrowseButton(ActionListener {
          var toSelect = getFileFromEditor()
          if (toSelect == null) {
            val scratchesRoot = ScratchFileService.getInstance().getRootPath(ScratchRootType.getInstance())
            toSelect = LocalFileSystem.getInstance().findFileByPath(scratchesRoot)
          }
          val descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor().withExtensionFilter(JavaFileType.INSTANCE)
          val file = FileChooser.chooseFile(descriptor, scratchFileField, project, toSelect)
          if (file != null) {
            setFileToEditor(file)
          }
        }))
          .align(AlignX.FILL)
          .component
      }
      collapsibleGroup(OpenRewriteBundle.message("open.rewrite.install.before.run.task.coordinates")) {
        row(OpenRewriteBundle.message("open.rewrite.install.before.run.task.group")) {
          groupField = textField()
            .text("")
            .align(AlignX.FILL)
            .component
        }
        row(OpenRewriteBundle.message("open.rewrite.install.before.run.task.artifact")) {
          artifactField = textField()
            .text("")
            .align(AlignX.FILL)
            .component
        }
        row(OpenRewriteBundle.message("open.rewrite.install.before.run.task.version")) {
          versionField = textField()
            .text("")
            .align(AlignX.FILL)
            .component
            .also { it.emptyText.text = DEFAULT_BEFORE_RUN_VERSION }
        }
      }
    }

    fun getFileFromEditor(): VirtualFile? {
      val path = FileUtil.toSystemIndependentName(scratchFileField.getText().trim { it <= ' ' })
      return if (!StringUtil.isEmpty(path)) LocalFileSystem.getInstance().findFileByPath(path) else null
    }

    fun setFileToEditor(file: VirtualFile?) {
      scratchFileField.text = if (file != null) FileUtil.toSystemDependentName(file.path) else ""
    }
  }
}