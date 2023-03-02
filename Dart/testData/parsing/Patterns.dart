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
}