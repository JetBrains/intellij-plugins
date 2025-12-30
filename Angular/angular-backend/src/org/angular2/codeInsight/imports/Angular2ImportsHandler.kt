package org.angular2.codeInsight.imports

import com.intellij.lang.javascript.modules.imports.JSImportCandidateWithExecutor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.extensions.ExtensionPointName
import org.angular2.entities.Angular2Entity
import org.angular2.entities.Angular2ImportsOwner

interface Angular2ImportsHandler {

  fun accepts(entity: Angular2Entity): Boolean

  fun insertImport(editor: Editor?, candidate: JSImportCandidateWithExecutor, importsOwner: Angular2ImportsOwner)

  companion object {

    private val EP_NAME: ExtensionPointName<Angular2ImportsHandler> = ExtensionPointName.create("org.angular2.importsHandler")

    fun getFor(entity: Angular2Entity): Angular2ImportsHandler =
      EP_NAME.extensionList.firstOrNull { it.accepts(entity) }
      ?: EmptyHandler

  }

}

private object EmptyHandler : Angular2ImportsHandler {
  override fun accepts(entity: Angular2Entity): Boolean =
    true

  override fun insertImport(editor: Editor?, candidate: JSImportCandidateWithExecutor, importsOwner: Angular2ImportsOwner) {
  }

}