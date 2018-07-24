package com.intellij.tapestry.tests.core.java;

import com.intellij.psi.PsiParameter;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaPrimitiveType;
import com.intellij.tapestry.intellij.core.java.IntellijMethodParameter;
import com.intellij.tapestry.tests.core.BaseTestCase;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class IntellijMethodParameterTest extends BaseTestCase {

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void test_class_type(IdeaProjectTestFixture fixture) {


        PsiParameter parameter = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.allScope(fixture.getProject())).findMethodsByName("method1", false)[0].getParameterList()
                .getParameters()[0];
        IntellijMethodParameter methodParameter = new IntellijMethodParameter(fixture.getModule(), parameter);

        assert methodParameter.getName().equals("param1");

        assert methodParameter.getType() instanceof IJavaClassType;

        assert ((IJavaClassType) methodParameter.getType()).getFullyQualifiedName().equals("com.app.util.Class1");
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void test_primitive_type(IdeaProjectTestFixture fixture) {


        PsiParameter parameter = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.allScope(fixture.getProject())).findMethodsByName("method1", false)[0].getParameterList()
                .getParameters()[1];
        IntellijMethodParameter methodParameter = new IntellijMethodParameter(fixture.getModule(), parameter);

        assert methodParameter.getName().equals("param2");

        assert methodParameter.getType() instanceof IJavaPrimitiveType;

        assert methodParameter.getType().getName().equals("int");
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void test_other_type(IdeaProjectTestFixture fixture) {


        PsiParameter parameter = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.allScope(fixture.getProject())).findMethodsByName("method1", false)[0].getParameterList()
                .getParameters()[2];
        IntellijMethodParameter methodParameter = new IntellijMethodParameter(fixture.getModule(), parameter);

        assert methodParameter.getName().equals("param3");

        assert methodParameter.getType() == null;
    }
}
