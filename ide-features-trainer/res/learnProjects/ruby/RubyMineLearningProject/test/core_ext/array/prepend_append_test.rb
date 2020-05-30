# frozen_string_literal: true

require "abstract_unit"

class PrependAppendTest < ActiveSupport::TestCase
  def test_requiring_prepend_and_append_is_deprecated
    assert_deprecated do
      require "active_support/core_ext/array/prepend_and_append"
    end
  end
end
