;;; TOOL: wat2wasm
;;; ERROR: 1
                                                                                                                                                                         whoops                      (; some text at the end of the line ;)
(;; STDERR ;;;
out/test/parse/bad-error-long-line.txt:3:170: error: unexpected token "whoops", expected a module field or a module.
...                                  whoops                      (; some text...
                                     ^^^^^^
;;; STDERR ;;)
