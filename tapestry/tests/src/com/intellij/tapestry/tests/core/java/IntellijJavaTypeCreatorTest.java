package com.intellij.tapestry.tests.core.java;

import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiImportList;
import com.intellij.tapestry.core.java.IJavaAnnotation;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.core.java.IntellijJavaTypeCreator;
import com.intellij.tapestry.tests.core.BaseTestCase;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.util.IncorrectOperationException;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class IntellijJavaTypeCreatorTest extends BaseTestCase {

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void createField(IdeaProjectTestFixture fixture) {
        IntellijJavaTypeCreator intellijJavaTypeCreator = new IntellijJavaTypeCreator(fixture.getModule());

        IJavaField psiField1 = intellijJavaTypeCreator.createField("field1",
                new IntellijJavaClassType(fixture.getModule(), getJavaFacade(fixture).findClass("java.lang.String", getAllScope(fixture)).getContainingFile()), true, true);
        assert psiField1.getName().equals("field1");
        assert ((IJavaClassType) psiField1.getType()).getFullyQualifiedName().equals("java.lang.String");
        assert psiField1.isPrivate();

        IJavaField psiField2 = intellijJavaTypeCreator.createField("Field1",
                new IntellijJavaClassType(fixture.getModule(), getJavaFacade(fixture).findClass("java.lang.String", getAllScope(fixture)).getContainingFile()), true, true);
        assert psiField2.getName().equals("field1");

        IJavaField psiField3 = intellijJavaTypeCreator.createField("Field1",
                new IntellijJavaClassType(fixture.getModule(), getJavaFacade(fixture).findClass("java.lang.String", getAllScope(fixture)).getContainingFile()), true, false);
        assert psiField3.getName().equals("Field1");

        IJavaField psiField4 = intellijJavaTypeCreator.createField("field1",
                new IntellijJavaClassType(fixture.getModule(), getJavaFacade(fixture).findClass("java.lang.String", getAllScope(fixture)).getContainingFile()), false, true);
        assert !psiField4.isPrivate();
    }

  @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void createFieldAnnotation_no_parameters(IdeaProjectTestFixture fixture) {
        IntellijJavaTypeCreator intellijJavaTypeCreator = new IntellijJavaTypeCreator(fixture.getModule());

        IJavaField psiField1 = intellijJavaTypeCreator.createField("field1",
                new IntellijJavaClassType(fixture.getModule(), getJavaFacade(fixture).findClass("java.lang.String", getAllScope(fixture)).getContainingFile()), true, true);
        intellijJavaTypeCreator.createFieldAnnotation(psiField1, "java.lang.Deprecated", new HashMap<>());

        assert psiField1.getAnnotations().size() == 1;

        assert ((IJavaAnnotation) psiField1.getAnnotations().values().toArray()[0]).getFullyQualifiedName().equals("java.lang.Deprecated");

        assert ((IJavaAnnotation) psiField1.getAnnotations().values().toArray()[0]).getParameters().size() == 0;
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void createFieldAnnotation_with_parameters(IdeaProjectTestFixture fixture) {
        IntellijJavaTypeCreator intellijJavaTypeCreator = new IntellijJavaTypeCreator(fixture.getModule());

        IJavaField psiField1 = intellijJavaTypeCreator.createField("field1",
                new IntellijJavaClassType(fixture.getModule(), getJavaFacade(fixture).findClass("java.lang.String", getAllScope(fixture)).getContainingFile()), true, true);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("param1", "param1value");
        parameters.put("param2", "param2value");

        intellijJavaTypeCreator.createFieldAnnotation(psiField1, "java.lang.Deprecated", parameters);

        assert psiField1.getAnnotations().size() == 1;

        IJavaAnnotation annotation = (IJavaAnnotation) psiField1.getAnnotations().values().toArray()[0];

        assert annotation.getFullyQualifiedName().equals("java.lang.Deprecated");
        assert annotation.getParameters().size() == 2;
        assert annotation.getParameters().get("param1")[0].equals("param1value");
        assert annotation.getParameters().get("param2")[0].equals("param2value");
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void ensureClassImport_already_imported(IdeaProjectTestFixture fixture) {
        IntellijJavaClassType testedClass = new IntellijJavaClassType(fixture.getModule(),
                getJavaFacade(fixture).findClass("com.app.ModuleBuilder", getAllScope(fixture)).getContainingFile());
        IntellijJavaTypeCreator intellijJavaTypeCreator = new IntellijJavaTypeCreator(fixture.getModule());

        assert intellijJavaTypeCreator.ensureClassImport(testedClass,
                new IntellijJavaClassType(fixture.getModule(), getJavaFacade(fixture).findClass("java.util.Collection", getAllScope(fixture)).getContainingFile()));
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void ensureClassImport_not_imported(IdeaProjectTestFixture fixture) throws IncorrectOperationException {
        IntellijJavaTypeCreator controlMock = createMock(IntellijJavaTypeCreator.class);
        controlMock.addImport(isA(PsiImportList.class), isA(PsiClass.class));
        replay(controlMock);

        IntellijJavaClassType testedClass = new IntellijJavaClassType(fixture.getModule(),
                getJavaFacade(fixture).findClass("com.app.ModuleBuilder", getAllScope(fixture)).getContainingFile());
        IntellijJavaTypeCreator intellijJavaTypeCreator = new IntellijJavaTypeCreatorDummy(fixture.getModule(), controlMock);

        assert intellijJavaTypeCreator.ensureClassImport(testedClass,
                new IntellijJavaClassType(fixture.getModule(), getJavaFacade(fixture).findClass(CommonClassNames.JAVA_UTIL_MAP, getAllScope(fixture)).getContainingFile()));

        verify(controlMock);
    }
}
