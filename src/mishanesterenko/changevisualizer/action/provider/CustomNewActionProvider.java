package mishanesterenko.changevisualizer.action.provider;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.actions.NewProjectAction;
import org.eclipse.ui.navigator.CommonActionProvider;

public class CustomNewActionProvider extends CommonActionProvider {

    public CustomNewActionProvider() {
    }

    @Override
    public void fillContextMenu(IMenuManager menu) {
        super.fillContextMenu(menu);
        IContributionItem[] items = menu.getItems();
        for (IContributionItem item : items) {
            if ("common.new.menu".equals(item.getId())) {
                IMenuManager newMenu = ((IMenuManager) item);
                IContributionItem[] newItems = newMenu.getItems();
                for (IContributionItem newItem : newItems) {
                    if (newItem instanceof ActionContributionItem) {
                        if (((ActionContributionItem) newItem).getAction() instanceof NewProjectAction) {
                            newMenu.remove(newItem);
                            return;
                        }
                    }
                }
            }
        }
    }

}
