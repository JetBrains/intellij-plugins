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
# @date: 07.06.2007

require 'teamcity/rakerunner_consts'
require 'teamcity/utils/logger_util'
require 'teamcity/runner_common'

RAKE_EXT_LOG = Rake::TeamCity::Utils::RakeFileLogger.new
RAKE_EXT_LOG.log_msg("rake_ext.rb loaded.")

# For RAKEVERSION =  0.7.3 - 0.8.3
require 'rake'

require 'teamcity/utils/service_message_factory'
require 'teamcity/utils/std_capture_helper'
######################################################################
######################################################################
# This file is teamcity extension for Rake API                       #
######################################################################
######################################################################

########## Rake  TeamCityApplication #################################
module Rake
  class TeamCityApplication < Application
    extend Rake::TeamCity::StdCaptureHelper
    extend Rake::TeamCity::RunnerCommon

    @@tc_message_factory = Rake::TeamCity::MessageFactory

    # msg factory
    def self.tc_message_factory
      @@tc_message_factory
    end

    def initialize
      # TODO
      # log(@message_factory.create_flow_message())
      begin
        super
      rescue Exception => e
        msg, stacktrace = Rake::TeamCityApplication.format_exception_msg(e)
        send_msg(tc_message_factory.create_msg_error(msg, stacktrace))

        RAKE_EXT_LOG.log_msg("Rake application initialization errors:\n #{msg}\n #{stacktrace}")
        exit(1)
      else
        RAKE_EXT_LOG.log_msg("Rake application initialized. #2.0")
      end
    end

    # Wraps block in pair of teamcity messages: blockStart, blockEnd.
    # Then executes it. If error occurs method will send information to TeamCity and
    # raise special exception to interrupt process, but prevent further handling of this exception
    def self.target_exception_handling(block_msg, is_execute = false, additional_message = "")
      RAKE_EXT_LOG.log_msg("Block: #{block_msg}, is execute #{is_execute}")

      show_invoke_block = Rake.application.options.trace || ENV[TEAMCITY_RAKERUNNER_RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED]
      create_block = is_execute || (!@already_invoked && show_invoke_block)

      block_msg = "#{is_execute ? "Execute" : "Invoke"} #{block_msg}"

      # Log in TeamCity
      send_msg(tc_message_factory.create_open_target(block_msg)) if create_block

      show_additional_msg = !is_execute && ENV[TEAMCITY_RAKERUNNER_RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED] && !Rake.application.options.trace
      send_msg(additional_message) if (additional_message && !additional_message.empty? && show_additional_msg)

      # Capture output for execution stage
      if is_execute
        old_out, old_err, new_out, new_err = capture_output_start_external
      end
      # Executes task safely
      begin
        yield
      rescue Rake::ApplicationAbortedException => app_e
        raise
      rescue Exception => exc
        # Log in TeamCity
        Rake::TeamCityApplication.process_exception(exc)
      ensure
        if is_execute
          stdout_string, stderr_string = capture_output_end_external(old_out, old_err, new_out, new_err)

          unless stdout_string.empty?
            send_msg(stdout_string)
            RAKE_EXT_LOG.log_msg("Task[#{block_msg}] Std Output:\n[#{stdout_string}]")
          end
          unless stderr_string.empty?
            send_msg(stderr_string)
            RAKE_EXT_LOG.log_msg("Task[#{block_msg}] Std Error:\n[#{stderr_string}]")
          end
        end
        # Log in TeamCity
        send_msg(tc_message_factory.create_close_target(block_msg)) if create_block
      end
    end

    # Logs exception in TeamCity and raises special(Rake::ApplicationAbortedException.new)
    # exception to prevent further handling
    #
    # *returns* Exit code
    #
    # *raises*  Rake::ApplicationAbortedExceptionExit if not on top level
    def self.process_exception(exception, on_top_level=false)
      # exit code value, 1 by default.
      exit_code = 1

      # Check if exception is Rake::ApplicationAbortedException
      if exception.instance_of?(Rake::ApplicationAbortedException)
        exc = exception.inner_exception
        application_aborted_exception = true
      else
        exc = exception
        application_aborted_exception = false
      end

      #Process exception
      case (exc)
        when SystemExit
          # Exit silently with current status
          exit_code = exc.status
        when RAKEVERSION >= '0.8.3' ? OptionParser::InvalidOption : GetoptLong::InvalidOption
          # Exit Silently
        else
          # Sends exception to buildserver, if exception hasn't been sent early(inside some markup block)
          unless application_aborted_exception
            # Send exception in current opened teamcity mark-up block.
            msg, stacktrace = Rake::TeamCityApplication.format_exception_msg(exc)
            send_msg(tc_message_factory.create_msg_error(msg, stacktrace))
          end

          if on_top_level
            # Rake aborted
            send_msg(tc_message_factory.create_msg_error("Rake aborted!"))
          end
      end

      # Rerise if not on top level to correctly close all parent markup blocks
      unless on_top_level
        # Exception was send to teamcity, now we should
        # raise special exception to prevent further handling
        raise Rake::ApplicationAbortedException, exc
      end
      exit_code
    end

    # Formats exception message and stacktrace according current error representation options
    # Returns error msg and stacktrace
    def self.format_exception_msg(exception, show_trace = true)
      back_trace_msg = "\nStacktrace:\n" + exception.backtrace.join("\n")
      if Rake.application.rakefile
        source_file = exception.backtrace.find { |str| str =~ /#{Regexp.quote(Rake.application.rakefile)}/ }
        stacktrace = back_trace_msg + (source_file ? "\n\nSource: #{source_file}" : "") + (show_trace ? "" : "\n(See full trace by running task with --trace option)")
      else
        stacktrace = back_trace_msg
      end
      return "#{exception.class.name}: #{exception.message}", stacktrace
    end

    def run
      exit_code = 0
      begin
        super
      rescue Exception => e
        exit_code = Rake::TeamCityApplication.process_exception(e, true)
      ensure
        exit(exit_code) if (exit_code != 0)
      end
    end
  end

  class ApplicationAbortedException < StandardError
    attr_reader :inner_exception

    def initialize(other_exception)
      @inner_exception = other_exception
    end
  end
end

################  Output extension ############################

#@Deprecated
(require File.dirname(__FILE__) + '/ext/output_ext') if ((ENV[TEAMCITY_RAKERUNNER_LOG_OUTPUT_CAPTURER_ENABLED_KEY] == "true") && (ENV[TEAMCITY_RAKERUNNER_LOG_OUTPUT_HACK_DISABLED_KEY] != "true"))

################  Module extension #############################
class Module
  #Overriding rake_extension of standard API. 0.7.3 - 0.8.3
  def rake_extension(method)
    if instance_methods.include?(method)
      msg = "WARNING: Possible conflict with Rake extension: #{self}##{method} already exists"
      Rake::TeamCityApplication.send_msg(Rake::TeamCityApplication.tc_message_factory.create_msg_warning(msg))
    else
      yield
    end
  end
end

################  Rake extension #############################
module Rake
  # Rake module singleton methods.
  class << self
    # Current Rake Application: 0.7.3 - 0.8.3
    def application
      @application ||= Rake::TeamCityApplication.new
    end
  end
end

################# Task extention #########################################
class Rake::Task
  NEW_API = defined? invoke_with_call_chain

  # Overrides standard API. 0.7.3 - 0.8.3
  #
  # Invoke the task if it is needed. Prerequisites are invoked first.
  def my_invoke_with_call_chain(*args, &block)
    Rake::TeamCityApplication.target_exception_handling(name, false, format_trace_flags) do
      standard_invoke_with_call_chain(*args, &block)
    end
  end

  private :my_invoke_with_call_chain
  if NEW_API
    # 0.8.0 and higher
    alias :standard_invoke_with_call_chain :invoke_with_call_chain
    # overrides 'invoke_with_call_chain' with 'my_invoke_with_call_chain'
    alias :invoke_with_call_chain :my_invoke_with_call_chain
  else
    # 0.7.3
    alias :standard_invoke_with_call_chain :invoke
    # overrides 'invoke' with 'my_invoke_with_call_chain'
    alias :invoke :my_invoke_with_call_chain
  end
  private :standard_invoke_with_call_chain
  public :invoke

  # Overrides standard API. 0.7.3 - 0.8.1
  #
  # Execute the actions associated with this task.
  alias :standard_execute :execute
  private :standard_execute

  def execute(*args, &block)
    standard_execute_block = Proc.new do
      standard_execute(*args, &block)
    end

    if application.options.dryrun
      Rake::TeamCityApplication.target_exception_handling(name, true, "(dry run)", &standard_execute_block)
    else
      Rake::TeamCityApplication.target_exception_handling(name, true, &standard_execute_block)
    end
  end
end

###########  RakeFilesUtils  extention ##############################
module RakeFileUtils

  # Overrides standard API. 0.7.3 - 0.8.3
  #
  # Use this function to prevent potentially destructive ruby code from
  # running when the :nowrite flag is set.
  #
  # Example:
  #
  #   when_writing("Building Project") do
  #     project.build
  #   end
  #
  # The following code will build the project under normal conditions. If the
  # nowrite(true) flag is set, then the example will print:
  #      DRYRUN: Building Project
  # instead of actually building the project.
  #
  alias standard_when_writing when_writing
  private :standard_when_writing

  def when_writing(msg = nil, &block)
    if RakeFileUtils.nowrite_flag
      Rake::TeamCityApplication.send_msg("DRYRUN: #{msg}") if msg
    end

    standard_when_writing(msg, &block)
  end
end

###########  Rake::Application  extention #############################
class Rake::Application
  # Overrides standard API. 0.7.3 - 0.8.3
  #
  # Provide standard exception handling for the given block.
  #
  # This method wraps exceptions into  Rake::TeamCityApplication.process_exception exception.
  # Such exceptions will be processed in Rake::TeamCityApplication.run
  def standard_exception_handling
    begin
      yield
    rescue Rake::ApplicationAbortedException => app_e
      raise
    rescue Exception => exc
      # Log in TeamCity
      Rake::TeamCityApplication.process_exception(exc)
    end
  end

  # Overrides standard API.  0.7.3 - 0.8.3
  #
  # Warn about deprecated use of top level constant names.
  def const_warning(const_name)
    @const_warning ||= false
    unless @const_warning
      msg = %{WARNING: Deprecated reference to top-level constant '#{const_name}' } +
          %{found at: #{rakefile_location}} +
          %{    Use --classic-namespace on rake command} +
          %{    or 'require "rake/classic_namespace"' in Rakefile}
      Rake::TeamCityApplication.send_msg(Rake::TeamCityApplication.tc_message_factory.create_msg_warning(msg))
    end
    @const_warning = true
  end
end

at_exit do
  RAKE_EXT_LOG.log_msg("rak_ext.rb: Finished.")
  RAKE_EXT_LOG.close
end