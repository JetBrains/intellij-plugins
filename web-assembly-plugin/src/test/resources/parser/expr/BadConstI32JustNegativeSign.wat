;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (func i32.const -))
(;; STDERR ;;;
out/test/parse/expr/bad-const-i32-just-negative-sign.txt:4:19: error: unexpected token "-", expected a numeric literal (e.g. 123, -45, 6.7e8).
  (func i32.const -))
                  ^
;;; STDERR ;;)