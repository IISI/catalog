package tw.com.citi.catalog.handler.java;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;
import tw.com.iisi.rabbithq.editors.IJavaHandler;

public class SelectGeneralFileHandler implements IJavaHandler {

    @Override
    public Object execute(Browser browser, Object[] args) {
    	FileDialog fd = new FileDialog(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell(), SWT.OPEN);
        String filePath = fd.open();
        return filePath;
    }

}
