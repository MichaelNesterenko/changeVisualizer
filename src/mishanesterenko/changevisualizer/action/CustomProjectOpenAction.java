package mishanesterenko.changevisualizer.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import mishanesterenko.changevisualizer.projectmodel.CustomProject;
import mishanesterenko.changevisualizer.view.HistoryView;

/**
 * @author Michael Nesterenko
 *
 */
public class CustomProjectOpenAction extends Action {
    private ISelectionProvider selectionProvider;

    private IWorkbenchPage page;

    public CustomProjectOpenAction(final ISelectionProvider provider, final IWorkbenchPage p) {
        selectionProvider = provider;
        page = p;
    }

    @Override
    public boolean isEnabled() {
        if (!selectionProvider.getSelection().isEmpty()) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selectionProvider.getSelection();
            Object element = structuredSelection.getFirstElement(); 
            return (element instanceof CustomProject && structuredSelection.size() == 1);
        }
        return false;
    }

    @Override
    public void run() {
        super.run();
        IViewPart viewPart = page.findView(HistoryView.ID);
        if (viewPart != null) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selectionProvider.getSelection();
            CustomProject project = (CustomProject) structuredSelection.getFirstElement();
            HistoryView historyView = (HistoryView) viewPart;
            historyView.selectProject(project);
        }
    }
}
