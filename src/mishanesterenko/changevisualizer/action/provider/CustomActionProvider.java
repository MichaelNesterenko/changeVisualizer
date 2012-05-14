package mishanesterenko.changevisualizer.action.provider;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.NewProjectAction;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import mishanesterenko.changevisualizer.action.CustomProjectOpenAction;

public class CustomActionProvider extends CommonActionProvider {
    private CustomProjectOpenAction openAction;

    public CustomActionProvider() {
    }

    @Override
    public void init(ICommonActionExtensionSite aSite) {
        super.init(aSite);
        if (aSite.getViewSite() instanceof ICommonViewerWorkbenchSite) {
            ICommonViewerWorkbenchSite ws = (ICommonViewerWorkbenchSite) aSite.getViewSite();
            openAction = new CustomProjectOpenAction(ws.getSelectionProvider(), ws.getPage());
        }
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

    @Override
    public void fillActionBars(IActionBars actionBars) {
        super.fillActionBars(actionBars);
        if (openAction.isEnabled()) {
            actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openAction);
        }
    }

}
