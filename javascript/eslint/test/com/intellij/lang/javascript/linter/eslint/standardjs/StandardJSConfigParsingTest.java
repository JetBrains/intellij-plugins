package com.intellij.lang.javascript.linter.eslint.standardjs;

import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.eslint.standardjs.StandardJSUtil;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;

@TestApplication
public class StandardJSConfigParsingTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);

  @Test
  public void testCanParseConfig() {
    configureByText("""
                      {
                        "standard": {
                          "globals": [
                            "ignoredGlobal"
                          ],
                          "ignore": "ignored/*",
                          "parser": "babel-eslint",
                          "plugins": "flowtype"
                        }
                      }""");
    StandardJSUtil.ConfigData packageJsonData = getParsedData();
    Assertions.assertNotNull(packageJsonData, "Expected config object but was null");
    Assertions.assertEquals(Arrays.asList("flowtype"), packageJsonData.plugins);
    Assertions.assertEquals(Arrays.asList("ignoredGlobal"), packageJsonData.globals);
    Assertions.assertEquals("babel-eslint", packageJsonData.parser);
  }

  @Test
  public void testInvalidConfig() {
    configureByText("""
                      {"standard": {
                        "globals": {},
                        ##
                      }}""");
    Assertions.assertNull(getParsedData());
  }

  @Test
  public void testEnvValues() {
    configureByText("""
                      {
                        "standard": {
                          "envs": "foobar"
                        }
                      }

                      """);
    Assertions.assertEquals(Arrays.asList("foobar"), getParsedData().env);
    configureByText("""
                      {
                        "standard": {
                          "env": ["jasmine", "jest"]
                        }
                      }

                      """);
    Assertions.assertEquals(Arrays.asList("jasmine", "jest"), getParsedData().env);
    configureByText("""
                      {
                        "standard": {
                          "env": "jasmine"  }
                      }

                      """);
    Assertions.assertEquals(Arrays.asList("jasmine"), getParsedData().env);
    configureByText("""
                      {
                        "standard": {
                          "env": {
                            "jasmine": true
                          }
                        }
                      }

                      """);
    Assertions.assertEquals(Arrays.asList("jasmine"), getParsedData().env);
  }


  private void configureByText(String text) {
    codeInsight.get().configureByText(PackageJsonUtil.FILE_NAME, text);
  }

  private @Nullable StandardJSUtil.ConfigData getParsedData() {
    return StandardJSUtil.getPackageJsonConfigData(codeInsight.get().getProject(), codeInsight.get().getFile().getVirtualFile());
  }
}
