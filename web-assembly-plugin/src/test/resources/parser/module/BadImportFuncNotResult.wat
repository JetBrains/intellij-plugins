;;; TOOL: wat2wasm
;;; ERROR: 1
(module (import "foo" "bar" (func (param i32) (resalt i32))))
(;; STDERR ;;;
out/test/parse/module/bad-import-func-not-result.txt:3:48: error: unexpected token "resalt", expected param or result.
(module (import "foo" "bar" (func (param i32) (resalt i32))))
                                               ^^^^^^
;;; STDERR ;;)