;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func i32.const i32 100))
(module (func i64.const i32 100))
(module (func f32.const i32 100))
(module (func f64.const i32 100))
(;; STDERR ;;;
out/test/parse/expr/bad-const-type-i32-in-non-simd-const.txt:3:25: error: unexpected token "i32", expected a numeric literal (e.g. 123, -45, 6.7e8).
(module (func i32.const i32 100))
                        ^^^
out/test/parse/expr/bad-const-type-i32-in-non-simd-const.txt:4:25: error: unexpected token "i32", expected a numeric literal (e.g. 123, -45, 6.7e8).
(module (func i64.const i32 100))
                        ^^^
out/test/parse/expr/bad-const-type-i32-in-non-simd-const.txt:5:25: error: unexpected token "i32", expected a numeric literal (e.g. 123, -45, 6.7e8).
(module (func f32.const i32 100))
                        ^^^
out/test/parse/expr/bad-const-type-i32-in-non-simd-const.txt:6:25: error: unexpected token "i32", expected a numeric literal (e.g. 123, -45, 6.7e8).
(module (func f64.const i32 100))
                        ^^^
;;; STDERR ;;)