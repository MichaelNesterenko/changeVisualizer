package mishanesterenko.changevisualizer.view;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.draw2d.SWTEventDispatcher;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.eclipse.zest.core.viewers.internal.ZoomListener;
import org.eclipse.zest.core.viewers.internal.ZoomManager;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import mishanesterenko.changevisualizer.activator.ChangeVisualizerPlugin;
import mishanesterenko.changevisualizer.common.Util;
import org.eclipse.zest.core.widgets.Graph;

import com.google.common.collect.BiMap;

import mishanesterenko.changevisualizer.vcsadapter.SvnAdapter;
import mishanesterenko.changevisualizer.matchingalgorithm.converter.ConvertingVisitor;
import mishanesterenko.changevisualizer.matchingalgorithm.domain.Node;
import mishanesterenko.changevisualizer.projectmodel.CustomProject;
import mishanesterenko.changevisualizer.content.provider.GraphContentProvider;
import mishanesterenko.changevisualizer.content.provider.GraphLabelProvider;
import mishanesterenko.changevisualizer.matchingalgorithm.TreeMatcher;
import mishanesterenko.changevisualizer.matchingalgorithm.TreeUtil;

@SuppressWarnings("restriction")
public class ChangesVisualization extends ViewPart implements IZoomableWorkbenchPart {
    private CustomGraphViewer leftViewer;

    private CustomGraphViewer rightViewer;

    public ChangesVisualization() {
    }

    @Override
    public void createPartControl(Composite parent) {
        Hashtable<String, Object> props = new Hashtable<String, Object>();
        props.put(EventConstants.EVENT_TOPIC, HistoryView.VISUALIZATOR_TOPIC);
        BundleContext ctx = ChangeVisualizerPlugin.getPlugin().getBundle().getBundleContext();
        ctx.registerService(new String[] { EventHandler.class.getName() }, new MessageHandler(), props);

        SashForm sf = new SashForm(parent, SWT.HORIZONTAL);
        leftViewer = createGraphViewer(sf, true);
        rightViewer = createGraphViewer(sf, false);

        leftViewer.getZoomManager().addZoomListener(new ZoomChangedHandler());
        leftViewer.getZoomManager().setZoomLevels(new double[] {0.1, 0.15, 0.20, 0.25, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6, 0.65, 0.7, 0.75, 1});

        IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
        toolbarManager.add(new ZoomContributionViewItem(this));
    }

    protected CustomGraphViewer createGraphViewer(final Composite parent, final boolean isLeftGraph) {
        final CustomGraphViewer graphViewer = new CustomGraphViewer(parent, SWT.NONE);
        graphViewer.setNodeStyle(ZestStyles.NODES_NO_LAYOUT_RESIZE);
        graphViewer.getGraphControl().getVerticalBar().addSelectionListener(new GraphScrollHandler());
        graphViewer.getGraphControl().getHorizontalBar().addSelectionListener(new GraphScrollHandler());
        {
            GraphMouseHandler mouseHandler = new GraphMouseHandler();
            graphViewer.getGraphControl().addMouseMoveListener(mouseHandler);
            graphViewer.getGraphControl().addMouseListener(mouseHandler);
        }
        graphViewer.setContentProvider(new GraphContentProvider());
        graphViewer.setLabelProvider(new GraphLabelProvider(isLeftGraph));
        return graphViewer;
    }

    @Override
    public void setFocus() {
        leftViewer.getControl().setFocus();
    }

    @Override
    public AbstractZoomableViewer getZoomableViewer() {
        return leftViewer;
    }

    protected ASTNode buildAst(final String contents, final IProgressMonitor monitor) {
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        @SuppressWarnings("unchecked")
        Map<String, Object> options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_6);
        parser.setCompilerOptions(options);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(contents.toCharArray());

        return parser.createAST(monitor);
    }

    protected Node convertAst(final ASTNode node, final IProgressMonitor monitor) {
        ConvertingVisitor converter = new ConvertingVisitor(monitor);
        node.accept(converter);
        return converter.getNode();
    }

    protected Set<Node> getNodeSet(final Node n) {
        Set<Node> nodes = new HashSet<Node>();
        nodes.add(n);
        for (Node child : n.getChildren()) {
            nodes.addAll(getNodeSet(child));
        }
        return nodes;
    }

    protected class MessageHandler implements EventHandler {
        @Override
        public void handleEvent(final Event event) {
            final SVNLogEntry logEntry = (SVNLogEntry) event.getProperty(HistoryView.LOG_ENTRY_KEY);
            final SVNLogEntryPath changedPath = (SVNLogEntryPath) event.getProperty(HistoryView.LOG_ENTRY_PATH_KEY);
            final CustomProject project = (CustomProject) event.getProperty(HistoryView.PROJECT_KEY);

            if (changedPath.getType() != SVNLogEntryPath.TYPE_MODIFIED
                    //|| changedPath.getKind() != SVNNodeKind.FILE
                    || !changedPath.getPath().endsWith("java")) {
                Display.getDefault().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        MessageBox mb = new MessageBox(getSite().getShell(), SWT.ICON_ERROR | SWT.OK);
                        mb.setText("Error");
                        mb.setMessage("Wrong type of file. Only modified java source code files are supported.");
                        mb.open();
                    }
                });
            } else {
                Display.getDefault().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(getSite().getShell());
                            progressDialog.run(false, true, new IRunnableWithProgress() {
                                @Override
                                public void run(final IProgressMonitor monitor)
                                        throws InvocationTargetException, InterruptedException {
                                    try {
                                        SubMonitor subMonitor = SubMonitor.convert(monitor, "Building tree from source code", 7);

                                        long previousRevision = logEntry.getRevision() - 1;
                                        long currentRevision = logEntry.getRevision();

                                        SvnAdapter adapter = new SvnAdapter(project);
                                        String previousContents = adapter.loadTextFile(changedPath.getPath(), previousRevision);
                                        subMonitor.worked(1);
                                        if (subMonitor.isCanceled()) {
                                            return;
                                        }
                                        String currentContents = adapter.loadTextFile(changedPath.getPath(), currentRevision);
                                        subMonitor.worked(1);
                                        if (subMonitor.isCanceled()) {
                                            return;
                                        }

                                        ASTNode astNodePrev = buildAst(previousContents, subMonitor.newChild(1));
                                        subMonitor.worked(1);
                                        if (subMonitor.isCanceled()) {
                                            return;
                                        }
                                        subMonitor.subTask("Building ast for " + currentRevision + " revision");
                                        ASTNode astNodeCurrent = buildAst(currentContents, subMonitor.newChild(1));
                                        if (subMonitor.isCanceled()) {
                                            return;
                                        }

                                        Node previousNode = convertAst(astNodePrev, subMonitor.newChild(1));
                                        if (subMonitor.isCanceled()) {
                                            return;
                                        }
                                        Node currentNode = convertAst(astNodeCurrent, subMonitor.newChild(1));
                                        if (subMonitor.isCanceled()) {
                                            return;
                                        }

                                        TreeMatcher m = new TreeMatcher(previousNode, currentNode);
                                        m.match(subMonitor.newChild(1));
                                        BiMap<Node, Node> matches = m.getMatches();

                                        Set<Node> leftNodes = getNodeSet(previousNode);
                                        Set<Node> rightNodes = getNodeSet(currentNode);

                                        { // viewer initialization
                                            GraphLabelProvider leftProvider = (GraphLabelProvider) leftViewer.getLabelProvider();
                                            GraphLabelProvider rightProvider = (GraphLabelProvider) rightViewer.getLabelProvider();

                                            leftProvider.setMatchedNodes(matches, leftNodes);
                                            rightProvider.setMatchedNodes(matches, rightNodes);

                                            final int heightPerNode = 100;
                                            final int widthPerNode = 250;
                                            leftViewer.getGraphControl().setPreferredSize(
                                                    widthPerNode * TreeUtil.computeBreadth(previousNode),
                                                    heightPerNode * TreeUtil.computeDepth(previousNode));
                                            rightViewer.getGraphControl().setPreferredSize(
                                                    widthPerNode * TreeUtil.computeBreadth(currentNode),
                                                    heightPerNode * TreeUtil.computeDepth(currentNode));
                                        }

                                        leftViewer.setInput(leftNodes);
                                        rightViewer.setInput(rightNodes);
                                    } catch (SVNException e) {
                                        throw new InvocationTargetException(e);
                                    } finally {
                                        monitor.done();
                                    }
                                }
                            });
                        } catch (InvocationTargetException e) {
                            Util.showError(e.getTargetException());
                        } catch (Exception e) {
                            Util.showError(e);
                        }
                    }
                });
            }
        }
    }

    protected class GraphScrollHandler extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            super.widgetSelected(e);
            updateScrollBars((ScrollBar) e.widget);
        }
    }

    protected void updateScrollBars(ScrollBar sb) {
        int selection = sb.getSelection();
        if (sb == ((Graph) leftViewer.getControl()).getHorizontalBar()) {
            rightViewer.getGraphControl().scrollToX(selection);
        } else if (sb == ((Graph) leftViewer.getControl()).getVerticalBar()) {
            rightViewer.getGraphControl().scrollToY(selection);
        } else if (sb == ((Graph) rightViewer.getControl()).getHorizontalBar()) {
            leftViewer.getGraphControl().scrollToX(selection);
        } else if (sb == ((Graph) rightViewer.getControl()).getVerticalBar()) {
            leftViewer.getGraphControl().scrollToY(selection);
        } else throw new IllegalStateException();
    }

    protected class ZoomChangedHandler implements ZoomListener {
        @Override
        public void zoomChanged(final double zoom) {
            rightViewer.getZoomManager().setZoom(zoom);
        }
    }

    protected class GraphMouseHandler extends MouseAdapter implements MouseMoveListener {
        private int lastX;

        private int lastY;

        private boolean mouseDown;

        @Override
        public void mouseMove(MouseEvent e) {
            if (mouseDown) {
                int dX = -(e.x - lastX);
                int dY = -(e.y - lastY);

                Graph g = ((Graph) e.widget);
                g.scrollTo(g.getHorizontalBar().getSelection() + dX, g.getVerticalBar().getSelection() + dY);
                updateScrollBars(g.getHorizontalBar());
                updateScrollBars(g.getVerticalBar());
            }
            lastX = e.x;
            lastY = e.y;
        }

        @Override
        public void mouseDown(MouseEvent e) {
            mouseDown = true;
        }

        @Override
        public void mouseUp(MouseEvent e) {
            mouseDown = false;
        }
        
    }

    protected class CustomGraphViewer extends GraphViewer {
        public CustomGraphViewer(Composite composite, int style) {
            super(composite, style);
            getGraphControl().getLightweightSystem().setEventDispatcher(
                    new SWTEventDispatcher() {
                        @Override
                        public void dispatchMouseMoved(final MouseEvent me) {
                        }
                    });
        }

        @Override
        public ZoomManager getZoomManager() {
            return super.getZoomManager();
        }
    }

}
