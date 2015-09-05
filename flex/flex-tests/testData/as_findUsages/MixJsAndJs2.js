function foo() {
  var x;
  return x.value;
}
function bar() {
  foo().value = 1;
  return <div><value /></div>.value;
}

