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

require 'teamcity/utils/logger_util'

UNIT_TESTS_RUNNER_LOG = Rake::TeamCity::Utils::TestUnitFileLogger.new
UNIT_TESTS_RUNNER_LOG.log_msg("testrunner.rb loaded.")

require 'test/unit/ui/testrunnermediator'
require 'test/unit/ui/testrunnerutilities'
require 'test/unit/ui/teamcity/testrunner_events'

# Runs a Test::Unit::TestSuite on teamcity server.
class Test::Unit::UI::TeamCity::TestRunner
  extend Test::Unit::UI::TestRunnerUtilities

  attr_reader :listeners

  # Includes module with event handlers
  include Test::Unit::UI::TeamCity::EventHandlers

  # Creates a new TestRunner for running the passed
  # suite.
  def initialize(*args)
    arity = args.size

    output_level = (defined? Test::Unit::UI::NORMAL) ? Test::Unit::UI::NORMAL : 2
    @options = {}

    if arity == 2
      suite = args[0]
      second_arg = args[1]
      if Hash === second_arg
        @options = second_arg
      elsif Numeric === second_arg
        output_level = second_arg
      else
        msg = "Unsupported Test::Unit version: Unkown API, 2nd arg is #{second_arg.class.name}"
        raise Rake::TeamCity::InnerException, msg, caller
      end
    elsif arity != 1
      msg = "Unsupported Test::Unit version: Unkown API, arity = #{arity}"
      raise Rake::TeamCity::InnerException, msg, caller
    end

    if suite.respond_to?(:suite)
      @root_suite = suite.suite
    else
      @root_suite = suite
    end

    @listeners = @options[:listeners] || []

    @result = nil
  end

  # Starts testing
  def start
    setup_mediator
    attach_to_mediator

    # Saves STDOUT, STDERR because bugs in testrunner can break it.
    sout, serr = copy_stdout_stderr
    begin
      start_mediator
    ensure
      # Repairs stdout and stderr just in case
      sout.flush
      serr.flush
      reopen_stdout_stderr(sout, serr)
    end

    @result
  end

  def start_mediator
    @mediator.send Test::Unit::UI::TestRunnerMediator::TC_RUN_METHOD_NAME
  end

  private

  def setup_mediator
    set_message_factory(Rake::TeamCity::MessageFactory)
    @mediator = Test::Unit::UI::TestRunnerMediator.new(@root_suite)
    @root_suite_name = (@root_suite.kind_of?(Module) ? @root_suite.name : @root_suite.to_s)
  end

  def attach_to_mediator
    @mediator.add_listener(Test::Unit::TestResult::FAULT, &method(:add_fault))
    @mediator.add_listener(Test::Unit::TestResult::CHANGED, &method(:result_changed))

    @mediator.add_listener(Test::Unit::TestCase::STARTED, &method(:test_started))
    @mediator.add_listener(Test::Unit::TestCase::FINISHED, &method(:test_finished))

    @mediator.add_listener(Test::Unit::TestSuite::STARTED, &method(:suite_started))
    @mediator.add_listener(Test::Unit::TestSuite::FINISHED, &method(:suite_finished))

    @mediator.add_listener(Test::Unit::UI::TestRunnerMediator::STARTED, &method(:started))
    @mediator.add_listener(Test::Unit::UI::TestRunnerMediator::FINISHED, &method(:finished))
    @mediator.add_listener(Test::Unit::UI::TestRunnerMediator::TC_TESTCOUNT, &method(:reset_ui))
    @mediator.add_listener(Test::Unit::UI::TestRunnerMediator::TC_REPORTER_ATTACHED, &method(:log_test_reporter_attached))
    #@mediator.add_listener(Test::Unit::UI::TestRunnerMediator::RESET, &method(:reset_ui))
  end
end

if __FILE__ == $0
  Test::Unit::UI::TeamCity::TestRunner.start_command_line_test
end

at_exit do
  UNIT_TESTS_RUNNER_LOG.log_msg("testrunner.rb: Finished")
  UNIT_TESTS_RUNNER_LOG.close
end