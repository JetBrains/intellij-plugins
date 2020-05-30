# frozen_string_literal: true

require "abstract_unit"
require "active_support/xml_mini"
require "active_support/core_ext/hash/conversions"

class XMLMiniEngineTest < ActiveSupport::TestCase
  def self.run_with_gem(gem_name)
    require gem_name
    yield
  rescue LoadError
    # Skip tests unless gem is available
  end

  def self.run_with_platform(platform_name)
    yield if RUBY_PLATFORM.include?(platform_name)
  end

  def self.inherited(base)
    base.include EngineTests
    super
  end

  def setup
    @default_backend = ActiveSupport::XmlMini.backend
    ActiveSupport::XmlMini.backend = engine
    super
  end

  def teardown
    ActiveSupport::XmlMini.backend = @default_backend
    super
  end

  module EngineTests
    def test_file_from_xml
      hash = Hash.from_xml(<<-eoxml)
        <blog>
          <logo type="file" name="logo.png" content_type="image/png">
          </logo>
        </blog>
      eoxml
      assert hash.key?("blog")
      assert hash["blog"].key?("logo")

      file = hash["blog"]["logo"]
      assert_equal "logo.png", file.original_filename
      assert_equal "image/png", file.content_type
    end

    def test_exception_thrown_on_expansion_attack
      assert_raise expansion_attack_error do
        Hash.from_xml(<<-eoxml)
          <?xml version="1.0" encoding="UTF-8"?>
          <!DOCTYPE member [
            <!ENTITY a "&b;&b;&b;&b;&b;&b;&b;&b;&b;&b;">
            <!ENTITY b "&c;&c;&c;&c;&c;&c;&c;&c;&c;&c;">
            <!ENTITY c "&d;&d;&d;&d;&d;&d;&d;&d;&d;&d;">
            <!ENTITY d "&e;&e;&e;&e;&e;&e;&e;&e;&e;&e;">
            <!ENTITY e "&f;&f;&f;&f;&f;&f;&f;&f;&f;&f;">
            <!ENTITY f "&g;&g;&g;&g;&g;&g;&g;&g;&g;&g;">
            <!ENTITY g "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx">
          ]>
          <member>
            &a;
          </member>
      eoxml
      end
    end

    def test_setting_backend
      assert_engine_class ActiveSupport::XmlMini.backend
    end

    def test_blank_returns_empty_hash
      assert_equal({}, ActiveSupport::XmlMini.parse(nil))
      assert_equal({}, ActiveSupport::XmlMini.parse(""))
    end

    def test_parse_from_frozen_string
      xml_string = "<root/>"
      assert_equal({ "root" => {} }, ActiveSupport::XmlMini.parse(xml_string))
    end

    def test_array_type_makes_an_array
      assert_equal_rexml(<<-eoxml)
        <blog>
          <posts type="array">
            <post>a post</post>
            <post>another post</post>
          </posts>
        </blog>
      eoxml
    end

    def test_one_node_document_as_hash
      assert_equal_rexml(<<-eoxml)
        <products/>
      eoxml
    end

    def test_one_node_with_attributes_document_as_hash
      assert_equal_rexml(<<-eoxml)
        <products foo="bar"/>
      eoxml
    end

    def test_products_node_with_book_node_as_hash
      assert_equal_rexml(<<-eoxml)
        <products>
          <book name="awesome" id="12345" />
        </products>
      eoxml
    end

    def test_products_node_with_two_book_nodes_as_hash
      assert_equal_rexml(<<-eoxml)
        <products>
          <book name="awesome" id="12345" />
          <book name="america" id="67890" />
        </products>
      eoxml
    end

    def test_single_node_with_content_as_hash
      assert_equal_rexml(<<-eoxml)
        <products>
          hello world
        </products>
      eoxml
    end

    def test_children_with_children
      assert_equal_rexml(<<-eoxml)
        <root>
          <products>
            <book name="america" id="67890" />
          </products>
        </root>
      eoxml
    end

    def test_children_with_text
      assert_equal_rexml(<<-eoxml)
        <root>
          <products>
            hello everyone
          </products>
        </root>
      eoxml
    end

    def test_children_with_non_adjacent_text
      assert_equal_rexml(<<-eoxml)
        <root>
          good
          <products>
            hello everyone
          </products>
          morning
        </root>
      eoxml
    end

    def test_parse_from_io
      skip_unless_extended_engine

      assert_equal_rexml(StringIO.new(<<-eoxml))
        <root>
          good
          <products>
            hello everyone
          </products>
          morning
        </root>
      eoxml
    end

    def test_children_with_simple_cdata
      skip_unless_extended_engine

      assert_equal_rexml(<<-eoxml)
        <root>
          <products>
             <![CDATA[cdatablock]]>
          </products>
        </root>
      eoxml
    end

    def test_children_with_multiple_cdata
      skip_unless_extended_engine

      assert_equal_rexml(<<-eoxml)
        <root>
          <products>
             <![CDATA[cdatablock1]]><![CDATA[cdatablock2]]>
          </products>
        </root>
      eoxml
    end

    def test_children_with_text_and_cdata
      skip_unless_extended_engine

      assert_equal_rexml(<<-eoxml)
        <root>
          <products>
            hello <![CDATA[cdatablock]]>
            morning
          </products>
        </root>
      eoxml
    end

    def test_children_with_blank_text
      skip_unless_extended_engine

      assert_equal_rexml(<<-eoxml)
        <root>
          <products>   </products>
        </root>
      eoxml
    end

    def test_children_with_blank_text_and_attribute
      skip_unless_extended_engine

      assert_equal_rexml(<<-eoxml)
        <root>
          <products type="file">   </products>
        </root>
      eoxml
    end

    private
      def engine
        raise NotImplementedError
      end

      def assert_engine_class(actual)
        assert_equal ActiveSupport.const_get("XmlMini_#{engine}"), actual
      end

      def assert_equal_rexml(xml)
        parsed_xml = ActiveSupport::XmlMini.parse(xml)
        xml.rewind if xml.respond_to?(:rewind)
        hash = ActiveSupport::XmlMini.with_backend("REXML") { ActiveSupport::XmlMini.parse(xml) }
        assert_equal(hash, parsed_xml)
      end

      def expansion_attack_error
        raise NotImplementedError
      end

      def extended_engine?
        true
      end

      def skip_unless_extended_engine
        skip "#{engine} is not an extended engine" unless extended_engine?
      end
  end
end
