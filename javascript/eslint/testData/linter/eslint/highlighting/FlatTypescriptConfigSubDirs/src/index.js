console.log()


var foo = {
  bar: "baz",
  qux: "quux",
};

var a = `some` <error descr="ESLint: Unexpected string concatenation of literals. (no-useless-concat)">+</error> `string`;