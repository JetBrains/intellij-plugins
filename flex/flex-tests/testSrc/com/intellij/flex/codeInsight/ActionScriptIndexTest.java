// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.codeInsight;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.index.JSIndexKeys;
import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.resolve.BackendJSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSClassResolver;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ActionScriptIndexTest extends BasePlatformTestCase {

  private static GlobalSearchScope getCollectScope(boolean includeNonProjectItems, Project project) {
    return includeNonProjectItems ? JSResolveUtil.getJavaScriptSymbolsResolveScope(project) : ProjectScope.getContentScope(project);
  }

  private static boolean isFromSwc(JSQualifiedNamedElement element) {
    return element.getContainingFile().getVirtualFile().getName().endsWith(".swf");
  }

  public static NavigationItem @NotNull [] getSymbolsByName(String name, boolean includeNonProjectItems, Project project) {
    GlobalSearchScope scope = getCollectScope(includeNonProjectItems, project);

    final Collection<JSPsiElementBase> symbolNavItems = JSClassResolver.findElementsByNameIncludingImplicit(name, scope, false);
    Collection<JSQualifiedNamedElement> qualified = BackendJSResolveUtil.findElementsByName(name, project, scope);

    for (Iterator<JSQualifiedNamedElement> q = qualified.iterator(); q.hasNext(); ) {
      JSQualifiedNamedElement element = q.next();
      if (!isFromSwc(element)) {
        q.remove();
      }
    }

    symbolNavItems.addAll(qualified);

    return symbolNavItems.toArray(NavigationItem.EMPTY_NAVIGATION_ITEM_ARRAY);
  }

  public void testClassSimple() {
    doTest("simple", "a");
    final NavigationItem[] symbols = getSymbolsByName("simple", false, getProject());
    assertEquals("A, " + getTestName(false) + ".js2", getLocationString(symbols[0]));
    doTest(true, "A");

    final NavigationItem[] navigationItems = JavaScriptIndex.getInstance(getProject()).getFileByName(getTestName(false) + "." + getExtension(), true);
    assertEquals(1, navigationItems.length);
    assertTrue(navigationItems[0] instanceof JSFile);
  }

  public void testPackageWithClass() {
    doTest("ZZZ", "simple");
    NavigationItem[] symbols = getSymbolsByName("simple", false, getProject());
    assertEquals("aaa.MyPackage.MyClazz, " + getTestName(false) + ".js2", getLocationString(symbols[0]));
    doTest(true, "MyClazz");
    symbols = JavaScriptIndex.getInstance(getProject()).getClassByName("MyClazz", false);
    assertEquals("(aaa.MyPackage, " + getTestName(false) + ".js2)", getLocationString(symbols[0]));
  }

  public void testClassAndFuncWithQName() {
    doTest("simple");
    NavigationItem[] symbols = getSymbolsByName("simple", false, getProject());
    assertEquals("(bbb, " + getTestName(false) + ".js2)", getLocationString(symbols[0]));

    doTest(true, "MyClass");
    symbols = JavaScriptIndex.getInstance(getProject()).getClassByName("MyClass", false);
    assertEquals("(aaa, " + getTestName(false) + ".js2)", getLocationString(symbols[0]));
  }

  public void testNSSimple() {
    doTest("var_", "NS", "zzz", "zzz2");
    NavigationItem[] symbols = getSymbolsByName("var_", false, getProject());
    assertEquals("Clazz.NS, " + getTestName(false) + ".js2", getLocationString(symbols[0]));

    symbols = getSymbolsByName("NS", false, getProject());
    assertEquals("Clazz, " + getTestName(false) + ".js2", getLocationString(symbols[0]));

    symbols = getSymbolsByName("zzz2", false, getProject());
    assertEquals("Clazz.NS, " + getTestName(false) + ".js2", getLocationString(symbols[0]));

    doTest(true, "Clazz");
  }

  private void doTest(String... names) {
    doTest(false, names);
  }

  private void doTest(boolean checkClasses, final String... names) {
    myFixture.configureByFile(getTestName(false) + "." + getExtension());

    final String[] symbolNameArray = getSymbolNames(getProject());

    final Set<String> strings = Set.of(symbolNameArray);
    for (String s : names) {
      assertTrue(s + " not in index", strings.contains(s));
      NavigationItem[] navigationItems = getSymbolsByName(s, false, getProject());
      if (navigationItems.length > 0) {
        for (NavigationItem navItem : navigationItems) {
          assertTrue(navItem.canNavigate());
        }
      }
      else {
        fail("No symbol:" + s);
      }
    }
  }

  private String getExtension() {
    return "js2";
  }

  private static String getLocationString(NavigationItem symbol) {
    ItemPresentation presentation = symbol != null ? symbol.getPresentation() : null;
    return presentation != null ? presentation.getLocationString() : null;
  }

  private static String[] getSymbolNames(Project project) {
    final Set<String> symbolNames = new HashSet<>();
    symbolNames.addAll(StubIndex.getInstance().getAllKeys(JSIndexKeys.JS_SYMBOL_INDEX_2_KEY, project));
    symbolNames.addAll(StubIndex.getInstance().getAllKeys(JSIndexKeys.JS_NAME_INDEX_KEY, project));
    return ArrayUtilRt.toStringArray(symbolNames);
  }

  @Override
  protected @NotNull String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("/index/");
  }
}
