;;; TOOL: wat2wasm
;;; ERROR: 1
(module (memory -100))
(;; STDERR ;;;
out/test/parse/module/bad-memory-init-size-negative.txt:3:17: error: unexpected token "-100", expected a natural number (e.g. 123).
(module (memory -100))
                ^^^^
;;; STDERR ;;)