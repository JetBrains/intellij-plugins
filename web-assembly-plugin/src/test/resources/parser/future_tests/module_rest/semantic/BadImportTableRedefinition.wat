;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (import "foo" "bar" (table $baz 0 anyfunc))
  (import "foo" "blah" (table $baz 0 anyfunc)))
(;; STDERR ;;;
out/test/parse/module/bad-import-table-redefinition.txt:5:4: error: redefinition of table "$baz"
  (import "foo" "blah" (table $baz 0 anyfunc)))
   ^^^^^^
;;; STDERR ;;)
