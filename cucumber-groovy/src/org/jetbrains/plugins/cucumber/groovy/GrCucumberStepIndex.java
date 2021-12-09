// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.groovy;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.impl.source.tree.LightTreeUtil;
import com.intellij.psi.impl.source.tree.RecursiveLighterASTNodeWalkingVisitor;
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberStepIndex;
import org.jetbrains.plugins.groovy.GroovyFileType;

import java.util.ArrayList;
import java.util.List;

import static org.jetbrains.plugins.groovy.lang.psi.GroovyElementTypes.*;

public class GrCucumberStepIndex extends CucumberStepIndex {
  public static final ID<Boolean, List<Integer>> INDEX_ID = ID.create("groovy.cucumber.step");
  private static final String CUCUMBER_GROOVY_PACKAGE = "cucumber.api.groovy.";
  private static final String CUCUMBER_GROOVY_1_0_PACKAGE = "cucumber.runtime.groovy.";
  private static final String CUCUMBER_GROOVY_4_5_PACKAGE = "io.cucumber.groovy.";
  private static final String[] PACKAGES_TO_SCAN = new String[]{CUCUMBER_GROOVY_PACKAGE, CUCUMBER_GROOVY_1_0_PACKAGE, CUCUMBER_GROOVY_4_5_PACKAGE};

  @NotNull
  @Override
  public ID<Boolean, List<Integer>> getName() {
    return INDEX_ID;
  }

  @Override
  public int getVersion() {
    return 6;
  }

  @Override
  protected String[] getPackagesToScan() {
    return PACKAGES_TO_SCAN;
  }

  @NotNull
  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return new DefaultFileTypeSpecificInputFilter(GroovyFileType.GROOVY_FILE_TYPE);
  }

  @Override
  protected List<Integer> getStepDefinitionOffsets(@NotNull LighterAST lighterAst, @NotNull CharSequence text) {
    List<Integer> result = new ArrayList<>();

    RecursiveLighterASTNodeWalkingVisitor visitor = new RecursiveLighterASTNodeWalkingVisitor(lighterAst) {
      @Override
      public void visitNode(@NotNull LighterASTNode element) {
        if (element.getTokenType() == METHOD_CALL_EXPRESSION) {
          List<LighterASTNode> methodNameAndArgumentList = lighterAst.getChildren(element);
          if (methodNameAndArgumentList.size() < 2) {
            super.visitNode(element);
            return;
          }
          LighterASTNode methodNameNode = methodNameAndArgumentList.get(0);
          if (methodNameNode != null && isStepDefinitionCall(methodNameNode, text)) {
            LighterASTNode expressionList = methodNameAndArgumentList.get(1);
            if (expressionList.getTokenType() == ARGUMENT_LIST) {
              List<LighterASTNode> expressionListChildren = LightTreeUtil.getChildrenOfType(lighterAst, expressionList, UNARY_EXPRESSION);
              if (expressionListChildren.size() > 0) {
                LighterASTNode expressionParameter = expressionListChildren.get(0);
                result.add(expressionParameter.getStartOffset());
              }
            }
          }
        }
        super.visitNode(element);
      }
    };
    visitor.visitNode(lighterAst.getRoot());

    return result;
  }
}
