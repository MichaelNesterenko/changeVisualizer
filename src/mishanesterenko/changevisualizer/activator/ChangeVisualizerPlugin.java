package mishanesterenko.changevisualizer.activator;
import org.eclipse.ui.plugin.AbstractUIPlugin;


public class ChangeVisualizerPlugin extends AbstractUIPlugin {

    private static ChangeVisualizerPlugin plugin;
    
    public ChangeVisualizerPlugin() {
        plugin = this;
    }

    public static ChangeVisualizerPlugin getPlugin() {
        return plugin;
    }

}
