;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func (local i32 i64 foo f32)))
(;; STDERR ;;;
out/test/parse/func/bad-local-type-list.txt:3:30: error: unexpected token foo, expected ).
(module (func (local i32 i64 foo f32)))
                             ^^^
;;; STDERR ;;)