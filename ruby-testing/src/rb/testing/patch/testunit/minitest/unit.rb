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

require 'teamcity/rakerunner_consts'

RUBY19_SDK_MINITEST_RUNNER_PATH = ENV[::Rake::TeamCity::RUBY19_SDK_MINITEST_RUNNER_PATH_KEY]
if RUBY19_SDK_MINITEST_RUNNER_PATH
  require RUBY19_SDK_MINITEST_RUNNER_PATH
else
  # if we are loaded w/o real version of minitest specified
  # we should fail so caller wouldn't think that there is a minitest
  # see RUBY-10545 for more information
  raise LoadError.new
end

REPORTERS_NAME = 'minitest-reporters'
REPORTERS_VERSION = '>= 0.5.0'
#noinspection RubyLocalVariableNamingConvention
minitest_reporters_gem_detected = false
if Gem
  begin
    minitest_reporters_gem_detected = Gem::Specification.find_by_name(REPORTERS_NAME, REPORTERS_VERSION)
  rescue Gem::LoadError
    false
  rescue
    minitest_reporters_gem_detected = Gem.available?(REPORTERS_NAME, REPORTERS_VERSION)
  end
else
  $:.each do |path|
    unless  path["/gems/minitest-reporters"].nil?
      minitest_reporters_gem_detected = true
      break
    end
  end
end

unless minitest_reporters_gem_detected
  module MiniTest
    class Unit
      class << self
        @@jb_runner_wrapper_applicable = MiniTest::Unit.respond_to?(:autorun, true)
        if @@jb_runner_wrapper_applicable
          alias jb_original_autorun autorun
          private :jb_original_autorun
        end

        def autorun(*args)
          jb_original_autorun(*args)
        end

        # warn user:
        product_name = ::Rake::TeamCity.is_in_buildserver_mode ? "TeamCity" : "RubyMine/IDEA Ruby plugin"
        msg = %Q{
MiniTest framework was detected. It is a lightweight version of original Test::Unit framework.
#{product_name} test runner requires '#{REPORTERS_NAME}' (#{REPORTERS_VERSION}) for integration
with MiniTest framework (see http://www.jetbrains.com/ruby/webhelp/minitest.html).
Or you can use full-featured Test::Unit framework version, provided by
'test-unit' gem, otherwise default console tests reporter will be used instead.

        }
        STDERR.flush
        STDOUT.flush
        STDERR.puts msg
        STDERR.flush
        STDOUT.flush

        unless @@jb_runner_wrapper_applicable
          STDERR.puts "Error: Cannot delegate to original 'minitest\\unit.rb' script.\n\n"
          exit(1)
        end
      end
    end
  end
end