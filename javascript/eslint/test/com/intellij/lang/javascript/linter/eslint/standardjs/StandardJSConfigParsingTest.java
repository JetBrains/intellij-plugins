package com.intellij.lang.javascript.linter.eslint.standardjs;

import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.eslint.standardjs.StandardJSUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.hamcrest.core.Is;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;

import java.util.Arrays;

public class StandardJSConfigParsingTest extends BasePlatformTestCase {

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
    Assert.assertNotNull("Expected config object but was null", packageJsonData);
    Assert.assertThat(packageJsonData.plugins, Is.is(Arrays.asList("flowtype")));
    Assert.assertThat(packageJsonData.globals, Is.is(Arrays.asList("ignoredGlobal")));
    Assert.assertThat(packageJsonData.parser, Is.is("babel-eslint"));
  }

  public void testInvalidConfig() {
    configureByText("""
                      {"standard": {
                        "globals": {},
                        ##
                      }}""");
    Assert.assertNull(getParsedData());
  }

  public void testEnvValues() {
    configureByText("""
                      {
                        "standard": {
                          "envs": "foobar"
                        }
                      }

                      """);
    Assert.assertThat(getParsedData().env, Is.is(Arrays.asList("foobar")));
    configureByText("""
                      {
                        "standard": {
                          "env": ["jasmine", "jest"]
                        }
                      }

                      """);
    Assert.assertThat(getParsedData().env, Is.is(Arrays.asList("jasmine", "jest")));
    configureByText("""
                      {
                        "standard": {
                          "env": "jasmine"  }
                      }

                      """);
    Assert.assertThat(getParsedData().env, Is.is(Arrays.asList("jasmine")));
    configureByText("""
                      {
                        "standard": {
                          "env": {
                            "jasmine": true
                          }
                        }
                      }

                      """);
    Assert.assertThat(getParsedData().env, Is.is(Arrays.asList("jasmine")));
  }


  private void configureByText(String text) {
    myFixture.configureByText(PackageJsonUtil.FILE_NAME, text);
  }

  private @Nullable StandardJSUtil.ConfigData getParsedData() {
    return StandardJSUtil.getPackageJsonConfigData(getProject(), myFixture.getFile().getVirtualFile());
  }
}
