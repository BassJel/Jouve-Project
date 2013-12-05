/**
 * Constellio, Open Source Enterprise Search
 * Copyright (C) 2010 DocuLibre inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package com.doculibre.constellio.wicket.application;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.HttpSessionStore;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebRequestCycleProcessor;
import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
import org.apache.wicket.request.IRequestCodingStrategy;
import org.apache.wicket.request.IRequestCycleProcessor;
import org.apache.wicket.request.target.coding.QueryStringUrlCodingStrategy;
import org.apache.wicket.request.target.component.IBookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.IPageRequestTarget;
import org.apache.wicket.request.target.component.listener.IListenerInterfaceRequestTarget;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.util.file.IResourceFinder;
import org.apache.wicket.util.io.IObjectStreamFactory;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.string.AppendingStringBuffer;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.init.InitApplicationPlugin;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.plugins.api.wicket.global.GlobalExtraParamsPlugin;
import com.doculibre.constellio.services.ConstellioInitServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.components.resource.ThemeResourceFinder;
import com.doculibre.constellio.wicket.pages.SolrServletPage;
import com.doculibre.constellio.wicket.pages.responsive.ResponsivePage;
import com.doculibre.constellio.wicket.pages.smb.SmbServletPage;
import com.doculibre.constellio.wicket.session.ConstellioSession;

public class ConstellioApplication extends AuthenticatedWebApplication {

    private String dictionaries;

    private static boolean initialized;
    
    @Override
    public RequestCycle newRequestCycle(Request request, Response response) {
//    	WebRequest webRequest = (WebRequest) request;
//		String displayLang = webRequest.getParameter(BaseConstellioPage.DISPLAY_LANG_PARAM);
//		if (StringUtils.isNotBlank(displayLang)) {
//			for (Locale supportedLocale : ConstellioSpringUtils.getSupportedLocales()) {
//				if (supportedLocale.getLanguage().equals(displayLang)) {
//					ConstellioSession.get().changeLocale(supportedLocale);
//					break;
//				}
//			}
//		}
        return new PersistenceAwareWebRequestCycle(this, (WebRequest) request, response);
    }

    @Override
    protected IResourceFinder getResourceFinder() {
        IResourceFinder defaultResourceFinder = super.getResourceFinder();
        return new ThemeResourceFinder(defaultResourceFinder);
    }

    private static String getExternalParams(CharSequence encoded) {
        String externalParams;
        WebRequestCycle webRequestCycle = (WebRequestCycle) RequestCycle.get();
        HttpServletRequest request = webRequestCycle.getWebRequest().getHttpServletRequest();
        GlobalExtraParamsPlugin globalExtraParamsPlugin = PluginFactory.getPlugin(GlobalExtraParamsPlugin.class);
        if (globalExtraParamsPlugin != null) {
            externalParams = globalExtraParamsPlugin.getExtraParams(encoded, request);
        } else {
            externalParams = "";
        }
        return externalParams;
    }

    @Override
    protected IRequestCycleProcessor newRequestCycleProcessor() {
        return new WebRequestCycleProcessor() {
            @Override
            protected IRequestCodingStrategy newRequestCodingStrategy() {
                return new WebRequestCodingStrategy() {
                    @Override
                    protected CharSequence encode(RequestCycle requestCycle,
                        IBookmarkablePageRequestTarget requestTarget) {
                        CharSequence encoded = super.encode(requestCycle, requestTarget);
                        return encoded + getExternalParams(encoded);
                    }

                    @Override
                    protected CharSequence encode(RequestCycle requestCycle,
                        IListenerInterfaceRequestTarget requestTarget) {
                        CharSequence encoded = super.encode(requestCycle, requestTarget);
                        return encoded + getExternalParams(encoded);
                    }

                    @Override
                    protected CharSequence encode(RequestCycle requestCycle, IPageRequestTarget requestTarget) {
                        CharSequence encoded = super.encode(requestCycle, requestTarget);
                        return encoded + getExternalParams(encoded);
                    }
                };
            }
        };
    }

    @Override
	protected ISessionStore newSessionStore() {
		return new HttpSessionStore(this);
	}

	@SuppressWarnings("rawtypes")
    protected void init() {
        Objects.setObjectStreamFactory(new IObjectStreamFactory() {
            @Override
            public ObjectInputStream newObjectInputStream(InputStream in) throws IOException {
                return new ObjectInputStream(in) {
                    @Override
                    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
                        ClassNotFoundException {
                        String className = desc.getName();
                        try {
                            return Class.forName(className);
                        } catch (ClassNotFoundException e) {
                            for (ClassLoader pluginClassLoader : PluginFactory.getClassLoaders()) {
                                try {
                                    return pluginClassLoader.loadClass(className);
                                } catch (ClassNotFoundException e2) {
                                }
                            }
                            throw e;
                        }
                    }
                };
            }

            @Override
            public ObjectOutputStream newObjectOutputStream(OutputStream out) throws IOException {
                return new ObjectOutputStream(out);
            }
        });

        getApplicationSettings().setPageExpiredErrorPage(getHomePage());
        // getApplicationSettings().setPageExpiredErrorPage(SearchExceptionHandlingPage.class);
        // getApplicationSettings().setInternalErrorPage(SearchExceptionHandlingPage.class);
        getDebugSettings().setAjaxDebugModeEnabled(false);
        getMarkupSettings().setStripWicketTags(true);

        dictionaries = getServletContext().getRealPath(ConstellioSpringUtils.getDictionaries());
        
        PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
        mount(new QueryStringUrlCodingStrategy("/form", pageFactoryPlugin.getSearchFormPage()) {
			@Override
            protected void appendParameters(AppendingStringBuffer url, Map parameters) {
                super.appendParameters(url, parameters);
                String params = getExternalParams(url);
                url.append(params);
            }
        });

//        mount(new QueryStringUrlCodingStrategy("/search", test.TestPage.class) {
//            @Override
//            protected void appendParameters(AppendingStringBuffer url, Map parameters) {
//                super.appendParameters(url, parameters);
//                String params = getExternalParams(url);
//                url.append(params);
//            }
//        });
        mount(new QueryStringUrlCodingStrategy("/search", pageFactoryPlugin.getSearchResultsPage()) {
            @Override
            protected void appendParameters(AppendingStringBuffer url, Map parameters) {
                super.appendParameters(url, parameters);
                String params = getExternalParams(url);
                url.append(params);
            }
        });
        mount(new QueryStringUrlCodingStrategy("/history", pageFactoryPlugin.getSearchHistoryPage()) {
            @Override
            protected void appendParameters(AppendingStringBuffer url, Map parameters) {
                super.appendParameters(url, parameters);
                String params = getExternalParams(url);
                url.append(params);
            }
        });
        mount(new QueryStringUrlCodingStrategy("/admin", pageFactoryPlugin.getAdminPage()) {
            @Override
            protected void appendParameters(AppendingStringBuffer url, Map parameters) {
                super.appendParameters(url, parameters);
                String params = getExternalParams(url);
                url.append(params);
            }
        });

        mount(new QueryStringUrlCodingStrategy("/login", pageFactoryPlugin.getLoginPage()) {
            @Override
            protected void appendParameters(AppendingStringBuffer url, Map parameters) {
                super.appendParameters(url, parameters);
                String params = getExternalParams(url);
                url.append(params);
            }
        });

        mount(new QueryStringUrlCodingStrategy("/responsive", ResponsivePage.class) {
            @Override
            protected void appendParameters(AppendingStringBuffer url, Map parameters) {
                super.appendParameters(url, parameters);
                String params = getExternalParams(url);
                url.append(params);
            }
        });
        
        mountBookmarkablePage("/solr", SolrServletPage.class);
        mountBookmarkablePage("/select", SolrServletPage.class);
        
        mountBookmarkablePage("/getSmbFile", SmbServletPage.class);
        
        initializeIfRequired();

        super.init();
    }

    @Override
    protected void onDestroy() {
        ConstellioInitServices constellioInitServices = ConstellioSpringUtils.getConstellioInitServices();
        constellioInitServices.shutdown();
        super.onDestroy();
    }

    @Override
    public synchronized Session newSession(Request request, Response response) {
        return new ConstellioSession(request);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class getHomePage() {
        PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
        return pageFactoryPlugin.getSearchFormPage();
    }

    public static ConstellioApplication get() {
        return (ConstellioApplication) Application.get();
    }

    public String getDictionaries() {
        return dictionaries;
    }

    @Override
    public Class<? extends WebPage> getSignInPageClass() {
        PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
        return pageFactoryPlugin.getLoginPage();
    }

    @Override
    protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
        return ConstellioSession.class;
    }

    /**
     * Get the restricted token for a user, using IP addresses as location parameter. This implementation
     * combines the "X-Forwarded-For" header with the remote address value so that unique
     * values result with and without proxying. (The forwarded header is not trusted on its own
     * because it can be most easily spoofed.)
     * 
     * @param user
     *            source of token
     * @return restricted token
     */
    public String getToken(ConstellioUser user) {
        HttpServletRequest req = ((WebRequest) RequestCycle.get().getRequest()).getHttpServletRequest();
        String fwd = req.getHeader("X-Forwarded-For");
        if (fwd == null)
            fwd = "nil";
        return user.getToken(fwd + "-" + req.getRemoteAddr());
    }

    /**
     * Also called by other servlets
     */
    public synchronized static void initializeIfRequired() {
		if (!initialized) {
			initialized = true;
			EntityManager entityManager = ConstellioPersistenceContext
					.getCurrentEntityManager();
			if (!entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().begin();
			}

			ConstellioInitServices constellioInitServices = ConstellioSpringUtils
					.getConstellioInitServices();
			constellioInitServices.init();

			for (InitApplicationPlugin initApplicationPlugin : PluginFactory
					.getPlugins(InitApplicationPlugin.class)) {
				initApplicationPlugin.init();
			}

			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().commit();
			}
			entityManager.close();
		}
    }
}
