package mishanesterenko.changevisualizer.wizard;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Michael Nesterenko
 *
 */
public interface IProjectSettingsUpdater {
    void preCreate(final IProjectDescription description) throws CoreException;
    void postCreate(final IProject project, final IProjectDescription description) throws CoreException;
}
