;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (func
    i32.const 0
    i32.load aline=64))
(;; STDERR ;;;
out/test/parse/expr/bad-load-align-misspelled.txt:6:14: error: unexpected token aline=64, expected ).
    i32.load aline=64))
             ^^^^^^^^
;;; STDERR ;;)