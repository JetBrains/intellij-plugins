// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes;

import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.Angular2InjectionUtils;
import org.angular2.entities.Angular2Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

import static com.intellij.lang.javascript.JSTokenTypes.COMMA;
import static com.intellij.util.ArrayUtil.getLastElement;
import static com.intellij.util.ObjectUtils.notNull;

public final class Angular2FixesPsiUtil {

  public static boolean insertNgModuleMember(Angular2Module module, String propertyName, String name) {
    ES6Decorator decorator = module.getDecorator();
    if (decorator == null) {
      return false;
    }
    JSObjectLiteralExpression initializer = Angular2DecoratorUtil.getObjectLiteralInitializer(decorator);
    if (initializer == null) {
      return false;
    }
    JSProperty targetListProp = initializer.findProperty(propertyName);
    if (targetListProp == null) {
      reformatJSObjectLiteralProperty(
        insertJSObjectLiteralProperty(initializer, propertyName, "[\n" + name + "\n]")
      );
    }
    else {
      JSExpression propValue = targetListProp.getValue();
      if (propValue == null) {
        return false;
      }
      else if (!(propValue instanceof JSArrayLiteralExpression)) {
        JSProperty newProperty = (JSProperty)JSChangeUtil.createObjectLiteralPropertyFromText(
          propertyName + ": [\n" + propValue.getText() + "\n]", initializer);
        targetListProp = (JSProperty)targetListProp.replace(newProperty);
        propValue = targetListProp.getValue();
        assert propValue instanceof JSArrayLiteralExpression;
      }

      JSReferenceExpression newModuleIdent = JSChangeUtil.createExpressionPsiWithContext(
        name, propValue, JSReferenceExpression.class);
      assert newModuleIdent != null;
      insertNewLinesAroundArrayItemIfNeeded(
        (JSExpression)propValue.addAfter(newModuleIdent,
                                         notNull(getLastElement(((JSArrayLiteralExpression)propValue).getExpressions()),
                                                 propValue.getFirstChild())));
      FormatFixer.create(targetListProp, FormatFixer.Mode.Reformat).fixFormat();
    }
    return true;
  }

  public static JSProperty insertJSObjectLiteralProperty(@NotNull JSObjectLiteralExpression objectLiteral,
                                                         @NotNull String propertyName,
                                                         @NotNull String propertyValue) {
    PsiElement property = JSChangeUtil.createObjectLiteralPropertyFromText(propertyName + ": " + propertyValue, objectLiteral);
    JSProperty added = (JSProperty)JSRefactoringUtil.addMemberToMemberHolder(objectLiteral, property, objectLiteral);
    insertNewLinesAroundPropertyIfNeeded(added);
    return added;
  }

  public static @NotNull JSProperty reformatJSObjectLiteralProperty(JSProperty property) {
    SmartPsiElementPointer<JSProperty> propertyPointer = SmartPointerManager.createPointer(property);
    FormatFixer.create(property.getParent(), FormatFixer.Mode.Reformat).fixFormat();
    property = propertyPointer.getElement();
    assert property != null;

    PsiDocumentManager documentManager = PsiDocumentManager.getInstance(property.getProject());
    Document document = documentManager.getDocument(property.getContainingFile());
    assert document != null;
    documentManager.commitDocument(document);
    PsiFile htmlContent = Angular2InjectionUtils.getFirstInjectedFile(property.getValue());
    if (htmlContent != null) {
      FormatFixer.create(htmlContent, FormatFixer.Mode.Reformat).fixFormat();
    }
    return propertyPointer.getElement();
  }

  private static void insertNewLinesAroundArrayItemIfNeeded(@NotNull JSExpression expression) {
    insertNewLinesAroundItemHolderIfNeeded(expression, JSArrayLiteralExpression::getExpressions);
  }

  private static void insertNewLinesAroundPropertyIfNeeded(@NotNull JSProperty property) {
    insertNewLinesAroundItemHolderIfNeeded(property, JSObjectLiteralExpression::getProperties);
  }

  private static <T extends PsiElement> void insertNewLinesAroundItemHolderIfNeeded(@NotNull JSElement item,
                                                                                    Function<T, ? extends JSElement[]> getChildren) {
    @SuppressWarnings("unchecked") final T parent = (T)item.getParent();
    boolean wrapWithNewLines = item.getText().contains("\n")
                               || ContainerUtil.find(getChildren.apply(parent),
                                                     e -> e != item && !isPrefixedWithNewLine(e)) == null;
    if (wrapWithNewLines) {
      if (!isPrefixedWithNewLine(item)) {
        JSChangeUtil.addWs(parent.getNode(), item.getNode(), "\n");
      }
      LeafPsiElement comma = findCommaOrBracket(item);
      if (comma != null) {
        PsiElement next = comma.getNextSibling();
        if (!(next instanceof PsiWhiteSpace) || !next.getText().contains("\n")) {
          JSChangeUtil.addWsAfter(parent, comma, "\n");
        }
      }
      else {
        for (PsiElement e = item.getNextSibling(); e != null; e = e.getNextSibling()) {
          if (e instanceof PsiWhiteSpace && e.getText().contains("\n")) {
            return;
          }
        }
        JSChangeUtil.addWsAfter(parent, item, "\n");
      }
    }
  }

  private static boolean isPrefixedWithNewLine(@NotNull JSElement property) {
    PsiWhiteSpace whiteSpace = ObjectUtils.tryCast(property.getPrevSibling(), PsiWhiteSpace.class);
    return whiteSpace != null && whiteSpace.getText().contains("\n");
  }

  private static @Nullable LeafPsiElement findCommaOrBracket(@NotNull JSElement property) {
    PsiElement el = PsiTreeUtil.skipWhitespacesForward(property);
    if (!(el instanceof LeafPsiElement)
        || el.getNode().getElementType() != COMMA) {
      return null;
    }
    return (LeafPsiElement)el;
  }
}
