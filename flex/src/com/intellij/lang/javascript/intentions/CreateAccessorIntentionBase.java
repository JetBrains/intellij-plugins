package com.intellij.lang.javascript.intentions;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.generation.BaseJSGenerateHandler;
import com.intellij.lang.javascript.generation.JSNamedElementNode;
import com.intellij.lang.javascript.generation.JavaScriptGenerateAccessorHandler;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSVarStatement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public abstract class CreateAccessorIntentionBase extends PsiElementBaseIntentionAction {

  public CreateAccessorIntentionBase() {
    setText(getDescription());
  }

  protected abstract String getDescription();

  protected abstract String getMessageKey();

  @NotNull
  public String getFamilyName() {
    return getText();
  }

  public boolean isAvailable(final @NotNull Project project, final Editor editor, final @NotNull PsiElement element) {
    if (!(element.getContainingFile() instanceof JSFile)) {
      return false;
    }
    final JSVariable variable = getVariable(element);
    if (variable != null && variable.isConst()) return false;

    final PsiElement parent = variable == null ? null : variable.getParent();
    final PsiElement parentParent = parent instanceof JSVarStatement ? parent.getParent() : null;
    final PsiElement context =
      parentParent == null ? null : InjectedLanguageManager.getInstance(parentParent.getProject()).getInjectionHost(parentParent);
    final JSClass jsClass = parentParent instanceof JSClass
                            ? ((JSClass)parentParent)
                            : (parentParent instanceof JSFile && context instanceof XmlText)
                              ? XmlBackedJSClassImpl.getContainingComponent((XmlText)context)
                              : null;
    final String varName = variable == null ? null : variable.getName();
    if (jsClass != null && StringUtil.isNotEmpty(varName)) {
      setText(FlexBundle.message(getMessageKey(), varName));
      final String accessorName = JSResolveUtil.transformVarNameToAccessorName(varName, project);
      return isAvailableFor(jsClass, accessorName);
    }
    return false;
  }

  @Nullable
  private static JSVariable getVariable(final PsiElement element) {
    final PsiElement var = PsiTreeUtil.getParentOfType(element, JSVariable.class, JSVarStatement.class);
    if (var instanceof JSVariable) {
      return (JSVariable)var;
    }
    else if (var instanceof JSVarStatement) {
      final JSVariable[] variables = ((JSVarStatement)var).getVariables();
      return variables.length == 1 ? variables[0] : null;
    }

    return null;
  }

  protected abstract boolean isAvailableFor(final JSClass jsClass, final String accessorName);

  @Override
  public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
    final PsiFile file = element.getContainingFile();
    final JSClass jsClass = BaseJSGenerateHandler.findClass(file, editor);
    if (jsClass == null) return;

    final JSVariable variable = getVariable(element);
    final JSNamedElementNode node = variable == null ? null : new JSNamedElementNode(variable);

    final JavaScriptGenerateAccessorHandler handler = new JavaScriptGenerateAccessorHandler(getGenerationMode()) {
      protected void collectCandidates(final JSClass clazz, final Collection<JSNamedElementNode> candidates) {
        ContainerUtil.addIfNotNull(node, candidates);
      }
    };

    handler.setSkipMemberChooserDialog(true);

    /*
    final JComponent optionsComponent =
      handler.getOptionsComponent(jsClass, node == null ? Collections.<JSNamedElementNode>emptyList() : Collections.singleton(node));
    if (optionsComponent != null) {
      final int result = showOptionsDialog(project, optionsComponent);
      if (result != DialogWrapper.OK_EXIT_CODE) return;
    }
    */

    handler.invoke(project, editor, file);
  }

  /*
  private int showOptionsDialog(final Project project, final JComponent optionsComponent) {
    final DialogWrapper dialog = new DialogWrapper(project) {
      {
        init();
      }

      protected JComponent createCenterPanel() {
        return optionsComponent;
      }
    };
    dialog.setTitle(getText());
    dialog.show();
    return dialog.getExitCode();
  }
  */

  protected abstract JavaScriptGenerateAccessorHandler.GenerationMode getGenerationMode();

  public boolean startInWriteAction() {
    return false;
  }
}
