function foo() {
  var x;
  return x.value;
}
function bar() {
  foo().v<caret>alue = 1;
  return <div><value /></div>.value;
}

