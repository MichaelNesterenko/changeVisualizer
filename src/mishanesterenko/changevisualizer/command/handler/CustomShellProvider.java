package mishanesterenko.changevisualizer.command.handler;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * @author Michael Nesterenko
 *
 */
public class CustomShellProvider implements IShellProvider {
    private Shell shell;

    public CustomShellProvider(final IWorkbenchWindow window) {
        shell = window.getShell();
    }

    @Override
    public Shell getShell() {
        return shell;
    }

}
