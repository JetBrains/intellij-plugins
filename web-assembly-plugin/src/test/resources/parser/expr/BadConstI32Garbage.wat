;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func i32.const one-hundred))
(;; STDERR ;;;
out/test/parse/expr/bad-const-i32-garbage.txt:3:25: error: unexpected token "one-hundred", expected a numeric literal (e.g. 123, -45, 6.7e8).
(module (func i32.const one-hundred))
                        ^^^^^^^^^^^
;;; STDERR ;;)