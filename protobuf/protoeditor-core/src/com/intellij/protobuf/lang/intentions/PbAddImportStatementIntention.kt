package com.intellij.protobuf.lang.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.protobuf.lang.PbFileType
import com.intellij.protobuf.lang.PbLangBundle
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbImportStatement
import com.intellij.protobuf.lang.psi.PbNamedElement
import com.intellij.protobuf.lang.psi.PbSymbolPath
import com.intellij.protobuf.lang.psi.util.PbPsiFactory
import com.intellij.protobuf.lang.resolve.ProtoSymbolPathReference
import com.intellij.protobuf.lang.stub.index.QualifiedNameIndex
import com.intellij.protobuf.lang.stub.index.ShortNameIndex
import com.intellij.protobuf.lang.util.ImportPathData
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.util.parentsOfType
import com.intellij.util.PathUtil
import com.intellij.util.castSafelyTo

internal class PbAddImportStatementIntention(private val elementOffset: Int) : IntentionAction {
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
    return findSymbolPathReference(editor, file)?.resolve() == null
  }

  private fun findSymbolPathReference(editor: Editor, file: PsiFile): PsiReference? {
    return file.findReferenceAt(editor.caretModel.offset).castSafelyTo<ProtoSymbolPathReference>()
  }

  private fun constructUnresolvedSymbolPath(file: PbFile, editor: Editor): String? {
    return file.findElementAt(elementOffset)
      ?.parentsOfType<PbSymbolPath>(true)
      ?.lastOrNull()
      ?.qualifiedName
      ?.join("/")
  }

  private fun allProtoFilesWithGivenFqn(unresolvedMessageFqn: String, project: Project): List<PsiFile> {
    return allProtoFilesWithFqnFromIndex(unresolvedMessageFqn, QualifiedNameIndex.KEY, project)
             .takeIf { it.isNotEmpty() }
           ?: allProtoFilesWithFqnFromIndex(unresolvedMessageFqn, ShortNameIndex.KEY, project)
  }

  private fun allProtoFilesWithFqnFromIndex(unresolvedMessageFqn: String,
                                            key: StubIndexKey<String, PbNamedElement>,
                                            project: Project): List<PsiFile> {
    return StubIndex.getElements(
      key,
      unresolvedMessageFqn,
      project,
      GlobalSearchScope.allScope(project),
      PbNamedElement::class.java
    ).map { it.containingFile }
  }

  private fun importCorrespondsToFile(currentImportStatement: PbImportStatement, protoFileCandidates: List<String>): Boolean {
    return currentImportStatement.importName
      ?.stringValue
      ?.value
      ?.takeIf(String::isNotBlank)
      ?.takeIf { existingImport -> protoFileCandidates.any { it.endsWith(existingImport) } } != null
  }

  private fun addImportStatement(project: Project, targetFile: PbFile, importCandidates: List<PsiFile>): PsiFile? {
    val chosenProtoFile = chooseFileToImport(importCandidates) ?: return null

    when {
      targetFile.importStatements.isNotEmpty() ->
        addImportAndNewLineAfter(project, targetFile, chosenProtoFile, targetFile.importStatements.last(), true)

      targetFile.importStatements.isEmpty() && targetFile.syntaxStatement != null ->
        addImportAndNewLineAfter(project, targetFile, chosenProtoFile, targetFile.syntaxStatement, true)

      targetFile.firstChild != null ->
        addImportAndNewLineAfter(project, targetFile, chosenProtoFile, targetFile.firstChild, false)

      else ->
        addImportAndNewLineAfter(project, targetFile, chosenProtoFile, null, false)
    }

    return chosenProtoFile
  }

  private fun addImportAndNewLineAfter(project: Project,
                                       targetFile: PsiFile,
                                       importedFile: PsiFile,
                                       anchor: PsiElement?,
                                       afterAnchor: Boolean) {

    val importStatement = PbPsiFactory.createImportStatement(project, importedFile.name)
    val newLine = PbPsiFactory.createNewLine(project)

    val newImport =
      if (afterAnchor)
        targetFile.addAfter(importStatement, anchor)
      else
        targetFile.addBefore(importStatement, anchor)

    if (afterAnchor)
      targetFile.addBefore(newLine, newImport)
    else
      targetFile.addAfter(newLine, newImport)
  }

  private fun chooseFileToImport(protoFileCandidates: List<PsiFile>): PsiFile? {
    return protoFileCandidates.firstOrNull() //fixme show popup from ui util
  }

  private fun configurePlugin(project: Project, importedFile: VirtualFile) {
    if (project.isDisposed) return

    val importPathData = ImportPathData(
      importedFile,
      importedFile.name,
      importedFile.parent.url,
      importedFile.path
    )

    PbImportIntentionVariant.AddImportPathToSettings(importPathData).invokeAction(project)
  }

  private fun findProtoFileByLocation(project: Project, relativePath: String): VirtualFile? {
    return FileTypeIndex.getFiles(PbFileType.INSTANCE, GlobalSearchScope.allScope(project))
      .firstOrNull { it.path.let(PathUtil::toSystemIndependentName).endsWith(relativePath) }
  }

  override fun invoke(project: Project, editor: Editor, file: PsiFile) {
    if (file !is PbFile) return

    val unresolvedMessageFqn = constructUnresolvedSymbolPath(file, editor) ?: return
    val protoFileCandidates = allProtoFilesWithGivenFqn(unresolvedMessageFqn, project)

    val protoFileCandidatePaths = protoFileCandidates.map { FileUtil.toSystemIndependentName(it.virtualFile.path) }

    val suitableExistingInCurrentFileImportStatements = file.importStatements
      .filter { importCorrespondsToFile(it, protoFileCandidatePaths) }

    // All inner commands should have the same group id to be merged with already running one
    WriteCommandAction.runWriteCommandAction(
      project,
      PbLangBundle.message("intention.add.import.path.popup.title"),
      PbLangBundle.message("intention.fix.import.problems.familyName"), {
        when (suitableExistingInCurrentFileImportStatements.size) {
          0 -> {
            val importedPsiFile = addImportStatement(project, file, protoFileCandidates) ?: return@runWriteCommandAction
            configurePlugin(project, importedPsiFile.virtualFile)
          }
          1 -> {
            val relativeProtoPath = suitableExistingInCurrentFileImportStatements.single().importName?.stringValue?.value
                                    ?: run {
                                      thisLogger().warn(
                                        "Empty import statement selected as suitable for import paths configuration")
                                      return@runWriteCommandAction
                                    }
            val foundProtoFile = findProtoFileByLocation(project, relativeProtoPath) ?: run {
              thisLogger().warn("Unable to find suitable PROTO file for specified import statement")
              return@runWriteCommandAction
            }
            configurePlugin(project, foundProtoFile)
          }
          else -> {
            thisLogger().warn("Several (${suitableExistingInCurrentFileImportStatements.size}) suitable import statements " +
                              "found for unresolved symbol path. Abort intention.")
          }
        }
      }, file)
  }
}