;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func 
          block $foo 
            br foo
          end))
(;; STDERR ;;;
out/test/parse/expr/bad-br-name.txt:5:16: error: unexpected token "foo", expected a numeric index or a name (e.g. 12 or $foo).
            br foo 
               ^^^
;;; STDERR ;;)