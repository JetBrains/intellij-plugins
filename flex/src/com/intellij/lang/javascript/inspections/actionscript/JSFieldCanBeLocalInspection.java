// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.inspections.actionscript;

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.findUsages.JSReadWriteAccessDetector;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.inspections.JSInspection;
import com.intellij.lang.javascript.inspections.actionscript.fixes.ConvertToLocalFix;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlText;
import org.jetbrains.annotations.NotNull;

import java.util.*;

// TODO: control flow
public class JSFieldCanBeLocalInspection extends JSInspection {
  private static final Logger LOG = Logger.getInstance(JSFieldCanBeLocalInspection.class);

  @NotNull
  @Override
  protected PsiElementVisitor createVisitor(final ProblemsHolder holder, final LocalInspectionToolSession session) {
    return new MyVisitor(holder);
  }

  private static class MyVisitor extends JSElementVisitor {
    private final ProblemsHolder myHolder;

    MyVisitor(ProblemsHolder holder) {
      myHolder = holder;
    }

    @Override
    public void visitJSVariable(final @NotNull JSVariable field) {
      if (!DialectDetector.isActionScript(field)) return;
      final PsiElement parentParent = field.getParent().getParent();
      final PsiElement context = parentParent.getContext();

      if (!(parentParent instanceof JSClass) &&
          !(parentParent instanceof JSFile &&
            context instanceof XmlText &&
            JavaScriptSupportLoader.isFlexMxmFile(context.getContainingFile()))) {
        return;
      }

      final JSAttributeList attributeList = field.getAttributeList();
      if (attributeList == null) return;
      if (attributeList.getAccessType() != JSAttributeList.AccessType.PRIVATE) return;
      if (field.isConst() && attributeList.hasModifier(JSAttributeList.ModifierType.STATIC)) return;
      if (attributeList.findAttributeByName("Embed") != null || attributeList.findAttributeByName("Inject") != null) return;

      // sorted - for predictable caret position after quick fix
      final SortedMap<JSFunction, Collection<PsiReference>> functionToReferences =
        new TreeMap<>(Comparator.comparingInt(f -> f.getTextRange().getStartOffset()));

      final Map<JSFunction, PsiElement> functionToFirstReadUsage = new HashMap<>();
      final Map<JSFunction, PsiElement> functionToFirstWriteUsage = new HashMap<>();

      final PsiFile topLevelFile = InjectedLanguageManager.getInstance(field.getProject()).getTopLevelFile(field);

      final boolean ok = ReferencesSearch.search(field, new LocalSearchScope(topLevelFile)).forEach(reference -> {

        final PsiElement element = reference.getElement();
        if (JSResolveUtil.isSelfReference(element)) return true;
        if (!(element instanceof JSReferenceExpression)) return false;
        if (((JSReferenceExpression)element).getQualifier() != null) return false;

        final JSFunction function = PsiTreeUtil.getParentOfType(element, JSFunction.class);
        if (function == null) return false;

        Collection<PsiReference> references = functionToReferences.get(function);
        if (references == null) {
          references = new ArrayList<>();
          functionToReferences.put(function, references);
        }
        references.add(reference);

        ReadWriteAccessDetector.Access access = JSReadWriteAccessDetector.ourInstance.getExpressionAccess(element);

        if (access.isReferencedForRead()) {
          final PsiElement previous = functionToFirstReadUsage.get(function);
          if (previous == null || element.getTextRange().getStartOffset() < previous.getTextRange().getStartOffset()) {
            functionToFirstReadUsage.put(function, element);
          }
        }

        if (access.isReferencedForWrite()) {
          final PsiElement previous = functionToFirstWriteUsage.get(function);
          if (previous == null || element.getTextRange().getStartOffset() < previous.getTextRange().getStartOffset()) {
            functionToFirstWriteUsage.put(function, element);
          }
        }

        return true;
      });

      if (!ok) return;
      if (functionToFirstWriteUsage.isEmpty() && functionToFirstReadUsage.isEmpty()) return;

      // can be local if field has trivial initializer and doesn't have any more write usages
      final boolean trivialInitializer = field.getInitializer() instanceof JSLiteralExpression;
      if (functionToFirstWriteUsage.isEmpty() && trivialInitializer) {
        registerCanBeLocal(field, functionToReferences);
        return;
      }

      // can be local if all read usages have write usage before them
      for (Map.Entry<JSFunction, PsiElement> entry : functionToFirstReadUsage.entrySet()) {
        final JSFunction function = entry.getKey();
        final PsiElement readUsage = entry.getValue();

        final PsiElement writeUsage = functionToFirstWriteUsage.get(function);

        if (writeUsage == null) return;
        if (writeUsage.getTextRange().getStartOffset() >= readUsage.getTextRange().getStartOffset()) return;

        final JSElement branchingParent =
          PsiTreeUtil.getParentOfType(writeUsage, JSConditionalExpression.class, JSIfStatement.class, JSSwitchStatement.class);
        if (branchingParent != null) return;

        final PsiElement commonParent = PsiTreeUtil.findCommonParent(readUsage, writeUsage);
        if (commonParent instanceof JSAssignmentExpression) return; // a = a + 1
      }

      registerCanBeLocal(field, functionToReferences);
    }

    private void registerCanBeLocal(final JSVariable field, final Map<JSFunction, Collection<PsiReference>> functionToReferences) {
      LOG.assertTrue(!functionToReferences.isEmpty());

      PsiElement element = field.getNameIdentifier();
      if (element == null) {
        element = field;
      }

      final LocalQuickFix[] fixes = new LocalQuickFix[]{new ConvertToLocalFix(field, functionToReferences)};
      myHolder.registerProblem(element, FlexBundle.message("js.field.can.be.converted.to.local"), fixes);
    }
  }
}
