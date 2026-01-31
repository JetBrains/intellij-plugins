// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.jhipster.model.JdlEnumListType;
import com.intellij.jhipster.model.JdlEnumType;
import com.intellij.jhipster.model.JdlOptionMapping;
import com.intellij.jhipster.model.JdlOptionModel;
import com.intellij.jhipster.psi.JdlAnnotationId;
import com.intellij.jhipster.psi.JdlConfigBlock;
import com.intellij.jhipster.psi.JdlConfigKeyword;
import com.intellij.jhipster.psi.JdlConfigurationOptionName;
import com.intellij.jhipster.psi.JdlConstantName;
import com.intellij.jhipster.psi.JdlDeployment;
import com.intellij.jhipster.psi.JdlEntityId;
import com.intellij.jhipster.psi.JdlEnumId;
import com.intellij.jhipster.psi.JdlEnumKey;
import com.intellij.jhipster.psi.JdlFieldConstraintId;
import com.intellij.jhipster.psi.JdlFieldConstraintParameters;
import com.intellij.jhipster.psi.JdlFieldName;
import com.intellij.jhipster.psi.JdlFieldNameRef;
import com.intellij.jhipster.psi.JdlFieldType;
import com.intellij.jhipster.psi.JdlId;
import com.intellij.jhipster.psi.JdlOptionName;
import com.intellij.jhipster.psi.JdlOptionNameValue;
import com.intellij.jhipster.psi.JdlRelationshipOptionId;
import com.intellij.jhipster.psi.JdlRelationshipType;
import com.intellij.jhipster.psi.JdlTokenTypes;
import com.intellij.jhipster.psi.JdlWildcardLiteral;
import com.intellij.jhipster.psi.JdlWithOptionValue;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;

import static com.intellij.jhipster.JdlConstants.APPLICATION_BASE_NAME;
import static com.intellij.psi.search.GlobalSearchScope.allScope;
import static com.intellij.psi.util.PsiTreeUtil.findFirstParent;

public final class JdlAnnotator implements Annotator, DumbAware {
  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (element instanceof JdlConfigKeyword) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(element.getTextRange())
        .textAttributes(JdlSyntaxHighlighter.JDL_KEYWORD)
        .create();
    }
    else if (element instanceof JdlWildcardLiteral) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(element.getTextRange())
        .textAttributes(JdlSyntaxHighlighter.JDL_KEYWORD)
        .create();
    }
    else if (element instanceof JdlOptionName) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(element.getTextRange())
        .textAttributes(JdlSyntaxHighlighter.JDL_OPTION_NAME)
        .create();
    }
    else if (element instanceof JdlFieldName || element instanceof JdlFieldNameRef) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(element.getTextRange())
        .textAttributes(JdlSyntaxHighlighter.JDL_FIELD_NAME)
        .create();
    }
    else if ((element instanceof JdlWithOptionValue) || (element instanceof JdlEnumKey)) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(element.getTextRange())
        .textAttributes(JdlSyntaxHighlighter.JDL_OPTION_ENUM_VALUE)
        .create();
    }
    else if (element instanceof JdlEntityId || element instanceof JdlEnumId) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(element.getTextRange())
        .textAttributes(JdlSyntaxHighlighter.JDL_IDENTIFIER)
        .create();
    }
    else if (element instanceof JdlId) {
      PsiElement idParent = element.getParent();
      if (idParent instanceof JdlFieldConstraintParameters) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
          .range(element.getTextRange())
          .textAttributes(JdlSyntaxHighlighter.JDL_CONSTANT)
          .create();
      }
      else {
        PsiElement optionNameValue = findFirstParent(idParent, p -> p instanceof JdlOptionNameValue);
        if (optionNameValue instanceof JdlOptionNameValue) {
          annotateOptionNameEnumValue(element, holder, (JdlOptionNameValue)optionNameValue);
        }
      }
    }
    else if (element instanceof JdlFieldConstraintId) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(element.getTextRange())
        .textAttributes(JdlSyntaxHighlighter.JDL_FIELD_CONSTRAINT)
        .create();
    }
    else if (element instanceof JdlRelationshipType) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(element.getTextRange())
        .textAttributes(JdlSyntaxHighlighter.JDL_OPTION_ENUM_VALUE)
        .create();
    }
    else if (element.getNode().getElementType() == JdlTokenTypes.IDENTIFIER) {
      if (element.getParent() instanceof JdlConstantName) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
          .range(element.getTextRange())
          .textAttributes(JdlSyntaxHighlighter.JDL_CONSTANT)
          .create();
      }
    }
    else if (element instanceof JdlConfigurationOptionName) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(element.getTextRange())
        .textAttributes(JdlSyntaxHighlighter.JDL_KEYWORD)
        .create();
    }
    else if (element instanceof JdlAnnotationId || element instanceof JdlRelationshipOptionId) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(element.getTextRange())
        .textAttributes(JdlSyntaxHighlighter.JDL_ANNOTATION)
        .create();
    }

    resolveIdentifierRefs(element, holder);
  }

  private void resolveIdentifierRefs(PsiElement element, AnnotationHolder holder) {
    if (element instanceof JdlFieldType
        && looksLikeJdkType((JdlFieldType)element)
        && !isJdkConfigured(element.getProject())) {
      return;
    }

    if (element instanceof JdlId || element instanceof JdlFieldType) {
      var reference = element.getReference();
      if (reference != null && reference.resolve() == null) {
        var message = JdlBundle.message("inspection.message.cannot.resolve.symbol.0", reference.getCanonicalText());
        holder.newAnnotation(HighlightSeverity.ERROR, message)
          .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
          .range(element)
          .create();
      }
    }
  }

  private static boolean looksLikeJdkType(@NotNull JdlFieldType fieldType) {
    return JdlConstants.FIELD_TYPES.containsKey(fieldType.getTypeName());
  }

  private static boolean isJdkConfigured(@NotNull Project project) {
    if (DumbService.isDumb(project)) return false;

    return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
      PsiClass stringClass = JavaPsiFacade.getInstance(project).findClass("java.lang.String", allScope(project));
      return Result.create(stringClass != null, ProjectRootManager.getInstance(project));
    });
  }

  private static void annotateOptionNameEnumValue(@NotNull PsiElement element, @NotNull AnnotationHolder holder,
                                                JdlOptionNameValue optionNameValue) {
    var optionName = optionNameValue.getOptionName();
    var optionKey = optionName.getText();

    if (APPLICATION_BASE_NAME.equals(optionKey) && optionNameValue.getParent() instanceof JdlConfigBlock) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(element.getTextRange())
        .textAttributes(JdlSyntaxHighlighter.JDL_BASE_NAME)
        .create();
    }
    else {
      JdlOptionMapping optionMapping;
      if (findFirstParent(element, p -> p instanceof JdlConfigBlock) != null) {
        optionMapping = JdlOptionModel.INSTANCE.getApplicationConfigOptions().get(optionKey);
      }
      else if (findFirstParent(element, p -> p instanceof JdlDeployment) != null) {
        optionMapping = JdlOptionModel.INSTANCE.getDeploymentOptions().get(optionKey);
      }
      else {
        optionMapping = null;
      }

      if (optionMapping != null && isEnumType(optionMapping)) {
        if (!JdlConstants.FALSE.equals(element.getText())) { // false is highlighted as boolean instead
          holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(element.getTextRange())
            .textAttributes(JdlSyntaxHighlighter.JDL_OPTION_ENUM_VALUE)
            .create();
        }
      }
    }
  }

  private static boolean isEnumType(JdlOptionMapping optionMapping) {
    return optionMapping.getPropertyType() instanceof JdlEnumType
           || optionMapping.getPropertyType() instanceof JdlEnumListType;
  }
}