package mishanesterenko.changevisualizer.content.provider;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.core.viewers.IFigureProvider;

import com.google.common.collect.BiMap;

import mishanesterenko.changevisualizer.matchingalgorithm.domain.Node;

public class GraphLabelProvider extends LabelProvider implements IFigureProvider {
    private BiMap<Node, Node> matchedNodes;

    /**
     * @return the matchedNodes
     */
    public BiMap<Node, Node> getMatchedNodes() {
        return matchedNodes;
    }

    /**
     * @param matchedNodes the matchedNodes to set
     */
    public void setMatchedNodes(BiMap<Node, Node> matchedNodes) {
        this.matchedNodes = matchedNodes;
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
    public IFigure getFigure(Object element) {
        final Display d = Display.getDefault();
        final Label l = new Label(getText(element));
        l.setOpaque(true);
        l.setFont(d.getSystemFont());
        l.setSize(l.getPreferredSize());
        l.setBackgroundColor(d.getSystemColor(matchedNodes.containsKey(element) || matchedNodes.containsValue(element)
                ? SWT.COLOR_GREEN : SWT.COLOR_RED));
//        l.addMouseMotionListener(new MouseMotionListener() {
//            @Override
//            public void mouseMoved(MouseEvent me) {
//            }
//
//            @Override
//            public void mouseHover(MouseEvent me) {
//            }
//
//            @Override
//            public void mouseExited(MouseEvent me) {
//                l.setBackgroundColor(d.getSystemColor(SWT.COLOR_BLUE));
//            }
//
//            @Override
//            public void mouseEntered(MouseEvent me) {
//                l.setBackgroundColor(d.getSystemColor(SWT.COLOR_GREEN));
//            }
//
//            @Override
//            public void mouseDragged(MouseEvent me) {
//            }
//        });
        return l;
    }
}