package mishanesterenko.changevisualizer.propertypage;

import mishanesterenko.changevisualizer.common.ProjectCustomPropertiesControls;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * @author Michael Nesterenko
 *
 */
public abstract class BasePropertyPage extends PropertyPage {
    public static final QualifiedName REPOSITORY_LOCATION = new QualifiedName("changeVisualizer", "repository_location");
    public static final QualifiedName USER_NAME = new QualifiedName("changeVisualizer", "user_name");
    public static final QualifiedName USER_PASSWORD = new QualifiedName("changeVisualizer", "user_password");
    
    private Text repositoryLocation;

    private Text userName;

    private Text userPassword;

    @Override
    protected Control createContents(Composite parent) {
        ProjectCustomPropertiesControls controlsSource = new ProjectCustomPropertiesControls();
        Composite tlc = controlsSource.createContents(parent);

        repositoryLocation = controlsSource.getRepositoryLocation();
        userName = controlsSource.getUserName();
        userPassword = controlsSource.getUserPassword();

        try {
            String value;
            repositoryLocation.setText((value = loadRepositoryLocation()) == null ? "" : value);
            userName.setText((value = loadUsername()) == null ? "" : value);
            userPassword.setText((value = loadUserPassword()) == null ? "" : value);
        } catch (CoreException e) {
            e.printStackTrace();
        }

        {
            repositoryLocation.setEnabled(false);
        }

        return tlc;
    }

    protected String loadRepositoryLocation() throws CoreException {
        IResource resource = (IResource) getElement().getAdapter(IResource.class);
        return resource.getPersistentProperty(REPOSITORY_LOCATION);
    }

    protected String loadUsername() throws CoreException {
        IResource resource = (IResource) getElement().getAdapter(IResource.class);
        return resource.getPersistentProperty(USER_NAME);
    }

    protected String loadUserPassword() throws CoreException {
        IResource resource = (IResource) getElement().getAdapter(IResource.class);
        return resource.getPersistentProperty(USER_PASSWORD);
    }

    protected Text getRepositoryLocationInput() {
        return repositoryLocation;
    }

    protected Text getUsernameInput() {
        return userName;
    }

    protected Text getUserPasswordInput() {
        return userPassword;
    }

    @Override
    public boolean performOk() {
        IResource resource = (IResource) getElement().getAdapter(IResource.class);

        try {
            resource.setPersistentProperty(USER_NAME, getUsernameInput().getText());
            resource.setPersistentProperty(USER_PASSWORD, getUserPasswordInput().getText());
        } catch (CoreException e) {
            e.printStackTrace();
        }

        return super.performOk();
    }
}
