;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func
          i32.const 0
          f32.loads))
(;; STDERR ;;;
out/test/parse/expr/bad-load-float-sign.txt:5:11: error: unexpected token f32.loads, expected ).
          f32.loads))
          ^^^^^^^^^
;;; STDERR ;;)