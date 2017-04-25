package com.intellij.flex.refactoring;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSClassResolver;
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil;
import com.intellij.lang.javascript.refactoring.moveMembers.JSMoveMembersDialog;
import com.intellij.lang.javascript.refactoring.moveMembers.JSMoveMembersOptions;
import com.intellij.lang.javascript.refactoring.moveMembers.JSMoveMembersProcessor;
import com.intellij.lang.javascript.refactoring.util.JSMemberInfo;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.impl.source.PostprocessReformattingAspect;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.MultiFileTestCase;
import com.intellij.refactoring.classMembers.MemberInfoBase;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class FlexMoveMembersTest extends MultiFileTestCase {

  private static final String VISIBILITY_AS_IS = null;

  private static final String[] ALL_MEMBERS = new String[]{};

  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
  }

  @NotNull
  @Override
  protected String getTestRoot() {
    return "moveMembers/";
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  private void doTest(final String sourceClassName, final String targetClassName, final String visibility, final String memberName)
    throws Exception {
    doTest(sourceClassName, targetClassName, visibility, new String[]{memberName});
  }

  private void doTest(final String sourceClassName, final String targetClassName, final String visibility, final String[] memberNames)
    throws Exception {
    try {
      doTestImpl(sourceClassName, targetClassName, visibility, memberNames);
    }
    catch (BaseRefactoringProcessor.ConflictsInTestsException e) {
      fail("Conflicts: " + toString(e.getMessages(), "\n"));
    }
  }

  private void doTestImpl(final String sourceClassName, final String targetClassName, final String visibility, final String[] memberNames)
    throws Exception {
    doTest(new PerformAction() {
      @Override
      public void performAction(VirtualFile rootDir, VirtualFile rootAfter) throws Exception {
        FlexMoveMembersTest.this.performAction(sourceClassName, targetClassName, visibility, memberNames);
      }
    });
  }

  private static void foo() {
    bar();
  }

  private static void bar() {
    foo();
  }

  private void performAction(String sourceClassName, final String targetClassName, final String visibility, final String[] memberNames)
    throws Exception {
    final JSClassResolver resolver =
      JSDialectSpecificHandlersFactory.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4).getClassResolver();
    final JSClass sourceClass = (JSClass)resolver.findClassByQName(sourceClassName, JSMoveMembersDialog.getScope(myProject));
    assertNotNull("Class " + sourceClassName + " not found", sourceClass);
    JSClass targetClass = (JSClass)resolver.findClassByQName(targetClassName, JSMoveMembersDialog.getScope(myProject));
    assertNotNull("Class " + targetClassName + " not found", targetClass);

    final List<JSMemberInfo> memberInfos = new ArrayList<>();
    JSMemberInfo.extractStaticMembers(sourceClass, memberInfos, new MemberInfoBase.Filter<JSAttributeListOwner>() {
      @Override
      public boolean includeMember(JSAttributeListOwner member) {
        return memberNames.length == 0 || ArrayUtil.contains(member.getName(), memberNames);
      }
    });

    JSMemberInfo.sortByOffset(memberInfos);
    for (JSMemberInfo memberInfo : memberInfos) {
      memberInfo.setChecked(true);
    }

    new JSMoveMembersProcessor(myProject, null, sourceClass, JSMoveMembersDialog.getScope(myProject), new JSMoveMembersOptions() {

      @Override
      public JSAttributeListOwner[] getSelectedMembers() {
        final JSMemberInfo[] selected = JSMemberInfo.getSelected(memberInfos, sourceClass, Conditions.alwaysTrue());
        JSAttributeListOwner[] result = new JSAttributeListOwner[selected.length];
        for (int i = 0; i < result.length; i++) {
          result[i] = selected[i].getMember();
        }
        return result;
      }

      @Override
      public String getTargetClassName() {
        return targetClassName;
      }

      @Override
      public String getMemberVisibility() {
        return visibility;
      }
    }).run();

    myProject.getComponent(PostprocessReformattingAspect.class).doPostponedFormatting();
    FileDocumentManager.getInstance().saveAllDocuments();
  }

  private void doTestConflicts(final String sourceClassName,
                               final String targetClassName,
                               final String visibility,
                               final String[] memberNames,
                               String[] expectedConflicts) throws Exception {
    try {
      doTestImpl(sourceClassName, targetClassName, visibility, memberNames);
      fail("conflicts expected");
    }
    catch (BaseRefactoringProcessor.ConflictsInTestsException e) {
      assertSameElements(e.getMessages(), expectedConflicts);
    }
  }

  public void testSamePackage() throws Exception {
    doTest("foo.From", "foo.To", null, "func");
  }

  public void testVar() throws Exception {
    doTest("From", "To", VISIBILITY_AS_IS, "aVar");
  }

  public void testVar1Of2() throws Exception {
    doTest("From", "To", VISIBILITY_AS_IS, "var1");
  }

  public void testVar2Of2() throws Exception {
    doTest("From", "To", VISIBILITY_AS_IS, "var2");
  }

  public void test2Vars() throws Exception {
    doTest("From", "To", VISIBILITY_AS_IS, new String[]{"var1", "var2"});
  }

  public void testBackAndForth() throws Exception {
    doTest(new PerformAction() {
      @Override
      public void performAction(VirtualFile rootDir, VirtualFile rootAfter) throws Exception {
        FlexMoveMembersTest.this.performAction("From", "To", VISIBILITY_AS_IS, new String[]{"foo"});
        FlexMoveMembersTest.this.performAction("To", "From", VISIBILITY_AS_IS, new String[]{"foo"});
      }
    });
  }

  public void testImports() throws Exception {
    doTest("a.From", "b.To", VISIBILITY_AS_IS, "foo");
  }

  public void testMultiple() throws Exception {
    doTest("a.From", "b.To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testEscalateVisibility1() throws Exception {
    doTest("a.From", "b.To", JSVisibilityUtil.ESCALATE_VISIBILITY, ALL_MEMBERS);
  }

  public void testEscalateVisibility2() throws Exception {
    doTest("From", "To", JSVisibilityUtil.ESCALATE_VISIBILITY, ALL_MEMBERS);
  }

  public void testEscalateVisibility3() throws Exception {
    doTest("From", "To", JSVisibilityUtil.ESCALATE_VISIBILITY, ALL_MEMBERS);
  }

  public void testEscalateVisibility4() throws Exception {
    doTest("a.From", "b.To", JSVisibilityUtil.ESCALATE_VISIBILITY, ALL_MEMBERS);
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testFromMxml1() throws Exception {
    doTest("From", "foo.To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testImports2() throws Exception {
    doTest("a.From", "a.To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testImports3() throws Exception {
    doTest("a.From", "b.To", VISIBILITY_AS_IS, new String[]{"foo", "bar"});
  }

  public void testImports4() throws Exception {
    doTest("a.From", "b.To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testConflicts1() throws Exception {
    String[] conflicts = new String[]{"Field From.foo with private visibility won't be accessible from method move1()",
      "Method move1() with private visibility in the target class is not accessible from field From.foo",
      "Constant move2 with protected visibility in the target class is not accessible from field From.bar",
      "Field From.zzz with internal visibility won't be accessible from method move1()",
      "Inner class Aux won't be accessible from method move1()",
      "Constant move3 with internal visibility in the target class is not accessible from field From.zzz",
      "Field From.bar with protected visibility won't be accessible from method move1()"};
    doTestConflicts("a.From", "b.To", VISIBILITY_AS_IS, new String[]{"move1", "move2", "move3"}, conflicts);
  }

  public void testConflicts2() throws Exception {
    String[] conflicts = new String[]{"Class To already contains a field bar", "Class To already contains a method foo()"};
    doTestConflicts("From", "To", VISIBILITY_AS_IS, new String[]{"foo", "bar", "zz"}, conflicts);
  }

  public void testConflicts3() throws Exception {
    String[] conflicts = new String[]{"Method From.move2() with protected visibility won't be accessible from method move()"};
    doTestConflicts("From", "a.To", VISIBILITY_AS_IS, new String[]{"move"}, conflicts);
  }

  public void testConflicts4() throws Exception {
    String[] conflicts =
      new String[]{"Method move() with protected visibility in the target class is not accessible from method From.move2()",
        "Class Class1 with internal visibility won't be accessible from method move()"};
    doTestConflicts("From", "a.To", VISIBILITY_AS_IS, new String[]{"move"}, conflicts);
  }

  public void testConflicts5() throws Exception {
    String[] conflicts = new String[]{"Field a with internal visibility in the target class is not accessible from method From.foo()"};
    doTestConflicts("foo.From", "bar.To", VISIBILITY_AS_IS, ALL_MEMBERS, conflicts);
  }

  public void testProperty() throws Exception {
    doTest("From", "To", VISIBILITY_AS_IS, new String[]{"readwrite", "read", "write"});
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testImportInMxml() throws Exception {
    doTest("From", "foo.To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testImportInMxml2() throws Exception {
    doTest("bar.From", "bar.To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testImports5() throws Exception {
    doTest("From", "To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testImportInMxml3() throws Exception {
    doTest("foo.From", "bar.To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testSimple() throws Exception {
    doTest("From", "com.To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testDequalify() throws Exception {
    doTest("From", "To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testReferenceToAS3() throws Exception {
    doTest("From", "To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testConstructorCall() throws Exception {
    doTest("foo.From", "To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testNamespaces() throws Exception {
    doTest("foo.From", "bar.To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testNamespaces2() throws Exception {
    doTest("foo.From", "bar.To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testConflictsNs() throws Exception {
    String[] conflicts = new String[]{"Namespace MyNs with internal visibility won't be accessible from field v",
      "Namespace MyNs with internal visibility won't be accessible from method foo()",
      "Namespace MyNs2 with internal visibility won't be accessible from method foo()"};
    doTestConflicts("foo.From", "bar.To", VISIBILITY_AS_IS, ALL_MEMBERS, conflicts);
  }

  public void testNamespaces3() throws Exception {
    doTest("foo.From", "bar.To", VISIBILITY_AS_IS, new String[]{"foo", "bar"});
  }

  public void testNamespaces4() throws Exception {
    doTest("From", "To", JSAttributeList.AccessType.PUBLIC.name(), new String[]{"foo", "bar", "ZZ"});
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testVector() throws Exception {
    doTest("foo.From", "To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testEmbeddedImage() throws Exception {
    doTest("From", "To", JSVisibilityUtil.ESCALATE_VISIBILITY, ALL_MEMBERS);
  }

  public void testToInnerClass() throws Exception {
    doTest("C", "Ggg", JSVisibilityUtil.ESCALATE_VISIBILITY, ALL_MEMBERS);
  }
}
