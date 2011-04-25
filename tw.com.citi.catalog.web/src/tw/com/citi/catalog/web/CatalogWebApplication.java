package tw.com.citi.catalog.web;

import org.apache.wicket.Page;
import org.apache.wicket.util.lang.PackageName;

import tw.com.citi.catalog.web.pages.Home;
import tw.com.iisi.aquarius.web.AbstractAquariusWebApplication;

public class CatalogWebApplication extends AbstractAquariusWebApplication {

    @Override
    protected void init() {
        super.init();
        mount("/app", PackageName.forClass(Home.class));
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return Home.class;
    }

}
