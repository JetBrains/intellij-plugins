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
# @date: 18:04:35

require 'teamcity/rakerunner_consts'

module Rake
  module TeamCity
    module Utils
      class FileLogger
        def initialize(logs_dir, file_suffix)
          @enabled = ENV[logs_dir]

          @log_file = @enabled ? File.new(ENV[logs_dir] + file_suffix, "a+") : nil
        end

        def current_proc_thread_info
          "[pid:#{Process.pid}x#{Thread.current}]"
        end

        def log_msg(msg, add_proc_thread_info=false)
          if @enabled
            @log_file << "[#{Time.now}] : #{(add_proc_thread_info ? current_proc_thread_info : "") + " " + msg}\n"
            @log_file.flush
          end
        end

        def log_block(id, data = nil, add_proc_thread_info=false)
          log_msg("Start [#{id}]" + (data ? " data=[#{data}]" : ""), add_proc_thread_info);
          begin
            yield
          rescue Exception => e
            log_msg("Exception [#{id}], type=#{e.class.name}, msg=#{e.message}", add_proc_thread_info)
            raise
          end
          log_msg("End [#{id}]", add_proc_thread_info)
        end

        def close
          @log_file.close if @enabled
        end
      end

      class RakeFileLogger < FileLogger
        def initialize
          super(TEAMCITY_RAKERUNNER_LOG_PATH_KEY, TEAMCITY_RAKERUNNER_LOG_FILENAME_SUFFIX)
        end
      end

      class RPCMessagesLogger < FileLogger
        def initialize
          super(TEAMCITY_RAKERUNNER_LOG_PATH_KEY, TEAMCITY_RAKERUNNER_RPC_LOG_FILENAME_SUFFIX)
        end
      end

      class RSpecFileLogger < FileLogger
        def initialize
          super(TEAMCITY_RAKERUNNER_LOG_PATH_KEY, TEAMCITY_RAKERUNNER_SPEC_LOG_FILENAME_SUFFIX)
        end
      end

      class TestUnitFileLogger < FileLogger
        def initialize
          super(TEAMCITY_RAKERUNNER_LOG_PATH_KEY, TEAMCITY_RAKERUNNER_TESTUNIT_LOG_FILENAME_SUFFIX)
        end
      end

      class TestUnitEventsFileLogger < FileLogger
        def initialize
          super(TEAMCITY_RAKERUNNER_LOG_PATH_KEY, TEAMCITY_RAKERUNNER_TESTUNIT_EVENTS_LOG_FILENAME_SUFFIX)
        end
      end
    end
  end
end
