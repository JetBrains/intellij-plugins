;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func (param foo)))
(;; STDERR ;;;
out/test/parse/func/bad-param.txt:3:22: error: unexpected token foo, expected ).
(module (func (param foo)))
                     ^^^
;;; STDERR ;;)