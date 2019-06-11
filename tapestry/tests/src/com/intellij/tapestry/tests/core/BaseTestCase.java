package com.intellij.tapestry.tests.core;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.tapestry.tests.Util;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.DataProvider;

import java.io.File;

public class BaseTestCase {

    protected static final String EMPTY_FIXTURE_PROVIDER = "emptyFixtureProvider";
    protected static final String JAVA_MODULE_FIXTURE_PROVIDER = "javaModuleFixtureProvider";
    protected static final String WEB_MODULE_FIXTURE_PROVIDER = "webModuleFixtureProvider";

    private static IdeaProjectTestFixture _emptyFixture;
    private static IdeaProjectTestFixture _javaModuleFixture;
    private static IdeaProjectTestFixture _webModuleFixture;

    @DataProvider(name = EMPTY_FIXTURE_PROVIDER)
    public Object[][] createEmptyFixtureData() throws Exception {
        if (_emptyFixture == null) {
            _emptyFixture = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder(getClass().getSimpleName()).getFixture();
            _emptyFixture.setUp();
        }

        return new Object[][]{{_emptyFixture}};
    }

    @DataProvider(name = JAVA_MODULE_FIXTURE_PROVIDER)
    public Object[][] createJavaModuleFixtureData() throws Exception {
        if (_javaModuleFixture == null) {
            TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder(getClass().getSimpleName());
            JavaModuleFixtureBuilder javaBuilder = fixtureBuilder.addModule(JavaModuleFixtureBuilder.class);
            javaBuilder.addContentRoot(new File("").getAbsoluteFile() + "/src/test/javaModule");
            javaBuilder.addSourceRoot("src");
            String sdkPath = System.getProperty("jdk.home");
            if (sdkPath != null) {
                javaBuilder.addJdk(sdkPath);
            }
            javaBuilder.addLibrary("library1");
            javaBuilder.addLibraryJars("library1", "", new File("").getAbsoluteFile() + "/src/test/javaModule/lib/dep1.jar");

            _javaModuleFixture = fixtureBuilder.getFixture();
            _javaModuleFixture.setUp();
        }

        return new Object[][]{{_javaModuleFixture}};
    }

    @DataProvider(name = WEB_MODULE_FIXTURE_PROVIDER)
    public Object[][] createWebModuleFixtureData() throws Exception {
        if (_webModuleFixture == null) {
          _webModuleFixture = Util.getWebModuleFixture(getClass().getSimpleName());
        }

        return new Object[][]{{_webModuleFixture}};
    }

    @AfterSuite
    public void releaseFixture() {
        releaseFixture(_emptyFixture);
        _emptyFixture = null;

        releaseFixture(_javaModuleFixture);
        _javaModuleFixture = null;

        releaseFixture(_webModuleFixture);
        _webModuleFixture = null;
    }

    private static void releaseFixture(IdeaTestFixture fixture) {
        if (fixture != null) {
          try {
            fixture.tearDown();
          }
          catch (Exception e) {
            // ignored
          }
        }
    }

    protected static JavaPsiFacade getJavaFacade(IdeaProjectTestFixture fixture) {
        return JavaPsiFacade.getInstance(fixture.getProject());
    }

    protected GlobalSearchScope getAllScope(IdeaProjectTestFixture fixture) {
        return GlobalSearchScope.allScope(fixture.getProject());
    }

}
