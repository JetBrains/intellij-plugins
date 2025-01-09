package org.angular2.lang.expr.psi.impl

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.config.JSConfig
import com.intellij.lang.javascript.config.JSConfigProvider
import com.intellij.lang.javascript.psi.impl.JSFileImpl
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil
import com.intellij.psi.FileViewProvider
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.html.Angular2HtmlFile

class Angular2FileImpl(fileViewProvider: FileViewProvider) : JSFileImpl(fileViewProvider, Angular2Language), JSConfigProvider {

  override fun getJSConfig(): JSConfig? =
    when (val topLevelFile = InjectedLanguageManager.getInstance(project).getTopLevelFile(this)) {
      is Angular2HtmlFile -> topLevelFile.jsConfig
      is JSFileImpl -> TypeScriptConfigUtil.getConfigForPsiFile(topLevelFile)
      else -> throw IllegalStateException("Unexpected file type $topLevelFile")
    }

}