;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func 
          nop
          foo))
(;; STDERR ;;;
out/test/parse/expr/bad-nop.txt:5:11: error: unexpected token foo, expected ).
          foo))
          ^^^
;;; STDERR ;;)