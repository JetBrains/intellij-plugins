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

  module Minitest
    def self.plugin_rm_reporter_options(opts, options)
    end

    def self.plugin_rm_reporter_init(options)
      if have_minitest_reporters(self.reporter.reporters)
        self.reporter.reporters.clear
        self.reporter << MyReporter.new(options)
      end
    end

    private

    def self.have_minitest_reporters(reporters)
      (reporter.reporters.index { |r| r.class.name == "Minitest::Reporters::RubyMineReporter" }).nil?
    end

    class MyReporter
      include ::Rake::TeamCity::RunnerCommon
      include ::Rake::TeamCity::RunnerUtils
      include ::Rake::TeamCity::Utils::UrlFormatter

      attr_accessor :io, :test_count, :assertion_count, :failures, :errors, :skips

      def initialize(options = {})
        @passed = true
        self.io= options[:io] || $stdout
        self.test_count = 0
        self.assertion_count = 0
        self.failures = 0
        self.errors = 0
        self.skips = 0
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
          log(@message_factory.create_tests_count(test_count))
        elsif ::Rake::TeamCity.is_in_buildserver_mode
          log(@message_factory.create_progress_message("Starting.. (#{self.test_count} tests)"))
        end
        @suites_start_time = Time.now
      end

      def report
        total_time = Time.now - @suites_start_time

        io.puts('Finished in %.5fs' % total_time)
        io.print('%d tests, %d assertions, ' % [test_count, assertion_count])
        io.print('%d failures, %d errors, ' % [failures, errors])
        io.print('%d skips' % skips)
        io.puts
      end

      def before_suite(suite)
        log(@message_factory.create_suite_started(suite.name, location_from_ruby_qualified_name(suite.name)))
      end

      def after_suite(suite)
        log(@message_factory.create_suite_finished(suite.name))
      end

      def before_test(test)
        @test_started = true
        fqn = "#{test.class}.#{test.name}"
        log(@message_factory.create_test_started(test.name, minitest_test_location(fqn)))
      end

      def record(result)
        self.test_count += 1
        self.assertion_count += result.assertions
        test_name = result.name
        if result.skipped?
          self.skips += 1
          with_result(result) do |exception_msg, backtrace|
            log(@message_factory.create_test_ignored(test_name, exception_msg, backtrace))
          end
        end
        # todo: replace this check with failed? when it will be available
        if not result.passed? and result.failure.class == Assertion
          self.failures += 1
          with_result(result) do |exception_msg, backtrace|
            log(@message_factory.create_test_failed(test_name, exception_msg, backtrace))
          end
        end
        if result.error?
          self.errors += 1
          with_result(result) do |exception_msg, backtrace|
            log(@message_factory.create_test_error(test_name, exception_msg, backtrace))
          end
        end
        if result.passed?
          unless @test_started
            # hope minitest will fire before_test callback soon, but for now we should emulate it
            fqn = "#{result.class}.#{test_name}"
            log(@message_factory.create_test_started(test_name, minitest_test_location(fqn)))
          end
          duration_ms = get_time_in_ms(result.time)
          log(@message_factory.create_test_finished(test_name, duration_ms.nil? ? 0 : duration_ms))
        end
        @test_started = false
      end

      def passed?
        @passed
      end

      private

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
