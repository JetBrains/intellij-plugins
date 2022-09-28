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
    doTest(false, false, false, false, """
      -Project
       -PsiDirectory: lib
        -library.swf
         -b
          -zz
           -supb
            -subp2
             -subp3
              -p4
               Cl3
              Cl2
          Class1
         -com
          -foo
           Abc
         _f88e99c07c4ddd0d4cc87856b293119472de97251391839c016c3581d9799c24_flash_display_Sprite
         aClass
         bClass
         Interface1
         myConst
         myfunc
         MyNs
         myVar"""
    );
  }

  public void testSwfSortByNameWithMembers() {
    doTest(false, false, false, true, """
      -Project
       -PsiDirectory: lib
        -library.swf
         -b
          -zz
           -supb
            -subp2
             -subp3
              -p4
               -Cl3
                c:uint
                Cl3()
                foo1(String, Boolean, *):*
                foo2(Array):void
                foo3(Number):*
                foo4():String
                v1:*
                v2:String
                v3:*
                v4:Object
              -Cl2
               Cl2()
          -Class1
           c:uint
           Class1()
           foo1(String, Boolean, *):*
           foo2(Array):void
           foo3(Number):*
           v1:*
           v2:String
           v3:*
           v4:Object
         -com
          -foo
           -Abc
            _field:String
            Abc()
            field:String
            foo1(String, Boolean, *):*
         -_f88e99c07c4ddd0d4cc87856b293119472de97251391839c016c3581d9799c24_flash_display_Sprite
          _f88e99c07c4ddd0d4cc87856b293119472de97251391839c016c3581d9799c24_flash_display_Sprite()
          allowDomainInRSL(Array):void
          allowInsecureDomainInRSL(Array):void
          ExcludeClass
         -aClass
          aClass()
         -bClass
          bClass()
         -Interface1
          i1():String
          i2():int
         myConst
         myfunc
         MyNs
         myVar"""
    );
  }

  public void testSwfSortByType() {
    doTest(true, false, false, false, """
      -Project
       -PsiDirectory: lib
        -library.swf
         -b
          -zz
           -supb
            -subp2
             -subp3
              -p4
               Cl3
              Cl2
          Class1
         -com
          -foo
           Abc
         myConst
         MyNs
         myVar
         myfunc
         Interface1
         _f88e99c07c4ddd0d4cc87856b293119472de97251391839c016c3581d9799c24_flash_display_Sprite
         aClass
         bClass""");
  }

  public void testSwfHideEmptyMiddlePackages() {
    doTest(true, true, false, false, """
      -Project
       -PsiDirectory: lib
        -library.swf
         -b
          -zz.supb.subp2.subp3
           -p4
            Cl3
           Cl2
          Class1
         -com.foo
          Abc
         myConst
         MyNs
         myVar
         myfunc
         Interface1
         _f88e99c07c4ddd0d4cc87856b293119472de97251391839c016c3581d9799c24_flash_display_Sprite
         aClass
         bClass""");
  }

  public void testSwfFlattenPackages() {
    doTest(true, false, true, false, """
      -Project
       -PsiDirectory: lib
        -library.swf
         -b
          Class1
         -b.zz.supb.subp2.subp3
          Cl2
         -b.zz.supb.subp2.subp3.p4
          Cl3
         -com.foo
          Abc
         myConst
         MyNs
         myVar
         myfunc
         Interface1
         _f88e99c07c4ddd0d4cc87856b293119472de97251391839c016c3581d9799c24_flash_display_Sprite
         aClass
         bClass""");
  }

  public void testMembers() {
    doTest(true, false, false, true, """
      -Project
       -PsiDirectory: Members
        -Class1.as
         c:uint
         v1:*
         v2:String
         v3:*
         v4:Object
         foo1(String, Boolean, *):*
         foo2(Array):void
         foo3(Number):*
        -Class2.as
         _field:String
         field:String
        -MyComp.mxml
         outerField:int
         v:*
         bar(String, Array):int
         foo(Application):*
         outer():String
        MyComp2.as""");
  }

  public void testMxmlWithCss() {
    doTest(true, false, false, true, """
      -Project
       -PsiDirectory: MxmlWithCss
        -Foo.mxml
         #rSl s|Button#track
         .myButtonStyle
         namespace mx
         namespace s""");
  }
}
