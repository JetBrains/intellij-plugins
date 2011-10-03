package com.intellij.flex.uiDesigner.testAssistant;

import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;

import java.io.File;

public class MyImplicitUsageProvider implements ImplicitUsageProvider {
  @Override
  public boolean isImplicitUsage(PsiElement element) {
    if (!(element instanceof JSFunction)) {
      return false;
    }

    final JSFunction method = (JSFunction)element;
    final String methodName = method.getName();
    if (methodName == null ||
        !Character.isUpperCase(methodName.charAt(0)) ||
        !(method.getParent() instanceof JSClass) ||
        method.getParent() instanceof XmlBackedJSClassImpl) {
      return false;
    }

    final JSClass clazz = (JSClass)method.getParent();
    if (!JSInheritanceUtil.isParentClass(clazz, "com.intellij.flex.uiDesigner.plugins.test.TestCase")) {
      return false;
    }

    final JSAttributeList attributeList = method.getAttributeList();
    if (attributeList == null || attributeList.getAccessType() != JSAttributeList.AccessType.PUBLIC) {
      return false;
    }

    final VirtualFile projectBaseDir = element.getProject().getBaseDir();
    if (projectBaseDir == null) {
      return false;
    }

    File testDataPath = new File(projectBaseDir.getPath(), "idea-plugin/testData");
    if (!testDataPath.exists()) {
      testDataPath = new File(projectBaseDir.getPath(), "flex/tools/flex-ui-designer/idea-plugin/testData");
      assert testDataPath.exists();
    }

    final JSAttributeList classAttributeList = clazz.getAttributeList();
    if (classAttributeList != null) {
      final JSAttribute testAnnotation = classAttributeList.findAttributeByName("Test");
      if (testAnnotation == null) {
        return false;
      }

      return new File(testDataPath, testAnnotation.getValueByName("dir").getSimpleValue() + File.separatorChar + methodName + ".mxml")
        .exists();
    }

    return false;
  }

  @Override
  public boolean isImplicitRead(PsiElement element) {
    return false;
  }

  @Override
  public boolean isImplicitWrite(PsiElement element) {
    return false;
  }
}
