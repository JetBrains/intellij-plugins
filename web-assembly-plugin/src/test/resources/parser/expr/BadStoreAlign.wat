;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func
          i32.const 0 
          i32.const 0
          i32.store align=foo))
(;; STDERR ;;;
out/test/parse/expr/bad-store-align.txt:6:21: error: unexpected token align=foo, expected ).
          i32.store align=foo))
                    ^^^^^^^^^
;;; STDERR ;;)