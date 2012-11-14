package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.refactoring.util.JSMemberInfo;
import com.intellij.lang.javascript.validation.fixes.CreateClassOrInterfaceFix;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.testIntegration.TestCreator;
import com.intellij.util.Consumer;

import java.util.Collections;

import static com.intellij.lang.javascript.psi.JSFunction.FunctionKind;

public class FlexUnitTestCreator implements TestCreator {
  public boolean isAvailable(final Project project, final Editor editor, final PsiFile file) {
    final VirtualFile vFile = file.getVirtualFile();
    return FlexUnitTestFinder.findContextClass(file) != null &&
           vFile != null &&
           !ProjectRootManager.getInstance(project).getFileIndex().isInTestSourceContent(vFile);
  }

  public void createTest(final Project project, final Editor editor, final PsiFile file) {
    final JSClass jsClass = FlexUnitTestFinder.findContextClass(file);
    if (jsClass == null) return;

    final String testClassName;
    final String packageName;
    final JSClass superClass;
    final PsiDirectory targetDirectory;
    final boolean generateSetUp;
    final boolean generateTearDown;
    final JSMemberInfo[] selectedMemberInfos;

    if (ApplicationManager.getApplication().isUnitTestMode()) {
      testClassName = jsClass.getName() + "Test";
      packageName = StringUtil.getPackageName(jsClass.getQualifiedName());
      superClass = null;
      targetDirectory = jsClass.getContainingFile().getContainingDirectory();
      generateSetUp = true;
      generateTearDown = true;
      selectedMemberInfos = new JSMemberInfo[0];
    }
    else {
      final CreateFlexUnitTestDialog dialog = new CreateFlexUnitTestDialog(ModuleUtil.findModuleForPsiElement(jsClass), jsClass);
      dialog.show();

      if (dialog.getExitCode() != DialogWrapper.OK_EXIT_CODE) {
        return;
      }

      testClassName = dialog.getTestClassName();
      packageName = dialog.getPackageName();
      superClass = dialog.getSuperClass();
      targetDirectory = dialog.getTargetDirectory();
      generateSetUp = dialog.isGenerateSetUp();
      generateTearDown = dialog.isGenerateTearDown();
      selectedMemberInfos = dialog.getSelectedMemberInfos();
    }

    final Consumer<JSClass> postProcessRunnable = new Consumer<JSClass>() {
      public void consume(final JSClass createdClass) {
        final String methodsText =
          getMethodsText(createdClass, generateSetUp, generateTearDown, selectedMemberInfos);
        if (!methodsText.isEmpty()) {
          final PsiElement methods =
            JSChangeUtil.createJSTreeFromText(project, "{" + methodsText + "}", JavaScriptSupportLoader.ECMA_SCRIPT_L4).getPsi();
          if (methods != null) {
            for (final PsiElement psiElement : methods.getChildren()) {
              createdClass.add(psiElement);
            }
          }
        }

        CodeStyleManager.getInstance(project).reformat(createdClass);
        createdClass.navigate(true);
      }
    };

    CreateClassOrInterfaceFix.createClass(JavaScriptSupportLoader.ACTION_SCRIPT_CLASS_TEMPLATE_NAME, testClassName,
                                          packageName, superClass, Collections.<String>emptyList(), targetDirectory,
                                          CodeInsightBundle.message("intention.create.test"), true, Collections.<String, String>emptyMap(),
                                          postProcessRunnable);
  }

  private static String getMethodsText(final JSClass createdClass,
                                       final boolean generateSetUp,
                                       final boolean generateTearDown,
                                       final JSMemberInfo[] selectedMemberInfos) {
    final StringBuilder builder = new StringBuilder();
    builder.append(generateSetUp ? JSInheritanceUtil
                                     .findMember("setUp", createdClass, JSInheritanceUtil.SearchedMemberType.Methods, FunctionKind.SIMPLE,
                                                 true) == null
                                   ? "[Before]\npublic function setUp():void{\n\n}"
                                   : "[Before]\npublic override function setUp():void{\nsuper.setUp();\n}"
                                 : "");
    builder.append(generateTearDown ? JSInheritanceUtil.findMember("tearDown", createdClass, JSInheritanceUtil.SearchedMemberType.Methods,
                                                                   FunctionKind.SIMPLE, true) == null
                                      ? "[After]\npublic function tearDown():void{\n\n}"
                                      : "[After]\npublic override function tearDown():void{\nsuper.tearDown();\n}"
                                    : "");
    for (final JSMemberInfo info : selectedMemberInfos) {
      final String testName = "test" + capitalizeFirstCharacter(info.getMember().getName());
      builder.append("[Test]\npublic function ").append(testName).append("():void{\n\n}");
    }
    return builder.toString();
  }

  private static String capitalizeFirstCharacter(final String s) {
    if (StringUtil.isEmpty(s)) return s;
    return Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }
}
