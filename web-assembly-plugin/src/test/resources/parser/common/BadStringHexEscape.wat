;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func) (export "foo\az" (func 0)))
(;; STDERR ;;;
out/test/parse/bad-string-hex-escape.txt:3:28: error: bad escape "\a"
(module (func) (export "foo\az" (func 0)))
                           ^^
out/test/parse/bad-string-hex-escape.txt:3:24: error: unexpected token "Invalid", expected a quoted string (e.g. "foo").
(module (func) (export "foo\az" (func 0)))
                       ^^^^^^^^
out/test/parse/bad-string-hex-escape.txt:3:39: error: unexpected token 0, expected ).
(module (func) (export "foo\az" (func 0)))
                                      ^
;;; STDERR ;;)