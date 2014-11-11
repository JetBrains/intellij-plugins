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

# @author Roman.Chernyatchik
# @date 18:02:54

require File.expand_path(File.dirname(__FILE__) + '/formatter_initializer.rb')

require 'teamcity/utils/logger_util'
require 'teamcity/rake_exceptions'
require 'teamcity/rakerunner_consts'

SPEC_FORMATTER_LOG = ::Rake::TeamCity::Utils::RSpecFileLogger.new
SPEC_FORMATTER_LOG.log_msg("spec formatter.rb loaded.")

require 'teamcity/runner_common'
require 'teamcity/utils/service_message_factory'
require 'teamcity/utils/std_capture_helper'
require 'teamcity/utils/runner_utils'
require 'teamcity/utils/url_formatter'

if Spec::Runner::Formatter::RSPEC_VERSION_3
  require File.expand_path(File.dirname(__FILE__) + '/rspec3_formatter')
else
module Spec
  module Runner
    module Formatter
      class TeamcityFormatter < (RSPEC_VERSION_2 ? RSpec::Core::Formatters::BaseFormatter : Spec::Runner::Formatter::BaseFormatter)
        include ::Rake::TeamCity::StdCaptureHelper
        include ::Rake::TeamCity::RunnerUtils
        include ::Rake::TeamCity::RunnerCommon
        include ::Rake::TeamCity::Utils::UrlFormatter

        RUNNER_ISNT_COMPATIBLE_MESSAGE = "TeamCity Rake Runner Plugin isn't compatible with this RSpec version.\n\n"
        RUNNER_RSPEC_FAILED = "Failed to run RSpec.."

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
        def initialize(*args)
          # Rspec 1.0.8 - 1.1.12, 1.2.0 rspec support
          # 1. initialize(where)
          # 2. initialize(options, where)
          # 3. initialize(options, output)
          #
          # RSpec 2.x support
          # 4. initialize(output)
          output_stream = nil
          method_arity = args.length
          if method_arity == 1
            # old API
            # initialize(where)
            output_stream = args[0]
            super(output_stream)
            @options = nil
          elsif method_arity == 2
            # initialize(options, where)
            # 1.1.3 and higher
            output_stream = args[1]
            super(args[0], output_stream)
          else
            log_and_raise_internal_error RUNNER_ISNT_COMPATIBLE_MESSAGE + "BaseFormatter.initialize arity = #{method_arity}.", true
          end

          # Initializes
          @groups_stack = []
          @ex_group_finished_event_supported = nil

          # check out output stream is a Drb stream, in such case all commands should be send there
          redirect_output_via_drb = !output_stream.nil? && (defined? DRb::DRbObject) && output_stream.kind_of?(DRb::DRbObject)
          if redirect_output_via_drb
            @@original_stdout = output_stream
          end

          ###############################################

          # Setups Test runner's MessageFactory
          set_message_factory(::Rake::TeamCity::MessageFactory)
          log_test_reporter_attached()
        end

        def start(example_count)
          super

          @example_count = example_count

          # Log count of examples
          if ::Rake::TeamCity.is_in_idea_mode
            log(@message_factory.create_tests_count(example_count))
          elsif ::Rake::TeamCity.is_in_buildserver_mode
            log(@message_factory.create_progress_message("Starting.. (#{@example_count} examples)"))
          end
          debug_log("Examples: (#{@example_count} examples)")

          # Saves STDOUT, STDERR because bugs in RSpec/formatter can break it
          @sout, @serr = copy_stdout_stderr

          debug_log("Starting..")
        end

        # For RSpec < 1.1
        def add_behaviour(name)
          super
          my_add_example_group(name)
        end

        #For RSpec >= 1.1, <= 1.2.3
        #For RSpec >= 2.0.0.beta1 and < 2.0.0.beta.19
        def add_example_group(example_group)
          super
          my_add_example_group(example_group.description, example_group)
        end

        #For RSpec >= 1.2.4
        #For RSpec >= 2.0.0.beta.19
        def example_group_started(example_group)
          super

          desc = if rspec_2? && !ex_group_finished_event_supported?
                   # temporary work around for rspec 2.0 < 2.0.0.beta22
                   example_group.ancestors.reverse.inject("") { |name, group| name + " " + group.description.strip }
                 else
                   # rspec 1.x && >= 2.0.0.beta22
                   example_group.description
                 end
          my_add_example_group(desc, example_group)
        end

        #For RSpec >= 2.0.0.beta.22
        def example_group_finished(example_group)
          close_example_group
        end

        #########################################################
        #########################################################
        # start / fail /pass /pending method
        #########################################################
        #########################################################
        @@RUNNING_EXAMPLES_STORAGE = {}

        #Sometimes example_started is executed in another group
        #Such behavior leds to inconsistent order of exaple_started, example_passed/failed/pending events
        #This bug usually reproduced in specs on RSpec project.
        #
        #This hack helps in this problem:
        # *Output start capture at example started
        # *Example start/passed/failed/pending methods shares example's full and output files in map
        #
        # In fact this is a HACK
        # TODO: events branch
        def example_started(example)
          # Due to rspec 2.1.0 regression
          if rspec_2? || rspec_1_2_0?
            return
          end
          # Rspec < 2.1.0
          report_example_started(example)
        end

        def example_passed(example)
          if rspec_2? || rspec_1_2_0?
            # Due to regression in rspec 1_2_0 we had to report event here
            report_example_started(example)
          end

          debug_log("example_passed[#{example_description(example)}]  #{example}")

          stop_capture_output_and_log_it(example)

          close_test_block(example)
        end

        # failure is  Spec::Runner::Reporter::Failure
        def example_failed(*args)
          method_arity = args.length
          if method_arity == 1
            # rspec 2.0
            # example_failed(example)
            example = args[0]
            execution_result = args[0].execution_result
            # :exception hash key rspec >= 2.2.0, :exception_encountered in older versions
            failure = execution_result[:exception] || execution_result[:exception_encountered]
          elsif method_arity == 3
            # rspec 1.x
            # RSpec < #3305 (i.e. <= 1.1.3)
            # example_failed(example, counter, failure)
            example, failure = args[0], args[2]
          else
            log_and_raise_internal_error RUNNER_ISNT_COMPATIBLE_MESSAGE + "BaseFormatter.example_pending arity = #{method_arity}.", true
          end
          example_failed_3args(example, failure)
        end

        # failure is  Spec::Runner::Reporter::Failure
        def example_failed_3args(example, failure)
          if rspec_2? || rspec_1_2_0?
            # Due to regression in rspec 1_2_0 we had to report event here
            report_example_started(example)
          end

          if get_data_from_storage(example).nil?
            #TODO: #638 - See http://rspec.lighthouseapp.com/projects/5645-rspec/tickets/638
            desc = example_description(example)
            if desc == "after(:all)" || desc == "before(:all)"
              @setup_failed = true
              example_started(example)
            end
          end

          debug_log("example failed[#{example_description(example)}]  #{example}")

          stop_capture_output_and_log_it(example)

          # example service data
          example_data = get_data_from_storage(example)
          additional_flowid_suffix = example_data.additional_flowid_suffix
          running_example_full_name = example_data.full_name

          # Failure message:
          if rspec_2?
            def failure.expectation_not_met?
              self.exception.kind_of?(RSpec::Expectations::ExpectationNotMetError)
            end

            def failure.pending_fixed?
              if defined? RSpec::Core::Pending::PendingExampleFixedError
                # rspec >= 2.8.0
                self.exception.kind_of?(RSpec::Core::Pending::PendingExampleFixedError)
              elsif defined? RSpec::Core::PendingExampleFixedError
                # rspec >= 2.0.0.beta.19
                self.exception.kind_of?(RSpec::Core::PendingExampleFixedError)
              else
                # rspec < 2.0.0.beta.19
                self.exception.kind_of?(RSpec::Expectations::PendingExampleFixedError)
              end
            end
          end

          message = if failure.exception.nil?
                      # for unknown failure
                      "[Without Exception]";
                    elsif (failure.expectation_not_met? || failure.pending_fixed?)
                      # for expectation error (Spec::Expectations::ExpectationNotMetError)
                      # and
                      # for pending fixed (Spec::Example::PendingExampleFixedError)
                      failure.exception.message
                    else
                      # for other exception
                      "#{failure.exception.class.name}: #{failure.exception.message}"
                    end

          # Backtrace
          backtrace = calc_backtrace(failure.exception, example)

          #if ::Rake::TeamCity.is_in_buildserver_mode
          #  # failure description
          #  #full_failure_description = message
          #  #(full_failure_description += "\n\n    " + backtrace) if backtrace
          #end

          debug_log("Example failing... full name = [#{running_example_full_name}], Message:\n#{message} \n\nBackrace:\n#{backtrace}\n\n, additional flowid suffix=[#{additional_flowid_suffix}]")

          # Expectation failures will be shown as failures and other exceptions as Errors
          if failure.expectation_not_met?
            log(@message_factory.create_test_failed(running_example_full_name, message, backtrace))
          else
            log(@message_factory.create_test_error(running_example_full_name, message, backtrace))
          end
          close_test_block(example)
        end

        def calc_backtrace(exception, example)
          return "" if exception.nil?
          if rspec_2? && respond_to?(:format_backtrace) && self.class.instance_method(:format_backtrace).arity == 2
            format_backtrace(exception.backtrace, example).join("\n")
          else
            ::Rake::TeamCity::RunnerCommon.format_backtrace(exception.backtrace)
          end
        end

        def example_pending(*args)
          method_arity = args.length
          if method_arity == 1
            # rspec 2.0
            # example_pending(example)
            example = args[0]
            execution_result = args[0].execution_result
            example_group_desc, example, message = nil, example, execution_result[:pending_message]
          elsif method_arity == 2
            # rev. #3305 (http://rspec.rubyforge.org/svn/trunk) changes
            # RSpec 1.1.4, RSPec >= 1.2.4(3rd args is optional)
            # example_pending(example, message)
            example_group_desc, example, message = nil, *args
          elsif method_arity == 3
            if args[1].is_a?(String)
              # RSpec 1.1.8 and higher, RSpec 1.2.0 ... RSPec 1.2.4
              # example_pending(example, message, pending_caller)
              example_group_desc, example, message = nil, *args
            else
              # RSpec < #3305 (i.e. <= 1.1.3)
              # example_pending(example_group_description, example, message)
              example_group_desc, example, message = args
            end
          else
            log_and_raise_internal_error RUNNER_ISNT_COMPATIBLE_MESSAGE + "BaseFormatter.example_pending arity = #{method_arity}.", true
          end
          example_pending_3args(example_group_desc, example, message)
        end

        # example_group_desc - can be nil
        def example_pending_3args(example_group_desc, example, message)
          if rspec_2? || rspec_1_2_0?
            # Due to regression in rspec 1_2_0 we had to report event here
            report_example_started(example)
          end

          debug_log("pending: #{example_group_desc}, #{example_description(example)}, #{message}, #{example}")

          # stop capturing
          stop_capture_output_and_log_it(example)

          if example_group_desc
            #Old API, <= 1.1.3
            assert_example_group_valid(example_group_desc)
          end

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
        def dump_summary(duration = @duration,
            example_count = @example_count,
            failure_count = failed_examples().length,
            pending_count = pending_examples().length)

          # Repairs stdout and stderr just in case
          repair_process_output

          if dry_run?
            totals = "This was a dry-run"
          else
            totals = "#{example_count} example#{'s' unless example_count == 1}, #{failure_count} failure#{'s' unless failure_count == 1}, #{example_count - failure_count - pending_count} passed"
            totals << ", #{pending_count} pending" if pending_count > 0
          end

          close_example_group

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
          @@RUNNING_EXAMPLES_STORAGE.clear()

          debug_log("Summary finished.")
        end
        
        # Report the used seed
        def seed(number)
          log("Randomized with seed #{number}")
        end

        # RSPec >= 2.0
        def close
          tc_rspec_do_close
        end

        ###########################################################################
        ###########################################################################
        ###########################################################################
        private

        # For rspec >= 2.0.0.beta22 API
        # new "example_group_finished" event was added in beta.22
        def ex_group_finished_event_supported?
          if @ex_group_finished_event_supported.nil?
            methods = self.class.superclass.instance_methods
            # Holy shit!!! ----> in ruby 1.8.x "instance_methods" returns collection of string and in 1.9.x collection of symbols!
            @ex_group_finished_event_supported = methods.include?("example_group_finished") || methods.include?(:example_group_finished)
          end
          @ex_group_finished_event_supported
        end

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
          example.description || "<noname>"
        end

        # Due to rspec 2.1.0 regression we had to report fake started event in pass/finish/fail/pendings events
        # and ignore it in example_started event
        def report_example_started(example)
          my_running_example_desc = example_description(example)
          debug_log("example started [#{my_running_example_desc}]  #{example}")

          current_group_description = @groups_stack.last
          my_running_example_full_name = "#{current_group_description} #{my_running_example_desc}"

          # Send open event
          debug_log("Example starting.. - full name = [#{my_running_example_full_name}], desc = [#{my_running_example_desc}]")
          log(@message_factory.create_test_started(my_running_example_full_name, location_from_link(*extract_source_location_from_example(example))))

          # Start capturing...
          std_files = capture_output_start_external
          started_at_ms = rspec_2? ?
              get_time_in_ms(example.execution_result[:started_at]) :
              get_current_time_in_ms

          debug_log("Output capturing started.")

          put_data_to_storage(example, RunningExampleData.new(my_running_example_full_name, "", started_at_ms, *std_files))
        end

        # Repairs SDOUT, STDERR from saved data
        def repair_process_output
          if !@sout.nil? && !@serr.nil?
            @sout.flush
            @serr.flush
            reopen_stdout_stderr(@sout, @serr)
          end
        end

        def dry_run?
          (@options && (@options.dry_run)) ? true : false
        end

        # Refactored initialize method. Is used for support rspec API < 1.1 and >= 1.1.
        # spec_location_info : "$PATH:$LINE_NUM"
        def my_add_example_group(group_desc, example_group = nil)
          # If "group finished" API isn't available, let's close the previous block
          if !rspec_2? || !ex_group_finished_event_supported?
            close_example_group
          end

          # New block starts.
          @groups_stack << "#{group_desc}"

          description = @groups_stack.last
          debug_log("Adding example group(behaviour)...: [#{description}]...")
          log(@message_factory.create_suite_started(description,
                                                    location_from_link(*extract_source_location_from_group(example_group))))
        end

        def close_test_block(example)
          example_data = remove_data_from_storage(example)
          finished_at_ms = rspec_2? ?
              get_time_in_ms(example.execution_result[:finished_at]) :
              get_current_time_in_ms
          duration = finished_at_ms - example_data.start_time_in_ms

          additional_flowid_suffix = example_data.additional_flowid_suffix
          running_example_full_name = example_data.full_name

          debug_log("Example finishing... full example name = [#{running_example_full_name}], duration = #{duration} ms, additional flowid suffix=[#{additional_flowid_suffix}]")
          diagnostic_info = (rspec_2? ? "rspec2 [#{::RSpec::Core::Version::STRING}]" : "rspec1") + ", f/s=(#{finished_at_ms}, #{example_data.start_time_in_ms}), duration=#{duration}, time.now=#{Time.now.to_s}" + (rspec_2? ? ", raw[:started_at]=#{example.execution_result[:started_at].to_s}, raw[:finished_at]=#{example.execution_result[:finished_at].to_s}, raw[:run_time]=#{example.execution_result[:run_time].to_s}" : "")

          log(@message_factory.create_test_finished(running_example_full_name, duration, ::Rake::TeamCity.is_in_buildserver_mode ? nil : diagnostic_info))
        end

        def close_example_group
          # do nothing if it no groups were added before (e.g. 1.x api)
          return if @groups_stack.empty?

          # get and remove
          current_group_description = @groups_stack.pop

          debug_log("Closing example group(behaviour): [#{current_group_description}].")
          log(@message_factory.create_suite_finished(current_group_description))
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
          log(@message_factory.create_build_error_report(RUNNER_RSPEC_FAILED))

          excep_data = [msg, caller]
          if raise_now
            @@RUNNING_EXAMPLES_STORAGE.clear()
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

        def rspec_2?
          ::Spec::Runner::Formatter::RSPEC_VERSION_2
        end

        def rspec_1_2_0?
          ::Spec::VERSION::MAJOR == 1 &&
              ::Spec::VERSION::MINOR == 2 &&
              ::Spec::VERSION::TINY == 0
        end

        ######################################################
        ######################################################
        #TODO remove flowid
        class RunningExampleData
          attr_reader :full_name # full task name, example name in build log
                                 #          TODO: Remove!
          attr_reader :additional_flowid_suffix # to support concurrently running examples
          attr_reader :start_time_in_ms # start time of example
          attr_reader :stdout_file_old # before capture
          attr_reader :stderr_file_old # before capture
          attr_reader :stdout_file_new #current capturing storage
          attr_reader :stderr_file_new # current capturing storage

          def initialize(full_name, additional_flowid_suffix, start_time_in_ms, stdout_file_old, stderr_file_old, stdout_file_new, stderr_file_new)
            @full_name = full_name
#          TODO: Remove!
            @additional_flowid_suffix = additional_flowid_suffix
            @start_time_in_ms = start_time_in_ms
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
end
def tc_rspec_do_close
  if ::Spec::Runner::Formatter::TeamcityFormatter.closed?
    return
  end

  ::Spec::Runner::Formatter::TeamcityFormatter.close

  SPEC_FORMATTER_LOG.log_msg("spec formatter.rb: Finished")
  SPEC_FORMATTER_LOG.close

  unless  Spec::Runner::Formatter::TeamcityFormatter::TEAMCITY_FORMATTER_INTERNAL_ERRORS.empty?
    several_exc = Spec::Runner::Formatter::TeamcityFormatter::TEAMCITY_FORMATTER_INTERNAL_ERRORS.length > 1
    excep_data = Spec::Runner::Formatter::TeamcityFormatter::TEAMCITY_FORMATTER_INTERNAL_ERRORS[0]

    common_msg = (several_exc ? "Several exceptions have occured. First exception:\n" : "") + excep_data[0] + "\n"
    common_backtrace = excep_data[1]

    raise ::Rake::TeamCity::InnerException, common_msg, common_backtrace
  end
end

at_exit do
  tc_rspec_do_close()
end