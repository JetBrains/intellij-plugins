package com.intellij.tapestry.tests;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.impl.ComponentManagerImpl;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiClass;
import com.intellij.tapestry.intellij.TapestryApplicationSupportLoader;
import com.intellij.tapestry.tests.mocks.TapestryApplicationSupportLoaderMock;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Alexey Chmutov
 *         Date: 03.04.2008
 */
public class Util {

  private Util() {
  }

  static String getFileText(final String filePath) {
    try {
      final FileReader reader = new FileReader(filePath);
      return FileUtil.loadTextAndClose(reader);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static PsiClass addEmptyJavaClassTo(JavaCodeInsightTestFixture fixture) throws IOException {
    return fixture.addClass("package foo; public class Bar {}");
  }

 public static void registerApplicationComponent() {
    if (!ApplicationManager.getApplication().hasComponent(TapestryApplicationSupportLoader.class)) {
      ((ComponentManagerImpl)ApplicationManager.getApplication())
          .registerComponent(TapestryApplicationSupportLoader.class, TapestryApplicationSupportLoaderMock.class);
    }
  }

  public static IdeaProjectTestFixture getWebModuleFixture() throws Exception {
    registerApplicationComponent();
    TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder();
    WebModuleFixtureBuilder webBuilder = fixtureBuilder.addModule(WebModuleFixtureBuilder.class);
    webBuilder.addContentRoot(new File("").getAbsoluteFile() + "/src/test/webModule");
    webBuilder.addSourceRoot("src");
    webBuilder.addJdk(System.getProperty("jdk.home"));
    webBuilder.addWebRoot(new File("").getAbsoluteFile() + "/src/test/webModule/resources", "/");
    webBuilder.addWebRoot(new File("").getAbsoluteFile() + "/src/test/webModule/WEB-INF", "/WEB-INF");

    IdeaProjectTestFixture webModuleFixture = fixtureBuilder.getFixture();
    webModuleFixture.setUp();
    return webModuleFixture;
  }
}