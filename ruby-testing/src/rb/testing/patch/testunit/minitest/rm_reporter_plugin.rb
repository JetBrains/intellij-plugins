# Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

# This file is the main entrypoint in the process of injection of RubyMine output formatter into minitest / minitest-reporters
# infrastructure. It sets up the runner and reporter for minitest.

begin
  require 'teamcity/runner_common'
  require 'teamcity/utils/service_message_factory'
  require 'teamcity/utils/runner_utils'
  require 'teamcity/utils/url_formatter'
rescue LoadError
  $stderr.puts("====================================================================================================\n")
  $stderr.puts("RubyMine reporter works only if it test was launched using RubyMine IDE or TeamCity CI server !!!\n")
  $stderr.puts("====================================================================================================\n")
  $stderr.puts("Using default results reporter...\n")
else
  require 'drb/drb'
  require 'minitest/rubymine_minitest_patch'

  if defined? Minitest::Test
    Minitest::Test.class_eval do
      include RubyMineMinitestPatch
      alias_method :original_run, :run
      alias_method :run, :run_with_rm_hook
    end
  elsif defined? MiniTest::Unit::TestCase
    MiniTest::Unit::TestCase.class_eval do
      include RubyMineMinitestPatch
      alias_method :original_run, :run
      alias_method :run, :run_with_rm_hook
    end
  end
  if defined? MiniTest::Unit
    MiniTest::Unit.module_eval do
      if defined? MiniTest::Unit and MiniTest::Unit.respond_to?(:status)
        alias_method :original_status, :status

        def status(io = nil)
          Minitest.rubymine_reporter.test_case_manager.end_execution
          if io.nil?
            original_status
          else
            original_status(io)
          end
        end
      end
    end
  end

  module Minitest
    class << self
      attr_accessor :rubymine_reporter
    end

    def self.plugin_rm_reporter_init(options)
      require 'rubymine_test_framework_initializer'

      Minitest.reporter.reporters.clear
      Minitest.reporter.reporters << self.rubymine_reporter
      # can't additional reporters be registered after we put our?
    end

    class TestResult < Struct.new(:suite, :name, :assertions, :time, :exception)
      def result
        case exception
        when nil then :pass
        when Skip then :skip
        when Assertion then :failure
        else :error
        end
      end

      def passed?
        self.result == :pass
      end

      def skipped?
        self.result == :skip
      end

      def failure
        self.exception
      end

      def error?
        self.result == :error
      end
    end

    class RubyMineMinitestParallelTestCaseManager
      attr_accessor :reporter, :my_mutex, :already_run_tests

      def initialize(reporter)
        GC.disable
        self.reporter = reporter
        self.my_mutex = Mutex.new
        self.already_run_tests = Set[]
      end

      def init
        my_drb_url = DRb.start_service('druby://localhost:0', self.already_run_tests).uri
        @my_pid = Process.pid
        @tests = DRbObject.new_with_uri(my_drb_url)
      end

      def end_execution
        close_all_suites
      end

      def process_test(test)
        my_mutex.synchronize {
          start_drb_server_smart
          unless @tests.include?(test.class.to_s)
            @tests.add(test.class.to_s)
            reporter.log(Rake::TeamCity::MessageFactory.create_suite_started(test.class.to_s, reporter.minitest_test_location(test), '0', test.class.to_s))
          end
        }
      end

      private

      # starts Drb service for the forked process if necessary.
      # See: https://docs.ruby-lang.org/en/master/DRb.html#module-DRb-label-Client+code
      def start_drb_server_smart
        unless @my_pid == Process.pid
          DRb.start_service('druby://localhost:0')
          @my_pid = Process.pid
        end
      end

      def close_all_suites
        already_run_tests.each do |test|
          reporter.log(Rake::TeamCity::MessageFactory.create_suite_finished(test, test))
        end
        already_run_tests.clear
        GC.enable
      end
    end

    class RubyMineMinitestSequenceTestCaseManager
      attr_accessor :reporter, :running_test_case

      def initialize(reporter)
        self.reporter = reporter
        self.running_test_case = nil
      end

      def end_execution
        if running_test_case
          reporter.log(Rake::TeamCity::MessageFactory.create_suite_finished(running_test_case, running_test_case))
        end
      end

      def process_test(test)
        if test.class.to_s != running_test_case
          if running_test_case
            reporter.log(Rake::TeamCity::MessageFactory.create_suite_finished(running_test_case, running_test_case))
          end
          reporter.log(Rake::TeamCity::MessageFactory.create_suite_started(test.class.to_s, reporter.minitest_test_location(test), '0', test.class.to_s))
          self.running_test_case = test.class.to_s
        end
      end
    end

    class MyRubyMineReporter < MiniTest::Unit
      include ::Rake::TeamCity::RunnerCommon
      include ::Rake::TeamCity::RunnerUtils
      include ::Rake::TeamCity::Utils::UrlFormatter

      attr_accessor :io, :options, :test_count, :assertion_count, :failures, :errors, :skips, :test_case_manager, :parallel_run

      def initialize(options = {})
        super()
        @passed = true
        self.options= options
        self.io= options[:io] || $stdout
        self.test_count = 0
        self.assertion_count = 0
        self.failures = 0
        self.errors = 0
        self.skips = 0

        self.parallel_run = parallel_run?
        if parallel_run
          self.test_case_manager = RubyMineMinitestParallelTestCaseManager.new(self)
          self.test_case_manager.init
        else
          self.test_case_manager = RubyMineMinitestSequenceTestCaseManager.new(self)
        end
      end

      # adds options from minitest
      # called from <path/to/minitest-reporters>/lib/minitest/minitest_reporter_plugin.rb:52
      def add_defaults(defaults)
        self.options = defaults.merge(options)
      end

      # copied from minitest-reporters: minitest_reporter_plugin.rb
      def total_count(options)
        filter = options[:filter] || '/./'
        filter = Regexp.new $1 if filter =~ /\/(.*)\//

        Minitest::Runnable.runnables.map(&:runnable_methods).flatten.find_all { |m|
          filter === m || filter === "#{self}##{m}"
        }.size
      end

      def start
        # Setup test runner's MessageFactory
        set_message_factory(Rake::TeamCity::MessageFactory)
        log_test_reporter_attached()

        # Report tests count:
        if ::Rake::TeamCity.is_in_idea_mode
          log(Rake::TeamCity::MessageFactory.create_tests_count(options[:total_count] || total_count(options)))
        elsif ::Rake::TeamCity.is_in_buildserver_mode
          log(Rake::TeamCity::MessageFactory.create_progress_message("Starting.. (#{options[:total_count] || total_count(options)} tests)"))
        end
        @suites_start_time = Time.now
      end

      def prerecord klass, name
        # do not remove, this method called from minitest-reporters
      end

      def report
        test_case_manager.end_execution
        []
      end

      def before_suite(suite)
        log(Rake::TeamCity::MessageFactory.create_suite_started(suite.name, location_from_ruby_qualified_name(suite.name)))
      end

      def after_suite(suite)
        already_run_test.remove suite.name
        log(Rake::TeamCity::MessageFactory.create_suite_finished(suite.name))
      end

      def before_test(test)
        fqn = get_fqn_from_test(test)

        test_case_manager.process_test(test)

        @test_start_time = Time.new
        @test_started = true
        test_name = get_test_name(test)
        log(Rake::TeamCity::MessageFactory.create_test_started(test_name, minitest_test_location(test), test.class.to_s, fqn))
      end

      def after_test(result)
        test_name = get_test_name(result)
        fqn = get_fqn_from_test(result)
        duration_ms = get_time_in_ms(Time.new - @test_start_time)
        log(Rake::TeamCity::MessageFactory.create_test_finished(test_name, duration_ms, nil, fqn))
      end

      def record(result,  name = nil, assertions = nil, time = nil, exceptions = nil)
        if name.nil?
          process_test_result(result)
        else
          test_result = TestResult.new(result, name.to_sym, assertions, time, exceptions)
          process_test_result(test_result)
        end
      end

      def process_test_result(result)
        fqn = get_fqn_from_test(result)
        self.test_count += 1
        self.assertion_count += result.assertions
        test_name = result.name
        if result.skipped?
          self.skips += 1
          with_result(result) do |exception_msg, backtrace|
            log(Rake::TeamCity::MessageFactory.create_test_ignored(test_name, exception_msg, backtrace, fqn))
          end
        end
        # todo: replace this check with failed? when it will be available
        if not result.passed? and result.failure.class == Assertion
          self.failures += 1
          with_result(result) do |exception_msg, backtrace|
            log(Rake::TeamCity::MessageFactory.create_test_failed(test_name, exception_msg, backtrace, fqn))
          end
        end
        if result.error?
          self.errors += 1
          with_result(result) do |exception_msg, backtrace|
            log(Rake::TeamCity::MessageFactory.create_test_error(test_name, exception_msg, backtrace, fqn))
          end
        end
      end

      def passed?
        @passed
      end

      def minitest_test_location(test)
        begin
          test_name = get_test_name(test)
          location = test.class.instance_method(test_name).source_location
          "file://#{location[0]}:#{location[1]}"
        rescue NameError, NoMethodError
          fqn = get_fqn_from_test(test)
          "ruby_minitest_qn://#{fqn}" if fqn
        end
      end

      def log(msg)
        io.flush
        io.puts("\n#{msg}")
        io.flush

        msg
      end

      # This method is a bit lame. It covers minitest thread-based parallelization, but won't cover ActiveSupport process-based parallelization.
      # Actually we should use parallel manager all the time, even for a single threaded run. It brings small overhead, but supports any
      # type of parallelization

      private

      def parallel_run?
        defined?(Minitest) && Minitest.respond_to?(:parallel_executor) && Minitest.parallel_executor.respond_to?(:start) &&
          Minitest.parallel_executor.respond_to?(:size) && Minitest.parallel_executor.size > 1 ||
          defined?(MiniTest::Unit::TestCase) && MiniTest::Unit::TestCase.respond_to?(:test_order) && MiniTest::Unit::TestCase.test_order == :parallel
      end

      def get_test_name(test)
        if ::Rake::TeamCity.is_in_buildserver_mode
          # for TeamCity it's necessary to provide FQN
          get_fqn_from_test(test)
        else
          get_short_test_name(test)
        end
      end

      def get_short_test_name(test)
        if defined? test.name
          test.name
        else
          test.__name__
        end
      end

      def get_fqn_from_test(test)
        test_name = get_short_test_name(test)

        if "#{test.class}".end_with?("MiniTest::TestResult")
          "#{test.suite}.#{test_name}"
        elsif test.respond_to? :klass
          "#{test.klass}.#{test_name}"
        else
          "#{test.class}.#{test_name}"
        end
      end

      def with_result(result)
        exception = result.failure
        msg = exception.nil? ? '' : "#{exception.class.name}: #{exception.message}"
        backtrace = exception.nil? ? '' : Minitest::filter_backtrace(exception.backtrace).join("\n")

        yield(msg, backtrace)
      end
    end
  end

  require 'minitest/rubymine_minitest_initializer'
end
