main() {
  final (a as A || b? && c! || == d && != e << f >>> g >> h & i ^ j | k-- + ~l++ - !m * +n / -o % ++p ~/ --r ) = null;

  @foo() final (lat, long) = geoCode('Aarhus');
  final ((a, b) && record) = (1, 2); // Parentheses.
  var [a, b] = [1, 2]; // List.
  var {1: a, 2: b} = {1: 2}; // Map.
  var (a, b, x: x) = (1, 2, x: 3); // Record.
  var Point(x: x, y: y) = Point(1, 2); // Object.

  for (@foo final (lat, long) in bar) {}
  for (@foo final ((a, b) && record) in bar) {}
  for (@foo var [a, b] in bar) {}
  for (var [a, b] in bar) {}
  for (var {1: a} in bar) {}
  for (var (a, b, x: x) in bar) {}
  for (var Point.a(x: x, y: y) in bar) {}

  (notFinal, unassignedFinal, lateFinal) = ('a', 'b', 'c');
  ((a, b) && record) = 1;
  (a && a) = 2;
  [a, b] = [a, b];
  {1: a, 2: b} = {1: 2};
  Point(x: x, y: y) = Point(1, 2);
}

foo() {
  switch (shape) {
    case Square(size: var s) || Circle(size: var s) when s > 0:
      print('Non-empty symmetric shape');
    case Square() || Circle():
      print('Empty symmetric shape');
    default:
      print('Asymmetric shape');
  }

  switch (list) {
    case ['a' || 'b', var c]:
    case {1: a, 2: b} when 1 == 2:
    case (a as A || b? && c! || == d && != e << f >>> g >> h & i ^ j | k-- + ~l++ - !m * +n / -o % ++p ~/ --r):
    case 5 + 5 * 2:
    case a + a:
    case -a:
    case (a, b, x: x):
    case (5 + 5 * 2):
    case const [a, b]:
    case SomeClass(1, 2):
    case const SomeClass(1, 2):
    case const (A + A):
    case const (A + 'b'):
    case const (-ER):
    case const (List<RPChoice>):
    case const (720 * 1280):
    case a when b:
    case a when b > c:
    case var s?: foo();

  }
}

bar() {
  var [...] = [1, 2];
  var [...x] = [1, 2];
  var [int a, num b] = [1, 2];
  var <int>[a, b] = <num>[1, 2]; // List<int> (and compile error).
  var [a, b] = <num>[1, 2]; // List<num>, a is num, b is num.
  var [int a, b] = <num>[1, 2]; // List<num>.
  var (int a, int b) = null;
  var (int? a, int? b) = (null, null);

  switch ((1, 2)) {
    case Square(size: var s) || Circle(size: var s) when s > 0:
    case (int a, int b) when a > b:
    // case (int a, int b) foo:
      print('First element greater');
      break;
    case (int a, int b):
      print('Other order');
      break;
    case [int a, int n] when n > 0:
    case {"a": int a}:
  }

  switch (n) {}
  var a = switch (n) {} //error
}

iff() {
  if (json case [int x, int y]);
  if (json case [int x, int y] when x == y);
  if (x case int(isEven: true));
  switch (n) {
    1 => a,
    2 =>> recover,
    _ => c
  }
}


// Define multiple returns
(double, double) geoCode(String city) {
  var lat = city == 'Aarhus' ? 56.1629 : 0.0;
  var long = city == 'Aarhus' ? 10.2039 : 0.0;
  return (lat, long);
}

void f00() {
  {
  // Use multiple returns
  final (lat, long) = geoCode('Aarhus');
  print('Location lat:$lat, long:$long');
  }
  {
  // Destructure list
  var list = [1, 2, 3];
  var [a, b, c] = list;
  print(a + b + c); // 6.
  }
  {
  // Destructure map
  var map = {'first': 1, 'second': 2};
  var {'first': a, 'second': b} = map;
  print(a + b); // 3.
  }
  {
  // Destructure and assign to existing variables
  var (a, b) = ('left', 'right');
  (b, a) = (a, b); // Swap!
  print('$a $b'); // Prints "right left".
  }
}

abstract class Shape {
  double get size;
}
class Square implements Shape {
  final double length;
  const Square(this.length);
  @override
  double get size => length;
}
class Circle implements Shape {
  final double radius;
  Circle(this.radius);
  @override
  double get size => radius;
}

double calculateArea(Shape shape) =>
  // Switch wth object patterns, from algebraic datatype example
  switch (shape) {
    Square(length: var l) => l * l,
    Circle(radius: var r) => math.pi * r * r
  };

late Color color;
var isPrimary = switch (color) {
  // Simple logical-or pattern
  Colors.red || Colors.yellow || Colors.blue => true,
  _ => false
};

void describeShape(Shape shape) {
  // Logical-or pattern where multiple patterns share a guard
  switch (shape) {
    case Square(size: var s) || Circle(size: var s) when s > 0:
      print('Non-empty symmetric shape');
    case Square() || Circle():
      print('Empty symmetric shape');
    default:
      print('Asymmetric shape');
  }
}

String? describeList(List<String> list) {
  // Destructuring logical-or pattern in switch statement
  switch(list) {
    case ['a' || 'b', var c]:
      return 'a or b and $c';
    default:
      return null;
  }
}

bool isAorBandAnother(List<String?> list) {
  // Destructuring logical-or pattern in switch expression
  return switch(list) {
    ['a' || 'b', var c] => c != null,
    _ => false
  };
}

String asciiCharType(int char) {
  const space = 32;
  const zero = 48;
  const nine = 57;

  // Relational pattern
  return switch(char) {
    == space => 'space', // Analyzer reports all below is dead code. 6Feb23
    < space => 'control',
    > space && < zero => 'punctuation',
    >= zero && <= nine => 'digit'
    // Etc...
  };
}

void f01() {
  // Cast pattern
  (num, Object) record = (1, 's');
  var (i as int, s as String) = record;
  print('$i $s');

  // Null-check pattern
  String? maybeString = describeList([]);
  switch (maybeString) {
    case var s?:
      // s has type non-nullable String here.
      geoCode(s);
  }

  (int?, int?) getPair(String s) => s.length > 2 ? (1,2) : (null, null);

  // Null-assert pattern
  var pair = getPair('test');
  var (lat!, long!) = pair;
  print('Location lat:$lat, long:$long');
  List<String?> row = ['user', 'Abe'];
  switch (row) {
    case ['user', var name!]:
      // name is a non-nullable string here.
      geoCode(name);
  }
}

String f02(Object selector) {
  // Constant patterns
  switch (selector) {
    case true || false : return 'bool';
    case 1 || -1 : return 'one';
    case math.pi : return 'qualified name';
    case ['a', 'b'] : return 'list literal';
    case const Square(3) : return 'constructor';
    case const (3 + 4) : return 'const expr';
  }
  return '';
}

void f03(Object record) {
  // Variable patterns
  switch ((1, 2)) {
    case (var a, var b): print('$a $b');
  }
  switch (record) {
    case (int x, String s):
      print('First field is int $x and second is String $s.');
    case (String _, int _):
      print('First field is String and second is int');
    case (var n, final m):
      print('$n $m');
  }
}

// Parenthesized patterns
void f05(List<String> l) {
  switch (l) {
    case ('a' || 'b'): break;
  }
}

// Rest elements and type annotations
void f06() {
  var [a, b, ...rest, c, d] = <int>[1, 2, 3, 4, 5, 6, 7];
  print('$a $b $rest $c $d'); // Prints "1 2 [3, 4, 5] 6 7".
  var map = <String, int>{'first': 1, 'second': 2, 'third': 3};
  var {'first': x, 'second': y, ...} = map;
  print(x + y);
}

// Record patterns
void f07() {
  // Variable:
  var (untyped: untyped1, typed: int typed1) = (untyped: 0, typed: 1);
  var (: untyped, :int typed) = (untyped: 0, typed: 1);
  print('$untyped1 $untyped $typed1 $typed');

  var obj = (untyped: 1, typed: 2);
  switch (obj) {
    case (untyped: var untyped, typed: int typed):
    // ignore: dead_code, unreachable_switch_case
    case (:var untyped, :int typed): print('$untyped $typed');
  }

  // Null-check and null-assert:
  switch (obj) {
    case (checked: var checked?, asserted: var asserted!):
    case (:var checked?, :var asserted!): print('$checked $asserted');
  }

  // Cast:
  var (field: field1 as int) = (field: 1.0);
  var (:field as int) = (field: 1.0);
  print('$field1 $field');
}

// Object pattern with getter omitted
void f08(Shape shape) {
  switch (shape) {
    case Square(: var size) : print(size);
  }
}

void f09() {
  var Point(x: x, y: y) = const Point(1, 2);
  print('$x $y');
  // Destructure while binding entire record
  var ((a, b) && record) = (1, 2);
  print('$a $b $record'); // Prints "1 2 (1, 2)"
}

// For-loop parts
void fibonacciTheHardWay() {
  var fns = <Function()>[];
  for (var (a, b) = (0, 1); a <= 13; (a, b) = (b, a + b)) {
    fns.add(() {
      print(a);
    });
  }
  for (var fn in fns) {
    fn();
  }
}

// from "pattern assignment"
void test(int parameter) {
  String notFinal;
  final String unassignedFinal;
  late final String lateFinal;

  if (isAorBandAnother([])) lateFinal = 'maybe assigned';

  (notFinal, unassignedFinal, lateFinal) = ('a', 'b', 'c');
  print('$notFinal $unassignedFinal $lateFinal');
}

void f10() {
  var map = {'a': 1, 'b': 2};
  int a, b;
  {'a': a, 'b': b} = map;
  print('$a $b');
}

void f11() {
  const a = 1;
  const b = 2;
  var obj = [1, 2]; // Not const.
  switch (obj) {
    case [a, b]: print('match'); // new
    default: print('no match'); // old
  }
}

class Rect {
  final double width, height;

  Rect(this.width, this.height);
}
void display(Object obj) {
  switch (obj) {
    // Object pattern with getter name omitted
    case Rect(:var width, :var height): print('Rect $width x $height');
    default: print(obj);
  }
}

void f12() {
  // pattern variable declaration
  var (a, [b, c]) = ('str', [1, 2]);
  print('$a $b $c');
}

void f13(Object pair) {
  // guard clauses
  switch (pair) {
    case (int a, int b) when a > b:
      print('First element greater');
    case F when a == b:
      print('Both elements equal');
    case (int a, int b) when a < b:
      print('Second element greater');
  }
}

// switch expression precedence
void f14(int n, Future<int> a, Future<int> b, Future<int> c) async {
  await switch (n) {
    1 => a,
    2 => b,
    _ => c
  };
}

void f15(int n, Object a, Object b, Object c) {
  var x = switch (n) {
    2 => b,
    q when a > b => a,
    _ => c
  }.toString();
  print(x);
}

// guard expression that ends in a function expression
void f16(Object obj, bool b, int c) {
  var x = switch (obj) {
    _ when ((a) => b)(null) => c
  };
  print(x);
}

// if-case
void f17(List<int> json) {
  if (json case [int x, int y]) {
    print('$x $y');
  } else {
    throw const FormatException('Invalid JSON');
  }
  if (json case [int x, int y] when x == y) {
    print('Was on coordinate x-y intercept');
  } else {
    throw const FormatException('Invalid JSON.');
  }
  var list = [if (json case [int x, int y] when x == y) 3];
  print(list);
}

// type substitution and rest elements
void f18() {
  T id<T>(T t) => t;
  (T Function<T>(T), dynamic) record = (id, 'str');
  var (int Function<int>(int) f, String s) = record;
  var [...] = [1, 2];
  var [...x] = [1, 2];
  print('$f $s $x');
}

// constant patterns for user-defined types; error in old Dart
class A {}
class B { const B(); }

void f19(A a) {
  switch (A()) {
    case const B(): print('hi');
  }
}

// coercions only in irrefutable contexts
void f20() {
  dynamic d = 1;
  if (d case String s) print('then $s'); else print('else');
}

// shared case scope
void f21(Object obj) {
  switch (obj) {
    case [int a, int n] when n > 0:
    case {'a': int a}:
      print(a.abs()); // OK.
  }
}
String f22() {
  late Function captured;

  bool capture(Function closure) {
    captured = closure;
    return true;
  }

  switch (['before']) {
    case [String a] when capture(() => print(a)):
    case [_, String a]:
      a = 'after';
      captured();
      return a;
  }
  return '';
}
void f23(Object obj) {
  switch (obj) {
    case [var a, int n] when n > 1:
    case [var a, double n] when n > 1.0:
    case [var a, String s] when s.isNotEmpty:
      print(a);
  }
  if (xhr.responseHeaders['content-length'] case final contentLengthHeader
      when contentLengthHeader != null &&
          !_digitRegex.hasMatch(contentLengthHeader)) {
    completer.completeError(ClientException(
      'Invalid content-length header [$contentLengthHeader].',
      request.url,
    ));
    return;
  }
}

void f24() {
  var list = [1, 2];
  switch (list) {
    case [1, _] && [_, < 4]: print('first');
    case [int(isEven: true), var a]: print('second $a');
  }
}

enum SomeEnum {foo}
String enumSwitchCase(SomeEnum someEnum) {
  // from https://github.com/dart-lang/sdk/issues/51320
  return switch (someEnum) { SomeEnum.foo => 'foo'};
}

class Color {
  const Color();
}
class Colors {
  static const red = Color();
  static const yellow = Color();
  static const blue = Color();
}