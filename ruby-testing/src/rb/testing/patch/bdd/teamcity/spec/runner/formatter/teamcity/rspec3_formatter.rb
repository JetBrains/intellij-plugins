# Copyright 2000-2014 JetBrains s.r.o.
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

require 'teamcity/utils/logger_util'
require 'teamcity/rake_exceptions'
require 'teamcity/rakerunner_consts'

require 'teamcity/runner_common'
require 'teamcity/utils/service_message_factory'
require 'teamcity/utils/std_capture_helper'
require 'teamcity/utils/runner_utils'
require 'teamcity/utils/url_formatter'

module Spec
  module Runner
    module Formatter
      class TeamcityFormatter < RSpec::Core::Formatters::BaseFormatter
        include ::Rake::TeamCity::StdCaptureHelper
        include ::Rake::TeamCity::RunnerUtils
        include ::Rake::TeamCity::RunnerCommon
        include ::Rake::TeamCity::Utils::UrlFormatter

        RSpec::Core::Formatters.register self, :start, :close,
                                         :example_group_started, :example_group_finished,
                                         :example_started, :example_passed,
                                         :example_pending, :example_failed,
                                         :dump_summary, :seed

        RUNNER_ISNT_COMPATIBLE_MESSAGE = "TeamCity Rake Runner Plugin isn't compatible with this RSpec version.\n\n"
        TEAMCITY_FORMATTER_INTERNAL_ERRORS =[]
        @@reporter_closed = false

        ########## Teamcity #############################
        def log(msg)
          send_msg(msg)

          # returns:
          msg
        end

        def self.closed?()
          @@reporter_closed
        end

        def self.close()
          @@reporter_closed = true
        end

        ######## Spec formatter ########################
        def initialize(output)
          super

          # Initializes
          @groups_stack = []

          # check out output stream is a Drb stream, in such case all commands should be send there
          if !output.nil? && (defined? DRb::DRbObject) && output.kind_of?(DRb::DRbObject)
            @@original_stdout = output
          end

          ###############################################

          # Setups Test runner's MessageFactory
          set_message_factory(::Rake::TeamCity::MessageFactory)
          log_test_reporter_attached
        end

        def start(count_notification)
          super

          @example_count = count_notification.count

          # Log count of examples
          if ::Rake::TeamCity.is_in_idea_mode
            log(@message_factory.create_tests_count(@example_count))
          elsif ::Rake::TeamCity.is_in_buildserver_mode
            log(@message_factory.create_progress_message("Starting.. (#{@example_count} examples)"))
          end
          debug_log("Examples: (#{@example_count} examples)")

          # Saves STDOUT, STDERR because bugs in RSpec/formatter can break it
          @sout, @serr = copy_stdout_stderr

          debug_log("Starting..")
        end

        def example_group_started(group_notification)
          super
          my_add_example_group(group_notification.group.description, group_notification.group)
        end


        def example_group_finished(group_notification)
          return if @groups_stack.empty?

          # get and remove
          current_group_description = @groups_stack.pop

          debug_log("Closing example group(behaviour): [#{current_group_description}].")
          log(@message_factory.create_suite_finished(current_group_description))
        end

        #########################################################
        #########################################################
        # start / fail /pass /pending method
        #########################################################
        #########################################################
        @@RUNNING_EXAMPLES_STORAGE = {}

        def example_started(example_notification)
          example = example_notification.example
          my_running_example_desc = example_description(example)
          debug_log("example started [#{my_running_example_desc}]  #{example}")

          current_group_description = @groups_stack.last
          my_running_example_full_name = "#{current_group_description} #{my_running_example_desc}"

          # Send open event
          debug_log("Example starting.. - full name = [#{my_running_example_full_name}], desc = [#{my_running_example_desc}]")
          log(@message_factory.create_test_started(my_running_example_full_name, location_from_link(*extract_source_location_from_example(example))))

          # Start capturing...
          std_files = capture_output_start_external

          debug_log('Output capturing started.')

          put_data_to_storage(example, RunningExampleData.new(my_running_example_full_name, '', *std_files))
        end

        def example_passed(example_notification)
          example = example_notification.example
          debug_log("example_passed[#{example_description(example)}]  #{example}")

          stop_capture_output_and_log_it(example)

          close_test_block(example)
        end

        def example_failed(example_notification)
          example = example_notification.example
          debug_log("example failed[#{example_description(example)}]  #{example}")

          stop_capture_output_and_log_it(example)

          # example service data
          example_data = get_data_from_storage(example)
          additional_flowid_suffix = example_data.additional_flowid_suffix
          running_example_full_name = example_data.full_name

          failure = example.exception
          expectation_not_met = failure.kind_of?(RSpec::Expectations::ExpectationNotMetError)
          pending_fixed = failure.kind_of?(RSpec::Core::Pending::PendingExampleFixedError)

          # Failure message:
          message = if failure.nil?
                      # for unknown failure
                      '[Without Exception]'
                    elsif expectation_not_met || pending_fixed
                      failure.message
                    else
                      # for other exception
                      "#{failure.class.name}: #{failure.message}"
                    end

          # Backtrace
          backtrace = example_notification.formatted_backtrace.join("\n")

          debug_log("Example failing... full name = [#{running_example_full_name}], Message:\n#{message} \n\nBackrace:\n#{backtrace}\n\n, additional flowid suffix=[#{additional_flowid_suffix}]")

          # Expectation failures will be shown as failures and other exceptions as Errors
          if expectation_not_met
            log(@message_factory.create_test_failed(running_example_full_name, message, backtrace))
          else
            log(@message_factory.create_test_error(running_example_full_name, message, backtrace))
          end
          close_test_block(example)
        end


        def example_pending(example_notification)
          example = example_notification.example
          message = example.execution_result.pending_message
          debug_log("pending: #{example_description(example)}, #{message}, #{example}")

          # stop capturing
          stop_capture_output_and_log_it(example)

          # example service data
          example_data = get_data_from_storage(example)
          additional_flowid_suffix = example_data.additional_flowid_suffix
          running_example_full_name = example_data.full_name

          debug_log("Example pending... [#{@groups_stack.last}].[#{running_example_full_name}] - #{message}, additional flowid suffix=[#{additional_flowid_suffix}]")
          log(@message_factory.create_test_ignored(running_example_full_name, "Pending: #{message}"))

          close_test_block(example)
        end


# see snippet_extractor.rb
# Here we can add file link or show code lined
#        def extra_failure_content(failure)
#          require 'spec/runner/formatter/snippet_extractor'
#          @snippet_extractor ||= SnippetExtractor.new
#          "    <pre class=\"ruby\"><code>#{@snippet_extractor.snippet(failure.exception)}</code></pre>"
#        end

# For Rspec:
#  4 args - rspec < 2.0
#  0 args - rspec >= 2.0
        def dump_summary(summary_notification)
          duration = summary_notification.duration
          example_count = summary_notification.example_count
          failure_count = summary_notification.failure_count
          pending_count = summary_notification.pending_count
          # Repairs stdout and stderr just in case
          repair_process_output
          totals = "#{example_count} example#{'s' unless example_count == 1}"
          totals << ", #{failure_count} failure#{'s' unless failure_count == 1}"
          totals << ", #{example_count - failure_count - pending_count} passed"
          totals << ", #{pending_count} pending" if pending_count > 0

          # Total statistic
          debug_log(totals)
          log(totals)

          # Time statistic from Spec Runner
          status_message = "Finished in #{duration} seconds"
          debug_log(status_message)
          log(status_message)

          #Really must be '@example_count == example_count', it is hack for spec trunk tests
          if !@setup_failed && @example_count > example_count
            msg = "#{RUNNER_ISNT_COMPATIBLE_MESSAGE}Error: Not all examples have been run! (#{example_count} of #{@example_count})\n#{gather_unfinished_examples_name}"

            log_and_raise_internal_error msg
            debug_log(msg)
          end unless @groups_stack.empty?

          unless @@RUNNING_EXAMPLES_STORAGE.empty?
            # unfinished examples statistics
            msg = RUNNER_ISNT_COMPATIBLE_MESSAGE + gather_unfinished_examples_name
            log_and_raise_internal_error msg
          end

          # finishing
          @@RUNNING_EXAMPLES_STORAGE.clear

          debug_log("Summary finished.")
        end
        
        def seed(notification)
          log(notification.fully_formatted) if notification.seed_used?
        end

        def close(notification)
          tc_rspec_do_close
        end

        ###########################################################################
        ###########################################################################
        ###########################################################################
        private

        def gather_unfinished_examples_name
          if @@RUNNING_EXAMPLES_STORAGE.empty?
            return ""
          end

          msg = "Following examples weren't finished:"
          count = 1
          @@RUNNING_EXAMPLES_STORAGE.each { |key, value|
            msg << "\n  #{count}. Example : '#{value.full_name}'"
            sout_str, serr_str = get_redirected_stdout_stderr_from_files(value.stdout_file_new, value.stderr_file_new)
            unless sout_str.empty?
              msg << "\n[Example Output]:\n#{sout_str}"
            end
            unless serr_str.empty?
              msg << "\n[Example Error Output]:\n#{serr_str}"
            end

            count += 1
          }
          msg
        end

        def example_description(example)
          example.description || '<noname>'
        end


        # Repairs SDOUT, STDERR from saved data
        def repair_process_output
          if !@sout.nil? && !@serr.nil?
            @sout.flush
            @serr.flush
            reopen_stdout_stderr(@sout, @serr)
          end
        end

        # Refactored initialize method. Is used for support rspec API < 1.1 and >= 1.1.
        # spec_location_info : "$PATH:$LINE_NUM"
        def my_add_example_group(group_desc, example_group = nil)
          # New block starts.
          @groups_stack << "#{group_desc}"

          description = @groups_stack.last
          debug_log("Adding example group(behaviour)...: [#{description}]...")
          log(@message_factory.create_suite_started(description,
                                                    location_from_link(*extract_source_location_from_group(example_group))))
        end

        def close_test_block(example)
          example_data = remove_data_from_storage(example)
          finished_at_ms = get_time_in_ms(example.execution_result.finished_at)
          started_at_ms = get_time_in_ms(example.execution_result.started_at)
          duration = finished_at_ms - started_at_ms

          additional_flowid_suffix = example_data.additional_flowid_suffix
          running_example_full_name = example_data.full_name

          debug_log("Example finishing... full example name = [#{running_example_full_name}], duration = #{duration} ms, additional flowid suffix=[#{additional_flowid_suffix}]")
          diagnostic_info = "rspec [#{::RSpec::Core::Version::STRING}]" + ", f/s=(#{finished_at_ms}, #{started_at_ms}), duration=#{duration}, time.now=#{Time.now.to_s}, raw[:started_at]=#{example.execution_result.started_at.to_s}, raw[:finished_at]=#{example.execution_result.finished_at.to_s}, raw[:run_time]=#{example.execution_result.run_time.to_s}"

          log(@message_factory.create_test_finished(running_example_full_name, duration, ::Rake::TeamCity.is_in_buildserver_mode ? nil : diagnostic_info))
        end


        def debug_log(string)
          # Logs output.
          SPEC_FORMATTER_LOG.log_msg(string)
        end

        def stop_capture_output_and_log_it(example)
          example_data = get_data_from_storage(example)
          additional_flowid_suffix = example_data.additional_flowid_suffix
          running_example_full_name = example_data.full_name

          stdout_string, stderr_string = capture_output_end_external(*example_data.get_std_files)
          debug_log("Example capturing was stopped.")

          debug_log("My stdOut: [#{stdout_string}] additional flow id=[#{additional_flowid_suffix}]")
          if stdout_string && !stdout_string.empty?
            log(@message_factory.create_test_output_message(running_example_full_name, true, stdout_string))
          end
          debug_log("My stdErr: [#{stderr_string}] additional flow id=[#{additional_flowid_suffix}]")
          if stderr_string && !stderr_string.empty?
            log(@message_factory.create_test_output_message(running_example_full_name, false, stderr_string))
          end
        end

        ######################################################
        ############# Assertions #############################
        ######################################################
        #        def assert_example_valid(example_desc)
        #           if (example_desc != @my_running_example_desc)
        #              msg = "Example [#{example_desc}] doesn't correspond to current running example [#{@my_running_example_desc}]!"
        #              debug_log(msg)
        #              ... [send error to teamcity] ...
        #              close_test_block
        #
        #              raise ::Rake::TeamCity::InnerException, msg, caller
        #            end
        #        end

        # We doesn't support concurrent example groups executing
        def assert_example_group_valid(group_description)
          current_group_description = @groups_stack.last
          if group_description != current_group_description
            msg = "Example group(behaviour) [#{group_description}] doesn't correspond to current running example group [#{ current_group_description}]!"
            debug_log(msg)

            raise ::Rake::TeamCity::InnerException, msg, caller
          end
        end

        ######################################################
        def log_and_raise_internal_error(msg, raise_now = false)
          debug_log(msg)

          log(msg)
          log(@message_factory.create_build_error_report("Failed to run RSpec.."))

          excep_data = [msg, caller]
          if raise_now
            @@RUNNING_EXAMPLES_STORAGE.clear
            raise ::Rake::TeamCity::InnerException, *excep_data
          end
          TEAMCITY_FORMATTER_INTERNAL_ERRORS << excep_data
        end

        def get_data_from_storage(example)
          @@RUNNING_EXAMPLES_STORAGE[example.object_id]
        end

        def remove_data_from_storage(example)
          @@RUNNING_EXAMPLES_STORAGE.delete(example.object_id)
        end

        def put_data_to_storage(example, data)
          @@RUNNING_EXAMPLES_STORAGE[example.object_id] = data
        end

        ######################################################
        ######################################################
        #TODO remove flowid
        class RunningExampleData
          attr_reader :full_name # full task name, example name in build log
          #          TODO: Remove!
          attr_reader :additional_flowid_suffix # to support concurrently running examples
          attr_reader :stdout_file_old # before capture
          attr_reader :stderr_file_old # before capture
          attr_reader :stdout_file_new #current capturing storage
          attr_reader :stderr_file_new # current capturing storage

          def initialize(full_name, additional_flowid_suffix, stdout_file_old, stderr_file_old, stdout_file_new, stderr_file_new)
            @full_name = full_name
#          TODO: Remove!
            @additional_flowid_suffix = additional_flowid_suffix
            @stdout_file_old = stdout_file_old
            @stderr_file_old = stderr_file_old
            @stdout_file_new = stdout_file_new
            @stderr_file_new = stderr_file_new
          end

          def get_std_files
            return @stdout_file_old, @stderr_file_old, @stdout_file_new, @stderr_file_new
          end
        end
      end
    end
  end
end

