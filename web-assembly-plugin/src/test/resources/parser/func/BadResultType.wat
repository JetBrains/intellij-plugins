;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func (result foo)))
(;; STDERR ;;;
out/test/parse/func/bad-result-type.txt:3:23: error: unexpected token foo, expected ).
(module (func (result foo)))
                      ^^^
;;; STDERR ;;)