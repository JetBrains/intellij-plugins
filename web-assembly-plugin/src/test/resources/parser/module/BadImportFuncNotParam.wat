;;; TOOL: wat2wasm
;;; ERROR: 1
(module (import "foo" "bar" (func (parump i32))))
(;; STDERR ;;;
out/test/parse/module/bad-import-func-not-param.txt:3:36: error: unexpected token "parump", expected param or result.
(module (import "foo" "bar" (func (parump i32))))
                                   ^^^^^^
;;; STDERR ;;)