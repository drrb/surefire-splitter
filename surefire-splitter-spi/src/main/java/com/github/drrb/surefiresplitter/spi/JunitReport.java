/**
 * surefire-splitter-spi
 * Copyright (C) 2016 drrb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with surefire-splitter-spi. If not, see <http://www.gnu.org/licenses />.
 */
package com.github.drrb.surefiresplitter.spi;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static com.github.drrb.surefiresplitter.spi.JunitReport.JunitTestCase.testCase;
import static java.lang.Double.parseDouble;

public class JunitReport {

    public static class ReadFailure extends Exception {
        public ReadFailure(Path report, Throwable cause) {
            super("Couldn't parse file as JUnit report '" + report + "'", cause);
        }
    }

    public static class JunitTestSuite {

        public static class Builder {

            private final List<JunitTestCase> testCases = new LinkedList<>();
            private String name;
            private double time;

            public Builder withName(String name) {
                this.name = name;
                return this;
            }

            public Builder withTime(double time) {
                this.time = time;
                return this;
            }

            public Builder withTestCase(JunitTestCase testCase) {
                this.testCases.add(testCase);
                return this;
            }

            public JunitTestSuite build() {
                return new JunitTestSuite(name, time, testCases);
            }
        }

        public static Builder testSuite() {
            return new Builder();
        }

        private final String name;
        private final double time;
        private final List<JunitTestCase> cases;

        public JunitTestSuite(String name, Double time, List<JunitTestCase> cases) {
            this.name = name;
            this.time = time;
            this.cases = cases;
        }

        public String getName() {
            return name;
        }

        public Double getTime() {
            return time;
        }

        public List<JunitTestCase> getCases() {
            return cases;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof JunitTestSuite) {
                JunitTestSuite that = (JunitTestSuite) o;
                return this.name.equals(that.name);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return "JunitTestSuite{" +
                    "name='" + name + '\'' +
                    ", time=" + time +
                    '}';
        }
    }

    public static class JunitTestCase {

        public static class Builder {

            private String name;

            private String className;

            public Builder withName(String name) {
                this.name = name;
                return this;
            }

            public Builder withClassName(String className) {
                this.className = className;
                return this;
            }

            public JunitTestCase build() {
                return new JunitTestCase(name, className);
            }

        }

        public static Builder testCase() {
            return new Builder();
        }

        private final String name;
        private final String className;

        public JunitTestCase(String name, String className) {
            this.name = name;
            this.className = className;
        }

        public String getName() {
            return name;
        }

        public String getClassName() {
            return className;
        }

    }

    public static JunitTestSuite parse(Path xml) throws ReadFailure {
        try {
            Document document = parseXml(xml);
            Element suiteElement = getElementOrFail(document, "testsuite");

            JunitTestSuite.Builder testSuite = JunitTestSuite.testSuite()
                    .withName(getAttributeOrFail(suiteElement, "name"))
                    .withTime(parseDouble(getAttributeOrFail(suiteElement, "time")));

            NodeList caseElements = suiteElement.getElementsByTagName("testcase");
            for (int i = 0; i < caseElements.getLength(); i++) {
                Element caseElement = (Element) caseElements.item(i);
                testSuite.withTestCase(
                        testCase()
                                .withName(getAttributeOrFail(caseElement, "name"))
                                .withClassName(getAttributeOrFail(caseElement, "classname"))
                                .build()
                );
            }

            return testSuite.build();
        } catch (ReadFailure e) {
            throw e;
        } catch (Exception e) {
            throw new ReadFailure(xml, e);
        }
    }

    private static Element getElementOrFail(Document document, String tagName) {
        NodeList elements = document.getElementsByTagName(tagName);
        Element element = (Element) elements.item(0);
        if (element == null) {
            throw new RuntimeException("No '" + tagName + "' element found");
        }
        return element;
    }

    private static String getAttributeOrFail(Element element, String attribute) {
        String value = element.getAttribute(attribute);
        if (value == null) {
            throw new RuntimeException("Couldn't get attribute '" + attribute + "' from element '" + element.getTagName() + "'");
        }
        return value;
    }

    private static Document parseXml(Path file) throws ReadFailure {
        DocumentBuilder documentBuilder = newDocumentBuilder();
        try {
            return documentBuilder.parse(file.toFile());
        } catch (IOException | SAXException e) {
            throw new ReadFailure(file, e);
        }
    }

    private static DocumentBuilder newDocumentBuilder() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            documentBuilderFactory.setXIncludeAware(false);
            documentBuilderFactory.setExpandEntityReferences(false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            return documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
