;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (type $t (func (param i32) (result f32)))
  (func (type $t) (param i32)))
(;; STDERR ;;;
out/test/parse/func/bad-sig-result-type-void.txt:5:4: error: expected 1 results, got 0
  (func (type $t) (param i32)))
   ^^^^
;;; STDERR ;;)
