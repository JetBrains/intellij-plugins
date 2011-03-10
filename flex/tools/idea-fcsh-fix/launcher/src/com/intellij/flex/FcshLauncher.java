package com.intellij.flex;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class FcshLauncher {

    // For each element XXXX of this array class com.intellij.flex.Fcsh4WithFixXXXX must exist
    private static final int[] FLEX_4_SDK_BUILDS_THAT_HAVE_FIX = new int[]{0};
    // For each element XXXX of this array class com.intellij.flex.Fcsh45WithFixXXXX must exist
    private static final int[] FLEX_45_SDK_BUILDS_THAT_HAVE_FIX = new int[]{0};

    public static void main(final String[] args) {
        try {
            final String flexSdkVersion = getFlexSdkVersion();
            if (flexSdkVersion == null) {
                launchFcshWithoutFix(args);
            } else {
                launchFcshWithFix(flexSdkVersion, args);
            }
        } catch (OutOfMemoryError oomError) {
            System.out.println("(fcsh) out of memory");
            System.exit(0);
        }
    }

    private static void launchFcshWithoutFix(final String[] args) {
        Class entryPointClass;
        entryPointClass = findClass("flex2.tools.Fcsh");
        if (entryPointClass == null) {
            entryPointClass = findClass("flex2.tools.SimpleShell");
        }

        if (entryPointClass != null) {
            try {
                final Method main = entryPointClass.getMethod("main", args.getClass());
                main.invoke(null, new Object[]{args});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof VirtualMachineError) {
                    throw (VirtualMachineError) e.getCause();
                } else {
                    e.printStackTrace();
                }
            }
        } else {
            System.err.println("Flex SDK is corrupted.");
        }
    }

    private static void launchFcshWithFix(final String flexSdkVersion, final String[] args) {
        final Class entryPointClass = getEntryPointClass(flexSdkVersion);
        if (entryPointClass == null) {
            launchFcshWithoutFix(args);
        } else {
            try {
                final Method main = entryPointClass.getMethod("main", args.getClass());
                try {
                    main.invoke(null, new Object[]{args});
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof VirtualMachineError) {
                        throw (VirtualMachineError) e.getCause();
                    } else {
                        throw e;
                    }
                }
            } catch (VirtualMachineError e) {
                // OutOfMemoryError will be will handled in main()
                throw e;
            } catch (Throwable t) {
                System.out.println("(fcsh) need to repeat command");
                launchFcshWithoutFix(args);
            }
        }
    }

    private static Class getEntryPointClass(final String flexSdkVersion) {
        if (flexSdkVersion.startsWith("3")) {
            return findClass("com.intellij.flex.SimpleShellWithFix");
        } else if (flexSdkVersion.startsWith("4")) {
            if (flexSdkVersion.startsWith("4.5")) {
                return getFcshWithFixClass(flexSdkVersion, "com.intellij.flex.Fcsh45WithFix", FLEX_45_SDK_BUILDS_THAT_HAVE_FIX);
            } else {
                return getFcshWithFixClass(flexSdkVersion, "com.intellij.flex.Fcsh4WithFix", FLEX_4_SDK_BUILDS_THAT_HAVE_FIX);
            }
        }
        return null;
    }

    private static Class getFcshWithFixClass(final String flexSdkVersion, final String classNamePrefix, final int[] sdkBuildsThatHaveFix) {
        try {
            int build = Integer.parseInt(flexSdkVersion.substring(flexSdkVersion.indexOf(" build ") + " build ".length()));
            if (build == 0) {
                System.out.println("Warning: Flex SDK build can not be 0.");
            } else {
                for (int i = 0; i < sdkBuildsThatHaveFix.length; i++) {
                    int buildWithFix = sdkBuildsThatHaveFix[i];
                    if (sdkBuildsThatHaveFix.length == i + 1 || build < sdkBuildsThatHaveFix[i + 1]) {
                        return findClass(classNamePrefix + buildWithFix);
                    }
                }
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    private static String getFlexSdkVersion() {
        String version = readVersionUsingAPI();
        if (!checkFlexSdkVersion(version)) {
            version = readVersionFromFlexSdkDescriptionXml();
        }
        return checkFlexSdkVersion(version) ? version : null;
    }

    private static boolean checkFlexSdkVersion(final String flexSdkVersion) {
        return flexSdkVersion != null && flexSdkVersion.matches("[0-9][.][0-9].* build [0-9]+");
    }

    private static String readVersionUsingAPI() {
        String version = null;
        try {
            final Class versionInfoClass = findClass("flex2.tools.VersionInfo");
            final Method buildMessageMethod = versionInfoClass == null ? null : versionInfoClass.getMethod("buildMessage");
            version = buildMessageMethod == null ? null : (String) buildMessageMethod.invoke(null);
            if (version != null && version.startsWith("Version ")) {
                version = version.substring("Version ".length());
            }
        } catch (Throwable ignored) {
        }
        return version;
    }

    private static String readVersionFromFlexSdkDescriptionXml() {
        final String flexSdkHome = System.getProperty("application.home");
        if (flexSdkHome == null) {
            return null;
        }

        final File flexSdkDescriptionFile = new File(flexSdkHome + File.separatorChar + "flex-sdk-description.xml");
        if (!flexSdkDescriptionFile.exists()) {
            return null;
        }

        final String versionElement = "<flex-sdk-description><version>";
        final String buildElement = "<flex-sdk-description><build>";

        FileInputStream xmlInputStream = null;
        try {
            xmlInputStream = new FileInputStream(flexSdkDescriptionFile);
            final Map<String, List<String>> versionInfo = findXMLElements(xmlInputStream, Arrays.asList(versionElement, buildElement));
            return (versionInfo.get(versionElement).isEmpty() ? null : versionInfo.get(versionElement).get(0)) +
                    (versionInfo.get(buildElement).isEmpty() ? "" : (" build " + versionInfo.get(buildElement).get(0)));
        } catch (IOException e) {
            return null;
        } finally {
            close(xmlInputStream);
        }
    }

    /**
     * Looks through input stream containing XML document and finds all entries of XML elements listed in <code>xmlElements</code>.
     * Content of these elements is put to result map. XML namespaces are not taken into consideration.
     *
     * @param xmlInputStream input stream with xml content to parse
     * @param xmlElements    list of XML elements to look for.
     *                       Format is: <code>"&lt;root_element&gt;&lt;child_element&gt;&lt;subelement_to_look_for&gt;"</code>.
     *                       Listed XML elements MUST NOT contain subelements, otherwise result is undefined
     * @return map, keys are XML elements listed in <code>xmlElements</code>,
     *         values are all entries of respective element (may be empty list)
     */
    private static Map<String, List<String>> findXMLElements(final InputStream xmlInputStream, final List<String> xmlElements) {
        final Map<String, List<String>> resultMap = new HashMap<String, List<String>>();
        for (final String element : xmlElements) {
            resultMap.put(element, new ArrayList<String>());
        }

        try {
            SAXParserFactory.newInstance().newSAXParser().parse(xmlInputStream, new DefaultHandler() {

                private String currentElement = "";
                private StringBuilder currentElementContent = new StringBuilder();

                public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
                    currentElement += "<" + qName + ">";
                }

                public void endElement(final String uri, final String localName, final String qName) throws SAXException {
                    if (xmlElements.contains(currentElement)) {
                        resultMap.get(currentElement).add(currentElementContent.toString());
                        currentElementContent.delete(0, currentElementContent.length());
                    }
                    assert currentElement.endsWith("<" + qName + ">");
                    currentElement = currentElement.substring(0, currentElement.length() - (qName.length() + 2));
                }

                public void characters(char[] ch, int start, int length) throws SAXException {
                    if (xmlElements.contains(currentElement)) {
                        currentElementContent.append(ch, start, length);
                    }
                }

            });
        } catch (SAXException ignored) {
        } catch (IOException ignored) {
        } catch (ParserConfigurationException ignored) {
        }

        return resultMap;
    }

    private static Class findClass(final String className) {
        try {
            return Class.forName(className);
        } catch (Throwable t) {
            return null;
        }
    }

    private static void close(final Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }
}
