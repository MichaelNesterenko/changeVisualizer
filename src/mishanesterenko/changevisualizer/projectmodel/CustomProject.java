package mishanesterenko.changevisualizer.projectmodel;

import mishanesterenko.changevisualizer.activator.ChangeVisualizerPlugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

/**
 * @author Michael Nesterenko
 *
 */
public class CustomProject {
    public static final String REPOSITORY_LOCATION = "repository_location";
    public static final String USER_NAME = "user_name";
    public static final String USER_PASSWORD = "user_password";

    private IProject wrappedProject;

    private IEclipsePreferences projectPreferences;

    public CustomProject(final IProject project) {
        wrappedProject = project;
        projectPreferences = new ProjectScope(project).getNode(ChangeVisualizerPlugin.getPlugin().getBundle().getSymbolicName());
    }

    public IProject getProject() {
        return wrappedProject;
    }

    public String getRepositoryLocation() {
        return projectPreferences.get(REPOSITORY_LOCATION, "");
    }

    public String getUsername() {
        return projectPreferences.get(USER_NAME, "");
    }

    public String getUserPassword() {
        return projectPreferences.get(USER_PASSWORD, "");
    }

    public void setRepositoryLocation(final String value) {
        projectPreferences.put(REPOSITORY_LOCATION, value);
    }

    public void setUsername(final String value) {
        projectPreferences.put(USER_NAME, value);
    }

    public void setUserPassword(final String value) {
        projectPreferences.put(USER_PASSWORD, value);
    }

    public void flushProperties() throws BackingStoreException {
        projectPreferences.flush();
    }

}
