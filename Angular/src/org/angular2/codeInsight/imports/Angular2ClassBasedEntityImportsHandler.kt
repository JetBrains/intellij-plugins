package org.angular2.codeInsight.imports

import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.javascript.modules.imports.JSImportCandidateWithExecutor
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.Editor
import org.angular2.Angular2DecoratorUtil
import org.angular2.entities.Angular2ClassBasedEntity
import org.angular2.entities.Angular2Entity
import org.angular2.entities.Angular2ImportsOwner
import org.angular2.inspections.quickfixes.Angular2FixesPsiUtil

class Angular2ClassBasedEntityImportsHandler : Angular2ImportsHandler {

  override fun accepts(entity: Angular2Entity): Boolean =
    entity is Angular2ClassBasedEntity

  override fun insertImport(editor: Editor?,
                            candidate: JSImportCandidateWithExecutor,
                            importsOwner: Angular2ImportsOwner) {
    val element = candidate.element ?: return
    val destinationModuleClass = (importsOwner as Angular2ClassBasedEntity).typeScriptClass
    if (destinationModuleClass == null || importsOwner.decorator == null) {
      return
    }

    val name = candidate.name
    WriteAction.run<RuntimeException> {
      ES6ImportPsiUtil.insertJSImport(destinationModuleClass, name, element, editor)
      Angular2FixesPsiUtil.insertEntityDecoratorMember(importsOwner, Angular2DecoratorUtil.IMPORTS_PROP, name)
      // TODO support NgModuleWithProviders static methods
    }
  }
}