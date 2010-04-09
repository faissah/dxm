package org.jahia.services.sites.jcr;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.analytics.GoogleAnalyticsProfile;
import org.jahia.services.content.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 5, 2010
 * Time: 11:48:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class JCRSitesProvider {
    private static Logger logger = Logger.getLogger(JCRSitesProvider.class);
    private JCRTemplate jcrTemplate;

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    public List<JahiaSite> getSites() {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<List<JahiaSite>>() {
                public List<JahiaSite> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    final List<JahiaSite> list = new ArrayList<JahiaSite>();
                    NodeIterator ni = session.getNode("/sites").getNodes();
                    while (ni.hasNext()) {
                        JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) ni.next();
                        if (nodeWrapper.isNodeType("jnt:virtualsite")) {
                            list.add(getSite(nodeWrapper));
                        }
                    }
                    return list;
                }
            });
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JahiaSite getSiteById(final int id) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JahiaSite>() {
                public JahiaSite doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:virtualsite] as s where s.[j:siteId]=" + id, Query.JCR_SQL2);
                    NodeIterator ni = q.execute().getNodes();
                    if (ni.hasNext()) {
                        JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) ni.next();
                        return getSite(nodeWrapper);
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error("cannot get site", e);
        }
        return null; //
    }

    public JahiaSite getSiteByKey(final String key) {
        try {
            if (StringUtils.isEmpty(key)) {
                return null;
            }
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JahiaSite>() {
                public JahiaSite doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper n = session.getNode("/sites/" + key);
                    return getSite(n);
                }
            });
        } catch (PathNotFoundException e) {
            return null;
        } catch (RepositoryException e) {
            logger.error("cannot get site", e);
        }
        return null; //
    }

    public JahiaSite getSiteByName(final String name) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JahiaSite>() {
                public JahiaSite doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:virtualsite] as s where s.[j:serverName]='" + name + "'", Query.JCR_SQL2);
                    NodeIterator ni = q.execute().getNodes();
                    if (ni.hasNext()) {
                        JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) ni.next();
                        return getSite(nodeWrapper);
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error("cannot get site", e);
        }
        return null; //
    }

    public int getNbSites() {
        return getSites().size();
    }

    public JahiaSite getDefaultSite() {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JahiaSite>() {
                public JahiaSite doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper node = session.getNode("/sites");
                    if (node.hasProperty("j:defaultSite")) {
                        return getSite((JCRNodeWrapper) node.getProperty("j:defaultSite").getNode());
                    } else {
                        return null;
                    }
                }
            });
        } catch (RepositoryException e) {
            logger.error("cannot get site", e);
        }
        return null;
    }

    public void setDefaultSite(final JahiaSite site) {
        try {
            jcrTemplate.doExecuteWithSystemSession(new JCRCallback() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper node = session.getNode("/sites");
                    if (!node.isCheckedOut()) {
                        session.checkout(node);
                    }
                    if (site != null) {
                        JCRNodeWrapper s = node.getNode(site.getSiteKey());
                        node.setProperty("j:defaultSite", s);
                        session.save();
                    } else if (node.hasProperty("j:defaultSite")) {
                        node.getProperty("j:defaultSite").remove();
                        session.save();
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error("cannot set default site", e);
        }
    }


    public void addSite(final JahiaSite site, JahiaUser user) {
        try {
            int id = 1;
            List<JahiaSite> sites = getSites();
            for (JahiaSite jahiaSite : sites) {
                if (id <= jahiaSite.getID()) {
                    id = jahiaSite.getID() + 1;
                }
            }
            site.setID(id);

            jcrTemplate.doExecuteWithSystemSession(new JCRCallback() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        Query q = session.getWorkspace().getQueryManager().createQuery("SELECT * FROM [jmix:virtualsitesFolder]", Query.JCR_SQL2);
                        QueryResult qr = q.execute();
                        NodeIterator ni = qr.getNodes();
                        try {
                            while (ni.hasNext()) {
                                Node sitesFolder = ni.nextNode();
                                String options = "";
                                if (sitesFolder.hasProperty("j:virtualsitesFolderConfig")) {
                                    options = sitesFolder.getProperty("j:virtualsitesFolderConfig").getString();
                                }

                                Node f = JCRContentUtils.getPathFolder(sitesFolder, site.getSiteKey(), options);
                                try {
                                    f.getNode(site.getSiteKey());
                                } catch (PathNotFoundException e) {
                                    session.getWorkspace().getVersionManager().checkout(f.getPath());
                                    f.addNode(site.getSiteKey(), Constants.JAHIANT_VIRTUALSITE);
                                    if (sitesFolder.hasProperty("j:virtualsitesFolderSkeleton")) {
                                        InputStream is = null;
                                        try {
                                            is = new FileInputStream(org.jahia.settings.SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/"+ sitesFolder.getProperty("j:virtualsitesFolderSkeleton").getString());
                                            session.importXML(f.getPath()+"/"+ site.getSiteKey(), is,ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, true);
                                        } finally {
                                            IOUtils.closeQuietly(is);
                                        }
                                    }

                                    Node siteNode = f.getNode(site.getSiteKey());
                                    siteNode.setProperty("j:title", site.getTitle());
                                    siteNode.setProperty("j:description", site.getDescr());
                                    siteNode.setProperty("j:serverName", site.getServerName());
                                    siteNode.setProperty("j:siteId", site.getID());
                                    siteNode.setProperty("j:installedModules", new String[]{site.getTemplateFolder()});
                                    siteNode.setProperty("j:defaultLanguage", site.getDefaultLanguage());
                                    siteNode.setProperty("j:mixLanguage", site.isMixLanguagesActive());
                                    siteNode.setProperty("j:languages", site.getLanguages().toArray(new String[site.getLanguages().size()]));
                                    siteNode.setProperty("j:mandatoryLanguages", site.getMandatoryLanguages().toArray(new String[site.getMandatoryLanguages().size()]));
                                    session.save();
                                    session.getWorkspace().getVersionManager().checkin(f.getPath());
                                    JCRPublicationService.getInstance().publish(siteNode.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, true);
                                }
                            }
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }

                        JCRNodeWrapper defaultSite = session.getNode("/templatesSet/" + site.getTemplatePackageName() + "/defaultSite");
                        defaultSite.copy(session.getNode("/sites"), site.getSiteKey(), false);
                        session.save();
                        session.getNode("/sites/"+site.getSiteKey()).clone(session.getNode("/users"), "users");
                        session.save();
                    } catch (PathNotFoundException e) {
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void deleteSite(final String siteKey) {
        try {
            JCRCallback deleteCacllback = new JCRCallback() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper sites = session.getNode("/sites");
                    if (!sites.isCheckedOut()) {
                        session.checkout(sites);
                    }
                    JCRNodeWrapper site = sites.getNode(siteKey);
                    site.remove();
                    session.save();
                    return null;
                }
            };
            JCRTemplate.getInstance().doExecuteWithSystemSession(deleteCacllback);
            // Now let's delete the live workspace site.
            JCRTemplate.getInstance().doExecuteWithSystemSession(null, Constants.LIVE_WORKSPACE, deleteCacllback);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void updateSite(final JahiaSite site) {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper sites = session.getNode("/sites");
                    if (!sites.isCheckedOut()) {
                        session.checkout(sites);
                    }
                    JCRNodeWrapper siteNode = sites.getNode(site.getSiteKey());
                    if (!siteNode.isCheckedOut()) {
                        session.checkout(siteNode);
                    }
                    siteNode.setProperty("j:title", site.getTitle());
                    siteNode.setProperty("j:description", site.getDescr());
                    siteNode.setProperty("j:serverName", site.getServerName());
//                    siteNode.setProperty("j:installedModules", new String[]{site.getTemplatePackageName()});
                    String defaultLanguage = site.getDefaultLanguage();
                    if (defaultLanguage != null)
                        siteNode.setProperty("j:defaultLanguage", defaultLanguage);
                    siteNode.setProperty("j:mixLanguage", site.isMixLanguagesActive());
                    siteNode.setProperty("j:languages", site.getLanguages().toArray(
                            new String[site.getLanguages().size()]));
                    siteNode.setProperty("j:mandatoryLanguages", site.getMandatoryLanguages().toArray(
                            new String[site.getMandatoryLanguages().size()]));

                    // add google analytics
                    GoogleAnalyticsProfile googleAnalyticsProfile = site.getGoogleAnalytics();
                    if (googleAnalyticsProfile != null && googleAnalyticsProfile.isEnabled()) {
                        siteNode.setProperty("j:gaAccount", googleAnalyticsProfile.getAccount());
                        siteNode.setProperty("j:gaLogin", googleAnalyticsProfile.getLogin());
                        siteNode.setProperty("j:gaPassword", googleAnalyticsProfile.getPassword());
                        siteNode.setProperty("j:gaTypeUrl", googleAnalyticsProfile.getTypeUrl());
                        siteNode.setProperty("j:gaProfile", googleAnalyticsProfile.getProfile());
                    }
                    if (googleAnalyticsProfile != null && googleAnalyticsProfile.isToDelete()) {
                        siteNode.getProperty("j:gaAccount").remove();
                        siteNode.getProperty("j:gaLogin").remove();
                        siteNode.getProperty("j:gaPassword").remove();
                        siteNode.getProperty("j:gaTypeUrl").remove();
                        siteNode.getProperty("j:gaProfile").remove();
                    }
                    session.save();
                    return null;
                }
            });
            JCRPublicationService.getInstance().publish("/sites/"+site.getSiteKey(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, false);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private JahiaSite getSite(JCRNodeWrapper node) throws RepositoryException {
        int siteId = (int) node.getProperty("j:siteId").getLong();

        Properties props = new Properties();

        JahiaSite site = new JahiaSite(siteId, node.getProperty("j:title").getString(), node.getProperty("j:serverName").getString(),
                node.getName(), node.getProperty("j:description").getString(), props, node.getPath());
        Value[] s = node.getProperty("j:installedModules").getValues();
        site.setTemplatePackageName(ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByFileName(s[0].getString()).getName());
        site.setMixLanguagesActive(node.getProperty("j:mixLanguage").getBoolean());
        site.setDefaultLanguage(node.getProperty("j:defaultLanguage").getString());
        Value[] languages = node.getProperty("j:languages").getValues();
        Set<String> languagesList = new LinkedHashSet<String>();
        for (Value language : languages) {
            languagesList.add(language.getString());
        }
        site.setLanguages(languagesList);
        languages = node.getProperty("j:mandatoryLanguages").getValues();
        languagesList = new LinkedHashSet<String>();
        for (Value language : languages) {
            languagesList.add(language.getString());
        }
        site.setMandatoryLanguages(languagesList);
        String account = node.getPropertyAsString("j:gaAccount");
        String login = node.getPropertyAsString("j:gaLogin");
        String password = node.getPropertyAsString("j:gaPassword");
        String profile = node.getPropertyAsString("j:gaProfile");
        String typeUrl = node.getPropertyAsString("j:gaTypeUrl");
        boolean enabled = true;
        site.setGoogleAnalyticsProfile(typeUrl, enabled, password, login, profile, account);
        return site;
    }

}
