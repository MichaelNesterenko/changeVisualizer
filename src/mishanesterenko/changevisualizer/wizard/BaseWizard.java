package mishanesterenko.changevisualizer.wizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mishanesterenko.changevisualizer.wizard.page.ProjectBasicSettingsPage;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.statushandlers.IStatusAdapterConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * @author Michael Nesterenko
 *
 */
public abstract class BaseWizard extends Wizard implements INewWizard {

    private ProjectBasicSettingsPage _projectBasicSettingsPage;

    public BaseWizard() {
        setWindowTitle(getWizardTitle());
        setNeedsProgressMonitor(true);
    }

    /**
     * @return
     */
    protected abstract String getWizardTitle();

    @Override
    public void addPages() {
        super.addPages();
        _projectBasicSettingsPage = createBasicSettingsPage();

        addPage(_projectBasicSettingsPage);
    }

    protected abstract String[] getProjectNatures();

    protected ProjectBasicSettingsPage createBasicSettingsPage() {
        ProjectBasicSettingsPage page = new ProjectBasicSettingsPage(getWizardTitle());
        page.setTitle(getWizardTitle());
        return page;
    }

    protected void addProjectNatures(final IProjectDescription desc, final String[] newNatures) {
        if (newNatures.length > 0) {
            String[] oldNatures = desc.getNatureIds();
            List<String> natures = new ArrayList<String>(oldNatures.length + newNatures.length);
            natures.addAll(Arrays.asList(oldNatures));
            natures.addAll(Arrays.asList(newNatures));
            desc.setNatureIds(natures.toArray(new String[] {}));
        }
    }

    @Override
    public boolean performFinish() {
        final boolean result[] = {true};
        SafeRunner.run(new ISafeRunnable() {
            @Override
            public void run() throws Exception {
                final IProject project = getBasicSettingsPage().getProjectHandle();
                final IProjectDescription description = ResourcesPlugin.getWorkspace().
                        newProjectDescription(getBasicSettingsPage().getProjectName());
                IWizardPage pages[] = getPages();
                for (IWizardPage page: pages) { // filling project configuration
                    if (page instanceof IProjectSettingsUpdater) {
                        IProjectSettingsUpdater updater = (IProjectSettingsUpdater) page;
                        updater.preCreate(description);
                    }
                }
                addProjectNatures(description, getProjectNatures());
                for (IWizardPage page: pages) { // filling project configuration
                    if (page instanceof IProjectSettingsUpdater) {
                        IProjectSettingsUpdater updater = (IProjectSettingsUpdater) page;
                        updater.postCreate(project, description);
                    }
                }
            }
            
            @Override
            public void handleException(Throwable exception) {
                StatusAdapter statusAdapter = new StatusAdapter(((CoreException) exception).getStatus());
                statusAdapter.setProperty(IStatusAdapterConstants.TITLE_PROPERTY, getWizardTitle());
                StatusManager.getManager().handle(statusAdapter, StatusManager.BLOCK);
                result[0] = false;
            }
        });
        return result[0];
    }

    protected ProjectBasicSettingsPage getBasicSettingsPage() {
        return _projectBasicSettingsPage;
    }
}
