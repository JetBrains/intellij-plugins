class Test {

  static staticMethod() {}
  static get staticGetter => null;
  static set staticSetter(x) {}

  baseMethod() {}
  get baseGetter => null;
  set baseSetter(x) {}

  examplesFromDEP() {
    var x, y;

    x = new List#;

    x = new DateTime#.utc;

    x = new List<String>#;

    x = new Map<String,int>#;

    x = new Map<Symbol,Type>#.from;

    y = new List<String>##call; // same as (new List<String>#)#call
    y = (new List<String>#)#call;
  }
}

class SubTest extends Test {

  instanceMethod() {}
  get instanceGetter => null;
  set instanceSetter(x) {}

  operator *(q) => null;

  examplesLikeDEP() {
    var o = new SubTest(), x;

    x = o#instanceMethod;
    x = o#instanceGetter;
    o#instanceSetter = null;

    x = Test#staticMethod;
    x = Test#staticGetter;
    Test#staticSetter = null;

    x = super#baseMethod;
    x = super#baseGetter;
    super#baseSetter = null;

    x = o#*;
  }
}
