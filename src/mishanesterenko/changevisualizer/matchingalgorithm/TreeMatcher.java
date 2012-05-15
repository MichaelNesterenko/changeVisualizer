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

    public static final int NGRAM = 2;

    private List<Entry<Double, Entry<Node, Node>>> matches;

    private Node leftRoot;

    private Node rightRoot;

    private PostOrderDepthTraversal leftTraversal;

    private BiMap<Node, Node> resultingMatches;

    private IProgressMonitor currentMonitor;

    private NodeFilter leafFilter = new NodeFilter() {
        @Override
        public boolean accept(final Node node) {
            return node.getChildren().size() == 0;
        }
    };

    private Action leftLeafAction;

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

        leftLeafAction = new Action() {
            private PostOrderDepthTraversal rightTraversal = new PostOrderDepthTraversal(rightRoot);

            private Node leftNode;

            private Action rightLeafAction = new Action() {
                @Override
                public void apply(final Node node) {
                    if (currentMonitor.isCanceled()) {
                        return;
                    }
                    double sim = similarity(leftNode.getValue(), node.getValue());
                    if (sim >= F) {
                        matches.add(new SimpleEntry<Double, Entry<Node, Node>>(sim, new SimpleEntry<Node, Node>(leftNode, node)));
                    }
                }
            };

            @Override
            public void apply(final Node node) {
                if (currentMonitor.isCanceled()) {
                    return;
                }
                try {
                    leftNode = node;
                    rightTraversal.foreach(rightLeafAction, leafFilter);
                } finally {
                    leftNode = null;
                }
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
        } finally {
            currentMonitor.done();
            currentMonitor = null;
        }
    }

    protected double internalSimilarity(final String l, final String r, final int nGram) {
        if (nGram <= 0) {
            return 0;
        }
        if (l.length() < nGram || r.length() < nGram) {
            return internalSimilarity(l, r, nGram - 1);
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

    protected double similarity(final String l, final String r) {
        return internalSimilarity(l, r, NGRAM);
    }

    protected double getSimilarity(final Node left, final Node right) {
        if (!left.getLabel().equals(right.getLabel()) && (left.getChildren().size() != 0 && right.getChildren().size() == 0
                || left.getChildren().size() == 0 && right.getChildren().size() != 0)) {
            return 0;
        }
        return 0;
    }
}
