;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (table funcref (elem garbage)))
(;; STDERR ;;;
out/test/parse/module/bad-table-elem.txt:4:24: error: unexpected token garbage, expected ).
  (table funcref (elem garbage)))
                       ^^^^^^^
;;; STDERR ;;)