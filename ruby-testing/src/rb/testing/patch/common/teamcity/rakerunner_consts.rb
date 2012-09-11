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
# @date: 29.01.2008

#TODO move all to TeamCity module

# Behaviour properties
# Debug
TEAMCITY_RAKERUNNER_LOG_RSPEC_XML_MSFS_KEY = 'TEAMCITY_RAKE_RUNNER_DEBUG_LOG_RSPEC_XML_MSGS'
TEAMCITY_RAKERUNNER_LOG_PATH_KEY = 'TEAMCITY_RAKE_RUNNER_DEBUG_LOG_PATH'
TEAMCITY_RAKERUNNER_LOG_OUTPUT_HACK_DISABLED_KEY = 'TEAMCITY_RAKE_RUNNER_DEBUG_OUTPUT_HACK_DISABLED'
TEAMCITY_RAKERUNNER_LOG_OUTPUT_CAPTURER_ENABLED_KEY = 'TEAMCITY_RAKE_RUNNER_DEBUG_OUTPUT_CAPTURER_ENABLED'
# Log files
TEAMCITY_RAKERUNNER_LOG_FILENAME_SUFFIX = '/rakeRunner_rake.log'
TEAMCITY_RAKERUNNER_RPC_LOG_FILENAME_SUFFIX = '/rakeRunner_xmlrpc.log'
TEAMCITY_RAKERUNNER_SPEC_LOG_FILENAME_SUFFIX = '/rakeRunner_rspec.log'
TEAMCITY_RAKERUNNER_TESTUNIT_LOG_FILENAME_SUFFIX = '/rakeRunner_testUnit.log'
TEAMCITY_RAKERUNNER_TESTUNIT_EVENTS_LOG_FILENAME_SUFFIX = '/rakeRunner_testUnit_events.log'

# Teamcity connection properties
#TODO it seems this section is deprecated!
IDEA_BUILDSERVER_BUILD_ID_KEY = 'IDEA_BUILD_SERVER_BUILD_ID'
IDEA_BUILDSERVER_AGENT_PORT_KEY = 'IDEA_BUILD_AGENT_PORT'
IDEA_BUILDSERVER_HOST_ID_KEY = 'IDEA_BUILD_SERVER_HOST'

# Name of Teamcity RPC logger method
TEAMCITY_LOGGER_RPC_NAME = "testRunnerLogger.log"
#TEAMCITY_LOGGER_RPC_NAME = "buildAgent.log"

# Rake runner dispatcher settings
TEAMCITY_RAKERUNNER_DISPATCHER_MAX_ATTEMPTS = 100
TEAMCITY_RAKERUNNER_DISPATCHER_RETRY_DELAY = 0.25

# Rakerunner system properties
ORIGINAL_SDK_AUTORUNNER_PATH_KEY = 'TEAMCIY_RAKE_TU_AUTORUNNER_PATH'
ORIGINAL_SDK_TESTRUNNERMEDIATOR_PATH_KEY = 'TEAMCITY_RAKE_TU_TESTRUNNERMADIATOR_PATH'
TEAMCITY_RAKERUNNER_RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED = 'TEAMCITY_RAKE_TRACE'

module Rake
  module TeamCity
    # Mode
    TEAMCITY_VERSION_KEY ='TEAMCITY_VERSION'


    # Test::Unit
    RUBY19_SDK_MINITEST_RUNNER_PATH_KEY = "TC_RUBY19_SDK_MINITEST_RUNNER_PATH_KEY"

    # TODO: remove TEAMCITY_* prefix
    TEAMCITY_RAKERUNNER_USED_FRAMEWORKS_KEY ='TEAMCITY_RAKE_RUNNER_USED_FRAMEWORKS'
    TEAMCITY_RAKERUNNER_DEBUG_OPTIONS_KEY ='TEAMCITY_RAKERUNNER_DEBUG_OPTIONS'

    TEAMCITY_RAKERUNNER_SUPPORTED_FRAMEWORKS =[:rspec, :test_spec, :test_unit, :cucumber, :shoulda]

    TC_EXCLUDED_DEFAULT_TEST_CASES = [
        "ActionController::IntegrationTest",
        "ActionController::TestCase",
        "ActionView::TestCase",
        "ActionMailer::TestCase",
        "ActiveRecord::TestCase",
        "ActiveSupport::TestCase",
        # Rails 3.0
        "ActionDispatch::IntegrationTest",
        "ActionDispatch::PerformanceTest"
    ]

    def self.is_in_idea_mode
      !is_in_buildserver_mode
    end

    def self.is_in_buildserver_mode
      version = ENV[TEAMCITY_VERSION_KEY]
      # version must be set and not empty
      if !version.nil? && !version.empty?
        return true
      end
    end

    # Supported frameworks
    # :rspec
    # :test_spec
    # :test_unit
    def self.is_framework_used(symbol)
      value = ENV[TEAMCITY_RAKERUNNER_USED_FRAMEWORKS_KEY]
      # check that symbol is name of supported framework
      # and that supported frameworks env variable is set
      return false if value.nil?
      if TEAMCITY_RAKERUNNER_SUPPORTED_FRAMEWORKS.index(symbol).nil?
        raise ArgumentError, "Unsupported framework: #{symbol}", caller
      end

      return !value.index(":#{symbol.to_s}").nil?
    end

    def self.is_fake_time_enabled?
      self.is_enabled_in_debug_options(:fake_time)
    end

    def self.is_fake_stacktrace_enabled?
      self.is_enabled_in_debug_options(:fake_stacktrace)
    end

    def self.is_fake_error_msg_enabled?
      self.is_enabled_in_debug_options(:fake_error_msg)
    end

    def self.is_fake_location_url_enabled?
      self.is_enabled_in_debug_options(:fake_location_url)
    end

    private
    def self.is_enabled_in_debug_options(symbol)
      debug_options = ENV[TEAMCITY_RAKERUNNER_DEBUG_OPTIONS_KEY]
      !debug_options.nil? && !debug_options[symbol.to_s].nil?
    end
  end
end