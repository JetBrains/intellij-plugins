;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (type $t (func (param i32)))
  (func (type $t) (param i32 i32)))
(;; STDERR ;;;
out/test/parse/func/bad-sig-too-many-params.txt:5:4: error: expected 1 arguments, got 2
  (func (type $t) (param i32 i32)))
   ^^^^
;;; STDERR ;;)
