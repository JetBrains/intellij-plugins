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

require 'cucumber/formatter/console'
require 'fileutils'

require 'teamcity/runner_common'
require 'teamcity/utils/service_message_factory'
require 'teamcity/utils/runner_utils'
require 'teamcity/utils/url_formatter'

module Teamcity
  module Cucumber
    module FormatterCommons
      include FileUtils
      include ::Cucumber::Formatter::Console
      include ::Rake::TeamCity::RunnerCommon
      include ::Rake::TeamCity::RunnerUtils
      include ::Rake::TeamCity::Utils::UrlFormatter

      attr_writer :indent
      attr_reader :step_mother
      alias :runtime :step_mother

      def tc_initialize (options, delim, path_or_io)
        redirect_output_via_drb = !path_or_io.nil? && (defined? DRb::DRbObject) && path_or_io.kind_of?(DRb::DRbObject)
        if redirect_output_via_drb
          # redirect cmds and output to DRB stream
          @@original_stdout = path_or_io
          @io = path_or_io
        else
          # ignore given file path or io object and output to $stdout
          @io = $stdout
        end

        # Setups Test runner's MessageFactory
        set_message_factory(Rake::TeamCity::MessageFactory)
        log_test_reporter_attached()

        @options = options

        # true when we are in context of Background block
        @in_background = nil

        # true when we are in context of scenario_outline Examples
        @in_outline_examples = nil

        # scenarios are provided as feature_elements. This helps to
        # understand either current feature outline example ot not
        # TODO: remove it. Extend @current_example_or_feature_elems_stack
        # to save this info instead of just name
        @in_outline_scenario_stack = []

        # cucumber reports only names for fake row-based scenarios
        # this is used for closing previous suite when new fake scenario has
        # started or whole example has finished
        @previous_examples_scenario_outline = nil

        # tags for current holder : feature, scenario or step
        @tags_holder_stack = []

        # stack of active (unfinished) feature_elements: scenario, scenario_outline, example
        @current_example_or_feature_elems_stack = []

        # current feature name - is used for closing feature block
        @current_feature_name = nil

        # is used for counting execution duration of steps
        @current_step_start_time = nil

        # for console runner methods
        @delim = delim
        @indent = 0
        @prefixes = options[:prefixes] || {}
      end


##################################################
# For tags gathering
      def tc_tag_name (tag_name)
        tag = format_string("@#{tag_name}", :tag)
        if @tags_holder_stack.empty?
          print_tags_collection([tag])
        else
          @tags_holder_stack.last.add_tag(tag)
        end
      end

#### Features
# @Processes: features
# Will be invoked before all features.
# we will use this method for runner initialization and
# test count reporting

      def tc_before_features(features)
        log_in_idea(@message_factory.create_custom_progress_tests_category("Scenarios"))

        # TODO - step count, but it seems it is unreal using current cucumber API.
      end

      def tc_after_features(features)
        log_in_idea(@message_factory.create_custom_progress_tests_category(nil))
        print_summary(features)
      end

      # @Processes: tags, feature name, background and all feature elements
      # we use to close feature in the end (after all elements will be processed)
      def tc_before_feature (feature)
        # this stack allows us to show tags under corresponding
        # feature node. This is necessary due to cucumber's
        # AST and visitor API specific
        register_tags_holder

        if feature.method(:file_colon_line).arity == 1
          # each feature file contains only one feature
          # so let's use 1 line of feature file for navigation
          @current_feature_file_colon_line = feature.file_colon_line(1)
        else
          @current_feature_file_colon_line = feature.file_colon_line
        end
      end

      def tc_after_feature (feature)
        # Cucumber doesn't provide before/end event thus
        # we should close here last started issue
        unregister_tags_holder
        log_suite_finished(@current_feature_name) unless @current_feature_name.nil?
        @current_feature_name = nil
      end

      # @Processes: feature name
      # We use for logging feature started event
      def tc_feature_name (new_feature_name)
        @current_feature_name = new_feature_name.split("\n").first || @current_feature_file_colon_line || ""
        log_suite_started(@current_feature_name, @current_feature_file_colon_line)
        # log tags because the are reported before feature name
        print_current_tags()
      end

####  Examples:
# Processes "Examples" section
# Cucumber doesn't provide start .. end events
# here we process examples ended event manually
#noinspection RubyUnusedLocalVariable

      def tc_before_examples(examples)
        @in_outline_examples = true
        @previous_examples_scenario_outline = nil
        register_tags_holder
      end

      def tc_after_examples(examples)
        unregister_tags_holder
        @in_outline_examples = nil

        # close outline of last scenario_outline in this example
        # (if it exist)
        unless @previous_examples_scenario_outline.nil?
          log_suite_finished(@current_example_or_feature_elems_stack.pop) unless @current_example_or_feature_elems_stack.empty?
          @previous_examples_scenario_outline = nil
        end

        # close example suite block
        log_suite_finished(@current_example_or_feature_elems_stack.pop) unless @current_example_or_feature_elems_stack.empty?
      end

      # Processes name of "Examples" section
      # Cucumber doesn't provide start .. end events
      # here we process examples started event manually
      def tc_examples_name(keyword, name)
        new_examples_name = "#{ensure_trailing_column(keyword)} #{name}"

        # report example suite started
        @current_example_or_feature_elems_stack.push(new_examples_name)
        log_suite_started(new_examples_name)
        # log tags because the are reported before example name
        print_current_tags
      end

#### Scenarios, Scenario Outlines and Backgrounds:
# @Processes: Background
# Actually all background steps will be merged with other steps of
# 'scenarios'/'outline examples' so we shouldn't show Background as separate test suite
#noinspection RubyUnusedLocalVariable
      def tc_before_background(background)
        @in_background = true
        register_tags_holder
      end

      def tc_after_background(background)
        unregister_tags_holder
        @in_background = nil
      end

#### Scenarios, Scenario Outlines(real and fake from Example's table rows):
# @Processes: Scenario(real without examples' fake scenarios), ScenarioOutline
# Here we can do cleanup for scenarios in current feature: Scenario, ScenarioOutline
      def scenario_outline?(feature_element)
        if ::Teamcity::Cucumber::CUCUMBER_VERSION_2
          # API changed in Cucumber 2.0
          feature_element.class == ::Cucumber::Core::Ast::ScenarioOutline
        else
          feature_element.class == ::Cucumber::Ast::ScenarioOutline
        end
      end

      def tc_before_feature_element (feature_element)
        @in_outline_scenario_stack.push(scenario_outline?(feature_element) ? true : nil)
        register_tags_holder
      end

      def tc_after_feature_element(feature_element)
        unregister_tags_holder
        # close scenario/scenario_outline suite
        log_suite_finished(@current_example_or_feature_elems_stack.pop) unless @current_example_or_feature_elems_stack.empty?
        @in_outline_scenario_stack.pop
      end

      # @Processes: Scenario (fake and real), ScenarioOutline names
      # scenario/scenario_outline contains of name and steps
      def tc_scenario_name(file_colon_line, keyword, name, source_indent)
        if row_based_scenario_outline?
          # for fake row-related scenarios it's reasonable to show row content
          # or at least line number instead of duplicating scenario_outline name
          colon_line = extract_cucumber_location(file_colon_line)[1]
          name = "Line: #{colon_line.nil? ? "<unknown>" : colon_line}"
        end

        process_new_feature_element_name(keyword, name, file_colon_line, source_indent)

        unless scenario_outline_suites_set?
          # don't trigger on "Scenario Outline" suite of substituted scenarios
          log_in_idea(@message_factory.create_custom_progress_test_status(:started))
        end
      end

#### Steps and Cells:
# Processes: StepInvocation
# We will remember before step invocation and then
# compare with time at "visit_step_result" moment
#noinspection RubyUnusedLocalVariable
      def tc_before_step(step)
        register_tags_holder
        @handled_exception = nil
        @current_step_start_time = get_current_time_in_ms
      end

      def tc_after_step(step)
        unregister_tags_holder
      end

      def fetch_file_colon_from_current_step(current_step_match, steps)
        steps.each do |step|
          step_match = step.instance_variable_get(:@step_match)
          if current_step_match == step_match
            # fetch file_colon_line from step
            return step.file_colon_line
          end
        end
        nil
      end

      # @Nullable
      def fetch_file_colon_line(current_step_name, current_step_match)
        # HACK - this method uses ugly hack!
        #
        # fix it when API will be available :
        #   Cucumber - #501 (https://rspec.lighthouseapp.com/projects/16211-cucumber/tickets/501) [status:hold]
        #   Cucumber github: https://github.com/cucumber/cucumber/issues/179
        # if step definitions wasn't matched - return nil
        if ::Cucumber::NoStepMatch === current_step_match
          # in case of unmatched step - "file_colon_line" will return step location
          return current_step_match.file_colon_line
        else
          step_definition = current_step_match.step_definition
          unless step_definition.nil?
            # fetch using HACK
            rb_language = step_definition.instance_variable_get(:@rb_language)
            unless rb_language.nil?
              # get all step and find current one
              mother_step = rb_language.instance_variable_get(:@step_mother)
              if mother_step.nil?
                return nil
              end
              if mother_step.respond_to?(:steps)
                # Cucumber < 0.9.x
                steps = mother_step.steps
                return fetch_file_colon_from_current_step(current_step_match, steps)
              else
                # cucumber 0.9.x (0.9.3 and higher)
                user_interface = step_mother.instance_variable_get(:@user_interface)
                unless user_interface.nil?
                  current_scenario = user_interface.instance_variable_get(:@current_scenario)
                  unless current_scenario.nil?
                    return current_scenario.file_colon_line
                  end
                else
                  # cucumber 0.10.2 and higher (most likely 0.10.0 also)
                  current_scenario = step_mother.instance_variable_get(:@current_scenario)
                  unless current_scenario.nil?
                    steps = current_scenario.instance_variable_get(:@steps)
                    unless steps.nil?
                      return fetch_file_colon_from_current_step(current_step_match, steps)
                    else
                      # cucumber 1.0 and smth around it
                      step_invocations = current_scenario.instance_variable_get(:@step_invocations)
                      return fetch_file_colon_from_current_step(current_step_match, step_invocations)
                    end
                  end
                end
              end
            end
            # Do not resolve, if HACK isn't available
            # for consistent behaviour no sense to resolve in
            # step definitions on old gems and resolve to
            # feature file on other cases
            #return step_match.file_colon_line
            return nil
          end
        end
      end

      # result of step invocation - calls v_exception, v_step_name
      #noinspection RubyUnusedLocalVariable
      def tc_before_step_result(exception, keyword, multiline_arg, source_indent, status, step_match, background, file_colon_line)
        finished_at_ms = get_current_time_in_ms
        duration_ms = finished_at_ms - @current_step_start_time
        @handled_exception = exception

        # Actually cucumber standard formatters doesn't count BG steps in
        # context of Background element. Instead it count such steps in each
        # affected scenario
        if @in_background
          return
        end

        # Also cucumber standard formatter doesn't count ScenarioOutline steps descriptions
        # e.g.
        # ------
        #   Given there are <start> cucumbers   # features/step_definitions/common_step.rb:13
        # ------
        # in this case let do not report always ignored step, but just output step definition in console

        is_fake_outline_step = !@in_outline_scenario_stack.empty? && # if there is no active outline - all is ok
            @in_outline_scenario_stack.last && # if not in scenario_outline - steps will be ok
            !@in_outline_examples # examples contains correct steps
        if is_fake_outline_step
          puts format_step(keyword, step_match, status, @options[:source] ? source_indent : nil)
          return
        end

        name = step_match.format_args(lambda { |a| a })
        # New cucumber API reports file colon line, in old version we had to
        # fetch it using reflection, see https://github.com/cucumber/cucumber/issues/179
        file_colon_line = file_colon_line || fetch_file_colon_line(name, step_match)

        # Now we can report step as test
        step_line = "#{keyword} #{name}"
        log_test_opened(step_line, file_colon_line)

        # log tags because the are reported before step name
        print_current_tags

        diagnostic_info = "cucumber  f/s=(#{finished_at_ms}, #{@current_step_start_time}), duration=#{duration_ms}, time.now=#{Time.now.to_s}"
        log_status_and_test_finished(status, step_line, duration_ms, exception, multiline_arg, keyword, diagnostic_info)
      end

      # handler for when exceptions occur - exceptions in steps are already handled but
      # others are not (i.e. Before / After hooks) so we need to reflect this
      def tc_exception(exception, status)
        msg = "#{exception.class.name}: #{exception.message}"
        backtrace = ::Rake::TeamCity::RunnerCommon.format_backtrace(exception.backtrace)

        log(@message_factory.create_msg_error(msg, backtrace)) unless @handled_exception 
      end

#######################################################################
#######################################################################
########### PRIVATE METHODS ###########################################
#######################################################################
#######################################################################
      private

      def row_based_scenario_outline?
        @in_outline_scenario_stack.last && @in_outline_examples
      end

      def scenario_outline_suites_set?
        @in_outline_scenario_stack.last && !@in_outline_examples
      end

      def log(msg)
        send_msg(msg)

        # returns:
        msg
      end

      def log_in_idea(msg)
        return unless ::Rake::TeamCity.is_in_idea_mode
        send_msg(msg)
        # returns:
        msg
      end

      def log_suite_started(suite_name, file_colon_line = nil)
        log(@message_factory.create_suite_started(suite_name,
                                                  location_from_link(*extract_cucumber_location(file_colon_line))))
      end

      def log_suite_finished(suite_name)
        log(@message_factory.create_suite_finished(suite_name))
      end

      # reports test open for step or example table fake step_invocation
      def log_test_opened(step_line, file_colon_line = nil)
        log(@message_factory.create_test_started(step_line,
                                                 location_from_link(*extract_cucumber_location(file_colon_line))))
      end

      # reports status and name for step or example table fake step_invocation
      # status: :passed, :pending, :undefined, :failed + :skipped, :skipped_param
      def log_status_and_test_finished(status, name, duration_ms,
          exception = nil, multiline_arg = nil, keyword = nil, diagnostic_info=nil)
        msg = exception.nil? ? "" : "#{exception.class.name}: #{exception.message}"
        backtrace = exception.nil? ? "" : ::Rake::TeamCity::RunnerCommon.format_backtrace(exception.backtrace)

        case status
          when :pending
            # remove redundant information
            if msg.index("Cucumber::Pending: ") != nil
              msg.gsub!("Cucumber::Pending: ", "")
              msg = msg + " (Cucumber::Pending exception)"
            end
            log(@message_factory.create_test_ignored(name, msg, backtrace))
          when :skipped, :skipped_param
            log(@message_factory.create_test_ignored(name, "Skipped step"))
          when :undefined
            print_undef_step_snippet(name, exception, multiline_arg, keyword)
            # remove redundant information
            if msg.index("Cucumber::Undefined: ") != nil
              msg.gsub!("Cucumber::Undefined: ", "")
              msg = msg + " (Cucumber::Undefined exception)"
            end

            if ::Rake::TeamCity.is_in_buildserver_mode
              # teamcity
              # TeamCity doesn't distinguish "error" and "failure".
              # according to cucumber console runner :undefined step is closer to :pending one
              log(@message_factory.create_test_ignored(name, msg, backtrace))
            else
              # idea
              log(@message_factory.create_test_error(name, msg, backtrace))
            end
          when :failed
            if !exception.nil? && exception.class.to_s == "Cucumber::Ast::Table::Different"
              print_diff_table(exception)
            end
            log(@message_factory.create_test_failed(name, msg, backtrace))
            log_in_idea(@message_factory.create_custom_progress_test_status(:failed))
        end
        log(@message_factory.create_test_finished(name, duration_ms.nil? ? 0 : duration_ms, ::Rake::TeamCity.is_in_buildserver_mode ? nil : diagnostic_info))
      end

      def process_new_feature_element_name(keyword, name, file_colon_line, source_indent)
        # TODO rename to new_feature_element_name

        # keyword may have trailing ':' (<0.7.0) or not (>= 0.7.0)
        new_bg_or_feature_element_name = "#{ensure_trailing_column(keyword)} #{name}"

        if @in_outline_examples && !@previous_examples_scenario_outline.nil?
          # Examples doesn't allow to identify scenario finished event
          # so we will close prev scenario
          log_suite_finished(@current_example_or_feature_elems_stack.pop)
        end
        # report suite started
        # TODO - to think, seems it isn't correct only scenario outline name
        # should be assigned to prev scenario outline
        @previous_examples_scenario_outline = new_bg_or_feature_element_name

        # report suite started
        @current_example_or_feature_elems_stack.push(new_bg_or_feature_element_name)
        log_suite_started(new_bg_or_feature_element_name, file_colon_line)
        # log tags because the are reported before Background or Feature
        print_current_tags
      end

      def extract_cucumber_location (file_colon_line)
        if file_colon_line =~ /(.+):(-?\d+)/
          return get_pair_by($1, $2)
        end
        return nil, nil
      end

      def print_my_stats(features)
        @io.puts scenario_summary(step_mother) { |status_count, status| format_string(status_count, status) }
        @io.puts step_summary(step_mother) { |status_count, status| format_string(status_count, status) }
        @io.puts(format_duration(features.duration)) if features && features.duration
        @io.flush
      end

      # Summary
      def print_summary(features)
        # If defined both old and new >= 0.3.8 api we should use
        # new one, because old api is backward compatibility
        if !(defined? print_stats) && (defined? print_counts)
          # cucumber < 0.3.8
          print_counts
        else
          # cucumber >= 0.3.8
          print_st_arity = method(:print_stats).arity
          case print_st_arity
            when 1, -2
              # cucumber < 1.0.2
              # print_stats(features)
              # print_stats(features, profiles = [])
              print_stats(features)
            when 2
              # cucumber >= 1.0.2
              print_my_stats(features)
            else
              @io.puts("Unsupported cucumber API detected! Wrong number of arguments (#{print_st_arity}) (ArgumentError)")
              @io.flush
          end

          print_passing_wip(@options)

          # Was removed in cucumber  v=0.8.5.
          #Cucumber >= 0.8.5 will throw exception in this case
          if self.respond_to? :print_tag_limit_warnings
            print_tag_limit_warnings(@options)
          end
        end
      end

      # Tags
      def print_tags_collection(tags)
        return if tags.empty?
        print "Tag#{tags.length > 1 ? "s" : ""}: "
        tags.each do |tag|
          print "#{tag}#{tag != tags[-1] ? ', ' : ""}"
        end
        puts
      end

      # Register holder in stack which allows us to show tags
      # under corresponding feature/scenarios/sc_outline/step node. This
      # is necessary due to cucumber's AST and visitor API specific
      def register_tags_holder
        @tags_holder_stack.push(TagsHolder.new)
      end

      def unregister_tags_holder
        # on exit we should remove visitor and dump tags(if they weren't dumped)
        print_current_tags
        @tags_holder_stack.pop
      end

      def print_current_tags()
        holder = @tags_holder_stack.last
        # holder cant be null here!
        print_tags_collection(holder.tags)
        holder.clear_tags
      end

      # prints snippets for undefined steps
      def print_undef_step_snippet(step_name, step_exception, step_multiline_arg, step_keyword)
        step_def_name = if step_exception.nil?
                          step_name.index(step_keyword) == 0 ? (step_name[step_keyword.length..-1]).lstrip : step_name
                        else
                          ::Cucumber::Undefined === step_exception ? step_exception.step_name : step_name
                        end

        snippet = create_snippet_text(step_def_name, step_keyword, step_multiline_arg)

        text = " \nYou can implement step definitions for undefined steps with these snippets:\n\n#{snippet}"
        puts text
      end

      def create_snippet_text(step_def_name, step_keyword, step_multiline_arg)
        if ::Teamcity::Cucumber::CUCUMBER_VERSION_2
          @step_mother.snippet_text(step_keyword || '', step_def_name, step_multiline_arg)
        else
          step_multiline_class = step_multiline_arg ? step_multiline_arg.class : nil
          @step_mother.snippet_text(step_keyword || '', step_def_name, step_multiline_class)
        end
      end

      def ensure_trailing_column(keyword)
        keyword + (keyword[-1, 1] == ':' ? '' : ':')
      end

      def cell_prefix(status)
        @prefixes[status]
      end

      def print_diff_table(exception)
        table = exception.table
        if table
          # iterate rows
          table.each_cells_row do |rows|
            @io.print '  |'.indent(1)
            count = rows.count
            count.times do |i|
              cell = rows[i]
              value = cell.value

              status = cell.status || :passed
              width = table.col_width(i)
              cell_text = value.to_s || ''
              # cucumber 0.7.3 no longer extends String with jcode (http://tinyurl.com/2v4xu3w)
              length = cell_text.respond_to?(:jlength) ? cell_text.jlength : cell_text.unpack('U*').length
              padded = cell_text + (' ' * (width - length))
              prefix = cell_prefix(status)
              message = ' ' + format_string("#{prefix}#{padded}", status) +
                  (::Cucumber::Formatter::ANSIColor.respond_to?(:reset) ? ::Cucumber::Formatter::ANSIColor.reset(" |") : " |")
              @io.print(message)
              @io.flush
            end
            @io.puts
            @io.flush
          end
          @io.puts
          @io.flush
        end
      end

      # Instance of this class allow to store tags for
      # current holder: feature, example, features_element, etc
      class TagsHolder
        attr_reader :tags

        def initialize
          @tags = []
        end

        def add_tag(tag)
          @tags << tag
        end

        def clear_tags
          @tags.clear
        end
      end
    end
  end
end
