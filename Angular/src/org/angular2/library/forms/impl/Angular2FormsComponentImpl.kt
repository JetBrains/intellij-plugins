package org.angular2.library.forms.impl

import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.AstLoadingFilter
import com.intellij.util.asSafely
import org.angular2.index.Angular2IndexUtil
import org.angular2.lang.expr.Angular2Language
import org.angular2.library.forms.Angular2FormGroup
import org.angular2.library.forms.Angular2FormsComponent

class Angular2FormsComponentImpl(private val componentClass: TypeScriptClass) : Angular2FormsComponent {

  override fun getFormGroupFor(reference: JSReferenceExpression): Angular2FormGroup? =
    reference
      .takeIf { it.qualifier == null || it.qualifier is JSThisExpression }
      ?.let {
        if (it.language == Angular2Language || it.qualifier is JSThisExpression)
          it.resolve()
        else
          Angular2IndexUtil.resolveLocally(it)
      }
      ?.asSafely<TypeScriptField>()
      ?.let { return getInfo().fields2Symbols[it] }

  private fun getInfo(): FormsInfo {
    val componentClass = this.componentClass
    return CachedValuesManager.getCachedValue(componentClass) {
      val formGroups = mutableListOf<Angular2FormGroup>()
      val builder = Angular2FormSymbolsBuilder {
        formGroups.add(it)
      }
      AstLoadingFilter.forceAllowTreeLoading<Throwable>(componentClass.containingFile) {
        for (it in componentClass.jsType.asRecordType().properties.asSequence()
          .plus(componentClass.staticJSType.asRecordType().callSignatures.filter { it.hasNew() })
        ) {
          it.memberSource.allSourceElements.forEach { source ->
            source.accept(builder)
          }
        }
      }
      CachedValueProvider.Result.create(FormsInfo(formGroups, formGroups.associateBy { it.source as TypeScriptField }),
                                        PsiModificationTracker.MODIFICATION_COUNT)
    }
  }

  private data class FormsInfo(
    val formGroups: List<Angular2FormGroup>,
    val fields2Symbols: Map<TypeScriptField, Angular2FormGroup>,
  )
}