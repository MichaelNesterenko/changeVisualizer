package mishanesterenko.changevisualizer.content.provider;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.core.viewers.IFigureProvider;
import mishanesterenko.changevisualizer.matchingalgorithm.domain.Node;

public class GraphLabelProvider extends LabelProvider implements IFigureProvider {
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
        Display d = Display.getDefault();
        Label l = new Label(getText(element));
        l.setOpaque(true);
        l.setFont(d.getSystemFont());
        l.setSize(l.getPreferredSize());
        l.setBackgroundColor(d.getSystemColor(SWT.COLOR_RED));
        return l;
    }
}