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
    class Formatter < ::Cucumber::Ast::Visitor
      include ::Teamcity::Cucumber::FormatterCommons

      def initialize(step_mother, io, options, delim='|')
        super(step_mother)
        # io - ignored

        tc_initialize(options, delim, io)
      end

##################################################
# For tags gathering
      def visit_tag_name(tag_name)
        tc_tag_name(tag_name)
        super
      end

#### Features
      # @Processes: features
      # Will be invoked before all features.
      # we will use this method for runner initialization and
      # test count reporting
      def visit_features(features)
        tc_before_features(features)
        super
        tc_after_features(features)
      end

      # @Processes: tags, feature name, background and all feature elements
      # we use to close feature in the end (after all elements will be processed)
      def visit_feature(feature)
        tc_before_feature(feature)
        super
        tc_after_feature(feature)
      end

      # @Processes: feature name# We use for logging feature started event
      def visit_feature_name(new_feature_name)
        tc_feature_name(new_feature_name)
        super
      end

####  Examples:
      # Processes "Examples" section
      # Cucumber doesn't provide start .. end events
      # here we process examples ended event manually
      def visit_examples(examples)
        tc_before_examples(examples)
        super
        tc_after_examples(examples)
      end

      # Processes name of "Examples" section
      # Cucumber doesn't provide start .. end events
      # here we process examples started event manually
      def visit_examples_name(keyword, name)
        tc_examples_name(keyword, name)
        super
      end

#### Scenarios, Scenario Outlines and Backgrounds:
      # @Processes: Background
      # Actually all background steps will be merged with other steps of
      # 'scenarios'/'outline examples' so we shouldn't show Background as separate test suite
      def visit_background(background)
        tc_before_background(background)
        super
        tc_after_background(background)
      end

#### Scenarios, Scenario Outlines(real and fake from Example's table rows):
      # @Processes: Scenario(real without examples' fake scenarios), ScenarioOutline
      # Here we can do cleanup for scenarios in current feature: Scenario, ScenarioOutline
      def visit_feature_element(feature_element)
        tc_before_feature_element(feature_element)
        super
        tc_after_feature_element(feature_element)
      end

      # @Processes: Scenario (fake and real), ScenarioOutline names
      # scenario/scenario_outline contains of name and steps
      def visit_scenario_name(keyword, name, file_colon_line, source_indent)
        tc_scenario_name(file_colon_line, keyword, name, source_indent)
        super
      end

#### Steps and Cells:
      # Processes: StepInvocation
      # We will remember before step invocation and then
      # compare with time at "visit_step_result" moment
      def visit_step(step)
        tc_before_step(step)
        super
        tc_after_step(step)
       end

      # result of step invocation - calls v_exception, v_step_name
      def visit_step_result(keyword, step_match, multiline_arg, status, exception, source_indent, background)
        tc_before_step_result(exception, keyword, multiline_arg, source_indent, status, step_match, background)
        super
      end
    end
  end
end