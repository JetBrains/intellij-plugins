;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func br $n))
(;; STDERR ;;;
out/test/parse/expr/bad-br-name-undefined.txt:3:18: error: undefined label variable "$n"
(module (func br $n))
                 ^^
;;; STDERR ;;)
