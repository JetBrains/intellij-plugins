package com.intellij.lang.javascript.generation;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitSupport;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.validation.fixes.BaseCreateMethodsFix;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.javascript.validation.fixes.BaseCreateFix.MyExpression;

public class JSGenerateTestMethodAction extends BaseJSGenerateAction {

  protected BaseJSGenerateHandler getGenerateHandler() {
    return new BaseJSGenerateHandler() {
      protected String getTitleKey() {
        return FlexBundle.message("generate.test.method");
      }

      protected BaseCreateMethodsFix createFix(JSClass clazz) {
        return new BaseCreateMethodsFix(clazz) {

          public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
            evalAnchor(editor, file);
            final PsiElement addedElement = doAddOneMethod(project, "public function fake():void{}", anchor);

            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());

            final TemplateManager manager = TemplateManager.getInstance(project);
            final Template template = manager.createTemplate("", "");
            template.setToReformat(true);

            template.addTextSegment("[Test]\npublic function test");
            template.addVariable(new MyExpression("Name"), true);
            template.addTextSegment("():void{\n");
            template.addEndVariable();
            template.addTextSegment("\n}");

            final TextRange range = addedElement.getTextRange();
            editor.getDocument().replaceString(range.getStartOffset(), range.getEndOffset(), "");
            editor.getCaretModel().moveToOffset(range.getStartOffset());

            manager.startTemplate(editor, template);
          }
        };
      }

      protected boolean canHaveEmptySelectedElements() {
        return true;
      }
    };
  }

  protected boolean isApplicableForJsClass(final @NotNull JSClass jsClass, final PsiFile psiFile, final @NotNull Editor editor) {
    final VirtualFile virtualFile = psiFile.getVirtualFile();
    return !(jsClass instanceof XmlBackedJSClassImpl) &&
           virtualFile != null &&
           ProjectRootManager.getInstance(jsClass.getProject()).getFileIndex().isInTestSourceContent(virtualFile) &&
           FlexUnitSupport.getModuleAndSupport(jsClass) != null;
  }
}
