package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Element;
import org.jdom2.Namespace;

import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;

public class DeleteChildNode extends MigrationStep {

    private String node;
    private String nodeNamespace;
    private String nodeNamespaceUri;

    public DeleteChildNode(String node, String nodeNamespace, String nodeNamespaceUri) {
        setNode(node);
        setNodeNamespace(nodeNamespace);
        setNodeNamespaceUri(nodeNamespaceUri);
    }

    public DeleteChildNode(){}

    public void execute() throws Exception {
        try {
            Namespace namespace = Namespace.getNamespace(getNodeNamespace(), getNodeNamespaceUri());
            for (Element node : getNodes()) {
                Element element = node.getChild(getNode(),namespace);
                if (element != null) {
                    node.removeChild(getNode(),namespace);

                    getReportingStrategy().log("Child Node <" + node.getQualifiedName() + "> was deleted", RULE_APPLIED);
                }
            }
        } catch (Exception ex) {
            throw new MigrationStepException("Remove child node exception. " + ex.getMessage());
        }
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getNodeNamespace() {
        return nodeNamespace;
    }

    public void setNodeNamespace(String nodeNamespace) {
        this.nodeNamespace = nodeNamespace;
    }

    public String getNodeNamespaceUri() {
        return nodeNamespaceUri;
    }

    public void setNodeNamespaceUri(String nodeNamespaceUri) {
        this.nodeNamespaceUri = nodeNamespaceUri;
    }
}