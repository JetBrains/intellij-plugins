// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixture;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.RecursiveTreeElementWalkingVisitor;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.intellij.terraform.TfTestUtils;
import org.intellij.terraform.config.model.HclType;
import org.intellij.terraform.hcl.HCLParserDefinition;
import org.intellij.terraform.hcl.HCLTokenTypes;
import org.intellij.terraform.hcl.psi.HCLBlock;
import org.intellij.terraform.hcl.psi.HCLProperty;
import org.intellij.terraform.hcl.psi.common.BaseExpression;
import org.intellij.terraform.hcl.psi.common.ParameterList;
import org.intellij.terraform.hil.psi.TypeCachedValueProvider;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Objects;
import java.util.Set;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixtureKt.parsingFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("unused")
@TestDataPath("$CONTENT_ROOT/../test-data/terraform/types/")
public abstract class TfExpressionTypesTest {
  protected static final TestFixture<Project> projectFixture = projectFixture();

  protected final TestFixture<ParsingTestFixture> parsingFixture =
    parsingFixture(projectFixture, TfTestUtils.getTestDataPath() + "/terraform/types", "tf", false,
                   new TfParserDefinition(), new HCLParserDefinition());

  @Test
  public void testForListExpression() {
    doTest();
  }

  @Test
  public void testForObjectExpression() {
    doTest();
  }

  @Test
  public void testVariablesAndLocals() {
    doTest();
  }

  @Test
  public void testComplexVariables() {
    doTest();
  }

  @Test
  public void testVariablesAndLocalsSelect() {
    doTest();
  }

  @Test
  public void testResources() {
    doTest();
  }

  @Test
  public void testVariableWithOptionalAttribute() {
    doTest();
  }

  //region helpers
  protected void doTest() {
    ParsingTestFixture fixture = parsingFixture.get();
    String name = fixture.getTestName();
    PsiFile file = fixture.parseFile(name, fixture.loadFile(name + "." + fixture.getFileExt()));
    ReadAction.runBlocking(() -> doCheckResult(fixture.getFullDataPath(), file, name));
    fixture.ensureNoErrorElements(file);
  }

  private static void doCheckResult(@NotNull String testDataDir,
                                    @NotNull PsiFile file,
                                    @NotNull String targetDataName) {
    FileViewProvider provider = file.getViewProvider();
    Set<Language> languages = provider.getLanguages();
    assertEquals(1, languages.size());

    String expectedFileName = testDataDir + File.separatorChar + targetDataName + ".txt";
    StringBuilder buffer = new StringBuilder();
    final ASTNode node = ((PsiElement) file).getNode();
    assertNotNull(node);
    assertInstanceOf(TreeElement.class, node);
    ((TreeElement) node).acceptTree(new TreeToBuffer(node, buffer));
    String actual = buffer.toString().trim();
    UsefulTestCase.assertSameLinesWithFile(expectedFileName, actual);
  }

  private static class TreeToBuffer extends RecursiveTreeElementWalkingVisitor {
    private final StringBuilder buffer;
    private int indent;
    private boolean myOut = true;

    TreeToBuffer(ASTNode node, StringBuilder buffer) {
      this.buffer = buffer;
      this.indent = 0;
    }

    @Override
    protected void visitNode(TreeElement root) {
      if (isStartNode(root)) {
        myOut = true;
      } else if (isStopNode(root)) {
        myOut = false;
      }
      if (shouldSkipNode(root)) {
        indent += 2;
        return;
      }
      if (!myOut) {
        indent += 2;
        super.visitNode(root);
        return;
      }

      StringUtil.repeatSymbol(buffer, ' ', indent);
      PsiElement psiElement = root.getPsi();

      if (root instanceof CompositeElement) {
        buffer.append(Objects.requireNonNullElseGet(psiElement, root::getElementType));
      } else {
        final String text = fixWhiteSpaces(root.getText());
        buffer.append(root).append("('").append(text).append("')");
      }
      if (psiElement instanceof BaseExpression && !(psiElement instanceof ParameterList)) {
        HclType type = TypeCachedValueProvider.Companion.getType((BaseExpression)psiElement);
        if (type != null) {
          buffer.append(" - ").append(type.getPresentableText());
        }
      }
      if (psiElement instanceof HCLProperty || psiElement instanceof HCLBlock) {
        Document document = FileDocumentManager.getInstance().getDocument(psiElement.getContainingFile().getVirtualFile());
        assertNotNull(document);
        buffer.append(" at line ").append(document.getLineNumber(root.getStartOffset()) + 1);
      }
      buffer.append("\n");
      indent += 2;
      if (root instanceof CompositeElement && root.getFirstChildNode() == null) {
        StringUtil.repeatSymbol(buffer, ' ', indent);
        buffer.append("<empty list>\n");
      }

      super.visitNode(root);
    }

    private static boolean isStartNode(TreeElement root) {
      return HCLTokenTypes.HCL_COMMENTARIES.contains(root.getElementType()) && root.textMatches("#start");
    }

    private static boolean isStopNode(TreeElement root) {
      return HCLTokenTypes.HCL_COMMENTARIES.contains(root.getElementType()) && root.textMatches("#stop");
    }

    protected boolean shouldSkipNode(TreeElement node) {
      return node.getElementType() == TokenType.WHITE_SPACE || !(node instanceof CompositeElement);
    }

    @Override
    protected void elementFinished(@NotNull ASTNode e) {
      indent -= 2;
    }
  }

  private static String fixWhiteSpaces(String text) {
    return text.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
  }
  //endregion helpers
}
