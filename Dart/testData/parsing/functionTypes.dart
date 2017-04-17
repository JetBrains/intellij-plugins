import 'dart:core';
import 'dart:core' as core;
import 'package:expect/expect.dart';

@NoInline()
@AssumeDynamic()
confuse(f) => f;

final bool inCheckedMode =
    (() { bool result = false; assert(result = true); return result; })();


typedef F0<T> = int Function(int x);
typedef F100<T> = Function Function(List<T> x);
typedef F200<T> = core.List<core.int> Function(int y, {List<Function> x});
typedef F300<T> = Function(int x0, {Function x});
typedef F400<T> = Function Function<A>(int x);
typedef F500<T> = int Function(int x0, {int x}) Function();
typedef F600<T> = int Function([core.List<core.int> x]) Function();
typedef F700<T> = Function Function(int y, [int x]) Function();
typedef F800<T> = Function Function(int x1, [List<Function> x2]) Function();
typedef F900<T> = Function Function(int x0, {List<T> x}) Function();
typedef F1000<T> = List<Function> Function(List<Function> x) Function();
typedef F1100<T> = List<Function> Function(int y, [List<T> x]) Function();
typedef F1200<T> = core.List<core.int> Function([Function x1]) Function();
typedef F1300<T> = core.List<core.int> Function({core.List<core.int> x}) Function();
typedef F1400<T> = List<T> Function(int y, {int x}) Function();
typedef F1500<T> = List<T> Function(int x0, [core.List<core.int> x]) Function();
typedef F1600<T> = Function(int x0) Function();
typedef F1700<T> = Function(int x, [List<Function> x2]) Function();
typedef F1800<T> = Function(int y, {List<T> x}) Function();
typedef F1900<T> = void Function([List<Function> x]) Function();
typedef F2000<T> = void Function(List<T> x0) Function();
typedef F2100<T> = List<Function> Function<A>(Function x) Function();
typedef F2200<T> = Function<A>(List<Function> x) Function();
typedef F2300<T> = void Function<A>(core.List<core.int> x) Function();


int f0(int x) => null;
Function f100(List<int> x) => null;
core.List<core.int> f200(int y, {List<Function> x}) => null;
f300(int x0, {Function x}) => null;
Function f400<A>(int x) => null;
int Function(int x0, {int x}) f500() => null;
int Function([core.List<core.int> x]) f600() => null;
Function Function(int y, [int x]) f700() => null;
Function Function(int x0, [List<Function> x1]) f800() => null;
Function Function(int x0, {List<int> x}) f900() => null;
List<Function> Function(List<Function> x) f1000() => null;
List<Function> Function(int y, [List<int> x]) f1100() => null;
core.List<core.int> Function([Function x0]) f1200() => null;
core.List<core.int> Function({core.List<core.int> x}) f1300() => null;
List<int> Function(int y, {int x}) f1400() => null;
List<int> Function(int x0, [core.List<core.int> x]) f1500() => null;
Function(int x0) f1600() => null;
Function(int x, [List<Function> x0]) f1700() => null;
Function(int y, {List<int> x}) f1800() => null;
void Function([List<Function> x]) f1900() => null;
void Function(List<int> x0) f2000() => null;
List<Function> Function<A>(Function x) f2100() => null;
Function<A>(List<Function> x) f2200() => null;
void Function<A>(core.List<core.int> x) f2300() => null;


class U0<T> {
  final bool tIsBool;
  final bool tIsInt;
  final bool tIsDynamic;

  int Function(int x) x0;
  Function Function(List<T> x) x100;
  core.List<core.int> Function(int y, {List<Function> x}) x200;
  Function(int x0, {Function x}) x300;
  Function Function<A>(int x) x400;
  int Function(int x0, {int x}) Function() x500;
  int Function([core.List<core.int> x]) Function() x600;
  Function Function(int y, [int x]) Function() x700;
  Function Function(int x1, [List<Function> x2]) Function() x800;
  Function Function(int x0, {List<T> x}) Function() x900;
  List<Function> Function(List<Function> x) Function() x1000;
  List<Function> Function(int y, [List<T> x]) Function() x1100;
  core.List<core.int> Function([Function x1]) Function() x1200;
  core.List<core.int> Function({core.List<core.int> x}) Function() x1300;
  List<T> Function(int y, {int x}) Function() x1400;
  List<T> Function(int x0, [core.List<core.int> x]) Function() x1500;
  Function(int x0) Function() x1600;
  Function(int x, [List<Function> x2]) Function() x1700;
  Function(int y, {List<T> x}) Function() x1800;
  void Function([List<Function> x]) Function() x1900;
  void Function(List<T> x0) Function() x2000;
  List<Function> Function<A>(Function x) Function() x2100;
  Function<A>(List<Function> x) Function() x2200;
  void Function<A>(core.List<core.int> x) Function() x2300;


  U0({this.tIsBool: false, this.tIsInt: false})
      : tIsDynamic = !tIsBool && !tIsInt;

  int m0(int x) => null;
  Function m100(List<T> x) => null;
  core.List<core.int> m200(int y, {List<Function> x}) => null;
  m300(int x0, {Function x}) => null;
  Function m400<A>(int x) => null;
  int Function(int x0, {int x}) m500() => null;
  int Function([core.List<core.int> x]) m600() => null;
  Function Function(int y, [int x]) m700() => null;
  Function Function(int x0, [List<Function> x1]) m800() => null;
  Function Function(int x0, {List<T> x}) m900() => null;
  List<Function> Function(List<Function> x) m1000() => null;
  List<Function> Function(int y, [List<T> x]) m1100() => null;
  core.List<core.int> Function([Function x0]) m1200() => null;
  core.List<core.int> Function({core.List<core.int> x}) m1300() => null;
  List<T> Function(int y, {int x}) m1400() => null;
  List<T> Function(int x0, [core.List<core.int> x]) m1500() => null;
  Function(int x0) m1600() => null;
  Function(int x, [List<Function> x0]) m1700() => null;
  Function(int y, {List<T> x}) m1800() => null;
  void Function([List<Function> x]) m1900() => null;
  void Function(List<T> x0) m2000() => null;
  List<Function> Function<A>(Function x) m2100() => null;
  Function<A>(List<Function> x) m2200() => null;
  void Function<A>(core.List<core.int> x) m2300() => null;


  runTests() {
    testF0();
    testF100();
    testF200();
    testF300();
    testF400();
    testF500();
    testF600();
    testF700();
    testF800();
    testF900();
    testF1000();
    testF1100();
    testF1200();
    testF1300();
    testF1400();
    testF1500();
    testF1600();
    testF1700();
    testF1800();
    testF1900();
    testF2000();
    testF2100();
    testF2200();
    testF2300();
  }

  void testF0() {
    // int Function(int x)
    Expect.isTrue(f0 is F0);
    Expect.isTrue(confuse(f0) is F0);
    // In checked mode, verifies the type.
    int Function(int x) l0;
    // The static function f0 sets `T` to `int`.
    if (!tIsBool) {
      x0 = f0 as dynamic;
      l0 = f0 as dynamic;
      x0 = confuse(f0);
      l0 = confuse(f0);
    }

    Expect.isTrue(m0 is F0);
    Expect.isTrue(m0 is int Function(int x));
    Expect.isTrue(confuse(m0) is F0);
    // In checked mode, verifies the type.
    x0 = m0;
    l0 = m0;
    x0 = confuse(m0);
    l0 = confuse(m0);

  }

  void testF100() {
    // Function Function(List<T> x)
    Expect.isTrue(f100 is F100);
    Expect.isTrue(confuse(f100) is F100);
    // In checked mode, verifies the type.
    Function Function(List<T> x) l100;
    // The static function f100 sets `T` to `int`.
    if (!tIsBool) {
      x100 = f100 as dynamic;
      l100 = f100 as dynamic;
      x100 = confuse(f100);
      l100 = confuse(f100);
    }

    Expect.isTrue(m100 is F100);
    Expect.isTrue(m100 is Function Function(List<T> x));
    Expect.isTrue(confuse(m100) is F100);
    // In checked mode, verifies the type.
    x100 = m100;
    l100 = m100;
    x100 = confuse(m100);
    l100 = confuse(m100);
    if (!tIsBool) {
      Expect.isTrue(f100 is F100<int>);
      Expect.isFalse(f100 is F100<bool>);
      Expect.isTrue(confuse(f100) is F100<int>);
      Expect.isFalse(confuse(f100) is F100<bool>);
      Expect.equals(tIsDynamic, m100 is F100<bool>);
      Expect.equals(tIsDynamic, confuse(m100) is F100<bool>);
    } else {
      if (inCheckedMode) {
        Expect.throws(() { x100 = (f100 as dynamic); });
        Expect.throws(() { x100 = confuse(f100); });
        Function Function(List<T> x) l100;
        Expect.throws(() { l100 = (f100 as dynamic); });
        Expect.throws(() { l100 = confuse(f100); });
      }
      Function Function(List<T> x) l100 = m100;
      // In checked mode, verifies the type.
      x100 = m100;
      x100 = confuse(m100);
    }
  }

  void testF200() {
    // core.List<core.int> Function(int y, {List<Function> x})
    Expect.isTrue(f200 is F200);
    Expect.isTrue(confuse(f200) is F200);
    // In checked mode, verifies the type.
    core.List<core.int> Function(int y, {List<Function> x}) l200;
    // The static function f200 sets `T` to `int`.
    if (!tIsBool) {
      x200 = f200 as dynamic;
      l200 = f200 as dynamic;
      x200 = confuse(f200);
      l200 = confuse(f200);
    }

    Expect.isTrue(m200 is F200);
    Expect.isTrue(m200 is core.List<core.int> Function(int y, {List<Function> x}));
    Expect.isTrue(confuse(m200) is F200);
    // In checked mode, verifies the type.
    x200 = m200;
    l200 = m200;
    x200 = confuse(m200);
    l200 = confuse(m200);

  }

  void testF300() {
    // Function(int x0, {Function x})
    Expect.isTrue(f300 is F300);
    Expect.isTrue(confuse(f300) is F300);
    // In checked mode, verifies the type.
    Function(int x0, {Function x}) l300;
    // The static function f300 sets `T` to `int`.
    if (!tIsBool) {
      x300 = f300 as dynamic;
      l300 = f300 as dynamic;
      x300 = confuse(f300);
      l300 = confuse(f300);
    }

    Expect.isTrue(m300 is F300);
    Expect.isTrue(m300 is Function(int x0, {Function x}));
    Expect.isTrue(confuse(m300) is F300);
    // In checked mode, verifies the type.
    x300 = m300;
    l300 = m300;
    x300 = confuse(m300);
    l300 = confuse(m300);

  }

  void testF400() {
    // Function Function<A>(int x)
    Expect.isTrue(f400 is F400);
    Expect.isTrue(confuse(f400) is F400);
    // In checked mode, verifies the type.
    Function Function<A>(int x) l400;
    // The static function f400 sets `T` to `int`.
    if (!tIsBool) {
      x400 = f400 as dynamic;
      l400 = f400 as dynamic;
      x400 = confuse(f400);
      l400 = confuse(f400);
    }

    Expect.isTrue(m400 is F400);
    Expect.isTrue(m400 is Function Function<A>(int x));
    Expect.isTrue(confuse(m400) is F400);
    // In checked mode, verifies the type.
    x400 = m400;
    l400 = m400;
    x400 = confuse(m400);
    l400 = confuse(m400);

  }

  void testF500() {
    // int Function(int x0, {int x}) Function()
    Expect.isTrue(f500 is F500);
    Expect.isTrue(confuse(f500) is F500);
    // In checked mode, verifies the type.
    int Function(int x0, {int x}) Function() l500;
    // The static function f500 sets `T` to `int`.
    if (!tIsBool) {
      x500 = f500 as dynamic;
      l500 = f500 as dynamic;
      x500 = confuse(f500);
      l500 = confuse(f500);
    }

    Expect.isTrue(m500 is F500);
    Expect.isTrue(m500 is int Function(int x0, {int x}) Function());
    Expect.isTrue(confuse(m500) is F500);
    // In checked mode, verifies the type.
    x500 = m500;
    l500 = m500;
    x500 = confuse(m500);
    l500 = confuse(m500);

  }

  void testF600() {
    // int Function([core.List<core.int> x]) Function()
    Expect.isTrue(f600 is F600);
    Expect.isTrue(confuse(f600) is F600);
    // In checked mode, verifies the type.
    int Function([core.List<core.int> x]) Function() l600;
    // The static function f600 sets `T` to `int`.
    if (!tIsBool) {
      x600 = f600 as dynamic;
      l600 = f600 as dynamic;
      x600 = confuse(f600);
      l600 = confuse(f600);
    }

    Expect.isTrue(m600 is F600);
    Expect.isTrue(m600 is int Function([core.List<core.int> x]) Function());
    Expect.isTrue(confuse(m600) is F600);
    // In checked mode, verifies the type.
    x600 = m600;
    l600 = m600;
    x600 = confuse(m600);
    l600 = confuse(m600);

  }

  void testF700() {
    // Function Function(int y, [int x]) Function()
    Expect.isTrue(f700 is F700);
    Expect.isTrue(confuse(f700) is F700);
    // In checked mode, verifies the type.
    Function Function(int y, [int x]) Function() l700;
    // The static function f700 sets `T` to `int`.
    if (!tIsBool) {
      x700 = f700 as dynamic;
      l700 = f700 as dynamic;
      x700 = confuse(f700);
      l700 = confuse(f700);
    }

    Expect.isTrue(m700 is F700);
    Expect.isTrue(m700 is Function Function(int y, [int x]) Function());
    Expect.isTrue(confuse(m700) is F700);
    // In checked mode, verifies the type.
    x700 = m700;
    l700 = m700;
    x700 = confuse(m700);
    l700 = confuse(m700);

  }

  void testF800() {
    // Function Function(int x1, [List<Function> x2]) Function()
    Expect.isTrue(f800 is F800);
    Expect.isTrue(confuse(f800) is F800);
    // In checked mode, verifies the type.
    Function Function(int x1, [List<Function> x2]) Function() l800;
    // The static function f800 sets `T` to `int`.
    if (!tIsBool) {
      x800 = f800 as dynamic;
      l800 = f800 as dynamic;
      x800 = confuse(f800);
      l800 = confuse(f800);
    }

    Expect.isTrue(m800 is F800);
    Expect.isTrue(m800 is Function Function(int x1, [List<Function> x2]) Function());
    Expect.isTrue(confuse(m800) is F800);
    // In checked mode, verifies the type.
    x800 = m800;
    l800 = m800;
    x800 = confuse(m800);
    l800 = confuse(m800);

  }

  void testF900() {
    // Function Function(int x0, {List<T> x}) Function()
    Expect.isTrue(f900 is F900);
    Expect.isTrue(confuse(f900) is F900);
    // In checked mode, verifies the type.
    Function Function(int x0, {List<T> x}) Function() l900;
    // The static function f900 sets `T` to `int`.
    if (!tIsBool) {
      x900 = f900 as dynamic;
      l900 = f900 as dynamic;
      x900 = confuse(f900);
      l900 = confuse(f900);
    }

    Expect.isTrue(m900 is F900);
    Expect.isTrue(m900 is Function Function(int x0, {List<T> x}) Function());
    Expect.isTrue(confuse(m900) is F900);
    // In checked mode, verifies the type.
    x900 = m900;
    l900 = m900;
    x900 = confuse(m900);
    l900 = confuse(m900);
    if (!tIsBool) {
      Expect.isTrue(f900 is F900<int>);
      Expect.isFalse(f900 is F900<bool>);
      Expect.isTrue(confuse(f900) is F900<int>);
      Expect.isFalse(confuse(f900) is F900<bool>);
      Expect.equals(tIsDynamic, m900 is F900<bool>);
      Expect.equals(tIsDynamic, confuse(m900) is F900<bool>);
    } else {
      if (inCheckedMode) {
        Expect.throws(() { x900 = (f900 as dynamic); });
        Expect.throws(() { x900 = confuse(f900); });
        Function Function(int x0, {List<T> x}) Function() l900;
        Expect.throws(() { l900 = (f900 as dynamic); });
        Expect.throws(() { l900 = confuse(f900); });
      }
      Function Function(int x0, {List<T> x}) Function() l900 = m900;
      // In checked mode, verifies the type.
      x900 = m900;
      x900 = confuse(m900);
    }
  }

  void testF1000() {
    // List<Function> Function(List<Function> x) Function()
    Expect.isTrue(f1000 is F1000);
    Expect.isTrue(confuse(f1000) is F1000);
    // In checked mode, verifies the type.
    List<Function> Function(List<Function> x) Function() l1000;
    // The static function f1000 sets `T` to `int`.
    if (!tIsBool) {
      x1000 = f1000 as dynamic;
      l1000 = f1000 as dynamic;
      x1000 = confuse(f1000);
      l1000 = confuse(f1000);
    }

    Expect.isTrue(m1000 is F1000);
    Expect.isTrue(m1000 is List<Function> Function(List<Function> x) Function());
    Expect.isTrue(confuse(m1000) is F1000);
    // In checked mode, verifies the type.
    x1000 = m1000;
    l1000 = m1000;
    x1000 = confuse(m1000);
    l1000 = confuse(m1000);

  }

  void testF1100() {
    // List<Function> Function(int y, [List<T> x]) Function()
    Expect.isTrue(f1100 is F1100);
    Expect.isTrue(confuse(f1100) is F1100);
    // In checked mode, verifies the type.
    List<Function> Function(int y, [List<T> x]) Function() l1100;
    // The static function f1100 sets `T` to `int`.
    if (!tIsBool) {
      x1100 = f1100 as dynamic;
      l1100 = f1100 as dynamic;
      x1100 = confuse(f1100);
      l1100 = confuse(f1100);
    }

    Expect.isTrue(m1100 is F1100);
    Expect.isTrue(m1100 is List<Function> Function(int y, [List<T> x]) Function());
    Expect.isTrue(confuse(m1100) is F1100);
    // In checked mode, verifies the type.
    x1100 = m1100;
    l1100 = m1100;
    x1100 = confuse(m1100);
    l1100 = confuse(m1100);
    if (!tIsBool) {
      Expect.isTrue(f1100 is F1100<int>);
      Expect.isFalse(f1100 is F1100<bool>);
      Expect.isTrue(confuse(f1100) is F1100<int>);
      Expect.isFalse(confuse(f1100) is F1100<bool>);
      Expect.equals(tIsDynamic, m1100 is F1100<bool>);
      Expect.equals(tIsDynamic, confuse(m1100) is F1100<bool>);
    } else {
      if (inCheckedMode) {
        Expect.throws(() { x1100 = (f1100 as dynamic); });
        Expect.throws(() { x1100 = confuse(f1100); });
        List<Function> Function(int y, [List<T> x]) Function() l1100;
        Expect.throws(() { l1100 = (f1100 as dynamic); });
        Expect.throws(() { l1100 = confuse(f1100); });
      }
      List<Function> Function(int y, [List<T> x]) Function() l1100 = m1100;
      // In checked mode, verifies the type.
      x1100 = m1100;
      x1100 = confuse(m1100);
    }
  }

  void testF1200() {
    // core.List<core.int> Function([Function x1]) Function()
    Expect.isTrue(f1200 is F1200);
    Expect.isTrue(confuse(f1200) is F1200);
    // In checked mode, verifies the type.
    core.List<core.int> Function([Function x1]) Function() l1200;
    // The static function f1200 sets `T` to `int`.
    if (!tIsBool) {
      x1200 = f1200 as dynamic;
      l1200 = f1200 as dynamic;
      x1200 = confuse(f1200);
      l1200 = confuse(f1200);
    }

    Expect.isTrue(m1200 is F1200);
    Expect.isTrue(m1200 is core.List<core.int> Function([Function x1]) Function());
    Expect.isTrue(confuse(m1200) is F1200);
    // In checked mode, verifies the type.
    x1200 = m1200;
    l1200 = m1200;
    x1200 = confuse(m1200);
    l1200 = confuse(m1200);

  }

  void testF1300() {
    // core.List<core.int> Function({core.List<core.int> x}) Function()
    Expect.isTrue(f1300 is F1300);
    Expect.isTrue(confuse(f1300) is F1300);
    // In checked mode, verifies the type.
    core.List<core.int> Function({core.List<core.int> x}) Function() l1300;
    // The static function f1300 sets `T` to `int`.
    if (!tIsBool) {
      x1300 = f1300 as dynamic;
      l1300 = f1300 as dynamic;
      x1300 = confuse(f1300);
      l1300 = confuse(f1300);
    }

    Expect.isTrue(m1300 is F1300);
    Expect.isTrue(m1300 is core.List<core.int> Function({core.List<core.int> x}) Function());
    Expect.isTrue(confuse(m1300) is F1300);
    // In checked mode, verifies the type.
    x1300 = m1300;
    l1300 = m1300;
    x1300 = confuse(m1300);
    l1300 = confuse(m1300);

  }

  void testF1400() {
    // List<T> Function(int y, {int x}) Function()
    Expect.isTrue(f1400 is F1400);
    Expect.isTrue(confuse(f1400) is F1400);
    // In checked mode, verifies the type.
    List<T> Function(int y, {int x}) Function() l1400;
    // The static function f1400 sets `T` to `int`.
    if (!tIsBool) {
      x1400 = f1400 as dynamic;
      l1400 = f1400 as dynamic;
      x1400 = confuse(f1400);
      l1400 = confuse(f1400);
    }

    Expect.isTrue(m1400 is F1400);
    Expect.isTrue(m1400 is List<T> Function(int y, {int x}) Function());
    Expect.isTrue(confuse(m1400) is F1400);
    // In checked mode, verifies the type.
    x1400 = m1400;
    l1400 = m1400;
    x1400 = confuse(m1400);
    l1400 = confuse(m1400);
    if (!tIsBool) {
      Expect.isTrue(f1400 is F1400<int>);
      Expect.isFalse(f1400 is F1400<bool>);
      Expect.isTrue(confuse(f1400) is F1400<int>);
      Expect.isFalse(confuse(f1400) is F1400<bool>);
      Expect.equals(tIsDynamic, m1400 is F1400<bool>);
      Expect.equals(tIsDynamic, confuse(m1400) is F1400<bool>);
    } else {
      if (inCheckedMode) {
        Expect.throws(() { x1400 = (f1400 as dynamic); });
        Expect.throws(() { x1400 = confuse(f1400); });
        List<T> Function(int y, {int x}) Function() l1400;
        Expect.throws(() { l1400 = (f1400 as dynamic); });
        Expect.throws(() { l1400 = confuse(f1400); });
      }
      List<T> Function(int y, {int x}) Function() l1400 = m1400;
      // In checked mode, verifies the type.
      x1400 = m1400;
      x1400 = confuse(m1400);
    }
  }

  void testF1500() {
    // List<T> Function(int x0, [core.List<core.int> x]) Function()
    Expect.isTrue(f1500 is F1500);
    Expect.isTrue(confuse(f1500) is F1500);
    // In checked mode, verifies the type.
    List<T> Function(int x0, [core.List<core.int> x]) Function() l1500;
    // The static function f1500 sets `T` to `int`.
    if (!tIsBool) {
      x1500 = f1500 as dynamic;
      l1500 = f1500 as dynamic;
      x1500 = confuse(f1500);
      l1500 = confuse(f1500);
    }

    Expect.isTrue(m1500 is F1500);
    Expect.isTrue(m1500 is List<T> Function(int x0, [core.List<core.int> x]) Function());
    Expect.isTrue(confuse(m1500) is F1500);
    // In checked mode, verifies the type.
    x1500 = m1500;
    l1500 = m1500;
    x1500 = confuse(m1500);
    l1500 = confuse(m1500);
    if (!tIsBool) {
      Expect.isTrue(f1500 is F1500<int>);
      Expect.isFalse(f1500 is F1500<bool>);
      Expect.isTrue(confuse(f1500) is F1500<int>);
      Expect.isFalse(confuse(f1500) is F1500<bool>);
      Expect.equals(tIsDynamic, m1500 is F1500<bool>);
      Expect.equals(tIsDynamic, confuse(m1500) is F1500<bool>);
    } else {
      if (inCheckedMode) {
        Expect.throws(() { x1500 = (f1500 as dynamic); });
        Expect.throws(() { x1500 = confuse(f1500); });
        List<T> Function(int x0, [core.List<core.int> x]) Function() l1500;
        Expect.throws(() { l1500 = (f1500 as dynamic); });
        Expect.throws(() { l1500 = confuse(f1500); });
      }
      List<T> Function(int x0, [core.List<core.int> x]) Function() l1500 = m1500;
      // In checked mode, verifies the type.
      x1500 = m1500;
      x1500 = confuse(m1500);
    }
  }

  void testF1600() {
    // Function(int x0) Function()
    Expect.isTrue(f1600 is F1600);
    Expect.isTrue(confuse(f1600) is F1600);
    // In checked mode, verifies the type.
    Function(int x0) Function() l1600;
    // The static function f1600 sets `T` to `int`.
    if (!tIsBool) {
      x1600 = f1600 as dynamic;
      l1600 = f1600 as dynamic;
      x1600 = confuse(f1600);
      l1600 = confuse(f1600);
    }

    Expect.isTrue(m1600 is F1600);
    Expect.isTrue(m1600 is Function(int x0) Function());
    Expect.isTrue(confuse(m1600) is F1600);
    // In checked mode, verifies the type.
    x1600 = m1600;
    l1600 = m1600;
    x1600 = confuse(m1600);
    l1600 = confuse(m1600);

  }

  void testF1700() {
    // Function(int x, [List<Function> x2]) Function()
    Expect.isTrue(f1700 is F1700);
    Expect.isTrue(confuse(f1700) is F1700);
    // In checked mode, verifies the type.
    Function(int x, [List<Function> x2]) Function() l1700;
    // The static function f1700 sets `T` to `int`.
    if (!tIsBool) {
      x1700 = f1700 as dynamic;
      l1700 = f1700 as dynamic;
      x1700 = confuse(f1700);
      l1700 = confuse(f1700);
    }

    Expect.isTrue(m1700 is F1700);
    Expect.isTrue(m1700 is Function(int x, [List<Function> x2]) Function());
    Expect.isTrue(confuse(m1700) is F1700);
    // In checked mode, verifies the type.
    x1700 = m1700;
    l1700 = m1700;
    x1700 = confuse(m1700);
    l1700 = confuse(m1700);

  }

  void testF1800() {
    // Function(int y, {List<T> x}) Function()
    Expect.isTrue(f1800 is F1800);
    Expect.isTrue(confuse(f1800) is F1800);
    // In checked mode, verifies the type.
    Function(int y, {List<T> x}) Function() l1800;
    // The static function f1800 sets `T` to `int`.
    if (!tIsBool) {
      x1800 = f1800 as dynamic;
      l1800 = f1800 as dynamic;
      x1800 = confuse(f1800);
      l1800 = confuse(f1800);
    }

    Expect.isTrue(m1800 is F1800);
    Expect.isTrue(m1800 is Function(int y, {List<T> x}) Function());
    Expect.isTrue(confuse(m1800) is F1800);
    // In checked mode, verifies the type.
    x1800 = m1800;
    l1800 = m1800;
    x1800 = confuse(m1800);
    l1800 = confuse(m1800);
    if (!tIsBool) {
      Expect.isTrue(f1800 is F1800<int>);
      Expect.isFalse(f1800 is F1800<bool>);
      Expect.isTrue(confuse(f1800) is F1800<int>);
      Expect.isFalse(confuse(f1800) is F1800<bool>);
      Expect.equals(tIsDynamic, m1800 is F1800<bool>);
      Expect.equals(tIsDynamic, confuse(m1800) is F1800<bool>);
    } else {
      if (inCheckedMode) {
        Expect.throws(() { x1800 = (f1800 as dynamic); });
        Expect.throws(() { x1800 = confuse(f1800); });
        Function(int y, {List<T> x}) Function() l1800;
        Expect.throws(() { l1800 = (f1800 as dynamic); });
        Expect.throws(() { l1800 = confuse(f1800); });
      }
      Function(int y, {List<T> x}) Function() l1800 = m1800;
      // In checked mode, verifies the type.
      x1800 = m1800;
      x1800 = confuse(m1800);
    }
  }

  void testF1900() {
    // void Function([List<Function> x]) Function()
    Expect.isTrue(f1900 is F1900);
    Expect.isTrue(confuse(f1900) is F1900);
    // In checked mode, verifies the type.
    void Function([List<Function> x]) Function() l1900;
    // The static function f1900 sets `T` to `int`.
    if (!tIsBool) {
      x1900 = f1900 as dynamic;
      l1900 = f1900 as dynamic;
      x1900 = confuse(f1900);
      l1900 = confuse(f1900);
    }

    Expect.isTrue(m1900 is F1900);
    Expect.isTrue(m1900 is void Function([List<Function> x]) Function());
    Expect.isTrue(confuse(m1900) is F1900);
    // In checked mode, verifies the type.
    x1900 = m1900;
    l1900 = m1900;
    x1900 = confuse(m1900);
    l1900 = confuse(m1900);

  }

  void testF2000() {
    // void Function(List<T> x0) Function()
    Expect.isTrue(f2000 is F2000);
    Expect.isTrue(confuse(f2000) is F2000);
    // In checked mode, verifies the type.
    void Function(List<T> x0) Function() l2000;
    // The static function f2000 sets `T` to `int`.
    if (!tIsBool) {
      x2000 = f2000 as dynamic;
      l2000 = f2000 as dynamic;
      x2000 = confuse(f2000);
      l2000 = confuse(f2000);
    }

    Expect.isTrue(m2000 is F2000);
    Expect.isTrue(m2000 is void Function(List<T> x0) Function());
    Expect.isTrue(confuse(m2000) is F2000);
    // In checked mode, verifies the type.
    x2000 = m2000;
    l2000 = m2000;
    x2000 = confuse(m2000);
    l2000 = confuse(m2000);
    if (!tIsBool) {
      Expect.isTrue(f2000 is F2000<int>);
      Expect.isFalse(f2000 is F2000<bool>);
      Expect.isTrue(confuse(f2000) is F2000<int>);
      Expect.isFalse(confuse(f2000) is F2000<bool>);
      Expect.equals(tIsDynamic, m2000 is F2000<bool>);
      Expect.equals(tIsDynamic, confuse(m2000) is F2000<bool>);
    } else {
      if (inCheckedMode) {
        Expect.throws(() { x2000 = (f2000 as dynamic); });
        Expect.throws(() { x2000 = confuse(f2000); });
        void Function(List<T> x0) Function() l2000;
        Expect.throws(() { l2000 = (f2000 as dynamic); });
        Expect.throws(() { l2000 = confuse(f2000); });
      }
      void Function(List<T> x0) Function() l2000 = m2000;
      // In checked mode, verifies the type.
      x2000 = m2000;
      x2000 = confuse(m2000);
    }
  }

  void testF2100() {
    // List<Function> Function<A>(Function x) Function()
    Expect.isTrue(f2100 is F2100);
    Expect.isTrue(confuse(f2100) is F2100);
    // In checked mode, verifies the type.
    List<Function> Function<A>(Function x) Function() l2100;
    // The static function f2100 sets `T` to `int`.
    if (!tIsBool) {
      x2100 = f2100 as dynamic;
      l2100 = f2100 as dynamic;
      x2100 = confuse(f2100);
      l2100 = confuse(f2100);
    }

    Expect.isTrue(m2100 is F2100);
    Expect.isTrue(m2100 is List<Function> Function<A>(Function x) Function());
    Expect.isTrue(confuse(m2100) is F2100);
    // In checked mode, verifies the type.
    x2100 = m2100;
    l2100 = m2100;
    x2100 = confuse(m2100);
    l2100 = confuse(m2100);

  }

  void testF2200() {
    // Function<A>(List<Function> x) Function()
    Expect.isTrue(f2200 is F2200);
    Expect.isTrue(confuse(f2200) is F2200);
    // In checked mode, verifies the type.
    Function<A>(List<Function> x) Function() l2200;
    // The static function f2200 sets `T` to `int`.
    if (!tIsBool) {
      x2200 = f2200 as dynamic;
      l2200 = f2200 as dynamic;
      x2200 = confuse(f2200);
      l2200 = confuse(f2200);
    }

    Expect.isTrue(m2200 is F2200);
    Expect.isTrue(m2200 is Function<A>(List<Function> x) Function());
    Expect.isTrue(confuse(m2200) is F2200);
    // In checked mode, verifies the type.
    x2200 = m2200;
    l2200 = m2200;
    x2200 = confuse(m2200);
    l2200 = confuse(m2200);

  }

  void testF2300() {
    // void Function<A>(core.List<core.int> x) Function()
    Expect.isTrue(f2300 is F2300);
    Expect.isTrue(confuse(f2300) is F2300);
    // In checked mode, verifies the type.
    void Function<A>(core.List<core.int> x) Function() l2300;
    // The static function f2300 sets `T` to `int`.
    if (!tIsBool) {
      x2300 = f2300 as dynamic;
      l2300 = f2300 as dynamic;
      x2300 = confuse(f2300);
      l2300 = confuse(f2300);
    }

    Expect.isTrue(m2300 is F2300);
    Expect.isTrue(m2300 is void Function<A>(core.List<core.int> x) Function());
    Expect.isTrue(confuse(m2300) is F2300);
    // In checked mode, verifies the type.
    x2300 = m2300;
    l2300 = m2300;
    x2300 = confuse(m2300);
    l2300 = confuse(m2300);

  }


}

void main() {
  new U0().runTests();
  new U0<int>(tIsInt: true).runTests();
  new U0<bool>(tIsBool: true).runTests();
}
    