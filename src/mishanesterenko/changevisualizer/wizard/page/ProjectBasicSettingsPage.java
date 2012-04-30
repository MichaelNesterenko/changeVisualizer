package mishanesterenko.changevisualizer.wizard.page;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import mishanesterenko.changevisualizer.activator.ChangeVisualizerPlugin;
import mishanesterenko.changevisualizer.wizard.IProjectSettingsUpdater;
import mishanesterenko.changevisualizer.wizard.WizardMessages;

/**
 * @author Michael Nesterenko
 *
 */
public class ProjectBasicSettingsPage extends WizardNewProjectCreationPage implements IProjectSettingsUpdater {

    public ProjectBasicSettingsPage(final String pageName) {
        super(pageName);
    }

    @Override
    public void preCreate(IProjectDescription description) throws CoreException {
        description.setLocation(useDefaults() ? null : getLocationPath());
    }

    @Override
    public void postCreate(final IProject project, final IProjectDescription description) throws CoreException {
        try {
            getContainer().run(true, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        project.create(description, monitor);
                        if (!project.isOpen()) {
                            project.open(monitor);
                        }
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (Exception e) {
            Throwable cause = e.getCause();
            cause = cause == null ? e : cause;
            if (cause instanceof CoreException) {
                throw (CoreException) e.getCause();
            }
            IStatus status = new Status(IStatus.ERROR,
                    ChangeVisualizerPlugin.getPlugin().getBundle().getSymbolicName(),
                    NLS.bind(WizardMessages.ProjectBasicSettingsPage_can_not_create_project, project.getName()),
                    cause);
            throw new CoreException(status);
        }
    }

}
