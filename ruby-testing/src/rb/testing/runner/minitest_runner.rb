require File.dirname(__FILE__) + '/runner_settings'
require 'teamcity/utils/runner_utils'
require 'rubymine_test_framework_initializer'

file = ENV["INTELLIJ_IDEA_RUN_CONF_TEST_FILE_PATH"]
if file.nil? or file.length == 0
   raise Exception.new("A test file must be provided")
end

require file
