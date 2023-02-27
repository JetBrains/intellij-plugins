/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.config;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.RecursiveTreeElementWalkingVisitor;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.testFramework.ParsingTestCase;
import com.intellij.testFramework.TestDataFile;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.util.KeyedLazyInstance;
import org.intellij.terraform.TerraformTestUtils;
import org.intellij.terraform.hcl.HCLParserDefinition;
import org.intellij.terraform.hcl.HCLTokenTypes;
import org.intellij.terraform.hcl.psi.HCLBlock;
import org.intellij.terraform.hcl.psi.HCLProperty;
import org.intellij.terraform.hcl.psi.common.BaseExpression;
import org.intellij.terraform.hcl.psi.common.ParameterList;
import org.intellij.terraform.config.TerraformParserDefinition;
import org.intellij.terraform.config.model.Type;
import org.intellij.terraform.config.model.TypeModelProvider;
import org.intellij.terraform.config.psi.TerraformReferenceContributor;
import org.intellij.terraform.hil.psi.ILReferenceContributor;
import org.intellij.terraform.hil.psi.TypeCachedValueProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("unused")
@TestDataPath("$CONTENT_ROOT/test-data/terraform/types/")
public abstract class TerraformExpressionTypesTest extends ParsingTestCase {

  public void testForListExpression() {
    doTest();
  }

  public void testForObjectExpression() {
    doTest();
  }

  public void testVariablesAndLocals() {
    doTest();
  }

  public void testComplexVariables() {
    doTest();
  }

  public void testVariablesAndLocalsSelect() {
    doTest();
  }

  public void testResources() {
    doTest();
  }

  public void testVariableWithOptionalAttribute() {
    doTest();
  }

  //region helpers
  public TerraformExpressionTypesTest() {
    super("terraform/types", "tf", false, new TerraformParserDefinition(), new HCLParserDefinition());
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    registerMockInjectedLanguageManager();
    //registerExtensionPoint(PsiReferenceContributor.EP_NAME, PsiReferenceContributor.class);
    registerExtension(PsiReferenceContributor.EP_NAME, new MyPsiReferenceContributor("HCL", new ILReferenceContributor(), new TerraformReferenceContributor()));
    registerExtension(PsiReferenceContributor.EP_NAME, new MyPsiReferenceContributor("HIL", new ILReferenceContributor()));
    getApplication().registerService(TypeModelProvider.class, new TypeModelProvider());
  }

  @Override
  protected String getTestDataPath() {
    return TerraformTestUtils.getTestDataPath();
  }

  protected void doTest() {
    doTest(true, true);
  }

  @Override
  protected void checkResult(@NotNull @TestDataFile String targetDataName, @NotNull PsiFile file) {
    doCheckResult(myFullDataPath, file, targetDataName);
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
    assertInstanceOf(node, TreeElement.class);
    ((TreeElement) node).acceptTree(new TreeToBuffer(buffer));
    String actual = buffer.toString().trim();
    UsefulTestCase.assertSameLinesWithFile(expectedFileName, actual);
  }

  private static class TreeToBuffer extends RecursiveTreeElementWalkingVisitor {
    private final StringBuilder buffer;
    private int indent;
    private boolean myOut = true;

    TreeToBuffer(StringBuilder buffer) {
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
        Type type = TypeCachedValueProvider.Companion.getType((BaseExpression) psiElement);
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

  private static class MyPsiReferenceContributor extends PsiReferenceContributor implements KeyedLazyInstance<PsiReferenceContributor> {
    private final String myKey;
    private final PsiReferenceContributor[] myContributors;

    private MyPsiReferenceContributor(String key, PsiReferenceContributor... contributors) {
      myKey = key;
      myContributors = contributors;
    }

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
      for (PsiReferenceContributor contributor : myContributors) {
        contributor.registerReferenceProviders(registrar);
      }
    }

    @Override
    public String getKey() {
      return myKey;
    }

    @NotNull
    @Override
    public PsiReferenceContributor getInstance() {
      return this;
    }
  }

  private static String fixWhiteSpaces(String text) {
    return text.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
  }
  //endregion helpers
}
