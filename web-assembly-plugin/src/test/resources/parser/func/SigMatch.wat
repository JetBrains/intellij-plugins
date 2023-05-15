;;; TOOL: wat2wasm
(module
  (type $empty (func))
  (type $i_v (func (param i32)))
  (type $f_i (func (param f32) (result i32)))
  (type $ii_i (func (param i32 i32) (result i32)))
  (type $v_f (func (result f32)))

  (func (type $empty))
  (func (type $i_v) (param i32))
  (func (type $f_i) (param f32) (result i32) i32.const 0)
  (func (type $ii_i) (param i32 i32) (result i32) i32.const 0)
  (func (type $v_f) (result f32) f32.const 0))