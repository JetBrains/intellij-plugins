# Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

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

def collect_test_scripts
  test_scripts = []
  Dir["#{IntelliJ::FOLDER_PATH}/#{IntelliJ::SEARCH_MASK}"].each { |file|
    next if File.directory?(file)

    # else just collect tests and run them using Drb
    test_scripts << file
  }
  test_scripts
end

def require_all_test_scripts(test_scripts)
  i = 1
  test_scripts.each do |test_script|
    begin
      # Changes directory before require, because previous required script can change it
      Dir.chdir(IntelliJ::WORK_DIR) if IntelliJ::WORK_DIR

      # if no DRB - load scripts to ObjectSpace and run in the same process
      # such way will support debugging out of the box
      require test_script

      i += 1
    rescue Exception => e
      message_factory = Rake::TeamCity::MessageFactory
      test_name = test_script[IntelliJ::FOLDER_PATH.length + 1 .. -1]
      puts message_factory.create_test_started(test_name, nil, '0')
      puts message_factory.create_test_failed(
               test_name,
               "Fail to load: #{test_script}:1\n      Exception message: #{e}",
               e.backtrace
           )
      puts message_factory.create_test_finished(test_name, 0)
    end
  end
end

# DRB: just pass tests files to drb runner
def drb_launch_tests(drb_runner, test_scripts, test_scripts_names, test_names)
  cmdline = []

  Dir.chdir(IntelliJ::WORK_DIR) if IntelliJ::WORK_DIR

  IntelliJ::parse_launcher_string(IntelliJ::RUBY_INTERPRETER_CMDLINE, cmdline)

  # drb runner
  cmdline << drb_runner

  if drb_runner.end_with?('spring')
    test_name_pattern = get_test_name_pattern(test_names)

    rails = Gem.loaded_specs['rails']

    version = rails ? rails.version : Gem::Version.new(IntelliJ::RAILS_VERSION || 0)

    if version && version >= Gem::Version.new('4')
      cmdline << 'rake'
      cmdline << 'test'

      ARGV.each { |arg|
        cmdline << arg
      }

      test_script_pattern = test_scripts_names.empty? ? "**/*_test.rb": test_scripts_names.join(",")
      cmdline << "TEST=#{IntelliJ::FOLDER_PATH}/{#{test_script_pattern}}"
      cmdline << "TESTOPTS=#{test_name_pattern}" unless test_names.empty?
    else
      cmdline << 'testunit'

      ARGV.each { |arg|
        cmdline << arg
      }

      cmdline << test_name_pattern

      cmdline.concat(test_scripts)
    end

  else
    if drb_runner.end_with?('zeus')
      cmdline << 'test'
    else
      load_path = cmdline.find_all {|param|
        (not param.nil?) and param.start_with?('-I')
      }
      cmdline.concat load_path
    end

    ARGV.each { |arg|
      cmdline << arg
    }

    # tests to launch
    cmdline.concat(test_scripts)
  end

  puts 'Command line: '
  p cmdline

  require 'rubygems'
  require 'rake'

  result = sh(*cmdline) do |ok, res|
    unless ok
      puts "Exit code: #{res.exitstatus}"
    end
  end
  puts result
end

def get_test_scripts(names)
  names.map do |name|
    File.join("#{IntelliJ::FOLDER_PATH}", "#{name}")
  end
end

def get_test_name_pattern(test_names)
  "--name=/#{test_names.join("|")}/"
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

# If work directory was specified
puts "Work directory: #{IntelliJ::WORK_DIR}" if IntelliJ::WORK_DIR

# Drb
drb_runner = IntelliJ::TUNIT_DRB_RUNNER_PATH

test_scripts_names = []
test_scripts = collect_test_scripts
test_names = []

array = "#{IntelliJ::SEARCH_MASK}".scan(/([.\/\w]*)#(\w*)/)

unless array.empty?
  h = Hash.new {|h, k| h[k] = []}
  array.each {|k, v| h[k] << v}

  test_scripts_names = h.keys
  test_scripts = get_test_scripts(test_scripts_names)
  test_names << h.values
end

unless drb_runner.nil?
  # Parses launch arguments - ruby interpreter and its arguments
  drb_launch_tests(drb_runner, test_scripts, test_scripts_names, test_names)
else
  ARGV << get_test_name_pattern(test_names) unless test_names.empty?

  # usual mode: tests will be launched in same process
  require_all_test_scripts(test_scripts)

  unless ::Rake::TeamCity::RunnerUtils.use_minitest?
    # TestUnit:
    ::Rake::TeamCity::RunnerUtils.ignore_root_test_case = true
    # Do nothing: 'testunit/autorun' will launch all stuff
  else
    # MiniTest:
    # Do nothing: 'minitest/autorun' will register and launch all stuff
  end
end