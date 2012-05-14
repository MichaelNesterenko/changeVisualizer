package mishanesterenko.changevisualizer.vcsadapter;

import mishanesterenko.changevisualizer.projectmodel.CustomProject;

/**
 * @author Michael Nesterenko
 *
 */
public class AbstractAdapter {
    private CustomProject project;

    public AbstractAdapter(final CustomProject p) {
        if (p == null) {
            throw new NullPointerException();
        }
        project = p;
    }

    public CustomProject getProject() {
        return project;
    }

}
