;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func (local $n)))
(;; STDERR ;;;
out/test/parse/func/bad-local-binding-no-type.txt:3:24: error: unexpected token ")", expected i32, i64, f32, f64, v128 or externref.
(module (func (local $n)))
                       ^
;;; STDERR ;;)