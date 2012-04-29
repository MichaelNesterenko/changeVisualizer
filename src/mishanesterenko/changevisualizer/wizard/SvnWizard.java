package mishanesterenko.changevisualizer.wizard;

import mishanesterenko.changevisualizer.nature.Svn;
import mishanesterenko.changevisualizer.wizard.page.ProjectBasicSettingsPage;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;

public class SvnWizard extends BaseWizard {
    /**
     * 
     */
    private static final String WIZARD_TITLE = "New SVN project"; //$NON-NLS-1$

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        // TODO Auto-generated method stub

    }

    @Override
    protected String getWizardTitle() {
        return WIZARD_TITLE;
    }

    @Override
    protected String[] getProjectNatures() {
        return new String[] {Svn.ID};
    }

    @Override
    protected ProjectBasicSettingsPage createBasicSettingsPage() {
        ProjectBasicSettingsPage page = super.createBasicSettingsPage();
        page.setTitle(WizardMessages.SvnWizard_location_page_title);
        page.setDescription(WizardMessages.SvnWizard_location_page_description);
        return page;
    }

}
