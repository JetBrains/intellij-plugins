;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (import "foo" "bar" (memory $baz 0))
  (import "foo" "blah" (memory $baz 0)))
(;; STDERR ;;;
out/test/parse/module/bad-import-memory-redefinition.txt:5:4: error: redefinition of memory "$baz"
  (import "foo" "blah" (memory $baz 0)))
   ^^^^^^
;;; STDERR ;;)
