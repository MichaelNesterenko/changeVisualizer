package mishanesterenko.changevisualizer.wizard.page;

import java.lang.reflect.InvocationTargetException;
import mishanesterenko.changevisualizer.common.ProjectCustomPropertiesValidator;
import mishanesterenko.changevisualizer.common.ProjectCustomPropertiesControls;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import mishanesterenko.changevisualizer.activator.ChangeVisualizerPlugin;
import mishanesterenko.changevisualizer.projectmodel.CustomProject;
import mishanesterenko.changevisualizer.wizard.IProjectSettingsUpdater;
import mishanesterenko.changevisualizer.wizard.WizardMessages;

/**
 * @author Michael Nesterenko
 *
 */
public class ProjectBasicSettingsPage extends WizardNewProjectCreationPage implements IProjectSettingsUpdater {
    private Text repositoryLocationInput;
    private Text usernameInput;
    private Text userPasswordInput;

    private Listener validateModifyListener = new Listener() {
        @Override
        public void handleEvent(Event event) {
            setPageComplete(validatePage());
        }
        
    };

    public ProjectBasicSettingsPage(final String pageName) {
        super(pageName);
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        ProjectCustomPropertiesControls customControlSource = new ProjectCustomPropertiesControls();
        Composite controls = customControlSource.createContents((Composite) getControl());
        controls.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        repositoryLocationInput = customControlSource.getRepositoryLocation();
        usernameInput = customControlSource.getUserName();
        userPasswordInput = customControlSource.getUserPassword();

        repositoryLocationInput.addListener(SWT.Modify, validateModifyListener);
    }

    @Override
    public void preCreate(IProjectDescription description) throws CoreException {
        description.setLocation(useDefaults() ? null : getLocationPath());
    }

    @Override
    public void postCreate(final IProject project, final IProjectDescription description) throws CoreException {
        try {
            final String repositoryLocation = repositoryLocationInput.getText();
            final String username = usernameInput.getText();
            final String userPassword = userPasswordInput.getText();
            getContainer().run(true, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        project.create(description, monitor);
                        if (!project.isOpen()) {
                            project.open(monitor);
                        }
                        CustomProject customProject = new CustomProject(project);
                        customProject.setRepositoryLocation(repositoryLocation);
                        customProject.setUsername(username);
                        customProject.setUserPassword(userPassword);
                        customProject.flushProperties();
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
                    ChangeVisualizerPlugin.PLUGIN_ID,
                    NLS.bind(WizardMessages.ProjectBasicSettingsPage_can_not_create_project, project.getName()),
                    cause);
            throw new CoreException(status);
        }
    }

    @Override
    protected boolean validatePage() {
        boolean superValid = super.validatePage();
        if (superValid && !(new ProjectCustomPropertiesValidator().validate(repositoryLocationInput.getText()))) {
            setErrorMessage("Can not find repository at the specified location");
            return false;
        }

        if (superValid) {
            setMessage(null);
            setErrorMessage(null);
        }

        return superValid;
    }

}
