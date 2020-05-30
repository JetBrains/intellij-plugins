# frozen_string_literal: true

require "abstract_unit"
require "active_support/core_ext/module/aliasing"

module AttributeAliasing
  class Content
    attr_accessor :title, :Data

    def initialize
      @title, @Data = nil, nil
    end

    def title?
      !title.nil?
    end

    def Data?
      !self.Data.nil?
    end
  end

  class Email < Content
    alias_attribute :subject, :title
    alias_attribute :body, :Data
  end
end

class AttributeAliasingTest < ActiveSupport::TestCase
  def test_attribute_alias
    e = AttributeAliasing::Email.new

    assert_not_predicate e, :subject?

    e.title = "Upgrade computer"
    assert_equal "Upgrade computer", e.subject
    assert_predicate e, :subject?

    e.subject = "We got a long way to go"
    assert_equal "We got a long way to go", e.title
    assert_predicate e, :title?
  end

  def test_aliasing_to_uppercase_attributes
    # Although it's very un-Ruby, some people's AR-mapped tables have
    # upper-case attributes, and when people want to alias those names
    # to more sensible ones, everything goes *foof*.
    e = AttributeAliasing::Email.new

    assert_not_predicate e, :body?
    assert_not_predicate e, :Data?

    e.body = "No, really, this is not a joke."
    assert_equal "No, really, this is not a joke.", e.Data
    assert_predicate e, :Data?

    e.Data = "Uppercased methods are the suck"
    assert_equal "Uppercased methods are the suck", e.body
    assert_predicate e, :body?
  end
end
