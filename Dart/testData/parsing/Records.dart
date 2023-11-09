var tuple = ("first", 2, true);
var record = (number: 123, name: "Main", type: "Street");
var record = (1, a: 2, 3, b: 4);

main() {
  var record = (1, a: 2, 3, b: 4);
  clientParams.forEach((k, v) => log.info()); // no records
  foo.catchError((ex, st) {}).then((List<Row> rows) {}); // no records
  print((1, 2, 3).toString()); // "(1, 2, 3)".
  print((a: 'str', 'int').toString()); // "(a: str, int)".

}

var number = (1); // The number 1.
var record = (1, ); // A record containing the number 1.
var t = (int, String);
var x = (a: say(1), b: say(2));

var tuple = const ("first", 2, true);
var record = const (number: 123, name: "Main", type: "Street");
var record = const (1, a: 2, 3, b: 4);

main() {
  var record = const (1, a: 2, 3, b: 4);
  print(const (1, 2, 3).toString()); // "(1, 2, 3)".
  print(const (a: 'str', 'int').toString()); // "(a: str, int)".
}

var record = const (1, ); // A record containing the number 1.
var t = const (int, String);
var x = const (a: say(1), b: say(2));

(int, ) main() => (1, );
(int, ) main() => (1, );
var a = 0;

(int, ) main() {
  return (1, );
}

void f((int, String) r) {
(f1: r.$0, );
(r.$0, );
}

final ({void Function() f1}) x = (f1: a);
final (void Function(), ) x = (a, );
final ({int f1}) x = (f1: a);
final (int, ) x = (a, );
final (A1, A2, A3, {A4 f1, A5 f2}) x = (g(), f1: g(), g(), f2: g(), g());
final ({int f1, String f2}) x = (f1: g(), f2: g());
final ({int f1, String f2}) x = (f2: g(), f1: g());
final Object x = (g(), g());
final (int, String) x = (g(), g());
final x = (0, f1: 1, 2, f2: 3, 4);
final x = (f1: 0, f2: true);
final x = (0, true);

({void Function() f1}) x = (f1: a);
(void Function(), ) x = (a, );
({@foo int f1}) x = (f1: a);
(@foo int, ) x = (a, );
(A1, A2, @foo A3, {A4 f1, @foo A5 f2}) x = (g(), f1: g(), g(), f2: g(), g());
({int f1, @foo() String f2}) x = (f1: g(), f2: g());
({int f1, String f2}) x = (f2: g(), f1: g());
Object x = (g(), g());
(int, @foo() String) x = (g(), g());

void someFunction({(int, int) x = (1, 2)}) => 1;
void foo((int, String) a) {}
(int, String) foo() => throw 0;
void g((int, String) a) {}
(int, String) g() => throw 0;
(int, String, {bool f3}) x;
({int f1, String f2}) x;
(int, String) x;
void f((int, String) a) {}
(int, String)? f() => throw 0;
(int, String) f() => throw 0;
final x = <(int, String)>[];

@metadata(a, b) function() {} // metadata with args
@metadata Type function() {}
@metadata (a, b) function() {} // record
@metadata/**/(a, b) function() {} // record
@metadata () function() => ""; // record
@metadata() function() => ""; // args

class BugReport {
  Option<(String, String)> _maybeTupple;

  set maybeTuple(Option<(String, String)> value) {
    _maybeTupple = value;
  }

  Option<String> get first => _maybeTupple.map((x) {
        var (first, _) = x;
        return first;
      });

  Option<String> get second => _maybeTupple.map((x) {
        var (_, second) = x;
        return second;
      });

  BugReport() : _maybeTupple = Option.none();
}