package mishanesterenko.changevisualizer.activator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;


public class ChangeVisualizerPlugin extends AbstractUIPlugin {
    public static final String PLUGIN_ID = "changeVisualizer";

    private static ChangeVisualizerPlugin plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        DAVRepositoryFactory.setup();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static ChangeVisualizerPlugin getPlugin() {
        return plugin;
    }

}
