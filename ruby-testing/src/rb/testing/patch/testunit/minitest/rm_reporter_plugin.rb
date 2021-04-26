# Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

# This file is the main entrypoint in the process of injection of RubyMine output formatter into minitest / minitest-reporters
# infrastructure. It sets up the runner and reporter for minitest.
# run with environment variable RM_MT_DEBUG=DEBUG to get a verbose output of the process

require 'teamcity/utils/service_message_factory'
require 'logger'
require 'set'
require 'pp'
require 'mutex_m'

module Minitest
  class << self
    def rm_logger
      @rm_logger ||=
        begin
          rm_logger = Logger.new(STDERR)
          rm_logger.level = ENV['RM_MT_DEBUG'] || Logger::ERROR
          rm_logger.formatter = -> (severity, datetime, progname, msg) {
            "#{datetime} #{severity} #{progname} #{Process.pid}##{Thread.current.object_id} #{msg}\n"
          }
          rm_logger.debug("Logger initialized")
          rm_logger
        end
    end

    def plugin_rm_reporter_init(options)
      assert_no_minitest_reporters
      Minitest.reporter.reporters.clear
      Minitest.reporter.reporters << Minitest::RubyMineReporter.new(options)
    end

    def assert_no_minitest_reporters
      if Object.const_defined?("Minitest::Reporters")
        if Minitest::Reporters.class_variable_defined?('@@loaded')
          raise RuntimeError.new("\nCurrent implementation of IntelliJ Minitest support conflicts with Minitest::Reporters. Please remove Minitest::Reporters.use! from your test code, then re-run your tests.")
        end
      end
    end
  end

  class RubymineTestData
    def initialize(class_name, method_name)
      raise RuntimeError.new("Incorrect class name class: #{class_name.class}") unless class_name.instance_of? String
      @class_name = class_name
      @method_name = method_name
    end

    def fqn
      "#{@class_name}.#{@method_name}"
    end

    def location
      begin
        location = klass.instance_method(@method_name).source_location
        "file://#{location[0]}:#{location[1]}"
      rescue NameError, NoMethodError
        "ruby_minitest_qn://#{@class_name}.#{@method_name}"
      end
    end

    def klass=(klass)
      return if klass.nil?
      raise RuntimeError.new("Class expected, got #{klass}; #{klass.class}") unless klass.instance_of?(Class)
      @klass = klass
    end

    def klass
      @klass || Object.const_get(@class_name)
    end
  end

  class RubyMineReporter < Reporter

    # Minitest 5.0 compatibility
    include Mutex_m

    def initialize(options = {})
      Minitest.assert_no_minitest_reporters
      super(options[:io] || $stdout, options)
      @test_data = Hash.new { |suites_hash, class_name|
        suites_hash[class_name] = Hash.new { |tests_hash, test_name|
          tests_hash[test_name] = RubymineTestData.new(class_name, test_name)
        }
      }
      @test_count = 0
      @assertion_count = 0
      @failures = 0
      @errors = 0
      @skips = 0
      debug("Reporter created #{self} with options: #{options.pretty_inspect}")
    end

    # adds options from minitest
    # called from <path/to/minitest-reporters>/lib/minitest/minitest_reporter_plugin.rb:52
    def add_defaults(defaults)
      debug("Adding defaults #{defaults.pretty_inspect}")
      self.options = defaults.merge(options)
    end

    ##
    # Starts reporting on the run.
    def start
      Minitest.assert_no_minitest_reporters
      debug("Starting reporting")
      collect_tests_to_run
      send_service_message(Rake::TeamCity::MessageFactory.create_tests_count(options[:total_count] || total_count))
    end

    ##
    # Did this run pass?
    def passed?
      @failures + @errors == 0
    end

    ##
    # Outputs the summary of the run.
    def report
      debug("Reporting summary")
      close_pending_suites
      []
    end

    ##
    # About to start running a test. This allows a reporter to show
    # that it is starting or that we are in the middle of a test run.
    def prerecord(klass, test_name)
      synchronize {
        test_started(klass.name, test_name, klass)
      }
    end

    ##
    # Output and record the result of the test. Call
    # {result#result_code}[rdoc-ref:Runnable#result_code] to get the
    # result character string. Stores the result of the run if the run
    # did not pass.
    def record(test_result)
      synchronize {
        # after test
        # Checking for Minitest::Result is for Minitest 5.0 compatibility
        class_name = Object.const_defined?('Minitest::Result') ? test_result.klass : test_result.class.name
        test_name = test_result.name
        unless @test_data[class_name].key?(test_name)
          debug("prerecord was not invoked for the #{class_name}.#{test_name}")
          test_started(class_name, test_name, nil)
        end
        test_data = @test_data[class_name][test_name]
        test_fqn = test_data.fqn
        debug("Test finished #{test_fqn}")

        normalized_test_name = normalize(test_name)

        # record
        @test_count += 1
        @assertion_count += test_result.assertions
        if test_result.skipped?
          @skips += 1
          with_message_and_backtrace(test_result) do |exception_msg, backtrace|
            send_service_message(Rake::TeamCity::MessageFactory.create_test_ignored(normalized_test_name, exception_msg, backtrace, test_fqn))
          end
        end
        # todo: replace this check with failed? when it will be available
        if !test_result.passed? && test_result.failure.class == Assertion
          @failures += 1
          with_message_and_backtrace(test_result) do |exception_msg, backtrace|
            send_service_message(Rake::TeamCity::MessageFactory.create_test_failed(normalized_test_name, exception_msg, backtrace, test_fqn))
          end
        end
        if test_result.error?
          @errors += 1
          with_message_and_backtrace(test_result) do |exception_msg, backtrace|
            send_service_message(Rake::TeamCity::MessageFactory.create_test_error(normalized_test_name, exception_msg, backtrace, test_fqn))
          end
        end
        send_service_message(Rake::TeamCity::MessageFactory.create_test_finished(normalized_test_name, time_in_ms(test_result.time), nil, test_fqn))
        @tests_to_run[class_name].delete(test_name)
        suite_finished(class_name) if @tests_to_run[class_name].empty?
      }
    end

    private

    def send_service_message(msg)
      io.flush
      io.puts("\n#{msg}")
      io.flush

      msg
    end

    def debug(msg)
      Minitest.rm_logger.debug(msg)
    end

    def suite_started(class_name, class_location)
      debug("Starting suite #{class_name} at #{class_location}")
      send_service_message(Rake::TeamCity::MessageFactory.create_suite_started(class_name, class_location, '0', class_name))
    end

    def suite_finished(suite_name)
      debug("Finishing suite #{suite_name}")
      send_service_message(Rake::TeamCity::MessageFactory.create_suite_finished(suite_name, suite_name))
      @tests_to_run.delete(suite_name)
    end

    def test_started(class_name, test_name, klass)
      debug("Starting test #{class_name}.#{test_name}")
      first_in_suite = @test_data[class_name].empty?
      test_data = @test_data[class_name][test_name]
      test_data.klass = klass
      suite_started(class_name, test_data.location) if first_in_suite
      send_service_message(Rake::TeamCity::MessageFactory.create_test_started(normalize(test_name), test_data.location, class_name, test_data.fqn))
      debug("Test started: #{test_data.fqn} from #{test_data.location}")
    end

    # copied from minitest-reporters: minitest_reporter_plugin.rb
    def total_count
      tests_count = 0
      process_suitable_tests { |test_class, method_name| tests_count = tests_count + 1 }
      tests_count
    end

    def collect_tests_to_run
      @tests_to_run = Hash.new { |hash, class_name| hash[class_name] = Set.new }
      process_suitable_tests { |test_class, method_name| @tests_to_run[test_class.name] << method_name }
    end

    # processes all tests matched by filtering options and passing each class/method to the processor
    def process_suitable_tests
      @suitable_tests ||= compute_suitable_tests
      @suitable_tests.each { |data| yield(*data) }
    end

    # see Minitest::Runnable.run
    def compute_suitable_tests
      filter = options[:filter] || "/./"
      filter = Regexp.new $1 if filter.is_a?(String) && filter =~ %r%/(.*)/%

      exclude = options[:exclude]
      exclude = Regexp.new $1 if exclude =~ %r%/(.*)/%

      debug("Filtering using #{filter} and exclude #{exclude}")

      Minitest::Runnable.runnables.flat_map { |test_class|
        test_class.runnable_methods.select { |test_method|
          (filter === test_method || filter === "#{test_class}##{test_method}") &&
            !(exclude === test_method || exclude === "#{test_class}##{test_method}")
        }.map { |test_method| [test_class, test_method] }
      }
    end

    def close_pending_suites
      @tests_to_run.each_key do |suite_name|
        debug("Force closing test suite #{suite_name}")
        suite_finished(suite_name)
      end
    end

    def with_message_and_backtrace(result)
      exception = result.failure
      msg = exception.nil? ? '' : "#{exception.class.name}: #{exception.message}"
      backtrace = exception.nil? ? '' : Minitest::filter_backtrace(exception.backtrace).join("\n")

      yield(msg, backtrace)
    end

    def normalize(test_method_name)
      test_method_name.gsub(/test_\d{4}_/, '')
    end

    def time_in_ms(time)
      ((time.to_f) * 1000).to_i
    end
  end
end

