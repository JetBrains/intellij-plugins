require File.dirname(__FILE__) + '/runner_settings'
require 'teamcity/utils/runner_utils'
require 'rubymine_test_framework_initializer'

if ARGV.length > 0
  file = ARGV.shift
  require file
else
  throw Exception("A test file must be provided")
end


