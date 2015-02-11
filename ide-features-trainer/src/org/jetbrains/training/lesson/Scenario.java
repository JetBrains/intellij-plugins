package org.jetbrains.training.lesson;


import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


/**
 * Created by karashevich on 30/12/14.
 */
public class Scenario {

    final private String path;
    private Element root;
    private Document doc;

    public Scenario(String path) throws JDOMException, IOException {
        this.path = path;

        InputStream is = this.getClass().getResourceAsStream(path);

        SAXBuilder builder = new SAXBuilder();
        doc = builder.build(is);
        root = doc.getRootElement();
    }

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

    public String getName(){
        return root.getAttribute("name").getValue();
    }

    public Element getRoot(){
        return root;
    }

    public String getTarget() {
        return root.getAttribute("target").getValue();
    }
}
