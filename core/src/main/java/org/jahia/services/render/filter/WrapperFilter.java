package org.jahia.services.render.filter;

import org.jahia.services.render.*;
import org.jahia.services.content.JCRNodeWrapper;
import org.apache.log4j.Logger;

import javax.jcr.RepositoryException;

/**
 * WrapperFilter
 *
 * Looks for all registered wrappers in the resource and calls the associated scripts around the output.
 * Output is made available to the wrapper script through the "wrappedContent" request attribute.
 *
 */
public class WrapperFilter extends AbstractFilter {
    private static Logger logger = Logger.getLogger(WrapperFilter.class);

    private String wrapper;

    public void setWrapper(String wrapper) {
        this.wrapper = wrapper;
    }

    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        if (renderContext.isAjaxRequest()) {
            return previousOut;
        }
        JCRNodeWrapper node = resource.getNode();
        if (wrapper == null) {
            while (resource.hasWrapper()) {
                String wrapper = resource.popWrapper();
                previousOut = wrap(renderContext, resource, previousOut, node, wrapper);
            }
        } else {
            previousOut = wrap(renderContext, resource, previousOut, node, wrapper);
        }
        return previousOut;
    }

    private String wrap(RenderContext renderContext, Resource resource, String output, JCRNodeWrapper node,
                        String wrapper) throws RepositoryException {
        try {
//                renderContext.getRequest().setAttribute("wrappedResource", resource);
            Resource wrapperResource = new Resource(node, resource.getTemplateType().equals("edit")?"html":resource.getTemplateType(),
                    wrapper,
                    Resource.CONFIGURATION_WRAPPER);
            if (service.hasTemplate(node, wrapper)) {
                Object wrappedContent = renderContext.getRequest().getAttribute("wrappedContent");
                try {
                    renderContext.getRequest().setAttribute("wrappedContent", output);
                    output = RenderService.getInstance().render(wrapperResource, renderContext);
                } finally {
                    renderContext.getRequest().setAttribute("wrappedContent", wrappedContent);
                }
            } else {
                logger.warn("Cannot get wrapper "+wrapper);
            }
        } catch (TemplateNotFoundException e) {
            logger.debug("Cannot find wrapper "+wrapper,e);
        } catch (RenderException e) {
            logger.error("Cannot execute wrapper "+wrapper,e);
        }
        return output;
    }
}
