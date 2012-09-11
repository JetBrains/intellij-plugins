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

module Rake
  module TeamCity
    module RunnerUtils

      ###########################################################################
      ####  Test::Unit
      ###########################################################################

      # Converts Ruby Test Names : $TEST_METHOD_NAME($TEST_CASE_QUALIFIED_NAME)
      # to Qualified name format :  $TEST_CASE_QUALIFIED_NAME.$TEST_METHOD_NAME
      def convert_ruby_test_name_to_qualified(ruby_name)
        if ruby_name && (ruby_name.strip =~ /(\S+)\(([\w:]*)\)/)
          # p [$1, $2]
          method_name = $1
          qualified_name = $2
          return convert_test_unit_to_qualified(qualified_name, method_name)
        end
        ruby_name
      end

      def convert_test_unit_to_qualified(class_qualified_name, method_name)
        if class_qualified_name.empty?
          "#{method_name}"
        else
          "#{class_qualified_name}.#{method_name}"
        end
      end

      def self.use_minitest?
        defined? ::MiniTest::Unit::TestCase
      end

      ###########################################################################
      ####  RSpec
      ###########################################################################

      #@Nullable
      #@returns pair of two strings: [source file path, line in source file] or [nil, nil]
      def extract_source_location_from_example(example)
        #example.instance_variable_hash['@_implementation'].to_s.gsub(/#<Proc:.+@/, "")

        #TODO - replace with example full name!!!!!

        if example.respond_to?(:location)
          # rspec 1.2.1 API
          return extract_rspec_proxy_location(example)
        elsif (example.respond_to?(:metadata))
          # rspec 2.0 beta API
          return parse_rspec_proxy_location(example.metadata[:location])
        end

        proc = (example.respond_to?(:instance_variable_hash)) ? example.instance_variable_hash['@_implementation'] : nil

        if !proc.nil? && proc.is_a?(Proc)
          return extract_source_location_from_closure(proc.to_s)
        end

        return nil, nil
      end

      #[@Nullable, @Nullable]
      def get_pair_by(src_file_path_str, src_file_line_str)
        if src_file_path_str && src_file_line_str
          return File.expand_path(src_file_path_str), src_file_line_str
        end

        return nil, nil
      end

      #@Nullable
      #@returns pair of two strings: [source file path, line in source file] or [nil, nil]
      def extract_source_location_from_path_info(spec_path_info)
        # E.g.: "/Users/romeo/IdeaProjects/dianaplugin/rails/spec/my_example_spec.rb:4"
        if spec_path_info =~ /(^)([^:]+)(:)(\d+)(\D*)($)/
          src_file_path_str = $2
          src_file_line_str = $4
          if src_file_path_str && src_file_line_str
            return src_file_path_str, src_file_line_str
          end
        elsif spec_path_info =~ /(^)([^:]+)($)/
          return spec_path_info, "0"
        end

        return nil, nil
      end

      @@ignore_root_test_case = true

      # usually we should ignore root test case that is test file
      # but in run_all_in_folder case we shouldn't ignore it!
      def self.ignore_root_test_case?
        @@ignore_root_test_case
      end

      def self.ignore_root_test_case=(value)
        @@ignore_root_test_case = value
      end


      def self.excluded_default_testcase_name?(suite_or_test_name)
        ::Rake::TeamCity::TC_EXCLUDED_DEFAULT_TEST_CASES.index(suite_or_test_name) != nil
      end

      def self.excluded_default_testcase?(suite_or_test)
        excluded_default_testcase_name?(suite_or_test.name) && (suite_or_test.size == 1)
      end

      def self.fake_default_test_for_empty_suite?(suite_or_test)
        !(defined? suite_or_test.tests) && ("default_test" == suite_or_test.method_name)
      end

      ############################################################
      ############################################################

      private

      #[@Nullable, @Nullable]
      #Exctracting location using new RSpec 1.2.1 API
      #@returns pair of two stings [source file path, line in source file] or [nil, nil]
      def extract_rspec_proxy_location(proxy_object)
        parse_rspec_proxy_location proxy_object.location
      end

      def parse_rspec_proxy_location(location)
        #TODO Add test for it!!!!!
        if location =~ /(.+):(\d+)/
          return get_pair_by($1, $2)
        end
        return nil, nil
      end

      #@Nullable
      #@returns pair of two strings: [source file path, line in source file] or [nil, nil]
      def extract_source_location_from_group(example_group)
        if example_group
          if example_group.respond_to?(:location)
            # rspec 1.2.1 API
            return extract_rspec_proxy_location(example_group)
          elsif (example_group.respond_to?(:metadata))
            # rspec 2.0 beta API
            return parse_rspec_proxy_location(example_group.metadata[:example_group][:location])
          elsif (example_group.respond_to?(:spec_path))
            return extract_source_location_from_path_info(example_group.spec_path)
          end
        end
        return nil, nil
      end

      #[@Nullable, @Nullable]
      #@returns pair of two strings: [source file path, line in source file] or [nil, nil]
      def extract_source_location_from_closure(closure_id)
        # E.g.: "#<Proc:0xaa3e9a@/Users/romeo/IdeaProjects/dianaplugin/rails/spec/my_example_spec.rb:16>"
        if closure_id =~ /(#<[^:]+:[^@]+@)([^:]+)(:)(\d+)(>)/
          return get_pair_by($2, $4)
        end

        return nil, nil
      end
    end
  end
end