;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (func    ;; depth 1 (implicit)
    block  ;; depth 0
      i32.const 0
      br_table 2
    end))
(;; STDERR ;;;
out/test/parse/expr/bad-brtable-bad-depth.txt:7:7: error: invalid depth: 2 (max 1)
      br_table 2
      ^^^^^^^^
;;; STDERR ;;)
