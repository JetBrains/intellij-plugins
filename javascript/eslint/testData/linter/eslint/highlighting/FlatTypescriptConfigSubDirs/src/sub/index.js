console.log()


var foo = {
  bar: "baz",
  qux: "quux"<error descr="ESLint: Unexpected trailing comma. (comma-dangle)">,</error>
};

var a = `some` + `string`;