a = "${true?1:1}" // OK
a = "${"true"?1:1}" // OK
a = "${"false"?1:1}" // OK
a = "${${true}?1:1}" // OK, condition type is Boolean
a = "${${"x"}?1:1}" // OK, condition type is String but cannot evaluate value

a = "${<error descr="Condition should be boolean or string with boolean value">1</error>?1:1}" // BAD: Condition
a = "${<error descr="Condition should be boolean or string with boolean value">"x"</error>?1:1}" // BAD: Condition

a = "${true?"1":1}" // OK
a = "${true?1:"1"}" // OK

a = "${true?"true":true}" // OK
a = "${<error descr="Both branches are expected to have the same type. 'then' is number, 'else' is bool">true?1:false</error>}" // BAD: else type

// Unfinished
a = "${true?"true":<error descr="<expression> expected, got '}'">}</error>" // OK
a = "${true?<error descr="<expression> expected, got ':'">:</error>true}" // OK

// Variable
a = "${var.count ? 1 : 0}"

variable count {type = bool}