a = "${1 == 2}" // OK
a = "${"2" == 3}" // OK (implicit)
a = "${1 + 1}" // OK
a = "${1 + <error descr="Number expected instead of string">"1"</error>}" // BAD
a = "${1 - <error descr="Number expected instead of string">"1"</error>}" // BAD
a = "${-<error descr="Number expected instead of string">"1"</error>}" // BAD
a = "${!<error descr="Boolean expected instead of number">1</error>}" // BAD
a = "${<error descr="Boolean expected instead of number">11</error> || <error descr="Boolean expected instead of number">22</error>}" // BAD
a = "${<error descr="Boolean expected instead of string">"x"</error> && true}" // BAD
a = "${!!true}" // OK
a = "${<error descr="Number expected instead of bool">false</error> < var.int}" // BAD (first)
a = "${var.bool || <error descr="Boolean expected instead of number">11</error>}" // BAD (second)

variable bool {type = bool}
variable int {type = number}