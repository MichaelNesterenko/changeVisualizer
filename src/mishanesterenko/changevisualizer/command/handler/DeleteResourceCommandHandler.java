package mishanesterenko.changevisualizer.command.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.DeleteResourceAction;
import org.eclipse.ui.handlers.HandlerUtil;
import mishanesterenko.changevisualizer.projectmodel.CustomProject;

/**
 * @author Michael Nesterenko
 *
 */
public class DeleteResourceCommandHandler extends AbstractHandler {
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow activeWindow = HandlerUtil.getActiveWorkbenchWindow(event);
        final ISelectionService selectionService = activeWindow.getSelectionService();

        DeleteResourceAction deleteAction = new DeleteResourceAction(new CustomShellProvider(activeWindow)) {
            @Override
            public IStructuredSelection getStructuredSelection() {
                IStructuredSelection selection = (IStructuredSelection) selectionService.getSelection();
                Object element = selection.getFirstElement();
                if (element instanceof CustomProject) {
                    return new StructuredSelection(((CustomProject) element).getProject());
                }
                return selection;
            }
        };
        deleteAction.run();
        return null;
    }

}
