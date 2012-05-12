package mishanesterenko.changevisualizer.matchingalgorithm.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Nesterenko
 *
 */
public class Node {
    private Node parent;
    private String label;
    private String value;
    private List<Node> children;

    public Node() {
        this(null, null, null, null);
    }

    public Node(final Node parentNode, final String lbl, final String val, final List<Node> childrenNodes) {
        parent = parentNode;
        label = lbl == null ? "" : lbl;
        value = val == null ? "" : val;
        children = childrenNodes == null ? new ArrayList<Node>() : childrenNodes;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(final Node parentNode) {
        parent = parentNode;
    }

    public List<Node> getChildren() {
        return children;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String lbl) {
        label = lbl;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String val) {
        value = val;
    }

}
