package training.statistic;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;

import java.io.*;

/**
 * Created by jetbrains on 01/02/16.
 */
public class TestAnalytics {


//    private static final String DOMAIN = "google-analytics";
//    private static final String SITE_ROOT = "http://" + DOMAIN + "/";

//    private static final String POST_URL = "https://www.google-analytics.com/debug/collect";
    private static final String POST_URL = "http://www.rbc.ru/";
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final String SITE_CHARSET = "CP1251";

    public static void main(String[] args) throws UnsupportedEncodingException {

        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setContentCharset(DEFAULT_CHARSET);
//        httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

        //ADD COOKIES IF NEEDED
//        HttpState state = new HttpState();
//        state.addCookie(new Cookie(DOMAIN, "name", "Platon", "/forum/", new Date(System.currentTimeMillis() + 3600*1000*24*365L), false));
//        state.addCookie(new Cookie(DOMAIN, "password", "******", "/forum/", new Date(System.currentTimeMillis() + 3600*1000*24*365L), false));
//        httpClient.setState(state);

        // Загрузка страницы с запросом методом GET
//        GetMethod getMethod = new GetMethod(SITE_ROOT + "index.html?cat=1&amp;search=" + URLEncoder.encode("Доктор Хаус", SITE_CHARSET));
//        getMethod.getParams().setContentCharset(SITE_CHARSET);
//        try {
//            int result = httpClient.executeMethod(getMethod);
//            if (result == HttpStatus.SC_OK) {
//                // Выводим страницу на экран
//                System.out.println(getMethod.getResponseBodyAsString());
//            } else {
//                System.out.println("А страничка-то и не загрузилась!!!");
//                return;
//            }
//        } catch (IOException e) {
//            System.out.println("Проблемы со связью");
//            return;
//        } finally {
//            getMethod.releaseConnection();
//        }
        // Загрузка страницы с запросом методом POST
        PostMethod postMethod = new PostMethod(POST_URL);
//        postMethod.addParameter("v", "1");
//        postMethod.addParameter("&tid", "UA-73167019-1");
//        postMethod.addParameter("&cid", "558");
//        postMethod.addParameter("&geoid", "DE");
//
//
//        postMethod.addParameter("&t", "pageview");       //hit type (pageview, event)
//        postMethod.addParameter("&dh", "plugin.default");
//        postMethod.addParameter("&dp", "home");
//        postMethod.addParameter("&dt", "homepage");

        postMethod.getParams().setContentCharset(DEFAULT_CHARSET);
        try {
            int result = httpClient.executeMethod(postMethod);
            if (result == HttpStatus.SC_OK) {
                // Выводим страницу на экран
                printStream(postMethod.getResponseBodyAsStream());
            } else {
                System.out.println("Page hasn't been loaded");
                return;
            }
        } catch (IOException e) {
            System.out.println("Error connection");
            return;
        } finally {
            postMethod.releaseConnection();
        }
    }
    private static void printStream(InputStream res) throws IOException {
        InputStreamReader reader = new InputStreamReader(res, DEFAULT_CHARSET);
        OutputStreamWriter writer = new OutputStreamWriter(System.out);
        int am;
        char[] buffer = new char[4096];
        while ((am = reader.read(buffer)) != -1)
            writer.write(buffer, 0, am);
    }

}
