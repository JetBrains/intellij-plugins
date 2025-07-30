// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.application.options.CodeStyle;
import com.intellij.html.embedding.HtmlEmbeddedContentSupport;
import com.intellij.javascript.JSEmbeddedTokenTypesProvider;
import com.intellij.javascript.JSHtmlEmbeddedContentSupport;
import com.intellij.lang.LanguageASTFactory;
import com.intellij.lang.LanguageHtmlScriptContentProvider;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.lang.css.CSSParserDefinition;
import com.intellij.lang.dtd.DTDLanguage;
import com.intellij.lang.dtd.DTDParserDefinition;
import com.intellij.lang.javascript.JavascriptParserDefinition;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.lang.xml.XMLParserDefinition;
import com.intellij.lang.xml.XmlASTFactory;
import com.intellij.lexer.EmbeddedTokenTypesProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.options.SchemeManagerFactory;
import com.intellij.psi.LanguageFileViewProviders;
import com.intellij.psi.codeStyle.*;
import com.intellij.psi.css.impl.CssTreeElementFactory;
import com.intellij.psi.impl.source.codeStyle.PersistableCodeStyleSchemes;
import com.intellij.psi.xml.StartTagEndTokenProvider;
import com.intellij.testFramework.ParsingTestCase;
import com.jetbrains.plugins.jade.formatter.JadeCodeStyleSettingsProvider;
import com.jetbrains.plugins.jade.formatter.JadeLanguageCodeStyleSettingsProvider;
import com.jetbrains.plugins.jade.js.JavascriptInJadeParserDefinition;
import com.jetbrains.plugins.jade.psi.JadeAstFactory;
import org.coffeescript.CoffeeScriptLanguage;
import org.coffeescript.lang.CoffeeScriptEmbeddedTokenTypesProvider;
import org.coffeescript.lang.CoffeeScriptHtmlScriptContentProvider;
import org.coffeescript.lang.parser.CoffeeScriptParserDefinition;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.javascript.JSElementTypeServiceHelper.registerJSElementTypeServices;
import static com.intellij.xml.testFramework.XmlElementTypeServiceHelper.registerXmlElementTypeServices;

public abstract class JadeBaseParsingTestCase extends ParsingTestCase {
  public JadeBaseParsingTestCase() {
    super("", "jade", true, new JadeParserDefinition(), new JavascriptInJadeParserDefinition(), new JavascriptParserDefinition(),
          new XMLParserDefinition(), new DTDParserDefinition(), new CSSParserDefinition(), new CoffeeScriptParserDefinition());
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    registerXmlElementTypeServices(getApplication(), getTestRootDisposable());
    registerJSElementTypeServices(getApplication(), getTestRootDisposable());

    addExplicitExtension(LanguageFileViewProviders.INSTANCE, JadeLanguage.INSTANCE, new JadeFileViewProviderFactory());
    addExplicitExtension(LanguageASTFactory.INSTANCE, JadeLanguage.INSTANCE, new JadeAstFactory());
    addExplicitExtension(LanguageASTFactory.INSTANCE, XMLLanguage.INSTANCE, new XmlASTFactory());
    addExplicitExtension(LanguageASTFactory.INSTANCE, DTDLanguage.INSTANCE, new XmlASTFactory());
    addExplicitExtension(LanguageASTFactory.INSTANCE, CSSLanguage.INSTANCE, new CssTreeElementFactory());

    addExplicitExtension(LanguageHtmlScriptContentProvider.INSTANCE, CoffeeScriptLanguage.INSTANCE,
                         new CoffeeScriptHtmlScriptContentProvider());
    HtmlEmbeddedContentSupport.register(getApplication(), getTestRootDisposable(), JSHtmlEmbeddedContentSupport.class);

    registerExtensionPoint(new ExtensionPointName<>("com.intellij.xml.startTagEndToken"),
                           StartTagEndTokenProvider.class);
    registerExtensionPoint(CodeStyleSettingsProvider.EXTENSION_POINT_NAME, CodeStyleSettingsProvider.class);
    registerExtensionPoint(LanguageCodeStyleSettingsProvider.EP_NAME, LanguageCodeStyleSettingsProvider.class);
    registerExtensionPoint(LanguageCodeStyleSettingsContributor.EP_NAME, LanguageCodeStyleSettingsContributor.class);
    registerExtensionPoint(FileTypeIndentOptionsProvider.EP_NAME, FileTypeIndentOptionsProvider.class);
    registerExtensionPoint(new ExtensionPointName<>("com.intellij.codeStyleSettingsProvider"),
                           JadeCodeStyleSettingsProvider.class);

    registerExtensionPoint(FileIndentOptionsProvider.EP_NAME, FileIndentOptionsProvider.class);
    getApplication().registerService(CodeStyleSettingsService.class, new CodeStyleSettingsServiceImpl());
    LanguageCodeStyleSettingsProvider.mockLanguageCodeStyleSettingsProviderService((aClass, o) -> {
      //noinspection unchecked
      getApplication().registerService((Class<Object>)aClass, o);
    });
    getProject().registerService(ProjectCodeStyleSettingsManager.class, new ProjectCodeStyleSettingsManager(getProject(), false));
    getApplication().registerService(AppCodeStyleSettingsManager.class, new AppCodeStyleSettingsManager());
    getApplication().registerService(CodeStyleSchemes.class, new PersistableCodeStyleSchemes(
      ApplicationManager.getApplication().getComponent(SchemeManagerFactory.class)));
    registerExtensionPoint(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, EmbeddedTokenTypesProvider.class);

    registerExtension(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, new CoffeeScriptEmbeddedTokenTypesProvider());
    registerExtension(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, new JSEmbeddedTokenTypesProvider());
    registerExtension(LanguageCodeStyleSettingsProvider.EP_NAME, new JadeLanguageCodeStyleSettingsProvider());


    CodeStyleSettings settings = CodeStyle.createTestSettings();
    CodeStyleSettingsManager.getInstance(getProject()).setTemporarySettings(settings);
  }

  @Override
  public void configureFromParserDefinition(@NotNull ParserDefinition definition, String extension) {
    super.configureFromParserDefinition(definition, extension);
  }
}
