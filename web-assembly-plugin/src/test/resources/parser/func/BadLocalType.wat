;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func (local foo)))
(;; STDERR ;;;
out/test/parse/func/bad-local-type.txt:3:22: error: unexpected token foo, expected ).
(module (func (local foo)))
                     ^^^
;;; STDERR ;;)