package com.intellij.tapestry.tests.core.java;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.core.resource.IntellijResource;
import com.intellij.tapestry.intellij.core.resource.IntellijResourceFinder;
import com.intellij.tapestry.tests.core.BaseTestCase;
import com.intellij.tapestry.tests.mocks.PsiFileMock;
import com.intellij.tapestry.tests.mocks.VirtualFileMock;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import org.testng.annotations.Test;

public class IntellijJavaClassTypeTest extends BaseTestCase {

    private IntellijJavaClassType _notJavaClassType;

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getFullyQualifiedName(IdeaProjectTestFixture fixture) {
        PsiClass class1 = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false));
        IntellijJavaClassType intellijJavaClassType = new IntellijJavaClassType(fixture.getModule(), class1.getContainingFile());

        assert intellijJavaClassType.getFullyQualifiedName().equals("com.app.util.Class1");

        assert intellijJavaClassType.getPsiClass().getQualifiedName().equals("com.app.util.Class1");
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getName(IdeaProjectTestFixture fixture) {
        PsiClass class1 = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false));
        IntellijJavaClassType intellijJavaClassType = new IntellijJavaClassType(fixture.getModule(), class1.getContainingFile());

        assert intellijJavaClassType.getName().equals("Class1");
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getFile(IdeaProjectTestFixture fixture) {
        PsiClass class1 = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false));
        IntellijJavaClassType intellijJavaClassType = new IntellijJavaClassType(fixture.getModule(), class1.getContainingFile());

        assert intellijJavaClassType.getFile().getName().equals("Class1.java");
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void isInterface_true(IdeaProjectTestFixture fixture) {
        PsiClass interface1 = getJavaFacade(fixture).findClass("com.app.util.Interface1", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false));
        IntellijJavaClassType intellijJavaClassType = new IntellijJavaClassType(fixture.getModule(), interface1.getContainingFile());

        assert intellijJavaClassType.isInterface();
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void isInterface_false(IdeaProjectTestFixture fixture) {
        PsiClass class1 = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false));
        IntellijJavaClassType intellijJavaClassType = new IntellijJavaClassType(fixture.getModule(), class1.getContainingFile());

        assert !intellijJavaClassType.isInterface();
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void isPublic_true(IdeaProjectTestFixture fixture) {
        PsiClass class1 = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false));
        IntellijJavaClassType intellijJavaClassType = new IntellijJavaClassType(fixture.getModule(), class1.getContainingFile());

        assert intellijJavaClassType.isPublic();
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void isPublic_false(IdeaProjectTestFixture fixture) {
        PsiClass class5 = getJavaFacade(fixture).findClass("com.app.util.Class5", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false));
        IntellijJavaClassType intellijJavaClassType = new IntellijJavaClassType(fixture.getModule(), class5.getContainingFile());

        assert !intellijJavaClassType.isPublic();
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void hasDefaultConstructor_true(IdeaProjectTestFixture fixture) {

        PsiClass class1 = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false));
        IntellijJavaClassType intellijJavaClassType = new IntellijJavaClassType(fixture.getModule(), class1.getContainingFile());

        assert intellijJavaClassType.hasDefaultConstructor();
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void hasDefaultConstructor_false(IdeaProjectTestFixture fixture) {

        PsiClass class2 = getJavaFacade(fixture).findClass("com.app.util.Class2", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false));
        IntellijJavaClassType intellijJavaClassType = new IntellijJavaClassType(fixture.getModule(), class2.getContainingFile());

        assert !intellijJavaClassType.hasDefaultConstructor();
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getPublicMethods_no_public_methods(IdeaProjectTestFixture fixture) {

        PsiClass class3 = getJavaFacade(fixture).findClass("com.app.util.Class3", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false));
        IntellijJavaClassType intellijJavaClassType = new IntellijJavaClassType(fixture.getModule(), class3.getContainingFile());

        assert intellijJavaClassType.getPublicMethods(true).size() == 0;
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getPublicMethods_with_public_methods(IdeaProjectTestFixture fixture) {

        PsiClass class1 = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false));
        IntellijJavaClassType intellijJavaClassType = new IntellijJavaClassType(fixture.getModule(), class1.getContainingFile());

        assert intellijJavaClassType.getPublicMethods(true).size() == 4;

        assert intellijJavaClassType.getPublicMethods(false).size() == 3;
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getAllMethods_no_methods(IdeaProjectTestFixture fixture) {

        PsiClass class3 = getJavaFacade(fixture).findClass("com.app.util.Class3", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false));
        IntellijJavaClassType intellijJavaClassType = new IntellijJavaClassType(fixture.getModule(), class3.getContainingFile());

        assert intellijJavaClassType.getAllMethods(true).size() == 2;
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getAllMethods_with_methods(IdeaProjectTestFixture fixture) {

        PsiClass class1 = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false));
        IntellijJavaClassType intellijJavaClassType = new IntellijJavaClassType(fixture.getModule(), class1.getContainingFile());

        assert intellijJavaClassType.getAllMethods(true).size() == 6;

        assert intellijJavaClassType.getAllMethods(false).size() == 5;
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void findMethods(IdeaProjectTestFixture fixture) {

        PsiClass class1 = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false));
        IntellijJavaClassType intellijJavaClassType = new IntellijJavaClassType(fixture.getModule(), class1.getContainingFile());

        assert intellijJavaClassType.findPublicMethods("[a-z]*[0-9]").size() == 3;
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getAnnotations_no_annotations(IdeaProjectTestFixture fixture) {

        PsiClass class3 = getJavaFacade(fixture).findClass("com.app.util.Class3", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false));
        IntellijJavaClassType intellijJavaClassType = new IntellijJavaClassType(fixture.getModule(), class3.getContainingFile());

        assert intellijJavaClassType.getAnnotations().size() == 0;

        IntellijResourceFinder resourceFinder = new IntellijResourceFinder(fixture.getModule());
        _notJavaClassType = new IntellijJavaClassType(fixture.getModule(), ((IntellijResource) resourceFinder.findClasspathResource("/com/app/util/Home.tml", false).toArray()[0]).getPsiFile());

        assert _notJavaClassType.getAnnotations().size() == 0;
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getAnnotations_with_annotations(IdeaProjectTestFixture fixture) {

        PsiClass class1 = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false));
        IntellijJavaClassType intellijJavaClassType = new IntellijJavaClassType(fixture.getModule(), class1.getContainingFile());

        assert intellijJavaClassType.getAnnotations().size() == 2;
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getFields(IdeaProjectTestFixture fixture) {

        PsiClass class1 = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false));
        IntellijJavaClassType intellijJavaClassType = new IntellijJavaClassType(fixture.getModule(), class1.getContainingFile());

        assert intellijJavaClassType.getFields(true).size() == 4;

        assert intellijJavaClassType.getFields(false).size() == 3;

        IntellijResourceFinder resourceFinder = new IntellijResourceFinder(fixture.getModule());
        _notJavaClassType = new IntellijJavaClassType(fixture.getModule(), ((IntellijResource) resourceFinder.findClasspathResource("/com/app/util/Home.tml", false).toArray()[0]).getPsiFile());

        assert _notJavaClassType.getFields(true).size() == 0;
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getDocumentation_no_documentation(IdeaProjectTestFixture fixture) {

        PsiClass class1 = getJavaFacade(fixture).findClass("com.app.util.Class1", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false));
        IntellijJavaClassType intellijJavaClassType = new IntellijJavaClassType(fixture.getModule(), class1.getContainingFile());

        assert intellijJavaClassType.getDocumentation().isEmpty();

        IntellijResourceFinder resourceFinder = new IntellijResourceFinder(fixture.getModule());
        _notJavaClassType = new IntellijJavaClassType(fixture.getModule(), ((IntellijResource) resourceFinder.findClasspathResource("/com/app/util/Home.tml", false).toArray()[0]).getPsiFile());

        assert _notJavaClassType.getDocumentation().length() == 0;
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getDocumentation_with_documentation(IdeaProjectTestFixture fixture) {

        PsiClass class2 = getJavaFacade(fixture).findClass("com.app.util.Class2", GlobalSearchScope.moduleRuntimeScope(fixture.getModule(), false));
        IntellijJavaClassType intellijJavaClassType = new IntellijJavaClassType(fixture.getModule(), class2.getContainingFile());

        assert intellijJavaClassType.getDocumentation().equals(" class2. docs.");
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void getFile_file_doesn_exist(IdeaProjectTestFixture fixture) {
        PsiFile psiFileMock = new PsiFileMock().setVirtualFile(new VirtualFileMock().setUrl("file:///doesnt.exist"));

        IntellijJavaClassType classType = new IntellijJavaClassType(fixture.getModule(), psiFileMock);

        assert classType.getFile() == null;
    }
}
