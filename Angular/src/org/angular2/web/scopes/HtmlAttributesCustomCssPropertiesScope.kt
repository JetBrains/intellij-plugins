package org.angular2.web.scopes

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.polySymbols.css.CSS_PROPERTIES
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.utils.PolySymbolsStructuredScope
import org.angular2.lang.html.psi.Angular2HtmlRecursiveElementVisitor
import org.angular2.web.scopes.Angular2CustomCssPropertiesScope.Companion.createCustomCssProperty

class HtmlAttributesCustomCssPropertiesScope(location: PsiElement) : PolySymbolsStructuredScope<PsiElement, PsiFile>(location) {

  override val rootPsiElement: PsiFile?
    get() = location.containingFile.takeIf { InjectedLanguageManager.getInstance(location.project).isInjectedFragment(it) }

  override val scopesBuilderProvider: (PsiFile, PolySymbolsPsiScopesHolder) -> PsiElementVisitor?
    get() = provider@{ file, holder ->
      CustomCssPropertyTemplateScopeBuilder(holder)
    }

  override val providedSymbolKinds: Set<PolySymbolQualifiedKind>
    get() = setOf(CSS_PROPERTIES)

  override fun createPointer(): Pointer<HtmlAttributesCustomCssPropertiesScope> {
    val locationPtr = location.createSmartPointer()
    return Pointer {
      val location = locationPtr.dereference() ?: return@Pointer null
      HtmlAttributesCustomCssPropertiesScope(location)
    }
  }

  private class CustomCssPropertyTemplateScopeBuilder(
    private val holder: PolySymbolsPsiScopesHolder,
  ) : Angular2HtmlRecursiveElementVisitor() {

    override fun visitXmlTag(tag: XmlTag) {
      holder.pushScope(tag)
      try {
        super.visitXmlTag(tag)
      }
      finally {
        holder.popScope()
      }
    }

    override fun visitXmlAttribute(attribute: XmlAttribute) {
      createCustomCssProperty(attribute)?.let { holder.currentScope { addSymbol(it) } }
    }

  }
}


