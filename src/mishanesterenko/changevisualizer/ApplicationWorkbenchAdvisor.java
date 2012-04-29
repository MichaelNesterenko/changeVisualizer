package mishanesterenko.changevisualizer;

import java.net.URL;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.ide.IDE;
import org.osgi.framework.Bundle;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

    private static final String PERSPECTIVE_ID = "changeVisualizer.perspective";

    @Override
    public void initialize(IWorkbenchConfigurer configurer) {
        super.initialize(configurer);
        IDE.registerAdapters();

        final String PATH_OBJECT = "icons/full/obj16/"; //$NON-NLS-1$
        Bundle ideBundle = Platform.getBundle("org.eclipse.ui.ide"); //$NON-NLS-1$
        declareWorkbenchImage(configurer, ideBundle, IDE.SharedImages.IMG_OBJ_PROJECT,
                PATH_OBJECT + "prj_obj.gif", true);
        declareWorkbenchImage(configurer, ideBundle, IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED,
                PATH_OBJECT + "cprj_obj.gif", true);
    }

    private void declareWorkbenchImage(IWorkbenchConfigurer configurer_p, Bundle ideBundle, String symbolicName,
            String path, boolean shared) {
        URL url = ideBundle.getEntry(path);
        ImageDescriptor desc = ImageDescriptor.createFromURL(url);
        configurer_p.declareImage(symbolicName, desc, shared);
    }

    @Override
    public IAdaptable getDefaultPageInput() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }

    @Override
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
            IWorkbenchWindowConfigurer configurer) {
        return new ApplicationWorkbenchWindowAdvisor(configurer);
    }

    @Override
    public String getInitialWindowPerspectiveId() {
        return PERSPECTIVE_ID;
    }

}
