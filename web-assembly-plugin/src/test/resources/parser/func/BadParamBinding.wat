;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func (param $bar $baz)))
(;; STDERR ;;;
out/test/parse/func/bad-param-binding.txt:3:27: error: unexpected token "$baz", expected i32, i64, f32, f64, v128 or externref.
(module (func (param $bar $baz)))
                          ^^^^
;;; STDERR ;;)