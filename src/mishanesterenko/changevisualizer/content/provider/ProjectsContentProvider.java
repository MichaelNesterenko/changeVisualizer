package mishanesterenko.changevisualizer.content.provider;

import java.util.ArrayList;
import java.util.List;

import mishanesterenko.changevisualizer.nature.Svn;
import mishanesterenko.changevisualizer.projectmodel.CustomProject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

public class ProjectsContentProvider implements ITreeContentProvider, IResourceChangeListener {

    public static final Object[] EMPTY_ARRAY = {};

    private Viewer _viewer;

    public ProjectsContentProvider() {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    }

    @Override
    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        _viewer = viewer;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IWorkspaceRoot) {
            IWorkspaceRoot root = (IWorkspaceRoot) parentElement;
            final IProject[] projects = root.getProjects();
            final List<CustomProject> filteredProjects = new ArrayList<CustomProject>();
            SafeRunner.run(new ISafeRunnable() {
                @Override
                public void run() throws Exception {
                    for (IProject project : projects) {
                        if (project.isOpen() && project.getNature(Svn.ID) != null) {
                            filteredProjects.add(new CustomProject(project));
                        }
                    }
                }

                @Override
                public void handleException(Throwable exception) {
                }
            });
            return filteredProjects.toArray();
        }
        return EMPTY_ARRAY;
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof IWorkspaceRoot) {
            return null;
        }
        if (element instanceof IProject) {
            return ((IProject) element).getWorkspace().getRoot();
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof IWorkspaceRoot) {
            return ((IWorkspaceRoot) element).getProjects().length > 0;
        }
        return false;
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                _viewer.refresh();
            }
        });
    }

}
