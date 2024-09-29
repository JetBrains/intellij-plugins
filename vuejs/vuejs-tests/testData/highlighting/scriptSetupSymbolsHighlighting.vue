<<symbolName descr="Custom tag name">template</symbolName>>
  {{<inject descr="null"> <symbolName descr="instance field">newName</symbolName> </inject>}}
  {{<inject descr="null"> <symbolName descr="identifiers//global variable">window</symbolName>.<symbolName descr="instance field">name</symbolName> </inject>}}
  {{<inject descr="null"> <symbolName descr="identifiers//local function">foo12</symbolName>() </inject>}}
  {{<inject descr="null"> <symbolName descr="instance field">foo</symbolName> </inject>}}
</<symbolName descr="Custom tag name">template</symbolName>>

<<symbolName descr="Custom tag name">script</symbolName> setup>

import {ref} <info descr="null">from</info> "vue";

export const <symbolName descr="identifiers//exported variable">foo</symbolName> = 12

let <symbolName descr="identifiers//local variable">newName</symbolName> = ref(12)

function <symbolName descr="identifiers//local function">foo12</symbolName>() {
  let <symbolName descr="identifiers//local variable">bar</symbolName> = {<symbolName descr="instance field">a</symbolName>: 12}
  return <symbolName descr="identifiers//local variable">bar</symbolName>.<symbolName descr="instance field">a</symbolName>.<symbolName descr="instance method">toExponential</symbolName>()
}

</<symbolName descr="Custom tag name">script</symbolName>>
