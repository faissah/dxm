/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.notification.templates;

import groovy.lang.Binding;

import org.apache.commons.codec.binary.Hex;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentObjectKey;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.notification.Subscription;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Creates Groovy-based e-mail messages for sending subscription confirmation
 * requests.
 * 
 * @author Sergiy Shyrkov
 */
public class SubscriptionConfirmationMessageBuilder extends MessageBuilder {

    private String objectType;

    protected Subscription subscription;

    /**
     * Initializes an instance of this class.
     * 
     * @param user
     * @param emailAddress
     * @param subscription
     *            subscriber information
     */
    public SubscriptionConfirmationMessageBuilder(JahiaUser user,
            String emailAddress, Subscription subscription) {
        super(user, emailAddress, subscription.getSiteId(), Jahia.getThreadParamBean());
        this.subscription = subscription;
    }

    protected Link getCancellationLink() {
        Link lnk = null;
        String url = Jahia.getContextPath()
                + "/ajaxaction/subscription?action=confirmCancel&key="
                + getEncodedConfirmationKey();
        lnk = new Link("confirmCancel", url, getServerUrl() + url);

        return lnk;
    }

    protected Link getConfirmationLink() {
        Link lnk = null;
        String url = Jahia.getContextPath()
                + "/ajaxaction/subscription?action=confirm&key="
                + getEncodedConfirmationKey();
        lnk = new Link("confirm", url, getServerUrl() + url);

        return lnk;
    }

    protected String getEncodedConfirmationKey() {
        return new String(Hex.encodeHex((subscription.getId() + "|"
                + subscription.getConfirmationKey() + "|" + subscription
                .getSiteId()).getBytes()));
    }

    protected String getObjectType() {
        if (objectType == null) {
            ContentObjectKey contentObjectKey = null;
            try {
                contentObjectKey = (ContentObjectKey) ContentObjectKey
                        .getInstance(subscription.getObjectKey());
            } catch (ClassNotFoundException e) {
                // not a content object key
            }
            objectType = JCRContentUtils
                    .cleanUpNodeName(contentObjectKey != null ? JCRContentUtils
                            .getNodeTypeName(contentObjectKey) : subscription
                            .getObjectKey());
        }

        return objectType;
    }

    @Override
    protected String getTemplateHtmlPart() {
        // TODO consider event and node type also
        return lookupTemplate("notifications/subscription/subscribeConfirmationBody.html");
    }

    @Override
    protected String getTemplateMailScript() {
        // TODO consider event and node type also
        return lookupTemplate("notifications/subscription/subscribeConfirmation.groovy");
    }

    @Override
    protected String getTemplateTextPart() {
        // TODO consider event and node type also
        return lookupTemplate("notifications/subscription/subscribeConfirmationBody.txt");
    }

    protected Link getUnsubscribeLink() {
        Link lnk = null;
        String url = Jahia.getContextPath()
                + "/ajaxaction/subscription?action=cancel&key="
                + new String(Hex.encodeHex((subscription.getId() + "|"
                        + subscription.getUsername() + "|" + subscription
                        .getSiteId()).getBytes()));
        lnk = new Link("confirm", url, getServerUrl() + url);

        return lnk;
    }

    protected Link getWatchedContentLink() {
        return getWatchedContentLink(subscription.getObjectKey());
    }

    @Override
    protected void populateBinding(Binding binding) {
        super.populateBinding(binding);
        binding.setVariable("eventType", subscription.getEventType());
        binding.setVariable("subscription", subscription);
        binding.setVariable("confirmationLink", getConfirmationLink());
        binding.setVariable("cancellationLink", getCancellationLink());
        binding.setVariable("watchedContentLink", getWatchedContentLink());
    }
}