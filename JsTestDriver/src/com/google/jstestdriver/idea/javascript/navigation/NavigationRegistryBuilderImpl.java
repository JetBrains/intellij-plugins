/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver.idea.javascript.navigation;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.FlagsImpl;
import com.google.jstestdriver.PathResolver;
import com.google.jstestdriver.config.Configuration;
import com.google.jstestdriver.config.ConfigurationSource;
import com.google.jstestdriver.config.UserConfigurationSource;
import com.google.jstestdriver.config.YamlParser;
import com.google.jstestdriver.hooks.FileParsePostProcessor;
import com.google.jstestdriver.idea.util.CastUtils;
import com.google.jstestdriver.util.DisplayPathSanitizer;
import com.intellij.execution.PsiLocation;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class NavigationRegistryBuilderImpl extends NavigationRegistryBuilder {

  private NavigationRegistryBuilderImpl() {}

  public static void register() {
    NavigationRegistryBuilder.setInstance(new NavigationRegistryBuilderImpl());
  }

  @Override
  public NavigationRegistry buildNavigationRegistry(Project project, @NotNull VirtualFile configVirtualFile) {
    File configFile = new File(configVirtualFile.getPath());
    if (!configFile.isFile()) {
      return null;
    }

    final Configuration configuration;
    try {
      configuration = resolveConfiguration(configFile);
    } catch (Exception e) {
      return null;
    }
    Set<FileInfo> fileInfo = configuration.getFilesList();

    List<TestCase> allTestCases = Lists.newArrayList();
    for (FileInfo info : fileInfo) {
      File file = info.toFile(null);
      VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(file);
      if (vf != null) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(vf);
        if (psiFile instanceof JSFile) {
          NavigationRegistry navigationRegistry = NavigationUtils.fetchNavigationRegistryByJsFile((JSFile) psiFile);
          if (navigationRegistry != null) {
            allTestCases.addAll(navigationRegistry.listTestCases());
          }
        }
      }
    }
    return new NavigationRegistry(configVirtualFile, allTestCases);
  }

  @Override
  public NavigationRegistry buildNavigationRegistryByJsFile(JSFile jsFile) {
    List<TestCase> testCases = extractDefaultTestCases(jsFile);
    List<TestCase> allTestCases = Lists.newArrayList();
    allTestCases.addAll(testCases);
    List<TestCase> qunitTestCases = extractQUnitTestCases(jsFile);
    allTestCases.addAll(qunitTestCases);
    return new NavigationRegistry(jsFile.getVirtualFile(), allTestCases);
  }

  private List<TestCase> extractQUnitTestCases(JSFile jsFile) {
    List<TestCaseBuilder> testCaseBuilders = Lists.newArrayList();
    testCaseBuilders.add(new TestCaseBuilder("Default Module", null));
    collectQUnitTestCases(jsFile, testCaseBuilders);
    return new ArrayList<TestCase>(Lists.transform(testCaseBuilders, new Function<TestCaseBuilder, TestCase>() {
      @Override
      public TestCase apply(TestCaseBuilder testCaseBuilder) {
        return testCaseBuilder.build();
      }
    }));
  }

  private void collectQUnitTestCases(JSElement jsElement, List<TestCaseBuilder> container) {
    JSCallExpression callExpression = CastUtils.tryCast(jsElement, JSCallExpression.class);
    if (callExpression != null) {
      JSReferenceExpression referenceExpression = CastUtils.tryCast(callExpression.getMethodExpression(), JSReferenceExpression.class);
      if (referenceExpression != null) {
        if ("module".equals(referenceExpression.getReferencedName())) {
          JSExpression[] argumentExprs = callExpression.getArgumentList().getArguments();
          if (argumentExprs.length > 0) {
            JSLiteralExpression literalExpression = CastUtils.tryCast(argumentExprs[0], JSLiteralExpression.class);
            if (literalExpression != null) {
              String name = removeQuotes(literalExpression.getText());
              TestCaseBuilder testCaseBuilder = new TestCaseBuilder(name, PsiLocation.fromPsiElement(callExpression));
              container.add(testCaseBuilder);
            }
          }
        }
        if ("test".equals(referenceExpression.getReferencedName())) {
          JSExpression[] argumentExprs = callExpression.getArgumentList().getArguments();
          if (argumentExprs.length > 0) {
            JSLiteralExpression literalExpression = CastUtils.tryCast(argumentExprs[0], JSLiteralExpression.class);
            if (literalExpression != null) {
              String name = removeQuotes(literalExpression.getText());
              TestCaseBuilder testCaseBuilder = container.get(container.size() - 1);
              testCaseBuilder.addTest(new Test("test " + name, PsiLocation.fromPsiElement(callExpression)));
            }
          }
        }
      }
    }
    for (PsiElement child : jsElement.getChildren()) {
      JSElement childJsElement = CastUtils.tryCast(child, JSElement.class);
      if (childJsElement != null) {
        collectQUnitTestCases(childJsElement, container);
      }
    }
  }

  private Configuration resolveConfiguration(File configFile) {
    FlagsImpl flags = new FlagsImpl();
    flags.setServer("test:1");
    try {
      ConfigurationSource confSrc = new UserConfigurationSource(configFile);
      File initialBasePath = configFile.getParentFile();
      Configuration parsedConf = confSrc.parse(initialBasePath, new YamlParser());
      File resolvedBasePath = parsedConf.getBasePath().getCanonicalFile();
      PathResolver pathResolver = new PathResolver(
          resolvedBasePath,
          Collections.<FileParsePostProcessor>emptySet(),
          new DisplayPathSanitizer(resolvedBasePath)
      );
      return parsedConf.resolvePaths(pathResolver, flags);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read settings file " + configFile, e);
    }
  }

  private List<TestCase> extractDefaultTestCases(JSFile jsFile) {
    List<TestCaseBuilder> testCaseBuilders = Lists.newArrayList();
    collectTestCases(jsFile, testCaseBuilders);
    for (TestCaseBuilder testCaseBuilder : testCaseBuilders) {
      collectTests(testCaseBuilder);
    }
    return new ArrayList<TestCase>(Lists.transform(testCaseBuilders, new Function<TestCaseBuilder, TestCase>() {
      @Override
      public TestCase apply(TestCaseBuilder testCaseBuilder) {
        return testCaseBuilder.build();
      }
    }));
  }

  private void collectTests(TestCaseBuilder testCaseBuilder) {
    JSCallExpression callExpression = testCaseBuilder.getLocation().getPsiElement();
    JSExpression[] exprs = callExpression.getArgumentList().getArguments();
    if (exprs.length > 1) {
      JSObjectLiteralExpression objectLiteralExpr = CastUtils.tryCast(exprs[1], JSObjectLiteralExpression.class);
      if (objectLiteralExpr != null) {
        JSProperty[] properties = objectLiteralExpr.getProperties();
        for (JSProperty property : properties) {
          ASTNode nameIdentifier = property.findNameIdentifier();
          if (nameIdentifier != null) {
            String testName = removeQuotes(nameIdentifier.getText());
            Test test = new Test(testName, PsiLocation.fromPsiElement(property));
            testCaseBuilder.addTest(test);
          }
        }
      }
    }
  }

  private void collectTestCases(JSElement jsElement, List<TestCaseBuilder> container) {
    JSCallExpression callExpression = CastUtils.tryCast(jsElement, JSCallExpression.class);
    if (callExpression != null) {
      JSReferenceExpression referenceExpression = CastUtils.tryCast(callExpression.getMethodExpression(), JSReferenceExpression.class);
      if (referenceExpression != null) {
        if ("TestCase".equals(referenceExpression.getReferencedName())) {
          JSExpression[] argumentExprs = callExpression.getArgumentList().getArguments();
          if (argumentExprs.length > 0) {
            JSLiteralExpression literalExpression = CastUtils.tryCast(argumentExprs[0], JSLiteralExpression.class);
            if (literalExpression != null) {
              String name = removeQuotes(literalExpression.getText());
              TestCaseBuilder testCaseBuilder = new TestCaseBuilder(name, PsiLocation.fromPsiElement(callExpression));
              container.add(testCaseBuilder);
            }
          }
        }
      }
    }
    for (PsiElement child : jsElement.getChildren()) {
      JSElement childJsElement = CastUtils.tryCast(child, JSElement.class);
      if (childJsElement != null) {
        collectTestCases(childJsElement, container);
      }
    }
  }

  private String removeQuotes(String str) {
    if (str == null) {
      return null;
    }
    String singleQ = "'";
    String doubleQ = "\"";
    if ((str.startsWith(singleQ) && str.endsWith(singleQ)) || (str.startsWith(doubleQ) && str.endsWith(doubleQ))) {
      str = str.substring(1, str.length() - 1);
    }
    return str;
  }

}
