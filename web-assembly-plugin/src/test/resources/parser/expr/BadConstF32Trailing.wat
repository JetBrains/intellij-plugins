;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func f32.const 1234.5678foo))
(;; STDERR ;;;
out/test/parse/expr/bad-const-f32-trailing.txt:3:25: error: unexpected token "1234.5678foo", expected a numeric literal (e.g. 123, -45, 6.7e8).
(module (func f32.const 1234.5678foo))
                        ^^^^^^^^^^^^
;;; STDERR ;;)