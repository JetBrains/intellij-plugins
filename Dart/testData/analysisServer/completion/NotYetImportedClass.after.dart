import 'dart:async' as prefix0;

class A extends prefix0.SynchronousStreamController<caret>

SynchronousStreamController(){} // it's here to test that 'dart.async' will be imported with prefix
