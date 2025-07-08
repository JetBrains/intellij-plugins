require 'teamcity/rakerunner_consts'

ORIGINAL_SDK_TEST_UNIT_PATH = ENV[ORIGINAL_SDK_TEST_UNIT_PATH_KEY]
if ORIGINAL_SDK_TEST_UNIT_PATH
  require ORIGINAL_SDK_TEST_UNIT_PATH
end

Test::Unit::AutoRunner.register_runner(:teamcity) do |auto_runner|
  require_relative 'unit/ui/teamcity/testrunner'
  Test::Unit::UI::TeamCity::TestRunner
end

Test::Unit::AutoRunner.default_runner = :teamcity
