;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func 
          i32.const 0 
          i32.load8_s align=-1))
(;; STDERR ;;;
out/test/parse/expr/bad-load-align-negative.txt:5:23: error: unexpected token align=-1, expected ).
          i32.load8_s align=-1))
                      ^^^^^^^^
;;; STDERR ;;)