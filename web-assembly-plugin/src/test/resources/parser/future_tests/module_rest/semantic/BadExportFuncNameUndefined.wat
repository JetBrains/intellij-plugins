;;; TOOL: wat2wasm
;;; ERROR: 1
(module (export "foo" (func $foo)))
(;; STDERR ;;;
out/test/parse/module/bad-export-func-name-undefined.txt:3:29: error: undefined function variable "$foo"
(module (export "foo" (func $foo)))
                            ^^^^
;;; STDERR ;;)
