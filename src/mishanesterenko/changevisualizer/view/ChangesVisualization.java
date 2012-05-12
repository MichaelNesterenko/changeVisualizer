package mishanesterenko.changevisualizer.view;

import java.util.Hashtable;

import org.eclipse.draw2d.SWTEventDispatcher;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
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
import mishanesterenko.changevisualizer.activator.ChangeVisualizerPlugin;
import mishanesterenko.misc.GraphContentProvider;
import mishanesterenko.misc.GraphLabelProvider;
import mishanesterenko.misc.ModelProvider;
import org.eclipse.zest.core.widgets.Graph;

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
        ctx.registerService(new String[] { EventHandler.class.getName() }, new EventReceiver(), props);

        leftViewer = createGraphViewer(parent);
        rightViewer = createGraphViewer(parent);

        leftViewer.getZoomManager().addZoomListener(new ZoomChangedHandler());

        IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
        toolbarManager.add(new ZoomContributionViewItem(this));
    }

    protected CustomGraphViewer createGraphViewer(final Composite parent) {
        final CustomGraphViewer graphViewer = new CustomGraphViewer(parent, SWT.NONE);
        graphViewer.setNodeStyle(ZestStyles.NODES_NO_LAYOUT_RESIZE);
        graphViewer.getGraphControl().getVerticalBar().addSelectionListener(new GraphScrollHandler());
        graphViewer.getGraphControl().getHorizontalBar().addSelectionListener(new GraphScrollHandler());
        graphViewer.getGraphControl().setPreferredSize(10000, 1500);
        {
            GraphMouseHandler mouseHandler = new GraphMouseHandler();
            graphViewer.getGraphControl().addMouseMoveListener(mouseHandler);
            graphViewer.getGraphControl().addMouseListener(mouseHandler);
        }
        graphViewer.setContentProvider(new GraphContentProvider());
        graphViewer.setLabelProvider(new GraphLabelProvider());
        graphViewer.setInput(ModelProvider.getModel());
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

    protected class EventReceiver implements EventHandler {
        @Override
        public void handleEvent(final Event event) {
            System.out.println(event);
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
