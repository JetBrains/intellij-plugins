;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func nop) (export "nop" (func 0 foo)))
(;; STDERR ;;;
out/test/parse/module/bad-export-func-too-many.txt:3:42: error: unexpected token foo, expected ).
(module (func nop) (export "nop" (func 0 foo)))
                                         ^^^
;;; STDERR ;;)