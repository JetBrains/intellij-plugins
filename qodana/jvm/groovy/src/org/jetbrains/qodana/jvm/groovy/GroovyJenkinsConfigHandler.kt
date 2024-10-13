package org.jetbrains.qodana.jvm.groovy

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import org.jetbrains.plugins.groovy.GroovyFileType
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.blocks.GrClosableBlockImpl
import org.jetbrains.qodana.extensions.ci.JenkinsConfigHandler

class GroovyJenkinsConfigHandler : JenkinsConfigHandler {
  override suspend fun addStage(project: Project, text: String, stageToAddText: String): String? {
    if (DumbService.isDumb(project)) return null
    return readAction {
      val fileFactory = PsiFileFactory.getInstance(project)

      val psiFile = fileFactory.createFileFromText("file1", GroovyFileType.GROOVY_FILE_TYPE, text) as GroovyFile
      val pipeline = psiFile.topStatements
        .filterIsInstance<GrMethodCallExpression>()
        .firstOrNull { it.callReference?.methodName == "pipeline" } ?: return@readAction null

      val pipelineStatements = pipeline.children.filterIsInstance<GrClosableBlock>().firstOrNull() ?: return@readAction null
      val stages = pipelineStatements.statements
        .filterIsInstance<GrMethodCallExpression>()
        .firstOrNull { it.callReference?.methodName == "stages" }
                   ?: addStagesBlock(project, pipelineStatements)
                   ?: return@readAction null

      val targetBlock = stages.children.filterIsInstance<GrClosableBlockImpl>().firstOrNull() ?: return@readAction null

      val stagesMethodCallFile = fileFactory.createFileFromText("file2", GroovyFileType.GROOVY_FILE_TYPE, stageToAddText) as GroovyFile
      val call = stagesMethodCallFile.topStatements.filterIsInstance<GrMethodCallExpression>().firstOrNull() ?: return@readAction null

      targetBlock.node.addChild(call.node, targetBlock.lastChild.node)
      targetBlock.node.addChild(GroovyPsiElementFactory.getInstance(project).createLineTerminator(1).node, targetBlock.lastChild.node)
      CodeStyleManager.getInstance(project).adjustLineIndent(psiFile, stages.textRange)
      psiFile.text
    }
  }

  override suspend fun isQodanaStagePresent(project: Project, virtualFile: VirtualFile): Boolean? {
    if (DumbService.isDumb(project)) return null
    return readAction {
      val fileFactory = PsiFileFactory.getInstance(project)

      val psiFile = fileFactory.createFileFromText("file1", GroovyFileType.GROOVY_FILE_TYPE, virtualFile.readText()) as GroovyFile
      val pipeline = psiFile.topStatements
                       .filterIsInstance<GrMethodCallExpression>()
                       .firstOrNull { it.callReference?.methodName == "pipeline" } ?: return@readAction false

      val pipelineStatements = pipeline.children.filterIsInstance<GrClosableBlock>().firstOrNull() ?: return@readAction false
      val stages = pipelineStatements.statements
                     .filterIsInstance<GrMethodCallExpression>()
                     .firstOrNull { it.callReference?.methodName == "stages" }
                   ?: addStagesBlock(project, pipelineStatements)
                   ?: return@readAction false

      val stagesStatements = stages.children.filterIsInstance<GrClosableBlock>().firstOrNull()?.children ?: return@readAction false
      stagesStatements.filterIsInstance<GrMethodCallExpression>().forEach { stage ->
        val stageChildren = stage.children.filterIsInstance<GrClosableBlock>().firstOrNull()?.statements ?: return@forEach
        val steps = stageChildren.filterIsInstance<GrMethodCallExpression>().firstOrNull { it.callReference?.methodName == "steps" } ?: return@forEach
        if (steps.children.any { it.text.contains("sh") && it.text.contains("qodana") }) return@readAction true
      }
      false
    }
  }

  private fun addStagesBlock(project: Project, pipelineStatements: GrClosableBlock): GrMethodCallExpression? {
    val text = """
      stages {
      }
    """.trimIndent()
    val createdFile = PsiFileFactory.getInstance(project)
      .createFileFromText("file3", GroovyFileType.GROOVY_FILE_TYPE, text) as GroovyFile

    val block = createdFile.topStatements.filterIsInstance<GrMethodCallExpression>().firstOrNull() ?: return null
    pipelineStatements.node.addChild(block.node, pipelineStatements.lastChild.node)
    pipelineStatements.node.addChild(GroovyPsiElementFactory.getInstance(project).createLineTerminator(1).node, pipelineStatements.lastChild.node)
    return block
  }
}