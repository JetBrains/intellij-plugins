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

# -------------------------------------------------
# -- Runner for Test::Unit / Shoulda / Minitest ---
# -------------------------------------------------

#########################################
# Settings
#########################################
require File.dirname(__FILE__) + '/runner_settings'
require 'teamcity/utils/runner_utils'
require 'teamcity/utils/service_message_factory'

def collect_test_scripts()
  test_scripts = []
  Dir["#{IntelliJ::FOLDER_PATH}/#{IntelliJ::SEARCH_MASK}"].each { |file|
    next if File.directory?(file)

    # else just collect tests and run them using Drb
    test_scripts << file
  }
  test_scripts
end

def require_all_test_scripts(test_scripts)
  puts "Loading files.... "
  puts SEPARATOR

  i = 1
  test_scripts.each do |test_script|
    begin
      # Changes directory before require, because previous required script can change it
      Dir.chdir(IntelliJ::WORK_DIR) if IntelliJ::WORK_DIR

      # if no DRB - load scripts to ObjectSpace and run in the same process
      # such way will support debugging out of the box
      require test_script

      puts "#{i}. #{test_script}:1"
      i += 1
    rescue Exception => e
      message_factory = Rake::TeamCity::MessageFactory
      test_name = test_script[IntelliJ::FOLDER_PATH.length + 1 .. -1]
      puts message_factory.create_test_started(test_name)
      puts message_factory.create_test_failed(
               test_name,
               "Fail to load: #{test_script}:1\n      Exception message: #{e}",
               e.backtrace
           )
      puts message_factory.create_test_finished(test_name, 0)
    end
  end
  puts " \n"
  puts "#{i-1} files were loaded."
end

# DRB: just pass tests files to drb runner
def drb_launch_tests(drb_runner, test_scripts)
  cmdline = []

  IntelliJ::parse_launcher_string(IntelliJ::RUBY_INTERPRETER_CMDLINE, cmdline)

  # drb runner
  cmdline << drb_runner

  if drb_runner.end_with?('zeus')
    cmdline << 'test'
  elsif drb_runner.end_with?('spring')
    cmdline << 'testunit'
  else
    load_path = cmdline.find_all{ |param|
      (not param.nil?) and param.start_with?('-I')
    }
    cmdline.concat load_path
  end

  ARGV.each { |arg|
    cmdline << arg
  }

  # tests to launch
  cmdline.concat(test_scripts)

  puts 'Command line: '
  p cmdline

  require 'rubygems'
  require 'rake'
  puts sh(*cmdline)
end

#########################################
#########################################
#########################################
#########################################
# Runner
#########################################
#########################################
#########################################
#########################################
SEPARATOR = "========================================="

# If work directory was specified
puts "Work directory: #{IntelliJ::WORK_DIR}" if IntelliJ::WORK_DIR

# Drb
drb_runner = IntelliJ::TUNIT_DRB_RUNNER_PATH

test_scripts = collect_test_scripts()

unless drb_runner.nil?
  # DRB
  puts " \n"
  puts "#{test_scripts.length} test scripts were detected:"
  puts SEPARATOR

  test_scripts.each_with_index do |test_script, i|
    puts "#{i+1}. #{test_script}:1"
  end
  puts SEPARATOR

  # Parses launch arguments - ruby interpreter and its arguments
  drb_launch_tests(drb_runner, test_scripts)
else
  # usual mode: tests will be launched in same process
  require_all_test_scripts(test_scripts)
  puts SEPARATOR

  puts "Running tests..."

  unless ::Rake::TeamCity::RunnerUtils.use_minitest?
    # TestUnit:
    ::Rake::TeamCity::RunnerUtils.ignore_root_test_case = true
    # Do nothing: 'testunit/autorun' will launch all stuff
  else
    # MiniTest:
    # Do nothing: 'minitest/autorun' will register and launch all stuff
  end
end