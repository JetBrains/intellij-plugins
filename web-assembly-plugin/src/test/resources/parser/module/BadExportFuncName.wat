;;; TOOL: wat2wasm
;;; ERROR: 1
(module (export "foo" (func foo)))
(;; STDERR ;;;
out/test/parse/module/bad-export-func-name.txt:3:29: error: unexpected token "foo", expected a numeric index or a name (e.g. 12 or $foo).
(module (export "foo" (func foo)))
                            ^^^
;;; STDERR ;;)