// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import one.util.streamex.StreamEx;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Entity;
import org.angular2.entities.Angular2Module;
import org.angularjs.AngularTestUtil;

import java.util.List;

import static com.intellij.util.containers.ContainerUtil.emptyList;
import static com.intellij.util.containers.ContainerUtil.sorted;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class ModulesTest extends LightPlatformCodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "modules";
  }

  public void testCommonModuleResolution() {
    List<String> allDeclarations = asList(
      "AsyncPipe", "CurrencyPipe", "DatePipe", "DecimalPipe", "I18nPluralPipe", "I18nSelectPipe", "JsonPipe", "KeyValuePipe",
      "LowerCasePipe", "NgClass", "NgComponentOutlet", "NgForOf", "NgIf", "NgPlural", "NgPluralCase", "NgStyle", "NgSwitch",
      "NgSwitchCase", "NgSwitchDefault", "NgTemplateOutlet", "PercentPipe", "SlicePipe", "TitleCasePipe", "UpperCasePipe");
    doResolutionTest("common", "common_module.ts", "export class Common<caret>Module",
                     emptyList(), allDeclarations, allDeclarations, allDeclarations, true, true);
  }

  public void testRouterModuleResolution() {
    doResolutionTest("router", "myModule.ts", "class AppRouting<caret>Module {",
                     singletonList("RouterModule"),
                     singletonList("MyDirective"),
                     emptyList(),
                     asList("MyDirective", "RouterOutlet", "RouterLink", "RouterLinkWithHref", "RouterLinkActive", "EmptyOutletComponent"),
                     true, true);
  }

  public void testRouterModuleResolutionNotFull() {
    doResolutionTest("router", "myModule.ts", "export class AppRoutingModule<caret>NotFullyResolved {",
                     singletonList("RouterModule"),
                     singletonList("MyDirective"),
                     singletonList("RouterOutlet"),
                     asList("MyDirective", "RouterOutlet", "RouterLink", "RouterLinkWithHref", "RouterLinkActive", "EmptyOutletComponent"),
                     true, false);
  }

  public void testBrowserModuleResolutionNotFull() {
    doResolutionTest("browser", "myModule.ts", "class BrowserModule<caret>Test {",
                     singletonList("BrowserModule"),
                     singletonList("MyDirective"),
                     singletonList("AsyncPipe"),
                     asList("MyDirective", "AsyncPipe"),
                     false, false);
  }

  private void doResolutionTest(String directory, String moduleFile, String signature,
                                List<String> imports, List<String> declarations, List<String> exports,
                                List<String> scope, boolean exportsFullyResolved, boolean scopeFullyResolved) {
    VirtualFile testDir = myFixture.copyDirectoryToProject(directory, "/");
    myFixture.openFileInEditor(testDir.findChild(moduleFile));
    int moduleOffset = AngularTestUtil.findOffsetBySignature(signature, myFixture.getFile());
    PsiElement el = myFixture.getFile().findElementAt(moduleOffset);
    assert el != null;
    TypeScriptClass moduleClass = PsiTreeUtil.getParentOfType(el, TypeScriptClass.class);
    assert moduleClass != null;
    Angular2Module module = Angular2EntitiesProvider.getModule(moduleClass);
    assert module != null;
    assertEquals(sorted(imports), toStringList(module.getImports()));
    assertEquals(sorted(declarations), toStringList(module.getDeclarations()));
    assertEquals(sorted(exports), toStringList(module.getExports()));
    assertEquals(sorted(scope), toStringList(module.getDeclarationsInScope()));
    assert exportsFullyResolved == module.areExportsFullyResolved();
    assert scopeFullyResolved == module.isScopeFullyResolved();
  }


  private static List<String> toStringList(List<? extends Angular2Entity> entities) {
    return StreamEx.of(entities)
      .map(decl -> decl.getTypeScriptClass())
      .nonNull()
      .map(cls -> cls.getNameIdentifier())
      .nonNull()
      .map(id -> id.getText())
      .sorted()
      .toList();
  }
}
