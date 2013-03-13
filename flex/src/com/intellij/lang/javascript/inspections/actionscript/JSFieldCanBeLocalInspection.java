package com.intellij.lang.javascript.inspections.actionscript;

import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.findUsages.JSReadWriteAccessDetector;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.inspections.JSInspection;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Processor;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Alexey.Ivanov
 */
// TODO: control flow
public class JSFieldCanBeLocalInspection extends JSInspection {
  private static final Logger LOG = Logger.getInstance("#" + JSFieldCanBeLocalInspection.class.getName());
  private static final ImplicitUsageProvider[] ourImplicitUsageProviders = Extensions.getExtensions(ImplicitUsageProvider.EP_NAME);

  @Override
  protected PsiElementVisitor createVisitor(final ProblemsHolder holder, final LocalInspectionToolSession session) {
    return new MyVisitor(holder);
  }

  private static class MyVisitor extends JSElementVisitor {
    private final ProblemsHolder myHolder;
    private final Set<JSVariable> myFields = new HashSet<JSVariable>();
    private PsiFile myFile;

    public MyVisitor(ProblemsHolder holder) {
      myHolder = holder;
    }

    @Override
    public void visitFile(PsiFile file) {
      if (JavaScriptSupportLoader.isFlexMxmFile(file)) {
        myFile = file;
        final Collection<JSClass> classes = XmlBackedJSClassImpl.getClasses((XmlFile)file);
        for (JSClass jsClass : classes) {
          checkClass(jsClass);
        }
      }
    }

    @Override
    public void visitJSClass(final JSClass aClass) {
      myFile = aClass.getContainingFile();
      checkClass(aClass);
    }

    private void checkClass(final JSClass aClass) {
      final Set<JSVariable> candidates = new LinkedHashSet<JSVariable>();
      for (JSVariable field : aClass.getFields()) {
        myFields.add(field);
        final JSAttributeList attributeList = field.getAttributeList();
        if (attributeList != null && attributeList.getAccessType() == JSAttributeList.AccessType.PRIVATE
            && !(attributeList.hasModifier(JSAttributeList.ModifierType.STATIC) && field.isConst())
            && attributeList.findAttributeByName("Embed") == null && attributeList.findAttributeByName("Inject") == null) {

          final PsiFile containingFile = field.getContainingFile();
          final PsiElement context = containingFile.getContext();
          if (containingFile == myFile || (context != null && context.getContainingFile() == myFile)) {
            candidates.add(field);
          }
        }
      }

      removeReferencesFromInitializers(aClass, candidates);
      if (candidates.isEmpty()) return;

      final Set<JSVariable> usedFields = new HashSet<JSVariable>();
      removeReadFields(aClass, candidates, usedFields);
      if (candidates.isEmpty()) return;

      highlight(candidates, usedFields);
    }

    private void removeReferencesFromInitializers(final JSClass aClass, final Set<JSVariable> candidates) {
      for (JSVariable field : aClass.getFields()) {
        final JSExpression initializer = field.getInitializer();
        if (initializer != null) {
          initializer.accept(new JSRecursiveElementVisitor() {
            @Override
            public void visitJSReferenceExpression(JSReferenceExpression node) {
              final PsiElement element = node.resolve();
              if (element instanceof JSVariable) {
                final JSVariable jsVariable = (JSVariable)element;
                if (isField(jsVariable)) {
                  candidates.remove(jsVariable);
                }
              }
            }
          });
          if (field.isConst()) candidates.remove(field);
        }
      }
    }

    private void removeReadFields(final JSClass aClass, final Set<JSVariable> candidates, final Set<JSVariable> usedFields) {
      final JSFunction[] functions = aClass.getFunctions();
      for (JSFunction function : functions) {
        checkCodeBlock(function.getBody(), candidates, usedFields);
      }
    }

    private void checkCodeBlock(final JSSourceElement[] body, final Set<JSVariable> candidates, final Set<JSVariable> usedFields) {
      for (JSSourceElement element : body) {
        if (element instanceof JSBlockStatement) {
          final JSBlockStatement blockStatement = (JSBlockStatement)element;
          final Set<JSVariable> usedVars = getUsedVars(blockStatement);
          for (JSVariable usedVar : usedVars) {
            if (isField(usedVar)) {
              if (!usedFields.add(usedVar)) {
                candidates.remove(usedVar);
              }
            }
          }

          final Collection<JSVariable> readBeforeWriteVars = getReadBeforeWriteVars(blockStatement);
          candidates.removeAll(readBeforeWriteVars);

          candidates.removeAll(computeStopList(blockStatement));
        }
      }
    }

    private static Set<JSVariable> getUsedVars(final JSBlockStatement element) {
      final Set<JSVariable> variables = new HashSet<JSVariable>();
      element.acceptChildren(new JSRecursiveElementVisitor() {
        @Override
        public void visitJSReferenceExpression(JSReferenceExpression node) {
          super.visitJSReferenceExpression(node);
          if (JSResolveUtil.isSelfReference(node)) {
            return;
          }
          final PsiElement resolved = node.resolve();
          if (resolved instanceof JSVariable) {
            variables.add((JSVariable)resolved);
          }
        }
      });
      return variables;
    }

    private static Collection<JSVariable> computeStopList(final JSBlockStatement element) {
      final Collection<JSVariable> result = new HashSet<JSVariable>();
      element.acceptChildren(new JSRecursiveElementVisitor() {
        public void visitJSIndexedPropertyAccessExpression(final JSIndexedPropertyAccessExpression node) {
          if (node.getQualifier() instanceof JSReferenceExpression) {
            addResult((JSReferenceExpression)node.getQualifier());
          }
        }

        @Override
        public void visitJSAssignmentExpression(JSAssignmentExpression node) {
          if (node.getOperationSign() != JSTokenTypes.EQ) {
            return;
          }
          final JSExpression rOperand = node.getROperand();
          if (rOperand instanceof JSReferenceExpression) {
            addResult((JSReferenceExpression)rOperand);
          }
          node.acceptChildren(this);
        }

        @Override
        public void visitJSVariable(JSVariable node) {
          final JSExpression initializer = node.getInitializer();
          if (initializer instanceof JSReferenceExpression) {
            addResult((JSReferenceExpression)initializer);
          }
        }

        private void addResult(final JSReferenceExpression referenceExpression) {
          final PsiElement resolved = referenceExpression.resolve();
          if (resolved instanceof JSVariable) {
            final JSVariable variable = (JSVariable)resolved;
            if (!JSTypeUtils.isImmutableType(variable.getType())) {
              result.add(variable);
            }
          }
        }
      });
      return result;
    }

    private static Collection<JSVariable> getReadBeforeWriteVars(final JSBlockStatement element) {
      final Set<JSVariable> variables = new HashSet<JSVariable>();
      final Set<JSVariable> written = new HashSet<JSVariable>();
      element.acceptChildren(new JSRecursiveElementVisitor() {
        @Override
        public void visitJSReferenceExpression(JSReferenceExpression node) {
          super.visitJSReferenceExpression(node);
          if (JSResolveUtil.isSelfReference(node)) {
            return;
          }

          ReadWriteAccessDetector.Access access = JSReadWriteAccessDetector.ourInstance.getExpressionAccess(node);
          final PsiElement resolved = node.resolve();
          if (resolved instanceof JSVariable) {
            final JSVariable variable = (JSVariable)resolved;
            if (access == ReadWriteAccessDetector.Access.Write) {
              written.add(variable);
            }
            else {
              if (!written.contains(variable)) {
                variables.add(variable);
              }
              if (access == ReadWriteAccessDetector.Access.ReadWrite) {
                written.add(variable);
              }
            }
          }
        }

        @Override
        public void visitJSAssignmentExpression(JSAssignmentExpression node) {
          final JSExpression rOperand = node.getROperand();
          if (rOperand != null) {
            rOperand.accept(this);
          }
          final JSExpression lOperand = node.getLOperand();
          if (lOperand != null) lOperand.accept(this);
        }
      });

      variables.retainAll(written);
      return variables;
    }


    private void highlight(final Collection<JSVariable> candidates, final Set<JSVariable> usedFields) {
      for (JSVariable field : candidates) {
        if (usedFields.contains(field) && !hasImplicitReadOrWriteUsage(field)) {
          PsiElement element = field.getNameIdentifier();
          if (element == null) {
            element = field;
          }
          final PsiFile containingFile = field.getContainingFile();
          final Collection<PsiReference> references = findReferences(field);
          if (references.isEmpty()) return;
          LocalQuickFix[] fixes = JSUtils.isActionScript(containingFile)
                                  ? new LocalQuickFix[]{new ConvertToLocalFix(field, references)}
                                  : LocalQuickFix.EMPTY_ARRAY;
          if (myFile == containingFile) {
            myHolder.registerProblem(element, JSBundle.message("js.field.can.be.converted.to.local"), fixes);
          }
          else {
            final TextRange textRange = InjectedLanguageManager.getInstance(myHolder.getProject()).injectedToHost(containingFile,
                                                                                                                  element.getTextRange());
            myHolder.registerProblem(myFile, textRange, JSBundle.message("js.field.can.be.converted.to.local"), fixes);
          }
        }
      }
    }

    private static Collection<PsiReference> findReferences(final PsiElement variable) {
      final Collection<PsiReference> references = new HashSet<PsiReference>();
      ReferencesSearch.search(variable).forEach(new Processor<PsiReference>() {
        @Override
        public boolean process(PsiReference psiReference) {
          if (!(psiReference instanceof JSReferenceExpression)) return true;
          if (JSResolveUtil.isSelfReference((JSReferenceExpression)psiReference)) return true;
          references.add(psiReference);
          return true;
        }
      });
      return references;
    }

    private static boolean hasImplicitReadOrWriteUsage(final JSVariable field) {
      for (ImplicitUsageProvider provider : ourImplicitUsageProviders) {
        if (provider.isImplicitRead(field) || provider.isImplicitWrite(field)) {
          return true;
        }
      }
      return false;
    }

    private boolean isField(final JSVariable jsVariable) {
      return myFields.contains(jsVariable);
    }
  }

  private static class ConvertToLocalFix implements LocalQuickFix {
    private final JSVariable myField;
    private final Collection<PsiReference> myReferences;

    public ConvertToLocalFix(final JSVariable field, Collection<PsiReference> references) {
      myField = field;
      myReferences = references;
    }

    @NotNull
    @Override
    public String getName() {
      return JSBundle.message("js.convert.to.local.quick.fix");
    }

    @NotNull
    @Override
    public String getFamilyName() {
      return getName();
    }

    @Override
    public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
      if (!CodeInsightUtilBase.preparePsiElementForWrite(myField)) return;

      final JSBlockStatement anchorBlock = findAnchorBlock(myReferences);
      if (anchorBlock == null) return;
      final PsiElement firstElement = getFirstElement(myReferences);
      final PsiElement anchorElement = getAnchorElement(anchorBlock, firstElement);

      final String typeString = myField.getTypeString();
      StringBuilder text = new StringBuilder("var ").append(myField.getName());
      if (!StringUtil.isEmpty(typeString)) {
        text.append(":").append(typeString);
      }
      final boolean b = isAssignment(anchorElement, firstElement);
      if (b) {
        final JSExpression expression = ((JSExpressionStatement)anchorElement).getExpression();
        final JSExpression rOperand = ((JSAssignmentExpression)expression).getROperand();
        text.append("=").append(rOperand.getText());
      }
      else {
        String initializerText = myField.getInitializerText();
        if (initializerText != null) {
          text.append("=").append(initializerText);
        }
      }

      text.append(JSChangeUtil.getSemicolon(project));
      final PsiElement varStatement =
        JSChangeUtil.createJSTreeFromText(project, text.toString(), JavaScriptSupportLoader.ECMA_SCRIPT_L4).getPsi();
      if (varStatement == null) return;

      final PsiElement newDeclaration;
      if (b) {
        newDeclaration = anchorElement.replace(varStatement);
      }
      else {
        newDeclaration = anchorBlock.addBefore(varStatement, anchorElement);
      }
      CodeStyleManager.getInstance(project).reformat(anchorBlock);

      if (newDeclaration != null) {
        PsiFile psiFile = myField.getContainingFile();
        int offset = newDeclaration.getTextOffset();
        if (psiFile.getContext() != null) {
          psiFile = psiFile.getContext().getContainingFile();
          offset = InjectedLanguageManager.getInstance(project).injectedToHost(newDeclaration, offset);
        }
        final Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor != null) {
          final PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
          if (file == psiFile) {
            editor.getCaretModel().moveToOffset(offset);
            editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
          }
        }
      }

      deleteField();
    }

    private void deleteField() {
      final PsiElement varStatement = myField.getParent();
      LOG.assertTrue(varStatement instanceof JSVarStatement);
      final PsiElement cl = varStatement.getParent();
      final PsiElement first = varStatement.getPrevSibling();
      if (first instanceof PsiWhiteSpace) {
        cl.deleteChildRange(first, varStatement);
      }
      else {
        myField.delete();
      }
    }

    private static boolean isAssignment(final PsiElement anchorElement, PsiElement ref) {
      if (anchorElement instanceof JSExpressionStatement) {
        final JSExpressionStatement expressionStatement = (JSExpressionStatement)anchorElement;
        final JSExpression expression = expressionStatement.getExpression();
        if (expression instanceof JSAssignmentExpression) {
          return ((JSAssignmentExpression)expression).getOperationSign() == JSTokenTypes.EQ &&
                 PsiTreeUtil.isAncestor(((JSAssignmentExpression)expression).getLOperand(), ref, true);
        }
      }
      return false;
    }

    @Nullable
    private static JSBlockStatement findAnchorBlock(final Collection<PsiReference> references) {
      JSBlockStatement result = null;
      for (PsiReference psiReference : references) {
        final PsiElement element = psiReference.getElement();
        JSBlockStatement block = PsiTreeUtil.getParentOfType(element, JSBlockStatement.class);
        if (result == null || block == null) {
          result = block;
        }
        else {
          final PsiElement commonParent = PsiTreeUtil.findCommonParent(result, block);
          result = PsiTreeUtil.getParentOfType(commonParent, JSBlockStatement.class, false);
        }
      }
      return result;
    }

    private static PsiElement getFirstElement(final Collection<PsiReference> references) {
      PsiElement firstElement = null;
      for (PsiReference reference : references) {
        final PsiElement element = reference.getElement();
        if (firstElement == null || firstElement.getTextRange().getStartOffset() > element.getTextRange().getStartOffset()) {
          firstElement = element;
        }
      }
      LOG.assertTrue(firstElement != null);
      return firstElement;
    }

    private static PsiElement getAnchorElement(final JSBlockStatement anchorBlock, @NotNull final PsiElement firstElement) {
      PsiElement element = firstElement;
      while (element != null && element.getParent() != anchorBlock) {
        element = element.getParent();
      }
      LOG.assertTrue(element != null);
      return element;
    }
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return JSBundle.message("js.field.can.be.local.name");
  }
}
