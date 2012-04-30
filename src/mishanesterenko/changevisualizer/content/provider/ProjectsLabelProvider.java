package mishanesterenko.changevisualizer.content.provider;

import mishanesterenko.changevisualizer.projectmodel.CustomProject;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class ProjectsLabelProvider extends LabelProvider {

    @Override
    public Image getImage(Object element) {
        if (element instanceof CustomProject) {
            IProject project = ((CustomProject) element).getProject();
            ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
            return project.isOpen() ? sharedImages.getImage(IDE.SharedImages.IMG_OBJ_PROJECT)
                    : sharedImages.getImage(IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED);
        }
        return null;
    }

    @Override
    public String getText(Object element) {
        if (element instanceof CustomProject) {
            return ((CustomProject) element).getProject().getName();
        }
        return "";
    }

}
