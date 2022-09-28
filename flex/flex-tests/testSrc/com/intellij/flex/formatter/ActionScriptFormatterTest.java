package com.intellij.flex.formatter;

import com.intellij.application.options.CodeStyle;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.lang.javascript.JavaScriptFormatterTestBase;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.formatter.ECMA4CodeStyleSettings;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.xml.XmlCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public class ActionScriptFormatterTest extends JavaScriptFormatterTestBase {
  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected String getBasePath() {
    return BASE_PATH;
  }

  private CommonCodeStyleSettings getCommonJSSettings() {
    return CodeStyle.getSettings(getProject()).getCommonSettings(JavaScriptSupportLoader.ECMA_SCRIPT_L4);
  }

  private CommonCodeStyleSettings getEcma4Settings() {
    return CodeStyle.getSettings(getProject()).getCommonSettings(JavaScriptSupportLoader.ECMA_SCRIPT_L4);
  }

  public void testWrapLongLinesInFlex() {
    final CommonCodeStyleSettings settings = getCommonJSSettings();
    boolean wrap = settings.WRAP_LONG_LINES;
    int rMargin = settings.RIGHT_MARGIN;
    int braceForce = settings.IF_BRACE_FORCE;
    settings.WRAP_LONG_LINES = true;
    settings.RIGHT_MARGIN = 30;
    settings.IF_BRACE_FORCE = CommonCodeStyleSettings.FORCE_BRACES_ALWAYS;
    doTestFromFile("as");
    settings.WRAP_LONG_LINES = wrap;
    settings.RIGHT_MARGIN = rMargin;
    settings.IF_BRACE_FORCE = braceForce;
  }

  public void testActionScriptClass() {
    CommonCodeStyleSettings settings = getEcma4Settings();
    settings.METHOD_BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE;
    settings.CLASS_BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE;
    settings.BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE;
    doTest("package foo { class A { function foo() {} } }", """
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

  public void testReformatIf2() {
    doTestFromFile("js2");
  }

  public void testReformatIf2_2() {
    doTestFromFile("js2");
  }

  public void testKeepPropertyMembersClose() {
    doTestFromFile("js2");
  }

  public void test2NewlinesAfterFunction() {
    doTestFromFile("js2");
    doTestFromFile("mxml");
  }

  public void testDocCommentInsideTag() {
    doTestFromFile("mxml");
  }

  public void testTopLevelDocComment() {
    doTestFromFile("mxml");
  }

  public void testCdataDamaged() {
    final CodeStyleSettings styleSettings = CodeStyle.getSettings(getProject());
    final XmlCodeStyleSettings xmlSettings = styleSettings.getCustomSettings(XmlCodeStyleSettings.class);
    int before = styleSettings.getTabSize(XmlFileType.INSTANCE);
    int aroundCDataBefore = xmlSettings.XML_WHITE_SPACE_AROUND_CDATA;

    try {
      styleSettings.getIndentOptions(XmlFileType.INSTANCE).TAB_SIZE = 4;
      xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = XmlCodeStyleSettings.WS_AROUND_CDATA_NEW_LINES;
      myUseReformatText = true;
      doTestFromFile("mxml");
    }
    finally {
      styleSettings.getIndentOptions(XmlFileType.INSTANCE).TAB_SIZE = before;
      xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = aroundCDataBefore;
      myUseReformatText = false;
    }
  }

  public void testECMAScript3() {
    doTest("private namespace yweather = \"http://xml.weather.yahoo.com/ns/rss/1.0\";",
           "private namespace yweather = \"http://xml.weather.yahoo.com/ns/rss/1.0\";",
           "as"
    );
  }

  public void testECMAScript4() {
    doTest("import xxx.*;import yyy.*;class A { var X; function xxx() {} }",
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

  public void testReformatXml() {
    doTestFromFile("js2");
  }

  public void testReformatXml2() {
    doTestFromFile("js2");
  }

  public void testBlankLineBetweenVarAndFunAsdoc() {
    doTestFromFile("js2");
  }

  public void testReformatSwitch() {
    final CommonCodeStyleSettings styleSettings = getEcma4Settings();
    styleSettings.BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE;
    doTestFromFile("js2");
  }


  public void testMxml() {
    doTest("""
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

  public void testMxml2() {
    try {
      myUseReformatText = true;
      doTest("""
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
      myUseReformatText = false;
    }
  }


  public void testBlankLinesAfterPackage() {
    final CommonCodeStyleSettings settings = getEcma4Settings();
    int blankLines = settings.BLANK_LINES_AFTER_PACKAGE;
    settings.BLANK_LINES_AFTER_PACKAGE = 2;
    doTest(
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

  public void testActionScriptRestParameter() {
    final CodeStyleSettings settings = CodeStyle.getSettings(getProject());
    final JSCodeStyleSettings jsSettings = settings.getCustomSettings(ECMA4CodeStyleSettings.class);
    jsSettings.SPACE_AFTER_DOTS_IN_REST_PARAMETER = false;
    doTest(
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
    doTest(
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

  public void testParameterList1() {
    doTestFromFile("js2");
  }

  public void testParameterList2() {
    CommonCodeStyleSettings settings = getEcma4Settings();
    int wrap = settings.METHOD_PARAMETERS_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS;
    doTestFromFile("js2");
    settings.METHOD_PARAMETERS_WRAP = wrap;
  }


  public void testNamespace() {
    doTest(
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

  public void testIfBraceEnforcer() {
    CommonCodeStyleSettings settings = getEcma4Settings();
    settings.IF_BRACE_FORCE = CommonCodeStyleSettings.FORCE_BRACES_ALWAYS;
    doTest(
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

  public void testSpaceBeforeTypeColon() {
    final CodeStyleSettings settings = CodeStyle.getSettings(getProject());
    final JSCodeStyleSettings jsSettings = settings.getCustomSettings(ECMA4CodeStyleSettings.class);
    boolean spaceBeforeTypeColon = jsSettings.SPACE_BEFORE_TYPE_COLON;
    boolean spaceAfterTypeColon = jsSettings.SPACE_AFTER_TYPE_COLON;
    jsSettings.SPACE_BEFORE_TYPE_COLON = true;
    jsSettings.SPACE_AFTER_TYPE_COLON = false;
    doTest(
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

  public void testImportStatement() {
    final CommonCodeStyleSettings settings = getCommonJSSettings();
    int wrapping = settings.METHOD_CALL_CHAIN_WRAP;
    int rightMargin = settings.RIGHT_MARGIN;
    settings.RIGHT_MARGIN = 30;
    settings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS;
    doTestFromFile("mxml");
    settings.RIGHT_MARGIN = rightMargin;
    settings.METHOD_CALL_CHAIN_WRAP = wrapping;
  }

  public void testMxmlForceBraces() {
    CommonCodeStyleSettings settings = getEcma4Settings();
    settings.IF_BRACE_FORCE = CommonCodeStyleSettings.FORCE_BRACES_ALWAYS;
    doTestFromFile("mxml");
  }

  public void testCDATAFormattingOptions1() {
    final CodeStyleSettings settings = CodeStyle.getSettings(getProject());
    final XmlCodeStyleSettings xmlSettings = settings.getCustomSettings(XmlCodeStyleSettings.class);
    int currCDATAWhitespace = xmlSettings.XML_WHITE_SPACE_AROUND_CDATA;
    xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = XmlCodeStyleSettings.WS_AROUND_CDATA_NEW_LINES;
    doTestFromFile("mxml");
    xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = currCDATAWhitespace;
  }

  public void testCDATAFormattingOptions2() {
    final CodeStyleSettings settings = CodeStyle.getSettings(getProject());
    final XmlCodeStyleSettings xmlSettings = settings.getCustomSettings(XmlCodeStyleSettings.class);
    int currCDATAWhitespace = xmlSettings.XML_WHITE_SPACE_AROUND_CDATA;
    xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = XmlCodeStyleSettings.WS_AROUND_CDATA_NONE;
    doTestFromFile("mxml");
    xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = currCDATAWhitespace;
  }

  public void testAlignOperations() {
    final CommonCodeStyleSettings styleSettings = getEcma4Settings();
    styleSettings.ALIGN_MULTILINE_EXTENDS_LIST = true;
    doTest("class _X extends X, \n Y implements Z,\n T {}", """
      class _X extends X,
                       Y implements Z,
                                    T {
      }""", JavaScriptSupportLoader.ECMA_SCRIPT_L4_FILE_EXTENSION2);
  }


  public void testMxml3() {
    try {
      myUseReformatText = true;
      doFileTest("", "mxml");
    }
    finally {
      myUseReformatText = false;
    }
  }

  public void testLBraceInClass() {
    doTest("class A    {\n}", "class A {\n}", "as");
  }

  public void testLBraceInClass2() {
    doTest("class A implements Foo   {\n}", "class A implements Foo {\n}", "as");
  }

  public void testCompoundStatement() {
    doFileTest("");
  }

  public void testSpaceNearType() {
    doFileTest("");

    JSCodeStyleSettings styleSettings = CodeStyle.getSettings(getProject()).getCustomSettings(ECMA4CodeStyleSettings.class);

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

  public void testSpaceAfterAccessModifier() {
    doFileTest("");
  }


  public void testSemicolonAfterVarStatement() {
    doFileTest("");
  }

  public void testECMAScript() {
    doFileTest("");
  }

  private void doFileTest(String ext) {
    doFileTest(ext, "js2");
  }

  private void doFileTest(String ext, String fileExt) {
    doTestFromFile(getTestName(false) + ext, fileExt);
    myFixture.configureByFile(BASE_PATH + getTestName(false) + ext + "." + fileExt);
    CommandProcessor.getInstance().executeCommand(getProject(),() ->
    ApplicationManager.getApplication().runWriteAction(() -> {
      CodeStyleManager.getInstance(getProject()).reformat(getFile());
    }), null, null);

    myFixture.checkResultByFile(BASE_PATH + getTestName(false) + ext + "_after." + fileExt);
  }

  public void testJSON() {
    doTest("var jsonObj:Object = JSON.decode(rawData) as Object\n" +
           "if (jsonObj is Object) return;",
           "var jsonObj:Object = JSON.decode(rawData) as Object\n" +
           "if (jsonObj is Object) return;",
           "as"
    );
  }

  public void testECMAScript2() {
    doFileTest("");
    doFileTest("_2");

    final JSCodeStyleSettings codeSettings =
      CodeStyle.getSettings(getProject()).getCustomSettings(ECMA4CodeStyleSettings.class);
    codeSettings.INDENT_PACKAGE_CHILDREN = JSCodeStyleSettings.INDENT;
    doFileTest("_3");
  }

  public void testECMAScript5() {
    doFileTest("");
  }

  public void testClassHeader() {
    doFileTest("");
  }

  public void testEnforceCodeStyleInActionScript() {
    CodeStyle.getSettings(getProject()).getCustomSettings(ECMA4CodeStyleSettings.class).FORCE_SEMICOLON_STYLE = true;
    doTest("var x: number = 1", "var x:number = 1;", "as");
  }

  public void testIdea124868() {
    CodeStyleSettings settings = getCommonJSSettings().getRootSettings();
    boolean tagsEnabled = settings.FORMATTER_TAGS_ENABLED;
    settings.FORMATTER_TAGS_ENABLED = true;
    try {
      doTestFromFile("mxml");
    }
    finally {
      settings.FORMATTER_TAGS_ENABLED = tagsEnabled;
    }
  }

  public void testImportStatement2() {
    doTestFromFile("js2");
  }
}
