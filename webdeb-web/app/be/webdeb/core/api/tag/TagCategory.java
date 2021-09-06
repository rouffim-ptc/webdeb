package be.webdeb.core.api.tag;

import be.webdeb.core.api.argument.ArgumentJustification;
import be.webdeb.core.api.citation.CitationJustification;
import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.debate.Debate;

import java.util.List;

/**
 * This interface represents a tag category in the webdeb system. A category is used in justification link in a context
 * contribution
 *
 * @author Martin Rouffiange
 */
public interface TagCategory extends Tag {

    /**
     * Get the current context contribution id linked to this category
     *
     * @return the context contribution id
     */
    Long getCurrentContextId();

    /**
     * Set the current context contribution id linked to this category
     *
     * @param context a context contribution id
     */
    void setCurrentContextId(Long context);

    /**
     * Get the list of context contribution where this tag is a category, should not be empty
     *
     * @return the list of context contribution where this tag is a category
     */
    List<ContextContribution> getContextContributions();

    /**
     * Get the list of all argument justification links that are in this tag category for the current debate id, if any
     *
     * @return a possibly empty list of argument justification links
     */
    List<ArgumentJustification> getAllArgumentJustificationLinks();

    /**
     * Get the list of all citation justification links that are in this tag category for the current debate id, if any
     *
     * @return a possibly empty list of citation justification links
     */
    List<CitationJustification> getAllCitationJustificationLinks();
}
