;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func
          f32.const 0 
          f32.converts.f64))
(;; STDERR ;;;
out/test/parse/expr/bad-convert-float-sign.txt:5:11: error: unexpected token f32.converts.f64, expected ).
          f32.converts.f64))
          ^^^^^^^^^^^^^^^^
;;; STDERR ;;)