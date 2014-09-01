# Copyright 2000-2014 JetBrains s.r.o.
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

# Change list
#
# 30.09.2009
#   Support for cucumber >= 0.3.103 new api
#
# 15.05.2009
#   Fully rewritten using RubyMine ServiceMessage API. Also semantics was changed:
#      * steps should be reported as tests
#      * scenarios, features as suites
#   New cucumber 0.3.6 API was used (including --expand) option
#
# 14.05.2009
#  Initial version was given from http://github.com/darrell/cucumber_teamcity/tree/master
#  Thanks to Darrell Fuhriman (darrell [at] garnix.org)
require 'cucumber/formatter/console'
require 'fileutils'

require 'teamcity/runner_common'
require 'teamcity/utils/service_message_factory'
require 'teamcity/utils/runner_utils'
require 'teamcity/utils/url_formatter'

module Teamcity
  module Cucumber

    def self.same_or_newer?(version)
      given_version = version.split('.', 4)
      cuke_version = ::Cucumber::VERSION.split('.', 4)
      while cuke_version.size < given_version.size
        cuke_version << "0"
      end
      cuke_version.each_with_index do |num, i|
        gnum = given_version[i]
        if num =~ /\d*/ && gnum =~ /\d*/ && num.to_i > gnum.to_i
          return true
        elsif num =~ /\d*/ && gnum =~ /a-zA-Z/
          return true
        end
      end
      false
    end

    # old formatter api, cucumber < 0.3.103
    # new formatter api, cucumber >= 0.3.103

    USE_OLD_API =  !same_or_newer?('1.2.0') && (defined? ::Cucumber::Ast::TreeWalker).nil?
    CUCUMBER_VERSION_2 = (::Cucumber::VERSION.split('.')[0] == '2')

    if USE_OLD_API
      require File.expand_path(File.dirname(__FILE__) + '/old_formatter')
    else
      require File.expand_path(File.dirname(__FILE__) + '/formatter_03103')
    end
  end
end