is_minitest_run = false
begin
  require "minitest/autorun"
rescue LoadError
  # it's ok in case of minitest 1.4
end

begin
  require "minitest/unit"
  require 'minitest/rm_reporter_plugin'
  is_minitest_run = true
  begin
    require 'minitest/reporters'
  rescue LoadError
    # ok, there is no minitest-reporters gem
  end
rescue LoadError
  begin
    require 'test/unit'
  rescue LoadError
    puts "Neither minitest nor test-unit libraries available"
  end
end

if is_minitest_run
  #this patch is to simulate before_test hook, see file rubymine_minitest_patch.rb
  Minitest.rubymine_reporter = Minitest::MyRubyMineReporter.new
  if defined? Minitest::Unit.runner=
    Minitest::Unit.runner = Minitest.rubymine_reporter
  elsif defined? MiniTest::Unit.runner
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
end