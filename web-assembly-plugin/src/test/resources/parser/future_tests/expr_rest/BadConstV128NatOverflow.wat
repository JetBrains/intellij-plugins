;;; TOOL: wat2wasm
;;; ARGS: --enable-simd
;;; ERROR: 1
(module
  (func v128.const i32x4 0x12345678 0x123 4294967296 0xabcd3478))
(;; STDERR ;;;
out/test/parse/expr/bad-const-v128-nat-overflow.txt:5:43: error: invalid literal "4294967296"
  (func v128.const i32x4 0x12345678 0x123 4294967296 0xabcd3478))
                                          ^^^^^^^^^^
;;; STDERR ;;)
