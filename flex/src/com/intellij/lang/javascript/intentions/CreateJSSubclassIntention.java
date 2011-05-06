package com.intellij.lang.javascript.intentions;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.generation.JSNamedElementNode;
import com.intellij.lang.javascript.generation.JavaScriptImplementMethodsHandler;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSReferenceList;
import com.intellij.lang.javascript.validation.fixes.CreateClassDialog;
import com.intellij.lang.javascript.validation.fixes.CreateClassOrInterfaceAction;
import com.intellij.lang.javascript.validation.fixes.ImplementMethodsFix;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class CreateJSSubclassIntention extends PsiElementBaseIntentionAction {
  private @NonNls static final String IMPL_SUFFIX = "Impl";

  public CreateJSSubclassIntention() {
    setText(CodeInsightBundle.message("intention.implement.abstract.class.subclass.text"));
  }

  @NotNull
  public String getFamilyName() {
    return FlexBundle.message("intention.create.subclass.or.implement.interface");
  }

  public boolean isAvailable(final @NotNull Project project, final Editor editor, final @NotNull PsiElement element) {
    final PsiFile psiFile = element.getContainingFile();
    if (!(psiFile instanceof JSFile) ||
        psiFile.getContext() != null ||
        !psiFile.getLanguage().isKindOf(JavaScriptSupportLoader.ECMA_SCRIPT_L4)) {
      return false;
    }

    final JSClass jsClass = PsiTreeUtil.getParentOfType(element, JSClass.class);
    if (jsClass == null || !(jsClass.getParent() instanceof JSPackageStatement)) {
      return false;
    }

    if (!jsClass.isInterface()) {
      final JSAttributeList attributeList = jsClass.getAttributeList();
      if (attributeList != null && attributeList.hasModifier(JSAttributeList.ModifierType.FINAL)) {
        return false;
      }
    }

    final TextRange declarationRange = getClassDeclarationTextRange(jsClass);
    final int offset = editor.getCaretModel().getOffset();

    if (offset < declarationRange.getStartOffset() || offset > declarationRange.getEndOffset()) { // not the same as TextRange.contains()
      return false;
    }

    setText(getTitle(jsClass));
    return true;
  }

  private static String getTitle(final JSClass jsClass) {
    return jsClass.isInterface()
           ? CodeInsightBundle.message("intention.implement.abstract.class.interface.text")
           : CodeInsightBundle.message("intention.implement.abstract.class.subclass.text");
  }

  public static TextRange getClassDeclarationTextRange(final JSClass jsClass) {
    int start = jsClass.getTextRange().getStartOffset();

    final JSAttributeList attributeList = jsClass.getAttributeList();
    if (attributeList != null) {
      final PsiElement accessTypeElement = attributeList.findAccessTypeElement();
      if (accessTypeElement != null) {
        start = accessTypeElement.getTextRange().getStartOffset();
      }
      else {
        final ASTNode node = jsClass.getNode();
        final ASTNode classKeyWordNode = node == null ? null : node.findChildByType(JSTokenTypes.CLASS_KEYWORD);
        if (classKeyWordNode != null) {
          start = classKeyWordNode.getTextRange().getStartOffset();
        }
      }
    }

    int end = start;

    JSReferenceList jsReferenceList = jsClass.getImplementsList();
    if (jsReferenceList == null) {
      jsReferenceList = jsClass.getExtendsList();
    }

    if (jsReferenceList != null) {
      end = jsReferenceList.getTextRange().getEndOffset();
    }
    else {
      final PsiElement nameIdentifier = jsClass.getNameIdentifier();
      if (nameIdentifier != null) {
        end = nameIdentifier.getTextRange().getEndOffset();
      }
    }

    return new TextRange(start, end);
  }

  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
    final PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());

    final JSClass jsClass = PsiTreeUtil.getParentOfType(element, JSClass.class);
    if (jsClass == null) return;

    final PsiElement parent = jsClass.getParent();
    if (!(parent instanceof JSPackageStatement)) return;
    final JSPackageStatement jsPackageStatement = (JSPackageStatement)parent;

    final String defaultTemplateName = JavaScriptSupportLoader.ACTION_SCRIPT_CLASS_TEMPLATE_NAME;

    final String className;
    final String packageName;
    final String templateName;
    final PsiDirectory targetDirectory;

    if (ApplicationManager.getApplication().isUnitTestMode()) {
      className = suggestSubclassName(jsClass.getName());
      packageName = "foo";
      templateName = defaultTemplateName;
      targetDirectory = ApplicationManager.getApplication().runWriteAction(new Computable<PsiDirectory>() {
        @Override
        public PsiDirectory compute() {
          return CreateClassOrInterfaceAction.findOrCreateDirectory(packageName, jsPackageStatement);
        }
      });
    }
    else {
      final CreateClassDialog dialog =
        new CreateClassDialog(project, suggestSubclassName(jsClass.getName()), true,
                              jsPackageStatement.getQualifiedName(), false, null, defaultTemplateName, jsClass, false);
      dialog.show();

      if (!dialog.isOK()) {
        return;
      }
      className = dialog.getClassName();
      packageName = dialog.getPackageName();
      templateName = dialog.getTemplateName();
      targetDirectory = dialog.getTargetDirectory();
    }

    JSClass createdClass = CreateClassOrInterfaceAction
      .createClass(templateName, className, packageName, jsClass, targetDirectory, getTitle(jsClass), new Consumer<JSClass>() {
        @Override
        public void consume(final JSClass aClass) {
          if (!aClass.isInterface() && jsClass.isInterface()) {
            new MyImplementMethodsHandler(aClass).execute();
          }
        }
      });

    if (createdClass != null) {
      createdClass.navigate(true);
    }
  }

  public boolean startInWriteAction() {
    return false;
  }

  private static String suggestSubclassName(final String name) {
    return name + IMPL_SUFFIX;
  }

  private static class MyImplementMethodsHandler extends JavaScriptImplementMethodsHandler {
    private final JSClass myClass;

    public MyImplementMethodsHandler(JSClass aClass) {
      myClass = aClass;
    }

    public void execute() {
      Collection<JSNamedElementNode> candidates = new ArrayList<JSNamedElementNode>();
      collectCandidates(myClass, candidates);
      ImplementMethodsFix fix = new ImplementMethodsFix(myClass);
      fix.addElementsToProcessFrom(candidates);
      fix.invoke(myClass.getProject(), null, myClass.getContainingFile());
    }
  }
}
