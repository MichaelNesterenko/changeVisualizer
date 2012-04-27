package changevisualizer.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

public class SvnWizard extends Wizard implements INewWizard {

    private WizardNewProjectCreationPage _pageOne;

    public SvnWizard() {
        setWindowTitle("Wizard title");
    }

    @Override
    public void addPages() {
        super.addPages();
        _pageOne = new WizardNewProjectCreationPage("Constructor String");
        _pageOne.setTitle("Page title");
        _pageOne.setDescription("Page description");

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
