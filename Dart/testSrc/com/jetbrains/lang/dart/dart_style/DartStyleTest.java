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
    KNOWN_TO_FAIL.add("splitting/expressions:2  space-separated adjacent strings are not split");
    KNOWN_TO_FAIL.add("splitting/expressions:18  conditions, same operator");
    KNOWN_TO_FAIL.add("splitting/expressions:26  conditions, different operators");
    KNOWN_TO_FAIL.add("splitting/expressions:33  split conditional because then doesn't fit");
    KNOWN_TO_FAIL.add("splitting/expressions:39  split conditional because else doesn't fit");
    KNOWN_TO_FAIL.add("splitting/expressions:51  split operator chain before block");
    KNOWN_TO_FAIL.add("splitting/expressions:62  split operator chain after block");
    KNOWN_TO_FAIL.add("splitting/expressions:73  indent previous line farther because later line is nested deeper");
    KNOWN_TO_FAIL.add("splitting/expressions:90  nested parenthesized are indented more");
    KNOWN_TO_FAIL.add("splitting/expressions:97  conditional operands are nested");
    KNOWN_TO_FAIL.add("splitting/expressions:107  index expressions can split after \"[\"");
    KNOWN_TO_FAIL.add("splitting/expressions:112  index arguments nest");
    KNOWN_TO_FAIL.add("whitespace/expressions:14  is!");
    KNOWN_TO_FAIL.add("whitespace/expressions:18  generic list literal");
    KNOWN_TO_FAIL.add("whitespace/expressions:22");
    KNOWN_TO_FAIL.add("whitespace/expressions:27  empty map literal (dartbug.com/16382)");
    KNOWN_TO_FAIL.add("whitespace/expressions:35  generic map literal");
    KNOWN_TO_FAIL.add("whitespace/expressions:47  long string literal");
    KNOWN_TO_FAIL.add("whitespace/expressions:64  DO use a space after : in named arguments.");
    KNOWN_TO_FAIL.add("whitespace/expressions:72  sequential \"-\" operators are not joined");
    KNOWN_TO_FAIL.add("whitespace/expressions:76  a \"-\" operator before a negative integer is not joined");
    KNOWN_TO_FAIL.add("whitespace/expressions:80  a \"-\" operator before a negative floating point number is not joined");
    KNOWN_TO_FAIL.add("splitting/exports:6  export keeps shows on one line");
    KNOWN_TO_FAIL.add("splitting/exports:10  export moves all shows to next line");
    KNOWN_TO_FAIL.add("splitting/exports:15  export moves all shows each to their own line");
    KNOWN_TO_FAIL.add("splitting/exports:27  export keeps hides on one line");
    KNOWN_TO_FAIL.add("splitting/exports:31  export moves hides to next line");
    KNOWN_TO_FAIL.add("splitting/exports:36  export moves hides each to their own line");
    KNOWN_TO_FAIL.add("splitting/exports:48  single line both");
    KNOWN_TO_FAIL.add("splitting/exports:52  multiline first");
    KNOWN_TO_FAIL.add("splitting/exports:64  multiline second");
    KNOWN_TO_FAIL.add("splitting/exports:76  multiline both");
    KNOWN_TO_FAIL.add("splitting/exports:94  multiline both");
    KNOWN_TO_FAIL.add("splitting/exports:112  double line both");
    KNOWN_TO_FAIL.add("splitting/exports:118  force both keywords to split even if first would fit on first line");
    KNOWN_TO_FAIL.add("splitting/exports:124  force split in list");
    KNOWN_TO_FAIL.add("whitespace/for:2  DO place spaces around in, and after each ; in a loop.");
    KNOWN_TO_FAIL.add("whitespace/for:14  empty initializer clause");
    KNOWN_TO_FAIL.add("whitespace/for:56");
    KNOWN_TO_FAIL.add("whitespace/for:76  async");
    KNOWN_TO_FAIL.add("splitting/arguments:2  many arguments");
    KNOWN_TO_FAIL.add("splitting/arguments:13  wrap before first argument");
    KNOWN_TO_FAIL.add("splitting/arguments:18  wrap with just one argument");
    KNOWN_TO_FAIL.add("splitting/arguments:23");
    KNOWN_TO_FAIL.add("splitting/arguments:28  force multi-line because of contained block");
    KNOWN_TO_FAIL.add("splitting/arguments:35");
    KNOWN_TO_FAIL.add("splitting/arguments:40  arguments, nested");
    KNOWN_TO_FAIL.add("splitting/arguments:52  hard split inside argument list");
    KNOWN_TO_FAIL.add("splitting/arguments:65  do split empty argument list if it contains a comment");
    KNOWN_TO_FAIL.add("splitting/arguments:74  move just named to second line even though all fit on second");
    KNOWN_TO_FAIL.add("splitting/arguments:80  split named and keep positional on first");
    KNOWN_TO_FAIL.add("splitting/arguments:87  only named arguments and move to second line");
    KNOWN_TO_FAIL.add("splitting/arguments:92  only named arguments and split");
    KNOWN_TO_FAIL.add("splitting/arguments:99  avoid splitting before single positional argument");
    KNOWN_TO_FAIL.add("comments/lists:10  line comment on opening line");
    KNOWN_TO_FAIL.add("comments/lists:25  block comment with trailing newline");
    KNOWN_TO_FAIL.add("comments/lists:39  inline block comment");
    KNOWN_TO_FAIL.add("comments/lists:43  multiple comments on opening line");
    KNOWN_TO_FAIL.add("comments/lists:50  multiple inline block comments");
    KNOWN_TO_FAIL.add("comments/lists:54  multiline trailing block comment");
    KNOWN_TO_FAIL.add("comments/lists:62  line comment between items");
    KNOWN_TO_FAIL.add("comments/lists:70  line comments after last item");
    KNOWN_TO_FAIL.add("comments/lists:79  line comments after trailing comma");
    KNOWN_TO_FAIL.add("comments/lists:88  space on left between block comment and \",\"");
    KNOWN_TO_FAIL.add("splitting/loops:2  do not split before first clause");
    KNOWN_TO_FAIL.add("splitting/loops:8  split after first clause");
    KNOWN_TO_FAIL.add("splitting/loops:14  split after second clause");
    KNOWN_TO_FAIL.add("splitting/loops:20  unsplit multiple variable declarations");
    KNOWN_TO_FAIL.add("splitting/loops:24  split multiple variable declarations");
    KNOWN_TO_FAIL.add("splitting/loops:31  unsplit updaters");
    KNOWN_TO_FAIL.add("splitting/loops:35  split between updaters splits everything");
    KNOWN_TO_FAIL.add("splitting/loops:43  nest wrapped initializer");
    KNOWN_TO_FAIL.add("splitting/loops:52  split in for-in loop");
    KNOWN_TO_FAIL.add("splitting/loops:57  split in while condition");
    KNOWN_TO_FAIL.add("splitting/maps:2  empty map");
    KNOWN_TO_FAIL.add("splitting/maps:10");
    KNOWN_TO_FAIL.add("splitting/maps:18  nested unsplit map");
    KNOWN_TO_FAIL.add("splitting/maps:23  nested split map");
    KNOWN_TO_FAIL.add("splitting/maps:37  force multi-line because of contained block");
    KNOWN_TO_FAIL.add("splitting/maps:47  containing comments");
    KNOWN_TO_FAIL.add("splitting/maps:54  const");
    KNOWN_TO_FAIL.add("splitting/maps:61  dangling comma");
    KNOWN_TO_FAIL.add("splitting/maps:65  dangling comma multiline");
    KNOWN_TO_FAIL.add("comments/mixed:2  block comment");
    KNOWN_TO_FAIL.add("comments/mixed:32  mixed doc and line comments");
    KNOWN_TO_FAIL.add("comments/mixed:48  mixed comments");
    KNOWN_TO_FAIL.add("comments/maps:10  line comment on opening line");
    KNOWN_TO_FAIL.add("comments/maps:25  block comment with trailing newline");
    KNOWN_TO_FAIL.add("comments/maps:39  inline block comment");
    KNOWN_TO_FAIL.add("comments/maps:43  multiple comments on opening line");
    KNOWN_TO_FAIL.add("comments/maps:50  multiple inline block comments");
    KNOWN_TO_FAIL.add("comments/maps:54  multiline trailing block comment");
    KNOWN_TO_FAIL.add("comments/maps:62  line comment between items");
    KNOWN_TO_FAIL.add("comments/maps:70  line comments after last item");
    KNOWN_TO_FAIL.add("comments/maps:79  line comments after trailing comma");
    KNOWN_TO_FAIL.add("comments/maps:88  inside map literal");
    KNOWN_TO_FAIL.add("whitespace/constructors:18  initializing formals");
    KNOWN_TO_FAIL.add("whitespace/constructors:28  constructor initialization list");
    KNOWN_TO_FAIL.add("whitespace/constructors:40  DO format constructor initialization lists with each field on its own line.");
    KNOWN_TO_FAIL.add("whitespace/constructors:52  DO format constructor initialization lists with each field on its own line.");
    KNOWN_TO_FAIL.add("comments/statements:2  inline after \"var\"");
    KNOWN_TO_FAIL.add("comments/statements:6  trailing line comment");
    KNOWN_TO_FAIL.add("comments/statements:10  multiple variable declaration list");
    KNOWN_TO_FAIL.add("comments/statements:14  continue with line comment");
    KNOWN_TO_FAIL.add("comments/statements:32  do with line comment");
    KNOWN_TO_FAIL.add("regression/25:1");
    KNOWN_TO_FAIL.add("regression/25:7  (indent 8)");
    KNOWN_TO_FAIL.add("regression/25:13  (indent 8)");
    KNOWN_TO_FAIL.add("regression/25:19  (indent 2)");
    KNOWN_TO_FAIL.add("whitespace/do:2  empty");
    KNOWN_TO_FAIL.add("whitespace/if:2  indentation");
    KNOWN_TO_FAIL.add("whitespace/if:34  single-expression then body");
    KNOWN_TO_FAIL.add("whitespace/if:44  single-expression else body");
    KNOWN_TO_FAIL.add("whitespace/functions:2  external");
    KNOWN_TO_FAIL.add("whitespace/functions:8  nested functions");
    KNOWN_TO_FAIL.add("whitespace/functions:24  async");
    KNOWN_TO_FAIL.add("whitespace/functions:39");
    KNOWN_TO_FAIL.add("whitespace/functions:45");
    KNOWN_TO_FAIL.add("whitespace/functions:49  empty function bodies are a single line");
    KNOWN_TO_FAIL.add("whitespace/functions:53");
    KNOWN_TO_FAIL.add("whitespace/functions:59  DO use a space after : in named parameters");
    KNOWN_TO_FAIL.add("whitespace/functions:63  DO use a spaces around = in optional positional parameters.");
    KNOWN_TO_FAIL.add("whitespace/functions:67  async*");
    KNOWN_TO_FAIL.add("whitespace/functions:73  sync* functions");
    KNOWN_TO_FAIL.add("whitespace/directives:18  collapse any other newlines");
    KNOWN_TO_FAIL.add("whitespace/directives:34  deferred");
    KNOWN_TO_FAIL.add("whitespace/directives:49  no spaces between library identifiers");
    KNOWN_TO_FAIL.add("comments/expressions:2  trailing line comment after split");
    KNOWN_TO_FAIL.add("comments/expressions:9  trailing line comment after non-split");
    KNOWN_TO_FAIL.add("comments/expressions:16  inside list literal");
    KNOWN_TO_FAIL.add("comments/expressions:23  inside argument list");
    KNOWN_TO_FAIL.add("comments/expressions:27  no space between \"(\" and \")\" and block comment");
    KNOWN_TO_FAIL.add("comments/expressions:31  space on left between block comment and \",\"");
    KNOWN_TO_FAIL.add("comments/expressions:35  space between block comment and other tokens");
    KNOWN_TO_FAIL.add("comments/expressions:39  preserve space before comment in expression");
    KNOWN_TO_FAIL.add("comments/expressions:61  hard line caused by a comment before a nested line");
    KNOWN_TO_FAIL.add("splitting/parameters:2  parameters fit but ) does not");
    KNOWN_TO_FAIL.add("splitting/parameters:11  parameters fit but } does not");
    KNOWN_TO_FAIL.add("splitting/parameters:20  many parameters");
    KNOWN_TO_FAIL.add("splitting/parameters:33  no split after \"(\" in lambda");
    KNOWN_TO_FAIL.add("splitting/parameters:43  keep mandatory and positional on same line");
    KNOWN_TO_FAIL.add("splitting/parameters:47  keep mandatory and named on same line");
    KNOWN_TO_FAIL.add("splitting/parameters:51  move just optional positional to second line even though all fit on second");
    KNOWN_TO_FAIL.add("splitting/parameters:56  move just named to second line even though all fit on second");
    KNOWN_TO_FAIL.add("splitting/parameters:61  avoid splitting in function type parameters");
    KNOWN_TO_FAIL.add("splitting/parameters:66");
    KNOWN_TO_FAIL.add("splitting/parameters:71  allow splitting in function type parameters");
    KNOWN_TO_FAIL.add("splitting/parameters:11  indent parameters more if body is a wrapped =>");
    KNOWN_TO_FAIL.add("splitting/constructors:2  Single initializers can be on one line");
    KNOWN_TO_FAIL.add("splitting/constructors:12  (or not)");
    KNOWN_TO_FAIL.add("splitting/constructors:23  Multiple initializers are one per line");
    KNOWN_TO_FAIL.add("splitting/constructors:35  try to keep constructor call together");
    KNOWN_TO_FAIL.add("splitting/constructors:41  splits before \":\" if the parameter list does not fit on one line");
    KNOWN_TO_FAIL.add("splitting/constructors:51  indent parameters more if body is a wrapped =>");
    KNOWN_TO_FAIL.add("whitespace/metadata:2  force newline before directives");
    KNOWN_TO_FAIL.add("whitespace/metadata:17  force newline before types");
    KNOWN_TO_FAIL.add("whitespace/metadata:32  force newline before variable declarations");
    KNOWN_TO_FAIL.add("whitespace/metadata:56  allow inline annotations before functions");
    KNOWN_TO_FAIL.add("whitespace/metadata:60  allow newline before functions");
    KNOWN_TO_FAIL.add("whitespace/metadata:68  allow inline annotations before members");
    KNOWN_TO_FAIL.add("whitespace/metadata:115  collapse newlines between annotations");
    KNOWN_TO_FAIL.add("whitespace/metadata:132  multiple top-level annotations always get their own line");
    KNOWN_TO_FAIL.add("whitespace/metadata:138  multiple member annotations always get their own line");
    KNOWN_TO_FAIL.add("whitespace/metadata:148  parameter annotations are inline");
    KNOWN_TO_FAIL.add("whitespace/metadata:158  type parameter annotations are inline");
    KNOWN_TO_FAIL.add("whitespace/metadata:176  comment between metadata");
    KNOWN_TO_FAIL.add("whitespace/metadata:193  metadata on parameters");
    KNOWN_TO_FAIL.add("whitespace/metadata:205  metadata on function-typed formal parameter");
    KNOWN_TO_FAIL.add("whitespace/metadata:213  metadata on default formal parameter");
    KNOWN_TO_FAIL.add("comments/classes:39  inline block comment");
    KNOWN_TO_FAIL.add("comments/classes:43  multiple comments on opening line");
    KNOWN_TO_FAIL.add("comments/classes:50  multiple inline block comments");
    KNOWN_TO_FAIL.add("comments/classes:74  block comment");
    KNOWN_TO_FAIL.add("comments/classes:82  block comment");
    KNOWN_TO_FAIL.add("comments/functions:39  inline block comment");
    KNOWN_TO_FAIL.add("comments/functions:43  multiple comments on opening line");
    KNOWN_TO_FAIL.add("comments/functions:50  multiple inline block comments");
    KNOWN_TO_FAIL.add("comments/functions:74  before \",\" in param list");
    KNOWN_TO_FAIL.add("comments/functions:78  after \",\" in param list");
    KNOWN_TO_FAIL.add("comments/functions:82  before \"[\" in param list");
    KNOWN_TO_FAIL.add("comments/functions:86  after \"[\" in param list");
    KNOWN_TO_FAIL.add("comments/functions:90  before \"]\" in param list");
    KNOWN_TO_FAIL.add("comments/functions:94  after \"]\" in param list");
    KNOWN_TO_FAIL.add("comments/functions:98  before \"{\" in param list");
    KNOWN_TO_FAIL.add("comments/functions:102  after \"{\" in param list");
    KNOWN_TO_FAIL.add("comments/functions:106  before \"}\" in param list");
    KNOWN_TO_FAIL.add("comments/functions:110  after \"{\" in param list");
    KNOWN_TO_FAIL.add("comments/functions:114");
    KNOWN_TO_FAIL.add("splitting/members:2  prefers to wrap at => before params");
    KNOWN_TO_FAIL.add("whitespace/methods:2");
    KNOWN_TO_FAIL.add("splitting/variables:6  initializer doesn't fit one line, wrap inside, keep name");
    KNOWN_TO_FAIL.add("splitting/variables:12  initializer doesn't fit one line, wrap inside, keep name");
    KNOWN_TO_FAIL.add("splitting/variables:17  initializer fits one line");
    KNOWN_TO_FAIL.add("splitting/variables:22  initializer doesn't fit one line, cannot be split");
    KNOWN_TO_FAIL.add("splitting/variables:27  long function call initializer");
    KNOWN_TO_FAIL.add("splitting/variables:32  long binary expression initializer");
    KNOWN_TO_FAIL.add("splitting/variables:37  lots of variables with no initializers");
    KNOWN_TO_FAIL.add("splitting/variables:44  multiple variables get their own line if any has an initializer #16849");
    KNOWN_TO_FAIL.add("splitting/variables:49");
    KNOWN_TO_FAIL.add("splitting/variables:58  dartbug.com/16379");
    KNOWN_TO_FAIL.add("whitespace/cascades:2  single cascades on same line");
    KNOWN_TO_FAIL.add("whitespace/cascades:6  long single cascade forces multi-line");
    KNOWN_TO_FAIL.add("whitespace/cascades:12  multiple cascades get the same line when the method names are the same");
    KNOWN_TO_FAIL.add("whitespace/cascades:30  cascades indent contained blocks (and force multi-line)");
    KNOWN_TO_FAIL.add("comments/top_level:8");
    KNOWN_TO_FAIL.add("comments/top_level:17");
    KNOWN_TO_FAIL.add("comments/top_level:21");
    KNOWN_TO_FAIL.add("comments/top_level:25");
    KNOWN_TO_FAIL.add("comments/top_level:50");
    KNOWN_TO_FAIL.add("comments/top_level:63");
    KNOWN_TO_FAIL.add("comments/top_level:80");
    KNOWN_TO_FAIL.add("comments/top_level:87");
    KNOWN_TO_FAIL.add("comments/top_level:96");
    KNOWN_TO_FAIL.add("comments/top_level:105");
    KNOWN_TO_FAIL.add("comments/top_level:114");
    KNOWN_TO_FAIL.add("comments/top_level:123");
    KNOWN_TO_FAIL.add("comments/top_level:133");
    KNOWN_TO_FAIL.add("comments/top_level:143  two lines between library and import");
    KNOWN_TO_FAIL.add("comments/top_level:150  two lines between library and export");
    KNOWN_TO_FAIL.add("comments/top_level:157  two lines between library and part");
    KNOWN_TO_FAIL.add("comments/top_level:164  before library name");
    KNOWN_TO_FAIL.add("comments/top_level:168  block comment before \".\" in library");
    KNOWN_TO_FAIL.add("comments/top_level:172  block comment after \".\" in library");
    KNOWN_TO_FAIL.add("comments/top_level:176  line comment before \".\" in library");
    KNOWN_TO_FAIL.add("comments/top_level:182  line comment after \".\" in library");
    KNOWN_TO_FAIL.add("splitting/assignments:2  initializer doesn't fit one line, wrap inside, keep name");
    KNOWN_TO_FAIL.add("splitting/assignments:8  initializer doesn't fit one line, wrap inside, keep name");
    KNOWN_TO_FAIL.add("splitting/assignments:13  wrapped initializer fits one line");
    KNOWN_TO_FAIL.add("splitting/assignments:18  initializer doesn't fit one line, name too long");
    KNOWN_TO_FAIL.add("splitting/assignments:24  initializer doesn't fit one line, cannot be split");
    KNOWN_TO_FAIL.add("splitting/assignments:29  long function call initializer");
    KNOWN_TO_FAIL.add("splitting/assignments:34  long binary expression initializer");
    KNOWN_TO_FAIL.add("splitting/arrows:2  do not split after \"(\"");
    KNOWN_TO_FAIL.add("splitting/arrows:2");
    KNOWN_TO_FAIL.add("whitespace/blocks:10  allow an extra newline between statements");
    KNOWN_TO_FAIL.add("whitespace/blocks:24  collapse any other newlines");
    KNOWN_TO_FAIL.add("whitespace/blocks:48  dartbug.com/16810");
    KNOWN_TO_FAIL.add("splitting/statements:6  wrapped assert");
    KNOWN_TO_FAIL.add("splitting/statements:11  split in do-while condition");
    KNOWN_TO_FAIL.add("splitting/statements:16  split in switch value");
    KNOWN_TO_FAIL.add("splitting/enums:2  multiple lines");
    KNOWN_TO_FAIL.add("splitting/enums:11  multiple lines trailing comma");
    KNOWN_TO_FAIL.add("whitespace/enums:2  single");
    KNOWN_TO_FAIL.add("whitespace/enums:6  single line");
    KNOWN_TO_FAIL.add("whitespace/enums:10  single line trailing comma");
    KNOWN_TO_FAIL.add("whitespace/enums:14  metadata");
    KNOWN_TO_FAIL.add("splitting/classes:2");
    KNOWN_TO_FAIL.add("splitting/classes:7");
    KNOWN_TO_FAIL.add("splitting/classes:12");
    KNOWN_TO_FAIL.add("splitting/classes:18");
    KNOWN_TO_FAIL.add("splitting/classes:23  class alias");
    KNOWN_TO_FAIL.add("whitespace/classes:2  indentation");
    KNOWN_TO_FAIL.add("whitespace/classes:20  trailing space inside body");
    KNOWN_TO_FAIL.add("whitespace/classes:25  leading space before \"class\"");
    KNOWN_TO_FAIL.add("whitespace/classes:36");
    KNOWN_TO_FAIL.add("whitespace/classes:41");
    KNOWN_TO_FAIL.add("whitespace/classes:48  eats newlines");
    KNOWN_TO_FAIL.add("whitespace/classes:57  native class");
    KNOWN_TO_FAIL.add("splitting/lists:10");
    KNOWN_TO_FAIL.add("splitting/lists:21  nested unsplit list");
    KNOWN_TO_FAIL.add("splitting/lists:26  nested split list");
    KNOWN_TO_FAIL.add("splitting/lists:43  force multi-line because of contained block");
    KNOWN_TO_FAIL.add("splitting/lists:58  dangling comma");
    KNOWN_TO_FAIL.add("splitting/lists:62  dangling comma multiline");
    KNOWN_TO_FAIL.add("splitting/lists:73  nested lists are forced to split");
    KNOWN_TO_FAIL.add("splitting/imports:6  wrap import at as");
    KNOWN_TO_FAIL.add("splitting/imports:11  import keeps shows on one line");
    KNOWN_TO_FAIL.add("splitting/imports:15  import moves all shows to next line");
    KNOWN_TO_FAIL.add("splitting/imports:20  import moves all shows each to their own line");
    KNOWN_TO_FAIL.add("splitting/imports:32  import keeps hides on one line");
    KNOWN_TO_FAIL.add("splitting/imports:36  import moves hides to next line");
    KNOWN_TO_FAIL.add("splitting/imports:41  import moves hides each to their own line");
    KNOWN_TO_FAIL.add("splitting/imports:53  single line both");
    KNOWN_TO_FAIL.add("splitting/imports:57  multiline first");
    KNOWN_TO_FAIL.add("splitting/imports:69  multiline second");
    KNOWN_TO_FAIL.add("splitting/imports:81  multiline both");
    KNOWN_TO_FAIL.add("splitting/imports:99  multiline both");
    KNOWN_TO_FAIL.add("splitting/imports:117  double line both");
    KNOWN_TO_FAIL.add("splitting/imports:123  force both keywords to split even if first would fit on first line");
    KNOWN_TO_FAIL.add("splitting/imports:129  force split in list");
    KNOWN_TO_FAIL.add("splitting/mixed:2  keeps map on one line if possible");
    KNOWN_TO_FAIL.add("splitting/mixed:7  prefers to wrap before \".\"");
    KNOWN_TO_FAIL.add("splitting/mixed:14");
    KNOWN_TO_FAIL.add("splitting/mixed:19  nested expression indentation");
    KNOWN_TO_FAIL.add("splitting/mixed:26  does not extra indent when multiple levels of nesting happen on one line");
    KNOWN_TO_FAIL.add("splitting/mixed:33  forces extra indent and lines, if later line needs it");
    KNOWN_TO_FAIL.add("splitting/mixed:39  function inside a collection");
    KNOWN_TO_FAIL.add("splitting/mixed:51  wrap before =>");
    KNOWN_TO_FAIL.add("splitting/mixed:57  wrap after =>");
    KNOWN_TO_FAIL.add("splitting/mixed:63  wrap at nested binary operator");
    KNOWN_TO_FAIL.add("splitting/mixed:69  list inside separable TODO(rnystrom): Is this what we want?");
    KNOWN_TO_FAIL.add("splitting/mixed:79  binary operators in ascending precedence");
    KNOWN_TO_FAIL.add("splitting/mixed:92  binary operators in descending precedence");
    KNOWN_TO_FAIL.add("splitting/mixed:105  mixed multiplicative operators");
    KNOWN_TO_FAIL.add("splitting/mixed:113  mixed additive operators");
    KNOWN_TO_FAIL.add("splitting/mixed:121  mixed shift operators");
    KNOWN_TO_FAIL.add("splitting/mixed:143  choose extra nesting if it leads to better solution");
    KNOWN_TO_FAIL.add("splitting/invocations:2  split all chained calls if they don't fit on one line");
    KNOWN_TO_FAIL.add("splitting/invocations:13  don't split before implicit receiver");
    KNOWN_TO_FAIL.add("splitting/invocations:20  allows chained calls on one line with multi-line last argument list");
    KNOWN_TO_FAIL.add("splitting/invocations:38  allow an inline chain before a hard newline but not after");
    KNOWN_TO_FAIL.add("splitting/invocations:49  allow an inline chain after a hard newline but not before");
    KNOWN_TO_FAIL.add("splitting/invocations:60  nest calls one more than target");
    KNOWN_TO_FAIL.add("whitespace/script:8  multiple lines between script and library");
    KNOWN_TO_FAIL.add("whitespace/script:24  multiple lines between script and import");
    KNOWN_TO_FAIL.add("whitespace/script:40  multiple lines between script and line comment");
    KNOWN_TO_FAIL.add("whitespace/script:56  multiple lines between script and block comment");
    KNOWN_TO_FAIL.add("whitespace/switch:11  allow an extra newline between statements in a case");
    KNOWN_TO_FAIL.add("whitespace/switch:26  collapse any other newlines in a case");
    KNOWN_TO_FAIL.add("whitespace/switch:60  allow an extra newline between statements in a default");
    KNOWN_TO_FAIL.add("whitespace/switch:75  collapse any other newlines in a default");
    KNOWN_TO_FAIL.add("whitespace/switch:109  allow an extra newline between cases");
    KNOWN_TO_FAIL.add("whitespace/switch:123  collapse any other newlines in a case");
    KNOWN_TO_FAIL.add("selections/selections:22  includes added whitespace");
    KNOWN_TO_FAIL.add("selections/selections:26  inside comment");
    KNOWN_TO_FAIL.add("selections/selections:30  in beginning of multi-line string literal");
    KNOWN_TO_FAIL.add("selections/selections:36  in middle of multi-line string literal");
    KNOWN_TO_FAIL.add("selections/selections:46  in end of multi-line string literal");
    KNOWN_TO_FAIL.add("selections/selections:52  in string interpolation");
    KNOWN_TO_FAIL.add("selections/selections:56  in moved comment");
    KNOWN_TO_FAIL.add("selections/selections:66  after comments");
    KNOWN_TO_FAIL.add("selections/selections:70  between adjacent comments");
    KNOWN_TO_FAIL.add("selections/selections:13  trailing comment");
    KNOWN_TO_FAIL.add("selections/selections:19  in discarded whitespace");
    KNOWN_TO_FAIL.add("selections/selections:23  in zero split whitespace");
    KNOWN_TO_FAIL.add("selections/selections:34  in soft space split whitespace");
    KNOWN_TO_FAIL.add("selections/selections:43  in hard split whitespace");
    KNOWN_TO_FAIL.add("selections/selections:53  across lines that get split separately");
    KNOWN_TO_FAIL.add("selections/selections:69  only whitespace in newline selected");
    KNOWN_TO_FAIL.add("selections/selections:74  only whitespace in double newline selected");
    KNOWN_TO_FAIL.add("splitting/strings:50  wrap first line if needed");
    KNOWN_TO_FAIL.add("whitespace/compilation_unit:20  discard newlines before first class");
    KNOWN_TO_FAIL.add("whitespace/compilation_unit:32  discard newlines before first function");
    KNOWN_TO_FAIL.add("whitespace/compilation_unit:38  collapse extra newlines between declarations");
    KNOWN_TO_FAIL.add("whitespace/compilation_unit:64  require at least a single newline between declarations");

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
    Pattern indentPattern = Pattern.compile("^\\(indent (\\d+)\\)\\s*");

    String testName = getTestName(true);
    if (Character.isLetter(testName.charAt(0)) && Character.isDigit(testName.charAt(testName.length() - 1))) {
      testName = testName.substring(0, testName.length() - 1);
    }

    File dir = new File(new File(getTestDataPath(), getBasePath()), dirName);
    boolean found = false;

    final StringBuilder combinedActualResult = new StringBuilder();
    final StringBuilder combinedExpectedResult = new StringBuilder();

    for (String ext : new String[]{".stmt", ".unit"}) {
      File entry = new File(dir, testName + ext);
      if (!entry.exists()) {
        continue;
      }

      found = true;
      String[] lines = ArrayUtil.toStringArray(FileUtil.loadLines(entry));
      boolean isCompilationUnit = entry.getName().endsWith(".unit");

      // The first line may have a "|" to indicate the page width.
      int pageWidth = 80;
      int i = 0;

      if (lines[0].endsWith("|")) {
        // As it happens, this is always 40 except for some files in 'regression'
        pageWidth = lines[0].indexOf("|");
        i = 1;
      }

      System.out.println("\nTest: " + dirName + "/" + testName + ext + ", Right margin: " + pageWidth);
      final CommonCodeStyleSettings settings = getSettings(DartLanguage.INSTANCE);
      settings.RIGHT_MARGIN = pageWidth;

      while (i < lines.length) {
        String description = (dirName + "/" + testName + ":" + (i + 1) + " " + lines[i++].replaceAll(">>>", "")).trim();

        // Let the test specify a leading indentation. This is handy for
        // regression tests which often come from a chunk of nested code.
        int leadingIndent = 0;
        Matcher matcher = indentPattern.matcher(description);

        if (matcher.matches()) {
          // The leadingIndent is only used by some tests in 'regression'.
          leadingIndent = Integer.parseInt(matcher.group(1));
          description = description.substring(matcher.end());
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
