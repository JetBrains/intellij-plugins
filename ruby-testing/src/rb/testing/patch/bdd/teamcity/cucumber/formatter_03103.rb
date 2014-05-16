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

require File.expand_path(File.dirname(__FILE__) + '/common')

module Teamcity
  module Cucumber
    class Formatter
      include ::Teamcity::Cucumber::FormatterCommons

      def initialize(step_mother, path_or_io, options, delim='|')
        @step_mother = step_mother
        tc_initialize(options, delim, path_or_io)
      end

##################################################
# For tags gathering

      def tag_name(tag_name)
        tc_tag_name(tag_name)
      end

#### Features

      # @Processes: features
      def before_features(features)
        tc_before_features(features)
      end
      def after_features(features)
        tc_after_features(features)
      end

      # @Processes: tags, feature name, background and all feature elements
      def before_feature(feature)
        tc_before_feature(feature)
      end
      def after_feature(feature)
        tc_after_feature(feature)
      end

      # @Processes: feature name
      def feature_name(*args)
        args_count = args.size
        if args_count == 1
          # cucumber < 0.7.0
          new_feature_name = args[0]
        elsif args_count == 2
          # cucumber >= 0.7.0 : feature_name(keyword, name)
          new_feature_name = "#{args[0]}: #{args[1]}"
        else
          raise ArgumentError, "Expected 1 or 2 args, but #{args_count} were found"
        end
        tc_feature_name(new_feature_name)
      end

####  Examples:
      # Processes "Examples" section
      def before_examples(examples)
        tc_before_examples(examples)
      end
      def after_examples(examples)
        tc_after_examples(examples)
      end

      # Processes name of "Examples" section
      # Cucumber doesn't provide start .. end events
      # here we process examples started event manually
      def examples_name(keyword, name)
        # * cucumber < 0.7.0 - keyword is provided with trailing ':'
        # *          >= 0.7.0 - without trailing :
        tc_examples_name(keyword, name)
      end

#### Scenarios, Scenario Outlines and Backgrounds:
      # @Processes: Background
      def before_background(background)
        tc_before_background(background)
      end
      def after_background(background)
        tc_after_background(background)
      end


#### Scenarios, Scenario Outlines(real and fake from Example's table rows):
      # @Processes: Scenario(real without examples' fake scenarios), ScenarioOutline
      def before_feature_element(feature_element)
        tc_before_feature_element(feature_element)
      end
      def after_feature_element(feature_element)
        tc_after_feature_element(feature_element)
      end

      # @Processes: Scenario (fake and real), ScenarioOutline names
      # scenario/scenario_outline contains of name and steps
      def scenario_name(keyword, name, file_colon_line, source_indent)
        # * cucumber < 0.7.0 - keyword is provided with trailing ':'
        # *          >= 0.7.0 - without trailing :
        tc_scenario_name(file_colon_line, keyword, name, source_indent)
      end

#### Steps and Cells:
      # Processes: StepInvocation
      def before_step(step)
        tc_before_step(step)
      end
      def after_step(step)
        tc_after_step(step)
      end

      # result of step invocation - calls v_exception, v_step_name
      def before_step_result(keyword, step_match, multiline_arg, status, exception, source_indent, background, file_colon_line = nil)
        tc_before_step_result(exception, keyword, multiline_arg, source_indent, status, step_match, background, file_colon_line)
      end

#### Exceptions:
      def exception(exception, status)
        tc_exception(exception, status)
      end
    end
  end
end
