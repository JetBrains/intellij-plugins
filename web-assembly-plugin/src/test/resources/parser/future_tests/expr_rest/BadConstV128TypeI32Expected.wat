;;; TOOL: wat2wasm
;;; ARGS: --enable-simd
;;; ERROR: 1
(module (func v128.const 0x12345678 0x00000000 0x00000000 0xabcd3478))
(module (func v128.const i64 0x12345678 0x00000000 0x00000000 0xabcd3478))
(;; STDERR ;;;
out/test/parse/expr/bad-const-v128-type-i32-expected.txt:4:26: error: Unexpected type at start of simd constant. Expected one of: i8x16, i16x8, i32x4, i64x2, f32x4, f64x2. Found "NAT".
(module (func v128.const 0x12345678 0x00000000 0x00000000 0xabcd3478))
                         ^^^^^^^^^^
;;; STDERR ;;)
