;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func
          i32.const 
          i32.convert.i32))
(;; STDERR ;;;
out/test/parse/expr/bad-convert-int-no-sign.txt:5:11: error: unexpected token "i32.convert.i32", expected a numeric literal (e.g. 123, -45, 6.7e8).
          i32.convert.i32))
          ^^^^^^^^^^^^^^^
;;; STDERR ;;)