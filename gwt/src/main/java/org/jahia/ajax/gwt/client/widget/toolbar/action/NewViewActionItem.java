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

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TreeGridEvent;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.google.gwt.user.client.Event;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineLoader;
import org.jahia.ajax.gwt.client.widget.edit.ContentTypeTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class NewViewActionItem extends BaseActionItem  {

    protected List<String> parentTypesAsList;

    private static int TEMPLATE_TYPE_FOLDER_TOKEN = 2;
    private static int VIEW_FILE_FOLDER_TOKEN = 3;


    private transient GWTJahiaNodeType fileNodeType;

    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        parentTypesAsList = Arrays.asList("jnt:moduleVersionFolder", "jnt:nodeTypeFolder", "jnt:templateTypeFolder");
    }

    public void onComponentSelection() {
        if (fileNodeType == null) {
            linker.loading(Messages.get("label.loading", "Loading"));
            JahiaContentManagementService.App.getInstance().getNodeType("jnt:viewFile", new BaseAsyncCallback<GWTJahiaNodeType>() {
                public void onSuccess(GWTJahiaNodeType result) {
                    fileNodeType = result;
                    newView(linker, true);
                }
            });
        } else {
            newView(linker, false);
        }
    }

    private void newView(final Linker linker, boolean isLoading) {
        final GWTJahiaNode selectedNode = linker.getSelectionContext().getSingleSelection();

        if (selectedNode.getNodeTypes().contains("jnt:moduleVersionFolder")) {
            // Open popup to select nodeType

            ArrayList<String> paths = new ArrayList<String>();
            paths.add(JahiaGWTParameters.getSiteNode().getPath());
            for (String s : (List<String>) JahiaGWTParameters.getSiteNode().getProperties().get("j:resolvedDependencies")) {
                for (GWTJahiaNode n : JahiaGWTParameters.getSitesMap().values()) {
                    if (n.getName().equals(s)) {
                        paths.add(n.getPath());

                    }
                }
            }

            if (!isLoading) {
                linker.loading(Messages.get("label.loading", "Loading"));
            }
            JahiaContentManagementService.App.getInstance().getContentTypesAsTree(Arrays.asList("nt:base"), null, true,
                    new BaseAsyncCallback<List<GWTJahiaNodeType>>() {
                        public void onSuccess(List<GWTJahiaNodeType> result) {
                            linker.loaded();
                            final com.extjs.gxt.ui.client.widget.Window popup = new com.extjs.gxt.ui.client.widget.Window();
                            popup.setHeadingHtml(Messages.get("label.addView", "Add view"));
                            popup.setHeight(400);
                            popup.setWidth(350);
                            popup.setModal(true);
                            popup.setLayout(new FillLayout());
                            ContentTypeTree contentTypeTree = new ContentTypeTree();
                            contentTypeTree.fillStore(result);
                            TreeGrid treeGrid = contentTypeTree.getTreeGrid();
                            treeGrid.sinkEvents(Event.ONDBLCLICK + Event.ONCLICK);
                            treeGrid.addListener(Events.OnDoubleClick, new Listener<BaseEvent>() {
                                public void handleEvent(BaseEvent baseEvent) {
                                    GWTJahiaNodeType gwtJahiaNodeType = (GWTJahiaNodeType) (((TreeGridEvent) baseEvent).getModel());
                                    if (gwtJahiaNodeType != null && linker != null) {
                                        createEngine(fileNodeType, selectedNode, gwtJahiaNodeType.getName());
                                        popup.hide();
                                    }
                                }
                            });
                            popup.add(contentTypeTree);
                            popup.show();
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            linker.loaded();
                            super.onFailure(caught);
                        }
                    }

            );


        } else {
            if (isLoading) {
                linker.loaded();
            }
            createEngine(fileNodeType,selectedNode,findNodeType(selectedNode));
        }
    }

    private void createEngine(GWTJahiaNodeType nodeType, GWTJahiaNode selectedNode, String targetName) {
        HashMap<String, GWTJahiaNodeProperty> props = new HashMap<String, GWTJahiaNodeProperty>();
        props.put("nodeTypeName", new GWTJahiaNodeProperty("nodeTypeName",targetName));
        EngineLoader.showCreateEngine(linker, selectedNode, nodeType, props, targetName.replaceAll(":", "_"), false);
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        GWTJahiaNode n = lh.getSingleSelection();

        boolean enabled = !"".equals(n.getChildConstraints().trim())
                && !lh.isLocked()
                && hasPermission(lh.getSelectionPermissions())
                && PermissionsUtils.isPermitted("jcr:addChildNodes", lh.getSelectionPermissions());

        if (enabled) {
            String  title = getGwtToolbarItem().getTitle();
            if (n.isNodeType("jnt:moduleVersionFolder")) {
                updateTitle(title);
            } else {
                String nodetype = findNodeType(n);
                if (nodetype.equals("")) {
                    // the node type has not been resolved
                    enabled = false;
                } else {
                    updateTitle(title + (!nodetype.equals("")?(" : " + nodetype):""));
                }

            }
        }
        setEnabled(enabled) ;
    }

    private String findNodeType(GWTJahiaNode n) {

        String[] splittedPath = n.getPath().split("/");
        if (n.isNodeType("jnt:nodeTypeFolder")) {
            return n.getName().replace("_",":");
        } else if (splittedPath.length > TEMPLATE_TYPE_FOLDER_TOKEN && n.isNodeType("jnt:templateTypeFolder")) {
            return  splittedPath[splittedPath.length - TEMPLATE_TYPE_FOLDER_TOKEN].replace("_",":");
        }else if (splittedPath.length > VIEW_FILE_FOLDER_TOKEN  && n.isNodeType("jnt:viewFile")) {
            return splittedPath[splittedPath.length - VIEW_FILE_FOLDER_TOKEN].replace("_",":");
        } else if (n.isNodeType("jnt:nodeType")) {
            return n.getName();
        }
        return "";
    }

}
