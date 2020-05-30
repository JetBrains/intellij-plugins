# frozen_string_literal: true

require "abstract_unit"
require "active_support/deprecation"

class MethodWrappersTest < ActiveSupport::TestCase
  def setup
    @klass = Class.new do
      def new_method; "abc" end
      alias_method :old_method, :new_method

      protected

        def new_protected_method; "abc" end
        alias_method :old_protected_method, :new_protected_method

      private

        def new_private_method; "abc" end
        alias_method :old_private_method, :new_private_method
    end
  end

  def test_deprecate_methods_without_alternate_method
    warning = /old_method is deprecated and will be removed from Rails \d.\d./
    ActiveSupport::Deprecation.deprecate_methods(@klass, :old_method)

    assert_deprecated(warning) { assert_equal "abc", @klass.new.old_method }
  end

  def test_deprecate_methods_warning_default
    warning = /old_method is deprecated and will be removed from Rails \d.\d \(use new_method instead\)/
    ActiveSupport::Deprecation.deprecate_methods(@klass, old_method: :new_method)

    assert_deprecated(warning) { assert_equal "abc", @klass.new.old_method }
  end

  def test_deprecate_methods_warning_with_optional_deprecator
    warning = /old_method is deprecated and will be removed from MyGem next-release \(use new_method instead\)/
    deprecator = ActiveSupport::Deprecation.new("next-release", "MyGem")
    ActiveSupport::Deprecation.deprecate_methods(@klass, old_method: :new_method, deprecator: deprecator)

    assert_deprecated(warning, deprecator) { assert_equal "abc", @klass.new.old_method }
  end

  def test_deprecate_methods_warning_when_deprecated_with_custom_deprecator
    warning = /old_method is deprecated and will be removed from MyGem next-release \(use new_method instead\)/
    deprecator = ActiveSupport::Deprecation.new("next-release", "MyGem")
    deprecator.deprecate_methods(@klass, old_method: :new_method)

    assert_deprecated(warning, deprecator) { assert_equal "abc", @klass.new.old_method }
  end

  def test_deprecate_methods_protected_method
    ActiveSupport::Deprecation.deprecate_methods(@klass, old_protected_method: :new_protected_method)

    assert(@klass.protected_method_defined?(:old_protected_method))
  end

  def test_deprecate_methods_private_method
    ActiveSupport::Deprecation.deprecate_methods(@klass, old_private_method: :new_private_method)

    assert(@klass.private_method_defined?(:old_private_method))
  end

  def test_deprecate_class_method
    mod = Module.new do
      extend self

      def old_method
        "abc"
      end
    end
    ActiveSupport::Deprecation.deprecate_methods(mod, old_method: :new_method)

    warning = /old_method is deprecated and will be removed from Rails \d.\d \(use new_method instead\)/
    assert_deprecated(warning) { assert_equal "abc", mod.old_method }
  end

  def test_deprecate_method_when_class_extends_module
    mod = Module.new do
      def old_method
        "abc"
      end
    end
    @klass.extend mod
    ActiveSupport::Deprecation.deprecate_methods(mod, old_method: :new_method)

    warning = /old_method is deprecated and will be removed from Rails \d.\d \(use new_method instead\)/
    assert_deprecated(warning) { assert_equal "abc", @klass.old_method }
  end

  def test_method_with_without_deprecation_is_exposed
    ActiveSupport::Deprecation.deprecate_methods(@klass, old_method: :new_method)

    warning = /old_method is deprecated and will be removed from Rails \d.\d \(use new_method instead\)/
    assert_deprecated(warning) { assert_equal "abc", @klass.new.old_method_with_deprecation }
    assert_equal "abc", @klass.new.old_method_without_deprecation
  end
end
