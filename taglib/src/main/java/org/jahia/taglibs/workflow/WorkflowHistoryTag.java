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

package org.jahia.taglibs.workflow;

import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.workflow.HistoryWorkflowTask;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.List;
import java.util.Locale;

/**
 * Get past tasks of a running workflow
 */
public class WorkflowHistoryTag extends AbstractJahiaTag {
    private static final long serialVersionUID = -4218981385406211476L;

    private String var;

    private int scope = PageContext.PAGE_SCOPE;

    private String workflowId;
    private String workflowProvider;
    private Locale locale;

    @Override
    public int doEndTag() throws JspException {
        WorkflowService service = WorkflowService.getInstance();

        List<HistoryWorkflowTask> tasks = service.getHistoryWorkflowTasks(workflowId,
                workflowProvider, locale != null ? locale : getUILocale());

        pageContext.setAttribute(var, tasks, scope);
        var = null;
        workflowId = null;
        workflowProvider = null;
        scope = PageContext.PAGE_SCOPE;

        return super.doEndTag();
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public void setWorkflowProvider(String workflowProvider) {
        this.workflowProvider = workflowProvider;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
