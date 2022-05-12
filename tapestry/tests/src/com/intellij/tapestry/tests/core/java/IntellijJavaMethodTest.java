package com.intellij.tapestry.tests.core.java;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.tapestry.core.java.IJavaArrayType;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.intellij.core.java.IntellijJavaMethod;
import com.intellij.tapestry.intellij.core.java.IntellijJavaPrimitiveType;
import com.intellij.tapestry.tests.core.BaseTestCase;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class IntellijJavaMethodTest extends BaseTestCase {

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void test_full_method(IdeaProjectTestFixture fixture) {

        PsiMethod psiMethod = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.allScope(fixture.getProject())).findMethodsByName("method1", false)[0];
        IntellijJavaMethod method = new IntellijJavaMethod(fixture.getModule(), psiMethod);

        assert method.getName().equals("method1");

        assert ((IJavaClassType) method.getReturnType()).getFullyQualifiedName().equals("com.app.util.Class1");

        assert method.getAnnotations().size() == 2;

        assert method.getDocumentation().equals(" method1 doc.");

        assert method.getParameters().size() == 3;
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void test_empty_methods(IdeaProjectTestFixture fixture) {

        PsiMethod psiMethod2 = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.allScope(fixture.getProject())).findMethodsByName("method2", false)[0];
        IntellijJavaMethod method2 = new IntellijJavaMethod(fixture.getModule(), psiMethod2);

        PsiMethod psiMethod3 = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.allScope(fixture.getProject())).findMethodsByName("method3", false)[0];
        IntellijJavaMethod method3 = new IntellijJavaMethod(fixture.getModule(), psiMethod3);

        PsiMethod psiMethod4 = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.allScope(fixture.getProject())).findMethodsByName("method4", false)[0];
        IntellijJavaMethod method4 = new IntellijJavaMethod(fixture.getModule(), psiMethod4);

        PsiMethod psiMethod5 = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.allScope(fixture.getProject())).findMethodsByName("method5", false)[0];
        IntellijJavaMethod method5 = new IntellijJavaMethod(fixture.getModule(), psiMethod5);

        assert method2.getName().equals("method2");

        assert method2.getReturnType() instanceof IJavaArrayType;

        assert method2.getAnnotations().size() == 0;

        assert method2.getDocumentation().isEmpty();

        assert method2.getParameters().size() == 0;


        assert method3.getName().equals("method3");

        assert method3.getReturnType().getName().equals("int");

        assert method3.getAnnotations().size() == 0;

        assert method3.getDocumentation().isEmpty();

        assert method3.getParameters().size() == 0;

        // void return
        assert method4.getReturnType() instanceof IntellijJavaPrimitiveType;

        assert method4.getReturnType().getName().equals("void");

        // invalid class return
        assert method5.getReturnType() == null;
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getAnnotation(IdeaProjectTestFixture fixture) {

        PsiMethod psiMethod1 = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.allScope(fixture.getProject())).findMethodsByName("method1", false)[0];
        IntellijJavaMethod method1 = new IntellijJavaMethod(fixture.getModule(), psiMethod1);

        assert method1.getAnnotation(null) == null;

        assert method1.getAnnotation("java.lang.SuppressWarnings").getFullyQualifiedName().equals("java.lang.SuppressWarnings");
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getContainingClass(IdeaProjectTestFixture fixture) {

        PsiMethod psiMethod1 = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.allScope(fixture.getProject())).findMethodsByName("method1", false)[0];
        IntellijJavaMethod method1 = new IntellijJavaMethod(fixture.getModule(), psiMethod1);

        assert method1.getContainingClass().getName().equals("Class1");
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getPsiMethod(IdeaProjectTestFixture fixture) {

        PsiMethod psiMethod1 = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.allScope(fixture.getProject())).findMethodsByName("method1", false)[0];
        IntellijJavaMethod method1 = new IntellijJavaMethod(fixture.getModule(), psiMethod1);

        assert method1.getPsiMethod().getName().equals("method1");
    }
}
