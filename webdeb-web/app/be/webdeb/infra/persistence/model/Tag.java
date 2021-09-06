package be.webdeb.infra.persistence.model;

import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.EWarnedWordContextType;
import be.webdeb.core.api.contribution.EWarnedWordType;
import be.webdeb.core.api.debate.EDebateType;
import be.webdeb.core.api.tag.ETagType;
import be.webdeb.core.impl.helper.SearchContainer;
import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The persistent class for the tag database table (formerly folder), conceptual subtype of contribution.
 * Tag regroups contribution behind a common theme. Concrete subtype is specified via the tagtype
 *
 * @author Martin Rouffiange
 * @see ETagType
 */
@Entity
@CacheBeanTuning
@Table(name = "tag")
public class Tag extends WebdebModel {

    private static final org.slf4j.Logger logger = play.Logger.underlying();
    private static final Model.Finder<Long, Tag> find = new Model.Finder<>(Tag.class);

    @Id
    @Column(name = "id_contribution", unique = true, nullable = false)
    private Long idContribution;

    // forcing updates from this object, deletions are handled at the contribution level
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "id_contribution", nullable = false, insertable = false, updatable = false)
    private Contribution contribution;

    @OneToMany(mappedBy = "tag", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<TagI18name> names;

    @OneToMany(mappedBy = "tag", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TagRewordingI18name> rewordingNames;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_type", nullable = false)
    private TTagType tagtype;

    // all parent tags of this tag
    @OneToMany(mappedBy = "tagChild", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<TagLink> parentTags;

    // all child tags of this tag
    @OneToMany(mappedBy = "tagParent", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<TagLink> childTags;

    // all parent tags of this tag as Tag
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
            name="tag_link",
            joinColumns = { @JoinColumn(name="id_tag_to", referencedColumnName = "id_contribution") },
            inverseJoinColumns = { @JoinColumn(name="id_tag_from", referencedColumnName = "id_contribution") }
    )
    private List<Tag> parents;

    // all child tags of this tag as Tag
    @ManyToMany(mappedBy = "parents", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Tag> children;

    // all contributions in this tag
    @ManyToMany(mappedBy = "tags", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Contribution> contributions;

    @Transient
    @Unqueryable
    private Long linkId;

    @Transient
    @Unqueryable
    private Integer nb_contributions;


    /**
     * Get the actor id
     *
     * @return an id
     */
    public Long getIdContribution() {
        return this.idContribution;
    }

    /**
     * Set the actor id
     *
     * @param idContribution an id
     */
    public void setIdContribution(Long idContribution) {
        this.idContribution = idContribution;
    }

    /**
     * Get the list of names for this tag (names may have multiple spellings)
     *
     * @return a list of names
     */
    public List<TagI18name> getNames() {
        return names;
    }

    /**
     * Get the name of the tag (prior in english, first one otherwise)
     *
     * @return a tag name
     */
    public String getDefaultName() {
        String name = "";
        if(names != null && !names.isEmpty()){
            Optional<TagI18name> nameFound = names.stream().filter(n -> "en".equals(n.getLang())).findFirst();
            if(nameFound.isPresent()){
                name = nameFound.get().getName();
            }else{
                name = names.stream().findFirst().get().getName();
            }
        }

        return name;
    }

    /**
     * Set the list of names for this tag (names may have multiple spellings)
     *
     * @param names a list of names to set
     */
    public void setNames(List<TagI18name> names) {
        if (names != null) {
            if (this.names == null) {
                this.names = new ArrayList<>();
            }
            // get previous languages for current names
            List<String> currentlangs = this.names.stream().map(TagI18name::getLang).collect(Collectors.toList());

            // add/update new names
            names.forEach(this::addName);

            currentlangs.stream().filter(lang -> names.stream().noneMatch(n -> n.getLang().equals(lang))).forEach(lang ->
                    this.names.removeIf(current -> current.getLang().equals(lang))
            );
        }
    }

    /**
     * Add a name to this tag, if such language already exists, will update existing name
     *
     * @param name a name structure
     */
    public void addName(TagI18name name) {
        if (names == null) {
            names = new ArrayList<>();
        }
        Optional<TagI18name> match = names.stream().filter(n ->
                n.getLang().equals(name.getLang())).findAny();

        if (!match.isPresent()) {
            names.add(name);
        }else{
            int i = names.lastIndexOf(match.get());
            if(i >= 0){
                names.get(i).setName(name.getName());
            }
        }
    }

    /**
     * Get the list of rewording names for this tag (names may have multiple language and rewording)
     *
     * @return a list of names
     */
    public List<TagRewordingI18name> getRewordingNames() {
        return rewordingNames;
    }

    /**
     * Set the list of names for this tag (names may have multiple language and rewording)
     *
     * @param names a list of names to set
     */
    public void setRewordingNames(List<TagRewordingI18name> names) {
        if (names != null) {
            if (this.rewordingNames == null) {
                this.rewordingNames = new ArrayList<>();
            }

            this.rewordingNames
                    .stream()
                    .filter(n -> names.stream().noneMatch(n2 -> n2.getLang().equals(n.getLang()) && n2.getName().equals(n.getName())))
                    .forEach(Model::delete);

            // get previous languages for current names
            names.removeIf(n ->
                    this.names.stream().anyMatch(n2 ->
                            n2.getLang().equals(n.getLang()) && n2.getName().equals(n.getName())) ||
                    this.rewordingNames.stream().anyMatch(n2 ->
                            n2.getLang().equals(n.getLang()) && n2.getName().equals(n.getName())));

            // add/update new names
            names.forEach(this::addRewordingName);
        }
    }

    /**
     * Add a rewording name to this tag.
     *
     * @param name a name structure
     */
    public void addRewordingName(TagRewordingI18name name) {
        if (rewordingNames == null) {
            rewordingNames = new ArrayList<>();
        }

        if (rewordingNames.stream().noneMatch(n ->
                n.getLang().equals(name.getLang()) && n.getName().equals(name.getName()))) {
            rewordingNames.add(name);
        }
    }

    /**
     * Get this tagtype
     *
     * @return the tagtype
     */
    public TTagType getTagtype() {
        return tagtype;
    }

    /**
     * Set the tagtype
     *
     * @param tagtype the atagtype to set
     */
    public void setTagType(TTagType tagtype) {
        this.tagtype = tagtype;
    }

    /**
     * Get the contribution parent object
     *
     * @return the contribution "supertype" object
     */
    public Contribution getContribution() {
        return contribution;
    }

    /**
     * Set the contribution parent object
     *
     * @param contribution the contribution "supertype" object
     */
    public void setContribution(Contribution contribution) {
        this.contribution = contribution;
    }

    /**
     * Get all parent tags
     *
     * @return a possibily empty list of tag links
     */
    public List<TagLink> getParentsAsLinks() {
        return parentTags;
    }

    /**
     * Get all parent tags as Tag
     *
     * @return a possibily empty list of tags
     */
    public List<Tag> getParentsAsTags() {
        return parents;
    }

    /**
     * Initialize children tags
     *
     */
    public void initParentTags() {
        this.parents = new ArrayList<>();
    }

    /**
     * Get all parent tags
     *
     * @param parentTags a list of tag links
     */
    public void setParentTags(List<TagLink> parentTags) {
        this.parentTags = parentTags;
    }

    /**
     * Get all parent tags
     *
     * @return a possibily empty list of tag links
     */
    public List<TagLink> getChildrenAsLinks() {
        return childTags;
    }

    /**
     * Get all parent tags as Tag
     *
     * @return a possibily empty list of tags
     */
    public List<Tag> getChildrenAsTags() {
        return children;
    }


    /**
     * Initialize children tags
     *
     */
    public void initChildTags() {
        if(this.childTags == null){
            this.childTags = new ArrayList<>();
        }
    }

    /**
     * Get all child tags
     *
     * @param childTags a list of tag links
     */
    public void setChildTags(List<TagLink> childTags) {
        this.childTags = childTags;
    }

    /**
     * Add a parent tag to this one.
     * As other fields, additions are persisted when calling save().
     *
     * @param link a parent tag to add
     */
    void addParent(TagLink link){
        if(link != null){
            if(parentTags == null){
                parentTags = new ArrayList<>();
            }
            parentTags.add(link);
        }
    }

    /**
     * Remove given tag from this one. If the tag is unfound, this one is unchanged.
     * As other fields, removals are persisted when calling save().
     *
     * @param tag a parent tag id to remove from this Tag
     */
    void removeParent(Long tag){
        TagLink link = TagLink.findByParentChild(tag, idContribution);
        if(link != null){
            link.delete();
        }
    }

    /**
     * Add a child tag to this one.
     * As other fields, additions are persisted when calling save().
     *
     * @param link a child tag to add
     */
    void addChild(TagLink link){
        if(link != null){
            if(childTags == null){
                childTags = new ArrayList<>();
            }
            childTags.add(link);
        }
    }

    /**
     * Remove given tag from this one. If the tag is unfound, this one is unchanged.
     * As other fields, removals are persisted when calling save().
     *
     * @param tag a child tag id to remove from this Tag
     */
    void removeChild(Long  tag){
        TagLink link = TagLink.findByParentChild(idContribution, tag);
        if(link != null){
            link.delete();
        }
    }

    /**
     * Get the contributions that are contained in this tag
     *
     * @return a possibly empty list of contributions
     */
    public List<Contribution> getContributions() {
        return contributions != null ? contributions : new ArrayList<>();
    }

    /**
     * Set the contributions that are contained in this tag
     *
     * @param contributions a possibly empty list of contributions
     */
    public void setContributions(List<Contribution> contributions) {
        this.contributions = contributions;
    }

    /**
     * Get the link id, if this debate tag comes from a debate multiple
     *
     * @return the debate link id
     */
    public Long getLinkId(){
        return linkId;
    }

    /**
     * Set the link id, if this debate tag comes from a debate multiple
     *
     * @param link the debate link id
     */
    public void setLinkId(Long link){
        this.linkId = link;
    }

    /*
     * CONVENIENCE METHODS
     */

    /**
     * Determine if this tag is a category in a justification link or not
     *
     * @return true if this tag is a justification link category
     */
    public boolean isTagCategory() {
        String select = "select t.id_contribution from tag t " +
                "inner join argument_justification_link l on t.id_contribution = l.id_category " +
                "inner join citation_justification_link l2 on t.id_contribution = l2.id_category";

        List<Tag> result = Ebean.find(Tag.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
        return result != null && !result.isEmpty();
    }

    /**
     * Get the current version of this tag
     *
     * @return a timestamp with the latest update moment of this tag
     */
    public Timestamp getVersion() {
        return getContribution().getVersion();
    }

    /**
     * Get the number of contributions if this data is asked in a query
     *
     * @return the number of contributions
     */
    public Integer getNbContributions() {
        return nb_contributions;
    }

    @Override
    public String toString() {
        // must use getters and explicitly loop into references, otherwise ebean may send back deferred beanlist
        // (lazy load not triggered from toString methods)
        StringBuilder builder = new StringBuilder(", named: {").append(getNames().stream()
                .map(t -> t.getName() + " [" + t.getLang() + "]").collect(Collectors.joining(", ")))
                .append("}, rewording names: {").append(getRewordingNames().stream()
                        .map(t -> t.getName() + " [" + t.getLang() + "]").collect(Collectors.joining(", ")))
                .append("}, parents: [").append(getParentsAsTags() != null ? getParentsAsTags().stream().map(Tag::getDefaultName).collect(Collectors.joining(", ")) : "none")
                .append("], children: [").append(getChildrenAsTags() != null ? getChildrenAsTags().stream().map(Tag::getDefaultName).collect(Collectors.joining(", ")) : "none")
                .append("]").append(", type: ").append(getTagtype().getIdType());

        return getModelDescription(getContribution(), builder.toString());
    }

    /*
     * QUERIES
     */

    /**
     * Get the list of links to remove with given tag id or himself
     *
     * @param tagId a tag id
     * @return a possibly empty list of tag links
     */
    public List<TagLink> getLinksToRemoveWithGivenTagOrHimself(Long tagId) {
        String select = "SELECT distinct l.id_contribution FROM tag_link l " +
                "where (l.id_tag_from = " + tagId + " and l.id_tag_to = " + idContribution + ") " +
                "or (l.id_tag_to = " + tagId + " and l.id_tag_from = " + idContribution + ") " +
                "or (l.id_tag_from = " + idContribution + " and l.id_tag_to = " + idContribution + ") " +
                "or (l.id_tag_to = " + idContribution + " and l.id_tag_from = " + idContribution + ")";
        return Ebean.find(TagLink.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }

    /**
     * Retrieve a tag by its id
     *
     * @param id an id
     * @return the Tag corresponding to the given id, or null if not found
     */
    public static Tag findById(Long id) {
        return id == null || id == -1L ? null : find.byId(id);
    }

    /**
     * Retrieve a list of tag of the given type with their name containing the given name
     *
     * @param name tag name
     * @param lang the name lang
     * @param type tag type (int representation, pass -1 if not relevant)
     * @return a list of matches (may be empty)
     */
    public static List<Tag > findByPartialName(String name, String lang, int type) {
        if (name == null) {
            return new ArrayList<>();
        }

        // will be automatically ordered by relevance
        String token = getSearchToken(name);

        String select = "select distinct t.id_contribution from tag t " +
                "left join tag_i18names n on t.id_contribution = n.id_contribution " +
                "left join tag_rewording_i18names n2 on t.id_contribution = n2.id_contribution " +
                "where (n.name like '%" + token + "%'" + (lang == null ? ")" : " and n.id_language = '" + lang + "')") +
                " or (n2.name like '%" + token + "%'" + (lang == null ? ")" : " and n2.id_language = '" + lang + "')") +
                (type == -1 ? "" : " and tagtype = " + type);

        //logger.debug("search for tag: " + select);
        List<Tag> result = Ebean.find(Tag.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
        return result != null ? result : new ArrayList<>();
    }

    /**
     * Get a randomly chosen tag from the database
     *
     * @return a random Tag
     */
    public static Tag random() {
        return findById(random(EContributionType.TAG));
    }

    /**
     * Find a tag by its complete name, lang and tag type
     *
     * @param name the name to find
     * @param lang a two-char iso-639-1 language code
     * @param type a tag type
     * @return a the matched Tag, null otherwise
     */
    public static Tag findByCompleteNameAndLang(String name, String lang, ETagType type) {
        String token = getStrictSearchToken(name);
        String select = "select distinct t.id_contribution from tag t " +
                "left join tag_i18names n on t.id_contribution = n.id_contribution " +
                "left join tag_rewording_i18names n2 on t.id_contribution = n2.id_contribution " +
                "where ((n.name = '" + token + "' " + (lang != null ? "and n.id_language = '" + lang + "'" : "") + ") " +
                "or (n2.name = '" + token + "' " + (lang != null ? "and n2.id_language = '" + lang + "'" : "") + "))" +
                (type != null ? " and t.id_type = " + type.id() : "");

        List<Tag> result = Ebean.find(Tag.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
        return !result.isEmpty() ? result.get(0) : null;
    }

    /**
     * Get the number of contributions contained in this tag
     *
     * @param tag a tag id
     * @param contributor a contributor id
     * @param group a group id
     * @return the number of contributions contained in this tag
     */
    public static int getNbContributions(Long tag, Long contributor, int group){
        String sql = "SELECT count(distinct cht.id_contribution) as 'count' FROM contribution_has_tag cht " +
                "inner join contribution c on c.id_contribution = cht.id_contribution" +
                getContributionStatsJoins() +
                " where cht.id_tag = " + tag + " and c.deleted = 0" +
                getContributionStatsWhereClause(contributor, group);
        return Ebean.createSqlQuery(sql).findUnique().getInteger("count");
    }

    /**
     * Get the number of contributions contained in this tag by contribution type
     *
     * @param tag a tag id
     * @param type a contribution type
     * @param contributor a contributor id
     * @param group a group id
     * @return the number of contributions contained in this tag
     */
    public static int getNbContributionsByType(Long tag, EContributionType type, Long contributor, int group){
        String sql = "SELECT count(distinct cht.id_contribution) as 'count' FROM contribution_has_tag cht " +
                "inner join contribution c on c.id_contribution = cht.id_contribution" +
                getContributionStatsJoins() +
                " where cht.id_tag = " + tag + " and c.deleted = 0 and c.contribution_type = " + type.id() +
                getContributionStatsWhereClause(contributor, group);
        return Ebean.createSqlQuery(sql).findUnique().getInteger("count");
    }

    /**
     * Get the number of links of this tag
     *
     * @param tag a tag id
     * @param contributor a contributor id
     * @param group a group id
     * @return the number of links of this tag
     */
    public static int getNbLinks(Long tag, Long contributor, int group){
        String sql = "SELECT count(distinct link.id_contribution) as 'count' FROM tag_link link " +
                "inner join contribution c on c.id_contribution = link.id_contribution" +
                getContributionStatsJoins() +
                " where link.id_tag_from = " + tag + " or link.id_tag_to = " + tag +
                getContributionStatsWhereClause(contributor, group);
        return Ebean.createSqlQuery(sql).findUnique().getInteger("count");
    }

    /**
     * Get the list context contribution where a given category tag is category
     *
     * @param category a tag category id
     * @return the possibly empty list of context contributions
     */
    public static List<Contribution> getContextContributions(Long category) {
        String select = "select distinct t.id_contribution from tag t " +
                "inner join argument_justification_link l on t.id_contribution = l.id_category " +
                "inner join citation_justification_link l2 on t.id_contribution = l2.id_category " +
                "where t.id_contribution = " + category;

        return Ebean.find(Contribution.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }

    /**
     * Get the list of categories of the given context contribution id, if any
     *
     * @param contribution a context contribution id
     * @return the possibly empty list of categories for the given context contribution
     */
    public static List<Tag> getCategories(Long contribution) {
        String select = "select distinct t.id_contribution from tag t " +
                    "inner join context_has_category chc on chc.id_category = t.id_contribution " +
                    "where chc.id_context = " + contribution;

        return Ebean.find(Tag.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }

    /**
     * Get the list of new children for tag in the database
     *
     * @return a possibly empty map of parent tag / set of new discovered children
     */
    public static Map<Long, Set<Long>> findNewTagsChildren() {
        Map<Long, Set<Long>> mapOfLinks = new HashMap<>();

        String forLang = "fr";
        StringBuilder name2 = new StringBuilder();

        List<TWarnedWord> words = TWarnedWord.findByTypesAndLang(EWarnedWordType.BEGIN.id(), EWarnedWordContextType.TAG.id(), forLang);

        words.forEach(word -> name2
                                    .append("if(POSITION('")
                                    .append(word.getTitle().replace("'", "''"))
                                    .append("' in lower(n2.name)) = 1, substring(n2.name, ")
                                    .append(word.getTitle().length() + 1)
                                    .append("), ")
                );

        name2.append("n2.name")
                .append(new String(new char[words.size()]).replace("\0", ")"));

        String select = "SELECT n2.id_contribution as 'parent', n.id_contribution as 'child' FROM tag t " +
                "left join tag t2 on t2.id_contribution != t.id_contribution " +
                "left join tag_i18names n on t.id_contribution = n.id_contribution  " +
                "left join tag_i18names n2 on t2.id_contribution = n2.id_contribution  " +
                "left join tag_rewording_i18names rn on t.id_contribution = rn.id_contribution  " +
                "left join tag_rewording_i18names rn2 on t2.id_contribution = rn2.id_contribution  " +
                "where t.id_type = 0 and t2.id_type = 0 and n.id_language = '" + forLang + "' and n2.id_language = '" + forLang + "' and ( " +
                "n.name like concat('% ', " + name2 + ", ' %')  " +
                "or n.name like concat('% ', " + name2 + ")  " +
                "or n.name like concat(" + name2 + ", ' %')) " +
                "and (select count(l.id_contribution) from tag_link l where l.id_tag_from = t2.id_contribution and l.id_tag_to = t.id_contribution) = 0";

        Ebean.createSqlQuery(select).findList().forEach(row -> {
            Long parent = row.getLong("parent");
            Long child = row.getLong("child");

            if(!mapOfLinks.containsKey(parent)) {
                mapOfLinks.put(parent, new HashSet<>());
            }

            mapOfLinks.get(parent).add(child);
        });

        return mapOfLinks;
    }

    /**
     * Get the list of debates that are linked to the given tag
     *
     * @param tag a tag id
     * @return the possibly empty list of debates
     */
    public static List<Debate> getDebates(Long tag) {
        String select = "select distinct d.id_contribution from debate d " +
                "inner join contribution c on d.id_contribution = c.id_contribution " +
                "inner join contribution_has_tag cht on d.id_contribution = cht.id_contribution " +
                "where cht.id_tag = " + tag + " order by c.version desc";

        return Ebean.find(Debate.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }

    /**
     * Get the list of debates that are linked to the given tag and that respect the given query
     *
     * @param query the query used for retrieve debates
     * @return the possibly empty list of debates
     */
    public static List<Debate> getDebates(SearchContainer query) {
        String select = addFiltersToSql("select distinct d.id_contribution from debate d " +
                "inner join contribution c on d.id_contribution = c.id_contribution " +
                "inner join contribution_has_tag cht on cht.id_contribution = c.id_contribution " +
                getContributionStatsJoins() +
                " where cht.id_tag = " + query.getId() +
                getContributionStatsWhereClause(query) +
                getOrderByContributionDate() +
                getSearchLimit(query),
                query.getFilters());

        return Ebean.find(Debate.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }

    /**
     * Get all parent tags by query
     *
     * @param query the query used for retrieve parents
     * @param forParents true for parents
     * @return a possibly empty list of tag links
     */
    public static List<TagLink> getLinks(SearchContainer query, boolean forParents) {
        String select = "select distinct link.id_contribution" +
                (query.getContributionType() != null ? ", (" +
                        "select count(distinct cht.id_contribution) from contribution_has_tag cht " +
                        "inner join contribution c on c.id_contribution = cht.id_contribution " +
                        getContributionStatsJoins() +
                        " where c.deleted = 0 and c.contribution_type = " + query.getContributionType().id() +
                        " and cht.id_tag = link." + (forParents ? "id_tag_from" : "id_tag_to") +
                        getContributionStatsWhereClause(query) +
                        ") as nb_contributions" : "") +
                " from tag_link link " +
                addFiltersToSql("inner join tag on tag.id_contribution = link." + (forParents ? "id_tag_from" : "id_tag_to") +
                " where link." + (forParents ? "id_tag_to" : "id_tag_from") + " = " + query.getId() +
                "  group by link.id_contribution " +
                (query.getContributionType() != null ? "order by nb_contributions desc" : "") +
                getSearchLimit(query),
                query.getFilters());

        return Ebean.find(TagLink.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }

    /**
     * Get a random list of tag children for given tag
     *
     * @param tag the parent tag
     * @return a possibly empty list of tag links
     */
    public static List<TagLink> getRandomChildren(Long tag) {
        String select = "SELECT distinct l.id_contribution FROM tag_link l " +
                "left join contribution_in_group cig on cig.id_contribution = l.id_contribution " +
                "where l.id_tag_from = " + tag + " and cig.id_group = " + Group.getPublicGroup().getIdGroup() +
                " order by rand() limit 3";

        return Ebean.find(TagLink.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }
}
