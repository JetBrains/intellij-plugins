;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (func     ;; 2 (implicit)
    block   ;; 1
      block ;; 0
        br 3
      end
    end))
(;; STDERR ;;;
out/test/parse/expr/bad-br-bad-depth.txt:7:9: error: invalid depth: 3 (max 2)
        br 3
        ^^
;;; STDERR ;;)
