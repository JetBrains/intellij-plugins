;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func) (export "
(;; STDERR ;;;
out/test/parse/bad-string-eof.txt:3:25: error: newline in string
(module (func) (export "
                        ^
out/test/parse/bad-string-eof.txt:4:1: error: unexpected token "EOF", expected a quoted string (e.g. "foo").
;;; STDERR ;;)
