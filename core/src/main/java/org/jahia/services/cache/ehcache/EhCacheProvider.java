/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.cache.ehcache;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.jahia.services.cache.CacheProvider;
import org.jahia.services.cache.CacheService;
import org.jahia.services.cache.CacheImplementation;
import org.jahia.settings.SettingsBean;
import org.jahia.exceptions.JahiaInitializationException;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.management.ManagementService;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

/**
 * EHCache based cache provider implementation.
 * User: Serge Huber
 * Date: 29 mars 2007
 * Time: 17:25:33
 */
public class EhCacheProvider implements CacheProvider {

    final private static Logger logger = org.slf4j.LoggerFactory.getLogger (EhCacheProvider.class);

    CacheManager cacheManager = null;
    private int groupsSizeLimit = 100;
    private String configurationResource = "/ehcache-jahia.xml";
    private String managedBeanServerName = "SimpleAgent";
    
    public void init(SettingsBean settingsBean, CacheService cacheService) throws JahiaInitializationException {
   		cacheManager = CacheManager.create(getClass().getResource(configurationResource));
    	if (StringUtils.isNotEmpty(managedBeanServerName)) {
	        MBeanServer mBeanServer = MBeanServerFactory.createMBeanServer(managedBeanServerName);
	        ManagementService.registerMBeans(cacheManager, mBeanServer, false, false, false, true, false);
    	}
    }

    public void shutdown() {
        logger.info("Shutting down cache provider, serializing to disk if active. Please wait...");
        long startTime = System.currentTimeMillis();
        cacheManager.shutdown();
        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("Cache provider shutdown completed in " + totalTime + "[ms]");
    }

    public void enableClusterSync() throws JahiaInitializationException {
    }

    public void stopClusterSync() {
    }

    public void syncClusterNow() {
    }

    public boolean isClusterCache() {
        return false;
    }

    public CacheImplementation newCacheImplementation(String name) {
        return new EhCacheImpl(name, cacheManager, this);
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public int getGroupsSizeLimit() {
        return groupsSizeLimit;
    }

    public void setGroupsSizeLimit(int groupsSizeLimit) {
        this.groupsSizeLimit = groupsSizeLimit;
    }

	public void setConfigurationResource(String configurationResource) {
    	this.configurationResource = configurationResource;
    }

	public void setManagedBeanServerName(String managedBeanServerName) {
    	this.managedBeanServerName = managedBeanServerName;
    }
}