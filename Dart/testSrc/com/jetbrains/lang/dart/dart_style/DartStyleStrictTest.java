// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.dart_style;

import java.util.HashSet;
import java.util.Set;

/**
 * Run the dart_style test suite.
 */
public class DartStyleStrictTest extends DartStyleTest {

  /**
   * The set of tests that are known to fail only in strict mode.
   */
  private final Set<String> KNOWN_TO_FAIL_STRICT = new HashSet<>();

  {
    KNOWN_TO_FAIL_STRICT.add("splitting/expressions.stmt:13  adjacent string lines all split together;");
    KNOWN_TO_FAIL_STRICT.add("splitting/expressions.stmt:32  conditions, same operator");
    KNOWN_TO_FAIL_STRICT.add("splitting/expressions.stmt:40  conditions, different operators");
    KNOWN_TO_FAIL_STRICT.add("splitting/expressions.stmt:100  nested parenthesized are indented more");
    KNOWN_TO_FAIL_STRICT.add("splitting/expressions.stmt:144  null coalescing operator");
    KNOWN_TO_FAIL_STRICT.add("whitespace/expressions.stmt:30");
    KNOWN_TO_FAIL_STRICT.add("whitespace/expressions.stmt:118  trailing comma in single argument list");
    KNOWN_TO_FAIL_STRICT.add("whitespace/expressions.stmt:124  trailing comma in argument list");
    KNOWN_TO_FAIL_STRICT.add("whitespace/expressions.stmt:131  trailing comma in named argument list");
    KNOWN_TO_FAIL_STRICT.add("splitting/exports.unit:100  force both keywords to split even if first would fit on first line");
    KNOWN_TO_FAIL_STRICT.add("whitespace/for.stmt:56");
    KNOWN_TO_FAIL_STRICT.add("regression/other/pub.stmt:1  (indent 6)");
    KNOWN_TO_FAIL_STRICT.add("splitting/arguments.stmt:39  arguments, nested");
    KNOWN_TO_FAIL_STRICT.add("splitting/arguments.stmt:53  force all arguments to split if an argument splits");
    KNOWN_TO_FAIL_STRICT.add("splitting/arguments.stmt:67  do force named single-argument list to split if argument splits");
    KNOWN_TO_FAIL_STRICT.add("splitting/arguments.stmt:86  move just named to second line even though all fit on second");
    KNOWN_TO_FAIL_STRICT.add("splitting/arguments.stmt:92  split named and keep positional on first");
    KNOWN_TO_FAIL_STRICT.add("splitting/arguments.stmt:104  only named arguments and split");
    KNOWN_TO_FAIL_STRICT.add("splitting/arguments.stmt:111  if split before first positional, split before first named too");
    KNOWN_TO_FAIL_STRICT.add("splitting/arguments.stmt:118  if split before other positional, split before first named too");
    KNOWN_TO_FAIL_STRICT.add("splitting/arguments.stmt:139  avoid splitting before single positional argument");
    KNOWN_TO_FAIL_STRICT.add("splitting/arguments.stmt:144  multiple nested collections");
    KNOWN_TO_FAIL_STRICT.add("splitting/arguments.stmt:185  non-collection non-preceding argument forces all collections to indent");
    KNOWN_TO_FAIL_STRICT.add("splitting/arguments.stmt:204  trailing comma");
    KNOWN_TO_FAIL_STRICT.add("splitting/arguments.stmt:212  trailing comma in named argument list");
    KNOWN_TO_FAIL_STRICT.add("regression/other/analysis_server.unit:133  (indent 2)");
    KNOWN_TO_FAIL_STRICT.add("regression/other/analysis_server.unit:148  (indent 2)");
    KNOWN_TO_FAIL_STRICT.add("regression/other/analysis_server.unit:190  (indent 2)");
    KNOWN_TO_FAIL_STRICT.add("regression/other/analysis_server.unit:200  (indent 2)");
    KNOWN_TO_FAIL_STRICT.add("regression/other/analysis_server.unit:217  (indent 2)");
    KNOWN_TO_FAIL_STRICT.add("regression/other/angular.unit:1  (indent 4)");
    KNOWN_TO_FAIL_STRICT.add("comments/lists.stmt:10  line comment on opening line");
    KNOWN_TO_FAIL_STRICT.add("comments/lists.stmt:25  block comment with trailing newline");
    KNOWN_TO_FAIL_STRICT.add("comments/lists.stmt:43  multiple comments on opening line");
    KNOWN_TO_FAIL_STRICT.add("comments/lists.stmt:54  multiline trailing block comment");
    KNOWN_TO_FAIL_STRICT.add("comments/lists.stmt:62  line comment between items");
    KNOWN_TO_FAIL_STRICT.add("comments/lists.stmt:70  line comments after last item");
    KNOWN_TO_FAIL_STRICT.add("comments/lists.stmt:79  line comments after trailing comma");
    KNOWN_TO_FAIL_STRICT.add("comments/lists.stmt:92  remove blank line before beginning of body");
    KNOWN_TO_FAIL_STRICT.add("splitting/loops.stmt:109  multi-line while without curlies");
    KNOWN_TO_FAIL_STRICT.add("splitting/maps.stmt:18  splits outer maps even if they fit");
    KNOWN_TO_FAIL_STRICT.add("splitting/maps.stmt:29  split indirect outer");
    KNOWN_TO_FAIL_STRICT.add("splitting/maps.stmt:39  nested split map");
    KNOWN_TO_FAIL_STRICT.add("splitting/maps.stmt:53  force multi-line because of contained block");
    KNOWN_TO_FAIL_STRICT.add("splitting/maps.stmt:77  trailing comma forces split");
    KNOWN_TO_FAIL_STRICT.add("splitting/maps.stmt:102  wrap between elements even when newlines are preserved");
    KNOWN_TO_FAIL_STRICT.add("splitting/maps.stmt:118  ignore line comment after the ']'");
    KNOWN_TO_FAIL_STRICT.add("splitting/maps.stmt:127  preserves one blank line between elements");
    KNOWN_TO_FAIL_STRICT.add("splitting/maps.stmt:153  ignore newlines between keys and values");
    KNOWN_TO_FAIL_STRICT.add("comments/mixed.unit:32  mixed doc and line comments");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0000.stmt:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0000.stmt:9  (indent 4)");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0006.stmt:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0019.stmt:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0019.stmt:8  (indent 4)");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0021.stmt:1  (indent 2)");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0022.stmt:15  (indent 2)");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0027.stmt:1  (indent 6)");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0028.unit:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0033.stmt:1  (indent 4)");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0033.stmt:7  (indent 4)");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0037.stmt:19");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0040.stmt:1  (indent 4)");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0040.stmt:10  (indent 6)");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0041.stmt:1  (indent 8)");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0044.stmt:10");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0054.stmt:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0056.stmt:1  (indent 8)");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0060.stmt:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0061.unit:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0070.stmt:1  (indent 2)");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0072.unit:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0077.stmt:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0000/0083.unit:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0100/0102.stmt:1  (indent 6)");
    KNOWN_TO_FAIL_STRICT.add("regression/0100/0108.unit:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0100/0119.stmt:1  (indent 4)");
    KNOWN_TO_FAIL_STRICT.add("regression/0100/0121.stmt:1  (indent 6)");
    KNOWN_TO_FAIL_STRICT.add("regression/0100/0139.unit:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0100/0142.stmt:1  (indent 4)");
    KNOWN_TO_FAIL_STRICT.add("regression/0100/0151.unit:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0100/0158.unit:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0100/0161.stmt:1  (indent 4)");
    KNOWN_TO_FAIL_STRICT.add("regression/0100/0177.stmt:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0200/0201.stmt:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0200/0203.stmt:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0200/0205.stmt:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0200/0205.stmt:8  (indent 6)");
    KNOWN_TO_FAIL_STRICT.add("regression/0200/0206.stmt:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0200/0211.unit:1  (indent 2)");
    KNOWN_TO_FAIL_STRICT.add("regression/0200/0211.unit:32  (indent 4)");
    KNOWN_TO_FAIL_STRICT.add("regression/0200/0211.unit:49  (indent 2)");
    KNOWN_TO_FAIL_STRICT.add("regression/0200/0211.unit:64  (indent 2)");
    KNOWN_TO_FAIL_STRICT.add("regression/0200/0212.stmt:1  (indent 4)");
    KNOWN_TO_FAIL_STRICT.add("regression/0200/0217.stmt:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0200/0218.stmt:1  (indent 2)");
    KNOWN_TO_FAIL_STRICT.add("regression/0200/0222.stmt:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0200/0224.stmt:47  (indent 22)");
    KNOWN_TO_FAIL_STRICT.add("regression/0200/0237.unit:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0200/0243.stmt:1  (indent 4)");
    KNOWN_TO_FAIL_STRICT.add("regression/0200/0243.stmt:14  (indent 10)");
    KNOWN_TO_FAIL_STRICT.add("regression/0300/0366.stmt:1  (indent 4)");
    KNOWN_TO_FAIL_STRICT.add("regression/0300/0367.stmt:14  (indent 4)");
    KNOWN_TO_FAIL_STRICT.add("regression/0300/0388.unit:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0300/0394.stmt:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0300/0399.unit:1  (indent 2)");
    KNOWN_TO_FAIL_STRICT.add("regression/0400/0437.unit:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0400/0448.stmt:15  (indent 4)");
    KNOWN_TO_FAIL_STRICT.add("regression/0400/0454.unit:1  original bug report actually did fit in 80");
    KNOWN_TO_FAIL_STRICT.add("regression/0400/0461.stmt:1  (indent 6)");
    KNOWN_TO_FAIL_STRICT.add("regression/0400/0463.unit:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0400/0465.stmt:1  (indent 8)");
    KNOWN_TO_FAIL_STRICT.add("regression/0400/0478.stmt:1  (indent 10)");
    KNOWN_TO_FAIL_STRICT.add("regression/0400/0478.stmt:28  (indent 8)");
    KNOWN_TO_FAIL_STRICT.add("regression/0400/0488.stmt:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0500/0506.unit:15  (indent 4)");
    KNOWN_TO_FAIL_STRICT.add("regression/0500/0513.unit:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0500/0513.unit:9");
    KNOWN_TO_FAIL_STRICT.add("regression/0500/0514.unit:1");
    KNOWN_TO_FAIL_STRICT.add("regression/0500/0519.stmt:1  (indent 4)");
    KNOWN_TO_FAIL_STRICT.add("comments/maps.stmt:10  line comment on opening line");
    KNOWN_TO_FAIL_STRICT.add("comments/maps.stmt:25  block comment with trailing newline");
    KNOWN_TO_FAIL_STRICT.add("comments/maps.stmt:43  multiple comments on opening line");
    KNOWN_TO_FAIL_STRICT.add("comments/maps.stmt:54  multiline trailing block comment");
    KNOWN_TO_FAIL_STRICT.add("comments/maps.stmt:62  line comment between items");
    KNOWN_TO_FAIL_STRICT.add("comments/maps.stmt:70  line comments after last item");
    KNOWN_TO_FAIL_STRICT.add("comments/maps.stmt:79  line comments after trailing comma");
    KNOWN_TO_FAIL_STRICT.add("comments/maps.stmt:88  inside map literal");
    KNOWN_TO_FAIL_STRICT.add("comments/maps.stmt:97  remove blank line before beginning of body");
    KNOWN_TO_FAIL_STRICT.add("regression/other/misc.unit:1");
    KNOWN_TO_FAIL_STRICT.add("whitespace/if.stmt:67  long if without curlies");
    KNOWN_TO_FAIL_STRICT.add("whitespace/functions.unit:2  external");
    KNOWN_TO_FAIL_STRICT.add("whitespace/functions.unit:24  async");
    KNOWN_TO_FAIL_STRICT.add("whitespace/functions.unit:53");
    KNOWN_TO_FAIL_STRICT.add("whitespace/functions.unit:96  trailing comma in all optional parameter list");
    KNOWN_TO_FAIL_STRICT.add("whitespace/functions.unit:105  trailing comma in all named parameter list");
    KNOWN_TO_FAIL_STRICT.add("whitespace/directives.unit:34  deferred");
    KNOWN_TO_FAIL_STRICT.add("whitespace/directives.unit:53  configuration");
    KNOWN_TO_FAIL_STRICT.add("whitespace/directives.unit:57  configuration");
    KNOWN_TO_FAIL_STRICT.add("comments/expressions.stmt:2  trailing line comment after split");
    KNOWN_TO_FAIL_STRICT.add("comments/expressions.stmt:17  inside list literal");
    KNOWN_TO_FAIL_STRICT.add("comments/expressions.stmt:36  space between block comment and other tokens");
    KNOWN_TO_FAIL_STRICT.add("comments/expressions.stmt:54  no trailing space after operand preceding comment");
    KNOWN_TO_FAIL_STRICT.add("comments/expressions.stmt:80  force named args to split on line comment in positional");
    KNOWN_TO_FAIL_STRICT.add("splitting/list_arguments.stmt:2  args before and after list forces nesting");
    KNOWN_TO_FAIL_STRICT.add("splitting/list_arguments.stmt:14  nothing but list args does not nest");
    KNOWN_TO_FAIL_STRICT.add("splitting/list_arguments.stmt:48  leading lists do not nest");
    KNOWN_TO_FAIL_STRICT.add("splitting/list_arguments.stmt:63  arg between lists forces nesting");
    KNOWN_TO_FAIL_STRICT.add("splitting/list_arguments.stmt:181  split before one leading arg");
    KNOWN_TO_FAIL_STRICT.add("splitting/list_arguments.stmt:215  split before all args including trailing");
    KNOWN_TO_FAIL_STRICT.add("splitting/list_arguments.stmt:224  named args before and after list forces nesting");
    KNOWN_TO_FAIL_STRICT.add("splitting/list_arguments.stmt:236  nothing but named list args does not nest");
    KNOWN_TO_FAIL_STRICT.add("splitting/list_arguments.stmt:256  some named list args does not nest");
    KNOWN_TO_FAIL_STRICT.add("splitting/list_arguments.stmt:276  allow leading non-collection to not split");
    KNOWN_TO_FAIL_STRICT.add("splitting/list_arguments.stmt:291  don't allow splitting before first arg while splitting collections");
    KNOWN_TO_FAIL_STRICT.add("splitting/list_arguments.stmt:320  trailing named arguments that do not split");
    KNOWN_TO_FAIL_STRICT.add("splitting/list_arguments.stmt:335  trailing named arguments that do split");
    KNOWN_TO_FAIL_STRICT.add("splitting/list_arguments.stmt:372  do nest because of nested many-arg fn");
    KNOWN_TO_FAIL_STRICT.add("splitting/list_arguments.stmt:392  do nest because of nested many-arg method call");
    KNOWN_TO_FAIL_STRICT.add("splitting/list_arguments.stmt:403  leading positional collections indent if their args split");
    KNOWN_TO_FAIL_STRICT.add("splitting/list_arguments.stmt:417  trailing positional collections indent if their args split");
    KNOWN_TO_FAIL_STRICT.add("splitting/list_arguments.stmt:430  comment before collection");
    KNOWN_TO_FAIL_STRICT.add("splitting/constructors.unit:41  splits before ':' if the parameter list does not fit on one line");
    KNOWN_TO_FAIL_STRICT.add("whitespace/metadata.unit:150  type parameter annotations are inline");
    KNOWN_TO_FAIL_STRICT.add("splitting/type_arguments.stmt:21  split one per line if they don't fit in two lines");
    KNOWN_TO_FAIL_STRICT.add("splitting/type_arguments.stmt:33  prefers to not split at type arguments");
    KNOWN_TO_FAIL_STRICT.add("comments/classes.unit:110  remove blank line before beginning of body");
    KNOWN_TO_FAIL_STRICT.add("comments/functions.unit:78  after ',' in param list");
    KNOWN_TO_FAIL_STRICT.add("comments/functions.unit:82  before '[' in param list");
    KNOWN_TO_FAIL_STRICT.add("comments/functions.unit:98  before '{' in param list");
    KNOWN_TO_FAIL_STRICT.add("splitting/function_arguments.stmt:73  split before all leading args");
    KNOWN_TO_FAIL_STRICT.add("splitting/function_arguments.stmt:164  do not force named args to split on positional function");
    KNOWN_TO_FAIL_STRICT.add("splitting/function_arguments.stmt:182  all named args with leading non-function forces functions to indent");
    KNOWN_TO_FAIL_STRICT.add("splitting/function_arguments.stmt:193  all named args with trailing non-function forces functions to indent");
    KNOWN_TO_FAIL_STRICT.add("splitting/variables.stmt:12  initializer doesn't fit one line, wrap inside, keep name");
    KNOWN_TO_FAIL_STRICT.add("splitting/variables.stmt:17  initializer fits one line");
    KNOWN_TO_FAIL_STRICT.add("splitting/variables.stmt:22  initializer doesn't fit one line, cannot be split");
    KNOWN_TO_FAIL_STRICT.add("splitting/variables.stmt:32  long binary expression initializer");
    KNOWN_TO_FAIL_STRICT.add("splitting/variables.stmt:37  lots of variables with no initializers");
    KNOWN_TO_FAIL_STRICT.add("splitting/type_parameters.unit:32  prefers to not split at type arguments");
    KNOWN_TO_FAIL_STRICT.add("comments/top_level.unit:8");
    KNOWN_TO_FAIL_STRICT.add("comments/top_level.unit:25");
    KNOWN_TO_FAIL_STRICT.add("comments/top_level.unit:50");
    KNOWN_TO_FAIL_STRICT.add("comments/top_level.unit:63");
    KNOWN_TO_FAIL_STRICT.add("comments/top_level.unit:87");
    KNOWN_TO_FAIL_STRICT.add("comments/top_level.unit:96");
    KNOWN_TO_FAIL_STRICT.add("comments/top_level.unit:188  inline block comment between different kinds of directives");
    KNOWN_TO_FAIL_STRICT.add("splitting/map_arguments.stmt:2  args before and after map forces nesting");
    KNOWN_TO_FAIL_STRICT.add("splitting/map_arguments.stmt:12  nothing but map args does not nest");
    KNOWN_TO_FAIL_STRICT.add("splitting/map_arguments.stmt:38  leading maps do not nest");
    KNOWN_TO_FAIL_STRICT.add("splitting/map_arguments.stmt:50  arg between maps forces nesting");
    KNOWN_TO_FAIL_STRICT.add("splitting/map_arguments.stmt:150  split before one leading arg");
    KNOWN_TO_FAIL_STRICT.add("splitting/map_arguments.stmt:184  split before all args including trailing");
    KNOWN_TO_FAIL_STRICT.add("splitting/map_arguments.stmt:200  do nest because of nested many-arg fn");
    KNOWN_TO_FAIL_STRICT.add("splitting/map_arguments.stmt:216  do nest because of nested many-arg method call");
    KNOWN_TO_FAIL_STRICT.add("splitting/assignments.stmt:8  initializer doesn't fit one line, wrap inside, keep name");
    KNOWN_TO_FAIL_STRICT.add("whitespace/blocks.stmt:58  force blank line after non-empty local function");
    KNOWN_TO_FAIL_STRICT.add("regression/other/chains.stmt:11  (indent 4)");
    KNOWN_TO_FAIL_STRICT.add("regression/other/chains.stmt:30  (indent 4)");
    KNOWN_TO_FAIL_STRICT.add("whitespace/statements.stmt:2  multiple labels");
    KNOWN_TO_FAIL_STRICT.add("whitespace/enums.unit:10  trailing comma always splits");
    KNOWN_TO_FAIL_STRICT.add("whitespace/classes.unit:20  trailing space inside body");
    KNOWN_TO_FAIL_STRICT.add("whitespace/classes.unit:25  leading space before 'class'");
    KNOWN_TO_FAIL_STRICT.add("whitespace/classes.unit:36");
    KNOWN_TO_FAIL_STRICT.add("whitespace/classes.unit:57  native class");
    KNOWN_TO_FAIL_STRICT.add("splitting/lists.stmt:21  splits outer lists even if they fit");
    KNOWN_TO_FAIL_STRICT.add("splitting/lists.stmt:34  split indirect outer");
    KNOWN_TO_FAIL_STRICT.add("splitting/lists.stmt:44  nested split list");
    KNOWN_TO_FAIL_STRICT.add("splitting/lists.stmt:76  trailing comma forces split");
    KNOWN_TO_FAIL_STRICT.add("splitting/lists.stmt:122  wrap between elements even when newlines are preserved");
    KNOWN_TO_FAIL_STRICT.add("splitting/lists.stmt:136  ignore line comment after the ']'");
    KNOWN_TO_FAIL_STRICT.add("splitting/lists.stmt:145  preserves one blank line between elements");
    KNOWN_TO_FAIL_STRICT.add("comments/generic_methods.unit:46  var");
    KNOWN_TO_FAIL_STRICT.add("splitting/imports.unit:110  force both keywords to split even if first would fit on first line");
    KNOWN_TO_FAIL_STRICT.add("splitting/mixed.stmt:7  prefers to wrap before '.'");
    KNOWN_TO_FAIL_STRICT.add("splitting/mixed.stmt:19  nested expression indentation");
    KNOWN_TO_FAIL_STRICT.add("splitting/mixed.stmt:29  does not extra indent when multiple levels of nesting happen on one line");
    KNOWN_TO_FAIL_STRICT.add("splitting/mixed.stmt:39  forces extra indent and lines, if later line needs it");
    KNOWN_TO_FAIL_STRICT.add("splitting/mixed.stmt:113  list inside method chain");
    KNOWN_TO_FAIL_STRICT.add("splitting/mixed.stmt:156  mixed multiplicative operators");
    KNOWN_TO_FAIL_STRICT.add("splitting/mixed.stmt:164  mixed additive operators");
    KNOWN_TO_FAIL_STRICT.add("splitting/mixed.stmt:172  mixed shift operators");
    KNOWN_TO_FAIL_STRICT.add("splitting/invocations.stmt:13  don't split before implicit receiver");
    KNOWN_TO_FAIL_STRICT.add("splitting/invocations.stmt:20  trailing functions in a chain do not force it to split");
    KNOWN_TO_FAIL_STRICT.add("splitting/invocations.stmt:178  index in property chain");
    KNOWN_TO_FAIL_STRICT.add("whitespace/script.unit:8  multiple lines between script and library");
    KNOWN_TO_FAIL_STRICT.add("whitespace/script.unit:23  multiple lines between script and import");
    KNOWN_TO_FAIL_STRICT.add("whitespace/script.unit:38  multiple lines between script and line comment");
    KNOWN_TO_FAIL_STRICT.add("whitespace/script.unit:53  multiple lines between script and block comment");
    KNOWN_TO_FAIL_STRICT.add("whitespace/switch.stmt:168  labeled cases");
    KNOWN_TO_FAIL_STRICT.add("splitting/strings.stmt:50  wrap first line if needed");
    KNOWN_TO_FAIL_STRICT.add("whitespace/compilation_unit.unit:38  collapse extra newlines between declarations");
  }

  /**
   * Run a test defined in "*.unit" or "*.stmt" file inside directory {@code dirName}.
   */
  @Override
  protected void runTestInDirectory(String dirName) throws Exception {
    Set<String> fail = new HashSet<>();
    fail.addAll(KNOWN_TO_FAIL);
    fail.addAll(KNOWN_TO_FAIL_STRICT);
    runTestInDirectory(dirName, fail);
  }

  @Override
  protected SourceCode extractSourceSelection(String input, String expectedOutput, boolean isCompilationUnit) {
    return extractSelection(input, isCompilationUnit);
  }
}
