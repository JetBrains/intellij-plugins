// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import "package:test/test.dart";

// example from https://pub.dev/packages/test/
void main() <fold text='{...}' expand='true'>{
  group<fold text='(...)' expand='true'>("String", () {
    test<fold text='(...)' expand='true'>(".split() splits the string on the delimiter", () {
      var string = "foo,bar,baz";
      expect(string.split(","), equals(["foo", "bar", "baz"]));
    })</fold>;

    test<fold text='(...)' expand='true'>(".trim() removes surrounding whitespace", () {
      var string = "  foo ";
      expect(string.trim(), equals("foo"));
    }, retry: 3)</fold>;
  }, retry: 4)</fold>;

  group<fold text='(...)' expand='true'>("int", () {
    test<fold text='(...)' expand='true'>(".remainder() returns the remainder of division", () {
      expect(11.remainder(3), equals(2));
    })</fold>;

    test<fold text='(...)' expand='true'>(".toRadixString() returns a hex string", () {
      expect(11.toRadixString(16), equals("b"));
    })</fold>;
  })</fold>;

  // some test methods not in a group:
  test<fold text='(...)' expand='true'>("String.split() splits the string on the delimiter - 2", () {
    var string = "foo,bar,baz";
    expect(string.split(","), equals(["foo", "bar", "baz"]));
  })</fold>;

  test<fold text='(...)' expand='true'>("String.trim() removes surrounding whitespace - 2", () {
    var string = "  foo ";
    expect(string.trim(), equals("foo"));
  })</fold>;

  // Some malformed, and thus not-folded, examples:
  group("String", 2, () {
    test(() {
      var string = "  foo ";
      expect(string.trim(), equals("foo"));
    });
  });

  group(() {
    test(".toRadixString() returns a hex string", () {
      expect(11.toRadixString(16), equals("b"));
    }, 4);
  });

}</fold>