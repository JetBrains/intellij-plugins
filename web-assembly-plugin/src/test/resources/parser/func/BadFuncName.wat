;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (func foo))
(;; STDERR ;;;
out/test/parse/func/bad-func-name.txt:4:9: error: unexpected token foo, expected ).
  (func foo))
        ^^^
;;; STDERR ;;)