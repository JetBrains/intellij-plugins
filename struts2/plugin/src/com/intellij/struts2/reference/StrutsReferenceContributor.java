/*
 * Copyright 2011 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts2.reference;

import com.intellij.javaee.model.xml.ParamValue;
import com.intellij.javaee.model.xml.web.Filter;
import com.intellij.patterns.XmlElementPattern;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.validator.Message;
import com.intellij.struts2.reference.web.WebXmlStrutsConstantNameReferenceProvider;
import com.intellij.struts2.reference.web.WebXmlStrutsConstantValueReferenceProvider;

import static com.intellij.patterns.DomPatterns.*;
import static com.intellij.patterns.PsiJavaPatterns.psiClass;
import static com.intellij.patterns.StandardPatterns.or;
import static com.intellij.patterns.StandardPatterns.string;
import static com.intellij.patterns.XmlPatterns.xmlAttributeValue;
import static com.intellij.patterns.XmlPatterns.xmlTag;

/**
 * Registers all {@link PsiReferenceProvider}s for {@code struts.xml/web.xml}.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsReferenceContributor extends PsiReferenceContributor {

  /**
   * web.xml: match inside {@code <filter>}-element with S2-Filter.
   */
  private static final XmlElementPattern.Capture WEB_XML_STRUTS_FILTER =
      withDom(domElement().
          withParent(domElement(ParamValue.class).
              withParent(domElement(Filter.class).
                  withChild("filter-class", genericDomValue().withValue(
                      or(psiClass().inheritorOf(false, StrutsConstants.STRUTS_2_0_FILTER_CLASS),
                         psiClass().inheritorOf(false, StrutsConstants.STRUTS_2_1_FILTER_CLASS)
                        )
                                                                       )
                           )
                        )
                    )
             );

  public void registerReferenceProviders(final PsiReferenceRegistrar registrar) {
    registerWebXml(registrar);
    registerStrutsXmlTags(registrar);
    registerValidationXmlTags(registrar);
  }

  /**
   * {@code <param-name>/<param-value>}.
   *
   * @param registrar Registrar.
   */
  private void registerWebXml(final PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(
        xmlTag().withLocalName("param-name").and(WEB_XML_STRUTS_FILTER),
        new WebXmlStrutsConstantNameReferenceProvider());

    registrar.registerReferenceProvider(
        xmlTag().withLocalName("param-value").and(WEB_XML_STRUTS_FILTER),
        new WebXmlStrutsConstantValueReferenceProvider());
  }

  private void registerStrutsXmlTags(final PsiReferenceRegistrar registrar) {

    // <result> "name" common values
    registrar.registerReferenceProvider(
        xmlAttributeValue().withLocalName("name").
            withSuperParent(2, withDom(domElement(Result.class))),
        new StaticStringValuesReferenceProvider("error", "input", "login", "success"));

    // <result> action property
    registrar.registerReferenceProvider(
        xmlTag().and(withDom(domElement(Result.class))).withText(string().contains("${")),
        new ResultActionPropertyReferenceProvider());
  }

  private void registerValidationXmlTags(final PsiReferenceRegistrar registrar) {

    // <message> "key"
    registrar.registerReferenceProvider(
        xmlAttributeValue().withLocalName("key").
            withSuperParent(2, withDom(domElement(Message.class))),
        new WrappedPropertiesReferenceProvider());
  }

}