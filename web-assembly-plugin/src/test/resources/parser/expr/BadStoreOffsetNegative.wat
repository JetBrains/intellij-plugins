;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func 
          i32.const 0
          i32.const 0
          i32.store8 offset=-1))
(;; STDERR ;;;
out/test/parse/expr/bad-store-offset-negative.txt:6:22: error: unexpected token offset=-1, expected ).
          i32.store8 offset=-1))
                     ^^^^^^^^^
;;; STDERR ;;)