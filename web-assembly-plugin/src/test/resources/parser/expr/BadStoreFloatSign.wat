;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func
          i32.const 0
          f32.const 0 
          f32.storeu))
(;; STDERR ;;;
out/test/parse/expr/bad-store-float.sign.txt:6:11: error: unexpected token f32.storeu, expected ).
          f32.storeu))
          ^^^^^^^^^^
;;; STDERR ;;)