package mishanesterenko.changevisualizer.projectmodel;

import org.eclipse.core.resources.IProject;

/**
 * @author Michael Nesterenko
 *
 */
public class CustomProject {
    private IProject wrappedProject;

    public CustomProject(final IProject project) {
        wrappedProject = project;
    }

    public IProject getProject() {
        return wrappedProject;
    }

}
