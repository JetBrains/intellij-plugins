public class If extends AbstractConditional
{
  /**
   * If true, then the body of the If component is rendered. If false, the body is omitted.
   */
  @Parameter(required = true)
  private boolean test;

  /**
   * Optional parameter to invert the test. If true, then the body is rendered when the test parameter is false (not
   * true).
   *
   * @see Unless
   * @deprecated Since 5.3 as property expressions support the '!' invert operator
   */
  @Parameter
  private boolean negate;

  /**
   * @return test parameter (if negate is false), or test parameter inverted (if negate is true)
   */
  protected boolean test()
  {
    return test != negate;
  }

  @Parameter(name = "else", defaultPrefix = BindingConstants.LITERAL)
  private Block elseBlock;
}