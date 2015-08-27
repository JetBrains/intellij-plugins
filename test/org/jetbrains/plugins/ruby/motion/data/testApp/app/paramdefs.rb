class Paramdefs
  def foo

  end

  def paramdefs
    NSObject.alloc.performSelector('foo', test)
    NSObject.alloc.performSelector('foo', withObject: self, test2)
    UITapGestureRecognizer.alloc.initWithTarget(self, action: 'foo')
  end
end