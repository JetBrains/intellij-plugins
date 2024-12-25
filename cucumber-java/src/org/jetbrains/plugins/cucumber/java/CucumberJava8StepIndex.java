// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.source.JavaFileElementType;
import com.intellij.psi.impl.source.JavaLightTreeUtil;
import com.intellij.psi.impl.source.tree.RecursiveLighterASTNodeWalkingVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberStepIndex;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.psi.impl.source.tree.JavaElementType.*;

public final class CucumberJava8StepIndex extends CucumberStepIndex {
  public static final ID<Boolean, List<Integer>> INDEX_ID = ID.create("java.cucumber.java8.step");
  private static final String JAVA_8_PACKAGE = "cucumber.api.java8.";
  private static final String JAVA_8_CUCUMBER_4_5_PACKAGE = "io.cucumber.java8.";
  private static final String[] PACKAGES_TO_SCAN = new String[]{JAVA_8_CUCUMBER_4_5_PACKAGE, JAVA_8_PACKAGE};

  @Override
  public @NotNull ID<Boolean, List<Integer>> getName() {
    return INDEX_ID;
  }

  @Override
  public int getVersion() {
    return 1;
  }

  @Override
  public boolean hasSnapshotMapping() {
    return true;
  }

  @Override
  protected String[] getPackagesToScan() {
    return PACKAGES_TO_SCAN;
  }

  @Override
  public @NotNull FileBasedIndex.InputFilter getInputFilter() {
    return new DefaultFileTypeSpecificInputFilter(JavaFileType.INSTANCE) {
      @Override
      public boolean acceptInput(@NotNull VirtualFile file) {
        // ToDo: remove
        return super.acceptInput(file) && JavaFileElementType.isInSourceContent(file);
      }
    };
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
            if (expressionList.getTokenType() == EXPRESSION_LIST) {
              List<LighterASTNode> expressionListChildren = JavaLightTreeUtil.getExpressionChildren(lighterAst, expressionList);
              if (expressionListChildren.size() > 1) {
                LighterASTNode expressionParameter = expressionListChildren.get(0);
                if (isStringLiteral(expressionParameter, text)) {
                  LighterASTNode stepDefImplementationArgument = expressionListChildren.get(1);
                  if (isNumber(stepDefImplementationArgument, text)) {
                    stepDefImplementationArgument = expressionListChildren.get(2);
                  }
                  IElementType type = stepDefImplementationArgument.getTokenType();
                  if (type == METHOD_REF_EXPRESSION || type == LOCAL_VARIABLE || type == LAMBDA_EXPRESSION) {
                    result.add(expressionParameter.getStartOffset());
                  }
                }
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
