;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (func
    block
      br
    end))
(;; STDERR ;;;
out/test/parse/expr/bad-br-no-depth.txt:7:5: error: unexpected token "end", expected a numeric index or a name (e.g. 12 or $foo).
    end))
    ^^^
;;; STDERR ;;)