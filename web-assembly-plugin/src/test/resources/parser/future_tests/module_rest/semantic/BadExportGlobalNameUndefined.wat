;;; TOOL: wat2wasm
;;; ERROR: 1
(module (export "foo" (global $bar)))
(;; STDERR ;;;
out/test/parse/module/bad-export-global-name-undefined.txt:3:31: error: undefined global variable "$bar"
(module (export "foo" (global $bar)))
                              ^^^^
;;; STDERR ;;)
