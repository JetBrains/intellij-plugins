;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (start 0)
  (func (result i32)
    i32.const 0))
(;; STDERR ;;;
out/test/parse/module/bad-start-not-void.txt:4:4: error: start function must not return anything
  (start 0)
   ^^^^^
;;; STDERR ;;)
