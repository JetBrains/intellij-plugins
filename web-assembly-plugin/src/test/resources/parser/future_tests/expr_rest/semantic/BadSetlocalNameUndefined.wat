;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func 
          i32.const 0
          set_local $n))
(;; STDERR ;;;
out/test/parse/expr/bad-setlocal-name-undefined.txt:5:21: error: undefined local variable "$n"
          set_local $n))
                    ^^
;;; STDERR ;;)
