package com.intellij.protobuf.lang.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.protobuf.lang.PbLangBundle
import com.intellij.protobuf.lang.intentions.util.ImportPathData
import com.intellij.protobuf.lang.intentions.util.PbImportPathResolver
import com.intellij.protobuf.lang.intentions.util.selectItemAndApply
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbImportStatement
import com.intellij.protobuf.lang.psi.PbNamedElement
import com.intellij.protobuf.lang.psi.PbSymbolPath
import com.intellij.protobuf.lang.resolve.ProtoSymbolPathReference
import com.intellij.protobuf.lang.stub.index.QualifiedNameIndex
import com.intellij.protobuf.lang.stub.index.ShortNameIndex
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.util.parentsOfType

internal class PbAddImportStatementIntention : IntentionAction {
  override fun startInWriteAction(): Boolean {
    return false
  }

  override fun getText(): String {
    return PbLangBundle.message("intention.add.import.statement.name")
  }

  override fun getFamilyName(): String {
    return PbLangBundle.message("intention.fix.import.problems.familyName")
  }

  override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
    return file is PbFile
           && findSymbolPathReference(editor, file)?.resolve() == null
           && constructUnresolvedSymbolPath(file, editor)?.let { allProtoFilesWithGivenFqn(it, project) }?.isNotEmpty() ?: false
  }

  override fun invoke(project: Project, editor: Editor, editedFile: PsiFile) {
    if (editedFile !is PbFile) return

    if (ApplicationManager.getApplication().isUnitTestMode) {
      selectItemAndApply(prepareQuickFixes(project, editor, editedFile), editor, project)
      return
    }

    ProgressManager.getInstance().runProcessWithProgressSynchronously(
      {
        val fixes = runReadAction { prepareQuickFixes(project, editor, editedFile) }
        invokeLater { selectItemAndApply(fixes, editor, project) }
      },
      PbLangBundle.message("background.task.title.add.import.prepare.variants"),
      true,
      project
    )
  }

  private fun prepareQuickFixes(project: Project, editor: Editor, editedFile: PbFile): List<PbImportIntentionVariant> {
    val unresolvedMessageFqn = constructUnresolvedSymbolPath(editedFile, editor) ?: return emptyList()
    val importCandidates = allProtoFilesWithGivenFqn(unresolvedMessageFqn, project)
    val importCandidatesPaths = importCandidates.map { FileUtil.toSystemIndependentName(it.virtualFile.path) }

    val suitableExistingInCurrentFileImportStatements = editedFile.importStatements
      .filter { importCorrespondsToFile(it, importCandidatesPaths) }

    return createPossibleImportIssueFixes(importCandidates, suitableExistingInCurrentFileImportStatements, editedFile)
  }

  private fun createPossibleImportIssueFixes(protoFileCandidates: List<PsiFile>,
                                             suitableImportStatements: List<PbImportStatement>,
                                             editedProtoFile: PbFile): List<PbImportIntentionVariant> {

    if (suitableImportStatements.isEmpty()) {
      return protoFileCandidates.map {
        val originalProtoFile = editedProtoFile.virtualFile
        val importedProtoFile = it.virtualFile
        val project = editedProtoFile.project
        val presentablePath = ImportPathData.shortenPath(importedProtoFile, project)

        ImportPathData(originalProtoFile, importedProtoFile, importedProtoFile.name, importedProtoFile.parent.url, presentablePath)
      }.map {
        PbImportIntentionVariant.AddImportStatementAndPathToSettings(it)
      }
    }
    else {
      return suitableImportStatements
        .asSequence()
        .mapNotNull { findTheOnlySuitableProtoFileToImport(it, editedProtoFile) }
        .map { PbImportIntentionVariant.AddImportPathToSettings(it) }
        .toList()
    }
  }

  private fun findSymbolPathReference(editor: Editor, file: PsiFile): PsiReference? {
    return file.findReferenceAt(editor.caretModel.offset) as? ProtoSymbolPathReference
  }

  private fun constructUnresolvedSymbolPath(file: PbFile, editor: Editor): String? {
    return file.findElementAt(editor.caretModel.offset)
      ?.parentsOfType<PbSymbolPath>(true)
      ?.lastOrNull()
      ?.qualifiedName
      ?.join("/")
  }

  private fun allProtoFilesWithGivenFqn(unresolvedMessageFqn: String, project: Project): List<PbFile> {
    return allProtoFilesWithFqnFromIndex(unresolvedMessageFqn, QualifiedNameIndex.KEY, project)
             .takeIf { it.isNotEmpty() }
           ?: allProtoFilesWithFqnFromIndex(unresolvedMessageFqn, ShortNameIndex.KEY, project)
  }

  private fun allProtoFilesWithFqnFromIndex(unresolvedMessageFqn: String,
                                            key: StubIndexKey<String, PbNamedElement>,
                                            project: Project): List<PbFile> {
    return StubIndex.getElements(
      key,
      unresolvedMessageFqn,
      project,
      GlobalSearchScope.allScope(project),
      PbNamedElement::class.java
    )
      .map(PsiElement::getContainingFile)
      .filterIsInstance<PbFile>()
  }

  private fun importCorrespondsToFile(currentImportStatement: PbImportStatement, protoFileCandidates: List<String>): Boolean {
    return currentImportStatement.importName
      ?.stringValue
      ?.value
      ?.takeIf(String::isNotBlank)
      ?.takeIf { existingImport -> protoFileCandidates.any { it.endsWith(existingImport) } } != null
  }

  private fun findTheOnlySuitableProtoFileToImport(unresolvedImportStatement: PbImportStatement, editedProtoFile: PbFile): ImportPathData? {
    val relativeProtoPath = unresolvedImportStatement.importName?.stringValue?.value
                            ?: run {
                              thisLogger().warn("Empty import statement selected as suitable for import paths configuration")
                              return null
                            }
    return PbImportPathResolver.findSuitableImportPaths(relativeProtoPath, editedProtoFile.virtualFile,
                                                        editedProtoFile.project).firstOrNull()
           ?: run {
             thisLogger().warn("Unable to find suitable PROTO file for specified import statement")
             null
           }
  }
}