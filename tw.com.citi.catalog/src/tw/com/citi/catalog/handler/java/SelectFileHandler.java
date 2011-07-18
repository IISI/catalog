package tw.com.citi.catalog.handler.java;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;

import tw.com.citi.catalog.util.HashUtil;
import tw.com.iisi.rabbithq.editors.IJavaHandler;

public class SelectFileHandler implements IJavaHandler {

    @Override
    public Object execute(Browser browser, Object[] args) {
        FileDialog fd = new FileDialog(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell(), SWT.OPEN);
        String filePath = fd.open();
        File file = new File(filePath);
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        try {
            sb.append("\"filename\":\"").append(URLEncoder.encode(filePath, "UTF-8")).append("\",");
        } catch (UnsupportedEncodingException e) {
            sb.append("\"filename\":\"").append(e.getMessage()).append("\",");
        }
        sb.append("\"lastModified\":").append(file.lastModified()).append(",");
        sb.append("\"size\":").append(file.length()).append(",");
        try {
            sb.append("\"md5\":\"").append(HashUtil.getMD5Checksum(file)).append("\"");
        } catch (Exception e) {
            sb.append("\"md5\":\"").append(e.getMessage()).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

}
