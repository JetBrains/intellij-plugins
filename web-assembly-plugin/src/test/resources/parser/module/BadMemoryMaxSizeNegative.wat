;;; TOOL: wat2wasm
;;; ERROR: 1
(module (memory 100 -5))
(;; STDERR ;;;
out/test/parse/module/bad-memory-max-size-negative.txt:3:21: error: unexpected token -5, expected ).
(module (memory 100 -5))
                    ^^
;;; STDERR ;;)