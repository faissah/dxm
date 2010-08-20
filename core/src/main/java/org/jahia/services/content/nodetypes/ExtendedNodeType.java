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

package org.jahia.services.content.nodetypes;

import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.spi.commons.nodetype.constraint.ValueConstraint;
import org.apache.jackrabbit.spi.commons.value.QValueValue;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.utils.i18n.JahiaTemplatesRBLoader;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.*;

import java.util.*;

/**
 * Jahia extended JCR node type information.
 * @author Thomas Draier
 * Date: 4 janv. 2008
 * Time: 14:02:22
 */
public class ExtendedNodeType implements NodeType {

    private static final transient Logger logger = Logger.getLogger(ExtendedNodeType.class);
    public static final Name NT_BASE_NAME = new Name("base", org.apache.jackrabbit.spi.Name.NS_NT_PREFIX, org.apache.jackrabbit.spi.Name.NS_NT_URI);
    
    private NodeTypeRegistry registry;
    private String systemId;
    private List<ExtendedItemDefinition> items = new ArrayList<ExtendedItemDefinition>();
    private List<String> groupedItems;

    private Map<String, ExtendedNodeDefinition> nodes = new LinkedHashMap<String, ExtendedNodeDefinition>();
    private Map<String, ExtendedPropertyDefinition> properties = new LinkedHashMap<String, ExtendedPropertyDefinition>();
    private Map<String, ExtendedNodeDefinition> unstructuredNodes = new LinkedHashMap<String, ExtendedNodeDefinition>();
    private Map<Integer, ExtendedPropertyDefinition> unstructuredProperties = new LinkedHashMap<Integer, ExtendedPropertyDefinition>();

    private Map<String, ExtendedNodeDefinition> allNodes;
    private Map<String, ExtendedPropertyDefinition> allProperties;
    private Map<String, ExtendedNodeDefinition> allUnstructuredNodes;
    private Map<Integer, ExtendedPropertyDefinition> allUnstructuredProperties;

    private Name name;
    private String alias;
    private boolean isAbstract;
    private boolean isMixin;
    private boolean hasOrderableChildNodes;
    private String primaryItemName;
    private String[] declaredSupertypeNames = new String[0];
    private ExtendedNodeType[] declaredSupertypes = new ExtendedNodeType[0];
    private List<ExtendedNodeType> declaredSubtypes = new ArrayList<ExtendedNodeType>();
    private String validator;
    private boolean queryable = true;
    private String itemsType;
    private List<String> mixinExtendNames = new ArrayList<String>();
    private List<ExtendedNodeType> mixinExtend = new ArrayList<ExtendedNodeType>();

    private Map<Locale, String> labels = new HashMap<Locale, String>(1);
    
    public ExtendedNodeType(NodeTypeRegistry registry, String systemId) {
        this.registry = registry;
        this.systemId = systemId;
    }

    public String getSystemId() {
        return systemId;
    }

    public String getName() {
        return name.toString();
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Name getNameObject() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
        this.alias = name != null ? name.toString() : null;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }

    public boolean isMixin() {
        return isMixin;
    }

    public void setMixin(boolean mixin) {
        isMixin = mixin;
    }

    public boolean hasOrderableChildNodes() {
        if(!hasOrderableChildNodes) {
            return hasOrderableChildNodes(true);
        }
        return hasOrderableChildNodes;
    }

    private boolean hasOrderableChildNodes(boolean checkSupertypes) {
        if (checkSupertypes) {
            final ExtendedNodeType[] supertypes = getSupertypes();
            for (ExtendedNodeType supertype : supertypes) {
                if (supertype.hasOrderableChildNodes(false)) {
                    return true;
                }
            }
            return false;
        }
        return hasOrderableChildNodes;
    }

    public void setHasOrderableChildNodes(boolean hasOrderableChildNodes) {
        this.hasOrderableChildNodes = hasOrderableChildNodes;
    }

    public boolean isQueryable() {
        return queryable;
    }

    public void setQueryable(boolean queryable) {
        this.queryable = queryable;
    }

    public String getPrimaryItemName() {
        return primaryItemName;
    }

    public void setPrimaryItemName(String primaryItemName) {
        this.primaryItemName = primaryItemName;
    }


    public ExtendedNodeType[] getSupertypes() {
        Set<ExtendedNodeType> l = new LinkedHashSet<ExtendedNodeType>();
        boolean primaryFound = false;
        ExtendedNodeType[] d = getDeclaredSupertypes();
        for (int i = 0; i < d.length; i++) {
            ExtendedNodeType s = d[i];
            if (s != null) {
                l.addAll(Arrays.asList(s.getSupertypes()));
                l.add(s);
                if (!s.isMixin()) {
                    primaryFound = true;
                }
            }
        }
        if (!primaryFound && !Constants.NT_BASE.equals(getName()) && !isMixin) {
            try {
                l.add(registry.getNodeType(Constants.NT_BASE));
            } catch (NoSuchNodeTypeException e) {
                logger.error("No such supertype for "+getName(),e);
            }
        }
        return new ArrayList<ExtendedNodeType>(l).toArray(new ExtendedNodeType[l.size()]);
    }

    public ExtendedNodeType[] getPrimarySupertypes() {
        List<ExtendedNodeType> l = new ArrayList<ExtendedNodeType>();
        boolean primaryFound = false;
        ExtendedNodeType[] d = getDeclaredSupertypes();
        for (int i = 0; i < d.length; i++) {
            ExtendedNodeType s = d[i];
            if (s != null && !s.isMixin()) {
                l.add(s);
                l.addAll(Arrays.asList(s.getPrimarySupertypes()));
                primaryFound = true;
            }
        }
        if (!primaryFound && !Constants.NT_BASE.equals(name.toString()) && !isMixin) {
            try {
                l.add(registry.getNodeType(Constants.NT_BASE));
            } catch (NoSuchNodeTypeException e) {
                logger.error("No such supertype for "+getName(),e);
            }
        }
        return l.toArray(new ExtendedNodeType[l.size()]);
    }

    public ExtendedNodeType[] getDeclaredSupertypes() {
        return declaredSupertypes;
    }

    public void setDeclaredSupertypes(String[] declaredSupertypes) {
        Arrays.sort(declaredSupertypes);
        this.declaredSupertypeNames = declaredSupertypes;
    }


    void validate() throws NoSuchNodeTypeException {
        this.declaredSupertypes = new ExtendedNodeType[declaredSupertypeNames.length];
        for (int i = 0; i < declaredSupertypes.length; i++) {
            final ExtendedNodeType nodeType = registry.getNodeType(declaredSupertypeNames[i]);
            if (!nodeType.isMixin && i>0) {
                System.arraycopy(this.declaredSupertypes, 0, this.declaredSupertypes, 1, i);
                this.declaredSupertypes[0] = nodeType;
            } else {
                this.declaredSupertypes[i] = nodeType;
            }
            nodeType.addSubType(this);
        }
        for (String s : mixinExtendNames) {
            final ExtendedNodeType type = registry.getNodeType(s);
            registry.addMixinExtension(this, type);
            mixinExtend.add(type);
        }
        for (ExtendedItemDefinition itemDefinition : items) {
            if (itemDefinition.getItemType() != null) {
                registry.addTypedItem(itemDefinition);
            }
        }
    }

    void addSubType(ExtendedNodeType subType) {
        if (!declaredSubtypes.contains(subType)) {
            declaredSubtypes.add(subType);
        }
    }

    public NodeTypeIterator getDeclaredSubtypes() {
        return new NodeTypeIteratorImpl(declaredSubtypes.iterator(), declaredSubtypes.size());
    }

    public String[] getDeclaredSupertypeNames() {
        return declaredSupertypeNames;
    }


    public NodeTypeIterator getSubtypes() {
        List<ExtendedNodeType> l = new ArrayList<ExtendedNodeType>();
        for (Iterator<ExtendedNodeType> iterator = declaredSubtypes.iterator(); iterator.hasNext();) {
            ExtendedNodeType s =  iterator.next();
            l.add(s);
            NodeTypeIterator subtypes = s.getSubtypes();
            while (subtypes.hasNext()) {
                l.add((ExtendedNodeType) subtypes.next());
            }
        }
        return new NodeTypeIteratorImpl(l.iterator(), l.size());
    }

    public List<ExtendedNodeType> getSubtypesAsList() {
        List<ExtendedNodeType> l = new ArrayList<ExtendedNodeType>();
        for (Iterator<ExtendedNodeType> iterator = declaredSubtypes.iterator(); iterator.hasNext();) {
            ExtendedNodeType s =  iterator.next();
            l.add(s);
            NodeTypeIterator subtypes = s.getSubtypes();
            while (subtypes.hasNext()) {
                l.add((ExtendedNodeType) subtypes.next());
            }
        }
        return l;
    }

    public ExtendedNodeType[] getMixinSubtypes() {
        List<ExtendedNodeType> l = new ArrayList<ExtendedNodeType>();
        for (Iterator<ExtendedNodeType> iterator = declaredSubtypes.iterator(); iterator.hasNext();) {
            ExtendedNodeType s =  iterator.next();
            if (s.isMixin()) {
                l.add(s);
                l.addAll(Arrays.asList(s.getMixinSubtypes()));
            }
        }
        return l.toArray(new ExtendedNodeType[l.size()]);
    }

    public boolean isNodeType(String typeName) {
        if (getName().equals(typeName) || Constants.NT_BASE.equals(typeName)) {
            return true;
        }
        ExtendedNodeType[] d = getDeclaredSupertypes();
        for (int i = 0; i < d.length; i++) {
            ExtendedNodeType s = d[i];
            if (s.isNodeType(typeName)) {
                return true;
            }
        }
        return false;
    }

    public List<ExtendedItemDefinition>  getItems() {
        List<ExtendedItemDefinition> l = new ArrayList<ExtendedItemDefinition>();
        l.addAll(getChildNodeDefinitionsAsMap().values());
        l.addAll(getPropertyDefinitionsAsMap().values());
        return l;
    }

    public List<ExtendedItemDefinition>  getDeclaredItems() {
        getPropertyDefinitionsAsMap();
        getChildNodeDefinitionsAsMap();
        return Collections.unmodifiableList(items);
    }

    public synchronized Map<String, ExtendedPropertyDefinition> getPropertyDefinitionsAsMap() {
        if (allProperties == null) {
            allProperties = new LinkedHashMap<String, ExtendedPropertyDefinition>();

            allProperties.putAll(properties);

            ExtendedNodeType[] supertypes = getSupertypes();
            for (int i = supertypes.length-1; i >=0 ; i--) {
                ExtendedNodeType nodeType = supertypes[i];
                Map<String, ExtendedPropertyDefinition> c = new HashMap<String, ExtendedPropertyDefinition>(nodeType.getDeclaredPropertyDefinitionsAsMap());
                Map<String, ExtendedPropertyDefinition> over = new HashMap<String, ExtendedPropertyDefinition>(allProperties);
                over.keySet().retainAll(c.keySet());
                for (ExtendedPropertyDefinition s : over.values()) {
                    s.setOverride(true);
                }
                c.keySet().removeAll(over.keySet());
                allProperties.putAll(c);
            }
        }

        return Collections.unmodifiableMap(allProperties);
    }

    public Map<Integer,ExtendedPropertyDefinition> getUnstructuredPropertyDefinitions() {
        if (allUnstructuredProperties == null) {
            allUnstructuredProperties = new LinkedHashMap<Integer,ExtendedPropertyDefinition>();

            allUnstructuredProperties.putAll(unstructuredProperties);

            ExtendedNodeType[] supertypes = getSupertypes();
            for (int i = supertypes.length-1; i >=0 ; i--) {
                ExtendedNodeType nodeType = supertypes[i];
                Map<Integer,ExtendedPropertyDefinition> c = new HashMap<Integer,ExtendedPropertyDefinition>(nodeType.getDeclaredUnstructuredPropertyDefinitions());
                Map<Integer,ExtendedPropertyDefinition> over = new HashMap<Integer,ExtendedPropertyDefinition>(allUnstructuredProperties);
                over.keySet().retainAll(c.keySet());
                for (ExtendedPropertyDefinition s : over.values()) {
                    s.setOverride(true);
                }
                c.keySet().removeAll(over.keySet());
                allUnstructuredProperties.putAll(c);
            }
        }
        return Collections.unmodifiableMap(allUnstructuredProperties);
    }

    public ExtendedPropertyDefinition[] getPropertyDefinitions() {
        Collection<ExtendedPropertyDefinition> c = new ArrayList<ExtendedPropertyDefinition>(getPropertyDefinitionsAsMap().values());
        c.addAll(getUnstructuredPropertyDefinitions().values());
        return c.toArray(new ExtendedPropertyDefinition[c.size()]);
    }

    public Map<String, ExtendedPropertyDefinition> getDeclaredPropertyDefinitionsAsMap() {
        getPropertyDefinitionsAsMap();
        return properties;
    }

    public Map<Integer,ExtendedPropertyDefinition> getDeclaredUnstructuredPropertyDefinitions() {
        getUnstructuredPropertyDefinitions();
        return unstructuredProperties;
    }

    public ExtendedPropertyDefinition[] getDeclaredPropertyDefinitions() {
        Collection<ExtendedPropertyDefinition> c = new ArrayList<ExtendedPropertyDefinition>(getDeclaredPropertyDefinitionsAsMap().values());
        c.addAll(getDeclaredUnstructuredPropertyDefinitions().values());
        return c.toArray(new ExtendedPropertyDefinition[c.size()]);
    }

    public Map<String, ExtendedNodeDefinition> getChildNodeDefinitionsAsMap() {
        if (allNodes == null) {
            allNodes = new LinkedHashMap<String, ExtendedNodeDefinition>();
            ExtendedNodeType[] supertypes = getSupertypes();
            for (int i = supertypes.length-1; i >=0 ; i--) {
                ExtendedNodeType nodeType = supertypes[i];
                Map<String, ExtendedNodeDefinition> c = new HashMap<String, ExtendedNodeDefinition>(nodeType.getDeclaredChildNodeDefinitionsAsMap());
                Map<String, ExtendedNodeDefinition> over = new HashMap<String, ExtendedNodeDefinition>(allNodes);
                over.keySet().retainAll(c.keySet());
                for (ExtendedNodeDefinition s : over.values()) {
                    s.setOverride(true);
                }
                c.keySet().removeAll(over.keySet());
                allNodes.putAll(c);
            }

            allNodes.putAll(nodes);
        }

        return Collections.unmodifiableMap(allNodes);
    }

    public Map<String,ExtendedNodeDefinition> getUnstructuredChildNodeDefinitions() {
        if (allUnstructuredNodes == null) {
            allUnstructuredNodes = new LinkedHashMap<String,ExtendedNodeDefinition>();
            allUnstructuredNodes.putAll(unstructuredNodes);

            ExtendedNodeType[] supertypes = getSupertypes();
            for (int i = supertypes.length-1; i >=0 ; i--) {
                ExtendedNodeType nodeType = supertypes[i];
                Map<String,ExtendedNodeDefinition> c = new HashMap<String,ExtendedNodeDefinition>(nodeType.getDeclaredUnstructuredChildNodeDefinitions());
                Map<String,ExtendedNodeDefinition> over = new HashMap<String,ExtendedNodeDefinition>(allUnstructuredNodes);
                over.keySet().retainAll(c.keySet());
                for (ExtendedNodeDefinition s : over.values()) {
                    s.setOverride(true);
                }
                c.keySet().removeAll(over.keySet());
                allUnstructuredNodes.putAll(c);
            }
        }
        return Collections.unmodifiableMap(allUnstructuredNodes);
    }

    public ExtendedNodeDefinition[] getChildNodeDefinitions() {
        Collection<ExtendedNodeDefinition> c = new ArrayList<ExtendedNodeDefinition>(getChildNodeDefinitionsAsMap().values());
        c.addAll(getUnstructuredChildNodeDefinitions().values());
        return c.toArray(new ExtendedNodeDefinition[c.size()]);
    }

    public Map<String, ExtendedNodeDefinition> getDeclaredChildNodeDefinitionsAsMap() {
        getChildNodeDefinitionsAsMap();
        return nodes;
    }

    public Map<String,ExtendedNodeDefinition> getDeclaredUnstructuredChildNodeDefinitions() {
        getUnstructuredChildNodeDefinitions();
        return unstructuredNodes;
    }

    public ExtendedNodeDefinition[] getDeclaredChildNodeDefinitions() {
        Collection<ExtendedNodeDefinition> c = new ArrayList<ExtendedNodeDefinition>(getDeclaredChildNodeDefinitionsAsMap().values());
        c.addAll(getDeclaredUnstructuredChildNodeDefinitions().values());
        return c.toArray(new ExtendedNodeDefinition[c.size()]);
    }

    public List<String> getGroupedItems() {
        return groupedItems;
    }

    public void setGroupedItems(List<String> groupedItems) {
        this.groupedItems = groupedItems;
    }

    public boolean canSetProperty(String propertyName, Value value) {
        if (value == null) {
            // setting a property to null is equivalent of removing it
            return canRemoveItem(propertyName);
        }
        try {
            ExtendedPropertyDefinition def = getPropertyDefinitionsAsMap()
                    .containsKey(propertyName) ? getPropertyDefinitionsAsMap().get(propertyName)
                    : getMatchingPropDef(getUnstructuredPropertyDefinitions().values(),
                            value.getType(), false);
            if (def == null) {
                def = getMatchingPropDef(getUnstructuredPropertyDefinitions().values(),
                        PropertyType.UNDEFINED, false);
            }
            if (def != null) {
                if (def.isMultiple() || def.isProtected()) {
                    return false;
                }
                int targetType;
                if (def.getRequiredType() != PropertyType.UNDEFINED
                        && def.getRequiredType() != value.getType()) {
                    // type conversion required
                    targetType = def.getRequiredType();
                } else {
                    // no type conversion required
                    targetType = value.getType();
                }
                // perform type conversion as necessary and create InternalValue
                // from (converted) Value
                InternalValue internalValue = null;
                if (targetType != value.getType()) {
                    // type conversion required, but Jahia cannot do it, because we have no valueFactory, resolver, store or session object here
                    // Value targetVal = ValueHelper.convert(
                    // value, targetType,
                    // valueFactory);
                    if (value.getType() != PropertyType.BINARY
                            && !((value.getType() == PropertyType.PATH || value.getType() == PropertyType.NAME) && !(value instanceof QValueValue))) {
                        internalValue = InternalValue.create(value, null, null);
                    }
                } else {
                    // no type conversion required
                    if (value.getType() != PropertyType.BINARY
                            && !((value.getType() == PropertyType.PATH || value.getType() == PropertyType.NAME) && !(value instanceof QValueValue))) {
                        internalValue = InternalValue.create(value, null, null);
                    }
                }
                if (internalValue != null) {
                    checkSetPropertyValueConstraints(def, new InternalValue[] { internalValue });
                }
                return true;
            }
        } catch (RepositoryException e) {
            // fall through
        }
        return false;
    }

    public boolean canSetProperty(String propertyName, Value[] values) {
        if (values == null) {
            // setting a property to null is equivalent of removing it
            return canRemoveItem(propertyName);
        }
        try {
            // determine type of values
            int type = PropertyType.UNDEFINED;
            for (Value value : values) {
                if (value == null) {
                    // skip null values as those would be purged
                    continue;
                }
                if (type == PropertyType.UNDEFINED) {
                    type = value.getType();
                } else if (type != value.getType()) {
                    // inhomogeneous types
                    return false;
                }
            }
            ExtendedPropertyDefinition def = getPropertyDefinitionsAsMap()
                    .containsKey(propertyName) ? getPropertyDefinitionsAsMap().get(propertyName)
                    : getMatchingPropDef(getUnstructuredPropertyDefinitions().values(), type, true);
            if (def == null) {
                def = getMatchingPropDef(getUnstructuredPropertyDefinitions().values(),
                        PropertyType.UNDEFINED, true);
            }
            if (def != null) {
                if (!def.isMultiple() || def.isProtected()) {
                    return false;
                }
                int targetType;
                if (def.getRequiredType() != PropertyType.UNDEFINED
                        && def.getRequiredType() != type) {
                    // type conversion required, but Jahia cannot do it, because we have no valueFactory, resolver, store or session object here
                    targetType = def.getRequiredType();
                } else {
                    // no type conversion required
                    targetType = type;
                }
                List<InternalValue> list = new ArrayList<InternalValue>();
                for (Value value : values) {
                    if (value != null) {
                        // perform type conversion as necessary and create InternalValue
                        // from (converted) Value
                        InternalValue internalValue = null;
                        if (targetType != value.getType()) {
                            // type conversion required
                            // Value targetVal = ValueHelper.convert(
                            // value, targetType,
                            // valueFactory);
                            if (value.getType() != PropertyType.BINARY
                                    && !((value.getType() == PropertyType.PATH || value.getType() == PropertyType.NAME) && !(value instanceof QValueValue))) {
                                internalValue = InternalValue.create(value, null, null);
                            }
                        } else {
                            // no type conversion required
                            if (value.getType() != PropertyType.BINARY
                                    && !((value.getType() == PropertyType.PATH || value.getType() == PropertyType.NAME) && !(value instanceof QValueValue))) {
                                internalValue = InternalValue.create(value, null, null);
                            }
                        }
                        list.add(internalValue);
                    }
                }
                if (!list.isEmpty()) {
                    InternalValue[] internalValues = list.toArray(new InternalValue[list.size()]);
                    checkSetPropertyValueConstraints(def, internalValues);
                }
                return true;
            }
        } catch (RepositoryException e) {
            // fall through
        }
        return false;
    }

    public boolean canAddChildNode(String childNodeName) {
        if (getChildNodeDefinitionsAsMap().containsKey(childNodeName)) {
            if (getChildNodeDefinitionsAsMap().get(childNodeName).getDefaultPrimaryType() != null) {
                return true;
            }
        }
        return false;
    }

    public boolean canAddChildNode(String childNodeName, String nodeTypeName) {
        try {
            ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(nodeTypeName);
            if (!nt.isAbstract() && !nt.isMixin()) {
                if (getChildNodeDefinitionsAsMap().containsKey(childNodeName)) {
                    if (canAddChildNode(nt,getChildNodeDefinitionsAsMap().get(childNodeName)))  {
                        return true;
                    }
                }
                Collection<ExtendedNodeDefinition> unstruct = getUnstructuredChildNodeDefinitions().values();
                for (ExtendedNodeDefinition definition : unstruct) {
                    if (canAddChildNode(nt,definition))  {
                        return true;
                    }
                }
            }
        } catch (RepositoryException e) {
            // fall through
        }
        return false;
    }
    
    private boolean canAddChildNode(ExtendedNodeType nt, ExtendedNodeDefinition nodeDef) {
        String[] epd = nodeDef.getRequiredPrimaryTypeNames();
        for (String s : epd) {
            if (!nt.isNodeType(s)) {
                return false;
            }
        }
        return true;
    }    

    public boolean canRemoveItem(String s) {
        try {
            checkRemoveItemConstraints(s);
            return true;
        } catch (RepositoryException re) {
            // fall through
        }
        return false;
    }

    public boolean canRemoveNode(String nodeName) {
        try {
            checkRemoveNodeConstraints(nodeName);
            return true;
        } catch (RepositoryException re) {
            // fall through
        }
        return true;
    }

    public boolean canRemoveProperty(String propertyName) {
        try {
            checkRemovePropertyConstraints(propertyName);
            return true;
        } catch (RepositoryException re) {
            // fall through
        }
        return false;
    }

    void setPropertyDefinition(String name, ExtendedPropertyDefinition p) {
        if (name.equals("*")) {
            if (p.isMultiple()) {
                unstructuredProperties.put(256 + p.getRequiredType(), p);
            } else {
                unstructuredProperties.put(p.getRequiredType(), p);
            }
        } else {
            properties.put(name, p);
        }
        items.add(p);
    }

    public ExtendedPropertyDefinition getPropertyDefinition(String name) {
        return properties.get(name);
    }

    void setNodeDefinition(String name, ExtendedNodeDefinition p) {
        if (name.equals("*")) {
            StringBuffer s = new StringBuffer("");
            if (p.getRequiredPrimaryTypeNames() == null) {
                logger.error("Required primary type names is null for extended node definition " + p);
            }
            for (String s1 : p.getRequiredPrimaryTypeNames()) {
                s.append(s1).append(" ");
            }
            unstructuredNodes.put(s.toString().trim(), p);
        } else {
            nodes.put(name, p);
        }
        items.add(p);
    }

    public ExtendedNodeDefinition getNodeDefinition(String name) {
        return nodes.get(name);
    }

    public String getValidator() {
        return validator;
    }

    public void setValidator(String validator) {
        this.validator = validator;
    }

    public String getItemsType() {
        return itemsType;
    }

    public void setItemsType(String itemsType) {
        this.itemsType = itemsType;
    }

    public void addMixinExtend(String mixinExtension) {
        this.mixinExtendNames.add(mixinExtension);
    }

    public List<ExtendedNodeType> getMixinExtends() {
        return mixinExtend;
    }

    protected JahiaTemplatesPackage getTemplatePackage() {
        JahiaTemplatesPackage pkg = null;
        if (!getSystemId().startsWith("system-")) {
            try {
                pkg = ServicesRegistry.getInstance()
                        .getJahiaTemplateManagerService().getTemplatePackage(
                                getSystemId());
            } catch (Exception e) {
                logger.warn(
                        "Unable to get the template package for the node with system id '"
                                + getSystemId() + "'", e);
            }
        }

        return pkg;
    }

    protected String getResourceBundleId() {
        JahiaTemplatesPackage pkg = getTemplatePackage();
        return pkg != null ? "modules." + pkg.getRootFolder() + "." + pkg.getResourceBundleName() : "JahiaTypesResources";
    }

    public String getLabel(Locale locale) {
        String label = labels.get(locale);
        if (label == null) {
            String key = getName().replace(':', '_');
            String tpl = getTemplatePackage() != null ? getTemplatePackage().getName() : null;
            label = new JahiaResourceBundle(getResourceBundleId(), locale, tpl, JahiaTemplatesRBLoader
                    .getInstance(Thread.currentThread().getContextClassLoader(), tpl)).getString(key, key);
            labels.put(locale, label);
        }
        return label;
    }

    public NodeTypeDefinition getNodeTypeDefinition() {
        return new Definition();
    }

    public String getLocalName() {
         return this.name.getLocalName();
    }

    public String getPrefix() {
        return this.name.getPrefix();
    }

    class Definition implements NodeTypeDefinition {
        public String getName() {
            return name.toString();
        }

        public String[] getDeclaredSupertypeNames() {
            String[] d = declaredSupertypeNames;

            ExtendedPropertyDefinition[] defs = ExtendedNodeType.this.getDeclaredPropertyDefinitions();
            for (ExtendedPropertyDefinition def : defs) {
                if (def.isInternationalized()) {
                    String[] newRes = new String[d.length+1];
                    System.arraycopy(d, 0, newRes, 0, d.length);
                    newRes[d.length] = "jmix:i18n";
                    return newRes;
                }
            }

            return d;
        }

        public boolean isAbstract() {
            return isAbstract;
        }

        public boolean isMixin() {
            return isMixin;
        }

        public boolean hasOrderableChildNodes() {
            return hasOrderableChildNodes;
        }

        public boolean isQueryable() {
            return true;
        }

        public String getPrimaryItemName() {
            return primaryItemName;
        }

        public PropertyDefinition[] getDeclaredPropertyDefinitions() {
            ExtendedPropertyDefinition[] defs = ExtendedNodeType.this.getDeclaredPropertyDefinitions();
            List<PropertyDefinition> r = new ArrayList<PropertyDefinition>();
            for (final ExtendedPropertyDefinition def : defs) {
                if (!def.isInternationalized() && !def.isOverride()) {
                    r.add(new PropertyDefinition() {
                        public int getRequiredType() {
                            return def.getRequiredType();
                        }

                        public String[] getValueConstraints() {
                            return def.getValueConstraints();
                        }

                        public Value[] getDefaultValues() {
                            return def.getDefaultValues();
                        }

                        public boolean isMultiple() {
                            return def.isMultiple();
                        }

                        public String[] getAvailableQueryOperators() {
                            return def.getAvailableQueryOperators();
                        }

                        public boolean isFullTextSearchable() {
                            return def.isFullTextSearchable();
                        }

                        public boolean isQueryOrderable() {
                            return def.isQueryOrderable();
                        }

                        public NodeType getDeclaringNodeType() {
                            return def.getDeclaringNodeType();
                        }

                        public String getName() {
                            return def.getName();
                        }

                        public boolean isAutoCreated() {
                            return def.isAutoCreated();
                        }

                        public boolean isMandatory() {
                            return def.isMandatory();
                        }

                        public int getOnParentVersion() {
                            return def.getOnParentVersion();
                        }

                        public boolean isProtected() {
                            return false;
                        }
                    });
                }
            }
            return r.toArray(new PropertyDefinition[r.size()]);
        }

        public NodeDefinition[] getDeclaredChildNodeDefinitions() {
            ExtendedNodeDefinition[] defs = ExtendedNodeType.this.getDeclaredChildNodeDefinitions();


            List<NodeDefinition> r = new ArrayList<NodeDefinition>();
            for (final ExtendedNodeDefinition def : defs) {
                if (!def.isOverride()) {
                    r.add(new NodeDefinition() {
                        public NodeType[] getRequiredPrimaryTypes() {
                            return def.getRequiredPrimaryTypes();
                        }

                        public String[] getRequiredPrimaryTypeNames() {
                            return def.getRequiredPrimaryTypeNames();
                        }

                        public NodeType getDefaultPrimaryType() {
                            return def.getDefaultPrimaryType();
                        }

                        public String getDefaultPrimaryTypeName() {
                            return def.getDefaultPrimaryTypeName();
                        }

                        public boolean allowsSameNameSiblings() {
                            return def.allowsSameNameSiblings();
                        }

                        public NodeType getDeclaringNodeType() {
                            return def.getDeclaringNodeType();
                        }

                        public String getName() {
                            return def.getName();
                        }

                        public boolean isAutoCreated() {
                            return false;
                        }

                        public boolean isMandatory() {
                            return !def.isAutoCreated() && def.isMandatory();
                        }

                        public int getOnParentVersion() {
                            return def.getOnParentVersion();
                        }

                        public boolean isProtected() {
                            return false;
                        }
                    });
                }
            }
            return r.toArray(new NodeDefinition[r.size()]);
        }
    }
    
    /**
     * @param name
     * @throws ConstraintViolationException
     */
    private void checkRemoveItemConstraints(String s) throws ConstraintViolationException {
        ExtendedItemDefinition def = getPropertyDefinitionsAsMap().get(name);
        if (def == null) {
            def = getChildNodeDefinitionsAsMap().get(name);
        }
        if (def != null) {
            if (def.isMandatory()) {
                throw new ConstraintViolationException("can't remove mandatory item");
            }
            if (def.isProtected()) {
                throw new ConstraintViolationException("can't remove protected item");
            }
        }
    }

    /**
     * @param name
     * @throws ConstraintViolationException
     */
    private void checkRemoveNodeConstraints(String name) throws ConstraintViolationException {
        ExtendedNodeDefinition def = getChildNodeDefinitionsAsMap().get(name);
        if (def != null) {
                if (def.isMandatory()) {
                    throw new ConstraintViolationException("can't remove mandatory node");
                }
                if (def.isProtected()) {
                    throw new ConstraintViolationException("can't remove protected node");
                }
        }
    }

    /**
     * @param name
     * @throws ConstraintViolationException
     */
    private void checkRemovePropertyConstraints(String propertyName)
            throws ConstraintViolationException {
        ExtendedPropertyDefinition def = getPropertyDefinitionsAsMap().get(propertyName);
        if (def != null) {
            if (def.isMandatory()) {
                throw new ConstraintViolationException("can't remove mandatory property");
            }
            if (def.isProtected()) {
                throw new ConstraintViolationException("can't remove protected property");
            }
        }
    }    
    
    private ExtendedPropertyDefinition getMatchingPropDef(Collection<ExtendedPropertyDefinition> defs, int type) {
        ExtendedPropertyDefinition match = null;
        for (ExtendedPropertyDefinition pd : defs) {
            int reqType = pd.getRequiredType();
            // match type
            if (reqType == PropertyType.UNDEFINED
                    || type == PropertyType.UNDEFINED
                    || reqType == type) {
                if (match == null) {
                    match = pd;
                } else {
                    // check if this definition is a better match than
                    // the one we've already got
                    if (match.getRequiredType() != pd.getRequiredType()) {
                        if (match.getRequiredType() == PropertyType.UNDEFINED) {
                            // found better match
                            match = pd;
                        }
                    } else {
                        if (match.isMultiple() && !pd.isMultiple()) {
                            // found better match
                            match = pd;
                        }
                    }
                }
                if (match.getRequiredType() != PropertyType.UNDEFINED
                        && !match.isMultiple()) {
                    // found best possible match, get outta here
                    return match;
                }
            }
        }
        return match;
    }    

    private ExtendedPropertyDefinition getMatchingPropDef(Collection<ExtendedPropertyDefinition> defs, int type,
            boolean multiValued) {
        ExtendedPropertyDefinition match = null;
        for (ExtendedPropertyDefinition pd : defs) {
            int reqType = pd.getRequiredType();
            // match type
            if (reqType == PropertyType.UNDEFINED || type == PropertyType.UNDEFINED
                    || reqType == type) {
                // match multiValued flag
                if (multiValued == pd.isMultiple()) {
                    // found match
                    if (pd.getRequiredType() != PropertyType.UNDEFINED) {
                        // found best possible match, get outta here
                        return pd;
                    } else {
                        if (match == null) {
                            match = pd;
                        }
                    }
                }
            }
        }
        return match;
    }
    
    private ExtendedNodeDefinition getMatchingNodeDef(String name, String nodeTypeName)
            throws NoSuchNodeTypeException, ConstraintViolationException {
        ExtendedNodeType entTarget = null;
        if (nodeTypeName != null) {
            entTarget = NodeTypeRegistry.getInstance().getNodeType(nodeTypeName);
        }

        // try named node definitions first
        ExtendedNodeDefinition def = getChildNodeDefinitionsAsMap().get(name);
            if (def != null) {
                String[] types = def.getRequiredPrimaryTypeNames();
                // node definition with that name exists
                if (entTarget != null && types != null) {
                    // check 'required primary types' constraint
                    if (includesNodeTypes(entTarget.getChildNodeDefinitionsAsMap().keySet(), types)) {
                        // found named node definition
                        return def;
                    }
                } else if (def.getDefaultPrimaryType() != null) {
                    // found node definition with default node type
                    return def;
                }
            }
        

        // no item with that name defined;
        // try residual node definitions
        Collection<ExtendedNodeDefinition> nda = getUnstructuredChildNodeDefinitions().values();
        for (ExtendedNodeDefinition nd : nda) {
            if (entTarget != null && nd.getRequiredPrimaryTypes() != null) {
                // check 'required primary types' constraint
                if (!includesNodeTypes(entTarget.getChildNodeDefinitionsAsMap().keySet(), nd.getRequiredPrimaryTypeNames())) {
                    continue;
                }
                // found residual node definition
                return nd;
            } else {
                // since no node type has been specified for the new node,
                // it must be determined from the default node type;
                if (nd.getDefaultPrimaryType() != null) {
                    // found residual node definition with default node type
                    return nd;
                }
            }
        }

        // no applicable definition found
        throw new ConstraintViolationException("no matching child node definition found for " + name);
    }
    
    /**
     * Tests if the value constraints defined in the property definition
     * <code>pd</code> are satisfied by the the specified <code>values</code>.
     * <p/>
     * Note that the <i>protected</i> flag is not checked. Also note that no
     * type conversions are attempted if the type of the given values does not
     * match the required type as specified in the given definition.
     */
    private static void checkSetPropertyValueConstraints(ExtendedPropertyDefinition pd,
                                                        InternalValue[] values)
            throws ConstraintViolationException, RepositoryException {
        // check multi-value flag
        if (!pd.isMultiple() && values != null && values.length > 1) {
            throw new ConstraintViolationException("the property is not multi-valued");
        }

        ValueConstraint[] constraints = pd.getValueConstraintObjects();
        if (constraints == null || constraints.length == 0) {
            // no constraints to check
            return;
        }
        if (values != null && values.length > 0) {
            // check value constraints on every value
            for (InternalValue value : values) {
                // constraints are OR-ed together
                boolean satisfied = false;
                ConstraintViolationException cve = null;
                for (ValueConstraint constraint : constraints) {
                    try {
                        constraint.check(value);
                        satisfied = true;
                        break;
                    } catch (ConstraintViolationException e) {
                        cve = e;
                    }
                }
                if (!satisfied) {
                    // re-throw last exception we encountered
                    throw cve;
                }
            }
        }
    }    
    private boolean includesNodeTypes(Set<String> allNodeTypes, String[] nodeTypeNames) {
        return allNodeTypes.containsAll(Arrays.asList(nodeTypeNames));
    }
}
