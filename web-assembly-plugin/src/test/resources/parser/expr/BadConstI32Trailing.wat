;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func i32.const 100x))
(;; STDERR ;;;
out/test/parse/expr/bad-const-i32-trailing.txt:3:25: error: unexpected token "100x", expected a numeric literal (e.g. 123, -45, 6.7e8).
(module (func i32.const 100x))
                        ^^^^
;;; STDERR ;;)