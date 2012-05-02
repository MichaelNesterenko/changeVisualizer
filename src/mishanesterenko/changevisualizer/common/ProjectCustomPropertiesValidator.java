package mishanesterenko.changevisualizer.common;

import org.eclipse.swt.widgets.Text;

class ProjectCustomPropertiesValidator {
    private Text repositoryLocation;
    private Text userName;
    private Text userPassword;

    public ProjectCustomPropertiesValidator(final Text repositoryLocation, final Text userName, final Text userPassword) {
        this.repositoryLocation = repositoryLocation;
        this.userName = userName;
        this.userPassword = userPassword;
    }

    public boolean validate() {
        return !repositoryLocation.getText().isEmpty();
    }
}