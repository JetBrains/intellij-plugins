;;; TOOL: wat2wasm
;;; ARGS: --enable-gc
;;; ERROR: 1
(type (array))
(;; STDERR ;;;
out/test/parse/module/bad-array-no-fields.txt:4:13: error: unexpected token ")", expected i32, i64, f32, f64, v128 or externref.
(type (array))
            ^
;;; STDERR ;;)
