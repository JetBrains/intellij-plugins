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

require 'teamcity/rakerunner_consts'
require 'teamcity/utils/runner_utils'

ORIGINAL_SDK_TESTRUNNERMEDIATOR_PATH = ENV[ORIGINAL_SDK_TESTRUNNERMEDIATOR_PATH_KEY]
if ORIGINAL_SDK_TESTRUNNERMEDIATOR_PATH
  require ORIGINAL_SDK_TESTRUNNERMEDIATOR_PATH
end

class Test::Unit::UI::TestRunnerMediator
  TC_TESTCOUNT = name + "::TC_TESTCOUNT"
  TC_REPORTER_ATTACHED = name + "::TC_REPORTER_ATTACHED"
  TC_RUN_METHOD_NAME = (method_defined? :run) ? "run" : "run_suite"

  class_eval %Q{
    alias :old_#{TC_RUN_METHOD_NAME} :#{TC_RUN_METHOD_NAME}
    def #{TC_RUN_METHOD_NAME}
      count = calc_patched_size(@suite)

      # Notify test reporter attached
      notify_listeners(TC_REPORTER_ATTACHED)

      # Notify patched size
      notify_listeners(TC_TESTCOUNT, count)
      # delegate call to super
      old_#{TC_RUN_METHOD_NAME}
    end
  }

  # Patched size
  def calc_patched_size(suite)
    # if is a test (not suite) - will have size 1, other way size depends on amount of nested tests.
    return 1 unless defined? suite.tests

    size = 0
    tests = suite.tests

    # if suite is empty it will contain only one test with name "default_test"
    # which will not be reported
    if tests.size == 1
      # if suite has only one test
      first_test = tests[0]

      # let's check if it is fake test method
      if ::Rake::TeamCity::RunnerUtils.fake_default_test_for_empty_suite?(first_test)
         return 0
       end
    end

    tests.each do |suite_or_test|
        # If suite is excluded and contains only one tes (default test)
        unless ::Rake::TeamCity::RunnerUtils.excluded_default_testcase?(suite_or_test)
            size += calc_patched_size(suite_or_test)
        end
    end
    size
  end
  private :calc_patched_size
end
