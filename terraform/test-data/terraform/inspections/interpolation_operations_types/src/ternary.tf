a = "${true?1:1}" // OK
a = "${"true"?1:1}" // OK
a = "${"false"?1:1}" // OK
a = "${${true}?1:1}" // OK, condition type is Boolean
a = "${${"x"}?1:1}" // OK, condition type is String but cannot evaluate value

a = "${1?1:1}" // BAD: Condition
a = "${"x"?1:1}" // BAD: Condition

a = "${true?"1":1}" // OK
a = "${true?1:"1"}" // OK

a = "${true?"true":true}" // OK
a = "${true?1:false}" // BAD: else type

// Unfinished
a = "${true?"true":}" // OK
a = "${true?:true}" // OK

// Variable
a = "${var.count ? 1 : 0}"

variable count {type = bool}