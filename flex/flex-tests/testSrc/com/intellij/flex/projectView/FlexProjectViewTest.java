// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.projectView;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.AbstractProjectViewPSIPane;
import com.intellij.ide.projectView.impl.ProjectViewImpl;
import com.intellij.projectView.BaseProjectViewTestCase;
import com.intellij.testFramework.PlatformTestUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

// this is to make sure that testMxmlWithCss() is not the first one, otherwise it sometimes fails on buildserver because of too long class loading
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FlexProjectViewTest extends BaseProjectViewTestCase {

  private static final String PANE_ID = "";
  protected AbstractProjectViewPSIPane myPane;

  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    myPane = null;
    super.tearDown();
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected void setUpJdk() {
    // no JDK
  }

  @NotNull
  @Override
  protected String getTestDirectoryName() {
    String testName = getTestName(false);
    return testName.startsWith("Swf") ? "lib" : testName;
  }

  private void doTest(boolean sortByType, boolean hideEmptyMiddlePackages, boolean flattenPackages, boolean showMembers, String expected) {
    myPane = myStructure.createPane();

    ProjectViewImpl projectView = (ProjectViewImpl)ProjectView.getInstance(myProject);
    //ProjectViewTestUtil.setupImpl(getProject(), false);
    projectView.addProjectPane(myPane);
    projectView.setSortByType(PANE_ID, sortByType);
    myStructure.setShowMembers(showMembers);
    myStructure.setHideEmptyMiddlePackages(hideEmptyMiddlePackages);
    myStructure.setFlattenPackages(flattenPackages);

    PlatformTestUtil.expandAll(myPane.getTree());
    PlatformTestUtil.assertTreeEqual(myPane.getTree(), expected + "\n" + " External Libraries\n", true);
  }

  public void testSwfSortByName() {
    doTest(false, false, false, false, "-Project\n" +
                                       " -PsiDirectory: lib\n" +
                                       "  -library.swf\n" +
                                       "   -b\n" +
                                       "    -zz\n" +
                                       "     -supb\n" +
                                       "      -subp2\n" +
                                       "       -subp3\n" +
                                       "        -p4\n" +
                                       "         Cl3\n" +
                                       "        Cl2\n" +
                                       "    Class1\n" +
                                       "   -com\n" +
                                       "    -foo\n" +
                                       "     Abc\n" +
                                       "   _f88e99c07c4ddd0d4cc87856b293119472de97251391839c016c3581d9799c24_flash_display_Sprite\n" +
                                       "   aClass\n" +
                                       "   bClass\n" +
                                       "   Interface1\n" +
                                       "   myConst\n" +
                                       "   myfunc\n" +
                                       "   MyNs\n" +
                                       "   myVar"
    );
  }

  public void testSwfSortByNameWithMembers() {
    doTest(false, false, false, true, "-Project\n" +
                                      " -PsiDirectory: lib\n" +
                                      "  -library.swf\n" +
                                      "   -b\n" +
                                      "    -zz\n" +
                                      "     -supb\n" +
                                      "      -subp2\n" +
                                      "       -subp3\n" +
                                      "        -p4\n" +
                                      "         -Cl3\n" +
                                      "          c:uint\n" +
                                      "          Cl3()\n" +
                                      "          foo1(String, Boolean, *):*\n" +
                                      "          foo2(Array):void\n" +
                                      "          foo3(Number):*\n" +
                                      "          foo4():String\n" +
                                      "          v1:*\n" +
                                      "          v2:String\n" +
                                      "          v3:*\n" +
                                      "          v4:Object\n" +
                                      "        -Cl2\n" +
                                      "         Cl2()\n" +
                                      "    -Class1\n" +
                                      "     c:uint\n" +
                                      "     Class1()\n" +
                                      "     foo1(String, Boolean, *):*\n" +
                                      "     foo2(Array):void\n" +
                                      "     foo3(Number):*\n" +
                                      "     v1:*\n" +
                                      "     v2:String\n" +
                                      "     v3:*\n" +
                                      "     v4:Object\n" +
                                      "   -com\n" +
                                      "    -foo\n" +
                                      "     -Abc\n" +
                                      "      _field:String\n" +
                                      "      Abc()\n" +
                                      "      field:String\n" +
                                      "      foo1(String, Boolean, *):*\n" +
                                      "   -_f88e99c07c4ddd0d4cc87856b293119472de97251391839c016c3581d9799c24_flash_display_Sprite\n" +
                                      "    _f88e99c07c4ddd0d4cc87856b293119472de97251391839c016c3581d9799c24_flash_display_Sprite()\n" +
                                      "    allowDomainInRSL(Array):void\n" +
                                      "    allowInsecureDomainInRSL(Array):void\n" +
                                      "    ExcludeClass\n" +
                                      "   -aClass\n" +
                                      "    aClass()\n" +
                                      "   -bClass\n" +
                                      "    bClass()\n" +
                                      "   -Interface1\n" +
                                      "    i1():String\n" +
                                      "    i2():int\n" +
                                      "   myConst\n" +
                                      "   myfunc\n" +
                                      "   MyNs\n" +
                                      "   myVar"
    );
  }

  public void testSwfSortByType() {
    doTest(true, false, false, false, "-Project\n" +
                                      " -PsiDirectory: lib\n" +
                                      "  -library.swf\n" +
                                      "   -b\n" +
                                      "    -zz\n" +
                                      "     -supb\n" +
                                      "      -subp2\n" +
                                      "       -subp3\n" +
                                      "        -p4\n" +
                                      "         Cl3\n" +
                                      "        Cl2\n" +
                                      "    Class1\n" +
                                      "   -com\n" +
                                      "    -foo\n" +
                                      "     Abc\n" +
                                      "   myConst\n" +
                                      "   MyNs\n" +
                                      "   myVar\n" +
                                      "   myfunc\n" +
                                      "   Interface1\n" +
                                      "   _f88e99c07c4ddd0d4cc87856b293119472de97251391839c016c3581d9799c24_flash_display_Sprite\n" +
                                      "   aClass\n" +
                                      "   bClass");
  }

  public void testSwfHideEmptyMiddlePackages() {
    doTest(true, true, false, false, "-Project\n" +
                                     " -PsiDirectory: lib\n" +
                                     "  -library.swf\n" +
                                     "   -b\n" +
                                     "    -zz.supb.subp2.subp3\n" +
                                     "     -p4\n" +
                                     "      Cl3\n" +
                                     "     Cl2\n" +
                                     "    Class1\n" +
                                     "   -com.foo\n" +
                                     "    Abc\n" +
                                     "   myConst\n" +
                                     "   MyNs\n" +
                                     "   myVar\n" +
                                     "   myfunc\n" +
                                     "   Interface1\n" +
                                     "   _f88e99c07c4ddd0d4cc87856b293119472de97251391839c016c3581d9799c24_flash_display_Sprite\n" +
                                     "   aClass\n" +
                                     "   bClass");
  }

  public void testSwfFlattenPackages() {
    doTest(true, false, true, false, "-Project\n" +
                                     " -PsiDirectory: lib\n" +
                                     "  -library.swf\n" +
                                     "   -b\n" +
                                     "    Class1\n" +
                                     "   -b.zz.supb.subp2.subp3\n" +
                                     "    Cl2\n" +
                                     "   -b.zz.supb.subp2.subp3.p4\n" +
                                     "    Cl3\n" +
                                     "   -com.foo\n" +
                                     "    Abc\n" +
                                     "   myConst\n" +
                                     "   MyNs\n" +
                                     "   myVar\n" +
                                     "   myfunc\n" +
                                     "   Interface1\n" +
                                     "   _f88e99c07c4ddd0d4cc87856b293119472de97251391839c016c3581d9799c24_flash_display_Sprite\n" +
                                     "   aClass\n" +
                                     "   bClass");
  }

  public void testMembers() {
    doTest(true, false, false, true, "-Project\n" +
                                     " -PsiDirectory: Members\n" +
                                     "  -Class1.as\n" +
                                     "   c:uint\n" +
                                     "   v1:*\n" +
                                     "   v2:String\n" +
                                     "   v3:*\n" +
                                     "   v4:Object\n" +
                                     "   foo1(String, Boolean, *):*\n" +
                                     "   foo2(Array):void\n" +
                                     "   foo3(Number):*\n" +
                                     "  -Class2.as\n" +
                                     "   _field:String\n" +
                                     "   field:String\n" +
                                     "  -MyComp.mxml\n" +
                                     "   outerField:int\n" +
                                     "   v:*\n" +
                                     "   bar(String, Array):int\n" +
                                     "   foo(Application):*\n" +
                                     "   outer():String\n" +
                                     "  MyComp2.as");
  }

  public void testMxmlWithCss() {
    doTest(true, false, false, true, "-Project\n" +
                                     " -PsiDirectory: MxmlWithCss\n" +
                                     "  -Foo.mxml\n" +
                                     "   #rSl s|Button#track\n" +
                                     "   .myButtonStyle\n" +
                                     "   namespace mx\n" +
                                     "   namespace s");
  }
}
