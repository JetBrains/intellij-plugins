// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

library dart_style.test.formatter_test;

import 'dart:io';

import 'package:path/path.dart' as p;
import 'package:unittest/compact_vm_config.dart';
import 'package:unittest/unittest.dart';

import 'package:dart_style/dart_style.dart';

void main() {
  // Tidy up the unittest output.
  filterStacks = true;
  formatStacks = true;
  useCompactVMConfiguration();

  testDirectory("comments");
  testDirectory("regression");
  testDirectory("selections");
  testDirectory("splitting");
  testDirectory("whitespace");

  test("throws a FormatterException on failed parse", () {
    var formatter = new DartFormatter();
    expect(() => formatter.format('wat?!'),
       throwsA(new isInstanceOf<FormatterException>()));
  });

  test("FormatterException describes parse errors", () {
    try {
      new DartFormatter().format("""

      var a = some error;

      var b = another one;
      """, uri: "my_file.dart");

      fail("Should throw.");
    } on FormatterException catch (err) {
      var message = err.message();
      expect(message, contains("my_file.dart"));
      expect(message, contains("line 2"));
      expect(message, contains("line 4"));
    }
  });

  test("adds newline to unit", () {
    expect(new DartFormatter().format("var x = 1;"),
        equals("var x = 1;\n"));
  });

  test("adds newline to unit after trailing comment", () {
    expect(new DartFormatter().format("library foo; //zamm"),
        equals("library foo; //zamm\n"));
  });

  test("removes extra newlines", () {
    expect(new DartFormatter().format("var x = 1;\n\n\n"),
        equals("var x = 1;\n"));
  });

  test("does not add newline to statement", () {
    expect(new DartFormatter().formatStatement("var x = 1;"),
        equals("var x = 1;"));
  });

  test('preserves initial indent', () {
    var formatter = new DartFormatter(indent: 2);
    expect(formatter.formatStatement('if (foo) {bar;}'),  equals(
        '    if (foo) {\n'
        '      bar;\n'
        '    }'));
  });

  group('line endings', () {
    test('uses given line ending', () {
      expect(new DartFormatter(lineEnding: "%").format("var i = 1;"),
          equals("var i = 1;%"));
    });

    test('infers \\r\\n if the first newline uses that', () {
      expect(new DartFormatter().format("var\r\ni\n=\n1;\n"),
          equals("var i = 1;\r\n"));
    });

    test('infers \\n if the first newline uses that', () {
      expect(new DartFormatter().format("var\ni\r\n=\r\n1;\r\n"),
          equals("var i = 1;\n"));
    });

    test('defaults to \\n if there are no newlines', () {
      expect(new DartFormatter().format("var i =1;"),
          equals("var i = 1;\n"));
    });

    test('handles Windows line endings in multiline strings', () {
      expect(new DartFormatter(lineEnding: "\r\n").formatStatement(
          '  """first\r\n'
          'second\r\n'
          'third"""  ;'), equals(
          '"""first\r\n'
          'second\r\n'
          'third""";'));
    });
  });
}

/// Run tests defined in "*.unit" and "*.stmt" files inside directory [name].
void testDirectory(String name) {
  var indentPattern = new RegExp(r"^\(indent (\d+)\)\s*");

  var dir = p.join(p.dirname(p.fromUri(Platform.script)), name);
  for (var entry in new Directory(dir).listSync()) {
    if (!entry.path.endsWith(".stmt") && !entry.path.endsWith(".unit")) {
      continue;
    }

    group("$name ${p.basename(entry.path)}", () {
      var lines = (entry as File).readAsLinesSync();

      // The first line may have a "|" to indicate the page width.
      var pageWidth;
      if (lines[0].endsWith("|")) {
        pageWidth = lines[0].indexOf("|");
        lines = lines.skip(1).toList();
      }

      var i = 0;
      while (i < lines.length) {
        var description = lines[i++].replaceAll(">>>", "").trim();

        // Let the test specify a leading indentation. This is handy for
        // regression tests which often come from a chunk of nested code.
        var leadingIndent = 0;
        var indentMatch = indentPattern.firstMatch(description);
        if (indentMatch != null) {
          // The test specifies it in spaces, but the formatter expects levels.
          leadingIndent = int.parse(indentMatch[1]) ~/ 2;
          description = description.substring(indentMatch.end);
        }

        if (description == "") {
          description = "line ${i + 1}";
        } else {
          description = "line ${i + 1}: $description";
        }

        var input = "";
        while (!lines[i].startsWith("<<<")) {
          input += lines[i++] + "\n";
        }

        var expectedOutput = "";
        while (++i < lines.length && !lines[i].startsWith(">>>")) {
          expectedOutput += lines[i] + "\n";
        }

        test(description, () {
          var isCompilationUnit = p.extension(entry.path) == ".unit";

          var inputCode = _extractSelection(input,
              isCompilationUnit: isCompilationUnit);

          var expected = _extractSelection(expectedOutput,
              isCompilationUnit: isCompilationUnit);

          var formatter = new DartFormatter(
              pageWidth: pageWidth, indent: leadingIndent);

          var actual = formatter.formatSource(inputCode);

          // The test files always put a newline at the end of the expectation.
          // Statements from the formatter (correctly) don't have that, so add
          // one to line up with the expected result.
          var actualText = actual.text;
          if (!isCompilationUnit) actualText += "\n";

          expect(actualText, equals(expected.text));
          expect(actual.selectionStart, equals(expected.selectionStart));
          expect(actual.selectionLength, equals(expected.selectionLength));
        });
      }
    });
  }
}

/// Given a source string that contains ‹ and › to indicate a selection, returns
/// a [SourceCode] with the text (with the selection markers removed) and the
/// correct selection range.
SourceCode _extractSelection(String source, {bool isCompilationUnit: false}) {
  var start = source.indexOf("‹");
  source = source.replaceAll("‹", "");

  var end = source.indexOf("›");
  source = source.replaceAll("›", "");

  return new SourceCode(source,
      isCompilationUnit: isCompilationUnit,
      selectionStart: start == -1 ? null : start,
      selectionLength: end == -1 ? null : end - start);
}
