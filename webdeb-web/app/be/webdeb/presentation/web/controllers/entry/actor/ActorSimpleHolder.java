package be.webdeb.presentation.web.controllers.entry.actor;


import be.webdeb.core.api.actor.*;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.presentation.web.controllers.entry.ContributionHelper;
import be.webdeb.presentation.web.controllers.entry.ContributionHolder;
import be.webdeb.presentation.web.controllers.entry.EFilterName;
import be.webdeb.presentation.web.controllers.viz.actor.AffiliationViz;
import be.webdeb.util.ValuesHelper;
import org.jetbrains.annotations.NotNull;
import play.api.Play;
import play.i18n.Lang;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Calendar.YEAR;

/**
 * This class holds a few data about an actor
 *
 * @author Martin Rouffiange
 */
public class ActorSimpleHolder implements Comparable<ActorSimpleHolder> {

    /*
     * person attributes
     */
    protected Long id;
    protected String name;
    protected String avatar;
    protected int actortype;
    protected String gender;
    protected String residence;
    protected int age;
    protected String lang;

    @Inject
    private ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

    @Inject
    protected ContributionHelper helper = Play.current().injector().instanceOf(ContributionHelper.class);

    @Inject
    protected ActorFactory actorFactory = Play.current().injector().instanceOf(ActorFactory.class);;

    /**
     * Play / JSON compliant constructor
     */
    public ActorSimpleHolder(Actor actor, String lang) {
        id = actor.getId();
        name = actor.getFullname(lang);
        avatar = helper.getDefaultActorAvatar(actor);
        actortype = actor.getActorTypeId();
        this.lang = lang;

        if(actor.getActorType() == EActorType.PERSON) {
            Person person = (Person) actor;
            gender = person.getGender() != null ? person.getGender().getCode() : "";
            residence = person.getResidence() != null ? person.getResidence().getName(lang) : "";
            age = person.getBirthdate() != null ? (getCalendar(new Date()).get(YEAR) - getCalendar(values.toDate(person.getBirthdate())).get(YEAR)) : 0;
        }
    }

    private Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance(Locale.FRENCH);
        cal.setTime(date);
        return cal;
    }

    /**
     * Helper method to get the avatar filename, regardless the actortype. If no avatar exists,
     * return a default file name
     *
     * @return a file name
     */
    public String getSomeAvatar() {
        return avatar;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAvatar() {
        return avatar;
    }

    public int getActortype() {
        return actortype;
    }

    public String getGender() {
        return gender;
    }

    public String getResidence() {
        return residence;
    }

    public int getAge() {
        return age;
    }

    /**
     * Get all affiliations related with this actor that are in the given actors id list
     *
     * @param actorIds the list of possibly related actors
     * @return the list of affiliations
     */
    public List<AffiliationViz> getAffiliationsRelatedTo(List<Long> actorIds){
        return actorFactory.getAffiliationsRelatedTo(id, actorIds).stream().map(e -> new AffiliationViz(e, lang)).sorted().collect(Collectors.toList());
    }

    /**
     * Get all affiliations related with this actor that are in the given actors id list by function
     *
     * @param actorIds the list of possibly related actors
     * @return the list of affiliations by function
     */
    public Map<String, List<AffiliationViz>> getAffiliationsRelatedToByFunction(List<Long> actorIds){
        Map<String, List<AffiliationViz>> map = new HashMap<>();

        actorFactory.getAffiliationsRelatedTo(id, actorIds).forEach(e -> {
            AffiliationViz viz = new AffiliationViz(e, lang);
            String key = viz.getFunction();
            if(!map.containsKey(key)) {
                map.put(key, new ArrayList<>());
            }
            map.get(key).add(viz);
        });

        map.forEach((k, v) -> Collections.sort(v));

        return map;
    }

    /**
     * Get the most recent affiliation related with this actor that are related to the given id
     *
     * @param actorId an actor id
     * @return an affiliation
     */
    public AffiliationViz getMostRecentAffiliationsRelatedTo(Long actorId){
        return getMostRecentAffiliationsRelatedTo(Collections.singletonList(actorId));
    }

    /**
     * Get the most recent affiliation related with this actor that are in the given actors id list
     *
     * @param actorIds the list of possibly related actors
     * @return an affiliation
     */
    public AffiliationViz getMostRecentAffiliationsRelatedTo(List<Long> actorIds){
        Optional<Affiliation> aff = actorFactory.getAffiliationsRelatedTo(id, actorIds).stream()
                .filter(e -> !values.isBlank(e.getEndDate()))
                .max((e1, e2) -> values.toDate(e1.getEndDate()).compareTo(values.toDate(e2.getEndDate())));
        return aff.isPresent() ? new AffiliationViz(aff.get(), lang) : null;
    }

    @Override
    public int compareTo(@NotNull ActorSimpleHolder o) {
        return name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return "id=" + id + ", name='" + name;
    }
}
