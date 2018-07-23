package com.intellij.tapestry.tests.core.resource;

import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.intellij.core.resource.IntellijResourceFinder;
import com.intellij.tapestry.tests.core.BaseTestCase;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import org.testng.annotations.Test;

public class IntellijResourceFinderTest extends BaseTestCase {

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void findClasspathResource_no_dependencies(IdeaProjectTestFixture fixture) {
        IntellijResourceFinder resourceFinder = new IntellijResourceFinder(fixture.getModule());

        assert resourceFinder.findClasspathResource("/com/app/util/Home.tml", false).toArray(
          IResource.EMPTY_ARRAY)[0].getName().equals("Home.tml");

        assert resourceFinder.findClasspathResource("com/app/util/Home.tml", false).toArray(
          IResource.EMPTY_ARRAY)[0].getName().equals("Home.tml");

        assert resourceFinder.findClasspathResource("/com/app/dep/Home.tml", false).size() == 0;

        assert resourceFinder.findClasspathResource("com/app/dep/Home1.tml", false).size() == 0;

        assert resourceFinder.findClasspathResource("com/app/dep1/Home1.tml", false).size() == 0;
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void findClasspathResource_with_dependencies(IdeaProjectTestFixture fixture) {
        IntellijResourceFinder resourceFinder = new IntellijResourceFinder(fixture.getModule());

        assert resourceFinder.findClasspathResource("/com/app/util/Home.tml", true).toArray(
          IResource.EMPTY_ARRAY)[0].getName().equals("Home.tml");

        assert resourceFinder.findClasspathResource("com/app/util/Home.tml", true).toArray(
          IResource.EMPTY_ARRAY)[0].getName().equals("Home.tml");

        assert resourceFinder.findClasspathResource("/com/app/dep/Home.tml", true).toArray(
          IResource.EMPTY_ARRAY)[0].getName().equals("Home.tml");

        assert resourceFinder.findClasspathResource("com/app/dep/Home.tml", true).toArray(
          IResource.EMPTY_ARRAY)[0].getName().equals("Home.tml");

        assert resourceFinder.findClasspathResource("com/app/dep/Home1.tml", true).size() == 0;
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void findLocalizedClasspathResource_no_dependencies(IdeaProjectTestFixture fixture) {
        IntellijResourceFinder resourceFinder = new IntellijResourceFinder(fixture.getModule());

        assert resourceFinder.findLocalizedClasspathResource("/com/app/util/Home.tml", false).size() == 2;

        assert resourceFinder.findLocalizedClasspathResource("com/app/util/Home.tml", false).size() == 2;

        assert resourceFinder.findLocalizedClasspathResource("/com/app/dep/Home.tml", false).size() == 0;

        assert resourceFinder.findLocalizedClasspathResource("com/app/dep/Home1.tml", false).size() == 0;

        assert resourceFinder.findClasspathResource("com/app/dep1/Home1.tml", false).size() == 0;
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void findLocalizedClasspathResource_with_dependencies(IdeaProjectTestFixture fixture) {
        IntellijResourceFinder resourceFinder = new IntellijResourceFinder(fixture.getModule());

        assert resourceFinder.findLocalizedClasspathResource("/com/app/util/Home.tml", true).size() == 2;

        assert resourceFinder.findLocalizedClasspathResource("com/app/util/Home.tml", true).size() == 2;

        assert resourceFinder.findLocalizedClasspathResource("/com/app/dep/Home.tml", true).size() == 2;

        assert resourceFinder.findLocalizedClasspathResource("com/app/dep/Home.tml", true).size() == 2;

        assert resourceFinder.findLocalizedClasspathResource("com/app/dep/Home1.tml", true).size() == 0;
    }

    //@TODO uncomment when http://www.jetbrains.net/jira/browse/IDEA-17361 is fixed
    /*@Test(dataProvider = WEB_MODULE_FIXTURE_PROVIDER)
    public void findContextResource(IdeaProjectTestFixture fixture) {
        IntellijResourceFinder resourceFinder = new IntellijResourceFinder(fixture.getModule());

        assert resourceFinder.findContextResource("/WEB-INF/web.xml").getName().equals("web.xml");

        assert resourceFinder.findContextResource("/web.xml") == null;

        assert resourceFinder.findContextResource("/Page1.tml").getName().equals("Page1.tml");
    }

    @Test(dataProvider = WEB_MODULE_FIXTURE_PROVIDER)
    public void findLocalizedContextResource(IdeaProjectTestFixture fixture) {
        IntellijResourceFinder resourceFinder = new IntellijResourceFinder(fixture.getModule());

        assert resourceFinder.findLocalizedContextResource("/Page1.tml").size() == 2;

        assert resourceFinder.findLocalizedContextResource("/folder1/Page2.tml").size() == 2;
    }*/
}
