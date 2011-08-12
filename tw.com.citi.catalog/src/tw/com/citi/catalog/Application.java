package tw.com.citi.catalog;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import tw.com.iisi.rabbithq.security.IModuleChecker;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

    //final Logger logger = LoggerFactory.getLogger(getClass());

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.
     * IApplicationContext)
     */
    public Object start(IApplicationContext context) {
        checkPermission();
        Display display = PlatformUI.createDisplay();
        try {
            int returnCode = PlatformUI.createAndRunWorkbench(display,
                    new ApplicationWorkbenchAdvisor());
            if (returnCode == PlatformUI.RETURN_RESTART) {
                return IApplication.EXIT_RESTART;
            }
            return IApplication.EXIT_OK;
        } finally {
            display.dispose();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.equinox.app.IApplication#stop()
     */
    public void stop() {
        if (!PlatformUI.isWorkbenchRunning())
            return;
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final Display display = workbench.getDisplay();
        display.syncExec(new Runnable() {
            public void run() {
                if (!display.isDisposed())
                    workbench.close();
            }
        });
    }

    private void checkPermission() {
        IConfigurationElement[] configs = Platform.getExtensionRegistry()
                .getConfigurationElementsFor("tw.com.iisi.rabbithq.security");
        //logger.info("Number of security configs: {}", configs.length);
        try {
            for (IConfigurationElement config : configs) {
                final Object o = config.createExecutableExtension("class");
                if (o instanceof IModuleChecker) {
                    ISafeRunnable runnable = new ISafeRunnable() {

                        @Override
                        public void run() throws Exception {
                            ((IModuleChecker) o).check(Platform
                                    .getApplicationArgs());
                        }

                        @Override
                        public void handleException(Throwable exception) {
                            //logger.error("Failed to check permission.", exception);
                        }
                    };
                    SafeRunner.run(runnable);
                }
            }
        } catch (CoreException e) {
            //logger.error("Failed to check permission.", e);
        }
    }

}
