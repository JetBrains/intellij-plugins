;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func) (export "foobar\x\n" (func 0)))
(;; STDERR ;;;
out/test/parse/bad-string-escape.txt:3:31: error: bad escape "\x"
(module (func) (export "foobar\x\n" (func 0)))
                              ^^
out/test/parse/bad-string-escape.txt:3:24: error: unexpected token "Invalid", expected a quoted string (e.g. "foo").
(module (func) (export "foobar\x\n" (func 0)))
                       ^^^^^^^^^^^^
out/test/parse/bad-string-escape.txt:3:43: error: unexpected token 0, expected ).
(module (func) (export "foobar\x\n" (func 0)))
                                          ^
;;; STDERR ;;)