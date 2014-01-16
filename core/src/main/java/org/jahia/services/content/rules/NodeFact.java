/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.rules;

import javax.jcr.RepositoryException;

/**
 * Common interface for node facts in rules engine.
 * 
 * @author Sergiy Shyrkov
 */
public interface NodeFact {

    /**
     * Returns the UUID of the corresponding node.
     * 
     * @return the UUID of the corresponding nodeo
     * @throws RepositoryException
     *             in case of a repository access error
     */
    String getIdentifier() throws RepositoryException;

    /**
     * Returns the parent node fact.
     * 
     * @return the parent node fact
     * @throws RepositoryException
     *             in case of a repository access error
     */
    AddedNodeFact getParent() throws RepositoryException;

    /**
     * Returns the corresponding path of the node.
     * 
     * @return the corresponding path of the node
     * @throws RepositoryException
     *             in case of a repository access error
     */
    String getPath() throws RepositoryException;

    /**
     * Returns the current JCR workspace name.
     * 
     * @return the current JCR workspace name
     * @throws RepositoryException
     *             in case of a repository access error
     * @since Jahia 6.6
     */
    String getWorkspace() throws RepositoryException;

    /**
     * Returns the current language.
     *
     * @return the current language
     * @throws RepositoryException
     *             in case of a repository access error
     * @since Jahia 6.6
     */
    String getLanguage() throws RepositoryException;

    /**
     * Returns the current JCR operation type.
     *
     * @return the current JCR operation type
     * @throws RepositoryException
     *             in case of a repository access error
     * @since Jahia 6.6
     */
    String getOperationType();

    void setOperationType(String operationType);
}
