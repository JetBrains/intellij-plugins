var me = 1, that = "yep";

function f() {
  for (let <error descr="ESLint: Parsing error: Unexpected token i">i</error> in arguments) {
    console.log(arguments[i]);
  }
}