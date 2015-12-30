component{

  public struct function getWebpage()
  {

    var httpresult = "";

    cfhttp(url = 'http://www.somewebpagedomain.com', method = "GET", resolveurl = "true", result = "httpresult"){
    cfhttpparam(type = "header", name = "Accept-Language", value = "en_US");
    cfhttpparam(type = "formField", name = "accountid", value = 100);
  }

    return httpresult;

  }

  public void function sendSomeEmail()
  {

    var body = "this is<br />an email body";

    cfmail(to = "someone@someone.com", from = "another@someone.com", subject = "this is the subject", type = "html"){
    WriteOutput(body);
  }
  }
}