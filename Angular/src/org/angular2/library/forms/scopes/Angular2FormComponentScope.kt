package org.angular2.library.forms.scopes

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.model.Pointer
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.AstLoadingFilter
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolsScopeWithCache
import org.angular2.Angular2Framework
import org.angular2.library.forms.NG_FORM_GROUP_FIELDS

class Angular2FormComponentScope(clazz: TypeScriptClass)
  : WebSymbolsScopeWithCache<TypeScriptClass, Unit>(Angular2Framework.ID, clazz.project, clazz, Unit) {

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
    AstLoadingFilter.forceAllowTreeLoading<Throwable>(dataHolder.containingFile) {
      val builder = Angular2FormSymbolsBuilder(consumer)
      dataHolder.jsType.asRecordType().properties.forEach {
        it.memberSource.allSourceElements.forEach { source ->
          source.accept(builder)
        }
      }
    }
  }

  override fun provides(qualifiedKind: WebSymbolQualifiedKind): Boolean =
    qualifiedKind == NG_FORM_GROUP_FIELDS

  override fun createPointer(): Pointer<Angular2FormComponentScope> {
    val filePtr = dataHolder.createSmartPointer()
    return Pointer {
      val file = filePtr.dereference() ?: return@Pointer null
      Angular2FormComponentScope(file)
    }
  }


}