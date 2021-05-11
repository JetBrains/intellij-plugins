package org.angularjs.lang.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.FileBasedTestCaseHelperEx;
import com.intellij.testFramework.LightPlatformCodeInsightTestCase;
import org.angularjs.AngularTestUtil;
import org.angularjs.lang.lexer.AngularJSLexer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * @author Dennis.Ushakov
 */
@RunWith(com.intellij.testFramework.Parameterized.class)
public class AngularJSParserTest extends LightPlatformCodeInsightTestCase implements FileBasedTestCaseHelperEx {
  @NotNull
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(AngularJSParserTest.class);
  }

  @Nullable
  @Override
  public String getFileSuffix(String fileName) {
    return fileName.endsWith("js") ? fileName : null;
  }

  @Test
  public void runSingle() throws Throwable {
    doSingleTest(myFileSuffix, myTestDataPath);
  }

  private void doSingleTest(String suffix, String path) throws Throwable{
    final String text = FileUtil.loadFile(new File(path, suffix), true);
    final StringBuilder result = new StringBuilder();
    for (String line : StringUtil.splitByLines(text)) {
      if (result.length() > 0) result.append("------\n");
      final AngularJSParserDefinition definition = new AngularJSParserDefinition();
      final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(definition, new AngularJSLexer(), line);
      final ASTNode root = definition.createParser(getProject()).parse(AngularJSParserDefinition.FILE, builder);
    result.append(DebugUtil.psiToString(root.getPsi(), true, false));
    }    

    assertEquals(FileUtil.loadFile(new File(path, suffix.replace("js", "txt")), true), result.toString());
  }

  @Override
  public String getRelativeBasePath() {
    return "";
  }
}
