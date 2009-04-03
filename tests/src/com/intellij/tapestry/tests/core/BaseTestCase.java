package com.intellij.tapestry.tests.core;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.impl.ComponentManagerImpl;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.intellij.tapestry.intellij.TapestryApplicationSupportLoader;
import com.intellij.tapestry.tests.mocks.TapestryApplicationSupportLoaderMock;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.search.GlobalSearchScope;
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
            TestFixtureBuilder<IdeaProjectTestFixture> testFixtureBuilder = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder();

            _emptyFixture = testFixtureBuilder.getFixture();
            _emptyFixture.setUp();
        }

        registerApplicationComponent();

        return new Object[][]{{_emptyFixture}};
    }

    @DataProvider(name = JAVA_MODULE_FIXTURE_PROVIDER)
    public Object[][] createJavaModuleFixtureData() throws Exception {
        if (_javaModuleFixture == null) {
            TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder();
            JavaModuleFixtureBuilder javaBuilder = fixtureBuilder.addModule(JavaModuleFixtureBuilder.class);
            javaBuilder.addContentRoot(new File("").getAbsoluteFile() + "/src/test/javaModule");
            javaBuilder.addSourceRoot("src");
            javaBuilder.addJdk(System.getProperty("jdk.home"));
            javaBuilder.addLibrary("library1");
            javaBuilder.addLibraryJars("library1", "", new File("").getAbsoluteFile() + "/src/test/javaModule/lib/dep1.jar");

            _javaModuleFixture = fixtureBuilder.getFixture();
            _javaModuleFixture.setUp();
        }

        registerApplicationComponent();

        return new Object[][]{{_javaModuleFixture}};
    }

    @DataProvider(name = WEB_MODULE_FIXTURE_PROVIDER)
    public Object[][] createWebModuleFixtureData() throws Exception {
        if (_webModuleFixture == null) {
            TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder();
            WebModuleFixtureBuilder webBuilder = fixtureBuilder.addModule(WebModuleFixtureBuilder.class);
            webBuilder.addContentRoot(new File("").getAbsoluteFile() + "/src/test/webModule");
            webBuilder.addSourceRoot("src");
            webBuilder.addJdk(System.getProperty("jdk.home"));
            webBuilder.addWebRoot(new File("").getAbsoluteFile() + "/src/test/webModule/resources", "/");
            webBuilder.addWebRoot(new File("").getAbsoluteFile() + "/src/test/webModule/WEB-INF", "/WEB-INF");

            _webModuleFixture = fixtureBuilder.getFixture();
            _webModuleFixture.setUp();
        }

        registerApplicationComponent();

        return new Object[][]{{_webModuleFixture}};
    }

    @AfterSuite
    public void releaseFixture() throws Exception {
        releaseFixture(_emptyFixture);
        _emptyFixture = null;

        releaseFixture(_javaModuleFixture);
        _javaModuleFixture = null;

        releaseFixture(_webModuleFixture);
        _webModuleFixture = null;
    }

    void registerApplicationComponent() {
        if (!ApplicationManager.getApplication().hasComponent(TapestryApplicationSupportLoader.class)) {
            ((ComponentManagerImpl) ApplicationManager.getApplication()).registerComponent(TapestryApplicationSupportLoader.class, TapestryApplicationSupportLoaderMock.class);
        }
    }

    private void releaseFixture(IdeaTestFixture fixture) throws Exception {
        if (fixture != null) {
            fixture.tearDown();
        }
    }

    protected static JavaPsiFacade getJavaFacade(IdeaProjectTestFixture fixture) {
        return JavaPsiFacade.getInstance(fixture.getProject());
    }

    protected GlobalSearchScope getAllScope(IdeaProjectTestFixture fixture) {
        return GlobalSearchScope.allScope(fixture.getProject());
    }

}
