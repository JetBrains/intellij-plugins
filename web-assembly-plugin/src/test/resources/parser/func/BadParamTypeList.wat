;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func (param i32 i64 foo f32)))
(;; STDERR ;;;
out/test/parse/func/bad-param-type-list.txt:3:30: error: unexpected token foo, expected ).
(module (func (param i32 i64 foo f32)))
                             ^^^
;;; STDERR ;;)