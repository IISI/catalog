package tw.com.citi.catalog.views;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tw.com.citi.catalog.Activator;
import tw.com.citi.catalog.dao.IAppFunctionDao;
import tw.com.citi.catalog.dao.IUserDao;
import tw.com.citi.catalog.model.AppFunction;
import tw.com.citi.catalog.model.User;
import tw.com.iisi.rabbithq.editors.BrowserEditorInput;
import tw.com.iisi.rabbithq.editors.MozillaEditor;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class MenuView extends ViewPart {

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "tw.com.citi.catalog.views.MenuView";

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private TreeViewer viewer;
    private DrillDownAdapter drillDownAdapter;
    private Action action1;
    private Action action2;
    private Action singleClickAction;

    /*
     * The content provider class is responsible for providing objects to the
     * view. It can wrap existing objects in adapters or simply return objects
     * as-is. These objects may be sensitive to the current input of the view,
     * or ignore it and always show the same content (like Task List, for
     * example).
     */

    class TreeObject implements IAdaptable {

        private String id;
        private String name;
        private String url;
        private TreeParent parent;

        public TreeObject(String name) {
            this.name = name;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setParent(TreeParent parent) {
            this.parent = parent;
        }

        public TreeParent getParent() {
            return parent;
        }

        public String toString() {
            return getName();
        }

        public Object getAdapter(Class key) {
            return null;
        }
    }

    class TreeParent extends TreeObject {
        private ArrayList children;

        public TreeParent(String name) {
            super(name);
            children = new ArrayList();
        }

        public void addChild(TreeObject child) {
            children.add(child);
            child.setParent(this);
        }

        public void removeChild(TreeObject child) {
            children.remove(child);
            child.setParent(null);
        }

        public TreeObject[] getChildren() {
            return (TreeObject[]) children.toArray(new TreeObject[children.size()]);
        }

        public boolean hasChildren() {
            return children.size() > 0;
        }
    }

    class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
        private TreeParent invisibleRoot;

        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        public void dispose() {
        }

        public Object[] getElements(Object parent) {
            if (parent.equals(getViewSite())) {
                if (invisibleRoot == null)
                    initialize();
                return getChildren(invisibleRoot);
            }
            return getChildren(parent);
        }

        public Object getParent(Object child) {
            if (child instanceof TreeObject) {
                return ((TreeObject) child).getParent();
            }
            return null;
        }

        public Object[] getChildren(Object parent) {
            if (parent instanceof TreeParent) {
                return ((TreeParent) parent).getChildren();
            }
            return new Object[0];
        }

        public boolean hasChildren(Object parent) {
            if (parent instanceof TreeParent)
                return ((TreeParent) parent).hasChildren();
            return false;
        }

        /*
         * We will set up a dummy model to initialize tree heararchy. In a real
         * code, you will connect to a real model and expose its hierarchy.
         */
        private void initialize() {

            URL config = Platform.getBundle(Activator.PLUGIN_ID).getResource("/configs/tree.json");
            JsonArray jsonArr = null;
            try {
                InputStreamReader reader = new InputStreamReader(config.openStream(), "UTF-8");
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(JsonArray.class, new CustomerDeserializer());
                jsonArr = gsonBuilder.create().fromJson(reader, JsonArray.class);
                // logger.debug("&&& json size="+jsonArr.get(0).getAsJsonObject().get("nodes"));

            } catch (IOException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < jsonArr.size(); i++) {
                JsonObject rootObj = jsonArr.get(i).getAsJsonObject();
                TreeObject parent = getFilterNodes(rootObj);
                invisibleRoot = new TreeParent("");
                invisibleRoot.addChild(parent);

            }

            IWorkbenchPage page = MenuView.this.getViewSite().getPage();
            BrowserEditorInput editorInput = new BrowserEditorInput("http://localhost:8088/catalog/app/JCS5000");
            try {
                page.openEditor(editorInput, MozillaEditor.ID);
            } catch (PartInitException e) {
                e.printStackTrace();
            }
        }

        private TreeObject getFilterNodes(JsonObject rootObj) {
            if (rootObj.get("nodes") != null && rootObj.get("nodes").isJsonArray()) {
                TreeParent treeParent = new TreeParent(rootObj.get("text").getAsString());
                JsonArray nodes = rootObj.get("nodes").getAsJsonArray();
                for (int i = 0; i < nodes.size(); i++) {
                    JsonObject jsonObj = nodes.get(i).getAsJsonObject();
                    TreeObject nodeObj = getFilterNodes(jsonObj);
                    treeParent.addChild(nodeObj);
                }
                return treeParent;

            } else if (rootObj.get("nodes") == null) {
                TreeObject treeObject = new TreeObject(rootObj.get("text").getAsString());
                if (rootObj.get("url") != null) {
                    treeObject.setUrl(rootObj.get("url").getAsString());
                }
                return treeObject;
            } else {
                return null;
            }

        }

    }

    class ViewLabelProvider extends LabelProvider {

        public String getText(Object obj) {
            return obj.toString();
        }

        public Image getImage(Object obj) {
            String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
            if (obj instanceof TreeParent)
                imageKey = ISharedImages.IMG_OBJ_FOLDER;
            return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
        }
    }

    /**
     * The constructor.
     */
    public MenuView() {
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    public void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        drillDownAdapter = new DrillDownAdapter(viewer);
        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());
        viewer.setInput(getViewSite());
        viewer.expandAll();

        makeActions();
        hookContextMenu();
        hookSingleClickAction();
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                MenuView.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void fillContextMenu(IMenuManager manager) {
        manager.add(action1);
        manager.add(action2);
        manager.add(new Separator());
        drillDownAdapter.addNavigationActions(manager);
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void makeActions() {
        action1 = new Action() {
            public void run() {
                showMessage("Action 1 executed");
            }
        };
        action1.setText("Action 1");
        action1.setToolTipText("Action 1 tooltip");
        action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

        action2 = new Action() {
            public void run() {
                showMessage("Action 2 executed");
            }
        };
        action2.setText("Action 2");
        action2.setToolTipText("Action 2 tooltip");
        action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
        singleClickAction = new Action() {

            @Override
            public void run() {
                ISelection selection = viewer.getSelection();
                TreeObject obj = (TreeObject) ((IStructuredSelection) selection).getFirstElement();
                if (obj == null || obj instanceof TreeParent)
                    return;
                IWorkbenchPage page = MenuView.this.getViewSite().getPage();

                logger.debug(obj.getUrl());
                BrowserEditorInput editorInput = new BrowserEditorInput(obj.getUrl());
                // "http://localhost:8080/catalog/app/" + obj.toString());
                IEditorPart editorPart = page.getActiveEditor();
                if (editorPart == null) {
                    try {
                        page.openEditor(editorInput, MozillaEditor.ID);
                    } catch (PartInitException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    ((MozillaEditor) editorPart).getBrowser().setUrl(editorInput.getName());
                }
            }
        };
    }

    private void hookSingleClickAction() {
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                singleClickAction.run();
            }
        });
    }

    private void showMessage(String message) {
        MessageDialog.openInformation(viewer.getControl().getShell(), "Sample View", message);
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    private class CustomerDeserializer implements JsonDeserializer<JsonArray> {

        private List<AppFunction> functionList = null;

        @Override
        public JsonArray deserialize(JsonElement element, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            return filter(element.getAsJsonArray());
        }

        private JsonArray filter(JsonArray array) {
            // LoggerFactory.getLogger(ApplicationWorkbenchWindowAdvisor.class).info("$$$$ filter start");
            JsonArray array2 = new JsonArray();
            for (Iterator<JsonElement> iter = array.iterator(); iter.hasNext();) {
                JsonObject function = iter.next().getAsJsonObject();
                boolean allowed = false;
                JsonElement id = function.get("id");
                if (id != null) {
                    logger.debug("function list=" + getFunctionList().size());
                    for (AppFunction func : getFunctionList()) {
                        String funcCode = func.getFuncCode() == null ? null : func.getFuncCode().trim();
                        if (id.getAsString().equals("") || id.getAsString().equals(funcCode)) {
                            allowed = true;
                            break;
                        }
                    }

                    if (allowed) {
                        if (function.has("nodes")) {
                            JsonElement node = function.get("nodes");
                            JsonArray array3 = filter(node.getAsJsonArray());
                            if (array3.size() > 0) {
                                function.add("nodes", array3);
                                array2.add(function);
                            }
                        } else {
                            array2.add(function);
                        }
                    }
                } else {
                    if (function.has("nodes")) {
                        JsonElement node = function.get("nodes");
                        JsonArray array3 = filter(node.getAsJsonArray());
                        if (array3.size() > 0) {
                            function.add("nodes", array3);
                            array2.add(function);
                        }
                    }
                }
            }
            return array2;
        }

        private IAppFunctionDao getAppFunctionDao() throws Exception {
            BundleContext bc = Platform.getBundle(Activator.PLUGIN_ID).getBundleContext();
            ServiceReference[] daoRefs = bc.getServiceReferences(IAppFunctionDao.class.getName(),
                    "(org.springframework.osgi.bean.name=appFunctionDao)");
            IAppFunctionDao dao = (IAppFunctionDao) bc.getService(daoRefs[0]);
            return dao;
        }

        private IUserDao getUserDao() throws Exception {
            BundleContext bc = Platform.getBundle(Activator.PLUGIN_ID).getBundleContext();
            ServiceReference[] daoRefs = bc.getServiceReferences(IUserDao.class.getName(),
                    "(org.springframework.osgi.bean.name=userDao)");
            IUserDao dao = (IUserDao) bc.getService(daoRefs[0]);
            return dao;
        }

        private List<AppFunction> getFunctionList() {

            logger.debug("$$$$ func list start");
            List<AppFunction> functions = new ArrayList<AppFunction>();
            if (functionList != null) {
                return functionList;
            }

            String[] args = Platform.getApplicationArgs();
            Map<String, String> params = new HashMap<String, String>();
            for (String arg : args) {
                logger.debug("$$$$ arg=" + arg);
                String[] keyValue = arg.split("=", 2);
                if ("userId".equalsIgnoreCase(keyValue[0])) {
                    params.put("userId", keyValue[1]);
                }
            }

            logger.debug("$$$$ userId=" + params.get("userId"));

            try {
                User user = getUserDao().findByUserId(params.get("userId"));
                functions = getAppFunctionDao().findUserFunctions(user);
            } catch (Exception e) {
                logger.warn("Failed to get function list of user.", e);
            }
            return functions;
        }

    }

}