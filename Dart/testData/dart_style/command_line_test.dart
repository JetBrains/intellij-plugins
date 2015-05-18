// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

library dart_style.test.command_line;

import 'dart:convert';

import 'package:path/path.dart' as p;
import 'package:scheduled_test/descriptor.dart' as d;
import 'package:scheduled_test/scheduled_test.dart';
import 'package:scheduled_test/scheduled_stream.dart';

import 'utils.dart';

void main() {
  setUpTestSuite();

  test("Exits with 0 on success.", () {
    d.dir("code", [
      d.file("a.dart", unformattedSource)
    ]).create();

    var process = runFormatterOnDir();
    process.shouldExit(0);
  });

  test("Exits with 64 on a command line argument error.", () {
    var process = runFormatterOnDir(["-wat"]);
    process.shouldExit(64);
  });

  test("Exits with 65 on a parse error.", () {
    d.dir("code", [
      d.file("a.dart", "herp derp i are a dart")
    ]).create();

    var process = runFormatterOnDir();
    process.shouldExit(65);
  });

  test("Errors if --dry-run and --overwrite are both passed.", () {
    d.dir("code", [
      d.file("a.dart", unformattedSource)
    ]).create();

    var process = runFormatterOnDir(["--dry-run", "--overwrite"]);
    process.shouldExit(64);
  });

  test("Errors if --dry-run and --machine are both passed.", () {
    d.dir("code", [
      d.file("a.dart", unformattedSource)
    ]).create();

    var process = runFormatterOnDir(["--dry-run", "--machine"]);
    process.shouldExit(64);
  });

  test("Errors if --machine and --overwrite are both passed.", () {
    d.dir("code", [
      d.file("a.dart", unformattedSource)
    ]).create();

    var process = runFormatterOnDir(["--machine", "--overwrite"]);
    process.shouldExit(64);
  });

  test("Errors if --dry-run and --machine are both passed.", () {
    d.dir("code", [
      d.file("a.dart", unformattedSource)
    ]).create();

    var process = runFormatter(["--dry-run", "--machine"]);
    process.shouldExit(64);
  });

  test("Errors if --machine and --overwrite are both passed.", () {
    d.dir("code", [
      d.file("a.dart", unformattedSource)
    ]).create();

    var process = runFormatter(["--machine", "--overwrite"]);
    process.shouldExit(64);
  });

  group("--dry-run", () {
    test("prints names of files that would change.", () {
      d.dir("code", [
        d.file("a_bad.dart", unformattedSource),
        d.file("b_good.dart", formattedSource),
        d.file("c_bad.dart", unformattedSource),
        d.file("d_good.dart", formattedSource)
      ]).create();

      var aBad = p.join("code", "a_bad.dart");
      var cBad = p.join("code", "c_bad.dart");

      var process = runFormatterOnDir(["--dry-run"]);

      // The order isn't specified.
      process.stdout.expect(either(aBad, cBad));
      process.stdout.expect(either(aBad, cBad));
      process.shouldExit();
    });

    test("does not modify files.", () {
      d.dir("code", [
        d.file("a.dart", unformattedSource)
      ]).create();

      var process = runFormatterOnDir(["--dry-run"]);
      process.stdout.expect(p.join("code", "a.dart"));
      process.shouldExit();

      d.dir('code', [
        d.file('a.dart', unformattedSource)
      ]).validate();
    });
  });

  group("--machine", () {
    test("writes each output as json", () {
      d.dir("code", [
        d.file("a.dart", unformattedSource),
        d.file("b.dart", unformattedSource)
      ]).create();

      var jsonA = JSON.encode({
        "path": p.join("code", "a.dart"),
        "source": formattedSource,
        "selection": {"offset": -1, "length": -1}
      });

      var jsonB = JSON.encode({
        "path": p.join("code", "b.dart"),
        "source": formattedSource,
        "selection": {"offset": -1, "length": -1}
      });

      var process = runFormatterOnDir(["--machine"]);

      // The order isn't specified.
      process.stdout.expect(either(jsonA, jsonB));
      process.stdout.expect(either(jsonA, jsonB));
      process.shouldExit();
    });
  });

  group("--preserve", () {
    test("errors if given paths.", () {
      var process = runFormatter(["--preserve", "path", "another"]);
      process.shouldExit(64);
    });

    test("errors on wrong number of components.", () {
      var process = runFormatter(["--preserve", "1"]);
      process.shouldExit(64);

      process = runFormatter(["--preserve", "1:2:3"]);
      process.shouldExit(64);
    });

    test("errors on non-integer component.", () {
      var process = runFormatter(["--preserve", "1:2.3"]);
      process.shouldExit(64);
    });

    test("updates selection.", () {
      var process = runFormatter(["--preserve", "6:10", "-m"]);
      process.writeLine(unformattedSource);
      process.closeStdin();

      var json = JSON.encode({
        "path": "<stdin>",
        "source": formattedSource,
        "selection": {"offset": 5, "length": 9}
      });

      process.stdout.expect(json);
      process.shouldExit();
    });
  });

  group("with no paths", () {
    test("errors on --overwrite.", () {
      var process = runFormatter(["--overwrite"]);
      process.shouldExit(64);
    });

    test("reads from stdin.", () {
      var process = runFormatter();
      process.writeLine(unformattedSource);
      process.closeStdin();

      // No trailing newline at the end.
      process.stdout.expect(formattedSource.trimRight());
      process.shouldExit();
    });
  });
}
