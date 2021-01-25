require File.dirname(__FILE__) + '/runner_settings'
require 'teamcity/utils/runner_utils'
require 'rubymine_test_framework_initializer'

files = ENV["INTELLIJ_IDEA_RUN_CONF_TEST_FILE_PATH"] # separated by "||" filenames
if files.nil? or files.length == 0
   raise Exception.new("A test file must be provided")
end

files.split("||").each do |file|
   require file
end
