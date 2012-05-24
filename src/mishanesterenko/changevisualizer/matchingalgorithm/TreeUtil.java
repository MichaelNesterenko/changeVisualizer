package mishanesterenko.changevisualizer.matchingalgorithm;

import mishanesterenko.changevisualizer.matchingalgorithm.domain.Node;

/**
 * @author Michael Nesterenko
 *
 */
public class TreeUtil {
    public static int computeBreadth(final Node node) {
        int breadth = node.getChildren().size() == 0 ? 1 : 0;
        for (Node child : node.getChildren()) {
            breadth += computeBreadth(child);
        }
        return breadth;
    }

    public static int computeDepth(final Node node) {
        int depth = 1;
        for (Node child : node.getChildren()) {
            depth = Math.max(depth, computeDepth(child) + 1);
        }
        return depth;
    }
}
