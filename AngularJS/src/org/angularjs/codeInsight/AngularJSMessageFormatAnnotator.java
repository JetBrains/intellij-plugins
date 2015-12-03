package org.angularjs.codeInsight;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.angularjs.lang.parser.AngularJSMessageFormatParser;
import org.angularjs.lang.psi.AngularJSElementVisitor;
import org.angularjs.lang.psi.AngularJSMessageFormatExpression;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Irina.Chernushina on 12/3/2015.
 */
public class AngularJSMessageFormatAnnotator extends AngularJSElementVisitor implements Annotator {
  private AnnotationHolder myHolder;

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    try {
      assert myHolder == null;
      myHolder = holder;
      element.accept(this);
    } finally {
      myHolder = null;
    }
  }

  @Override
  public void visitMessageFormatExpression(@NotNull final AngularJSMessageFormatExpression expression) {
    final AngularJSMessageFormatParser.ExtensionType type = expression.getExtensionType();
    if (type == null) myHolder.createErrorAnnotation(expression, "missing or unknown message format extension");// will not happen, but
    final List<PsiElement> elements = expression.getSelectionKeywordElements();
    final List<String> selectionKeywords = ContainerUtil.map(elements, new Function<PsiElement, String>() {
      @Override
      public String fun(PsiElement element) {
        return element.getText();
      }
    });

    checkForRequiredSelectionKeywords(type, expression, selectionKeywords);
    checkForDuplicateSelectionKeywords(selectionKeywords, elements);
    checkForSelectionKeywordValues(type, selectionKeywords, elements);
  }

  private void checkForSelectionKeywordValues(AngularJSMessageFormatParser.ExtensionType type,
                                              List<String> keywords,
                                              List<PsiElement> elements) {
    if (AngularJSMessageFormatParser.ExtensionType.plural.equals(type)) {
      final Map<String, String> errors = new HashMap<String, String>();
      for (String keyword : keywords) {
        if (keyword.startsWith("=")) {
          try {
            Integer.parseInt(keyword.substring(1));
          } catch (NumberFormatException e) {
            errors.put(keyword, "Integer expected after =");
          }
        } else {
          try {
            AngularJSPluralCategories.valueOf(keyword);
          } catch (IllegalArgumentException e) {
            errors.put(keyword, "Expected plural category");
          }
        }
      }
      if (!errors.isEmpty()) {
        for (PsiElement element : elements) {
          final String errorText = errors.get(element.getText());
          if (errorText != null) {
            myHolder.createErrorAnnotation(element, errorText);
          }
        }
      }
    }
  }

  private void checkForDuplicateSelectionKeywords(List<String> keywords, List<PsiElement> elements) {
    final Set<String> passedSet = new HashSet<String>();
    final Set<String> duplicate = new HashSet<String>(ContainerUtil.filter(keywords, new Condition<String>() {
      @Override
      public boolean value(String s) {
        final boolean contains = passedSet.contains(s);
        if (!contains) passedSet.add(s);
        return contains;
      }
    }));
    if (!duplicate.isEmpty()) {
      for (PsiElement element : elements) {
        if (duplicate.contains(element.getText())) {
          myHolder.createErrorAnnotation(element, "Duplicate selection keyword");
        }
      }
    }
  }

  private void checkForRequiredSelectionKeywords(AngularJSMessageFormatParser.ExtensionType type,
                                                 @NotNull AngularJSMessageFormatExpression expression,
                                                 List<String> selectionKeywords) {
    if (type != null) {
      final Set<String> requiredKeywords = type.getRequiredSelectionKeywords();
      if (!requiredKeywords.isEmpty()) {
        for (String requiredKeyword : requiredKeywords) {
          if (!selectionKeywords.contains(requiredKeyword)) {
            myHolder.createErrorAnnotation(expression, "Missing required selection keyword '" + requiredKeyword + "'");
          }
        }
      }
    }
  }
}
