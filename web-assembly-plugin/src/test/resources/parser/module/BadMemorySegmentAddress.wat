;;; TOOL: wat2wasm
;;; ERROR: 1
;;; ARGS: --enable-bulk-memory
(module
  (memory 100)
  (data foo))
(;; STDERR ;;;
out/test/parse/module/bad-memory-segment-address.txt:6:9: error: unexpected token foo, expected ).
  (data foo))
        ^^^
;;; STDERR ;;)