;;; TOOL: wat2wasm
;;; ARGS: --enable-simd
;;; ERROR: 1
(module
  (func
    v128.const i32x4 0xff00ff01 0xff00ff0f 0xff00ffff 0xff00ff7f
    v128.const i32x4 0x00550055 0x00550055 0x00550055 0x00550155
    v8x16.shuffle 1 1 1 1
    ))

(;; STDERR ;;;
out/test/parse/expr/bad-simd-shuffle-not-enough-indices.txt:9:5: error: unexpected token ")", expected a natural number in range [0, 32).
    ))
    ^
;;; STDERR ;;)
