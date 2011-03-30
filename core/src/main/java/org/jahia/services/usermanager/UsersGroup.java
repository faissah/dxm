/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

 package org.jahia.services.usermanager;

import org.jahia.registries.ServicesRegistry;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.jcr.JCRGroup;

import javax.jcr.Node;
import java.util.*;
import java.security.Principal;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 13 mars 2006
 * Time: 16:44:20
 * 
 */
public class UsersGroup extends JCRGroup {
    private static org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger(UsersGroup.class);

    public UsersGroup(Node nodeWrapper, JCRTemplate jcrTemplate, int siteID) {
        super(nodeWrapper, siteID);
    }

    public Enumeration<Principal> members() {
        return new Vector<Principal>(getRecursiveUserMembers()).elements();
    }

    public Set<Principal> getRecursiveUserMembers() {
        Set<Principal> users = new HashSet<Principal> ();

        List<Principal> userList = null;
        try {
            userList = ServicesRegistry.getInstance().
                          getJahiaSiteUserManagerService().getMembers(
                getSiteID());
            JahiaUser guest = ServicesRegistry.getInstance().
                    getJahiaSiteUserManagerService().getMember(mSiteID, JahiaUserManagerService.GUEST_USERNAME);
            userList.remove(guest);
        } catch (JahiaException ex) {
            UsersGroup.logger.error("Error while trying to retrieve full user list for site " + getSiteID(), ex);
        }
        if (userList != null) {
            users.addAll(userList);
        }
        // should we list ldap users here ?
        return users;
    }

    public boolean addMember(Principal principal) {
        return false;
    }

    public boolean removeMember(Principal principal) {
        return false;
    }

    public boolean isMember(Principal principal) {
        if (principal == null) {
            return false;
        }
        if (principal.getName().equals(JahiaUserManagerService.GUEST_USERNAME)) {
            return false;
        }

        return true;
    }
}
