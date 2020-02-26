module RubyMineMinitestPatch
  def run_with_rm_hook(*args)
    Minitest.rubymine_reporter.before_test(self)
    result = original_run(*args)
    Minitest.rubymine_reporter.after_test(self)
    result
  end
end
