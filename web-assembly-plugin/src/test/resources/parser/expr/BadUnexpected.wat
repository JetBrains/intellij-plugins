;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func (module)))
(;; STDERR ;;;
out/test/parse/expr/bad-unexpected.txt:3:16: error: unexpected token "module", expected an instr.
(module (func (module)))
               ^^^^^^
;;; STDERR ;;)