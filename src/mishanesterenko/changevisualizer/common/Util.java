package mishanesterenko.changevisualizer.common;

import mishanesterenko.changevisualizer.activator.ChangeVisualizerPlugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.IStatusAdapterConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * @author Michael Nesterenko
 *
 */
public class Util {
    public static void showError(final Throwable cause, final String title) {
        IStatus status = new Status(IStatus.ERROR, ChangeVisualizerPlugin.PLUGIN_ID, cause.getMessage(), cause);

        StatusAdapter statusAdapter = new StatusAdapter(status);
        statusAdapter.setProperty(IStatusAdapterConstants.TITLE_PROPERTY, title == null ? "Error" : title);
        StatusManager.getManager().handle(statusAdapter, StatusManager.BLOCK);
    }
}
