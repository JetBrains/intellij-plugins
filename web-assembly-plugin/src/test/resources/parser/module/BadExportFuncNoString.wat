;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func nop) (export nop nop))
(;; STDERR ;;;
out/test/parse/module/bad-export-func-no-string.txt:3:28: error: unexpected token "nop", expected a quoted string (e.g. "foo").
(module (func nop) (export nop nop))
                           ^^^
;;; STDERR ;;)