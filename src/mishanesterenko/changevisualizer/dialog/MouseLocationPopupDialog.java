package mishanesterenko.changevisualizer.dialog;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Michael Nesterenko
 *
 */
public class MouseLocationPopupDialog extends PopupDialog {
    private final static int SHELL_STYLE = PopupDialog.INFOPOPUPRESIZE_SHELLSTYLE;

    public MouseLocationPopupDialog(Shell parent, String infoText) {
        this(parent, SHELL_STYLE, true, false, false, false, false, null, infoText);
    }

    public MouseLocationPopupDialog(Shell parent, String titleText, String infoText) {
        this(parent, SHELL_STYLE, true, false, false, false, false, titleText, infoText);
    }

    public MouseLocationPopupDialog(Shell parent, String infoText, final Point size) {
        this(parent, infoText);
        getShell().setSize(size);
    }

    public MouseLocationPopupDialog(Shell parent, int shellStyle, boolean takeFocusOnOpen, boolean persistSize, boolean persistLocation, boolean showDialogMenu, boolean showPersistActions, String titleText, String infoText) {
        super(parent, shellStyle, takeFocusOnOpen, persistSize, persistLocation, showDialogMenu, showPersistActions, titleText, infoText);
    }

    @Override
    protected void adjustBounds() {
        super.adjustBounds();
        Display d = Display.getCurrent();
        if (d == null) {
            d = Display.getDefault();
        }
        Point point = d.getCursorLocation();
        getShell().setLocation(point.x + 9, point.y + 14);
    }

    @Override
    protected Control createInfoTextArea(final Composite parent) {
        Label infoLabel = (Label) super.createInfoTextArea(parent);
        FontData[] datas = infoLabel.getFont().getFontData();
        for (FontData data : datas) {
            data.setHeight(15);
        }
        infoLabel.setFont(new Font(getShell().getDisplay(), datas));
        return infoLabel;
    }
}

