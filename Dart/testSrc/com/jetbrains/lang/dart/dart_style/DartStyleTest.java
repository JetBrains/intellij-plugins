package com.jetbrains.lang.dart.dart_style;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.FormatterTestCase;
import com.intellij.util.ArrayUtil;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.util.DartTestUtils;
import gnu.trove.THashSet;
import junit.framework.ComparisonFailure;

import java.io.File;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DartStyleTest extends FormatterTestCase {

  protected String getFileExtension() {
    return DartFileType.DEFAULT_EXTENSION;
  }

  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH;
  }

  protected String getBasePath() {
    return "dart_style";
  }

  private static final Set<String> KNOWN_TO_FAIL = new THashSet<String>();

  static {
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:18  conditions, same operator");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:33  split conditional because then doesn't fit");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:39  split conditional because else doesn't fit");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:51  split operator chain before block");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:62  split operator chain after block");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:73  indent previous line farther because later line is nested deeper");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:90  nested parenthesized are indented more");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:97  conditional operands are nested");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:107  index expressions can split after \"[\"");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:112  index arguments nest");
    KNOWN_TO_FAIL.add("splitting/exports.unit:15  export moves all shows each to their own line");
    KNOWN_TO_FAIL.add("splitting/exports.unit:36  export moves hides each to their own line");
    KNOWN_TO_FAIL.add("splitting/exports.unit:52  multiline first");
    KNOWN_TO_FAIL.add("splitting/exports.unit:64  multiline second");
    KNOWN_TO_FAIL.add("splitting/exports.unit:76  multiline both");
    KNOWN_TO_FAIL.add("splitting/exports.unit:94  multiline both");
    KNOWN_TO_FAIL.add("splitting/exports.unit:118  force both keywords to split even if first would fit on first line");
    KNOWN_TO_FAIL.add("splitting/exports.unit:124  force split in list");
    KNOWN_TO_FAIL.add("whitespace/for.stmt:2  DO place spaces around in, and after each ; in a loop.");
    KNOWN_TO_FAIL.add("whitespace/for.stmt:56");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:2  many arguments");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:28  force multi-line because of contained block");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:35");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:40  arguments, nested");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:52  hard split inside argument list");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:65  do split empty argument list if it contains a comment");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:74  move just named to second line even though all fit on second");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:80  split named and keep positional on first");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:87  only named arguments and move to second line");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:92  only named arguments and split");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:99  avoid splitting before single positional argument");
    KNOWN_TO_FAIL.add("comments/lists.stmt:10  line comment on opening line");
    KNOWN_TO_FAIL.add("comments/lists.stmt:25  block comment with trailing newline");
    KNOWN_TO_FAIL.add("comments/lists.stmt:39  inline block comment");
    KNOWN_TO_FAIL.add("comments/lists.stmt:43  multiple comments on opening line");
    KNOWN_TO_FAIL.add("comments/lists.stmt:50  multiple inline block comments");
    KNOWN_TO_FAIL.add("comments/lists.stmt:54  multiline trailing block comment");
    KNOWN_TO_FAIL.add("comments/lists.stmt:62  line comment between items");
    KNOWN_TO_FAIL.add("comments/lists.stmt:70  line comments after last item");
    KNOWN_TO_FAIL.add("comments/lists.stmt:79  line comments after trailing comma");
    KNOWN_TO_FAIL.add("comments/lists.stmt:88  space on left between block comment and \",\"");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:2  do not split before first clause");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:8  split after first clause");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:14  split after second clause");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:24  split multiple variable declarations");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:35  split between updaters splits everything");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:43  nest wrapped initializer");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:52  split in for-in loop");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:57  split in while condition");
    KNOWN_TO_FAIL.add("splitting/maps.stmt:10");
    KNOWN_TO_FAIL.add("splitting/maps.stmt:18  nested unsplit map");
    KNOWN_TO_FAIL.add("splitting/maps.stmt:23  nested split map");
    KNOWN_TO_FAIL.add("splitting/maps.stmt:37  force multi-line because of contained block");
    KNOWN_TO_FAIL.add("splitting/maps.stmt:47  containing comments");
    KNOWN_TO_FAIL.add("splitting/maps.stmt:54  const");
    KNOWN_TO_FAIL.add("splitting/maps.stmt:61  dangling comma");
    KNOWN_TO_FAIL.add("splitting/maps.stmt:65  dangling comma multiline");
    KNOWN_TO_FAIL.add("comments/mixed.unit:2  block comment");
    KNOWN_TO_FAIL.add("comments/mixed.unit:32  mixed doc and line comments");
    KNOWN_TO_FAIL.add("comments/mixed.unit:48  mixed comments");
    KNOWN_TO_FAIL.add("comments/maps.stmt:10  line comment on opening line");
    KNOWN_TO_FAIL.add("comments/maps.stmt:25  block comment with trailing newline");
    KNOWN_TO_FAIL.add("comments/maps.stmt:39  inline block comment");
    KNOWN_TO_FAIL.add("comments/maps.stmt:43  multiple comments on opening line");
    KNOWN_TO_FAIL.add("comments/maps.stmt:50  multiple inline block comments");
    KNOWN_TO_FAIL.add("comments/maps.stmt:54  multiline trailing block comment");
    KNOWN_TO_FAIL.add("comments/maps.stmt:62  line comment between items");
    KNOWN_TO_FAIL.add("comments/maps.stmt:70  line comments after last item");
    KNOWN_TO_FAIL.add("comments/maps.stmt:79  line comments after trailing comma");
    KNOWN_TO_FAIL.add("comments/maps.stmt:88  inside map literal");
    KNOWN_TO_FAIL.add("whitespace/constructors.unit:18  initializing formals");
    KNOWN_TO_FAIL.add("whitespace/constructors.unit:28  constructor initialization list");
    KNOWN_TO_FAIL.add("whitespace/constructors.unit:40  DO format constructor initialization lists with each field on its own line.");
    KNOWN_TO_FAIL.add("whitespace/constructors.unit:52  DO format constructor initialization lists with each field on its own line.");
    KNOWN_TO_FAIL.add("comments/statements.stmt:2  inline after \"var\"");
    KNOWN_TO_FAIL.add("comments/statements.stmt:6  trailing line comment");
    KNOWN_TO_FAIL.add("comments/statements.stmt:10  multiple variable declaration list");
    KNOWN_TO_FAIL.add("comments/statements.stmt:14  continue with line comment");
    KNOWN_TO_FAIL.add("comments/statements.stmt:32  do with line comment");
    KNOWN_TO_FAIL.add("regression/25.stmt:1");
    KNOWN_TO_FAIL.add("regression/25.stmt:13  (indent 8)");
    KNOWN_TO_FAIL.add("whitespace/do.stmt:2  empty");
    KNOWN_TO_FAIL.add("whitespace/if.stmt:2  indentation");
    KNOWN_TO_FAIL.add("whitespace/if.stmt:34  single-expression then body");
    KNOWN_TO_FAIL.add("whitespace/if.stmt:44  single-expression else body");
    KNOWN_TO_FAIL.add("whitespace/functions.unit:24  async");
    KNOWN_TO_FAIL.add("whitespace/functions.unit:39");
    KNOWN_TO_FAIL.add("whitespace/functions.unit:45");
    KNOWN_TO_FAIL.add("whitespace/functions.unit:53");
    KNOWN_TO_FAIL.add("whitespace/functions.unit:59  DO use a space after : in named parameters");
    KNOWN_TO_FAIL.add("whitespace/functions.unit:63  DO use a spaces around = in optional positional parameters.");
    KNOWN_TO_FAIL.add("whitespace/functions.unit:67  async*");
    KNOWN_TO_FAIL.add("whitespace/functions.unit:73  sync* functions");
    KNOWN_TO_FAIL.add("whitespace/directives.unit:18  collapse any other newlines");
    KNOWN_TO_FAIL.add("whitespace/directives.unit:49  no spaces between library identifiers");
    KNOWN_TO_FAIL.add("comments/expressions.stmt:2  trailing line comment after split");
    KNOWN_TO_FAIL.add("comments/expressions.stmt:9  trailing line comment after non-split");
    KNOWN_TO_FAIL.add("comments/expressions.stmt:16  inside list literal");
    KNOWN_TO_FAIL.add("comments/expressions.stmt:23  inside argument list");
    KNOWN_TO_FAIL.add("comments/expressions.stmt:31  space on left between block comment and \",\"");
    KNOWN_TO_FAIL.add("comments/expressions.stmt:35  space between block comment and other tokens");
    KNOWN_TO_FAIL.add("comments/expressions.stmt:39  preserve space before comment in expression");
    KNOWN_TO_FAIL.add("comments/expressions.stmt:61  hard line caused by a comment before a nested line");
    KNOWN_TO_FAIL.add("splitting/parameters.stmt:2  parameters fit but ) does not");
    KNOWN_TO_FAIL.add("splitting/parameters.stmt:11  parameters fit but } does not");
    KNOWN_TO_FAIL.add("splitting/parameters.stmt:20  many parameters");
    KNOWN_TO_FAIL.add("splitting/parameters.stmt:33  no split after \"(\" in lambda");
    KNOWN_TO_FAIL.add("splitting/parameters.stmt:51  move just optional positional to second line even though all fit on second");
    KNOWN_TO_FAIL.add("splitting/parameters.stmt:56  move just named to second line even though all fit on second");
    KNOWN_TO_FAIL.add("splitting/parameters.stmt:61  avoid splitting in function type parameters");
    KNOWN_TO_FAIL.add("splitting/parameters.stmt:66");
    KNOWN_TO_FAIL.add("splitting/parameters.stmt:71  allow splitting in function type parameters");
    KNOWN_TO_FAIL.add("splitting/parameters.unit:11  indent parameters more if body is a wrapped =>");
    KNOWN_TO_FAIL.add("splitting/constructors.unit:2  Single initializers can be on one line");
    KNOWN_TO_FAIL.add("splitting/constructors.unit:12  (or not)");
    KNOWN_TO_FAIL.add("splitting/constructors.unit:23  Multiple initializers are one per line");
    KNOWN_TO_FAIL.add("splitting/constructors.unit:35  try to keep constructor call together");
    KNOWN_TO_FAIL.add("splitting/constructors.unit:41  splits before \":\" if the parameter list does not fit on one line");
    KNOWN_TO_FAIL.add("splitting/constructors.unit:51  indent parameters more if body is a wrapped =>");
    KNOWN_TO_FAIL.add("whitespace/metadata.unit:2  force newline before directives");
    KNOWN_TO_FAIL.add("whitespace/metadata.unit:17  force newline before types");
    KNOWN_TO_FAIL.add("whitespace/metadata.unit:32  force newline before variable declarations");
    KNOWN_TO_FAIL.add("whitespace/metadata.unit:68  allow inline annotations before members");
    KNOWN_TO_FAIL.add("whitespace/metadata.unit:132  multiple top-level annotations always get their own line");
    KNOWN_TO_FAIL.add("whitespace/metadata.unit:138  multiple member annotations always get their own line");
    KNOWN_TO_FAIL.add("whitespace/metadata.unit:148  parameter annotations are inline");
    KNOWN_TO_FAIL.add("whitespace/metadata.unit:158  type parameter annotations are inline");
    KNOWN_TO_FAIL.add("whitespace/metadata.unit:205  metadata on function-typed formal parameter");
    KNOWN_TO_FAIL.add("whitespace/metadata.unit:213  metadata on default formal parameter");
    KNOWN_TO_FAIL.add("comments/classes.unit:39  inline block comment");
    KNOWN_TO_FAIL.add("comments/classes.unit:43  multiple comments on opening line");
    KNOWN_TO_FAIL.add("comments/classes.unit:50  multiple inline block comments");
    KNOWN_TO_FAIL.add("comments/classes.unit:74  block comment");
    KNOWN_TO_FAIL.add("comments/classes.unit:82  block comment");
    KNOWN_TO_FAIL.add("comments/functions.unit:39  inline block comment");
    KNOWN_TO_FAIL.add("comments/functions.unit:43  multiple comments on opening line");
    KNOWN_TO_FAIL.add("comments/functions.unit:50  multiple inline block comments");
    KNOWN_TO_FAIL.add("comments/functions.unit:74  before \",\" in param list");
    KNOWN_TO_FAIL.add("comments/functions.unit:78  after \",\" in param list");
    KNOWN_TO_FAIL.add("comments/functions.unit:82  before \"[\" in param list");
    KNOWN_TO_FAIL.add("comments/functions.unit:86  after \"[\" in param list");
    KNOWN_TO_FAIL.add("comments/functions.unit:90  before \"]\" in param list");
    KNOWN_TO_FAIL.add("comments/functions.unit:94  after \"]\" in param list");
    KNOWN_TO_FAIL.add("comments/functions.unit:98  before \"{\" in param list");
    KNOWN_TO_FAIL.add("comments/functions.unit:102  after \"{\" in param list");
    KNOWN_TO_FAIL.add("comments/functions.unit:106  before \"}\" in param list");
    KNOWN_TO_FAIL.add("comments/functions.unit:110  after \"{\" in param list");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:12  initializer doesn't fit one line, wrap inside, keep name");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:17  initializer fits one line");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:22  initializer doesn't fit one line, cannot be split");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:27  long function call initializer");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:32  long binary expression initializer");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:37  lots of variables with no initializers");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:44  multiple variables get their own line if any has an initializer #16849");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:49");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:58  dartbug.com/16379");
    KNOWN_TO_FAIL.add("whitespace/cascades.stmt:6  long single cascade forces multi-line");
    KNOWN_TO_FAIL.add("whitespace/cascades.stmt:30  cascades indent contained blocks (and force multi-line)");
    KNOWN_TO_FAIL.add("comments/top_level.unit:8");
    KNOWN_TO_FAIL.add("comments/top_level.unit:17");
    KNOWN_TO_FAIL.add("comments/top_level.unit:21");
    KNOWN_TO_FAIL.add("comments/top_level.unit:80");
    KNOWN_TO_FAIL.add("comments/top_level.unit:87");
    KNOWN_TO_FAIL.add("comments/top_level.unit:96");
    KNOWN_TO_FAIL.add("comments/top_level.unit:105");
    KNOWN_TO_FAIL.add("comments/top_level.unit:114");
    KNOWN_TO_FAIL.add("comments/top_level.unit:143  two lines between library and import");
    KNOWN_TO_FAIL.add("comments/top_level.unit:150  two lines between library and export");
    KNOWN_TO_FAIL.add("comments/top_level.unit:157  two lines between library and part");
    KNOWN_TO_FAIL.add("comments/top_level.unit:164  before library name");
    KNOWN_TO_FAIL.add("comments/top_level.unit:168  block comment before \".\" in library");
    KNOWN_TO_FAIL.add("comments/top_level.unit:172  block comment after \".\" in library");
    KNOWN_TO_FAIL.add("comments/top_level.unit:176  line comment before \".\" in library");
    KNOWN_TO_FAIL.add("comments/top_level.unit:182  line comment after \".\" in library");
    KNOWN_TO_FAIL.add("splitting/assignments.stmt:8  initializer doesn't fit one line, wrap inside, keep name");
    KNOWN_TO_FAIL.add("splitting/assignments.stmt:13  wrapped initializer fits one line");
    KNOWN_TO_FAIL.add("splitting/assignments.stmt:18  initializer doesn't fit one line, name too long");
    KNOWN_TO_FAIL.add("splitting/assignments.stmt:24  initializer doesn't fit one line, cannot be split");
    KNOWN_TO_FAIL.add("splitting/assignments.stmt:29  long function call initializer");
    KNOWN_TO_FAIL.add("splitting/assignments.stmt:34  long binary expression initializer");
    KNOWN_TO_FAIL.add("whitespace/blocks.stmt:10  allow an extra newline between statements");
    KNOWN_TO_FAIL.add("whitespace/blocks.stmt:24  collapse any other newlines");
    KNOWN_TO_FAIL.add("splitting/statements.stmt:6  wrapped assert");
    KNOWN_TO_FAIL.add("splitting/statements.stmt:11  split in do-while condition");
    KNOWN_TO_FAIL.add("splitting/statements.stmt:16  split in switch value");
    KNOWN_TO_FAIL.add("splitting/enums.unit:2  multiple lines");
    KNOWN_TO_FAIL.add("splitting/enums.unit:11  multiple lines trailing comma");
    KNOWN_TO_FAIL.add("whitespace/enums.unit:2  single");
    KNOWN_TO_FAIL.add("whitespace/enums.unit:6  single line");
    KNOWN_TO_FAIL.add("whitespace/enums.unit:10  single line trailing comma");
    KNOWN_TO_FAIL.add("whitespace/enums.unit:14  metadata");
    KNOWN_TO_FAIL.add("splitting/classes.unit:2");
    KNOWN_TO_FAIL.add("splitting/classes.unit:7");
    KNOWN_TO_FAIL.add("splitting/classes.unit:12");
    KNOWN_TO_FAIL.add("splitting/classes.unit:18");
    KNOWN_TO_FAIL.add("splitting/classes.unit:23  class alias");
    KNOWN_TO_FAIL.add("whitespace/classes.unit:2  indentation");
    KNOWN_TO_FAIL.add("whitespace/classes.unit:41");
    KNOWN_TO_FAIL.add("whitespace/classes.unit:48  eats newlines");
    KNOWN_TO_FAIL.add("whitespace/classes.unit:57  native class");
    KNOWN_TO_FAIL.add("splitting/lists.stmt:10");
    KNOWN_TO_FAIL.add("splitting/lists.stmt:21  nested unsplit list");
    KNOWN_TO_FAIL.add("splitting/lists.stmt:26  nested split list");
    KNOWN_TO_FAIL.add("splitting/lists.stmt:43  force multi-line because of contained block");
    KNOWN_TO_FAIL.add("splitting/lists.stmt:58  dangling comma");
    KNOWN_TO_FAIL.add("splitting/lists.stmt:62  dangling comma multiline");
    KNOWN_TO_FAIL.add("splitting/lists.stmt:73  nested lists are forced to split");
    KNOWN_TO_FAIL.add("splitting/imports.unit:6  wrap import at as");
    KNOWN_TO_FAIL.add("splitting/imports.unit:20  import moves all shows each to their own line");
    KNOWN_TO_FAIL.add("splitting/imports.unit:41  import moves hides each to their own line");
    KNOWN_TO_FAIL.add("splitting/imports.unit:57  multiline first");
    KNOWN_TO_FAIL.add("splitting/imports.unit:69  multiline second");
    KNOWN_TO_FAIL.add("splitting/imports.unit:81  multiline both");
    KNOWN_TO_FAIL.add("splitting/imports.unit:99  multiline both");
    KNOWN_TO_FAIL.add("splitting/imports.unit:123  force both keywords to split even if first would fit on first line");
    KNOWN_TO_FAIL.add("splitting/imports.unit:129  force split in list");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:7  prefers to wrap before \".\"");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:19  nested expression indentation");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:26  does not extra indent when multiple levels of nesting happen on one line");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:33  forces extra indent and lines, if later line needs it");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:39  function inside a collection");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:51  wrap before =>");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:57  wrap after =>");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:69  list inside separable TODO(rnystrom): Is this what we want?");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:79  binary operators in ascending precedence");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:92  binary operators in descending precedence");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:105  mixed multiplicative operators");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:113  mixed additive operators");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:121  mixed shift operators");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:143  choose extra nesting if it leads to better solution");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:2  split all chained calls if they don't fit on one line");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:13  don't split before implicit receiver");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:38  allow an inline chain before a hard newline but not after");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:49  allow an inline chain after a hard newline but not before");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:60  nest calls one more than target");
    KNOWN_TO_FAIL.add("whitespace/script.unit:8  multiple lines between script and library");
    KNOWN_TO_FAIL.add("whitespace/script.unit:24  multiple lines between script and import");
    KNOWN_TO_FAIL.add("whitespace/script.unit:40  multiple lines between script and line comment");
    KNOWN_TO_FAIL.add("whitespace/script.unit:56  multiple lines between script and block comment");
    KNOWN_TO_FAIL.add("whitespace/switch.stmt:11  allow an extra newline between statements in a case");
    KNOWN_TO_FAIL.add("whitespace/switch.stmt:26  collapse any other newlines in a case");
    KNOWN_TO_FAIL.add("whitespace/switch.stmt:60  allow an extra newline between statements in a default");
    KNOWN_TO_FAIL.add("whitespace/switch.stmt:75  collapse any other newlines in a default");
    KNOWN_TO_FAIL.add("whitespace/switch.stmt:109  allow an extra newline between cases");
    KNOWN_TO_FAIL.add("whitespace/switch.stmt:123  collapse any other newlines in a case");
    KNOWN_TO_FAIL.add("selections/selections.stmt:22  includes added whitespace");
    KNOWN_TO_FAIL.add("selections/selections.stmt:26  inside comment");
    KNOWN_TO_FAIL.add("selections/selections.stmt:30  in beginning of multi-line string literal");
    KNOWN_TO_FAIL.add("selections/selections.stmt:36  in middle of multi-line string literal");
    KNOWN_TO_FAIL.add("selections/selections.stmt:46  in end of multi-line string literal");
    KNOWN_TO_FAIL.add("selections/selections.stmt:52  in string interpolation");
    KNOWN_TO_FAIL.add("selections/selections.stmt:56  in moved comment");
    KNOWN_TO_FAIL.add("selections/selections.stmt:66  after comments");
    KNOWN_TO_FAIL.add("selections/selections.stmt:70  between adjacent comments");
    KNOWN_TO_FAIL.add("selections/selections.unit:13  trailing comment");
    KNOWN_TO_FAIL.add("selections/selections.unit:23  in zero split whitespace");
    KNOWN_TO_FAIL.add("selections/selections.unit:34  in soft space split whitespace");
    KNOWN_TO_FAIL.add("selections/selections.unit:43  in hard split whitespace");
    KNOWN_TO_FAIL.add("selections/selections.unit:53  across lines that get split separately");
    KNOWN_TO_FAIL.add("selections/selections.unit:69  only whitespace in newline selected");
    KNOWN_TO_FAIL.add("selections/selections.unit:74  only whitespace in double newline selected");
    KNOWN_TO_FAIL.add("splitting/strings.stmt:50  wrap first line if needed");
    KNOWN_TO_FAIL.add("whitespace/compilation_unit.unit:38  collapse extra newlines between declarations");
    KNOWN_TO_FAIL.add("whitespace/compilation_unit.unit:64  require at least a single newline between declarations");

    //KNOWN_TO_FAIL.clear();
  }

  public void testClasses() throws Exception {
    runTestInDirectory("comments");
  }

  public void testEnums() throws Exception {
    runTestInDirectory("comments");
  }

  public void testExpressions() throws Exception {
    runTestInDirectory("comments");
  }

  public void testFunctions() throws Exception {
    runTestInDirectory("comments");
  }

  public void testLists() throws Exception {
    runTestInDirectory("comments");
  }

  public void testMaps() throws Exception {
    runTestInDirectory("comments");
  }

  public void testMixed() throws Exception {
    runTestInDirectory("comments");
  }

  public void testStatements() throws Exception {
    runTestInDirectory("comments");
  }

  public void testTop_level() throws Exception {
    runTestInDirectory("comments");
  }

  public void testSelections() throws Exception {
    runTestInDirectory("selections");
  }

  public void testArguments() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testArrows() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testAssignments() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testClasses2() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testConstructors2() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testEnums2() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testExports() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testExpressions2() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testImports() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testInvocations() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testLists2() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testLoops() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testMaps2() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testMembers() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testMixed2() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testParameters() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testStatements2() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testStrings() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testVariables() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testBlocks() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testCascades() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testClasses3() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testCompilation_unit() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testConstructors() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testDirectives() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testDo() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testEnums3() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testExpressions3() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testFor() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testFunctions3() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testIf() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testMetadata() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testMethods() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testScript() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testSwitch() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testTry() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testWhile() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void test25() throws Exception {
    // Verify leadingIndent is working.
    runTestInDirectory("regression");
  }

  // TODO Add more of the tests in 'regression'. Currently, they just clutter the results.

  /**
   * Run a test defined in "*.unit" or "*.stmt" file inside directory <code>dirName</code>.
   */
  private void runTestInDirectory(String dirName) throws Exception {
    Pattern indentPattern = Pattern.compile("^.*\\s\\(indent (\\d+)\\)\\s*");

    String testName = getTestName(true);
    if (Character.isLetter(testName.charAt(0)) && Character.isDigit(testName.charAt(testName.length() - 1))) {
      testName = testName.substring(0, testName.length() - 1);
    }

    File dir = new File(new File(getTestDataPath(), getBasePath()), dirName);
    boolean found = false;

    final StringBuilder combinedActualResult = new StringBuilder();
    final StringBuilder combinedExpectedResult = new StringBuilder();

    for (String ext : new String[]{".stmt", ".unit"}) {
      String testFileName = testName + ext;
      File entry = new File(dir, testFileName);
      if (!entry.exists()) {
        continue;
      }

      found = true;
      String[] lines = ArrayUtil.toStringArray(FileUtil.loadLines(entry, "UTF-8"));
      boolean isCompilationUnit = entry.getName().endsWith(".unit");

      // The first line may have a "|" to indicate the page width.
      int pageWidth = 80;
      int i = 0;

      if (lines[0].endsWith("|")) {
        // As it happens, this is always 40 except for some files in 'regression'
        pageWidth = lines[0].indexOf("|");
        i = 1;
      }

      System.out.println("\nTest: " + dirName + "/" + testFileName + ", Right margin: " + pageWidth);
      final CommonCodeStyleSettings settings = getSettings(DartLanguage.INSTANCE);
      settings.RIGHT_MARGIN = pageWidth;
      settings.KEEP_LINE_BREAKS = false; // TODO Decide whether this should be the default -- risky!

      while (i < lines.length) {
        String description = (dirName + "/" + testFileName + ":" + (i + 1) + " " + lines[i++].replaceAll(">>>", "")).trim();

        // Let the test specify a leading indentation. This is handy for
        // regression tests which often come from a chunk of nested code.
        int leadingIndent = 0;
        Matcher matcher = indentPattern.matcher(description);

        if (matcher.matches()) {
          // The leadingIndent is only used by some tests in 'regression'.
          leadingIndent = Integer.parseInt(matcher.group(1));
          settings.RIGHT_MARGIN = pageWidth - leadingIndent;
        }

        String input = "";
        // If the input isn't a top-level form, wrap everything in a function.
        // The formatter fails horribly otherwise.
        if (!isCompilationUnit) input += "m() {\n";

        while (!lines[i].startsWith("<<<")) {
          String line = lines[i++];
          if (leadingIndent > 0) line = line.substring(leadingIndent);
          if (!isCompilationUnit) line = "  " + line;
          input += line + "\n";
        }

        if (!isCompilationUnit) input += "}\n";

        String expectedOutput = "";
        if (!isCompilationUnit) expectedOutput += "m() {\n";

        i++;

        while (i < lines.length && !lines[i].startsWith(">>>")) {
          String line = lines[i++];
          if (leadingIndent > 0) line = line.substring(leadingIndent);
          if (!isCompilationUnit) line = "  " + line;
          expectedOutput += line + "\n";
        }

        if (!isCompilationUnit) expectedOutput += "}\n";

        SourceCode inputCode = extractSelection(input, isCompilationUnit);
        SourceCode expected = extractSelection(expectedOutput, isCompilationUnit);

        myTextRange = new TextRange(inputCode.selectionStart, inputCode.selectionEnd());

        try {
          doTextTest(inputCode.text, expected.text);
          if (KNOWN_TO_FAIL.contains(description)) {
            fail("The test passed, but was expected to fail: " + description);
          }
          System.out.println("TEST PASSED: " + (description.isEmpty() ? "(unnamed)" : description));
        }
        catch (ComparisonFailure failure) {
          if (!KNOWN_TO_FAIL.contains(description)) {
            combinedExpectedResult.append("TEST: ").append(description).append("\n").append(failure.getExpected()).append("\n");
            combinedActualResult.append("TEST: ").append(description).append("\n").append(failure.getActual()).append("\n");
          }
        }
      }
    }

    if (!found) {
      fail("No test data for " + testName);
    }

    assertEquals(combinedExpectedResult.toString(), combinedActualResult.toString());
  }

  /*
   * Given a source string that contains ‹ and › to indicate a selection, returns
   * a <code>SourceCode</code> with the text (with the selection markers removed)
   * and the correct selection range.
   */
  private static SourceCode extractSelection(String source, boolean isCompilationUnit) {
    int start = source.indexOf("‹");
    source = source.replaceAll("‹", "");

    int end = source.indexOf("›");
    source = source.replaceAll("›", "");

    return new SourceCode(source, isCompilationUnit, start == -1 ? 0 : start, end == -1 ? source.length() : end - start);
  }

  private static class SourceCode {
    String text;
    boolean isCompilationUnit;
    int selectionStart, selectionLength;

    SourceCode(String content, boolean isCU, int start, int len) {
      this.text = content;
      this.isCompilationUnit = isCU;
      this.selectionStart = start;
      this.selectionLength = len;
    }

    int selectionEnd() {
      return selectionStart + selectionLength;
    }
  }
}
