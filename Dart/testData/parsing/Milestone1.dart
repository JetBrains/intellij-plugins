#library('test');

// line comment
/// line doc comment

/*
    multi line comment
 */

/**
*
*/

class Logger {
  // Use display in a const expression:
  static const defaultLogger = const Logger(display);

  final logCallback;
  const Logger(this.logCallback);
}

abstract class A {
  abstract foo();

}

class SomeClass {
  static final someConstant = 123;
  static final aConstList = const [someConstant];
}

main() {
  var on = @"set";
  on.split(" ");

  var class = r"class";
  class.split(" ");

  var as = r'not keyword';
  as.split(" ");

  try {
    dispatcher.on(()=> print(foo('a')));
  } on IllegalArgumentException catch(ex) {

  }

}

foo(p){
  var a = p as int;
  return a + 3;
}

int get theAnswer => 42;

class MagicNumber {
  bool operator ==(other) => null;
  MagicNumber operator -() => null; // unary negate
  MagicNumber operator -(other) => null; // infix minus
}

display(arg) => print(arg);

enableFlags1([bool bold, bool hidden]) {
  // ...
}

enableFlags2(bool test, {bool bold, bool hidden: true}) {
  // ...
}
