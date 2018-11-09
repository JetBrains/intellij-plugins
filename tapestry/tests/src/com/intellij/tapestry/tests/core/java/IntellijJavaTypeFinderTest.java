package com.intellij.tapestry.tests.core.java;

import com.intellij.tapestry.intellij.core.java.IntellijJavaTypeFinder;
import com.intellij.tapestry.tests.core.BaseTestCase;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class IntellijJavaTypeFinderTest extends BaseTestCase {

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void findType_no_dependencies(IdeaProjectTestFixture fixture) {
        assert new IntellijJavaTypeFinder(fixture.getModule()).findType("com.app.util.Class1", false).getFullyQualifiedName().equals("com.app.util.Class1");

        assert new IntellijJavaTypeFinder(fixture.getModule()).findType("com.app.dep.Dep1", false) == null;
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void findType_with_dependencies(IdeaProjectTestFixture fixture) {
        assert new IntellijJavaTypeFinder(fixture.getModule()).findType("com.app.util.Class1", true).getFullyQualifiedName().equals("com.app.util.Class1");

        assert new IntellijJavaTypeFinder(fixture.getModule()).findType("com.app.dep.Dep1", true).getFullyQualifiedName().equals("com.app.dep.Dep1");
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void findTypesInPackage_no_dependencies(IdeaProjectTestFixture fixture) {
        assert new IntellijJavaTypeFinder(fixture.getModule()).findTypesInPackage("com.app.util", false).size() == 6;

        assert new IntellijJavaTypeFinder(fixture.getModule()).findTypesInPackage("com.app.dep", false).size() == 0;
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void findTypesInPackage_with_dependencies(IdeaProjectTestFixture fixture) {
        assert new IntellijJavaTypeFinder(fixture.getModule()).findTypesInPackage("com.app.util", true).size() == 6;

        assert new IntellijJavaTypeFinder(fixture.getModule()).findTypesInPackage("com.app.dep", true).size() == 2;
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void findTypesInPackageRecursively_no_dependencies(IdeaProjectTestFixture fixture) {
        assert new IntellijJavaTypeFinder(fixture.getModule()).findTypesInPackageRecursively("com.app.util", false).size() == 7;

        assert new IntellijJavaTypeFinder(fixture.getModule()).findTypesInPackageRecursively("com.app.dep", false).size() == 0;
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void findTypesInPackageRecursively_with_dependencies(IdeaProjectTestFixture fixture) {
        assert new IntellijJavaTypeFinder(fixture.getModule()).findTypesInPackageRecursively("com.app.util", true).size() == 7;

        assert new IntellijJavaTypeFinder(fixture.getModule()).findTypesInPackageRecursively("com.app.dep", true).size() == 3;
    }
}
