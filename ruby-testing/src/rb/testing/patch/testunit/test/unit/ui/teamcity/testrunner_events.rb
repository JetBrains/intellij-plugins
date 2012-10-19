# Copyright 2000-2012 JetBrains s.r.o.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Created by IntelliJ IDEA.
#
# @author: Roman.Chernyatchik
# @date: 02.06.2007

require 'teamcity/utils/service_message_factory'
require 'teamcity/utils/std_capture_helper'

require 'teamcity/rake_exceptions'
require 'teamcity/rakerunner_consts'
require 'teamcity/runner_common'
require 'teamcity/utils/runner_utils'
require 'teamcity/utils/url_formatter'

module Test
  module Unit
    module UI
      module TeamCity
        module EventHandlers
          include Rake::TeamCity::StdCaptureHelper
          include Test::Unit::Util::BacktraceFilter
          include Rake::TeamCity::RunnerUtils
          include Rake::TeamCity::RunnerCommon
          include Rake::TeamCity::Utils::UrlFormatter

          NO_STACK_TRACE = "No stacktrace."
          UNIT_TESTS_EVENT_LOG = Rake::TeamCity::Utils::TestUnitEventsFileLogger.new

          # add ability to use "unmangle" method in case of test_spec framework
          if Rake::TeamCity.is_framework_used(:test_spec)
            begin
              require "test/spec/dox"
              @@test_spec_helper = Test::Unit::UI::SpecDox::TestRunner.new(nil)

              def @@test_spec_helper.unmangle t_name
                super
              end
            rescue LoadError => ex
              msg = "Cannot load 'test/spec/dox'. It seems 'test/spec' gem wasn't loaded. Please " +
                  "check that you are running 'test/spec' tests(original error message: #{ex.message})."
              raise Rake::TeamCity::InnerException, msg, ex.backtrace
            end
          end

          ###########################################
          ## Sengs service message text to stdout
          def log(msg)
            is_capturing = @output_is_capturing
            # active capturing will affect sending service message
            # we should stop capturing.
            if is_capturing
              test_output_capturer_stop()
            end

            send_msg(msg)

            # if capturing was enabled we should continue it
            if is_capturing
              test_output_capturer_start()
            end

            msg
          end

          #################### TestMediator events ####################
          # Test mediator started
          def started(result)
            @result = result

            #Default value
            @output_is_capturing = false

            debug_log("Test mediator started: #{@root_suite_name}...")
          end

          # Test mediator stopped
          def finished(elapsed_time)
            # Total statistic
            statistics = @result.to_s

            log(statistics)
            debug_log(statistics)

            # Time statistic from Rake Runner
            status_message = "Test suite finished: #{elapsed_time} seconds"

            log(status_message)
            debug_log(status_message)
          end

          # Reset all results from previous test suites.
          # Occurs before each testMediator started.
          def reset_ui(count)
            if ::Rake::TeamCity.is_in_idea_mode
              log(@message_factory.create_tests_count(count))
            elsif ::Rake::TeamCity.is_in_buildserver_mode
              log(@message_factory.create_progress_message("Starting.. (#{count} tests)"))
            end
          end

          def ignore_suite?(suite_name)
            ((@root_suite_name == suite_name) && ::Rake::TeamCity::RunnerUtils.ignore_root_test_case?) ||
                ::Rake::TeamCity::RunnerUtils.excluded_default_testcase_name?(suite_name)
          end

          #################### TestSuite events ####################
          # Test suite started
          def suite_started(suite_name)
            debug_log("Test suite started: #{suite_name}...")
            unless ignore_suite?(suite_name)
              log(@message_factory.create_suite_started(suite_name,
                                                        location_from_ruby_qualified_name(suite_name)))
            end
          end

          # Test suite finished
          def suite_finished(suite_name)
            debug_log("Test suite finished: #{suite_name}...")

            # We should ignore root suite, because it usually fake and contains only
            # information about test's collector - etc 'rake_test_loader' script or 'ruby-debug-ide' script
            unless ignore_suite?(suite_name)
              log(@message_factory.create_suite_finished(suite_name))
            end
          end

          ########################### Tests events #######################

          # Test case started
          # Test::Unit provides uniq name - as suite name with test name
          def test_started(test_name)
            # save message name before patching
            test_name_before_converting = test_name
            test_name = convert_test_name_according_framework(test_name)

            qualified_test_name =
                if test_name == test_name_before_converting
                  convert_ruby_test_name_to_qualified(test_name)
                else
                  # we would use BDD test name as qualified name
                  test_name
                end

            debug_log("Test started #{test_name}...[#{qualified_test_name}]")

            @my_running_test_name = qualified_test_name
            @my_running_test_name_runner_original = test_name
            @my_running_test_start_time = get_current_time_in_ms
            log(@message_factory.create_test_started(@my_running_test_name,
                                                     location_from_ruby_qualified_name(qualified_test_name)))
            #capture output
            test_output_capturer_start()
          end

          # Test case finished
          def test_finished(test_name)
            test_name = convert_test_name_according_framework(test_name)

            # stop capturing
            test_output_capturer_stop()

            assert_test_valid(test_name)

            debug_log("Test finished #{@my_running_test_name_runner_original}...[#{@my_running_test_name}]")

            #close_test_block
            if @my_running_test_name
              duration_ms = get_current_time_in_ms - @my_running_test_start_time
              log(@message_factory.create_test_finished(@my_running_test_name, duration_ms))
              @my_running_test_name = nil
              @my_running_test_name_runner_original = nil
            end
          end

          # Test fault
          def add_fault(fault)
            # test_spec framework : attached and loaded
            if Rake::TeamCity.is_framework_used(:test_spec) && (defined? Test::Spec) &&
                 (Test::Spec::Disabled === fault || Test::Spec::Empty === fault)
              # test_name = fault.short_display
              #  assert_test_valid(test_name) #"short_display" doesn't contains suite name, we cant check test name =/
              log(@message_factory.create_test_ignored(@my_running_test_name, fault.long_display))

            elsif Test::Unit::Failure === fault || omission?(fault)
              omission = omission?(fault)

              if fault.location.kind_of?(Array)
                backtrace = fault.location.join("\n    ")
              else
                backtrace = fault.location.to_s
              end
              message = fault.message.to_s
              test_name = convert_test_name_according_framework(fault.test_name)
              debug_log("Add #{omission ? "omission" : "failure"} for #{test_name}, \n    Backtrace:    \n#{backtrace}")

              assert_test_valid(test_name)
              if omission
                log(@message_factory.create_test_ignored(@my_running_test_name, "#{fault.label}: Test #{message.split("\n")[0]}", backtrace))
              else
                log(@message_factory.create_test_failed(@my_running_test_name, message, backtrace))
              end

            elsif Test::Unit::Error === fault
              backtrace = filter_backtrace(fault.exception.backtrace).join("\n    ")
              message = "#{fault.exception.class.name}: #{fault.exception.message.to_s}"
              test_name = convert_test_name_according_framework(fault.test_name)

              debug_log("Add error for #{test_name}, \n    Backtrace:    \n#{backtrace}")

              if @my_running_test_name_runner_original.nil?
                # test suite error
                log(@message_factory.create_msg_error(message, backtrace))
              else
                # test error
                assert_test_valid(test_name)
                log(@message_factory.create_test_error(@my_running_test_name, message, backtrace))
              end
            elsif (defined? Test::Unit::Notification) && ( Test::Unit::Notification === fault)
              if ::Rake::TeamCity.is_in_idea_mode
                if fault.location.kind_of?(Array)
                  backtrace = fault.location.join("\n    ")
                else
                  backtrace = fault.location.to_s
                end
                log(@message_factory.create_msg_warning("#{fault.label}: #{fault.message.split("\n")[0]}", backtrace))
              else
                log(@message_factory.create_msg_warning(fault.long_display))
              end
            else
              test_name = convert_test_name_according_framework(
                  ((defined? fault.test_name) ? fault.test_name : (@my_running_test_name || "<unknown>")).to_s
              )
              message = "#{fault.class.to_s}: #{((defined? fault.message) ? fault.message : fault).to_s}"
              backtrace = ((defined? fault.backtrace) ? fault.backtrace : NO_STACK_TRACE).to_s
              debug_log("Add unknown fault #{test_name}, \n    Backtrace:    \n#{backtrace}")

              assert_test_valid(test_name)
              log(@message_factory.create_test_error(@my_running_test_name, message, backtrace))
            end
          end

          # Test result changed - update statistics
          def result_changed(result)
            debug_log("result_changed: all=#{result.run_count.to_s}, " +
                          "asserts=#{result.assertion_count.to_s}, " +
                          "failure=#{result.failure_count.to_s}, " +
                          "error count=#{result.error_count.to_s}")
          end

          ###########################################################################
          ###########################################################################
          ###########################################################################

          private

          def omission?(fault)
            (defined? Test::Unit::Omission) && (Test::Unit::Omission === fault)
          end

          def assert_test_valid(test_name)
            if test_name != @my_running_test_name_runner_original
              qualified_test_name = convert_ruby_test_name_to_qualified(test_name)
              msg = "Finished test '#{test_name}'[#{qualified_test_name}] doesn't correspond to current running test '#{@my_running_test_name_runner_original}'[#{@my_running_test_name}]!"
              debug_log(msg)
              raise Rake::TeamCity::InnerException, msg, caller
            end
          end

          # If necessary converts test name by rules of 'test/spec' framework
          def convert_test_name_according_framework(test_name)
            #if !test_name.nil? && Rake::TeamCity.is_framework_used(:test_spec) &&
            #  (!Rake::TeamCity.is_framework_used(:test_unit) || (test_name =~ /\Atest_spec /))
            #
            #  # 'test/spec' framework isn't attached or attached but current test
            #  # is 'test/spec' test (i.e. our test runner mixes 'test/spec' and Test::Unit tests)
            #  spec_test_unmangle(test_name);
            #else
            #  test_name
            #end

            if test_name.nil?
              return nil
            end
            # test-spec
            if Rake::TeamCity.is_framework_used(:test_spec)
              if test_name =~ /\Atest_spec / ||
                  (!Rake::TeamCity.is_framework_used(:test_unit) && !Rake::TeamCity.is_framework_used(:shoulda))
                # is 'test/spec' test (i.e. our test runner mixes 'test/spec' and Test::Unit tests)
                return spec_test_unmangle(test_name)
              end
            end
            # Shoulda
            if Rake::TeamCity.is_framework_used(:shoulda) || Rake::TeamCity.is_framework_used(:test_unit)
              if test_name =~ /\Atest: /
                # is 'should' test or 'test' test
                return shoulda_unmangle(test_name)
              end
            end
            return test_name
          end

          def debug_log(string)
            # Logs output.
            UNIT_TESTS_EVENT_LOG.log_msg(string)
          end

          def test_output_capturer_stop
            stdout_string, stderr_string = capture_output_end_external(@old_out, @old_err, @new_out, @new_err)
            @output_is_capturing = false

            if stdout_string && !stdout_string.empty?
              log(@message_factory.create_test_output_message(@my_running_test_name, true, stdout_string))
            end
            debug_log("My stdOut: [#{stdout_string}]")
            if stderr_string && !stderr_string.empty?
              log(@message_factory.create_test_output_message(@my_running_test_name, false, stderr_string))
            end
            debug_log("My stdErr: [#{stderr_string}]")
          end

          def test_output_capturer_start
            @output_is_capturing = true
            @old_out, @old_err, @new_out, @new_err = capture_output_start_external
          end

          def spec_test_unmangle(test_name)
            # unmangle using Test::Unit::UI::SpecDox::TestRunner.unmangle(name),
            # see test-spec/lib/test/spec/dox.rb
            context, spec_name = (@@test_spec_helper.unmangle test_name.to_s)
            # returns test name with test_case name
            "#{context}.#{spec_name}"
          end

          def shoulda_unmangle(test_name)
            # test name is generate using:
            # test_name = ["test:", full_name, "should", "#{should[:name]}. "].flatten.join(' ').to_sym
            # see (shoulda/lib/shoulda.rb, Thoughtbot::Context.create_test_from_should_hash(should) method)

            if test_name =~ /\Atest: (.*)?\. \(([\w:]*)\)/
              # p [$1, $2]
              example_full_name = $1
              class_name = $2
              return convert_test_unit_to_qualified(class_name, example_full_name)
            end

            # almost the same as shoulda test name just do not add period at the end of name
            if test_name =~ /\Atest: (.*)?\(([\w:]*)\)/
              example_full_name = $1
              class_name = $2
              return convert_test_unit_to_qualified(class_name, example_full_name)
            end
            test_name
          end
        end
      end
    end
  end
end