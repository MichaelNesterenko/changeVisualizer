package mishanesterenko.changevisualizer.propertypage;

import mishanesterenko.changevisualizer.common.ProjectCustomPropertiesControls;
import mishanesterenko.changevisualizer.projectmodel.CustomProject;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;

/**
 * @author Michael Nesterenko
 *
 */
public abstract class BasePropertyPage extends PropertyPage {
    private Text repositoryLocation;

    private Text userName;

    private Text userPassword;

    private CustomProject project;

    @Override
    protected Control createContents(Composite parent) {
        ProjectCustomPropertiesControls controlsSource = new ProjectCustomPropertiesControls();
        Composite tlc = controlsSource.createContents(parent);

        project = (CustomProject) getElement().getAdapter(CustomProject.class);

        repositoryLocation = controlsSource.getRepositoryLocation();
        userName = controlsSource.getUserName();
        userPassword = controlsSource.getUserPassword();

        {
            repositoryLocation.setText(project.getRepositoryLocation());
            userName.setText(project.getUsername());
            userPassword.setText(project.getUserPassword());
        }

        {
            repositoryLocation.setEnabled(false);
        }

        return tlc;
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
        try {
            project.setUsername(getUsernameInput().getText());
            project.setUserPassword(getUserPasswordInput().getText());
            project.flushProperties();
        } catch (BackingStoreException e) {
            e.printStackTrace(); // TODO fix it
        }

        return super.performOk();
    }
}
