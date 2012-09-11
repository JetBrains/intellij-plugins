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
# @date: 10.01.2008
require 'teamcity/rakerunner_consts'

module Rake
  module TeamCity

    # Captures STDOUT and STDERR
    module StdCaptureHelper
      require 'tempfile'

      def isCaptureDisabled()
        ENV[TEAMCITY_RAKERUNNER_LOG_OUTPUT_CAPTURER_ENABLED_KEY] != "true"
      end

      def capture_output_start_external
        old_out, old_err = copy_stdout_stderr

        if isCaptureDisabled()
          return old_out, old_err, nil, nil
        end

        new_out = Tempfile.new("tempfile_out")
        new_err = Tempfile.new("tempfile_err")

        reopen_stdout_stderr(new_out, new_err)

        return old_out, old_err, new_out, new_err
      end

      def copy_stdout_stderr
         if isCaptureDisabled()
           return STDOUT, STDERR
         else
           return STDOUT.dup, STDERR.dup
         end
      end

      def reopen_stdout_stderr(sout, serr)
        STDOUT.reopen(sout)
        STDERR.reopen(serr)
        nil
      end

      # returns STDOUT and STDERR content
      def capture_output_end_external(old_out, old_err, new_out, new_err)
        STDOUT.flush
        STDERR.flush

        if isCaptureDisabled()
          return "", ""
        end

        reopen_stdout_stderr(old_out, old_err)

        return get_redirected_stdout_stderr_from_files(new_out, new_err)
      end

      # Closes files' streams and gets its output.
      def get_redirected_stdout_stderr_from_files(new_out, new_err)
        if isCaptureDisabled()
          return "", ""
        end

        begin
          new_out.close
          new_out.open
          s_out = new_out.readlines.join
          new_out.close
        rescue Exception => ex
          s_out = "Error: Teamcity agent is unable to capture STDOUT: #{ex}"
        end

        begin
          new_err.close
          new_err.open
          s_err = new_err.readlines.join
          new_err.close
        rescue Exception => ex
          s_err = "Error: Teamcity agent is unable to capture STDERR: #{ex}"
        end

        return s_out, s_err
      end
    end
  end
end
