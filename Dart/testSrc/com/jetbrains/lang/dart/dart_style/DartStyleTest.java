// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.dart_style;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.FormatterTestCase;
import com.intellij.util.ArrayUtilRt;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.util.DartTestUtils;
import junit.framework.ComparisonFailure;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Define the dart_style test suite.
 */
public abstract class DartStyleTest extends FormatterTestCase {

  /**
   * The set of tests that are known to fail in all test modes.
   */
  protected final Set<String> KNOWN_TO_FAIL = new HashSet<>();

  {
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:7  space-separated adjacent strings are split if they don't fit");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:47  split conditional because condition doesn't fit");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:54  split conditional because condition splits");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:73  split operator chain around block");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:83  indent previous line farther because later line is nested deeper");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:107  conditional operands are nested");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:117  index expressions can split after '['");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:122  index arguments nest");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:128  successive index arguments");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:134  is");
    KNOWN_TO_FAIL.add("splitting/expressions.stmt:139  as");
    KNOWN_TO_FAIL.add("whitespace/expressions.stmt:110  ?. operator");
    KNOWN_TO_FAIL.add("splitting/exports.unit:15  export moves all shows each to their own line");
    KNOWN_TO_FAIL.add("splitting/exports.unit:36  export moves hides each to their own line");
    KNOWN_TO_FAIL.add("splitting/exports.unit:52  multiline first");
    KNOWN_TO_FAIL.add("splitting/exports.unit:64  multiline second");
    KNOWN_TO_FAIL.add("splitting/exports.unit:76  multiline both");
    KNOWN_TO_FAIL.add("splitting/exports.unit:106  force split in list");
    KNOWN_TO_FAIL.add("regression/other/pub.stmt:22  (indent 4) was slow");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:77  do split empty argument list if it contains a comment");
    KNOWN_TO_FAIL.add("splitting/arguments.stmt:156  trailing collections are not indented");
    KNOWN_TO_FAIL.add("regression/other/analysis_server.unit:1  (indent 2)");
    KNOWN_TO_FAIL.add("regression/other/analysis_server.unit:11");
    KNOWN_TO_FAIL.add("regression/other/analysis_server.unit:28");
    KNOWN_TO_FAIL.add("regression/other/analysis_server.unit:75");
    KNOWN_TO_FAIL.add("regression/other/analysis_server.unit:99  (indent 6)");
    KNOWN_TO_FAIL.add("comments/lists.stmt:39  inline block comment");
    KNOWN_TO_FAIL.add("comments/lists.stmt:50  multiple inline block comments");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:2  do not split before first clause");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:8  split after first clause");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:14  split after second clause");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:24  split multiple variable declarations");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:35  split between updaters splits everything");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:44  nest wrapped initializer");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:53  split in for-in loop");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:58  split in while condition");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:67  don't force variables to split if clauses do");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:77  don't force updates to split if clauses do");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:87  single line for without curlies");
    KNOWN_TO_FAIL.add("splitting/loops.stmt:96  single line for-in without curlies");
    KNOWN_TO_FAIL.add("regression/0000/0000.stmt:17  (indent 4)");
    KNOWN_TO_FAIL.add("regression/0000/0000.stmt:66  (indent 6)");
    KNOWN_TO_FAIL.add("regression/0000/0005.stmt:1");
    KNOWN_TO_FAIL.add("regression/0000/0006.stmt:8");
    KNOWN_TO_FAIL.add("regression/0000/0013.unit:2  no trailing whitespace before initializer comment");
    KNOWN_TO_FAIL.add("regression/0000/0013.unit:14  no trailing whitespace before initializer comment when params wrap");
    KNOWN_TO_FAIL.add("regression/0000/0014.unit:1  https://github.com/dart-lang/dart_style/issues/14");
    KNOWN_TO_FAIL.add("regression/0000/0019.stmt:15  (indent 4)");
    KNOWN_TO_FAIL.add("regression/0000/0019.stmt:21  (indent 6)");
    KNOWN_TO_FAIL.add("regression/0000/0022.stmt:1");
    KNOWN_TO_FAIL.add("regression/0000/0026.stmt:1");
    KNOWN_TO_FAIL.add("regression/0000/0031.stmt:1");
    KNOWN_TO_FAIL.add("regression/0000/0042.unit:2");
    KNOWN_TO_FAIL.add("regression/0000/0043.stmt:1");
    KNOWN_TO_FAIL.add("regression/0000/0050.stmt:1  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0000/0055.unit:17  (indent 12)");
    KNOWN_TO_FAIL.add("regression/0000/0057.stmt:1");
    KNOWN_TO_FAIL.add("regression/0000/0058.unit:1");
    KNOWN_TO_FAIL.add("regression/0000/0068.stmt:13");
    KNOWN_TO_FAIL.add("regression/0000/0075.unit:1");
    KNOWN_TO_FAIL.add("regression/0000/0076.unit:1");
    KNOWN_TO_FAIL.add("regression/0000/0080.unit:1");
    KNOWN_TO_FAIL.add("regression/0000/0081.unit:1  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0000/0084.unit:1");
    KNOWN_TO_FAIL.add("regression/0000/0090.stmt:1");
    KNOWN_TO_FAIL.add("regression/0000/0090.stmt:11");
    KNOWN_TO_FAIL.add("regression/0000/0095.unit:1  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0000/0096.unit:1");
    KNOWN_TO_FAIL.add("regression/0000/0098.stmt:1  (indent 4)");
    KNOWN_TO_FAIL.add("regression/0100/0100.stmt:1  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0100/0108.unit:75  pathologically deep");
    KNOWN_TO_FAIL.add("regression/0100/0108.unit:182");
    KNOWN_TO_FAIL.add("regression/0100/0108.unit:208");
    KNOWN_TO_FAIL.add("regression/0100/0110.stmt:1");
    KNOWN_TO_FAIL.add("regression/0100/0111.unit:1");
    KNOWN_TO_FAIL.add("regression/0100/0112.stmt:1  (indent 4)");
    KNOWN_TO_FAIL.add("regression/0100/0112.stmt:13  (indent 4)");
    KNOWN_TO_FAIL.add("regression/0100/0115.stmt:1");
    KNOWN_TO_FAIL.add("regression/0100/0115.stmt:15  (indent 4)");
    KNOWN_TO_FAIL.add("regression/0100/0119.stmt:9  (indent 6)");
    KNOWN_TO_FAIL.add("regression/0100/0122.unit:1");
    KNOWN_TO_FAIL.add("regression/0100/0122.unit:12");
    KNOWN_TO_FAIL.add("regression/0100/0130.unit:1  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0100/0137.stmt:1  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0100/0140.stmt:1  (indent 8)");
    KNOWN_TO_FAIL.add("regression/0100/0141.unit:1  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0100/0144.unit:1");
    KNOWN_TO_FAIL.add("regression/0100/0144.unit:15  a knock-on issue caused by the initial fix for the above");
    KNOWN_TO_FAIL.add("regression/0100/0155.stmt:1");
    KNOWN_TO_FAIL.add("regression/0100/0156.unit:1");
    KNOWN_TO_FAIL.add("regression/0100/0158.unit:18");
    KNOWN_TO_FAIL.add("regression/0100/0158.unit:31");
    KNOWN_TO_FAIL.add("regression/0100/0162.stmt:1");
    KNOWN_TO_FAIL.add("regression/0100/0162.stmt:54  (indent 4)");
    KNOWN_TO_FAIL.add("regression/0100/0184.unit:1");
    KNOWN_TO_FAIL.add("regression/0100/0185.stmt:1");
    KNOWN_TO_FAIL.add("regression/0100/0186.stmt:1");
    KNOWN_TO_FAIL.add("regression/0100/0187.stmt:1  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0100/0189.stmt:1  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0100/0189.stmt:20  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0100/0198.stmt:1  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0100/0199.stmt:1  (indent 4)");
    KNOWN_TO_FAIL.add("regression/0200/0203.stmt:8  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0200/0204.stmt:1");
    KNOWN_TO_FAIL.add("regression/0200/0204.stmt:10  (indent 8)");
    KNOWN_TO_FAIL.add("regression/0200/0211.unit:16");
    KNOWN_TO_FAIL.add("regression/0200/0217.stmt:9  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0200/0221.unit:1");
    KNOWN_TO_FAIL.add("regression/0200/0221.unit:22");
    KNOWN_TO_FAIL.add("regression/0200/0221.unit:47  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0200/0221.unit:72");
    KNOWN_TO_FAIL.add("regression/0200/0221.unit:87");
    KNOWN_TO_FAIL.add("regression/0200/0222.stmt:7  (indent 4)");
    KNOWN_TO_FAIL.add("regression/0200/0223.stmt:1");
    KNOWN_TO_FAIL.add("regression/0200/0224.stmt:1  (indent 4)");
    KNOWN_TO_FAIL.add("regression/0200/0229.unit:1");
    KNOWN_TO_FAIL.add("regression/0200/0236.unit:1");
    KNOWN_TO_FAIL.add("regression/0200/0238.unit:1");
    KNOWN_TO_FAIL.add("regression/0200/0241.unit:1");
    KNOWN_TO_FAIL.add("regression/0200/0242.unit:1");
    KNOWN_TO_FAIL.add("regression/0200/0247.unit:1  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0200/0250.unit:1");
    KNOWN_TO_FAIL.add("regression/0200/0255.stmt:1  (indent 10)");
    KNOWN_TO_FAIL.add("regression/0200/0256.unit:1");
    KNOWN_TO_FAIL.add("regression/0200/0257.unit:1");
    KNOWN_TO_FAIL.add("regression/0200/0258.unit:1");
    KNOWN_TO_FAIL.add("regression/0300/0357.stmt:1  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0300/0360.stmt:1");
    KNOWN_TO_FAIL.add("regression/0300/0364.unit:1");
    KNOWN_TO_FAIL.add("regression/0300/0367.stmt:1  (indent 6)");
    KNOWN_TO_FAIL.add("regression/0300/0368.unit:1");
    KNOWN_TO_FAIL.add("regression/0300/0369.stmt:1");
    KNOWN_TO_FAIL.add("regression/0300/0370.stmt:1");
    KNOWN_TO_FAIL.add("regression/0300/0373.unit:1  (indent 4)");
    KNOWN_TO_FAIL.add("regression/0300/0375.stmt:1  (indent 4)");
    KNOWN_TO_FAIL.add("regression/0300/0377.stmt:1  (indent 6)");
    KNOWN_TO_FAIL.add("regression/0300/0378.stmt:1  (indent 4)");
    KNOWN_TO_FAIL.add("regression/0300/0380.unit:1");
    KNOWN_TO_FAIL.add("regression/0300/0381.unit:1");
    KNOWN_TO_FAIL.add("regression/0300/0383.unit:1");
    KNOWN_TO_FAIL.add("regression/0300/0384.stmt:1  (indent 4)");
    KNOWN_TO_FAIL.add("regression/0300/0387.unit:1");
    KNOWN_TO_FAIL.add("regression/0300/0389.unit:1");
    KNOWN_TO_FAIL.add("regression/0300/0391.stmt:1");
    KNOWN_TO_FAIL.add("regression/0400/0407.unit:15  (indent 4)");
    KNOWN_TO_FAIL.add("regression/0400/0410.stmt:1");
    KNOWN_TO_FAIL.add("regression/0400/0410.stmt:18  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0400/0410.stmt:36  (indent 4)");
    KNOWN_TO_FAIL.add("regression/0400/0413.unit:1");
    KNOWN_TO_FAIL.add("regression/0400/0420.unit:1");
    KNOWN_TO_FAIL.add("regression/0400/0421.unit:1");
    KNOWN_TO_FAIL.add("regression/0400/0421.unit:42");
    KNOWN_TO_FAIL.add("regression/0400/0422.unit:1");
    KNOWN_TO_FAIL.add("regression/0400/0424.stmt:1");
    KNOWN_TO_FAIL.add("regression/0400/0429.stmt:1  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0400/0434.unit:1  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0400/0436.unit:1");
    KNOWN_TO_FAIL.add("regression/0400/0438.stmt:1");
    KNOWN_TO_FAIL.add("regression/0400/0439.stmt:1  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0400/0439.stmt:9  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0400/0441.unit:1");
    KNOWN_TO_FAIL.add("regression/0400/0444.unit:1");
    KNOWN_TO_FAIL.add("regression/0400/0454.unit:10");
    KNOWN_TO_FAIL.add("regression/0400/0462.unit:1");
    KNOWN_TO_FAIL.add("regression/0400/0462.unit:21");
    KNOWN_TO_FAIL.add("regression/0400/0466.unit:1  (indent 4)");
    KNOWN_TO_FAIL.add("regression/0400/0467.unit:1");
    KNOWN_TO_FAIL.add("regression/0400/0474.unit:1");
    KNOWN_TO_FAIL.add("regression/0400/0475.unit:1");
    KNOWN_TO_FAIL.add("regression/0400/0480.unit:1");
    KNOWN_TO_FAIL.add("regression/0400/0497.unit:1");
    KNOWN_TO_FAIL.add("regression/0500/0503.unit:1");
    KNOWN_TO_FAIL.add("regression/0500/0506.unit:1");
    KNOWN_TO_FAIL.add("regression/0500/0506.unit:27  (indent 6)");
    KNOWN_TO_FAIL.add("regression/0500/0506.unit:51  (indent 4)");
    KNOWN_TO_FAIL.add("regression/0500/0506.unit:77  (indent 2)");
    KNOWN_TO_FAIL.add("regression/0500/0511.unit:1");
    KNOWN_TO_FAIL.add("regression/0500/0520.unit:1");
    KNOWN_TO_FAIL.add("comments/maps.stmt:39  inline block comment");
    KNOWN_TO_FAIL.add("comments/maps.stmt:50  multiple inline block comments");
    KNOWN_TO_FAIL.add("whitespace/constructors.unit:18  initializing formals");
    KNOWN_TO_FAIL.add("whitespace/constructors.unit:28  constructor initialization list");
    KNOWN_TO_FAIL.add("whitespace/constructors.unit:52  DO format constructor initialization lists with each field on its own line.");
    KNOWN_TO_FAIL.add("whitespace/functions.unit:79  trailing comma in single parameter list");
    KNOWN_TO_FAIL.add("whitespace/functions.unit:87  trailing comma in parameter list");
    KNOWN_TO_FAIL.add("whitespace/functions.unit:114  trailing comma in mixed optional parameter list");
    KNOWN_TO_FAIL.add("whitespace/functions.unit:124  trailing comma in mixed named parameter list");
    KNOWN_TO_FAIL.add("whitespace/functions.unit:134  trailing comma with => function containing split");
    KNOWN_TO_FAIL.add("whitespace/functions.unit:143  trailing comma with wrap at =>");
    KNOWN_TO_FAIL.add("whitespace/functions.unit:151  trailing comma function nested in expression");
    KNOWN_TO_FAIL.add("comments/expressions.stmt:10  trailing line comment after non-split");
    KNOWN_TO_FAIL.add("comments/expressions.stmt:63  hard line caused by a comment before a nested line");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:33  trailing lists do not nest");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:99  split in middle of leading args");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:109  split before all leading args");
    KNOWN_TO_FAIL.add("splitting/list_arguments.stmt:155  split before all trailing args");
    KNOWN_TO_FAIL.add("splitting/parameters.stmt:2  many parameters");
    KNOWN_TO_FAIL.add("splitting/parameters.stmt:23  parameters fit but ) does not");
    KNOWN_TO_FAIL.add("splitting/parameters.stmt:32  parameters fit but } does not");
    KNOWN_TO_FAIL.add("splitting/parameters.stmt:79  allow splitting in function type parameters");
    KNOWN_TO_FAIL.add("splitting/parameters.stmt:85  split optional onto one per line if they don't fit on one line");
    KNOWN_TO_FAIL.add("splitting/parameters.stmt:92  split on positional default value");
    KNOWN_TO_FAIL.add("splitting/parameters.stmt:102  split on named value");
    KNOWN_TO_FAIL.add("splitting/parameters.unit:11  indent parameters more if body is a wrapped =>");
    KNOWN_TO_FAIL.add("splitting/constructors.unit:2  Single initializers can be on one line");
    KNOWN_TO_FAIL.add("splitting/constructors.unit:12  (or not)");
    KNOWN_TO_FAIL.add("splitting/constructors.unit:23  Multiple initializers are one per line");
    KNOWN_TO_FAIL.add("splitting/constructors.unit:35  try to keep constructor call together");
    KNOWN_TO_FAIL.add("splitting/constructors.unit:51  indent parameters more if body is a wrapped =>");
    KNOWN_TO_FAIL.add("splitting/constructors.unit:61  wrap initializers past the ':'");
    KNOWN_TO_FAIL.add("splitting/constructors.unit:75  split at '=' in initializer");
    KNOWN_TO_FAIL.add("whitespace/metadata.unit:77  force newline before member");
    KNOWN_TO_FAIL.add("whitespace/metadata.unit:95  multiple annotations before members get own line");
    KNOWN_TO_FAIL.add("whitespace/metadata.unit:197  metadata on function-typed formal parameter");
    KNOWN_TO_FAIL.add("whitespace/metadata.unit:205  metadata on default formal parameter");
    KNOWN_TO_FAIL.add("whitespace/metadata.unit:211  split between metadata and parameter indents");
    KNOWN_TO_FAIL.add("whitespace/metadata.unit:218  unsplit with trailing commas");
    KNOWN_TO_FAIL.add("whitespace/metadata.unit:225  split with trailing commas");
    KNOWN_TO_FAIL.add("splitting/type_arguments.stmt:6  prefer to split between args even when they all fit on next line");
    KNOWN_TO_FAIL.add("splitting/type_arguments.stmt:11  split before first if needed");
    KNOWN_TO_FAIL.add("splitting/type_arguments.stmt:16  split in middle if fit in two lines");
    KNOWN_TO_FAIL.add("comments/classes.unit:39  inline block comment");
    KNOWN_TO_FAIL.add("comments/classes.unit:50  multiple inline block comments");
    KNOWN_TO_FAIL.add("comments/classes.unit:147  force doc comment between classes to have two newlines before");
    KNOWN_TO_FAIL.add("comments/classes.unit:157  force doc comment between classes to have newline after");
    KNOWN_TO_FAIL.add("comments/functions.unit:39  inline block comment");
    KNOWN_TO_FAIL.add("comments/functions.unit:50  multiple inline block comments");
    KNOWN_TO_FAIL.add("comments/functions.unit:86  after '[' in param list");
    KNOWN_TO_FAIL.add("comments/functions.unit:102  after '{' in param list");
    KNOWN_TO_FAIL.add("comments/functions.unit:106  before '}' in param list");
    KNOWN_TO_FAIL.add("splitting/function_arguments.stmt:18  trailing functions do not nest");
    KNOWN_TO_FAIL.add("splitting/function_arguments.stmt:36  arg between functions forces nesting");
    KNOWN_TO_FAIL.add("splitting/function_arguments.stmt:64  split in middle of leading args");
    KNOWN_TO_FAIL.add("splitting/function_arguments.stmt:171  args before and after functions split independently");
    KNOWN_TO_FAIL.add("splitting/members.unit:11  can split on getter");
    KNOWN_TO_FAIL.add("splitting/members.unit:20  can split on setter");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:27  long function call initializer");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:44  multiple variables stay on one line if they fit");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:48");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:62  initializers get extra indentation if there are multiple variables");
    KNOWN_TO_FAIL.add("splitting/variables.stmt:70  dartbug.com/16379");
    KNOWN_TO_FAIL.add("splitting/type_parameters.unit:6  prefer to split between params even when they all fit on next line");
    KNOWN_TO_FAIL.add("splitting/type_parameters.unit:11  split before first if needed");
    KNOWN_TO_FAIL.add("splitting/type_parameters.unit:16  split in middle if fit in two lines");
    KNOWN_TO_FAIL.add("splitting/type_parameters.unit:21  split one per line if they don't fit in two lines");
    KNOWN_TO_FAIL.add("comments/top_level.unit:216  in dotted name");
    KNOWN_TO_FAIL.add("splitting/map_arguments.stmt:26  trailing maps do not nest");
    KNOWN_TO_FAIL.add("splitting/map_arguments.stmt:79  split in middle of leading args");
    KNOWN_TO_FAIL.add("splitting/map_arguments.stmt:88  split before all leading args");
    KNOWN_TO_FAIL.add("splitting/map_arguments.stmt:126  split before all trailing args");
    KNOWN_TO_FAIL.add("regression/other/dart2js.unit:1  (indent 4) preemption follows constraints");
    KNOWN_TO_FAIL.add("splitting/arrows.stmt:7  newline before fn expression should not force => to split");
    KNOWN_TO_FAIL.add("splitting/statements.stmt:6  wrapped assert");
    KNOWN_TO_FAIL.add("splitting/statements.stmt:15  split assert with message before both");
    KNOWN_TO_FAIL.add("splitting/statements.stmt:20  split assert with message after first");
    KNOWN_TO_FAIL.add("splitting/statements.stmt:25  split assert with message at both");
    KNOWN_TO_FAIL.add("splitting/statements.stmt:31  split in do-while condition");
    KNOWN_TO_FAIL.add("splitting/statements.stmt:36  split in switch value");
    KNOWN_TO_FAIL.add("splitting/classes.unit:7");
    KNOWN_TO_FAIL.add("whitespace/classes.unit:2  indentation");
    KNOWN_TO_FAIL.add("whitespace/classes.unit:41");
    KNOWN_TO_FAIL.add("whitespace/classes.unit:64  require blank line after non-empty block-bodied members");
    KNOWN_TO_FAIL.add("whitespace/classes.unit:88  no required blank line after empty block-bodied members");
    KNOWN_TO_FAIL.add("splitting/lists.stmt:61  force multi-line because of contained block");
    KNOWN_TO_FAIL.add("comments/generic_methods.unit:10  method");
    KNOWN_TO_FAIL.add("splitting/imports.unit:6  wrap import at as");
    KNOWN_TO_FAIL.add("splitting/imports.unit:11  split before deferred");
    KNOWN_TO_FAIL.add("splitting/imports.unit:25  import moves all shows each to their own line");
    KNOWN_TO_FAIL.add("splitting/imports.unit:46  import moves hides each to their own line");
    KNOWN_TO_FAIL.add("splitting/imports.unit:62  multiline first");
    KNOWN_TO_FAIL.add("splitting/imports.unit:74  multiline second");
    KNOWN_TO_FAIL.add("splitting/imports.unit:86  multiline both");
    KNOWN_TO_FAIL.add("splitting/imports.unit:116  force split in list");
    KNOWN_TO_FAIL.add("splitting/imports.unit:128  if configurations don't fit, they all split");
    KNOWN_TO_FAIL.add("splitting/imports.unit:134  do not split before uri");
    KNOWN_TO_FAIL.add("splitting/imports.unit:139  split before ==");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:48  function inside a collection");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:62  function inside an argument list");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:81  nested function inside nested expression");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:94  wrap before =>");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:101  wrap after =>");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:130  binary operators in ascending precedence");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:143  binary operators in descending precedence");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:194  choose extra nesting if it leads to better solution");
    KNOWN_TO_FAIL.add("splitting/mixed.stmt:207  no extra indent before binary operators in => body");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:30  a function in the middle of a chain is indented");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:40  a function in the middle of a chain is indented");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:51  a function in the middle of a chain is indented");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:131  unsplit cascade unsplit method");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:135  split cascade unsplit method");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:141  unsplit cascade split method");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:149  split cascade split method");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:160  cascade setters on method chain");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:192  chained indexes");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:220  target splits more deeply than method chain");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:227  splitting the target forces methods to split");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:234  target splits more deeply than property chain");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:241  splitting the target forces methods to split");
    KNOWN_TO_FAIL.add("splitting/invocations.stmt:258  do not split on '.' when target is map");
    KNOWN_TO_FAIL.add("selections/selections.unit:70  only whitespace in newline selected");
    KNOWN_TO_FAIL.add("whitespace/compilation_unit.unit:96  require newline between non-class declarations");
    KNOWN_TO_FAIL.add("whitespace/compilation_unit.unit:104  require blank line after non-empty block-bodied members");
    KNOWN_TO_FAIL.add("whitespace/compilation_unit.unit:125  no required blank line after empty block-bodied members");
  }

  @Override
  protected String getFileExtension() {
    return DartFileType.DEFAULT_EXTENSION;
  }

  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH;
  }

  @Override
  protected String getBasePath() {
    return "dart_style";
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

  public void testGeneric_methods() throws Exception {
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

  public void testStatements3() throws Exception {
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

  public void test0000() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0005() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0006() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0009() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0013() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0014() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0019() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0021() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0022() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0023() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0025() throws Exception {
    // Verify leadingIndent is working.
    runTestInDirectory("regression/0000");
  }

  public void test0026() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0027() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0028() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0029() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0031() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0033() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0036() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0037() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0038() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0039() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0040() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0041() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0042() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0043() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0044() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0045() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0046() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0047() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0049() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0050() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0054() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0055() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0056() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0057() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0058() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0060() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0061() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0066() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0068() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0069() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0070() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0071() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0072() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0075() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0076() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0077() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0080() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0081() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0082() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0083() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0084() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0085() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0086() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0087() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0089() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0090() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0091() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0095() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0096() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0098() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0099() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0100() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0102() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0108() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0109() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0110() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0111() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0112() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0113() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0114() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0115() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0119() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0121() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0122() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0130() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0132() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0135() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0137() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0139() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0140() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0141() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0142() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0144() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0146() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0151() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0152() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0154() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0155() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0156() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0158() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0161() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0162() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0168() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0170() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0171() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0176() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0177() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0178() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0184() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0185() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0186() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0187() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0189() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0192() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0197() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0198() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0199() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0200() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0201() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0203() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0204() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0205() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0206() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0211() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0212() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0217() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0218() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0221() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0222() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0223() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0224() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0228() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0229() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0232() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0235() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0236() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0237() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0238() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0241() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0242() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0243() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0247() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0249() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0250() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0255() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0256() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0257() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0258() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0259() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0357() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0360() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0361() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0364() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0366() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0367() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0368() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0369() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0370() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0373() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0374() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0375() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0377() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0378() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0379() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0380() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0381() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0383() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0384() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0387() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0388() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0389() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0391() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0394() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0398() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0399() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0404() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0407() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0410() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0413() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0420() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0421() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0422() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0424() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0429() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0434() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0436() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0437() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0438() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0439() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0441() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0443() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0444() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0448() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0449() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0454() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0461() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0462() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0463() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0465() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0466() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0467() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0474() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0475() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0478() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0480() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0481() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0484() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0488() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0489() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0494() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0497() throws Exception {
    runTestInDirectory("regression/0400");
  }

  public void test0503() throws Exception {
    runTestInDirectory("regression/0500");
  }

  public void test0506() throws Exception {
    runTestInDirectory("regression/0500");
  }

  public void test0511() throws Exception {
    runTestInDirectory("regression/0500");
  }

  public void test0513() throws Exception {
    runTestInDirectory("regression/0500");
  }

  public void test0514() throws Exception {
    runTestInDirectory("regression/0500");
  }

  public void test0519() throws Exception {
    runTestInDirectory("regression/0500");
  }

  public void test0527() throws Exception {
    runTestInDirectory("regression/0500");
  }

  public void test0529() throws Exception {
    runTestInDirectory("regression/0500");
  }

  public void test0520() throws Exception {
    runTestInDirectory("regression/0500");
  }

  public void testAnalysis_server() throws Exception {
    runTestInDirectory("regression/other");
  }

  public void testAngular() throws Exception {
    runTestInDirectory("regression/other");
  }

  public void testChains() throws Exception {
    runTestInDirectory("regression/other");
  }

  public void testDart2js() throws Exception {
    runTestInDirectory("regression/other");
  }

  public void testMisc() throws Exception {
    runTestInDirectory("regression/other");
  }

  public void testPub() throws Exception {
    runTestInDirectory("regression/other");
  }

  /**
   * Run a test defined in "*.unit" or "*.stmt" file inside directory {@code dirName}.
   */
  protected abstract void runTestInDirectory(String dirName) throws Exception;

  /**
   * Run a test defined in "*.unit" or "*.stmt" file inside directory {@code dirName}.
   * Only signal failures for tests that fail and are not listed in {@code knownFailures.}
   */
  protected void runTestInDirectory(String dirName, Set knownFailures) throws Exception {
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
      String[] lines = ArrayUtilRt.toStringArray(FileUtil.loadLines(entry, "UTF-8"));
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

      LOG.debug("\nTest: " + dirName + "/" + testFileName + ", Right margin: " + pageWidth);
      final CommonCodeStyleSettings settings = getSettings(DartLanguage.INSTANCE);
      settings.RIGHT_MARGIN = pageWidth;
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
          if (leadingIndent > 0 && leadingIndent < line.length()) line = line.substring(leadingIndent);
          if (!isCompilationUnit && !line.isEmpty()) line = "  " + line;
          input += line + "\n";
        }

        if (!isCompilationUnit) input += "}\n";

        String expectedOutput = "";
        if (!isCompilationUnit) expectedOutput += "m() {\n";

        i++;

        while (i < lines.length && !lines[i].startsWith(">>>")) {
          String line = lines[i++];
          if (leadingIndent > 0 && leadingIndent < line.length()) line = line.substring(leadingIndent);
          if (!isCompilationUnit && !line.isEmpty()) line = "  " + line;
          expectedOutput += line + "\n";
        }

        if (!isCompilationUnit) expectedOutput += "}\n";

        SourceCode inputCode = extractSourceSelection(input, expectedOutput, isCompilationUnit);
        SourceCode expected = extractSelection(expectedOutput, isCompilationUnit);

        try {
          doTextTest(inputCode.text, expected.text);
          if (knownFailures.contains(description)) {
            fail("The test passed, but was expected to fail: " + description);
          }
          LOG.debug("TEST PASSED: " + (description.isEmpty() ? "(unnamed)" : description));
        }
        catch (ComparisonFailure failure) {
          if (!knownFailures.contains(description.replace('"', '\''))) {
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

  protected abstract SourceCode extractSourceSelection(String input, String expectedOutput, boolean isCompilationUnit);

  /*
   * Given a source string that contains ‹ and › to indicate a selection, returns
   * a <code>SourceCode</code> with the text (with the selection markers removed)
   * and the correct selection range.
   */
  protected static SourceCode extractSelection(String source, boolean isCompilationUnit) {
    int start = source.indexOf("‹");
    source = source.replaceAll("‹", "");

    int end = source.indexOf("›");
    source = source.replaceAll("›", "");

    return new SourceCode(source, isCompilationUnit, start == -1 ? 0 : start, end == -1 ? source.length() : end - start);
  }

  protected static class SourceCode {
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
