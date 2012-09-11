# encoding: UTF-8

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
######################
#noinspection RubyResolve
require "teamcity/rakerunner_consts"

module Rake
  module TeamCity
    module MessageFactory

      MSG_BLOCK_TYPES = {
          #      :build => "Build"               # BLOCK_TYPE_BUILD
          :progress => "$BUILD_PROGRESS$", # BLOCK_TYPE_PROGRESS
          :test => "$TEST_BLOCK$", # BLOCK_TYPE_TEST
          :test_suite => "$TEST_SUITE$", # BLOCK_TYPE_TEST_SUITE
          :compilation => "$COMPILATION_BLOCK$", # BLOCK_TYPE_COMPILATION
          :target => "$TARGET_BLOCK$", # BLOCK_TYPE_TARGET
          :task => "rakeTask"
      }

      MSG_STATUS_TYPES = {
          :warning => "WARNING",
          :error => "ERROR"
      }

      CUSTOM_MSG_TYPES = {
          :started => 'testStarted',
          :failed => 'testFailed'
      }

      MOCK_ATTRIBUTES_VALUES = {
          :details => {:value => '##STACK_TRACE##', :enabled => ::Rake::TeamCity.is_fake_stacktrace_enabled?, :remove_empty => true},
          :errorDetails => {:value => '##STACK_TRACE##', :enabled => ::Rake::TeamCity.is_fake_stacktrace_enabled?, :remove_empty => true},
          :locationHint => {:value => '##LOCATION_URL##', :enabled => ::Rake::TeamCity.is_fake_location_url_enabled?, :remove_empty => true},
          :duration => {:value => '##DURATION##', :enabled => ::Rake::TeamCity.is_fake_time_enabled?},
          :time => {:value => '##TIME##', :enabled => ::Rake::TeamCity.is_fake_time_enabled?},
          :error_msg => {:value => '##MESSAGE##', :enabled => ::Rake::TeamCity.is_fake_error_msg_enabled?}
      }

      def self.create_suite_started(suite_name, location_url = nil)
        create_message :message_name => "testSuiteStarted",
                       :name => suite_name,
                       :locationHint => location_url
      end

      def self.create_suite_finished(suite_name)
        create_message :message_name => "testSuiteFinished",
                       :name => suite_name
      end

      def self.create_tests_count(int_count)
        create_message :message_name => "testCount",
                       :count => int_count
      end

      def self.create_test_started(test_name, location_url = nil)
        create_message :message_name => "testStarted",
                       :name => test_name,
                       :captureStandardOutput => 'true',
                       :locationHint => location_url
      end

      # Duration in millisec
      def self.create_test_finished(test_name, duration_ms, diagnostic_info=nil)
        create_message :message_name => "testFinished",
                       :name => test_name,
                       :duration => [duration_ms, 0].max,
                       :diagnosticInfo => diagnostic_info
      end

      def self.create_test_output_message(test_name, is_std_out, out_text)
        create_message :message_name => "testStd#{is_std_out ? "Out" : "Err"}",
                       :name => test_name,
                       :out => out_text
      end

      def self.create_test_failed(test_name, message, stacktrace)
        stacktrace = format_stacktrace_if_needed(message, stacktrace)
        create_message :message_name => 'testFailed',
                       :name => test_name,
                       :message => message,
                       :details => stacktrace
      end

      def self.create_test_error(test_name, message, stacktrace)
        stacktrace = format_stacktrace_if_needed(message, stacktrace)
        create_message :message_name => 'testFailed',
                       :name => test_name,
                       :message => message,
                       :details => stacktrace,
                       :error => 'true'
      end

      def self.create_test_ignored(test_name, message, stacktrace = nil)
        create_message :message_name => 'testIgnored',
                       :name => test_name,
                       :message => message,
                       :details => stacktrace
      end

      # This message should show progress on buildserver and can be
      # ignored by IDE tests runners
      def self.create_progress_message(message)
        # This kind of message doesn't support timestamp attribute
        create_message :message_name => 'progressMessage',
                       :message_text => message
      end

      # This message should show custom build status on buildserver and can be
      # ignored by IDE tests runners
      def self.create_build_error_report(message)
        create_message :message_name => 'buildStatus',
                       :status => MSG_STATUS_TYPES[:error],
                       :text => message
      end

      def self.create_msg_error(message, stacktrace = nil)
        attrs = {
            :message_name => 'message',
            :text => message,
            :status => MSG_STATUS_TYPES[:error],
            :errorDetails => stacktrace
        }
        attrs[:text] &&= MOCK_ATTRIBUTES_VALUES[:error_msg][:value] if MOCK_ATTRIBUTES_VALUES[:error_msg][:enabled]

        create_message attrs
      end

      def self.create_msg_warning(message, stacktrace = nil)
        create_message :message_name => 'message',
                       :text => message,
                       :status => MSG_STATUS_TYPES[:warning],
                       :errorDetails => stacktrace
      end

      def self.create_open_target(message)
        create_message :message_name => 'blockOpened',
                       :name => message,
                       :type => MSG_BLOCK_TYPES[:target]
      end

      def self.create_close_target(message)
        create_message :message_name => 'blockClosed',
                       :name => message,
                       :type => MSG_BLOCK_TYPES[:target]
      end

      #noinspection RubyClassMethodNamingConvention
      def self.create_custom_progress_tests_category(category_name, int_count = 0)
        create_message :message_name => 'customProgressStatus',
                       :testsCategory => category_name,
                       :count => int_count
      end

      #noinspection RubyClassMethodNamingConvention
      def self.create_custom_progress_test_status(status)
        create_message :message_name => 'customProgressStatus',
                       :type => CUSTOM_MSG_TYPES[status]
      end

      def self.create_test_reported_attached
        # Allows to distinguish 2 situations
        # * nothing to test  - no tests, suites
        # * test reporter wasn't attached
        # Can be reported several times
        create_message :message_name => 'enteredTheMatrix'
      end

      ###################################################################
      ###################################################################
      ###################################################################

      def self.replace_escaped_symbols(text)
        copy_of_text = String.new(text)

        copy_of_text.gsub!(/\|/, "||")

        copy_of_text.gsub!(/'/, "|'")
        copy_of_text.gsub!(/\n/, "|n")
        copy_of_text.gsub!(/\r/, "|r")
        copy_of_text.gsub!(/\]/, "|]")

        copy_of_text.gsub!(/\[/, "|[")

        begin
          copy_of_text.encode!('UTF-8') if copy_of_text.respond_to? :encode!
          copy_of_text.gsub!(/\u0085/, "|x") # next line
          copy_of_text.gsub!(/\u2028/, "|l") # line separator
          copy_of_text.gsub!(/\u2029/, "|p") # paragraph separator
        rescue
          # it is not an utf-8 compatible string :(
        end

        copy_of_text
      end

      private
      def self.format_stacktrace_if_needed message, stacktrace
        if Rake::TeamCity.is_in_buildserver_mode()
          # At current moment TC doesn't extract message from corresponding attribute.
          # see [TW-6270] http://jetbrains.net/tracker/workspace?currentIssueId=TW-6270
          message + "\n\nStack trace:\n" + stacktrace
        else
          stacktrace
        end
      end

      protected

      def self.create_message(msg_attrs = {})
        # message type:
        message_name = msg_attrs.delete(:message_name)

        # optional body
        message_text = msg_attrs[:message_text]

        # if diagnostic info is null - don't pass it'
        diagnostic = msg_attrs[:diagnosticInfo]
        unless diagnostic
          msg_attrs.delete(:diagnosticInfo)
        end

        if message_text.nil?
          # mock some attrs
          [:details, :errorDetails, :locationHint, :duration].each do |key|
            if msg_attrs[key].nil?
              # if key is nil - don't include in msg attrs
              msg_attrs.delete(key) if MOCK_ATTRIBUTES_VALUES[key][:remove_empty]
            else
              # if not nil & debug mode - mock it
              msg_attrs[key] = MOCK_ATTRIBUTES_VALUES[key][:value] if MOCK_ATTRIBUTES_VALUES[key][:enabled]
            end
          end

          # add auto timestamp
          msg_attrs[:timestamp] ||= convert_time_to_java_simple_date(Time.now)

          # message args
          message_args = msg_attrs.map { |k, v| "#{k.to_s} = '#{v.nil? ? "" : replace_escaped_symbols(v.to_s)}'" }.join(" ")
        else
          message_args = "'#{message_text}'"
        end

        "##teamcity[#{message_name}#{message_args.empty? ? '' : " #{message_args}"}]"
      end

      #noinspection RubyClassMethodNamingConvention
      def self.convert_time_to_java_simple_date(time)
        if MOCK_ATTRIBUTES_VALUES[:time][:enabled]
          return MOCK_ATTRIBUTES_VALUES[:time][:value]
        end
        gmt_offset = time.gmt_offset
        gmt_sign = gmt_offset < 0 ? "-" : "+"
        gmt_hours = gmt_offset.abs / 3600
        gmt_minutes = gmt_offset.abs % 3600

        millisec = time.usec == 0 ? 0 : time.usec / 1000

        #Time string in Java SimpleDateFormat
        sprintf("#{time.strftime("%Y-%m-%dT%H:%M:%S.")}%03d#{gmt_sign}%02d%02d", millisec, gmt_hours, gmt_minutes)
      end
    end
  end
end
