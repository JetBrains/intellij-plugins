;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (func (param i32))
  (table anyfunc (elem 0))
  (table anyfunc (elem 0)))
(;; STDERR ;;;
out/test/parse/module/bad-table-too-many.txt:6:4: error: only one table allowed
  (table anyfunc (elem 0)))
   ^^^^^
;;; STDERR ;;)
