// here we have to have a multiline error but we highlight only the first line
<error descr="ESLint: Unexpected var, use let or const instead. (no-var)">var a = function test() {</error>
  // body
  return 'some-string'
}

<error descr="ESLint: Unexpected var, use let or const instead. (no-var)">var b = function nextLine() { return 42}</error>