package org.jetbrains.training;


import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;


/**
 * Created by karashevich on 30/12/14.
 */
public class Scenario {

    final private String path;
    private Element root;

    public Scenario(String path) throws JDOMException, IOException {
        this.path = path;

        InputStream is = this.getClass().getResourceAsStream(path);

        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(is);
        root = doc.getRootElement();
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
            } else {
                System.out.println(element.getContent());
            }
        }

    }

    public Element getRoot(){
        return root;
    }

}
