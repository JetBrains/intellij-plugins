package com.intellij.protobuf.lang.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.protobuf.lang.PbLangBundle
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbImportStatement
import com.intellij.protobuf.lang.psi.PbNamedElement
import com.intellij.protobuf.lang.psi.PbSymbolPath
import com.intellij.protobuf.lang.resolve.ProtoSymbolPathReference
import com.intellij.protobuf.lang.stub.index.QualifiedNameIndex
import com.intellij.protobuf.lang.stub.index.ShortNameIndex
import com.intellij.protobuf.lang.util.ImportPathData
import com.intellij.protobuf.lang.util.PbImportPathResolver
import com.intellij.protobuf.lang.util.PbUiUtils
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.util.parentsOfType
import com.intellij.util.castSafelyTo

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

    val unresolvedMessageFqn = constructUnresolvedSymbolPath(editedFile, editor) ?: return
    val importCandidates = allProtoFilesWithGivenFqn(unresolvedMessageFqn, project)
    val importCandidatesPaths = importCandidates.map { FileUtil.toSystemIndependentName(it.virtualFile.path) }

    val suitableExistingInCurrentFileImportStatements = editedFile.importStatements
      .filter { importCorrespondsToFile(it, importCandidatesPaths) }


    //val guessedOrSelectedImportData = guessOrSelectFileToImport(importCandidates, suitableExistingInCurrentFileImportStatements, editedFile) ?: return

    PbUiUtils.selectItemAndApply(
      createPossibleImportIssueFixes(importCandidates, suitableExistingInCurrentFileImportStatements, editedFile),
      editor,
      project
    )
    // All inner commands should have the same group id to be merged with already running one
    //WriteCommandAction.runWriteCommandAction(
    //  project,
    //  PbLangBundle.message("intention.add.import.path.popup.title"),
    //  PbLangBundle.message("intention.fix.import.problems.familyName"),
    //  { fixImportIssue(guessedOrSelectedImportData, editedFile, project) },
    //  editedFile
    //)
  }

  private fun createPossibleImportIssueFixes(protoFileCandidates: List<PsiFile>,
                                             suitableImportStatements: List<PbImportStatement>,
                                             editedProtoFile: PbFile): List<PbImportIntentionVariant> {

    when (suitableImportStatements.size) {
      1 -> {
        val onlySuitableImport = findTheOnlySuitableProtoFileToImport(suitableImportStatements.single(), editedProtoFile)
                                 ?: return emptyList()
        return listOf(PbImportIntentionVariant.AddImportPathToSettings(onlySuitableImport))
      }
      else -> {
        return protoFileCandidates.map {
          ImportPathData.create(
            editedProtoFile.virtualFile,
            it.virtualFile,
            editedProtoFile.project
          )
        }
          .map {
            PbImportIntentionVariant.AddImportStatementAndPathToSettings(it)
          }
      }
    }
  }

  private fun findSymbolPathReference(editor: Editor, file: PsiFile): PsiReference? {
    return file.findReferenceAt(editor.caretModel.offset).castSafelyTo<ProtoSymbolPathReference>()
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