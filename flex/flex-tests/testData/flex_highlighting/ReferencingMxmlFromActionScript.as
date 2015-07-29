package <error>com</error> {
  public class ReferencingMxmlFromActionScript {
    function xxx(p):* {
      var a:MxmlInTopLevelPackage = new MxmlInTopLevelPackage();
      xxx(a.width) * xxx(a.yyy());
    }
  }
}