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
#
# @author: Roman Chernyatchik

module Rake
  module TeamCity
    module RunnerCommon
      # Let's keep STDOUT, our users likes to capture it and block our events
      @@original_stdout = STDOUT

      # Current time in ms
      def get_current_time_in_ms
        get_time_in_ms(Time.now)
      end

      def get_time_in_ms(time)
        ((time.to_f) * 1000 ).to_i
      end

      # Sends msg to runner
      def send_msg(msg)
        @@original_stdout.flush
        @@original_stdout.puts("\n#{msg}")
        @@original_stdout.flush
      end

      # Sets factory for creating messages
      def set_message_factory(factory)
        @message_factory = factory
      end

      def log_test_reporter_attached
        if ::Rake::TeamCity.is_in_idea_mode
          # log method is be defined in target class
          log(@message_factory.create_test_reported_attached)
        end
      end

      # Is from base_text_formatter.rb of rspec 1.1.4
      def self.backtrace_line(line)
        line.sub(/\A([^:]+:\d+)$/, '\\1:')
      end

      def self.format_backtrace(backtrace)
        return "" if backtrace.nil?
        backtrace.map { |line| backtrace_line(line) }.join("\n")
      end
    end
  end
end
