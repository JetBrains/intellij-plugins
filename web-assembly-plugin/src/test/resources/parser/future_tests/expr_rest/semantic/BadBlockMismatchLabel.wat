;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (func
    block $foo
    end $bar))
(;; STDERR ;;;
out/test/parse/expr/bad-block-mismatch-label.txt:6:9: error: mismatching label "$foo" != "$bar"
    end $bar))
        ^^^^
;;; STDERR ;;)
