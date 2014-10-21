#! program comment
library test;

// line comment
/// line doc comment

/**//***//***************/

/******
********/

/* * / * / * * * / / / ** // ** // ** ** ** // // // */

/**
**** doc

 *
 *
/*
*/
*/

/*
 comment
*/

/*//////*/
*/*/

/*/**//******//*/*
*/****/*/

/**  /*   */*  */

/*
/*/ * /*/ * * * / / / * / * /////*/
*/*/****////
*/

var a = "${{
// line comment
/// line doc comment

/**//***//***************/

/******
********/

/* * / * / * * * / / / ** // ** // ** ** ** // // // */

/**
 * doc
 */

/*
 comment
*/

/*//////*/
*/*/
}
/*/**//******//*/*
*/****/*/

/**  /*   */*  */

/*
/*/ * /*/ * * * / / / * / * /////*/
*/*/****////
*/
}}}";

class Logger {
  // Use display in a const expression:
  static const defaultLogger = const Logger(display);

  final logCallback;
  const Logger(this.logCallback);
}

abstract class A {}

class SomeClass {
  static final someConstant = 123;
  static final aConstList = const [someConstant];
}

main() {
  var on = r"set";
  on.split(" ");

  var as = r'not keyword';
  as.split(" ");

  try {
    dispatcher.on(()=> print(foo('a')));
  } catch(ex) {
  } catch(e, s) {
  } on IllegalArgumentException {
  } on IllegalArgumentException catch(e) {
  } on IllegalArgumentException catch(e, s) {}
  try {} finally {}
  try {} catch(e) {} finally {}
  try {} catch(e, s) {} finally {}
  try {} on IllegalArgumentException {} finally {}
  try {} on IllegalArgumentException catch(e) {} finally {}
  try {} on IllegalArgumentException catch(e, s) {} finally {}
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
