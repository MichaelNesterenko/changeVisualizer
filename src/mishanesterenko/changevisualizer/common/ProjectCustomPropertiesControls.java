package mishanesterenko.changevisualizer.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Michael Nesterenko
 *
 */
public class ProjectCustomPropertiesControls {
    private Text repositoryLocation;

    private Text userName;

    private Text userPassword;

    public Composite createContents(final Composite parent) {
        Composite myComposite = new Composite(parent, SWT.NONE);
        GridLayout mylayout = new GridLayout();
        mylayout.marginHeight = 1;
        mylayout.marginWidth = 1;
        myComposite.setLayout(mylayout);

        {  // repository location
            Label mylabel = new Label(myComposite, SWT.NONE);
            mylabel.setLayoutData(new GridData());
            mylabel.setText("Repository location");
            repositoryLocation = new Text(myComposite, SWT.BORDER);
            repositoryLocation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }

        { // separator
            Label mylabel = new Label(myComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
            mylabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }

        { // username
            Label mylabel = new Label(myComposite, SWT.NONE);
            mylabel.setLayoutData(new GridData());
            mylabel.setText("Username:");
            userName = new Text(myComposite, SWT.BORDER);
            userName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }

        { // password
            Label mylabel = new Label(myComposite, SWT.NONE);
            mylabel.setLayoutData(new GridData());
            mylabel.setText("Password:");
            userPassword = new Text(myComposite, SWT.BORDER);
            userPassword.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }

        return myComposite;
    }

    public Text getRepositoryLocation() {
        return repositoryLocation;
    }

    public Text getUserName() {
        return userName;
    }

    public Text getUserPassword() {
        return userPassword;
    }
}
