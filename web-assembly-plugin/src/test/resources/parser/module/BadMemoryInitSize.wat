;;; TOOL: wat2wasm
;;; ERROR: 1
(module (memory foo))
(;; STDERR ;;;
out/test/parse/module/bad-memory-init-size.txt:3:17: error: unexpected token "foo", expected a natural number (e.g. 123).
(module (memory foo))
                ^^^
;;; STDERR ;;)