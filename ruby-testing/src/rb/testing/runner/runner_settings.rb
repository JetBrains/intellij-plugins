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

#########################################
# Settings
#########################################

module IntelliJ
  ######## Environment parameters ################

  # TestUnit run Class
  SCRIPT_PATH = ENV["INTELLIJ_IDEA_RUN_CONF_SCRIPT_PATH"]

  # TestUnit run Class
  # TestUnit run all in folder
  # RSpec run all in folder
  USE_CONSOLE_RUNNER = ENV["INTELLIJ_IDEA_RUN_CONF_USE_CONSOLE_RUNNER"] ? true : false

  # TestUnit run all in folder
  # RSpec run all in folder
  FOLDER_PATH = ENV["INTELLIJ_IDEA_RUN_CONF_FOLDER_PATH"]

  TUNIT_DRB_RUNNER_PATH = ENV["INTELLIJ_IDEA_TUNIT_DRB_RUNNER_PATH"]

  # TestUnit run all in folder
  # RSpec run all in folder
  SEARCH_MASK = ENV["INTELLIJ_IDEA_RUN_CONF_SEARCH_MASK"] # **/*.rb or **/*_spec.rb, etc.

  # TestUnit run all in folder
  # RSpec run all in folder
  WORK_DIR = ENV["INTELLIJ_IDEA_RUN_CONF_WORKING_DIR"]

  # RSpec run all in folder
  SPEC_SCRIPT = ENV["INTELLIJ_IDEA_RUN_CONF_SPEC_RUNNER_SCRIPT"]

  # Gem version requirement
  RUNNER_GEM_REQUIREMENT = ENV["INTELLIJ_IDEA_RUN_CONF_RUNNER_GEM_REQUIREMENT"]

  # RCov arguments
  RCOV = ENV["INTELLIJ_IDEA_RCOV"]

  if RCOV
    RCOV_ARGS = []
    env = ""
    i = 0
    while env do
      env = ENV["INTELLIJ_IDEA_RCOV_ARG#{i}"]
      RCOV_ARGS << env if env
      i += 1
    end
  end

  # RSpec run all in folder
  LOAD_SEPARATELY = ENV["INTELLIJ_IDEA_RUN_CONF_RUN_SPECS_SEPARATELY"] ? true : false

  # RSpec run all in folder  
  RUBY_ARGS_STRING = ENV["INTELLIJ_IDEA_RUN_CONF_RUBY_ARGS"]
  SCRIPT_ARGS_STRING = ENV["INTELLIJ_IDEA_RUN_CONF_SCRIPT_ARGS"]

  # RSpec run all in folder
  IDEA_RSPEC_FORMATTER_CLASS_NAME = ENV["INTELLIJ_IDEA_RUN_CONF_IDEA_SPEC_FORMATTER_CLASS_NAME"]

  # RSpec run all in folder
  IDEA_RSPEC_FORMATTER_PATH = ENV["INTELLIJ_IDEA_RUN_CONF_IDEA_SPEC_FORMATTER_PATH"]

  ##### Other Settings ###########################

  # Ruby interpreter path and args (e.g of run all in folder)
  RUBY_INTERPRETER_CMDLINE = ENV["INTELLIJ_IDEA_RUN_CONF_LAUNCHER_CMDLINE"]

  # Parses string with launcher settings
  def self.parse_launcher_string(args_string, args_array)
    args_string.split("\n").each { |item|
      if '\'$stdout.sync=true;$stderr.sync=true;load($0=ARGV.shift)\'' == item
        item = '$stdout.sync=true;$stderr.sync=true;load($0=ARGV.shift)'
      end
      (args_array << item) unless item.strip.empty?
    }
  end
end