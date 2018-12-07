class C {
  var field;
}
class A extends C {
  var field;

  function aaa() {
    super.fi<caret>eld
  }
}

class B {
  var field;
}
