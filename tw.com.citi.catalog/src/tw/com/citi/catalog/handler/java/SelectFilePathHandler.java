package tw.com.citi.catalog.handler.java;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.PlatformUI;
import tw.com.iisi.rabbithq.editors.IJavaHandler;

public class SelectFilePathHandler implements IJavaHandler {

    @Override
    public Object execute(Browser browser, Object[] args) {
    	DirectoryDialog fd = new DirectoryDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		return fd.open();
    }

}
