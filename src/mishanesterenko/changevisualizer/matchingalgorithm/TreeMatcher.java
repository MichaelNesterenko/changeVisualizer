package mishanesterenko.changevisualizer.matchingalgorithm;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import mishanesterenko.changevisualizer.matchingalgorithm.domain.Node;
import mishanesterenko.changevisualizer.matchingalgorithm.traversal.AbstractTraversal.Action;
import mishanesterenko.changevisualizer.matchingalgorithm.traversal.PostOrderDepthTraversal;

import mishanesterenko.changevisualizer.matchingalgorithm.traversal.AbstractTraversal.NodeFilter;

/**
 * @author Michael Nesterenko
 *
 */
public class TreeMatcher {
    public static final double F = 0.5;

    public static final double F_SMALL_TREE = 0.4;

    public static final double F_NORMAL_TREE = 0.6;

    public static final int N_SMALL_TREE = 4;

    public static final int NGRAM = 2;

    private List<Entry<Double, Entry<Node, Node>>> matches;

    private Node leftRoot;

    private Node rightRoot;

    private PostOrderDepthTraversal leftTraversal;

    private BiMap<Node, Node> resultingMatches;

    private IProgressMonitor currentMonitor;

    private NodeFilter leafFilter = new LeafFilter();

    private Action leftLeafAction;

    private Action leftNodeAction;

    private PostOrderDepthTraversal rightTraversal;

    NonMatchedNodeFilter nonMatchedNodeFilter = new NonMatchedNodeFilter();

    private Comparator<Entry<Double, Entry<Node, Node>>> matchComparator = new Comparator<Entry<Double, Entry<Node, Node>>>() {
        @Override
        public int compare(Entry<Double, Entry<Node, Node>> o1, Entry<Double, Entry<Node, Node>> o2) {
            return Double.compare(o2.getKey(), o1.getKey());
        }
    };

    public TreeMatcher(final Node leftNode, final Node rightNode) {
        leftRoot = leftNode;
        rightRoot = rightNode;
        matches = new ArrayList<Entry<Double,Entry<Node,Node>>>();
        resultingMatches = HashBiMap.create();

        rightTraversal = new PostOrderDepthTraversal(rightRoot);
        leftLeafAction = new AbstractLeftAction() {
            private Action rightLeafAction = new Action() {
                @Override
                public void apply(final Node node) {
                    if (currentMonitor.isCanceled()) {
                        return;
                    }
                    double sim;
                    if (getLeftNode().getLabel().equals(node.getLabel())
                            && (sim = leafSimilarity(getLeftNode().getValue(), node.getValue())) >= F) {
                        matches.add(
                                new SimpleEntry<Double, Entry<Node, Node>>(sim, new SimpleEntry<Node, Node>(getLeftNode(), node)));
                    }
                }
            };

            @Override
            protected Action getRightAction() {
                return rightLeafAction;
            }

            @Override
            protected NodeFilter getFilter() {
                return leafFilter;
            }
        };

        leftNodeAction = new AbstractLeftAction() {
            private Action rightNodeAction = new Action() {
                @Override
                public void apply(final Node node) {
                    if (resultingMatches.containsKey(getLeftNode()) || resultingMatches.containsValue(node)) {
                        return;
                    }
                    if (equal(getLeftNode(), node)) {
                        resultingMatches.put(getLeftNode(), node);
                    }
                }
            };

            @Override
            protected Action getRightAction() {
                return rightNodeAction;
            }

            @Override
            protected NodeFilter getFilter() {
                return nonMatchedNodeFilter;
            }
        };

        leftTraversal = new PostOrderDepthTraversal(leftRoot);
    }

    public BiMap<Node, Node> getMatches() {
        return resultingMatches;
    }

    public void match(final IProgressMonitor monitor) {
        try {
            if (matches == null) {
                return;
            }
            currentMonitor = SubMonitor.convert(monitor);
            leftTraversal.foreach(leftLeafAction, leafFilter);
            Collections.sort(matches, matchComparator);

            Set<Node> foundNodeSet = new HashSet<Node>();
            resultingMatches = HashBiMap.create();
            for(Entry<Double, Entry<Node, Node>> match : matches) {
                if (currentMonitor.isCanceled()) {
                    return;
                }
                Node l = match.getValue().getKey();
                Node r = match.getValue().getValue();
                if (foundNodeSet.contains(l) || foundNodeSet.contains(r)) {
                    continue;
                }
                foundNodeSet.add(l);
                foundNodeSet.add(r);
                resultingMatches.put(l, r);
            }
            matches = null;

            leftTraversal.foreach(leftNodeAction, nonMatchedNodeFilter);
        } finally {
            currentMonitor.done();
            currentMonitor = null;
        }
    }

    protected double internalLeafSimilarity(final String l, final String r, final int nGram) {
        if (nGram <= 0) {
            return 0;
        }
        if (l.length() < nGram || r.length() < nGram) {
            return internalLeafSimilarity(l, r, nGram - 1);
        }
        String[] lNGrams = new String[l.length() - nGram + 1];
        String[] rNGrams = new String[r.length() - nGram + 1];
        int length = l.length();
        for (int i = 0; i <= length - nGram; ++i) {
            lNGrams[i] = l.substring(i, i + nGram);
        }
        length = r.length();
        for (int i = 0; i <= length - nGram; ++i) {
            rNGrams[i] = r.substring(i, i + nGram);
        }
        int simCount = 0;
        for (int i = 0; i < lNGrams.length; ++i) {
            for (int j = 0; j < rNGrams.length; ++j) {
                if (rNGrams[j] == null) {
                    continue;
                }
                if (lNGrams[i].equals(rNGrams[j])) {
                    simCount++;
                    rNGrams[j] = null;
                    break;
                }
            }
        }
        return (double) 2 * simCount / (lNGrams.length + rNGrams.length);
    }

    protected double leafSimilarity(final String l, final String r) {
        return internalLeafSimilarity(l, r, NGRAM);
    }

    protected double nodeSimilarity(final Node l, final Node r) {
        if (l.getLabel().equals(r.getLabel())) {
            final int max = Math.max(l.getChildren().size(), r.getChildren().size());
            int common = 0;
            Set<Node> matchedSet = new HashSet<Node>();
            for (Node child : l.getChildren()) {
                Node companion = resultingMatches.get(child);
                if (companion != null && companion.getParent() == r
                        && !matchedSet.contains(child) && !matchedSet.contains(companion)) {
                    matchedSet.add(child);
                    matchedSet.add(companion);
                    common++;
                }
            }
            for (Node child : r.getChildren()) {
                Node companion = resultingMatches.get(child);
                if (companion != null && companion.getParent() == l
                        && !matchedSet.contains(child) && !matchedSet.contains(companion)) {
                    matchedSet.add(child);
                    matchedSet.add(companion);
                    common++;
                }
            }
            return (double) common / max;
        }
        return 0;
    }

    protected boolean equal(final Node l, final Node r) {
        final double f = Math.min(l.getChildren().size(), r.getChildren().size()) <= N_SMALL_TREE ? F_SMALL_TREE : F_NORMAL_TREE;
        return nodeSimilarity(l, r) >= f;
    }

    protected class NonMatchedNodeFilter extends LeafFilter {
        //private BiMap<Node, Node> matchedNodes;

//        public NonMatchedNodeFilter(final BiMap<Node, Node> matched) {
//            matchedNodes = matched;
//        }

        @Override
        public boolean accept(final Node node) {
            return !super.accept(node) && !resultingMatches.containsKey(node) && !resultingMatches.containsValue(node);
        }
    }

    protected class LeafFilter extends NodeFilter {
        @Override
        public boolean accept(final Node node) {
            return node.getChildren().size() == 0;
        }
    }

    protected abstract class AbstractLeftAction extends Action {
        private Node leftNode;

        protected void setLeftNode(final Node node) {
            leftNode = node;
        }

        protected Node getLeftNode() {
            return leftNode;
        }

        protected abstract Action getRightAction();

        protected abstract NodeFilter getFilter();

        @Override
        public void apply(final Node node) {
            if (currentMonitor.isCanceled()) {
                return;
            }
            try {
                setLeftNode(node);
                rightTraversal.foreach(getRightAction(), getFilter());
            } finally {
                setLeftNode(null);
            }
        }
    }
}
