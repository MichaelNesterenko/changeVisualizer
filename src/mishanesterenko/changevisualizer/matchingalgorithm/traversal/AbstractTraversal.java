package mishanesterenko.changevisualizer.matchingalgorithm.traversal;

import mishanesterenko.changevisualizer.matchingalgorithm.domain.Node;

/**
 * @author Michael Nesterenko
 *
 */
public abstract class AbstractTraversal {
    private Node rootNode;

    public AbstractTraversal(final Node root) {
        rootNode = root;
    }

    public void foreach(final Action action, final NodeFilter filter) {
        internalForeach(action, rootNode, filter);
    }

    protected void internalForeach(final Action action, final Node node, final NodeFilter filter) {
    }

    public static class NodeFilter {
        public boolean accept(final Node node) {
            return true;
        }

        public boolean acceptChildren(final Node node) {
            return true;
        }
    }

    public static class Action {
        public void apply(final Node node) {
        }
    }
}
