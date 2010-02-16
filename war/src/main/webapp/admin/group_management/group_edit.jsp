<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@page language="java" %>
<%@page import="java.security.Principal" %>
<%@page import="java.util.*" %>
<%@page import="org.jahia.bin.*" %>
<%@page import="org.jahia.utils.*" %>
<%@page import="org.jahia.services.pages.*" %>
<%@page import="org.jahia.data.JahiaData" %>
<%@page import="org.jahia.data.viewhelper.principal.PrincipalViewHelper" %>
<%@page import="org.jahia.params.ParamBean" %>
<%@page import="org.jahia.registries.ServicesRegistry" %>
<%@page import="org.jahia.services.acl.JahiaBaseACL" %>
<%@page import="org.jahia.utils.i18n.JahiaResourceBundle" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<jsp:useBean id="groupMessage" class="java.lang.String" scope="session"/>
<c:set var="noneLabel"><fmt:message key="org.jahia.userMessage.none"/></c:set>
<%
    JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    String groupName = (String) session.getAttribute("selectedGroup");
//predrag
    String providerName = (String) session.getAttribute("providerName");
//end predrag
    int stretcherToOpen = 1; %>
<script type="text/javascript" src="<%=request.getContextPath()%>/javascript/selectbox.js">
</script>
<script type="text/javascript">
    window.onunload = closeEngineWin;

    // These following are destined to paste select user from the group members popup.
    var usrgrpname = new Array();
    var index = 0;

    function addOptions(text, value)
    {
        if (document.mainForm.selectMember.options[0].value == "null") {
            document.mainForm.selectMember.options[0] = null;
        }
        var i = document.mainForm.selectMember.length;
        for (j = 0; j < i; j++) {
            var val = document.mainForm.selectMember.options[j].value;
            if (value == val) {
                usrgrpname[index++] = val;
                return;
            }
        }
        document.mainForm.selectMember.options[i] = new Option(text, value);
        document.mainForm.selectMember.disabled = false;

    }

    function addOptionsBalance()
    {
        if (index > 0) {
            var badName = "\n";
            for (i = 0; i < index; i++) {
                badName += "- " + usrgrpname[i].substr(1, usrgrpname[i].lastIndexOf(':') - 1) + "\n";
            }
            alert("<%=JahiaTools.html2text(JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.users.ManageGroups.alertUsersGroupAlreadyMember.label",
          jParams.getUILocale()))%>" + badName);
            index = 0;
        }
    }

    function beforeSubmit() {
        selectAllOptionsSelectBox(document.mainForm.selectMember);
    }

    function openUserDetail()
    {
        params = "width=800px,height=750px,resizable=yes,scrollbars=yes,status=no";
        uDetailurl = '<%=JahiaAdministration.composeActionURL(request,response,"users","&isPopup=true&sub=edit&selectedUsers=")%>' + document.mainForm.selectMember.value;
        //alert(uDetailurl);
        window.open(uDetailurl, "mywindow", params);
    }

    function homePageSelected(pid, url, title) {
        var titleElement = document.getElementById('homePageLabel');
        titleElement.removeChild(titleElement.firstChild);
        titleElement.appendChild(document.createTextNode(title));
        return true;
    }
    function homePageRemoved() {
    	document.getElementById('homePageID').value = ''; 
        var titleElement = document.getElementById('homePageLabel');
        titleElement.removeChild(titleElement.firstChild);
        titleElement.appendChild(document.createTextNode('${noneLabel}'));
        return true;
    }
</script>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="org.jahia.admin.users.ManageGroups.editGroup.label"/></h2>
</div>
<div id="main">
    <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
        <tbody>
        <tr>
            <td style="vertical-align: top;" align="left">
                <%@include file="/admin/include/tab_menu.inc" %>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top;" align="left" height="100%">
                <div class="dex-TabPanelBottom">
                    <div class="tabContent">
                        <jsp:include page="/admin/include/left_menu.jsp">
                            <jsp:param name="mode" value="site"/>
                        </jsp:include>
                        <div id="content" class="fit">
                            <div class="head">
                                <div class="object-title">
                                    <fmt:message key="org.jahia.admin.users.ManageGroups.editSelectedGroup.label"/>
                                </div>
                            </div>
                            <div class="content-item-noborder">
                                <%
                                    if (groupMessage.length() > 0) { %>
                                <p class="errorbold">
                                    <%=groupMessage %>
                                </p>
                                <% } %>
                                <!--//predrag   --><%if (providerName.equals("jahia") || providerName.equals("jcr")) { %>
                                <% String openWindowURL = "javascript:openUserGroupSelect('users','addMember', 'Principal|Provider, 6|Name, 15|Properties, 20');"; %>
                                <!--//end predrag-->
                                <div class="content-body">
                                    <div id="operationMenu">
                    <span class="dex-PushButton">
                      <span class="first-child">
                        <a class="ico-user-add" href="<%=openWindowURL%>"><fmt:message key="org.jahia.admin.users.ManageGroups.addMembers.label"/></a>
                      </span>
                    </span>
                    <span class="dex-PushButton">
                      <span class="first-child">
                        <a class="ico-user-delete"
                           href="javascript:removeSelectBox(document.mainForm.selectMember, '- - - - - - - - - - <fmt:message key="org.jahia.admin.users.ManageGroups.noMoreMembers.label"/> - - - - - - - - - -');"><fmt:message key="org.jahia.admin.users.ManageGroups.removeMembers.label"/></a>
                      </span>
                    </span>
                                    </div>
                                </div>
                                <!--//predrag--><%} %>
                                <!--//end predrag-->
                                <form name="mainForm"
                                      action='<%=JahiaAdministration.composeActionURL(request,response,"groups","&sub=processEdit")%>'
                                      method="post">
                                    <!-- Edit group -->
                                    <table border="0" cellpadding="3">
                                        <tr>
                                            <td>
                                                <fmt:message key="org.jahia.admin.users.ManageGroups.groupName.label"/>&nbsp;
                                            </td>
                                            <td>
                                                <!-- This hidden field is here when one will decide that a group name can be changed --><input
                                                    type="hidden" name="groupName" value="<%=groupName%>"><!-- This hidden field can be changed to 'update' so that we keep the edited data without confirming changes --><input
                                                    type="hidden" name="actionType" value="save"/><b><%=groupName %></b>
                                            </td>
                                        </tr>
                                        <tr style="vertical-align: top">
                                            <td>
                                                <fmt:message key="org.jahia.admin.homePage.label"/>&nbsp;
                                            </td>
                                            <td>
                                                <b id="homePageLabel">${not empty homePageLabel ? homePageLabel : noneLabel}</b>
                                                <input type="hidden" name="homePageID" id="homePageID" value="${homePageID}">
                                                <br/>
                                                <span class="dex-PushButton">
                                                    <span class="first-child">
                                                        <c:set var="label"><fmt:message key='org.jahia.admin.users.ManageGroups.altSetHomePageForThisGroup.label'/></c:set>
                                                        <%-- 
                                                        <ui:pageSelector fieldId="homePageID" displayIncludeChildren="false" onSelect="homePageSelected" label="${label}" title="${label}" class="ico-home-add"/>
                                                        --%>
                                                    </span>
                                                </span>
                                                <span class="dex-PushButton">
                                                    <span class="first-child">
                                                        <a href="#remove" class="ico-delete" onclick="homePageRemoved(); return false;"><fmt:message key="org.jahia.admin.users.ManageGroups.altSetHomePageForThisGroupToNone.label"/></a>
                                                    </span>
                                                </span>
                                            </td>
                                        </tr>
                                        <% if (jParams.getUser().isPermitted("global/password-policy")) { %>
                                        <c:if test="${enforcePasswordPolicyForSite}">
                                            <tr>
                                                <td>
                                                    <label for="enforcePasswordPolicy">
                                                        <fmt:message key="org.jahia.admin.site.ManageSites.enforcePasswordPolicy.label"/>&nbsp;
                                                    </label>
                                                </td>
                                                <td>
                                                    <input class="input" type="checkbox" id="enforcePasswordPolicy"
                                                           name="enforcePasswordPolicy" value="true"
                                                            <c:if test="${enforcePasswordPolicy}">
                                                                checked="checked"
                                                            </c:if>
                                                            <c:if test="${empty enforcePasswordPolicy}">
                                                                disabled="disabled"
                                                            </c:if>/>
                                                </td>
                                            </tr>
                                        </c:if>
                                        <% } %>
                                    </table>
                                    <br>
                                    <table class="text" border="0" cellspacing="0" cellpadding="3">
                                        <tr>
                                            <td align="center">
                                                <div align="left">
                                                    <i><fmt:message key="org.jahia.admin.users.ManageGroups.groupMembers.label"/>:</i>
                                                </div>
                                                <table class="text" width="100%" border="0" cellspacing="0"
                                                       cellpadding="0">
                                                    <tr>
                                                        <td>
                                <span class="dex-PushButton">
                                    <span class="first-child">
                                        <a class="sort"
                                           href="javascript:sortSelectBox(document.mainForm.selectMember, false, /.{2}/);"
                                           title="<fmt:message key='label.sortByProvider'/>"><fmt:message key="label.sortByProvider"/></a>
                                    </span>
                                </span>
                                <span class="dex-PushButton">
                                    <span class="first-child">
                                        <a class="sort"
                                           href="javascript:sortSelectBox(document.mainForm.selectMember, false, /.{8}/);"
                                           title="<fmt:message key='org.jahia.admin.users.ManageGroups.altSortByIdentifier.label'/>"><fmt:message key="org.jahia.admin.users.ManageGroups.altSortByIdentifier.label"/></a>
                                    </span>
                                </span>
                                <span class="dex-PushButton">
                                    <span class="first-child">
                                        <a class="sort"
                                           href="javascript:sortSelectBox(document.mainForm.selectMember, false, /.{24}/);"
                                           title="<fmt:message key='label.sortByProperty'/>"><fmt:message key="label.sortByProperty"/></a>
                                    </span>
                                </span>
                                                        </td>

                                                    </tr>
                                                </table>
                                                <%
                                                    Set groupMembersSet = (Set) request.getAttribute("groupMembers");
                                                    String[] textPattern = {"Principal", "Provider, 6", "Name, 15", "Properties, 20"};
                                                    PrincipalViewHelper principalViewHelper = new PrincipalViewHelper(textPattern); %>
                                                <select class="fontfix" name="selectMember" size="25"
                                                        style="width:435px;" multiple="multiple"
                                                        <%if (groupMembersSet.size() == 0) { %>disabled="disabled" <%} %>
                                                        <%if (providerName.equals("jahia")) { %>
                                                        ondblclick="javascript:{ if (<%= jParams.getUser().isRoot() %> || this.value != 'uroot:0') { openUserDetail(); } }"<%} %>
                                                        ><%
                                                    if (groupMembersSet.size() == 0) { %>
                                                    <option value="null">- - - - - - - - - - <fmt:message key="org.jahia.admin.users.ManageGroups.noMoreMembers.label"/> - - - - - - - - - -</option>
                                                    <%
                                                    } else {
                                                        Iterator it = groupMembersSet.iterator();
                                                        while (it.hasNext()) {
                                                            Principal p = (Principal) it.next(); %>
                                                    <option value="<%=principalViewHelper.getPrincipalValueOption(p)%>"><%=principalViewHelper.getPrincipalTextOption(p) %>
                                                    </option>
                                                    <%
                                                            }
                                                        } %>
                                                </select>
                                            </td>
                                            <td valign="top" align="left">
                                                <br/><br/><br/><br/>
                                                <span class="dex-PushButton">
                                                    <span class="first-child">
                                                        <a class="ico-selection-all"
                                                           href="#select-all" onclick="selectAllOptionsSelectBox(document.mainForm.selectMember); return false;"><fmt:message key="org.jahia.admin.users.ManageGroups.altSelectAllGroupMembers.label"/></a>
                                                    </span>
                                                </span>
                                                <br/>
                                                <span class="dex-PushButton">
                                                    <span class="first-child">
                                                        <a class="ico-selection-invert"
                                                           href="#select-all" onclick="invertSelectionSelectBox(document.mainForm.selectMember); return false;"><fmt:message key="org.jahia.admin.users.ManageGroups.altInvertGroupMembersSelection.label"/></a>
                                                    </span>
                                                </span>
                                            </td>
                                        </tr>
                                        <tr>
	                                        <td colspan="2">
		                                        <div id="gwtroleprincipal" siteKey="<%=jParams.getSiteKey()%>" group="true" principalKey="<%=groupName%>" class="jahia-admin-gxt"></div>
	                                        </td>
                                        </tr>
                                    </table>
                                    <br/>
                                    <br/>
                                </form>
                            </div>
                        </div>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<div id="actionBar">
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-cancel" href="<%=JahiaAdministration.composeActionURL(request,response,"groups","&sub=display")%>
"><fmt:message key="label.cancel"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">

              <a class="ico-ok"
                 href="javascript:selectAllOptionsSelectBox(document.mainForm.selectMember);javascript:document.mainForm.submit();"><fmt:message key="label.ok"/></a>
            </span>
          </span>
</div>
</div>

