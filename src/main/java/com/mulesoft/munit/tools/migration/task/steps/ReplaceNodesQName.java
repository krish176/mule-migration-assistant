package com.mulesoft.munit.tools.migration.task.steps;

import org.jdom2.Element;
import org.jdom2.Namespace;

public class ReplaceNodesQName extends MigrationStep {

    private String newNamespace;
    private String namespaceName;

    public ReplaceNodesQName(String newNamespace, String namespaceName) {
        this.newNamespace = newNamespace;
        this.namespaceName = namespaceName;
    }

    public void execute() throws Exception {
        if (getDocument() != null) {
            Namespace namespace = getDocument().getRootElement().getNamespace(newNamespace);
            if (namespace != null) {
                for (Element node : getNodes()) {
                    node.setNamespace(namespace);
                    node.setName(namespaceName);
                }
            }
        }
    }

}
