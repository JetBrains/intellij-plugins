package com.intellij.tapestry.tests.core.java;

import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.tapestry.intellij.core.java.IntellijJavaAnnotation;
import com.intellij.tapestry.tests.core.BaseTestCase;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class IntellijJavaAnnotationTest extends BaseTestCase {

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getFullyQualifiedName(IdeaProjectTestFixture fixture) {

      IntellijJavaAnnotation annotation = new IntellijJavaAnnotation(
                getJavaFacade(fixture).findClass(
                        "com.app.util.Class1", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false)
                ).getModifierList().getAnnotations()[0]
        );

        assert annotation.getFullyQualifiedName().equals("java.lang.Deprecated");
    }

  @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getParameters_no_parameters(IdeaProjectTestFixture fixture) {
        PsiManager psiManager = PsiManager.getInstance(fixture.getProject());

        IntellijJavaAnnotation annotation = new IntellijJavaAnnotation(
                getJavaFacade(fixture).findClass(
                        "com.app.util.Class1", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false)
                ).getModifierList().getAnnotations()[0]
        );

        assert annotation.getParameters().size() == 0;
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getParameters_with_parameters(IdeaProjectTestFixture fixture) {
        PsiManager psiManager = PsiManager.getInstance(fixture.getProject());

        IntellijJavaAnnotation annotation = new IntellijJavaAnnotation(
                getJavaFacade(fixture).findClass(
                        "com.app.util.Class1", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false)
                ).getModifierList().getAnnotations()[1]
        );

        Map<String, String[]> parameters = annotation.getParameters();
        assert parameters.size() == 1;

        assert parameters.get(null)[0].equals("warning1");
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getParameters_with_array_parameters(IdeaProjectTestFixture fixture) {
        PsiManager psiManager = PsiManager.getInstance(fixture.getProject());

        IntellijJavaAnnotation annotation = new IntellijJavaAnnotation(
                getJavaFacade(fixture).findClass(
                        "com.app.util.Class1", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false)
                ).getFields()[0].getModifierList().getAnnotations()[0]
        );

        Map<String, String[]> parameters = annotation.getParameters();

        assert parameters.size() == 1;

        assert parameters.get("parameters").length == 3;
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getPsiAnnotation(IdeaProjectTestFixture fixture) {
        PsiManager psiManager = PsiManager.getInstance(fixture.getProject());

        IntellijJavaAnnotation annotation = new IntellijJavaAnnotation(
                getJavaFacade(fixture).findClass(
                        "com.app.util.Class1", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false)
                ).getModifierList().getAnnotations()[0]
        );

        assert annotation.getPsiAnnotation().getQualifiedName().equals("java.lang.Deprecated");
    }
}
