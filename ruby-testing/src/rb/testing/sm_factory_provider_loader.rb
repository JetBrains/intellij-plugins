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

# @author: Roman Chernyatchik

# Is used only for testing purposes

# Patching load path for tests
$: << File.dirname(__FILE__) + '/patch/bdd'
$: << File.dirname(__FILE__) + '/patch/common'
$: << File.dirname(__FILE__) + '/patch/testunit'

include Java

require 'teamcity/utils/service_message_factory'

import org.jetbrains.plugins.ruby.testing.ServiceMessageFactoryProvider
import org.jetbrains.plugins.ruby.testing.ServiceMessageFactory

#noinspection RubyInstanceMethodNamingConvention
class MyServiceMessageFactoryDelegate
  include ServiceMessageFactory

  FACTORY = Rake::TeamCity::MessageFactory

  def createSuiteStarted(suite_name)
    FACTORY.create_suite_started(suite_name)
  end

  def createSuiteStartedWithLocation(suite_name, location_url)
    FACTORY.create_suite_started(suite_name, location_url)
  end

  def createSuiteFinished(suite_name)
    FACTORY.create_suite_finished(suite_name)
  end

  def createTestsCount(count)
    FACTORY.create_tests_count(count)
  end

  def createTestStarted(test_name)
    FACTORY.create_test_started(test_name)
  end

  def createTestStartedWithLocation(test_name, location_url)
    FACTORY.create_test_started(test_name, location_url)
  end

  def createTestFinished(test_name, duration)
    FACTORY.create_test_finished(test_name, duration)
  end

  def createTestOutputMessage(test_name, is_std_out, out_text)
    FACTORY.create_test_output_message(test_name, is_std_out, out_text)
  end

  def createTestFailed(test_name, message, stacktrace)
    FACTORY.create_test_failed(test_name, message, stacktrace)
  end

  def createTestError(test_name, message, stacktrace)
    FACTORY.create_test_error(test_name, message, stacktrace)
  end

  def createTestIgnored(test_name, message)
    FACTORY.create_test_ignored(test_name, message)
  end

  def createProgressMessage(message)
    FACTORY.create_progress_message(message)
  end

  def createBuildErrorReport(message)
    FACTORY.create_build_error_report(message)
  end

  def createCustomProgressTestsCategory(category_name, count)
    FACTORY.create_custom_progress_tests_category(category_name, count)
  end

  def createCustomProgressTestStatus(status)
    FACTORY.create_custom_progress_test_status(status.to_s.to_sym)
  end

  def createMsgError(message, stacktrace)
    FACTORY.create_msg_error(message, stacktrace)
  end

  def createTestReportedAttached()
    FACTORY.create_test_reported_attached
  end
end
