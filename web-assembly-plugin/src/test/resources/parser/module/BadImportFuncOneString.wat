;;; TOOL: wat2wasm
;;; ERROR: 1
(module (import "foo" (param i32)))
(;; STDERR ;;;
out/test/parse/module/bad-import-func-one-string.txt:3:23: error: unexpected token "(", expected a quoted string (e.g. "foo").
(module (import "foo" (param i32)))
                      ^
;;; STDERR ;;)