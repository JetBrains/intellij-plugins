package training.learn;


import com.intellij.util.xmlb.annotations.Transient;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.Nullable;
import training.util.MyClassLoader;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * Created by karashevich on 30/12/14.
 */
public class Scenario {

    private String path;
    private Element root;
    private Document doc;

    public Scenario(){
    }

    public Scenario(String path) throws JDOMException, IOException {
        this.path = path;

        InputStream is = MyClassLoader.getInstance().getResourceAsStream(path);

        SAXBuilder builder = new SAXBuilder();
        doc = builder.build(is);
        root = doc.getRootElement();
    }

    @Deprecated
    public void saveState() throws IOException {
        XMLOutputter xmlOutput = new XMLOutputter();
        String saveFile = this.getClass().getResource(path).getFile();
        FileWriter writer = new FileWriter(saveFile);

        xmlOutput.output(doc, writer);
        writer.flush();
        writer.close();
    }

    public void printScenario(){

        String s;

        List<Element> scenarioBody = root.getChildren();
        for (Element element : scenarioBody) {
            System.out.print(">>" + element.getName().toString() + ">>");
            if (element.getName().equals("CopyText")) {
                s = (element.getContent().isEmpty() ? "" : element.getContent().get(0).getValue());
                System.out.println(s);
            } else if(element.getName().equals("Action")) {
                s = (element.getAttribute("action").getValue().toString());
                System.out.println("action = \"" + s + "\"");
            } else if(element.getName().equals("MoveCaret")) {
                s =  (element.getAttribute("offset").getValue().toString());
                int offset = Integer.parseInt(s);
                System.out.println("offset: " + offset);
            } else {
                System.out.println(element.getContent());
            }
        }

    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    @Nullable
    public String getLang(){
        final Attribute lang = root.getAttribute("lang");
        if (lang != null) return lang.getValue();
        return null;
    }

    public String getName(){
        return root.getAttribute("name").getValue();
    }

    @Transient
    public Element getRoot(){
        return root;
    }

    public String getTarget() {
        return root.getAttribute("target").getValue();
    }

    public String getSubtype() {
        if (root.getAttribute("subtype") != null) {
            return root.getAttribute("subtype").getValue();
        } else {
            return "targeted";
        }
    }
}
