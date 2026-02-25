begin
  require 'minitest/assertions'
rescue LoadError
  return
end

module RubyMineAssertionPatch
  attr_accessor :rm_expected_str, :rm_actual_str
end

module RubyMineAssertionsPatch
  def rm_inject_assertion_details(e, exp, act)
    e.extend(RubyMineAssertionPatch)
    if exp.is_a?(String) && act.is_a?(String)
      e.rm_expected_str = exp
      e.rm_actual_str = act
    else
      e.rm_expected_str = mu_pp(exp)
      e.rm_actual_str = mu_pp(act)
    end
  end

  def assert_equal(exp, act, msg = nil)
    super
  rescue Minitest::Assertion => e
    rm_inject_assertion_details e, exp, act
    raise
  end

  def diff(exp, act)
    nil # prevent minitest from computing and showing its own diff
  end
end

unless ENV["INTELLIJ_IDEA_RUN_CONF_MINITEST_DIFF_VIEWER_DISABLE"]
  Minitest::Assertions.prepend(RubyMineAssertionsPatch)
end
