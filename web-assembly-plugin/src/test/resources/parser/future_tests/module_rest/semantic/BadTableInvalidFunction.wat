;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (table 1 anyfunc)
  (func $f)
  (elem (i32.const 0) $f 1))
(;; STDERR ;;;
out/test/parse/module/bad-table-invalid-function.txt:6:26: error: function variable out of range: 1 (max 1)
  (elem (i32.const 0) $f 1))
                         ^
;;; STDERR ;;)
