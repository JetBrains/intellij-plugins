;;; TOOL: wat2wasm
;;; ERROR: 1
(module (memory 100 foo))
(;; STDERR ;;;
out/test/parse/module/bad-memory-max-size.txt:3:21: error: unexpected token foo, expected ).
(module (memory 100 foo))
                    ^^^
;;; STDERR ;;)