package mishanesterenko.changevisualizer.matchingalgorithm.traversal;

import mishanesterenko.changevisualizer.matchingalgorithm.domain.Node;

/**
 * @author Michael Nesterenko
 *
 */
public class PostOrderDepthTraversal extends AbstractTraversal {

    /**
     * @param root
     */
    public PostOrderDepthTraversal(Node root) {
        super(root);
    }

    @Override
    protected void internalForeach(Action action, Node node, NodeFilter filter) {
        if (filter.acceptChildren(node)) {
            for (Node child : node.getChildren()) {
                internalForeach(action, child, filter);
            }
        }
        if (filter.accept(node)) {
            action.apply(node);
        }
    }

}
