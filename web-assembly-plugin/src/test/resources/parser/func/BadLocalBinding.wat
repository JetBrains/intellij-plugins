;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func (local $foo $bar)))
(;; STDERR ;;;
out/test/parse/func/bad-local-binding.txt:3:27: error: unexpected token "$bar", expected i32, i64, f32, f64, v128 or externref.
(module (func (local $foo $bar)))
                          ^^^^
;;; STDERR ;;)