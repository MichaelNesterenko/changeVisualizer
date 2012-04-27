package mishanesterenko.changevisualizer.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

public class SvnWizard extends Wizard implements INewWizard {

    /**
     * 
     */
    private static final String PAGE_NAME = "Svn Wizard"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String WIZARD_TITLE = "New SVN project"; //$NON-NLS-1$

    private WizardNewProjectCreationPage _pageOne;

    public SvnWizard() {
        setWindowTitle(WIZARD_TITLE);
    }

    @Override
    public void addPages() {
        super.addPages();
        _pageOne = new WizardNewProjectCreationPage(PAGE_NAME);
        _pageOne.setTitle(WizardMessages.SvnWizard_location_page_title);
        _pageOne.setDescription(WizardMessages.SvnWizard_location_page_description);

        addPage(_pageOne);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean performFinish() {
        // TODO Auto-generated method stub
        return true;
    }

}
