;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func
   i32.const 0 
   i32.add))
(;; STDERR ;;;
out/test/parse/expr/bad-binary-one-expr.txt:5:4: error: type mismatch in i32.add, expected [i32, i32] but got [i32]
   i32.add))
   ^^^^^^^
out/test/parse/expr/bad-binary-one-expr.txt:5:4: error: type mismatch in function, expected [] but got [i32]
   i32.add))
   ^^^^^^^
;;; STDERR ;;)