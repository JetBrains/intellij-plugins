;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func
          i32.const 0
          f32.const 0 
          store.float32))
(;; STDERR ;;;
out/test/parse/expr/bad-store-type.txt:6:11: error: unexpected token store.float32, expected ).
          store.float32))
          ^^^^^^^^^^^^^
;;; STDERR ;;)