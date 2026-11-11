package com.intellij.flex.formatter;

import com.intellij.application.options.CodeStyle;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSFormatterFixture;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.formatter.ECMA4CodeStyleSettings;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.xml.XmlCodeStyleSettings;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSFormatterFixture.jsFormatterFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;

@TestApplication
public class ActionScriptFormatterTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSFormatterFixture> formatter = jsFormatterFixture(
    projectFixture,
    new JSFixtureOptions(FlexTestUtils.getTestDataPath(""), "js").withBasePath(JSFormatterFixture.BASE_PATH));

  private CommonCodeStyleSettings getCommonJSSettings() {
    return CodeStyle.getSettings(formatter.get().getProject()).getCommonSettings(FlexSupportLoader.ECMA_SCRIPT_L4);
  }

  private CommonCodeStyleSettings getEcma4Settings() {
    return CodeStyle.getSettings(formatter.get().getProject()).getCommonSettings(FlexSupportLoader.ECMA_SCRIPT_L4);
  }

  @Test
  public void testWrapLongLinesInFlex() {
    final CommonCodeStyleSettings settings = getCommonJSSettings();
    boolean wrap = settings.WRAP_LONG_LINES;
    int rMargin = settings.RIGHT_MARGIN;
    int braceForce = settings.IF_BRACE_FORCE;
    settings.WRAP_LONG_LINES = true;
    settings.RIGHT_MARGIN = 30;
    settings.IF_BRACE_FORCE = CommonCodeStyleSettings.FORCE_BRACES_ALWAYS;
    formatter.get().doTestFromFile("as");
    settings.WRAP_LONG_LINES = wrap;
    settings.RIGHT_MARGIN = rMargin;
    settings.IF_BRACE_FORCE = braceForce;
  }

  @Test
  public void testActionScriptClass() {
    CommonCodeStyleSettings settings = getEcma4Settings();
    settings.METHOD_BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE;
    settings.CLASS_BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE;
    settings.BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE;
    formatter.get().doTest("package foo { class A { function foo() {} } }", """
      package foo
      {
      class A
      {
          function foo()
          {
          }
      }
      }""", "js2");
  }

  @Test
  public void testReformatIf2() {
    formatter.get().doTestFromFile("js2");
  }

  @Test
  public void testReformatIf2_2() {
    formatter.get().doTestFromFile("js2");
  }

  @Test
  public void testKeepPropertyMembersClose() {
    formatter.get().doTestFromFile("js2");
  }

  @Test
  public void test2NewlinesAfterFunction() {
    formatter.get().doTestFromFile("js2");
    formatter.get().doTestFromFile("mxml");
  }

  @Test
  public void testDocCommentInsideTag() {
    formatter.get().doTestFromFile("mxml");
  }

  @Test
  public void testTopLevelDocComment() {
    formatter.get().doTestFromFile("mxml");
  }

  @Test
  public void testCdataDamaged() {
    final CodeStyleSettings styleSettings = CodeStyle.getSettings(formatter.get().getProject());
    final XmlCodeStyleSettings xmlSettings = styleSettings.getCustomSettings(XmlCodeStyleSettings.class);
    int before = styleSettings.getTabSize(XmlFileType.INSTANCE);
    int aroundCDataBefore = xmlSettings.XML_WHITE_SPACE_AROUND_CDATA;

    try {
      styleSettings.getIndentOptions(XmlFileType.INSTANCE).TAB_SIZE = 4;
      xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = XmlCodeStyleSettings.WS_AROUND_CDATA_NEW_LINES;
      formatter.get().setUseReformatText(true);
      formatter.get().doTestFromFile("mxml");
    }
    finally {
      styleSettings.getIndentOptions(XmlFileType.INSTANCE).TAB_SIZE = before;
      xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = aroundCDataBefore;
      formatter.get().setUseReformatText(false);
    }
  }

  @Test
  public void testECMAScript3() {
    formatter.get().doTest("private namespace yweather = \"http://xml.weather.yahoo.com/ns/rss/1.0\";",
           "private namespace yweather = \"http://xml.weather.yahoo.com/ns/rss/1.0\";",
           "as"
    );
  }

  @Test
  public void testECMAScript4() {
    formatter.get().doTest("import xxx.*;import yyy.*;class A { var X; function xxx() {} }",
           """
             import xxx.*;

             import yyy.*;

             class A {
                 var X;

                 function xxx() {
                 }
             }""",
           "as"
    );
  }

  @Test
  public void testReformatXml() {
    formatter.get().doTestFromFile("js2");
  }

  @Test
  public void testReformatXml2() {
    formatter.get().doTestFromFile("js2");
  }

  @Test
  public void testBlankLineBetweenVarAndFunAsdoc() {
    formatter.get().doTestFromFile("js2");
  }

  @Test
  public void testReformatSwitch() {
    final CommonCodeStyleSettings styleSettings = getEcma4Settings();
    styleSettings.BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE;
    formatter.get().doTestFromFile("js2");
  }


  @Test
  public void testMxml() {
    formatter.get().doTest("""
             <?xml version="1.0" ?>
             <mx:Application xmlns:mx="http://www.adobe.com/2006/mxml">
                 <mx:Script>
             <![CDATA[
             ]]>
             </mx:Script>
             </mx:Application>""",
           """
             <?xml version="1.0" ?>
             <mx:Application xmlns:mx="http://www.adobe.com/2006/mxml">
                 <mx:Script>
             <![CDATA[
             ]]>
             </mx:Script>
             </mx:Application>""",
           "mxml"
    );
  }

  @Test
  public void testMxml2() {
    try {
      formatter.get().setUseReformatText(true);
      formatter.get().doTest("""
               <mx:Application xmlns:mx="http://www.adobe.com/2006/mxml">
                   <mx:Script>
                       <![CDATA[
               import mx.rpc.events.AbstractEvent;import mx.rpc.events.AbstractEvent
                       ]]></mx:Script>
               </mx:Application>""", """
               <mx:Application xmlns:mx="http://www.adobe.com/2006/mxml">
                   <mx:Script>
                       <![CDATA[
                       import mx.rpc.events.AbstractEvent;
                       import mx.rpc.events.AbstractEvent
                       ]]></mx:Script>
               </mx:Application>""", "mxml");
    }
    finally {
      formatter.get().setUseReformatText(false);
    }
  }


  @Test
  public void testBlankLinesAfterPackage() {
    final CommonCodeStyleSettings settings = getEcma4Settings();
    int blankLines = settings.BLANK_LINES_AFTER_PACKAGE;
    settings.BLANK_LINES_AFTER_PACKAGE = 2;
    formatter.get().doTest(
      """
        package {
        import com.jetbrains.flex.Demo;
        import com.jetbrains.flex.Sample;
        class Foo {
        }
        }""",
      """
        package {


        import com.jetbrains.flex.Demo;
        import com.jetbrains.flex.Sample;

        class Foo {
        }
        }""",
      ".as");
    settings.BLANK_LINES_AFTER_PACKAGE = blankLines;
  }

  @Test
  public void testActionScriptRestParameter() {
    final CodeStyleSettings settings = CodeStyle.getSettings(formatter.get().getProject());
    final JSCodeStyleSettings jsSettings = settings.getCustomSettings(ECMA4CodeStyleSettings.class);
    jsSettings.SPACE_AFTER_DOTS_IN_REST_PARAMETER = false;
    formatter.get().doTest(
      """
        class Bar {
            public function foo(...    rest):void {
            }
        }""",
      """
        class Bar {
            public function foo(...rest):void {
            }
        }""",
      ".as");
    jsSettings.SPACE_AFTER_DOTS_IN_REST_PARAMETER = true;
    formatter.get().doTest(
      """
        class Bar {
            public function foo(...    rest):void {
            }
        }""",
      """
        class Bar {
            public function foo(... rest):void {
            }
        }""",
      ".as");
  }

  @Test
  public void testParameterList1() {
    formatter.get().doTestFromFile("js2");
  }

  @Test
  public void testParameterList2() {
    CommonCodeStyleSettings settings = getEcma4Settings();
    int wrap = settings.METHOD_PARAMETERS_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS;
    formatter.get().doTestFromFile("js2");
    settings.METHOD_PARAMETERS_WRAP = wrap;
  }


  @Test
  public void testNamespace() {
    formatter.get().doTest(
      """
        package foo {

        public             namespace    MyNs     =             "aaaa";
        }""",

      """
        package foo {

        public namespace MyNs = "aaaa";
        }""",

      ".as");
  }

  @Test
  public void testIfBraceEnforcer() {
    CommonCodeStyleSettings settings = getEcma4Settings();
    settings.IF_BRACE_FORCE = CommonCodeStyleSettings.FORCE_BRACES_ALWAYS;
    formatter.get().doTest(
      """
        package mx.styles {
        public class A {
        private var virtualRendererIndices:Vector.<int>;
        public function A() {
        if (!virtualRendererIndices)
          virtualRendererIndices = new Vector.<int>();
        }
        }
        }""",

      """
        package mx.styles {
        public class A {
            private var virtualRendererIndices:Vector.<int>;

            public function A() {
                if (!virtualRendererIndices) {
                    virtualRendererIndices = new Vector.<int>();
                }
            }
        }
        }""",

      ".as"
    );
  }

  @Test
  public void testSpaceBeforeTypeColon() {
    final CodeStyleSettings settings = CodeStyle.getSettings(formatter.get().getProject());
    final JSCodeStyleSettings jsSettings = settings.getCustomSettings(ECMA4CodeStyleSettings.class);
    boolean spaceBeforeTypeColon = jsSettings.SPACE_BEFORE_TYPE_COLON;
    boolean spaceAfterTypeColon = jsSettings.SPACE_AFTER_TYPE_COLON;
    jsSettings.SPACE_BEFORE_TYPE_COLON = true;
    jsSettings.SPACE_AFTER_TYPE_COLON = false;
    formatter.get().doTest(
      """
        package aaa {
        class XXX {
            private var _field:int;
            function get field():int {
                return _field;
            }

            function set field(val:int):void {
                varName = val;
            }
        }""",
      """
        package aaa {
        class XXX {
            private var _field :int;
            function get field() :int {
                return _field;
            }

            function set field(val :int) :void {
                varName = val;
            }
        }""",
      ".as");
    jsSettings.SPACE_BEFORE_TYPE_COLON = spaceBeforeTypeColon;
    jsSettings.SPACE_AFTER_TYPE_COLON = spaceAfterTypeColon;
  }

  @Test
  public void testImportStatement() {
    final CommonCodeStyleSettings settings = getCommonJSSettings();
    int wrapping = settings.METHOD_CALL_CHAIN_WRAP;
    int rightMargin = settings.RIGHT_MARGIN;
    settings.RIGHT_MARGIN = 30;
    settings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS;
    formatter.get().doTestFromFile("mxml");
    settings.RIGHT_MARGIN = rightMargin;
    settings.METHOD_CALL_CHAIN_WRAP = wrapping;
  }

  @Test
  public void testMxmlForceBraces() {
    CommonCodeStyleSettings settings = getEcma4Settings();
    settings.IF_BRACE_FORCE = CommonCodeStyleSettings.FORCE_BRACES_ALWAYS;
    formatter.get().doTestFromFile("mxml");
  }

  @Test
  public void testCDATAFormattingOptions1() {
    final CodeStyleSettings settings = CodeStyle.getSettings(formatter.get().getProject());
    final XmlCodeStyleSettings xmlSettings = settings.getCustomSettings(XmlCodeStyleSettings.class);
    int currCDATAWhitespace = xmlSettings.XML_WHITE_SPACE_AROUND_CDATA;
    xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = XmlCodeStyleSettings.WS_AROUND_CDATA_NEW_LINES;
    formatter.get().doTestFromFile("mxml");
    xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = currCDATAWhitespace;
  }

  @Test
  public void testCDATAFormattingOptions2() {
    final CodeStyleSettings settings = CodeStyle.getSettings(formatter.get().getProject());
    final XmlCodeStyleSettings xmlSettings = settings.getCustomSettings(XmlCodeStyleSettings.class);
    int currCDATAWhitespace = xmlSettings.XML_WHITE_SPACE_AROUND_CDATA;
    xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = XmlCodeStyleSettings.WS_AROUND_CDATA_NONE;
    formatter.get().doTestFromFile("mxml");
    xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = currCDATAWhitespace;
  }

  @Test
  public void testAlignOperations() {
    final CommonCodeStyleSettings styleSettings = getEcma4Settings();
    styleSettings.ALIGN_MULTILINE_EXTENDS_LIST = true;
    formatter.get().doTest("class _X extends X, \n Y implements Z,\n T {}", """
      class _X extends X,
                       Y implements Z,
                                    T {
      }""", FlexSupportLoader.ECMA_SCRIPT_L4_FILE_EXTENSION2);
  }


  @Test
  public void testMxml3() {
    try {
      formatter.get().setUseReformatText(true);
      doFileTest("", "mxml");
    }
    finally {
      formatter.get().setUseReformatText(false);
    }
  }

  @Test
  public void testLBraceInClass() {
    formatter.get().doTest("class A    {\n}", "class A {\n}", "as");
  }

  @Test
  public void testLBraceInClass2() {
    formatter.get().doTest("class A implements Foo   {\n}", "class A implements Foo {\n}", "as");
  }

  @Test
  public void testCompoundStatement() {
    doFileTest("");
  }

  @Test
  public void testSpaceNearType() {
    doFileTest("");

    JSCodeStyleSettings styleSettings = CodeStyle.getSettings(formatter.get().getProject()).getCustomSettings(ECMA4CodeStyleSettings.class);

    try {
      styleSettings.SPACE_BEFORE_TYPE_COLON = true;
      styleSettings.SPACE_AFTER_TYPE_COLON = true;
      doFileTest("_2");
      styleSettings.SPACE_BEFORE_TYPE_COLON = false;
      styleSettings.SPACE_AFTER_TYPE_COLON = true;
      doFileTest("_3");
    }
    finally {
      styleSettings.SPACE_BEFORE_TYPE_COLON = false;
      styleSettings.SPACE_AFTER_TYPE_COLON = false;
    }
  }

  @Test
  public void testSpaceAfterAccessModifier() {
    doFileTest("");
  }


  @Test
  public void testSemicolonAfterVarStatement() {
    doFileTest("");
  }

  @Test
  public void testECMAScript() {
    doFileTest("");
  }

  private void doFileTest(String ext) {
    doFileTest(ext, "js2");
  }

  private void doFileTest(String ext, String fileExt) {
    JSFormatterFixture js = formatter.get();
    js.doTestFromFile(js.getTestName(false) + ext, fileExt);
    js.getFixture().configureByFile(JSFormatterFixture.BASE_PATH + js.getTestName(false) + ext + "." + fileExt);
    EdtTestUtil.runInEdtAndWait(() -> CommandProcessor.getInstance().executeCommand(js.getProject(),() ->
    ApplicationManager.getApplication().runWriteAction(() -> {
      CodeStyleManager.getInstance(js.getProject()).reformat(js.getFile());
    }), null, null));

    js.getFixture().checkResultByFile(JSFormatterFixture.BASE_PATH + js.getTestName(false) + ext + "_after." + fileExt);
  }

  @Test
  public void testJSON() {
    formatter.get().doTest("var jsonObj:Object = JSON.decode(rawData) as Object\n" +
           "if (jsonObj is Object) return;",
           "var jsonObj:Object = JSON.decode(rawData) as Object\n" +
           "if (jsonObj is Object) return;",
           "as"
    );
  }

  @Test
  public void testECMAScript2() {
    doFileTest("");
    doFileTest("_2");

    final JSCodeStyleSettings codeSettings =
      CodeStyle.getSettings(formatter.get().getProject()).getCustomSettings(ECMA4CodeStyleSettings.class);
    codeSettings.INDENT_PACKAGE_CHILDREN = JSCodeStyleSettings.INDENT;
    doFileTest("_3");
  }

  @Test
  public void testECMAScript5() {
    doFileTest("");
  }

  @Test
  public void testClassHeader() {
    doFileTest("");
  }

  @Test
  public void testEnforceCodeStyleInActionScript() {
    CodeStyle.getSettings(formatter.get().getProject()).getCustomSettings(ECMA4CodeStyleSettings.class).FORCE_SEMICOLON_STYLE = true;
    formatter.get().doTest("var x: number = 1", "var x:number = 1;", "as");
  }

  @Test
  public void testIdea124868() {
    CodeStyleSettings settings = getCommonJSSettings().getRootSettings();
    formatter.get().doTestFromFile("mxml");
  }

  @Test
  public void testImportStatement2() {
    formatter.get().doTestFromFile("js2");
  }
}
