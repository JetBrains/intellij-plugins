;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (type $t (func (param i32) (result f32)))
  (func (type $t) (param i32) (result i64)))
(;; STDERR ;;;
out/test/parse/func/bad-sig-result-type-mismatch.txt:5:4: error: type mismatch for result 0 of function. got i64, expected f32
  (func (type $t) (param i32) (result i64)))
   ^^^^
;;; STDERR ;;)
