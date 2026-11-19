package org.angularjs.lang.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.angularjs.AngularTestUtil;
import org.angularjs.lang.lexer.AngularJSLexer;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Runs one test for each {@code .js} file of the test data. Each line of the file is one expression.
 *
 * @author Dennis.Ushakov
 */
@TestApplication
public class AngularJSParserTest {
  private static final TestFixture<Project> projectFixture = projectFixture();

  static Stream<Named<String>> testFiles() {
    String path = getTestDataPath();
    return Arrays.stream(Objects.requireNonNull(new File(path).list()))
      .filter(name -> name.endsWith("js"))
      .sorted()
      .map(name -> Named.of(name, name));
  }

  private static String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(AngularJSParserTest.class);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testFiles")
  public void runSingle(String suffix) throws IOException {
    String path = getTestDataPath();
    final String text = FileUtil.loadFile(new File(path, suffix), true);
    final StringBuilder result = new StringBuilder();
    for (String line : StringUtil.splitByLines(text)) {
      if (result.length() > 0) result.append("------\n");
      final AngularJSParserDefinition definition = new AngularJSParserDefinition();
      result.append(ReadAction.compute(() -> {
        final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(definition, new AngularJSLexer(), line);
        final ASTNode root = definition.createParser(projectFixture.get()).parse(AngularJSParserDefinition.FILE, builder);
        return DebugUtil.psiToString(root.getPsi(), true, false);
      }));
    }

    assertEquals(FileUtil.loadFile(new File(path, suffix.replace("js", "txt")), true), result.toString());
  }
}
