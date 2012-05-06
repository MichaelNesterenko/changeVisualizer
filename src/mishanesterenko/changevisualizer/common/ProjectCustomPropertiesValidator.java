package mishanesterenko.changevisualizer.common;

import java.io.File;

public class ProjectCustomPropertiesValidator {

    public boolean validate(final String repositoryLocation) {
        return true;//new File(repositoryLocation).isDirectory();
    }
}