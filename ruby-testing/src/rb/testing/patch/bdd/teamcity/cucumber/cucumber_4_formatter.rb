require 'cucumber/formatter/backtrace_filter'
require 'cucumber/formatter/io'
require 'cucumber/formatter/ast_lookup'

require File.expand_path(File.dirname(__FILE__) + '/common')

module Teamcity
  module Cucumber
    class Formatter
      EXAMPLES_NODE = 'Examples'
      RULE_NODE_PREFIX = 'Rule: '
      FEATURE_NODE_PREFIX = 'Feature: '

      include ::Teamcity::Cucumber::FormatterCommons
      include ::Cucumber::Formatter::Io

      attr_reader :config, :options

      def initialize(config)
        if method(:ensure_io).arity == 2 # 2 arguments needed since Cucumber 5.2.0
          @io = ensure_io(config.out_stream, config.error_stream)
        else
          @io = ensure_io(config.out_stream)
        end
        @config = config
        @ast_lookup = ::Cucumber::Formatter::AstLookup.new(config)
        @passed_test_cases = []
        @current_scenario_outline = nil
        @current_feature_file = nil
        @test_step = nil

        config.on_event :test_case_started, &method(:on_test_case_started)
        config.on_event :test_case_finished, &method(:on_test_case_finished)
        config.on_event :test_step_started, &method(:on_test_step_started)
        config.on_event :test_step_finished, &method(:on_test_step_finished)
        config.on_event :test_run_finished, &method(:on_test_run_finished)

        tc_initialize(config.to_hash, '|', @io)
        tc_before_features(nil)
      end

      def on_test_case_started(event)
        unless is_current_scenario_outline(event.test_case)
          close_scenario_outline
        end

        feature_file = event.test_case.location.file
        if @current_feature_file.nil? || feature_file != @current_feature_file
          close_rule
          close_feature
          open_feature(feature_file)
        end

        rule = get_rule(event.test_case)
        if @current_rule != rule
          close_rule
          open_rule(rule) if rule
        end

        location = event.test_case.location
        if @current_scenario_outline.nil? && location.lines.max > location.lines.min
          #It's Scenario Outline
          @current_scenario_outline = event.test_case
          scenario_outline_location = "#{location.file}:#{location.lines.min}"

          scenario_outline_node_name = scenario_node_name(@current_scenario_outline, outline_container: true)
          log_suite_started(scenario_outline_node_name, file_colon_line=scenario_outline_location)
          log_suite_started(EXAMPLES_NODE)

          scenario_outline = scenario_source(@current_scenario_outline).scenario_outline
          print_tags(scenario_outline)
        end


        file_colon_line = "#{location.file}:#{location.lines.max}"
        scenario_node_name = scenario_node_name(event.test_case)
        log_suite_started(scenario_node_name, file_colon_line=file_colon_line)

        if @current_scenario_outline.nil?
          scenario = scenario_source(event.test_case).scenario
          print_tags(scenario)
        end
      end

      def on_test_step_started(event)
        step = event.test_step
        location = step.location
        file_colon_line = "#{location.file}:#{location.lines.max}"

        log_test_opened(step_node_name(step), file_colon_line=file_colon_line)
      end

      def on_test_step_finished(event)
        @test_step = event.test_step
        step_node_name = step_node_name(@test_step)
        if event.result.kind_of?(::Cucumber::Core::Test::Result::Undefined)
          result = :undefined
          log_status_and_test_finished(result, step_node_name, 0, exception=nil)
          return
        end

        if event.result.duration.kind_of?(::Cucumber::Core::Test::Result::Duration)
          duration_ms = event.result.duration.nanoseconds / 1000
        else
          duration_ms = 0
        end

        exception = nil
        if event.result.kind_of?(::Cucumber::Core::Test::Result::Skipped)
          result = :skipped
        elsif event.result.kind_of?(::Cucumber::Core::Test::Result::Pending)
          result = :pending
        elsif event.result.kind_of?(::Cucumber::Core::Test::Result::Failed)
          result = :failed
          exception = event.result.exception
        elsif event.result.kind_of?(::Cucumber::Core::Test::Result::Passed)
          result = :passed
        else
          raise 'Unsupported step result'
        end

        log_status_and_test_finished(result, step_node_name, duration_ms, exception=exception)
      end

      def on_test_case_finished(event)
        scenario_node_name = scenario_node_name(event.test_case)
        @passed_test_cases << event.test_case if @config.wip? && event.result.passed?
        log_suite_finished(scenario_node_name)
      end

      def on_test_run_finished(event)
        close_scenario_outline
        close_rule
        close_feature
        tc_after_features(nil)
      end

      def create_snippet_text(step_name, step_exception, step_keyword, step_multiline_arg)
        step_keyword = @ast_lookup.snippet_step_keyword(@test_step)
        snippet_text(step_keyword, @test_step.text, @test_step.multiline_arg)
      end

      private
      def is_current_scenario_outline(test_case)
        if !@current_scenario_outline.nil? && @current_scenario_outline.location.file == test_case.location.file
          @current_scenario_outline.location.lines.min == test_case.location.lines.min
        end
      end

      def close_scenario_outline
        if @current_scenario_outline
          log_suite_finished(EXAMPLES_NODE)
          scenario_outline_node_name = scenario_node_name(@current_scenario_outline, outline_container: true)
          log_suite_finished(scenario_outline_node_name)
          @current_scenario_outline = nil
        end
      end

      def get_rule(test_case)
        gherkin_document = @ast_lookup.gherkin_document(test_case.location.file)
        feature = gherkin_document.feature

        feature.children.map(&:rule).reject(&:nil?).select do |rule|
          rule.location.line <= test_case.location.lines.min
        end.last
      end

      def close_rule
        if @current_rule
          log_suite_finished(RULE_NODE_PREFIX + @current_rule.name)
          @current_rule = nil
        end
      end

      def open_rule(rule)
        @current_rule = rule
        file_colon_line = @current_feature_file + ':' + rule.location.line.to_s
        log_suite_started(RULE_NODE_PREFIX + rule.name, file_colon_line=file_colon_line)
      end

      def current_feature_display_name
        feature = @ast_lookup.gherkin_document(@current_feature_file).feature
        FEATURE_NODE_PREFIX + feature.name
      end

      def get_feature(feature_file)
        @ast_lookup.gherkin_document(feature_file).feature
      end

      def close_feature
        log_suite_finished(current_feature_display_name) if @current_feature_file
      end

      def open_feature(feature_file)
        @current_feature_file = feature_file
        feature = get_feature(feature_file)
        feature_line = feature.location.line
        file_colon_line = feature_file + ":" + feature_line.to_s
        log_suite_started(current_feature_display_name, file_colon_line=file_colon_line)
        print_tags(feature)
      end

      def scenario_source(test_case)
        @ast_lookup.scenario_source(test_case)
      end

      def print_tags(test_case)
        @io.puts(test_case.tags.map { |tag| format_string(tag.name, :tag) }.join(' '))
      end

      def scenario_node_name(test_case, outline_container: false)
        source = scenario_source(test_case)
        if source.respond_to? :scenario_outline
          unless outline_container
            return "Scenario: Line: #{test_case.location.lines.max}"
          end
          prefix = source.scenario_outline.keyword
        else
          prefix = source.scenario.keyword
        end
        "#{prefix}: #{test_case.name}"
      end

      def step_node_name(test_step)
        step_name_prefix = ''
        if test_step.instance_of? ::Cucumber::Core::Test::Step
          step_name_prefix = @ast_lookup.step_source(test_step).step.keyword

        end
        "#{step_name_prefix}#{test_step.text}"
      end

      def print_summary(features)
        print_statistics(@total_duration, @config, @counts, [])
        print_passing_wip(@config, @passed_test_cases, @ast_lookup)
      end
    end
  end
end