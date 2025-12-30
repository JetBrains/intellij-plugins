package org.angular2.lang.expr.psi.impl

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.config.JSConfig
import com.intellij.lang.javascript.config.JSConfigProvider
import com.intellij.lang.javascript.psi.impl.JSFileImpl
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil
import com.intellij.psi.FileViewProvider
import com.intellij.psi.xml.XmlFile
import org.angular2.entities.source.Angular2SourceUtil
import org.angular2.lang.expr.Angular2ExprDialect
import org.angular2.lang.html.Angular2HtmlFile

class Angular2FileImpl(fileViewProvider: FileViewProvider, language: Angular2ExprDialect) : JSFileImpl(fileViewProvider, language), JSConfigProvider {

  override fun getJSConfig(): JSConfig? =
    when (val topLevelFile = InjectedLanguageManager.getInstance(project).getTopLevelFile(this)) {
      is Angular2HtmlFile -> topLevelFile.jsConfig
      is XmlFile -> TypeScriptConfigUtil.getPreferableConfig(Angular2SourceUtil.findComponentClass(topLevelFile)?.containingFile, false)
      is JSFileImpl -> TypeScriptConfigUtil.getPreferableConfig(topLevelFile, false)
      else -> throw IllegalStateException("Unexpected file type $topLevelFile")
    }

}