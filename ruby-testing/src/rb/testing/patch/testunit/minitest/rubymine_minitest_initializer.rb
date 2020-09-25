#this patch is to simulate before_test hook, see file rubymine_minitest_patch.rb
Minitest.rubymine_reporter = Minitest::MyRubyMineReporter.new
if defined?(Minitest::Unit) && Minitest::Unit.method_defined?(:runner=)
  Minitest::Unit.runner = Minitest.rubymine_reporter
elsif defined?(MiniTest::Unit) && MiniTest::Unit.method_defined?(:runner=)
  MiniTest::Unit.runner = Minitest.rubymine_reporter
end

if defined? Minitest::Reporters.use!
  Minitest::Reporters.module_eval do
    def self.use!(*args)
      # do not register RubyMineReporter from minitest-reporters
    end
  end
end
if defined? Minitest::Reporters.reporters
  Minitest::Reporters.reporters = [Minitest.rubymine_reporter]
end
