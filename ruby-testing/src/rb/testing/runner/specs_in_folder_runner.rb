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
require File.dirname(__FILE__) + '/runner_settings'

#########################################
# Runner Methods
#########################################
require 'rubygems'
require 'rake'

SEPARATOR = "========================================="
LAUNCHER_ARGS = []


# Searches spec files in given folder using given search mask
def search_files
  specs = []

  if IntelliJ::SEARCH_MASK.strip.empty?
    specs << IntelliJ::FOLDER_PATH
  else
    puts SEPARATOR
    Dir["#{IntelliJ::FOLDER_PATH}/#{IntelliJ::SEARCH_MASK}"].each { |file_or_folder|
      #next if File.directory?(testCase)
      specs << file_or_folder
    }
  end
  specs
end

# Prints list of files to output
def print_filenames(files)
  i = 1
  files.each { |file|
    puts "#{i}. #{file}:1"
    i+=1
  }
  puts "\n\n#{i-1} files were found."
end

# Generates command line to run spec files
# files - Spec files
#
# returns - Command line sting
def generate_cmd_line(files)
  cmd_line = []

  # Interpreter && its args
  LAUNCHER_ARGS.each { |option|
    cmd_line << option
  }

  if IntelliJ::RCOV
    cmd_line << IntelliJ::RCOV
  end

  # Spec
  cmd_line << IntelliJ::SPEC_SCRIPT

  if IntelliJ::RCOV
    cmd_line.push(*IntelliJ::RCOV_ARGS)
    cmd_line << "--"
  end

  # Spec arguments
  # Gem version
  if IntelliJ::RUNNER_GEM_REQUIREMENT
    cmd_line << IntelliJ::RUNNER_GEM_REQUIREMENT
  end

  unless IntelliJ::USE_CONSOLE_RUNNER
    # IntelliJ IDEA spec formatter
    cmd_line << "--require"
    cmd_line << IntelliJ::IDEA_RSPEC_FORMATTER_PATH
    cmd_line << "--format"
    cmd_line << IntelliJ::IDEA_RSPEC_FORMATTER_CLASS_NAME
  end
  files.each { |file|
    cmd_line << file
  }
  ARGV.each { |arg|
    cmd_line << arg
  }
  puts 'Command line: '
  p cmd_line
  cmd_line
end

#########################################
# Runner Body
#########################################

# Parses launch arguments and fills LAUNCHER_ARGS array
IntelliJ::parse_launcher_string(IntelliJ::RUBY_INTERPRETER_CMDLINE, LAUNCHER_ARGS)

# If work directory was specified
puts "Work directory: #{IntelliJ::WORK_DIR}" if IntelliJ::WORK_DIR

puts "Searching files.... "
specs = search_files

# Prints list of found files
print_filenames(specs)
puts SEPARATOR

# Prints environment description
puts "RSpec script : #{IntelliJ::SPEC_SCRIPT}\n"

puts "\nSpec Options:"
p ARGV
if IntelliJ::RCOV
  puts "\nRCov Options:"
  p IntelliJ::RCOV_ARGS
end
puts SEPARATOR

# Executes specs
puts "Running specs..."
FileUtils::RUBY.gsub!(/^"|"$/, '')
if !IntelliJ::LOAD_SEPARATELY || IntelliJ::RCOV
  Dir.chdir(IntelliJ::WORK_DIR) if IntelliJ::WORK_DIR # not necessary
  puts sh(*generate_cmd_line(specs))
else
  specs.each { |spec|
    Dir.chdir(IntelliJ::WORK_DIR) if IntelliJ::WORK_DIR
    puts sh(*generate_cmd_line([spec]))
  }
end

