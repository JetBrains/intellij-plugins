;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func 
          i32.const 1
          set_global 0))
(;; STDERR ;;;
out/test/parse/expr/bad-setglobal-undefined.txt:5:22: error: global variable out of range: 0 (max 0)
          set_global 0))
                     ^
;;; STDERR ;;)
