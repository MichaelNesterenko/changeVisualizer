package mishanesterenko.changevisualizer.view;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import mishanesterenko.changevisualizer.activator.ChangeVisualizerPlugin;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.actions.ModifyWorkingSetDelegate;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.layout.GridData;

public class HistoryView extends ViewPart {
    /**
     * 
     */
    private static final int DAYS_IN_PAST = -10;

    public static final String ID = "changeVisualizer.view.history";

    private Color errorColor;

    private TableViewer viewer;

    private Image searchTypeImage;

    private DateTime dateFromInput;

    private DateTime dateToInput;

    private boolean messagesIncluded = true;
    private boolean pathsIncluded = true;
    private boolean regexEnabled = true;

    private Date dateFrom;
    private Date dateTo;

    private String searchText;

    private Text searchTextInput;

    private Button goButton;

    private int errorCount;

    private SearchTextInputValidator searchTextValidator;

    public HistoryView() {
        ImageDescriptor id = AbstractUIPlugin.imageDescriptorFromPlugin(ChangeVisualizerPlugin.PLUGIN_ID, "icons/searchtype.png");
        searchTypeImage = id.createImage();
    }

    /**
     * The content provider class is responsible for providing objects to the
     * view. It can wrap existing objects in adapters or simply return objects
     * as-is. These objects may be sensitive to the current input of the view,
     * or ignore it and always show the same content (like Task List, for
     * example).
     */
    class ViewContentProvider implements IStructuredContentProvider {
        @Override
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public Object[] getElements(Object parent) {
            if (parent instanceof Object[]) {
                return (Object[]) parent;
            }
            return new Object[0];
        }
    }

    class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(Object obj, int index) {
            return getText(obj);
        }

        @Override
        public Image getColumnImage(Object obj, int index) {
            return getImage(obj);
        }

        @Override
        public Image getImage(Object obj) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
        }
    }

    @Override
    public void createPartControl(Composite parent) {
        errorColor = getSite().getShell().getDisplay().getSystemColor(SWT.COLOR_RED);

        GridLayout gl_parent = new GridLayout(1, false);
        gl_parent.verticalSpacing = 0;
        gl_parent.horizontalSpacing = 0;
        gl_parent.marginHeight = 0;
        gl_parent.marginWidth = 0;
        parent.setLayout(gl_parent);

        createTopPanel(parent);
        createHistoryTable(parent);
        createBottomPanel(parent);
    }

    private MenuManager createSearchTypeMenu() {
        MenuManager mm = new MenuManager();
        mm.add(new MenuCheckAction("Messages", messagesIncluded) {
            @Override
            public void run() {
                super.run();
                messagesIncluded = isChecked();
            }
        });
        mm.add(new MenuCheckAction("Paths", pathsIncluded) {
            @Override
            public void run() {
                super.run();
                pathsIncluded = isChecked();
            }
        });
        mm.add(new Separator());
        mm.add(new MenuCheckAction("Regex", regexEnabled) {
            @Override
            public void run() {
                super.run();
                regexEnabled = isChecked();
                searchTextValidator.validateSearchText();
            }
        });
        return mm;
    }

    protected void createTopPanel(final Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout(3, false));
        panel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        { // image with a guy
            Label image = new Label(panel, SWT.NONE);
            image.setImage(searchTypeImage);
            image.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
            image.setMenu(createSearchTypeMenu().createContextMenu(image));
        }

        { // search input
            searchTextInput = new Text(panel, SWT.BORDER);
            searchTextInput.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
            searchTextInput.addModifyListener(searchTextValidator = new SearchTextInputValidator() {
                @Override
                public void modifyText(ModifyEvent e) {
                    super.modifyText(e);
                    searchText = searchTextInput.getText();
                }
            });
        }

        { // go button
            goButton = new Button(panel, SWT.PUSH);
            goButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    MessageBox mb = new MessageBox(getSite().getShell(), SWT.OK);
                    mb.setMessage(searchText + "; messages: " + messagesIncluded + ", paths: " + pathsIncluded + ", regex: "
                            + regexEnabled + "\n from: " + dateFrom + ", to: " + dateTo);
                    mb.open();
                }
            });
            goButton.setText("Go");
            goButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
        }
    }

    protected void createHistoryTable(final Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        Table table = viewer.getTable();
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());
        // Provide the input to the ContentProvider
        viewer.setInput(new String[] {"One", "Two", "Three"});
    }

    protected void createBottomPanel(final Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout(5, false));
        panel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        {
            Label l = new Label(panel, SWT.NONE);
            l.setText("From:");
            l.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

            Composite controlParent = new Composite(panel, SWT.NONE);
            FillLayout fillLayout = new FillLayout();
            fillLayout.marginHeight = 5;
            fillLayout.marginWidth = 5;
            controlParent.setLayout(fillLayout);
            controlParent.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
            dateFromInput = new DateTime(controlParent, SWT.BORDER | SWT.DATE | SWT.DROP_DOWN);

            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_MONTH, DAYS_IN_PAST);
            dateFromInput.setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        }

        {
            Label l = new Label(panel, SWT.NONE);
            l.setText("To:");
            l.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

            Composite parentControl = new Composite(panel, SWT.NONE);
            FillLayout fillLayout = new FillLayout();
            fillLayout.marginHeight = 5;
            fillLayout.marginWidth = 5;
            parentControl.setLayout(fillLayout);
            parentControl.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
            dateToInput = new DateTime(parentControl, SWT.BORDER | SWT.DATE | SWT.DROP_DOWN);
        }

        {
            DateInputListener dateListener = new DateInputListener(dateFromInput, dateToInput);
            new Label(panel, SWT.NONE);
            dateFromInput.addSelectionListener(dateListener);
            dateToInput.addSelectionListener(dateListener);
        }
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    @Override
    public void dispose() {
        searchTypeImage.dispose();
        super.dispose();
    }

    protected void updateValidStatus() {
        goButton.setEnabled(isValid());
    }

    protected boolean isValid() {
        return errorCount == 0;
    }

    protected void increaseErrorCount() {
        errorCount++;
        updateValidStatus();
    }

    protected void decreaseErrorCount() {
        errorCount--;
        errorCount = errorCount < 0 ? 0 : errorCount;
        updateValidStatus();
    }

    protected class MenuCheckAction extends Action {
        public MenuCheckAction(final String text, final boolean checked) {
            super(text, IAction.AS_CHECK_BOX);
            setChecked(checked);
        }
    }

    protected class SearchTextInputValidator implements ModifyListener {
        private boolean wasValid = true;

        public void validateSearchText() {
            if (regexEnabled) {
                try {
                    Pattern.compile(searchTextInput.getText());
                    if(!wasValid) {
                        wasValid = true;
                        decreaseErrorCount();
                        searchTextInput.setBackground(null);
                    }
                } catch (PatternSyntaxException ee) {
                    if (wasValid) {
                        wasValid = false;
                        increaseErrorCount();
                        searchTextInput.setBackground(errorColor);
                    }
                }
            } else if (!wasValid) {
                wasValid = true;
                decreaseErrorCount();
                searchTextInput.setBackground(null);
            }
        }

        @Override
        public void modifyText(ModifyEvent e) {
            validateSearchText();
        }
    }

    protected class DateInputListener extends SelectionAdapter {
        private boolean wasValid = true;

        private DateTime fromInput;

        private DateTime toInput;

        public DateInputListener(final DateTime from, final DateTime to) {
            fromInput = from;
            toInput = to;
        }

        public void validate() {
            Date companionDate = getDateFromDateTimeControl(fromInput);
            Date myDate = getDateFromDateTimeControl(toInput);
            if (myDate.compareTo(companionDate) < 0) {
                if (wasValid) {
                    wasValid = false;
                    fromInput.getParent().setBackground(errorColor);
                    toInput.getParent().setBackground(errorColor);
                    increaseErrorCount();
                }
            } else if (!wasValid) {
                wasValid = true;
                fromInput.getParent().setBackground(null);
                toInput.getParent().setBackground(null);
                decreaseErrorCount();
            }
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            super.widgetSelected(e);
            validate();
            if (dateFromInput == e.getSource()) {
                dateFrom = getDateFromDateTimeControl(dateFromInput);
            } else if (dateToInput == e.getSource()) {
                dateTo = getDateFromDateTimeControl(dateToInput);
            } else {
                throw new IllegalStateException("Wrong date input contro");
            }
        }
    }

    //protected class GoButtonClickHandler extends

    public static Date getDateFromDateTimeControl(final DateTime control) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, control.getYear());
        c.set(Calendar.MONTH, control.getMonth());
        c.set(Calendar.DAY_OF_MONTH, control.getDay());
        return c.getTime();
    }
}