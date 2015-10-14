////////////////////////////////////////////////////////////////////////////////
//
//  Copyright (C) 3000 by me.
//
////////////////////////////////////////////////////////////////////////////////

package controls
{

// !!!!
[Exclude(name="maxHorizontalScrollPosition", kind="property")]

/**
 *  The CCC control is super special control.
 */
public class CCC {
  /**
   * Constructor
   */
    function CCC() {}

  /**
   * my super
   * func
   * <table>
   *   <tr>
   *       <th>Property</th>
   *       <th>Value</th>
   *   </tr>
   *   <tr>
   *       <td><code>bubbles</code></td>
   *       <td>false</td>
   *   </tr>
   * </table>
   *
   * doc1
   * doc2
   *
   * <TABLE width="100%"></TABLE>
   * doc3
   * doc4
   */
    function aaa(a:int = 1) {
      a<caret>aa()
    }
  }
}
