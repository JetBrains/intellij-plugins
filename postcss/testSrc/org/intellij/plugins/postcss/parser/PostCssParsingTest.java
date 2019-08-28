package org.intellij.plugins.postcss.parser;

import com.intellij.lang.LanguageASTFactory;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.lang.css.CSSParserDefinition;
import com.intellij.psi.css.CssElementDescriptorProvider;
import com.intellij.psi.css.impl.CssTreeElementFactory;
import com.intellij.psi.css.impl.util.scheme.CssElementDescriptorFactory2;
import com.intellij.psi.css.impl.util.scheme.CssElementDescriptorProviderImpl;
import com.intellij.testFramework.ParsingTestCase;
import com.intellij.testFramework.TestDataPath;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.intellij.plugins.postcss.PostCssTestUtils;
import org.intellij.plugins.postcss.descriptors.PostCssElementDescriptorProvider;
import org.intellij.plugins.postcss.psi.impl.PostCssTreeElementFactory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

@TestDataPath("$CONTENT_ROOT/testData/parser/")
public abstract class PostCssParsingTest extends ParsingTestCase {
  public PostCssParsingTest(@NonNls @NotNull String dataPath) {
    super(dataPath, "pcss", new PostCssParserDefinition(), new CSSParserDefinition());
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    registerExtensionPoint(CssElementDescriptorProvider.EP_NAME, CssElementDescriptorProvider.class);
    registerExtension(CssElementDescriptorProvider.EP_NAME, new CssElementDescriptorProviderImpl());
    registerExtension(CssElementDescriptorProvider.EP_NAME, new PostCssElementDescriptorProvider());

    getApplication().registerService(CssElementDescriptorFactory2.class, new CssElementDescriptorFactory2("css-parsing-tests.xml"));

    addExplicitExtension(LanguageASTFactory.INSTANCE, PostCssLanguage.INSTANCE, new PostCssTreeElementFactory());
    addExplicitExtension(LanguageASTFactory.INSTANCE, CSSLanguage.INSTANCE, new CssTreeElementFactory());
  }

  protected void doTest() {
    super.doTest(true);
  }

  @Override
  protected String getTestDataPath() {
    return PostCssTestUtils.getFullTestDataPath(getClass());
  }
}
