package org.angular2.codeInsight.imports

import com.intellij.lang.javascript.imports.JSOptimizeImportUtil
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.refactoring.FormatFixer
import com.intellij.lang.typescript.imports.TypeScriptImportOptimizer
import com.intellij.openapi.util.EmptyRunnable
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.parentOfType
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil
import org.angular2.entities.source.Angular2SourceUtil
import org.angular2.inspections.AngularImportsExportsOwnerConfigurationInspection
import org.angular2.inspections.quickfixes.Angular2FixesPsiUtil.removeReferenceFromImportsList
import org.angular2.lang.Angular2LangUtil

class Angular2TypeScriptImportsOptimizer : TypeScriptImportOptimizer() {

  override fun processFile(file: PsiFile): Runnable {
    if (!JSOptimizeImportUtil.isAvailable(file))
      return EmptyRunnable.INSTANCE

    if (!Angular2LangUtil.isAngular2Context(file))
      return super.processFile(file)

    val unusedElements = Angular2SourceUtil.findComponentClassesInFile(file) { _, _ -> true }
      .asSequence()
      .mapNotNull { Angular2DecoratorUtil.findDecorator(it, Angular2DecoratorUtil.COMPONENT_DEC) }
      .flatMap { AngularImportsExportsOwnerConfigurationInspection.getUnusedImports(it) }
      .mapNotNull { it.asSafely<JSReferenceExpression>()?.takeIf { ref -> ref.qualifier == null || ref.referenceName == null } }
      .toSet()

    if (unusedElements.isEmpty())
      return super.processFile(file)

    val infos = getModulesInfo(file, unusedElements)
    val unusedElementsPtrs = unusedElements.map { it.createSmartPointer() }

    return Runnable {
      val project = file.project
      val manager = PsiDocumentManager.getInstance(project)
      val document = manager.getDocument(file)
      if (document != null) {
        manager.commitDocument(document)
      }

      val tsFormatFixers = infos.mapNotNull { info -> processModule(project, document, info) }

      if (document != null) {
        manager.commitDocument(document)
      }

      val ngFormatFixers = unusedElementsPtrs.mapNotNull { it.element?.parentOfType<JSProperty>() }.distinct()
                            .map { FormatFixer.create(it, FormatFixer.Mode.Reformat) }

      unusedElementsPtrs.forEach { ptr ->
        ptr.dereference()?.let { removeReferenceFromImportsList(it) }
      }
      FormatFixer.fixAll(tsFormatFixers + ngFormatFixers)
    }
  }


}