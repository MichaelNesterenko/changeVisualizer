package mishanesterenko.changevisualizer.activator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


public class ChangeVisualizerPlugin extends AbstractUIPlugin {

    private static ChangeVisualizerPlugin plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
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
