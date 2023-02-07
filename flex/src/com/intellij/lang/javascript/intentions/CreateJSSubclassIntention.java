package com.intellij.lang.javascript.intentions;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.generation.JSChooserElementNode;
import com.intellij.lang.javascript.generation.JavaScriptImplementMethodsHandlerForFlex;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSReferenceList;
import com.intellij.lang.javascript.validation.fixes.ActionScriptCreateClassOrInterfaceFix;
import com.intellij.lang.javascript.validation.fixes.CreateClassParameters;
import com.intellij.lang.javascript.validation.fixes.ImplementMethodsFix;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CreateJSSubclassIntention extends PsiElementBaseIntentionAction {
  private @NonNls static final String IMPL_SUFFIX = "Impl";

  public CreateJSSubclassIntention() {
    setText(CodeInsightBundle.message("intention.implement.abstract.class.subclass.text"));
  }

  @Override
  @NotNull
  public String getFamilyName() {
    return FlexBundle.message("intention.create.subclass.or.implement.interface");
  }

  @Override
  public boolean isAvailable(final @NotNull Project project, final Editor editor, final @NotNull PsiElement element) {
    final PsiFile psiFile = element.getContainingFile();
    if (!(psiFile instanceof JSFile) ||
        InjectedLanguageManager.getInstance(project).getInjectionHost(psiFile) != null ||
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

  private static @NlsContexts.DialogTitle @NotNull String getTitle(final JSClass jsClass) {
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

  @Override
  public void invoke(@NotNull final Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
    final JSClass jsClass = PsiTreeUtil.getParentOfType(element, JSClass.class);
    if (jsClass == null) return;

    final PsiElement parent = jsClass.getParent();
    if (!(parent instanceof JSPackageStatement jsPackageStatement)) return;

    final String defaultTemplateName = ActionScriptCreateClassOrInterfaceFix.ACTION_SCRIPT_CLASS_WITH_SUPERS_TEMPLATE_NAME;

    final String className;
    final String packageName;
    final String templateName;
    final PsiDirectory targetDirectory;
    final Collection<String> interfaces;
    final Map<String, Object> templateAttributes;
    final JSClass superClass;

    if (ApplicationManager.getApplication().isUnitTestMode()) {
      className = suggestSubclassName(jsClass.getName());
      packageName = "foo";
      templateName = defaultTemplateName;
      targetDirectory = WriteAction
        .compute(() -> ActionScriptCreateClassOrInterfaceFix.findOrCreateDirectory(packageName, jsPackageStatement));
      interfaces = jsClass.isInterface() ? Collections.singletonList(jsClass.getQualifiedName()) : Collections.emptyList();
      templateAttributes = Collections.emptyMap();
      superClass = jsClass.isInterface() ? null : jsClass;
    }
    else {
      CreateClassParameters p = ActionScriptCreateClassOrInterfaceFix
        .createAndShow(defaultTemplateName, jsClass, suggestSubclassName(jsClass.getName()), true, jsPackageStatement.getQualifiedName(),
                       jsClass, JavaScriptBundle.message("new.actionscript.class.dialog.title"),
                       () -> ActionScriptCreateClassOrInterfaceFix.getApplicableTemplates(ActionScriptCreateClassOrInterfaceFix.ACTIONSCRIPT_TEMPLATES_EXTENSIONS,
                                                                                          project));

      if (p == null) return;

      className = p.getClassName();
      packageName = p.getPackageName();
      templateName = p.getTemplateName();
      targetDirectory = p.getTargetDirectory();
      superClass = ActionScriptCreateClassOrInterfaceFix.calcClass(p.getSuperclassFqn(), element);
      interfaces = p.getInterfacesFqns();
      templateAttributes = new HashMap<>(p.getTemplateAttributes());
    }

    JSClass createdClass = ActionScriptCreateClassOrInterfaceFix
      .createClass(templateName, className, packageName, superClass, interfaces, targetDirectory,
                   getTitle(jsClass),
                   true, templateAttributes, aClass -> {
                     if (aClass != null && !aClass.isInterface() && (jsClass.isInterface() || !interfaces.isEmpty())) {
                       new MyImplementMethodsHandlerForFlex(aClass).execute();
                     }
                   });

    if (createdClass != null) {
      createdClass.navigate(true);
    }
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }

  private static String suggestSubclassName(final String name) {
    return name + IMPL_SUFFIX;
  }

  private static class MyImplementMethodsHandlerForFlex extends JavaScriptImplementMethodsHandlerForFlex {
    private final JSClass myClass;

    MyImplementMethodsHandlerForFlex(JSClass aClass) {
      myClass = aClass;
    }

    public void execute() {
      Collection<JSChooserElementNode> candidates = new ArrayList<>();
      collectCandidates(myClass, candidates);
      ImplementMethodsFix fix = new ImplementMethodsFix(myClass);
      for(JSChooserElementNode el: candidates) {
        fix.addElementToProcess((JSFunction)el.getPsiElement());
      }
      fix.invoke(myClass.getProject(), null, myClass.getContainingFile());
    }
  }
}
