/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementsAfter;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate message filters
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MessageFilter extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = "//*[local-name()='message-filter']";

  @Override
  public String getDescription() {
    return "Update message-filters.";
  }

  public MessageFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    if (element.getParentElement().isRootElement()) {
      // Nothing to do, this will be removed
      return;
    }

    if (element.getAttribute("throwOnUnaccepted") == null || element.getAttributeValue("throwOnUnaccepted").equals("false")) {
      report.report(ERROR, element, element,
                    "Validations always raise an error when the condition is not met.",
                    "https://docs.mulesoft.com/mule-runtime/4.1/migration-filters#applying_filters");
    }
    element.removeAttribute("throwOnUnaccepted");

    if (element.getAttribute("onUnaccepted") != null) {
      Element wrappingTry = new Element("try", CORE_NAMESPACE);

      addElementAfter(wrappingTry, element);
      wrappingTry.addContent(element.cloneContent());
      element.detach();

      wrappingTry.addContent(new Element("error-handler", CORE_NAMESPACE)
          .addContent(new Element("on-error-propagate", CORE_NAMESPACE)
              .setAttribute("type", "MULE:VALIDATION")
              .setAttribute("logException", "false")
              .addContent(new Element("flow-ref", CORE_NAMESPACE)
                  .setAttribute("name", element.getAttributeValue("onUnaccepted")))));

    } else {
      addElementsAfter(element.cloneContent(), element);
      element.detach();
    }

  }
}