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
    KNOWN_TO_FAIL.add("comments/classes.unit:110  remove blank line before beginning of body");
    KNOWN_TO_FAIL.add("comments/classes.unit:39  inline block comment");
    KNOWN_TO_FAIL.add("comments/classes.unit:50  multiple inline block comments");
    KNOWN_TO_FAIL.add("comments/expressions.stmt:10  trailing line comment after non-split");
    KNOWN_TO_FAIL.add("comments/expressions.stmt:17  inside list literal");
    KNOWN_TO_FAIL.add("comments/expressions.stmt:2  trailing line comment after split");
    KNOWN_TO_FAIL.add("comments/expressions.stmt:36  space between block comment and other tokens");
    KNOWN_TO_FAIL.add("comments/expressions.stmt:54  no trailing space after operand preceding comment");
    KNOWN_TO_FAIL.add("comments/expressions.stmt:63  hard line caused by a comment before a nested line");
    KNOWN_TO_FAIL.add("comments/expressions.stmt:80  force named args to split on line comment in positional");
    KNOWN_TO_FAIL.add("comments/functions.unit:102  after '{' in param list");
    KNOWN_TO_FAIL.add("comments/functions.unit:106  before '}' in param list");
    KNOWN_TO_FAIL.add("comments/functions.unit:119  remove blank line before beginning of body");
    KNOWN_TO_FAIL.add("comments/functions.unit:39  inline block comment");
    KNOWN_TO_FAIL.add("comments/functions.unit:43  multiple comments on opening line");
    KNOWN_TO_FAIL.add("comments/functions.unit:50  multiple inline block comments");
    KNOWN_TO_FAIL.add("comments/functions.unit:78  after ',' in param list");
    KNOWN_TO_FAIL.add("comments/functions.unit:82  before '[' in param list");
    KNOWN_TO_FAIL.add("comments/functions.unit:86  after '[' in param list");
    KNOWN_TO_FAIL.add("comments/functions.unit:98  before '{' in param list");
    KNOWN_TO_FAIL.add("comments/lists.stmt:10  line comment on opening line");
    KNOWN_TO_FAIL.add("comments/lists.stmt:25  block comment with trailing newline");
    KNOWN_TO_FAIL.add("comments/lists.stmt:39  inline block comment");
    KNOWN_TO_FAIL.add("comments/lists.stmt:43  multiple comments on opening line");
    KNOWN_TO_FAIL.add("comments/lists.stmt:50  multiple inline block comments");
    KNOWN_TO_FAIL.add("comments/lists.stmt:54  multiline trailing block comment");
    KNOWN_TO_FAIL.add("comments/lists.stmt:62  line comment between items");
    KNOWN_TO_FAIL.add("comments/lists.stmt:70  line comments after last item");
    KNOWN_TO_FAIL.add("comments/lists.stmt:79  line comments after trailing comma");
    KNOWN_TO_FAIL.add("comments/lists.stmt:92  remove blank line before beginning of body");
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
    KNOWN_TO_FAIL.add("comments/maps.stmt:97  remove blank line before beginning of body");
    KNOWN_TO_FAIL.add("comments/mixed.unit:2  block comment");
    KNOWN_TO_FAIL.add("comments/mixed.unit:70  multiline comment inside nested blocks");
    KNOWN_TO_FAIL.add("comments/mixed.unit:95  commented out comments are not mistaken for doc comments");
    KNOWN_TO_FAIL.add("comments/statements.stmt:14  continue with line comment");
    KNOWN_TO_FAIL.add("comments/statements.stmt:32  do with line comment");
    KNOWN_TO_FAIL.add("comments/statements.stmt:47  remove blank line before beginning of block");
    KNOWN_TO_FAIL.add("comments/statements.stmt:6  trailing line comment");
    KNOWN_TO_FAIL.add("comments/top_level.unit:133");
    KNOWN_TO_FAIL.add("comments/top_level.unit:164  before library name");
    KNOWN_TO_FAIL.add("comments/top_level.unit:168  block comment before '.' in library");
    KNOWN_TO_FAIL.add("comments/top_level.unit:172  block comment after '.' in library");
    KNOWN_TO_FAIL.add("comments/top_level.unit:176  line comment before '.' in library");
    KNOWN_TO_FAIL.add("comments/top_level.unit:182  line comment after '.' in library");
    KNOWN_TO_FAIL.add("comments/top_level.unit:188  inline block comment between different kinds of directives");
    KNOWN_TO_FAIL.add("comments/top_level.unit:195  inline block comment between directives");
    KNOWN_TO_FAIL.add("comments/top_level.unit:207  ensure blank line above doc comments");
    KNOWN_TO_FAIL.add("comments/top_level.unit:8");
    KNOWN_TO_FAIL.add("regression/0000/0025.stmt:1");
    KNOWN_TO_FAIL.add("regression/0000/0025.stmt:13  (indent 8)");
    KNOWN_TO_FAIL.add("regression/other/analysis_server.unit:1  (indent 2)");
    KNOWN_TO_FAIL.add("regression/other/analysis_server.unit:11");
    KNOWN_TO_FAIL.add("regression/other/analysis_server.unit:28");
    KNOWN_TO_FAIL.add("regression/other/analysis_server.unit:75");
    KNOWN_TO_FAIL.add("regression/other/analysis_server.unit:99  (indent 6)");
    KNOWN_TO_FAIL.add("regression/other/analysis_server.unit:148  (indent 2)");
    KNOWN_TO_FAIL.add("regression/other/analysis_server.unit:160  (indent 2)");
    KNOWN_TO_FAIL.add("regression/other/dart2js.unit:1  (indent 4) preemption follows constraints");
    KNOWN_TO_FAIL.add("regression/other/pub.stmt:1  (indent 6)");
    KNOWN_TO_FAIL.add("regression/other/pub.stmt:22  (indent 4) was slow");
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
    KNOWN_TO_FAIL.add("selections/selections.unit:54  across lines that get split separately");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:105  if split before first positional, split before first named too");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:112  if split before other positional, split before first named too");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:119  if positional args go one per line, named do too");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:133  avoid splitting before single positional argument");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:138  multiple nested collections");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:150  trailing collections are not indented");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:166  all trailing collections");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:179  non-body non-preceding argument forces all bodies to indent");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:2  many arguments");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:39  arguments, nested");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:53  force all arguments to split if an argument splits");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:71  do split empty argument list if it contains a comment");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:80  move just named to second line even though all fit on second");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:86  split named and keep positional on first");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:93  only named arguments and move to second line");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:98  only named arguments and split");
    KNOWN_TO_FAIL.add("splitting/arrows.stmt:7  newline before fn expression should not force => to split");
    KNOWN_TO_FAIL.add("splitting/assignments.stmt:2  initializer doesn't fit one line, wrap inside, keep name");
    KNOWN_TO_FAIL.add("splitting/assignments.stmt:8  initializer doesn't fit one line, wrap inside, keep name");
    KNOWN_TO_FAIL.add("splitting/classes.unit:106  multiline mixin with multiline interface");
    KNOWN_TO_FAIL.add("splitting/classes.unit:126  force implements to split even if it would fit after with");
    KNOWN_TO_FAIL.add("splitting/classes.unit:59  one interface per line");
    KNOWN_TO_FAIL.add("splitting/classes.unit:77  one mixin per line");
    KNOWN_TO_FAIL.add("splitting/classes.unit:86  multiline mixin with single-line interface");
    KNOWN_TO_FAIL.add("splitting/classes.unit:97  single-line mixin with multiline interface");
    KNOWN_TO_FAIL.add("splitting/constructors.unit:35  try to keep constructor call together");
    KNOWN_TO_FAIL.add("splitting/constructors.unit:41  splits before ':' if the parameter list does not fit on one line");
    KNOWN_TO_FAIL.add("splitting/constructors.unit:51  indent parameters more if body is a wrapped =>");
    KNOWN_TO_FAIL.add("splitting/constructors.unit:61  wrap initializers past the ':'");
    KNOWN_TO_FAIL.add("splitting/constructors.unit:75  split at '=' in initializer");
    KNOWN_TO_FAIL.add("splitting/exports.unit:100  force both keywords to split even if first would fit on first line");
    KNOWN_TO_FAIL.add("splitting/exports.unit:106  force split in list");
    KNOWN_TO_FAIL.add("splitting/exports.unit:15  export moves all shows each to their own line");
    KNOWN_TO_FAIL.add("splitting/exports.unit:36  export moves hides each to their own line");
    KNOWN_TO_FAIL.add("splitting/exports.unit:52  multiline first");
    KNOWN_TO_FAIL.add("splitting/exports.unit:64  multiline second");
    KNOWN_TO_FAIL.add("splitting/exports.unit:76  multiline both");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:100  successive index arguments");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:18  conditions, same operator");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:45  split operator chain around block");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:55  indent previous line farther because later line is nested deeper");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:72  nested parenthesized are indented more");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:79  conditional operands are nested");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:89  index expressions can split after '['");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:94  index arguments nest");
    KNOWN_TO_FAIL.add("splitting/function_arguments.stmt:117  split before all trailing args");
    KNOWN_TO_FAIL.add("splitting/function_arguments.stmt:149  do nest because of nested many-arg fn");
    KNOWN_TO_FAIL.add("splitting/function_arguments.stmt:163  do nest because of nested many-arg method call");
    KNOWN_TO_FAIL.add("splitting/function_arguments.stmt:171  force named args to split on positional function");
    KNOWN_TO_FAIL.add("splitting/function_arguments.stmt:2  args before and after function forces nesting");
    KNOWN_TO_FAIL.add("splitting/function_arguments.stmt:21  trailing functions do not nest");
    KNOWN_TO_FAIL.add("splitting/function_arguments.stmt:39  arg between functions forces nesting");
    KNOWN_TO_FAIL.add("splitting/function_arguments.stmt:58  split before leading args");
    KNOWN_TO_FAIL.add("splitting/function_arguments.stmt:67  split in middle of leading args");
    KNOWN_TO_FAIL.add("splitting/function_arguments.stmt:76  split before all leading args");
    KNOWN_TO_FAIL.add("splitting/imports.unit:105  force both keywords to split even if first would fit on first line");
    KNOWN_TO_FAIL.add("splitting/imports.unit:111  force split in list");
    KNOWN_TO_FAIL.add("splitting/imports.unit:20  import moves all shows each to their own line");
    KNOWN_TO_FAIL.add("splitting/imports.unit:41  import moves hides each to their own line");
    KNOWN_TO_FAIL.add("splitting/imports.unit:57  multiline first");
    KNOWN_TO_FAIL.add("splitting/imports.unit:6  wrap import at as");
    KNOWN_TO_FAIL.add("splitting/imports.unit:69  multiline second");
    KNOWN_TO_FAIL.add("splitting/imports.unit:81  multiline both");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:103  split before all properties if they don't fit on two lines");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:116  unsplit cascade unsplit method");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:120  split cascade unsplit method");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:126  unsplit cascade split method");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:13  don't split before implicit receiver");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:134  split cascade split method");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:145  cascade setters on method chain");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:2  split all chained calls if they don't fit on one line");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:38  allow an inline chain before a hard newline but not after");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:49  allow an inline chain after a hard newline but not before");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:60  nest calls one more than target");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:66  split properties after a method chain");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:75  split properties in a method chain");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:84  do not split leading properties in a chain");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:91  do not split leading properties even if others splits");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:98  split between a pair of properties");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:109  split before all leading args");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:125  unsplit trailing args");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:134  split before trailing args");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:14  nothing but list args does not nest");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:145  split in middle of trailing args");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:155  split before all trailing args");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:181  split before one leading arg");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:186  split before all args including leading");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:2  args before and after list forces nesting");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:215  split before all args including trailing");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:224  named args before and after list forces nesting");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:236  nothing but named list args does not nest");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:256  some named list args does not nest");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:276  nest trailing named if there are non-body named");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:295  leading named arguments");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:303  don't nest because of nested 1-arg fn");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:312  do nest because of nested many-arg fn");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:323  don't nest because of nested 1-arg method call");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:33  trailing lists do not nest");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:332  do nest because of nested many-arg method call");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:48  leading lists do not nest");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:63  arg between lists forces nesting");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:80  unsplit leading args");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:89  split before leading args");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:99  split in middle of leading args");
    KNOWN_TO_FAIL.add("splitting/lists.stmt:108  preserve newlines in lists containing a line comment");
    KNOWN_TO_FAIL.add("splitting/lists.stmt:120  wrap between elements even when newlines are preserved");
    KNOWN_TO_FAIL.add("splitting/lists.stmt:134  ignore line comment after the ']'");
    KNOWN_TO_FAIL.add("splitting/lists.stmt:143  preserves one blank line between elements");
    KNOWN_TO_FAIL.add("splitting/lists.stmt:21  splits outer lists even if they fit");
    KNOWN_TO_FAIL.add("splitting/lists.stmt:34  split indirect outer");
    KNOWN_TO_FAIL.add("splitting/lists.stmt:61  force multi-line because of contained block");
    KNOWN_TO_FAIL.add("splitting/lists.stmt:91  nested lists are forced to split");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:14  split after second clause");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:2  do not split before first clause");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:24  split multiple variable declarations");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:35  split between updaters splits everything");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:43  nest wrapped initializer");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:52  split in for-in loop");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:57  split in while condition");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:8  split after first clause");
    KNOWN_TO_FAIL.add("splitting/map_arguments.stmt:109  split before trailing args");
    KNOWN_TO_FAIL.add("splitting/map_arguments.stmt:126  split before all trailing args");
    KNOWN_TO_FAIL.add("splitting/map_arguments.stmt:150  split before one leading arg");
    KNOWN_TO_FAIL.add("splitting/map_arguments.stmt:155  split before all args including leading");
    KNOWN_TO_FAIL.add("splitting/map_arguments.stmt:184  split before all args including trailing");
    KNOWN_TO_FAIL.add("splitting/map_arguments.stmt:2  args before and after map forces nesting");
    KNOWN_TO_FAIL.add("splitting/map_arguments.stmt:200  do nest because of nested many-arg fn");
    KNOWN_TO_FAIL.add("splitting/map_arguments.stmt:216  do nest because of nested many-arg method call");
    KNOWN_TO_FAIL.add("splitting/map_arguments.stmt:26  trailing maps do not nest");
    KNOWN_TO_FAIL.add("splitting/map_arguments.stmt:50  arg between maps forces nesting");
    KNOWN_TO_FAIL.add("splitting/map_arguments.stmt:71  split before leading args");
    KNOWN_TO_FAIL.add("splitting/map_arguments.stmt:79  split in middle of leading args");
    KNOWN_TO_FAIL.add("splitting/map_arguments.stmt:88  split before all leading args");
    KNOWN_TO_FAIL.add("splitting/maps.stmt:100  wrap between elements even when newlines are preserved");
    KNOWN_TO_FAIL.add("splitting/maps.stmt:116  ignore line comment after the ']'");
    KNOWN_TO_FAIL.add("splitting/maps.stmt:125  preserves one blank line between elements");
    KNOWN_TO_FAIL.add("splitting/maps.stmt:151  ignore newlines between keys and values");
    KNOWN_TO_FAIL.add("splitting/maps.stmt:18  splits outer maps even if they fit");
    KNOWN_TO_FAIL.add("splitting/maps.stmt:29  split indirect outer");
    KNOWN_TO_FAIL.add("splitting/maps.stmt:35  empty literal does not force outer split");
    KNOWN_TO_FAIL.add("splitting/maps.stmt:53  force multi-line because of contained block");
    KNOWN_TO_FAIL.add("splitting/maps.stmt:88  preserve newlines in maps containing a line comment");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:110  list inside method chain");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:127  binary operators in ascending precedence");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:140  binary operators in descending precedence");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:153  mixed multiplicative operators");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:161  mixed additive operators");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:169  mixed shift operators");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:19  nested expression indentation");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:191  choose extra nesting if it leads to better solution");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:29  does not extra indent when multiple levels of nesting happen on one line");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:39  forces extra indent and lines, if later line needs it");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:48  function inside a collection");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:60  function inside an argument list");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:7  prefers to wrap before '.'");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:71  unnested function inside nested expression");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:79  nested function inside nested expression");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:92  wrap before =>");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:98  wrap after =>");
    KNOWN_TO_FAIL.add("splitting/parameters.stmt:2  many parameters");
    KNOWN_TO_FAIL.add("splitting/parameters.stmt:23  parameters fit but ) does not");
    KNOWN_TO_FAIL.add("splitting/parameters.stmt:32  parameters fit but } does not");
    KNOWN_TO_FAIL.add("splitting/parameters.stmt:84  split optional onto one per line if they don't fit on one line");
    KNOWN_TO_FAIL.add("splitting/parameters.unit:11  indent parameters more if body is a wrapped =>");
    KNOWN_TO_FAIL.add("splitting/statements.stmt:11  split in do-while condition");
    KNOWN_TO_FAIL.add("splitting/statements.stmt:16  split in switch value");
    KNOWN_TO_FAIL.add("splitting/statements.stmt:6  wrapped assert");
    KNOWN_TO_FAIL.add("splitting/strings.stmt:50  wrap first line if needed");
    KNOWN_TO_FAIL.add("splitting/type_arguments.stmt:11  split before first if needed");
    KNOWN_TO_FAIL.add("splitting/type_arguments.stmt:16  split in middle if fit in two lines");
    KNOWN_TO_FAIL.add("splitting/type_arguments.stmt:21  split one per line if they don't fit in two lines");
    KNOWN_TO_FAIL.add("splitting/type_arguments.stmt:6  prefer to split between args even when they all fit on next line");
    KNOWN_TO_FAIL.add("splitting/type_parameters.unit:11  split before first if needed");
    KNOWN_TO_FAIL.add("splitting/type_parameters.unit:16  split in middle if fit in two lines");
    KNOWN_TO_FAIL.add("splitting/type_parameters.unit:21  split one per line if they don't fit in two lines");
    KNOWN_TO_FAIL.add("splitting/type_parameters.unit:6  prefer to split between params even when they all fit on next line");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:12  initializer doesn't fit one line, wrap inside, keep name");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:17  initializer fits one line");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:22  initializer doesn't fit one line, cannot be split");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:27  long function call initializer");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:32  long binary expression initializer");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:37  lots of variables with no initializers");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:44  multiple variables stay on one line if they fit");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:48");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:6  initializer doesn't fit one line, wrap inside, keep name");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:62  dartbug.com/16379");
    KNOWN_TO_FAIL.add("whitespace/classes.unit:64  require blank line after non-empty block-bodied members");
    KNOWN_TO_FAIL.add("whitespace/classes.unit:88  no required blank line after empty block-bodied members");
    KNOWN_TO_FAIL.add("whitespace/compilation_unit.unit:104  require blank line after non-empty block-bodied members");
    KNOWN_TO_FAIL.add("whitespace/compilation_unit.unit:125  no required blank line after empty block-bodied members");
    KNOWN_TO_FAIL.add("whitespace/compilation_unit.unit:38  collapse extra newlines between declarations");
    KNOWN_TO_FAIL.add("whitespace/compilation_unit.unit:88  blank line between functions and classes");
    KNOWN_TO_FAIL.add("whitespace/compilation_unit.unit:96  require newline between non-class declarations");
    KNOWN_TO_FAIL.add("whitespace/if.stmt:2  indentation"); // would pass if test were correct
    KNOWN_TO_FAIL.add("whitespace/metadata.unit:68  allow inline annotations before members");

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

  public void testFunction_arguments() throws Exception {
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

  public void testList_arguments() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testLoops() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testMaps2() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testMap_arguments() throws Exception {
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

  public void testType_arguments() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testType_parameters() throws Exception {
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

  public void testType_arguments2() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testType_parameters2() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testWhile() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void test0025() throws Exception {
    // Verify leadingIndent is working.
    runTestInDirectory("regression/0000");
  }

  public void testAnalysis_server() throws Exception {
    runTestInDirectory("regression/other");
  }

  public void testDart2js() throws Exception {
    runTestInDirectory("regression/other");
  }

  public void testPub() throws Exception {
    runTestInDirectory("regression/other");
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
      if (!isCompilationUnit) pageWidth += 2; // Adjust for indent in case test is near margin.

      System.out.println("\nTest: " + dirName + "/" + testFileName + ", Right margin: " + pageWidth);
      final CommonCodeStyleSettings settings = getSettings(DartLanguage.INSTANCE);
      settings.RIGHT_MARGIN = pageWidth;
      settings.KEEP_LINE_BREAKS = false; // TODO Decide whether this should be the default -- risky!
      settings.KEEP_BLANK_LINES_IN_CODE = 1;

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
          if (!isCompilationUnit && !line.isEmpty()) line = "  " + line;
          input += line + "\n";
        }

        if (!isCompilationUnit) input += "}\n";

        String expectedOutput = "";
        if (!isCompilationUnit) expectedOutput += "m() {\n";

        i++;

        while (i < lines.length && !lines[i].startsWith(">>>")) {
          String line = lines[i++];
          if (leadingIndent > 0) line = line.substring(leadingIndent);
          if (!isCompilationUnit && !line.isEmpty()) line = "  " + line;
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
          if (!KNOWN_TO_FAIL.contains(description.replace('"', '\''))) {
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
