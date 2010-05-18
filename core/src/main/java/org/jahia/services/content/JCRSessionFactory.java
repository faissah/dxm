package org.jahia.services.content;

import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.jahia.jaas.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.springframework.web.context.ServletContextAware;

import javax.jcr.*;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.*;

/**
 * The entry point into the content repositories provided by the <code>JCRStoreProvider</code> list.
 *  
 * Instead of using this class for creating and using sessions, please rather use the JCRTemplate.
 * 
 * @see JCRTemplate
 * 
 * @author toto
 */
public class JCRSessionFactory implements Repository, ServletContextAware {
    private static transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(
            JCRSessionFactory.class);
    protected ThreadLocal<Map<String, Map<String, JCRSessionWrapper>>> userSession = new ThreadLocal<Map<String, Map<String, JCRSessionWrapper>>>();
    private NamespaceRegistryWrapper namespaceRegistry;
    private Map<String, String> descriptors = new HashMap<String, String>();
    private JahiaUserManagerService userService;
    private Map<String, JCRStoreProvider> providers = new HashMap<String, JCRStoreProvider>();
    private List<JCRStoreProvider> providerList = new ArrayList<JCRStoreProvider>();
    private SortedMap<String, JCRStoreProvider> mountPoints = new TreeMap<String, JCRStoreProvider>();
    private SortedMap<String, JCRStoreProvider> dynamicMountPoints = new TreeMap<String, JCRStoreProvider>();
    private static JCRSessionFactory instance;
    private String servletContextAttributeName;
    private ServletContext servletContext;
    private ThreadLocal<JahiaUser> currentUser = new ThreadLocal<JahiaUser>();
    private ThreadLocal<Locale> currentLocale = new ThreadLocal<Locale>();


    private JCRSessionFactory() {
    }

    public void start() {
        Comparator<String> invertedStringComparator = new Comparator<String>() {
            public int compare(String s1, String s2) {
                return s2.compareTo(s1);
            }
        };
        this.mountPoints = new TreeMap<String, JCRStoreProvider>(invertedStringComparator);
        this.dynamicMountPoints = new TreeMap<String, JCRStoreProvider>(invertedStringComparator);
        namespaceRegistry = new NamespaceRegistryWrapper();

        if ((servletContextAttributeName != null) &&
                (servletContext != null)) {
            servletContext.setAttribute(servletContextAttributeName, this);
        }
    }

    public void setServletContextAttributeName(String servletContextAttributeName) {
        this.servletContextAttributeName = servletContextAttributeName;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setDescriptors(Map<String, String> descriptors) {
        this.descriptors = descriptors;
    }

    public void setUserService(JahiaUserManagerService userService) {
        this.userService = userService;
    }


    public JCRSessionWrapper getCurrentUserSession() throws RepositoryException {
        return getCurrentUserSession(null);
    }

    public JCRSessionWrapper getCurrentUserSession(String workspace) throws RepositoryException {
        return getCurrentUserSession(workspace, null);
    }

    public JCRSessionWrapper getCurrentUserSession(String workspace, Locale locale) throws RepositoryException {
        return getCurrentUserSession(workspace, locale, null);
    }

    public JCRSessionWrapper getCurrentUserSession(String workspace, Locale locale,Locale fallbackLocale) throws RepositoryException {
        // thread user session might be inited/closed in an http filter, instead of keeping it
        Map<String, Map<String, JCRSessionWrapper>> smap = userSession.get();
        if (smap == null) {
            smap = new HashMap<String, Map<String, JCRSessionWrapper>>();
        }
        userSession.set(smap);
        String username;

        if (getCurrentUser() == null) {
            logger.error("Null thread user");
            throw new RepositoryException("Null thread user");
        }

        JahiaUser user = getCurrentUser();

        if (JahiaUserManagerService.isGuest(user)) {
            username = JahiaLoginModule.GUEST;
        } else {
            username = user.getUsername();
        }

        Map<String, JCRSessionWrapper> wsMap = smap.get(username);
        if (wsMap == null) {
            wsMap = new HashMap<String, JCRSessionWrapper>();
            smap.put(username, wsMap);
        }

        if (workspace == null) {
            workspace = "default";
        }

        String localeString = "default";
        if (locale != null) {
            localeString = locale.toString();
        }

        final String key = workspace + "-" + localeString + "-" + fallbackLocale;
        JCRSessionWrapper s = wsMap.get(key);

        if (s == null || !s.isLive()) {
            if (!JahiaLoginModule.GUEST.equals(username)) {
                s = login(JahiaLoginModule.getCredentials(username), workspace, locale, fallbackLocale);
                // should be done somewhere else, call can be quite expensive
                if (!(user instanceof JCRUser)) {
                    mountPoints.get("/").deployExternalUser(username, user.getProviderName());
                }
            } else {
                s = login(JahiaLoginModule.getGuestCredentials(), workspace, locale, fallbackLocale);
            }
            wsMap.put(key, s);
        } else {
            s.refresh(true);
        }
        return s;
    }

    protected JCRSessionWrapper getSystemSession() throws RepositoryException {
        return login(JahiaLoginModule.getSystemCredentials());
    }

    protected JCRSessionWrapper getSystemSession(String username) throws RepositoryException {
        return login(JahiaLoginModule.getSystemCredentials(username));
    }

    protected JCRSessionWrapper getSystemSession(String username, String workspace) throws RepositoryException {
        return login(JahiaLoginModule.getSystemCredentials(username), workspace);
    }

    protected JCRSessionWrapper getSystemSession(String username, String workspace, Locale locale) throws RepositoryException {
        return login(JahiaLoginModule.getSystemCredentials(username), workspace, locale, null);
    }

    protected JCRSessionWrapper getUserSession(String username, String workspace) throws RepositoryException {
        return login(JahiaLoginModule.getCredentials(username), workspace);
    }

    protected JCRSessionWrapper getUserSession(String username, String workspace, Locale locale) throws RepositoryException {
        return login(JahiaLoginModule.getCredentials(username), workspace, locale, null);
    }

    public String[] getDescriptorKeys() {
        return descriptors.keySet().toArray(new String[descriptors.size()]);
    }

    public String getDescriptor(String s) {
        return descriptors.get(s);
    }

    public JCRSessionWrapper login(Credentials credentials, String workspace)
            throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return login(credentials, workspace, null, null);
    }

    private JCRSessionWrapper login(Credentials credentials, String workspace, Locale locale, Locale fallbackLocale)
            throws LoginException, NoSuchWorkspaceException, RepositoryException {
        if (!(credentials instanceof SimpleCredentials)) {
            throw new LoginException("Only SimpleCredentials supported in this implementation");
        }

        final SimpleCredentials simpleCreds = (SimpleCredentials) credentials;

        JahiaLoginModule m = new JahiaLoginModule();
        Subject s = new Subject();
        HashMap<String, ?> sharedState = new HashMap<String, Object>();
        HashMap<String, ?> options = new HashMap<String, Object>();
        m.initialize(s, new CallbackHandler() {
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback callback : callbacks) {
                    if (callback instanceof NameCallback) {
                        ((NameCallback) callback).setName(simpleCreds.getUserID());
                    } else if (callback instanceof PasswordCallback) {
                        ((PasswordCallback) callback).setPassword(simpleCreds.getPassword());
                    } else {
                        throw new UnsupportedCallbackException(callback);
                    }
                }
            }
        }, sharedState, options);

        try {
            JahiaLoginModule.Token t = JahiaLoginModule.getToken(simpleCreds.getUserID(), new String(
                    simpleCreds.getPassword()));
            m.login();
            m.commit();
            credentials = JahiaLoginModule.getCredentials(simpleCreds.getUserID(), t != null ? t.deniedPath : null);
        } catch (javax.security.auth.login.LoginException e) {
            throw new LoginException(e);
        }

        Set<JahiaPrincipal> p = s.getPrincipals(JahiaPrincipal.class);
        for (JahiaPrincipal jahiaPrincipal : p) {
            JahiaUser user = null;
            if (!jahiaPrincipal.getName().startsWith(JahiaLoginModule.SYSTEM)) {
                if (jahiaPrincipal.isGuest()) {
                    user = userService.lookupUser(JahiaUserManagerService.GUEST_USERNAME);
                } else {
                    user = userService.lookupUser(jahiaPrincipal.getName());
                }
            }
            return new JCRSessionWrapper(user, credentials, jahiaPrincipal.isSystem(), workspace, locale, this, fallbackLocale);
        }
        throw new LoginException("Can't login");
    }

    public JCRSessionWrapper login(Credentials credentials) throws LoginException, RepositoryException {
        return login(credentials, null);
    }

    public JCRSessionWrapper login(String workspace)
            throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return login(JahiaLoginModule.getGuestCredentials(), workspace);
    }

    public JCRSessionWrapper login() throws LoginException, RepositoryException {
        return login(null, null);
    }

    public boolean isStandardDescriptor(String key) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isSingleValueDescriptor(String key) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Value getDescriptorValue(String key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Value[] getDescriptorValues(String key) {
        return new Value[0];  //To change body of implemented methods use File | Settings | File Templates.
    }


    public Map<String, JCRStoreProvider> getMountPoints() {
        return mountPoints;
    }

    public Map<String, JCRStoreProvider> getDynamicMountPoints() {
        return dynamicMountPoints;
    }

    public Map<String, JCRStoreProvider> getProviders() {
        return providers;
    }

    public void addProvider(String key, String mountPoint, JCRStoreProvider p) {
        providers.put(key, p);
        providerList.add(p);

        if (mountPoint != null) {
            if (p.isDynamicallyMounted()) {
                dynamicMountPoints.put(mountPoint, p);
            } else {
                mountPoints.put(mountPoint, p);
            }
        }
    }

    public void removeProvider(String key) {
        JCRStoreProvider p = providers.remove(key);
        providerList.remove(p);
        if (p != null && p.getMountPoint() != null) {
            mountPoints.remove(p.getMountPoint());
            dynamicMountPoints.remove(p.getMountPoint());
        }
    }

    /**
     * Returns a list of providers ordered by registration order. This is important because some providers
     * are more "low-level" than others.
     *
     * @return an ORDERED list of providers
     */
    public List<JCRStoreProvider> getProviderList() {
        return providerList;
    }

    public static JCRSessionFactory getInstance() {
        if (instance == null) {
            instance = new JCRSessionFactory();
        }
        return instance;
    }

    public void closeAllSessions() {
        Map<String, Map<String, JCRSessionWrapper>> smap = userSession.get();
        if (smap != null) {
            for (Map<String, JCRSessionWrapper> wsMap : smap.values()) {
                for (JCRSessionWrapper s : wsMap.values()) {
                    s.logout();
                }
            }
            userSession.set(null);
        }
    }

    public JCRStoreProvider getProvider(String path) {
        for (String mp : dynamicMountPoints.keySet()) {
            if (path.startsWith(mp + "/")) {
                return dynamicMountPoints.get(mp);
            }
        }
        for (String mp : mountPoints.keySet()) {
            if (mp.equals("/") || path.equals(mp) || path.startsWith(mp + "/")) {
                return mountPoints.get(mp);
            }
        }
        return null;
    }
    
    public boolean unmount(JCRStoreProvider p) {
        if (p != null && p.isDynamicallyMounted()) {
            p.stop();
            return true;
        }
        return false;
    }

    public NamespaceRegistry getNamespaceRegistry() throws RepositoryException {
        return namespaceRegistry;
    }

    public JahiaUser getCurrentUser() {
        return currentUser.get();
    }

    public void setCurrentUser(JahiaUser user) {
        currentUser.set(user);
    }

    public Locale getCurrentLocale() {
        return currentLocale.get();
    }

    public void setCurrentLocale(Locale locale) {
        currentLocale.set(locale);
    }
}