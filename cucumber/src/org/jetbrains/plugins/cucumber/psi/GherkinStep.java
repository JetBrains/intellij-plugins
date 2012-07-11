package org.jetbrains.plugins.cucumber.psi;

import com.intellij.lang.ASTNode;
import com.intellij.pom.PomTarget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * @author yole
 */
public interface GherkinStep extends GherkinPsiElement, GherkinSuppressionHolder, PomTarget {
  GherkinStep[] EMPTY_ARRAY = new GherkinStep[0];

  ASTNode getKeyword();
  String getStepName();

  String getStepNameWithParameters();

  @Nullable
  GherkinTable getTable();

  @Nullable
  GherkinPystring getPystring();

  GherkinStepsHolder getStepHolder();
  /**
   * @return List with not empty unique possible substitutions names
   */
  List<String> getParamsSubstitutions();

  @Nullable
  String getSubstitutedName();

  @NotNull
  Set<String> getSubstitutedNameList();
}
