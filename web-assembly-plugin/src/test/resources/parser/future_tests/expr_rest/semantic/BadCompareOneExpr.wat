;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func
          i32.const 0 
          i32.lt_s))
(;; STDERR ;;;
out/test/parse/expr/bad-compare-one-expr.txt:5:11: error: type mismatch in i32.lt_s, expected [i32, i32] but got [i32]
          i32.lt_s))
          ^^^^^^^^
out/test/parse/expr/bad-compare-one-expr.txt:5:11: error: type mismatch in function, expected [] but got [i32]
          i32.lt_s))
          ^^^^^^^^
;;; STDERR ;;)