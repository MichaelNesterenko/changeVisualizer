package mishanesterenko.changevisualizer.view;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import mishanesterenko.changevisualizer.activator.ChangeVisualizerPlugin;
import mishanesterenko.changevisualizer.projectmodel.CustomProject;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.layout.GridData;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

public class HistoryView extends ViewPart {
    public static final String ID = "changeVisualizer.view.history";

    private Color errorColor;

    private TableViewer commitViewer;

    private Image searchTypeImage;

    private boolean messagesIncluded = true;
    private boolean pathsIncluded = true;
    private boolean regexEnabled = true;

    private int showCommitCount = 100;

    private String searchText;

    private Text searchTextInput;

    private Button goButton;

    private int errorCount;

    private SearchTextInputValidator searchTextValidator;

    private ISelectionListener selectionHandler;

    private CustomProject currentProject;

    private Text showCommitCountInput;

    public HistoryView() {
        ImageDescriptor id = AbstractUIPlugin.imageDescriptorFromPlugin(ChangeVisualizerPlugin.PLUGIN_ID, "icons/searchtype.png");
        searchTypeImage = id.createImage();
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

        getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(selectionHandler = new SelectionHandler());
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
                searchTextValidator.validate();
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
            goButton.addSelectionListener(new GoButtonClickHandler());
            goButton.setText("Go");
            goButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
        }
    }

    protected void createHistoryTable(final Composite parent) {
        commitViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        Table table = commitViewer.getTable();
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

//        Listener paintListener = new Listener() {
//            @Override
//            public void handleEvent(Event event) {
//                switch (event.type) {
//                case SWT.MeasureItem: {
//                    TableItem item = (TableItem) event.item;
//                    String text = item.getText(event.index).replace("\n", "\n|");//getText(item, event.index);
//                    Point size = event.gc.textExtent(text);
//                    event.width = size.x;
//                    event.height = Math.max(event.height, size.y);
//                    break;
//                }
//                case SWT.PaintItem: {
//                    TableItem item = (TableItem) event.item;
//                    String text = item.getText(event.index).replace("\n", "\n|");//getText(item, event.index);
//                    Point size = event.gc.textExtent(text);
//                    int offset2 = event.index == 0 ? Math.max(0, (event.height - size.y) / 2) : 0;
//                    event.gc.drawText(text, event.x, event.y + offset2, true);
//                    break;
//                }
//                case SWT.EraseItem: {
//                    event.detail &= ~SWT.FOREGROUND;
//                    break;
//                }
//                }
//            }
//        };
//
//        table.addListener(SWT.MeasureItem, paintListener);
//        table.addListener(SWT.PaintItem, paintListener);
//        table.addListener(SWT.EraseItem, paintListener);

        commitViewer.setContentProvider(ArrayContentProvider.getInstance());

        TableViewerColumn column = createTableViewerColumn("Revision", 100, 0);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                SVNLogEntry logEntry = (SVNLogEntry) element;
                return Long.toString(logEntry.getRevision());
            }
        });

        column = createTableViewerColumn("Author", 100, 1);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                SVNLogEntry logEntry = (SVNLogEntry) element;
                return logEntry.getAuthor();
            }
        });

        column = createTableViewerColumn("Message", 500, 2);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                SVNLogEntry logEntry = (SVNLogEntry) element;
                return logEntry.getMessage().replace("\n", " ");
            }
        });
    }

    protected TableViewerColumn createTableViewerColumn(final String title, final int bound, final int colNumber) {
        final TableViewerColumn viewerColumn = new TableViewerColumn(commitViewer, SWT.NONE);
        final TableColumn column = viewerColumn.getColumn();
        column.setText(title);
        column.setWidth(bound);
        column.setResizable(true);
        column.setMoveable(true);
        return viewerColumn;
    }

    protected void createBottomPanel(final Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout(2, false));
        panel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        {
            Label l = new Label(panel, SWT.NONE);
            l.setText("Show commit count:");
            l.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

            showCommitCountInput = new Text(panel, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
            GridData gd_showCommitCountInput = new GridData(SWT.LEFT, SWT.CENTER, true, false);
            gd_showCommitCountInput.minimumWidth = 50;
            showCommitCountInput.setLayoutData(gd_showCommitCountInput);
            showCommitCountInput.setText(Integer.toString(showCommitCount));
            showCommitCountInput.addModifyListener(new CommitCountValidator());
        }
    }

    /**
     * Passing the focus request to the commitViewer's control.
     */
    @Override
    public void setFocus() {
        commitViewer.getControl().setFocus();
    }

    @Override
    public void dispose() {
        searchTypeImage.dispose();
        getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(selectionHandler);
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

    protected void performUpdateCommits() {
        try {
            updateCommits();
        } catch (SVNException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    protected void updateCommits() throws SVNException {
        SVNRepository repository = SVNRepositoryFactory.create(SVNURL.parseURIDecoded(currentProject.getRepositoryLocation()));
        try {
            ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(currentProject.getUsername(),
                    currentProject.getUserPassword());
            repository.setAuthenticationManager(authManager);

            Collection<SVNLogEntry> logEntries = repository.log(new String[] {""}, null, -1, -1, false, true);
            Iterator<SVNLogEntry> it = logEntries.iterator();
            SVNLogEntry logEntry = null;
            while (it.hasNext()) {
                logEntry = it.next();
                break;
            }
            if (logEntry != null) { 
                long fromRevision = logEntry.getRevision() - showCommitCount;
                logEntries = repository.log(new String[] {""}, null, fromRevision, -1, true, true);
                while (fromRevision > 1 && logEntries.size() < showCommitCount) {
                    long probableRevision = fromRevision - (showCommitCount - logEntries.size());
                    Collection<SVNLogEntry> moreLogEntries =
                            repository.log(new String[] {""}, null, probableRevision, fromRevision - 1, true, true);
                    logEntries.addAll(moreLogEntries);
                    fromRevision = probableRevision;
                }
                SVNLogEntry[] logEntryArray = logEntries.toArray(new SVNLogEntry[] {});
                commitViewer.setInput(logEntryArray);
            } else {
                commitViewer.setInput(new SVNLogEntry[] {});
            }
        } finally {
            repository.closeSession();
        }
    }

    protected class MenuCheckAction extends Action {
        public MenuCheckAction(final String text, final boolean checked) {
            super(text, IAction.AS_CHECK_BOX);
            setChecked(checked);
        }
    }

    protected class SearchTextInputValidator implements ModifyListener {
        private boolean wasValid = true;

        public void validate() {
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
            validate();
        }
    }

    protected class CommitCountValidator implements ModifyListener {
        private boolean wasValid = true;

        public void validate() {
            try {
                if (Integer.parseInt(showCommitCountInput.getText()) <= 0) {
                    throw new NumberFormatException();
                }
                if(!wasValid) {
                    wasValid = true;
                    decreaseErrorCount();
                    showCommitCountInput.setBackground(null);
                }
            } catch (NumberFormatException ee) {
                if (wasValid) {
                    wasValid = false;
                    increaseErrorCount();
                    showCommitCountInput.setBackground(errorColor);
                }
            }
        }

        @Override
        public void modifyText(ModifyEvent e) {
            validate();
        }
    }

    protected class GoButtonClickHandler extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            MessageBox mb = new MessageBox(getSite().getShell(), SWT.OK);
            mb.setMessage(searchText + "; messages: " + messagesIncluded + ", paths: " + pathsIncluded + ", regex: "
                    + regexEnabled);
            mb.open();
        }
    }

    protected class SelectionHandler implements ISelectionListener {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            if (selection instanceof IStructuredSelection) {
                IStructuredSelection structuredSelection = (IStructuredSelection) selection;
                if (structuredSelection.getFirstElement() instanceof CustomProject
                        && currentProject !=null && structuredSelection.getFirstElement() != currentProject) {
                    currentProject = (CustomProject) structuredSelection.getFirstElement();
                    performUpdateCommits();
                }
            }
        }
    }

}