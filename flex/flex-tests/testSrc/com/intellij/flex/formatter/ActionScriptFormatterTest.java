package com.intellij.flex.formatter;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JavaScriptFormatterTestBase;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.formatter.ECMA4CodeStyleSettings;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.xml.XmlCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public class ActionScriptFormatterTest extends JavaScriptFormatterTestBase {
  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  private static CommonCodeStyleSettings getCommonJSSettings() {
    return CodeStyleSettingsManager.getSettings(getProject()).getCommonSettings(JavaScriptSupportLoader.ECMA_SCRIPT_L4);
  }

  private static CommonCodeStyleSettings getEcma4Settings() {
    return CodeStyleSettingsManager.getSettings(getProject()).getCommonSettings(JavaScriptSupportLoader.ECMA_SCRIPT_L4);
  }

  public void testWrapLongLinesInFlex() throws Exception {
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

  public void testActionScriptClass() throws Exception {
    CommonCodeStyleSettings settings = getEcma4Settings();
    settings.METHOD_BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE;
    settings.CLASS_BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE;
    settings.BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE;
    doTest("package foo { class A { function foo() {} } }", "package foo\n" +
                                                            "{\n" +
                                                            "class A\n" +
                                                            "{\n" +
                                                            "    function foo()\n" +
                                                            "    {\n" +
                                                            "    }\n" +
                                                            "}\n" +
                                                            "}", "js2");
  }

  public void testReformatIf2() throws Exception {
    doTestFromFile("js2");
  }

  public void testReformatIf2_2() throws Exception {
    doTestFromFile("js2");
  }

  public void testKeepPropertyMembersClose() throws Exception {
    doTestFromFile("js2");
  }

  public void test2NewlinesAfterFunction() throws Exception {
    doTestFromFile("js2");
    doTestFromFile("mxml");
  }

  public void testDocCommentInsideTag() throws Exception {
    doTestFromFile("mxml");
  }

  public void testTopLevelDocComment() throws Exception {
    doTestFromFile("mxml");
  }

  public void testCdataDamaged() throws Exception {
    final CodeStyleSettings styleSettings = CodeStyleSettingsManager.getSettings(getProject());
    final XmlCodeStyleSettings xmlSettings = styleSettings.getCustomSettings(XmlCodeStyleSettings.class);
    int before = styleSettings.getTabSize(StdFileTypes.XML);
    int aroundCDataBefore = xmlSettings.XML_WHITE_SPACE_AROUND_CDATA;

    try {
      styleSettings.getIndentOptions(StdFileTypes.XML).TAB_SIZE = 4;
      xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = XmlCodeStyleSettings.WS_AROUND_CDATA_NEW_LINES;
      myUseReformatText = true;
      doTestFromFile("mxml");
    }
    finally {
      styleSettings.getIndentOptions(StdFileTypes.XML).TAB_SIZE = before;
      xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = aroundCDataBefore;
      myUseReformatText = false;
    }
  }

  public void testECMAScript3() throws Exception {
    doTest("private namespace yweather = \"http://xml.weather.yahoo.com/ns/rss/1.0\";",
           "private namespace yweather = \"http://xml.weather.yahoo.com/ns/rss/1.0\";",
           "as"
    );
  }

  public void testECMAScript4() throws Exception {
    doTest("import xxx.*;import yyy.*;class A { var X; function xxx() {} }",
           "import xxx.*;\n\nimport yyy.*;\n\nclass A {\n" + "    var X;\n\n" + "    function xxx() {\n" + "    }\n" + "}",
           "as"
    );
  }

  public void testReformatXml() throws Exception {
    doTestFromFile("js2");
  }

  public void testReformatXml2() throws Exception {
    doTestFromFile("js2");
  }

  public void testBlankLineBetweenVarAndFunAsdoc() throws Exception {
    doTestFromFile("js2");
  }

  public void testReformatSwitch() throws Exception {
    final CommonCodeStyleSettings styleSettings = getEcma4Settings();
    styleSettings.BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE;
    doTestFromFile("js2");
  }


  public void testMxml() throws Exception {
    doTest("<?xml version=\"1.0\" ?>\n" +
           "<mx:Application xmlns:mx=\"http://www.adobe.com/2006/mxml\">\n" +
           "    <mx:Script>\n" +
           "<![CDATA[\n" +
           "]]>\n" +
           "</mx:Script>\n" +
           "</mx:Application>",
           "<?xml version=\"1.0\" ?>\n" +
           "<mx:Application xmlns:mx=\"http://www.adobe.com/2006/mxml\">\n" +
           "    <mx:Script>\n" +
           "<![CDATA[\n" +
           "]]>\n" +
           "</mx:Script>\n" +
           "</mx:Application>",
           "mxml"
    );
  }

  public void testMxml2() throws Exception {
    try {
      myUseReformatText = true;
      doTest("<mx:Application xmlns:mx=\"http://www.adobe.com/2006/mxml\">\n" +
             "    <mx:Script>\n" +
             "        <![CDATA[\n" +
             "import mx.rpc.events.AbstractEvent;import mx.rpc.events.AbstractEvent\n" +
             "        ]]></mx:Script>\n" +
             "</mx:Application>", "<mx:Application xmlns:mx=\"http://www.adobe.com/2006/mxml\">\n" +
                                  "    <mx:Script>\n" +
                                  "        <![CDATA[\n" +
                                  "        import mx.rpc.events.AbstractEvent;\n" +
                                  "        import mx.rpc.events.AbstractEvent\n" +
                                  "        ]]></mx:Script>\n" +
                                  "</mx:Application>", "mxml");
    }
    finally {
      myUseReformatText = false;
    }
  }


  public void testBlankLinesAfterPackage() throws Exception {
    final CommonCodeStyleSettings settings = getEcma4Settings();
    int blankLines = settings.BLANK_LINES_AFTER_PACKAGE;
    settings.BLANK_LINES_AFTER_PACKAGE = 2;
    doTest(
      "package {\n" +
      "import com.jetbrains.flex.Demo;\n" +
      "import com.jetbrains.flex.Sample;\n" +
      "class Foo {\n" +
      "}\n" +
      "}",
      "package {\n" +
      "\n" +
      "\n" +
      "import com.jetbrains.flex.Demo;\n" +
      "import com.jetbrains.flex.Sample;\n" +
      "\n" +
      "class Foo {\n" +
      "}\n" +
      "}",
      ".as");
    settings.BLANK_LINES_AFTER_PACKAGE = blankLines;
  }

  public void testActionScriptRestParameter() throws Exception {
    final CodeStyleSettings settings = CodeStyleSettingsManager.getInstance(getProject()).getCurrentSettings();
    final JSCodeStyleSettings jsSettings = settings.getCustomSettings(ECMA4CodeStyleSettings.class);
    jsSettings.SPACE_AFTER_DOTS_IN_REST_PARAMETER = false;
    doTest(
      "class Bar {\n" +
      "    public function foo(...    rest):void {\n" +
      "    }\n" +
      "}",
      "class Bar {\n" +
      "    public function foo(...rest):void {\n" +
      "    }\n" +
      "}",
      ".as");
    jsSettings.SPACE_AFTER_DOTS_IN_REST_PARAMETER = true;
    doTest(
      "class Bar {\n" +
      "    public function foo(...    rest):void {\n" +
      "    }\n" +
      "}",
      "class Bar {\n" +
      "    public function foo(... rest):void {\n" +
      "    }\n" +
      "}",
      ".as");
  }

  public void testParameterList1() throws Exception {
    doTestFromFile("js2");
  }

  public void testParameterList2() throws Exception {
    CommonCodeStyleSettings settings = getEcma4Settings();
    int wrap = settings.METHOD_PARAMETERS_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS;
    doTestFromFile("js2");
    settings.METHOD_PARAMETERS_WRAP = wrap;
  }


  public void testNamespace() throws Exception {
    doTest(
      "package foo {\n" +
      "\n" +
      "public             namespace    MyNs     =             \"aaaa\";\n" +
      "}",

      "package foo {\n" +
      "\n" +
      "public namespace MyNs = \"aaaa\";\n" +
      "}",

      ".as");
  }

  public void testIfBraceEnforcer() throws Exception {
    CommonCodeStyleSettings settings = getEcma4Settings();
    settings.IF_BRACE_FORCE = CommonCodeStyleSettings.FORCE_BRACES_ALWAYS;
    doTest(
      "package mx.styles {\n" +
      "public class A {\n" +
      "private var virtualRendererIndices:Vector.<int>;\n" +
      "public function A() {\n" +
      "if (!virtualRendererIndices)\n" +
      "  virtualRendererIndices = new Vector.<int>();\n" +
      "}\n" +
      "}\n" +
      "}",

      "package mx.styles {\n" +
      "public class A {\n" +
      "    private var virtualRendererIndices:Vector.<int>;\n" +
      "\n" +
      "    public function A() {\n" +
      "        if (!virtualRendererIndices) {\n" +
      "            virtualRendererIndices = new Vector.<int>();\n" +
      "        }\n" +
      "    }\n" +
      "}\n" +
      "}",

      ".as"
    );
  }

  public void testSpaceBeforeTypeColon() throws Exception {
    final CodeStyleSettings settings = CodeStyleSettingsManager.getInstance(getProject()).getCurrentSettings();
    final JSCodeStyleSettings jsSettings = settings.getCustomSettings(ECMA4CodeStyleSettings.class);
    boolean spaceBeforeTypeColon = jsSettings.SPACE_BEFORE_TYPE_COLON;
    boolean spaceAfterTypeColon = jsSettings.SPACE_AFTER_TYPE_COLON;
    jsSettings.SPACE_BEFORE_TYPE_COLON = true;
    jsSettings.SPACE_AFTER_TYPE_COLON = false;
    doTest(
      "package aaa {\n" +
      "class XXX {\n" +
      "    private var _field:int;\n" +
      "    function get field():int {\n" +
      "        return _field;\n" +
      "    }\n" +
      "\n" +
      "    function set field(val:int):void {\n" +
      "        varName = val;\n" +
      "    }\n" +
      "}",
      "package aaa {\n" +
      "class XXX {\n" +
      "    private var _field :int;\n" +
      "    function get field() :int {\n" +
      "        return _field;\n" +
      "    }\n" +
      "\n" +
      "    function set field(val :int) :void {\n" +
      "        varName = val;\n" +
      "    }\n" +
      "}",
      ".as");
    jsSettings.SPACE_BEFORE_TYPE_COLON = spaceBeforeTypeColon;
    jsSettings.SPACE_AFTER_TYPE_COLON = spaceAfterTypeColon;
  }

  public void testImportStatement() throws Exception {
    final CommonCodeStyleSettings settings = getCommonJSSettings();
    int wrapping = settings.METHOD_CALL_CHAIN_WRAP;
    int rightMargin = settings.RIGHT_MARGIN;
    settings.RIGHT_MARGIN = 30;
    settings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS;
    doTestFromFile("mxml");
    settings.RIGHT_MARGIN = rightMargin;
    settings.METHOD_CALL_CHAIN_WRAP = wrapping;
  }

  public void testMxmlForceBraces() throws Exception {
    CommonCodeStyleSettings settings = getEcma4Settings();
    settings.IF_BRACE_FORCE = CommonCodeStyleSettings.FORCE_BRACES_ALWAYS;
    doTestFromFile("mxml");
  }

  public void testCDATAFormattingOptions1() throws Exception {
    final CodeStyleSettings settings = CodeStyleSettingsManager.getInstance(getProject()).getCurrentSettings();
    final XmlCodeStyleSettings xmlSettings = settings.getCustomSettings(XmlCodeStyleSettings.class);
    int currCDATAWhitespace = xmlSettings.XML_WHITE_SPACE_AROUND_CDATA;
    xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = XmlCodeStyleSettings.WS_AROUND_CDATA_NEW_LINES;
    doTestFromFile("mxml");
    xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = currCDATAWhitespace;
  }

  public void testCDATAFormattingOptions2() throws Exception {
    final CodeStyleSettings settings = CodeStyleSettingsManager.getInstance(getProject()).getCurrentSettings();
    final XmlCodeStyleSettings xmlSettings = settings.getCustomSettings(XmlCodeStyleSettings.class);
    int currCDATAWhitespace = xmlSettings.XML_WHITE_SPACE_AROUND_CDATA;
    xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = XmlCodeStyleSettings.WS_AROUND_CDATA_NONE;
    doTestFromFile("mxml");
    xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = currCDATAWhitespace;
  }

  public void testAlignOperations() throws Exception {
    final CommonCodeStyleSettings styleSettings = getEcma4Settings();
    styleSettings.ALIGN_MULTILINE_EXTENDS_LIST = true;
    doTest("class _X extends X, \n Y implements Z,\n T {}", "class _X extends X,\n" +
                                                            "                 Y implements Z,\n" +
                                                            "                              T {\n" +
                                                            "}", JavaScriptSupportLoader.ECMA_SCRIPT_L4_FILE_EXTENSION2);
  }


  public void testMxml3() throws Exception {
    try {
      myUseReformatText = true;
      doFileTest("", "mxml");
    }
    finally {
      myUseReformatText = false;
    }
  }

  public void testLBraceInClass() throws Exception {
    doTest("class A    {\n}", "class A {\n}", "as");
  }

  public void testLBraceInClass2() throws Exception {
    doTest("class A implements Foo   {\n}", "class A implements Foo {\n}", "as");
  }

  public void testCompoundStatement() throws Exception {
    doFileTest("");
  }

  public void testSpaceNearType() throws Exception {
    doFileTest("");

    JSCodeStyleSettings styleSettings = CodeStyleSettingsManager.getSettings(getProject()).getCustomSettings(ECMA4CodeStyleSettings.class);

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

  public void testSpaceAfterAccessModifier() throws Exception {
    doFileTest("");
  }


  public void testSemicolonAfterVarStatement() throws Exception {
    doFileTest("");
  }

  public void testECMAScript() throws Exception {
    doFileTest("");
  }

  private void doFileTest(String ext) throws Exception {
    doFileTest(ext, "js2");
  }

  private void doFileTest(String ext, String fileExt) throws Exception {
    doTestFromFile(getTestName(false) + ext, fileExt);
    configureByFile(BASE_PATH + getTestName(false) + ext + "." + fileExt);
    ApplicationManager.getApplication().runWriteAction(() -> {
      CodeStyleManager.getInstance(getProject()).reformat(getFile());
    });

    checkResultByFile(BASE_PATH + getTestName(false) + ext + "_after." + fileExt);
  }

  public void testJSON() throws Exception {
    doTest("var jsonObj:Object = JSON.decode(rawData) as Object\n" +
           "if (jsonObj is Object) return;",
           "var jsonObj:Object = JSON.decode(rawData) as Object\n" +
           "if (jsonObj is Object) return;",
           "as"
    );
  }

  public void testECMAScript2() throws Exception {
    doFileTest("");
    doFileTest("_2");

    final JSCodeStyleSettings codeSettings =
      CodeStyleSettingsManager.getSettings(getProject()).getCustomSettings(ECMA4CodeStyleSettings.class);
    codeSettings.INDENT_PACKAGE_CHILDREN = JSCodeStyleSettings.INDENT;
    doFileTest("_3");
  }

  public void testECMAScript5() throws Exception {
    doFileTest("");
  }

  public void testClassHeader() throws Exception {
    doFileTest("");
  }

  public void testEnforceCodeStyleInActionScript() {
    CodeStyleSettingsManager.getSettings(getProject()).getCustomSettings(ECMA4CodeStyleSettings.class).FORCE_SEMICOLON_STYLE = true;
    doTest("var x: number = 1", "var x:number = 1;", "as");
  }

  public void testIdea124868() throws Exception {
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

  public void testImportStatement2() throws Exception {
    doTestFromFile("js2");
  }
}
