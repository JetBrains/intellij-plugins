;;; TOOL: wat2wasm
;;; ARGS: --enable-simd
(module
  (func
    v128.const i32x4 0xff00ff01 0xff00ff0f 0xff00ffff 0xff00ff7f
    v128.const i32x4 0x00550055 0x00550055 0x00550055 0x00550155
    v8x16.shuffle 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
    drop
    ))
