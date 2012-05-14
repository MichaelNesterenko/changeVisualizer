package mishanesterenko.changevisualizer.content.provider;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.zest.core.viewers.IGraphEntityContentProvider;
import mishanesterenko.changevisualizer.matchingalgorithm.domain.Node;

public class GraphContentProvider implements IGraphEntityContentProvider {
    @Override
    public Object[] getConnectedTo(Object entity) {
        return ((Node) entity).getChildren().toArray();
    }

    @Override
    public void dispose() {
        ArrayContentProvider.getInstance().dispose();
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        ArrayContentProvider.getInstance().dispose();
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return ArrayContentProvider.getInstance().getElements(inputElement);
    }
}