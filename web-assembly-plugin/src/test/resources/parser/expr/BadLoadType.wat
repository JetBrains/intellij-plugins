;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func
          i32.const 0
          load.x32))
(;; STDERR ;;;
out/test/parse/expr/bad-load-type.txt:5:11: error: unexpected token load.x32, expected ).
          load.x32))
          ^^^^^^^^
;;; STDERR ;;)