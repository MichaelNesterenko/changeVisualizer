package mishanesterenko.changevisualizer.adapter;

import java.util.Arrays;

import mishanesterenko.changevisualizer.projectmodel.CustomProject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 * @author Michael Nesterenko
 *
 */
public class CustomProjectAdapterFactory implements IAdapterFactory {
    private static final Class[] adapters = {IAdaptable.class, IResource.class, IProject.class};
    @Override
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adaptableObject instanceof CustomProject && Arrays.asList(adapters).contains(adapterType)) {
            return ((CustomProject) adaptableObject).getProject();
        }
        return null;
    }

    @Override
    public Class[] getAdapterList() {
        return adapters;
    }

}
