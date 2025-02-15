// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.dmarcotte.handlebars.parsing;

import com.dmarcotte.handlebars.HbLanguage;
import com.dmarcotte.handlebars.file.HbFileViewProviderFactory;
import com.intellij.html.embedding.HtmlEmbeddedContentSupport;
import com.intellij.javascript.JSHtmlEmbeddedContentSupport;
import com.intellij.javascript.JSScriptContentProvider;
import com.intellij.lang.LanguageASTFactory;
import com.intellij.lang.LanguageHtmlScriptContentProvider;
import com.intellij.lang.html.HTMLParserDefinition;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.JavascriptParserDefinition;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.lang.xml.XmlASTFactory;
import com.intellij.lexer.EmbeddedTokenTypesProvider;
import com.intellij.psi.LanguageFileViewProviders;

import static com.intellij.lang.javascript.JSElementTypeServiceHelper.registerJSElementTypeServices;
import static com.intellij.xml.XmlElementTypeServiceHelper.registerXmlElementTypeServices;

public class HbHtmlParserTest extends HbParserTest {

  public HbHtmlParserTest() {
    super(new HTMLParserDefinition(), new JavascriptParserDefinition());
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    registerXmlElementTypeServices(getApplication(), getTestRootDisposable());
    registerJSElementTypeServices(getApplication(), getTestRootDisposable());

    addExplicitExtension(LanguageFileViewProviders.INSTANCE, HbLanguage.INSTANCE, new HbFileViewProviderFactory());
    addExplicitExtension(LanguageASTFactory.INSTANCE, HbLanguage.INSTANCE, new HbAstFactory());
    addExplicitExtension(LanguageASTFactory.INSTANCE, XMLLanguage.INSTANCE, new XmlASTFactory());

    registerExtensionPoint(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, EmbeddedTokenTypesProvider.class);
    addExplicitExtension(LanguageHtmlScriptContentProvider.INSTANCE, JavascriptLanguage.INSTANCE, new JSScriptContentProvider());
    HtmlEmbeddedContentSupport.register(getApplication(), getTestRootDisposable(), JSHtmlEmbeddedContentSupport.class);
  }

  @Override
  protected boolean checkAllPsiRoots() {
    return true;
  }

  public void testInjectionInJSString() {
    doTest(true);
  }
}
