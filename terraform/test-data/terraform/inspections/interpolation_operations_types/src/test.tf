a = "${1 == 2}" // OK
a = "${"2" == 3}" // OK (implicit)
a = "${1 + 1}" // OK
a = "${1 + "1"}" // BAD
a = "${1 - "1"}" // BAD
a = "${-"1"}" // BAD
a = "${!1}" // BAD
a = "${11 || 22}" // BAD
a = "${"x" && true}" // BAD
a = "${!!true}" // OK
a = "${false < var.int}" // BAD (first)
a = "${var.bool || 11}" // BAD (second)

variable bool {type = bool}
variable int {type = number}