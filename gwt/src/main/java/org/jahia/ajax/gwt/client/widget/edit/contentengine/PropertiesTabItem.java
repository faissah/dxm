package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.form.Field;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.util.definition.FormFieldCreator;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 6, 2010
 * Time: 6:34:40 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class PropertiesTabItem extends EditEngineTabItem {
    protected PropertiesEditor propertiesEditor;
    protected Map<String, PropertiesEditor> langPropertiesEditorMap;
    protected String dataType;
    protected List<String> excludedTypes;
    protected boolean multiLang = false;


    protected PropertiesTabItem(String title, AbstractContentEngine engine, String dataType) {
        super(title, engine);
        this.dataType = dataType;
        langPropertiesEditorMap = new HashMap<String, PropertiesEditor>();
    }

    /**
     * Get properties editor of the default lang
     *
     * @return
     */
    public PropertiesEditor getPropertiesEditor() {
        return propertiesEditor;
    }

    /**
     * Get properties editor by langCode
     *
     * @param locale
     * @return
     */
    public PropertiesEditor getPropertiesEditorByLang(GWTJahiaLanguage locale) {
        if (locale == null) {
            Log.error("Locale is null");
            return null;
        }
        return langPropertiesEditorMap.get(locale.getCountryIsoCode());
    }

    /**
     * set properties editor by lang
     *
     * @param locale
     */
    private void setPropertiesEditorByLang(GWTJahiaLanguage locale) {
        if (langPropertiesEditorMap == null || locale == null) {
            return;
        }
        langPropertiesEditorMap.put(locale.getCountryIsoCode(), propertiesEditor);
    }

    @Override
    public void create(GWTJahiaLanguage locale) {
        // do not re-process the view if it's already done and the tabItem is not multilang
        if (!isMultiLang() && isProcessed()) {
            return;
        }
        if (engine.getMixin() != null) {
            boolean addSharedLangLabel = true;
            List<GWTJahiaNodeProperty> previousNon18nProperties = null;

            if (propertiesEditor != null) {
                addSharedLangLabel = false;
                propertiesEditor.setVisible(false);
                // keep tarck of the old values
                previousNon18nProperties = propertiesEditor.getProperties(false, true);
            }
            if (!isMultiLang()) {
                setProcessed(true);
            }
            propertiesEditor = getPropertiesEditorByLang(locale);

            if (propertiesEditor == null) {
                if (engine.isExistingNode() && engine.getNode().getNodeTypes().contains("jmix:shareable")) {
                    // this label is shared among languages.
                    if (addSharedLangLabel) {
                        Label label = new Label("Important : This is a shared node, editing it will modify its value for all its usages");
                        label.setStyleAttribute("color", "rgb(200,80,80)");
                        label.setStyleAttribute("font-size", "14px");
                        add(label);
                    }
                }

                propertiesEditor = new PropertiesEditor(engine.getNodeTypes(), engine.getMixin(), engine.getProperties(), false, true, dataType, null, excludedTypes, !engine.isExistingNode() || engine.getNode().isWriteable(), true);

                setPropertiesEditorByLang(locale);

                postCreate();

            }

            // synch non18n properties
            if (isMultiLang()) {
                if (previousNon18nProperties != null && !previousNon18nProperties.isEmpty()) {
                    Map<String, Field<?>> fieldsMap = propertiesEditor.getFieldsMap();
                    for (GWTJahiaNodeProperty property : previousNon18nProperties) {
                        FormFieldCreator.fillValue(fieldsMap.get(property.getName()), propertiesEditor.getGWTJahiaItemDefinition(property), property);
                    }
                }
            }

            propertiesEditor.setVisible(true);

            layout();
        }
    }


    /**
     * call after created
     */
    public void postCreate() {
        add(propertiesEditor);
    }

    public boolean isMultiLang() {
        return multiLang;
    }

    public void setMultiLang(boolean multiLang) {
        this.multiLang = multiLang;
    }


    /**
     * Get lang properties per map
     *
     * @return
     */
    public Map<String, List<GWTJahiaNodeProperty>> getLangPropertiesMap() {
        Map<String, List<GWTJahiaNodeProperty>> mapProperties = new HashMap<String, List<GWTJahiaNodeProperty>>();
        Iterator<String> langCodes = langPropertiesEditorMap.keySet().iterator();
        while (langCodes.hasNext()) {
            String langCode = langCodes.next();
            mapProperties.put(langCode, langPropertiesEditorMap.get(langCode).getProperties(true, false));
        }
        return mapProperties;
    }


}
