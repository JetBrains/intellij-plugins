;;; TOOL: wat2wasm
;;; ENV: FORCE_COLOR=1
;;; ERROR: 1
(module
  (func badname (param i32) (result badtype)
    drop))
(;; STDERR ;;;
[1mout/test/parse/force-color.txt:5:9: [31merror: [0munexpected token badname, expected ).
  (func badname (param i32) (result badtype)
        [1m[32m^^^^^^^[0m
[1mout/test/parse/force-color.txt:5:37: [31merror: [0munexpected token badtype.
  (func badname (param i32) (result badtype)
                                    [1m[32m^^^^^^^[0m
;;; STDERR ;;)