package {

class From {

  var f1 = function (i:int) { // propagate
    foo(i);
  }

  var f2 = function() {
    foo(100);
  }

  trace(function() {});

  function foo(i:int) {
  }

  public function From() {
    f1(100);
    trace(f2);
  }
}

}