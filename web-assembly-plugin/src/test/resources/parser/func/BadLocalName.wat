;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func (local 0 i32)))
(;; STDERR ;;;
out/test/parse/func/bad-local-name.txt:3:22: error: unexpected token 0, expected ).
(module (func (local 0 i32)))
                     ^
;;; STDERR ;;)