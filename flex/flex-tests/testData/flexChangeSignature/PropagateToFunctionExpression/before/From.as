package {

class From {

  var f1 = function() { // propagate
    foo();
  }

  var f2 = function() {
    foo();
  }

  trace(function() {});

  function fo<caret>o() {
  }

  public function From() {
    f1();
    trace(f2);
  }
}

}