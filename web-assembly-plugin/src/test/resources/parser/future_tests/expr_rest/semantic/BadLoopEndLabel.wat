;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (func
    loop
    end $foo))
(;; STDERR ;;;
out/test/parse/expr/bad-loop-end-label.txt:6:9: error: unexpected label "$foo"
    end $foo))
        ^^^^
;;; STDERR ;;)