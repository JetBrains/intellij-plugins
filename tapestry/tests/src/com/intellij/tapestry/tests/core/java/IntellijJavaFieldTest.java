package com.intellij.tapestry.tests.core.java;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.tapestry.core.java.IJavaArrayType;
import com.intellij.tapestry.core.java.IJavaPrimitiveType;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.core.java.IntellijJavaField;
import com.intellij.tapestry.tests.core.BaseTestCase;
import com.intellij.tapestry.tests.mocks.PsiClassTypeMock;
import com.intellij.tapestry.tests.mocks.PsiFieldMock;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.util.IncorrectOperationException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class IntellijJavaFieldTest extends BaseTestCase {

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getName(IdeaProjectTestFixture fixture) throws IncorrectOperationException {
        PsiField psiField = getJavaFacade(fixture).getElementFactory().createField("_fieldName", PsiType.BOOLEAN);

        assertEquals(new IntellijJavaField(fixture.getModule(), psiField).getName(), "_fieldName");

        assertEquals(new IntellijJavaField(fixture.getModule(), psiField).getPsiField().getName(), "_fieldName");
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getType_primitive(IdeaProjectTestFixture fixture) throws IncorrectOperationException {
        PsiField psiField = getJavaFacade(fixture).getElementFactory().createField("_fieldName", PsiType.BOOLEAN);

        assertTrue(new IntellijJavaField(fixture.getModule(), psiField).getType() instanceof IJavaPrimitiveType);

        assertEquals(new IntellijJavaField(fixture.getModule(), psiField).getType().getName(), "boolean");
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getType_class(IdeaProjectTestFixture fixture) throws IncorrectOperationException {
        IntellijJavaField field = new IntellijJavaField(
                fixture.getModule(),
                getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(fixture.getModule())).getFields()[1]
        );

        assertTrue(field.getType() instanceof IntellijJavaClassType);

        assertEquals(((IntellijJavaClassType)field.getType()).getFullyQualifiedName(), "com.app.util.Class1");
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getType_cant_resolve(IdeaProjectTestFixture fixture) throws IncorrectOperationException {
        IntellijJavaField field = new IntellijJavaField(fixture.getModule(), new PsiFieldMock().setType(new PsiClassTypeMock().setResolve(null)));

        assertNull(field.getType());
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getType_array(IdeaProjectTestFixture fixture) throws IncorrectOperationException {
        IntellijJavaField field = new IntellijJavaField(
                fixture.getModule(),
                getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(fixture.getModule())).getFields()[2]
        );

        assertTrue(field.getType() instanceof IJavaArrayType);
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void isPrivate(IdeaProjectTestFixture fixture) throws IncorrectOperationException {
        IntellijJavaField field1 = new IntellijJavaField(
                fixture.getModule(),
                getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(fixture.getModule())).getFields()[0]
        );

        IntellijJavaField field2 = new IntellijJavaField(
                fixture.getModule(),
                getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(fixture.getModule())).getFields()[1]
        );

        assertTrue(field1.isPrivate());

        assertFalse(field2.isPrivate());
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getAnnotations_no_annotations(IdeaProjectTestFixture fixture) {
        IntellijJavaField field1 = new IntellijJavaField(
                fixture.getModule(),
                getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(fixture.getModule())).getFields()[2]
        );

        assertEquals(field1.getAnnotations().size(), 0);
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getAnnotations_with_annotations(IdeaProjectTestFixture fixture) {
        IntellijJavaField field2 = new IntellijJavaField(
                fixture.getModule(),
                getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(fixture.getModule())).getFields()[1]
        );

        assertEquals(field2.getAnnotations().size(), 1);
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getDocumentation_no_documentation(IdeaProjectTestFixture fixture) {
        IntellijJavaField field1 = new IntellijJavaField(
                fixture.getModule(),
                getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(fixture.getModule())).getFields()[0]
        );

        assertTrue(field1.getDocumentation().isEmpty());
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getDocumentation_with_documentation(IdeaProjectTestFixture fixture) {
        IntellijJavaField field2 = new IntellijJavaField(
                fixture.getModule(),
                getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(fixture.getModule())).getFields()[1]
        );

        assertEquals(field2.getDocumentation(), " field2. docs.");
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getStringRepresentation(IdeaProjectTestFixture fixture) throws IncorrectOperationException {
        assertEquals(
          new IntellijJavaField(fixture.getModule(), getJavaFacade(fixture).getElementFactory().createField("_fieldName", PsiType.BOOLEAN))
            .getStringRepresentation(), "private boolean _fieldName;");
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void isValid(IdeaProjectTestFixture fixture) {
        assertTrue(new IntellijJavaField(fixture.getModule(), new PsiFieldMock().setValid(true)).isValid());

        assertFalse(new IntellijJavaField(fixture.getModule(), new PsiFieldMock().setValid(false)).isValid());
    }

    @SuppressWarnings({"SimplifiedTestNGAssertion", "EqualsBetweenInconvertibleTypes", "ConstantConditions"})
    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void testEquals(IdeaProjectTestFixture fixture) {
        IntellijJavaField field1 = new IntellijJavaField(fixture.getModule(), new PsiFieldMock().setMockName("field1"));
        IntellijJavaField field2 = new IntellijJavaField(fixture.getModule(), new PsiFieldMock().setMockName("field2"));
        IntellijJavaField field3 = new IntellijJavaField(fixture.getModule(), new PsiFieldMock().setMockName("field1"));

        assertFalse(field1.equals(null));
        assertFalse(field1.equals(""));

        assertNotEquals(field2, field1);

        assertEquals(field3, field1);
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void hashCode(IdeaProjectTestFixture fixture) {
        IntellijJavaField field1 = new IntellijJavaField(fixture.getModule(), new PsiFieldMock().setMockName("field1"));

        assertEquals("field1".hashCode(), field1.hashCode());
    }
}
