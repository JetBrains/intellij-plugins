;;; TOOL: wat2wasm
;;; ERROR: 1
(module (export))
(;; STDERR ;;;
out/test/parse/module/bad-export-func-empty.txt:3:16: error: unexpected token ")", expected a quoted string (e.g. "foo").
(module (export))
               ^
;;; STDERR ;;)