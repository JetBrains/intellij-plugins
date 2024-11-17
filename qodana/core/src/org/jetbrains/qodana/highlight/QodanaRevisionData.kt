package org.jetbrains.qodana.highlight

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiFile

internal val QODANA_REVISION_DATA = Key.create<QodanaRevisionData>("Qodana.Base.Revision")

sealed interface QodanaRevisionData {
  class VCSInfo(val revisionPsiFiles: Map<String?, PsiFile?>) : QodanaRevisionData

  class LocalInfo(val localDocumentData: QodanaLocalDocumentData) : QodanaRevisionData
}

data class QodanaLocalDocumentData(val timestamp: Long, val document: Document)
