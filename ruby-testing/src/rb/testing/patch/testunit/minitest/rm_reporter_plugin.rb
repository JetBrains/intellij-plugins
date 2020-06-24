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
          Minitest.rubymine_reporter.close_all_suites()
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
      attr_accessor :my_drb_url
    end

    def self.plugin_rm_reporter_init(options)
      require 'rubymine_test_framework_initializer'

      Minitest.reporter.reporters.clear
      Minitest.reporter.reporters << self.rubymine_reporter
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

    class MyRubyMineReporter < MiniTest::Unit
      include ::Rake::TeamCity::RunnerCommon
      include ::Rake::TeamCity::RunnerUtils
      include ::Rake::TeamCity::Utils::UrlFormatter

      attr_accessor :io, :test_count, :assertion_count, :failures, :errors, :skips
      attr_accessor :already_run_tests
      attr_accessor :my_mutex

      def initialize(options = {})
        super()
        @passed = true
        self.io= options[:io] || $stdout
        self.test_count = 0
        self.assertion_count = 0
        self.failures = 0
        self.errors = 0
        self.skips = 0
        self.my_mutex = Mutex.new
        self.already_run_tests = []
        Minitest.my_drb_url = DRb.start_service(nil, self.already_run_tests).uri
      end

      def start
        io.puts 'Started'
        io.puts

        # Setup test runner's MessageFactory
        set_message_factory(Rake::TeamCity::MessageFactory)
        log_test_reporter_attached()

        # Report tests count:
        # todo: get test count
        if ::Rake::TeamCity.is_in_idea_mode
          log(Rake::TeamCity::MessageFactory.create_tests_count(test_count))
        elsif ::Rake::TeamCity.is_in_buildserver_mode
          log(Rake::TeamCity::MessageFactory.create_progress_message("Starting.. (#{self.test_count} tests)"))
        end
        @suites_start_time = Time.now
      end

      def close_all_suites
        (0...already_run_tests.count).each do |i|
          log(Rake::TeamCity::MessageFactory.create_suite_finished(already_run_tests[i], self.already_run_tests[i]))
        end
        self.already_run_tests.clear()
      end

      def prerecord klass, name
        # do not remove, this method called from minitest-reporters
      end

      def report
        close_all_suites
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
        tests = DRbObject.new_with_uri(Minitest.my_drb_url)
        DRb.start_service

        my_mutex.synchronize {
          unless tests.include? test.class.to_s
            tests << test.class.to_s
            log(Rake::TeamCity::MessageFactory.create_suite_started(test.class.to_s, minitest_test_location(test.class.to_s), '0', test.class.to_s))
          end
        }

        @test_start_time = Time.new
        @test_started = true
        test_name = get_test_name(test)
        log(Rake::TeamCity::MessageFactory.create_test_started(test_name, minitest_test_location(fqn), test.class.to_s, fqn))
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

      private
      def get_test_name(test)
        if defined? test.name
          test.name
        else
          test.__name__
        end
      end

      def get_fqn_from_test(test)
        test_name = get_test_name(test)

        if "#{test.class}".end_with?("MiniTest::TestResult")
          "#{test.suite}.#{test_name}"
        elsif test.respond_to? :klass
          "#{test.klass}.#{test_name}"
        else
          "#{test.class}.#{test_name}"
        end
      end

      def log(msg)
        io.flush
        io.puts("\n#{msg}")
        io.flush

        # returns:
        msg
      end

      def minitest_test_location(fqn)
        return nil if (fqn.nil?)
        "ruby_minitest_qn://#{fqn}"
      end

      def with_result(result)
        exception = result.failure
        msg = exception.nil? ? '' : "#{exception.class.name}: #{exception.message}"
        backtrace = exception.nil? ? '' : Minitest::filter_backtrace(exception.backtrace).join("\n")

        yield(msg, backtrace)
      end
    end
  end
end
