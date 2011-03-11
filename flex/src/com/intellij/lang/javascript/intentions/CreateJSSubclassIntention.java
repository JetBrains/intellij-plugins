package com.intellij.lang.javascript.intentions;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.ECMAScriptImportOptimizer;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.generation.JavaScriptImplementMethodsHandler;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSReferenceList;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.validation.fixes.CreateClassDialog;
import com.intellij.lang.javascript.validation.fixes.CreateClassOrInterfaceAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Properties;

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
    doCreateSubclass(jsClass, className, packageName, templateName, targetDirectory);
  }

  private void doCreateSubclass(final JSClass baseClass,
                                final String className,
                                final String packageName,
                                final String templateName,
                                final PsiDirectory targetDirectory) {
    final Project project = baseClass.getProject();
    final Ref<PsiElement> createdElementRef = new Ref<PsiElement>();

    final String error = ApplicationManager.getApplication().runWriteAction(new NullableComputable<String>() {
      public String compute() {
        try {
          final Properties additionalTemplateProperties = new Properties();
          final String propName = baseClass.isInterface() ? "Implemented_interface_name" : "Super_class_name";
          final String superClassQname = baseClass.getQualifiedName();
          additionalTemplateProperties.setProperty(propName, superClassQname);
          final PsiElement createdElement = CreateClassOrInterfaceAction
            .createClass(className, packageName, targetDirectory, templateName, additionalTemplateProperties);

          createdElementRef.set(createdElement);

          final String superClassPackage = StringUtil.getPackageName(superClassQname);
          final JSClass createdClass = JSPsiImplUtils.findClass((JSFile)createdElement);
          if (!StringUtil.isEmpty(superClassPackage) && !superClassPackage.equals(packageName) && createdClass != null) {
            ImportUtils.insertImportStatements(createdClass, Collections.singletonList(superClassQname));
          }
          new ECMAScriptImportOptimizer().processFile(createdElement.getContainingFile()).run();
        }
        catch (IncorrectOperationException e) {
          return e.getMessage();
        }
        catch (Exception e) {
          Logger.getInstance(getClass().getName()).error(e);
        }
        return null;
      }
    });

    if (error != null) {
      Messages.showErrorDialog(project, error, getTitle(baseClass));
      return;
    }

    final PsiElement createdElement = createdElementRef.get();
    final VirtualFile createdFile = createdElement == null ? null : createdElement.getContainingFile().getVirtualFile();
    if (createdFile != null) {
      final Editor newEditor = FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, createdFile), true);
      if (createdElement instanceof JSFile && newEditor != null) {
        final JSClass createdClass = JSPsiImplUtils.findClass((JSFile)createdElement);
        if (createdClass != null) {
          newEditor.getCaretModel().moveToOffset(createdClass.getTextOffset());

          if (baseClass.isInterface()) {
            final JavaScriptImplementMethodsHandler implementMethodsHandler = new JavaScriptImplementMethodsHandler();
            implementMethodsHandler.setSkipMemberChooserDialog(true);
            implementMethodsHandler.invoke(project, newEditor, createdElement.getContainingFile());
          }
          else {
            CreateClassOrInterfaceAction.makeSureConstructorMatchesSuper(createdClass);
          }
        }
      }
    }
  }

  public boolean startInWriteAction() {
    return false;
  }

  private static String suggestSubclassName(final String name) {
    return name + IMPL_SUFFIX;
  }
}
