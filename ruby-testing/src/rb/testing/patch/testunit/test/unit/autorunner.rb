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
# @date: 02.06.2007

require 'teamcity/rakerunner_consts'

ORIGINAL_SDK_AUTORUNNER_PATH = ENV[ORIGINAL_SDK_AUTORUNNER_PATH_KEY]
if ORIGINAL_SDK_AUTORUNNER_PATH
  require ORIGINAL_SDK_AUTORUNNER_PATH
end

module Test
  module Unit
    class AutoRunner
      RUNNERS[:teamcity] = proc do |r|
        require 'test/unit/ui/teamcity/testrunner'
        Test::Unit::UI::TeamCity::TestRunner
      end

       alias original_initialize initialize
       private :original_initialize

       def initialize(*args)
         original_initialize(*args)

         @runner = RUNNERS[:teamcity]
       end
    end
  end
end