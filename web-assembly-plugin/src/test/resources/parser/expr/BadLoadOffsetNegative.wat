;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func
          i32.const 0 
          i32.load8_s offset=-1))
(;; STDERR ;;;
out/test/parse/expr/bad-load-offset-negative.txt:5:23: error: unexpected token offset=-1, expected ).
          i32.load8_s offset=-1))
                      ^^^^^^^^^
;;; STDERR ;;)