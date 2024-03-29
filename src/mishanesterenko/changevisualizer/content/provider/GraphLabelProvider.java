package mishanesterenko.changevisualizer.content.provider;

import java.util.Set;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.zest.core.viewers.IFigureProvider;

import com.google.common.collect.BiMap;

import mishanesterenko.changevisualizer.dialog.MouseLocationPopupDialog;
import mishanesterenko.changevisualizer.matchingalgorithm.domain.Node;

public class GraphLabelProvider extends LabelProvider implements IFigureProvider {
    private static final int UPDATED_COLOR = SWT.COLOR_YELLOW;

    private static final int MOVED_COLOR = SWT.COLOR_BLUE;

    private static final int ADDED_COLOR = SWT.COLOR_GREEN;

    private static final int DELETED_COLOR = SWT.COLOR_RED;

    private static final int UNCHANGED_COLOR = SWT.COLOR_WHITE;

    private BiMap<Node, Node> matchedNodes;

    private Set<Node> owningNodes;

    private boolean isLeftGraph;

    private Shell viewerShell;

    public GraphLabelProvider(final boolean isLeftGraph, final Shell shell) {
        this.isLeftGraph = isLeftGraph;
        viewerShell = shell;
    }

    /**
     * @return the matchedNodes
     */
    public BiMap<Node, Node> getMatchedNodes() {
        return matchedNodes;
    }

    public Set<Node> getOwningNodes() {
        return owningNodes;
    }

    /**
     * @param matchedNodes the matchedNodes to set
     */
    public void setMatchedNodes(final BiMap<Node, Node> matchedNodes, final Set<Node> ownNodes) {
        this.matchedNodes = matchedNodes;
        owningNodes = ownNodes;
    }

    @Override
    public String getText(Object element) {
        Node n = element instanceof Node ? ((Node) element) : null;
        if (n == null) {
            return "";
        }
        String value = n.getValue().substring(0, Math.min(n.getValue().length(), 100));
//        int newLinePos = value.indexOf("\n");
//        value = newLinePos == -1 ? value : value.substring(0, newLinePos);
//        value = n.getLabel() + "(" + value + ")";
        return value;
    }

    @Override
    public IFigure getFigure(final Object element) {
        final Node node = (Node) element;
        final Label l = new Label(getText(element));
        
        l.setOpaque(true);
        l.setFont(viewerShell.getDisplay().getSystemFont());
        l.setSize(l.getPreferredSize());
        l.setBackgroundColor(determineColor(node));
        l.setBorder(new LineBorder(2));
        l.addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(final MouseEvent me) {
            }

            @Override
            public void mouseReleased(final MouseEvent me) {
            }

            @Override
            public void mouseDoubleClicked(final MouseEvent me) {
                MouseLocationPopupDialog d = new MouseLocationPopupDialog(viewerShell, node.getValue());
                d.open();
            }
        });
        return l;
    }

    protected Color determineColor(final Node node) {
        int foundColor = UNCHANGED_COLOR;
        if (isLeftGraph) {
            foundColor = matchedNodes.containsKey(node) ? foundColor : DELETED_COLOR;
        } else {
            if (matchedNodes.containsValue(node)) {
                Node leftNode = matchedNodes.inverse().get(node);
                if (!leftNode.getValue().equals(node.getValue())) {
                    foundColor = UPDATED_COLOR;
                }
                { // move detection
                    Node leftMatchedParent = matchedNodes.inverse().get(node).getParent();
                    if (leftMatchedParent != leftNode.getParent()) {
                        foundColor = MOVED_COLOR;
                    }
                }
            } else {
                foundColor = ADDED_COLOR;
            }
        }
        return Display.getDefault().getSystemColor(foundColor);
    }
}