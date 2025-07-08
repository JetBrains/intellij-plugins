require 'teamcity/rakerunner_consts'

ORIGINAL_SDK_TEST_UNIT_PATH = ENV[ORIGINAL_SDK_TEST_UNIT_PATH_KEY]
if ORIGINAL_SDK_TEST_UNIT_PATH
  require ORIGINAL_SDK_TEST_UNIT_PATH
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