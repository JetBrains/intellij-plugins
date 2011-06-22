package com.google.jstestdriver.idea.testIntegration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.jstestdriver.idea.util.CastUtils;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSExpressionStatement;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveResult;

public abstract class JstdQUnitCreateModuleLifecycleBaseAction extends AnAction {

  protected static final String MODULE_SETUP_METHOD_NAME = "setup";
  protected static final String MODULE_TEARDOWN_METHOD_NAME = "teardown";

  @Override
  public void actionPerformed(AnActionEvent e) {
    final DataContext dataContext = e.getDataContext();
    final Context context = Context.build(dataContext, this);
    if (context == null) {
      throw new RuntimeException("Can't fetch " + JstdQUnitCreateTestAction.class.getSimpleName() + " from ActionEvent " + e);
    }

    TemplateManager templateManager = TemplateManager.getInstance(context.getProject());
    Template template = new TemplateImpl("", "");
    template.setToIndent(true);
    template.setToReformat(true);
    template.setToShortenLongNames(false);
//    template.setInline(false);

    PsiElement precedingPsiElement = context.getPrecedingPsiElement();
    JSExpression[] argExpressions = context.getArgExpressions();
    if (argExpressions.length == 1) {
      template.addTextSegment(", {\n");
      addMethodInsideObjectLiteral(template, false, false);
      template.addTextSegment("}\n");
    } else if (argExpressions.length == 2) {
      boolean leadingComma = precedingPsiElement instanceof JSProperty;
      addMethodInsideObjectLiteral(template, leadingComma, !leadingComma);
    }

    int insertOffset = precedingPsiElement.getTextOffset() + precedingPsiElement.getTextLength();
    context.getEditor().getCaretModel().moveToOffset(insertOffset);
    templateManager.startTemplate(context.getEditor(), "", template);
  }

  protected abstract String getModuleLifecyclePrecedingMethodName();

  protected abstract String getModuleLifecycleMethodName();

  protected abstract String getActionDisplayName();

  private void addMethodInsideObjectLiteral(Template template, boolean leadingComma, boolean trailingComma) {
    if (leadingComma) {
      template.addTextSegment(",\n");
    }
    template.addTextSegment(getModuleLifecycleMethodName() + ": function(){");
    template.addEndVariable();
    template.addTextSegment("}");
    if (trailingComma) {
      template.addTextSegment(",");
    }
  }

  @Override
  public void update(AnActionEvent e) {
    Presentation presentation = e.getPresentation();
    Context context = Context.build(e.getDataContext(), this);

    if (context == null) {
      presentation.setVisible(false);
      return;
    }

    presentation.setVisible(true);

    StringBuilder buf = new StringBuilder();
    buf.append("QUnit ").append(this.getActionDisplayName());
    if (context.getModuleName() != null) {
      buf.append(" in '").append(context.getModuleName()).append("'");
    }

    presentation.setText(buf.toString(), true);
    presentation.setEnabled(true);
  }

  private static class Context {
    @NotNull
    final Editor myEditor;
    @Nullable
    final String myModuleName;
    @NotNull
    final PsiElement myPrecedingPsiElement;
    @NotNull
    final JSExpression[] myArgExpressions;

    Context(@NotNull Editor editor, @Nullable String moduleName, @NotNull PsiElement precedingPsiElement, @NotNull JSExpression[] argExpressions) {
      myEditor = editor;
      myModuleName = moduleName;
      myPrecedingPsiElement = precedingPsiElement;
      myArgExpressions = argExpressions;
    }

    Editor getEditor() {
      return myEditor;
    }

    String getModuleName() {
      return myModuleName;
    }

    Project getProject() {
      return myPrecedingPsiElement.getProject();
    }

    JSExpression[] getArgExpressions() {
      return myArgExpressions;
    }

    PsiElement getPrecedingPsiElement() {
      return myPrecedingPsiElement;
    }

    @Nullable
    private static Context build(DataContext dataContext, JstdQUnitCreateModuleLifecycleBaseAction action) {
      Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
      if (editor == null) {
        return null;
      }
      PsiFile psiFile = LangDataKeys.PSI_FILE.getData(dataContext);
      if (!(psiFile instanceof JSFile)) {
        return null;
      }
      int caretOffsetInDocument = editor.getCaretModel().getOffset();
      PsiElement psiElementUnderCaret = psiFile.findElementAt(caretOffsetInDocument);
      if (psiElementUnderCaret == null) {
        psiElementUnderCaret = psiFile.getLastChild();
      }
      if (psiElementUnderCaret != null) {
        PsiElement topPsiElement = getTopmostPsiElements(psiElementUnderCaret);
        JSCallExpression moduleCallExpression = findFirstModuleCallInBeforeScope(topPsiElement);
        if (moduleCallExpression != null && moduleCallExpression.getArgumentList() != null) {
          JSExpression[] argExpressions = moduleCallExpression.getArgumentList().getArguments();
          if (argExpressions != null && argExpressions.length > 0) {
            final String moduleName = findModuleName(argExpressions[0]);
            final PsiElement precedingPsiElement = findPrecedingPsiElement(argExpressions, action);
            if (precedingPsiElement != null) {
              return new Context(editor, moduleName, precedingPsiElement, argExpressions);
            }
          }
        }
      }
      return null;
    }

    @Nullable
    private static PsiElement findPrecedingPsiElement(JSExpression[] argExpressions, JstdQUnitCreateModuleLifecycleBaseAction action) {
      PsiElement precedingPsiElement = null;
      if (argExpressions.length == 1) {
        precedingPsiElement = argExpressions[0];
      } else if (argExpressions.length == 2) {
        JSObjectLiteralExpression objectLiteralExpression = CastUtils.tryCast(
            argExpressions[1], JSObjectLiteralExpression.class);
        if (objectLiteralExpression != null) {
          precedingPsiElement = objectLiteralExpression.getFirstChild();
          JSProperty[] properties = objectLiteralExpression.getProperties();
          if (properties != null) {
            for (JSProperty property : properties) {
              if (Comparing.equal(property.getName(), action.getModuleLifecyclePrecedingMethodName())) {
                precedingPsiElement = property;
              } else if (Comparing.equal(property.getName(), action.getModuleLifecycleMethodName())) {
                return null;
              }
            }
          }
        }
      }
      return precedingPsiElement;
    }

    @Nullable
    private static String findModuleName(@NotNull JSExpression moduleNameExpression) {
      JSLiteralExpression literalExpression = CastUtils.tryCast(moduleNameExpression, JSLiteralExpression.class);
      if (literalExpression != null && literalExpression.isQuotedLiteral()) {
        return literalExpression.getName();
      }
      return null;
    }

    @NotNull
    private static PsiElement getTopmostPsiElements(@NotNull PsiElement psiElement) {
      PsiElement lastElement = psiElement;
      while (!(psiElement instanceof PsiFile)) {
        lastElement = psiElement;
        psiElement = psiElement.getParent();
      }
      return lastElement;
    }

    @Nullable
    private static JSCallExpression findFirstModuleCallInBeforeScope(PsiElement psiElement) {
      while (psiElement != null) {
        JSCallExpression moduleCreation = extractModuleCreationCall(psiElement);
        if (moduleCreation != null) {
          return moduleCreation;
        }
        psiElement = psiElement.getPrevSibling();
      }
      return null;
    }

    @Nullable
    private static JSCallExpression extractModuleCreationCall(PsiElement psiElement) {
      JSExpressionStatement expressionStatement = CastUtils.tryCast(psiElement, JSExpressionStatement.class);
      if (expressionStatement != null) {
        JSCallExpression callExpression = CastUtils.tryCast(expressionStatement.getExpression(), JSCallExpression.class);
        if (callExpression != null) {
          JSReferenceExpression referenceExpression = CastUtils.tryCast(
              callExpression.getMethodExpression(), JSReferenceExpression.class);
          if (referenceExpression != null && "module".equals(referenceExpression.getReferencedName())) {
            ResolveResult[] resolveResults = referenceExpression.multiResolve(false);
            boolean qunitAdapterFound = false;
            for (ResolveResult resolveResult : resolveResults) {
              PsiElement resolvedElement = resolveResult.getElement();
              PsiFile resolvedElementPsiFile;
              if (resolvedElement != null && (resolvedElementPsiFile = resolvedElement.getContainingFile()) != null) {
                VirtualFile virtualFile = resolvedElementPsiFile.getVirtualFile();
                if (virtualFile != null && "QUnitAdapter.js".equals(virtualFile.getName())) {
                  qunitAdapterFound = true;
                  break;
                }
              }
            }
            if (qunitAdapterFound) {
              return callExpression;
            }
          }
        }
      }
      return null;
    }

  }

}
