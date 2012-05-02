package mishanesterenko.changevisualizer.command.handler;

import mishanesterenko.changevisualizer.projectmodel.CustomProject;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Michael Nesterenko
 *
 */
public class CustomSelectionProvider implements ISelectionProvider {

    private ISelectionProvider wrappedSelectionProvider;

    public CustomSelectionProvider(final ISelectionProvider selectionProvider) {
        if (selectionProvider == null) {
            throw new NullPointerException();
        }
        wrappedSelectionProvider = selectionProvider;
    }

    @Override
    public void addSelectionChangedListener(final ISelectionChangedListener listener) {
        wrappedSelectionProvider.addSelectionChangedListener(listener);
    }

    @Override
    public ISelection getSelection() {
        ISelection selection = wrappedSelectionProvider.getSelection();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            if (structuredSelection.getFirstElement() instanceof CustomProject) {
                Object[] objs = structuredSelection.toArray();
                for (int i = 0; i < objs.length; ++i) {
                    if (objs[i] instanceof CustomProject) {
                        objs[i] = ((CustomProject) objs[i]).getProject();
                    }
                }
                return new StructuredSelection(objs);
            }
        }
        return selection;
    }

    @Override
    public void removeSelectionChangedListener(final ISelectionChangedListener listener) {
        wrappedSelectionProvider.removeSelectionChangedListener(listener);
    }

    @Override
    public void setSelection(final ISelection selection) {
        wrappedSelectionProvider.setSelection(selection);
    }

}
