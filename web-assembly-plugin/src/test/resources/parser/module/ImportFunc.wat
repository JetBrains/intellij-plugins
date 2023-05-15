;;; TOOL: wat2wasm
(module
  ;; unnamed
  (import "foo" "bar" (func (param i32) (result i64)))

  ;; named
  (import "stdio" "print" (func $print_i32 (param i32)))
  (import "math" "add" (func $add_i32 (param i32 i32) (result i32)))
  (import "test" "f32" (func $f32 (param f32) (result f32)))
  (import "test" "f64" (func $f64 (param f64) (result f64)))
  (import "test" "i64" (func $i64 (param i64) (result i64))))