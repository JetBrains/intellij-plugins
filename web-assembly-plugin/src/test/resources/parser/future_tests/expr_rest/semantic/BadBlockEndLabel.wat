;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (func
    block
    end $foo))
(;; STDERR ;;;
out/test/parse/expr/bad-block-end-label.txt:6:9: error: unexpected label "$foo"
    end $foo))
        ^^^^
;;; STDERR ;;)
