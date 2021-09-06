/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program (see COPYING file).
 * If not, see <http://www.gnu.org/licenses/>.
 */

package be.webdeb.presentation.web.controllers.account.admin.project;

import be.webdeb.core.api.project.Project;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.account.group.GroupForm;
import play.data.validation.ValidationError;

import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ProjectForm extends BaseProjectForm implements Comparable<ProjectForm>{

    private String beginDate = "";
    private String endDate = "";

    private String beginDateToDisplay = "";
    private String endDateToDisplay = "";

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat format2 = new SimpleDateFormat("dd/MM/yyyy");
    private Date convertedBeginDate;
    private Date convertedEndDate;

    private Project projet;
    private boolean hasContributors;

    protected List<ProjectGroupForm> groups = null;

    /**
     * Play / JSON compliant constructor
     */
    public ProjectForm() {
        super();
    }

    /**
     * Constructor from a project object
     *
     * @param project a project
     */
    public ProjectForm(Project project) {
        super(project);
        this.projet = project;

        this.beginDate = format.format(project.getBeginDate());
        this.endDate = format.format(project.getEndDate());

        this.beginDateToDisplay = format2.format(project.getBeginDate());
        this.endDateToDisplay = format2.format(project.getEndDate());

        this.hasContributors = project.hasTmpContributors();
    }

    /**
     * Validator (called from form submit)
     *
     * @return null if no error has been found, otherwise the list of found errors
     */
    public Map<String, List<ValidationError>> validate() {
        Map<String, List<ValidationError>> errors = new HashMap<>();

        // must have a name
        if (values.isBlank(name)) {
            errors.put("name", Collections.singletonList(new ValidationError("name", "project.project.error.name")));
        }

        // must have a technical name
        if (values.isBlank(technicalName)) {
            errors.put("technicalName", Collections.singletonList(new ValidationError("technicalName", "project.project.error.name")));
        }else {
            Project project = factory.findProjectByTechnicalName(technicalName);
            if(project != null && !project.getId().equals(id)){
                errors.put("technicalName", Collections.singletonList(new ValidationError("technicalName", "project.project.error.name.alreadyexists")));
            }
        }

        try {
            convertedBeginDate = format.parse(beginDate);
        } catch (Exception e) {
            errors.put("beginDate", Collections.singletonList(new ValidationError("beginDate", "")));
        }

        try {
            convertedEndDate = format.parse(endDate);
        } catch (Exception e) {
            errors.put("endDate", Collections.singletonList(new ValidationError("endDate", "")));
        }

        if(values.isDate(beginDate)){
            try {
                convertedBeginDate = format.parse(beginDate);
            } catch (ParseException e) {
                errors.put("beginDate", Collections.singletonList(new ValidationError("beginDate", "")));
            }
        }else{
            errors.put("beginDate", Collections.singletonList(new ValidationError("beginDate", "")));
        }

        if(values.isDate(endDate)){
            try {
                convertedEndDate = format.parse(endDate);
            } catch (ParseException e) {
                errors.put("endDate", Collections.singletonList(new ValidationError("endDate", "")));
            }
        }else{
            errors.put("endDate", Collections.singletonList(new ValidationError("endDate", "")));
        }

        if(convertedBeginDate != null && convertedEndDate != null && convertedBeginDate.after(convertedEndDate)){
            errors.put("beginDate", Collections.singletonList(new ValidationError("beginDate", "")));
        }

        return errors.isEmpty() ? null : errors;
    }

    /**
     * Save this project in database
     *
     * @throws PersistenceException if an error occurred while saving this object into DB
     */
    public void save() throws PersistenceException, FormatException {
        logger.debug("try to save project " + name);
        // set all primitive fields
        Project project = factory.getProject();
        project.setId(id);
        project.setName(name);
        project.setTechnicalName(technicalName);
        project.setBeginDate(convertedBeginDate);
        project.setEndDate(convertedEndDate);

        // now save it
        project.save();
    }

    @Override
    public int compareTo(@NotNull ProjectForm form) {
        int result = form.projet.getEndDate().compareTo(getProjet().getEndDate());

        if(result == 0){
            result = name.compareTo(form.getName());
        }

        return result;
    }

    /*
     * Getters and setters
     */

    public String getBeginDate() {
        return beginDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getBeginDateToDisplay() {
        return beginDateToDisplay;
    }

    public String getEndDateToDisplay() {
        return endDateToDisplay;
    }

    public List<ProjectGroupForm> getGroups() {
        return groups;
    }

    public Project getProjet(){
        return projet;
    }

    public void setBeginDate(String beginDate) {
        this.beginDate = beginDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setGroups(List<ProjectGroupForm> groups) {
        this.groups = groups;
    }

    public boolean getHasContributors() {
        return hasContributors;
    }

    /*
     * Helpers
     */

    /**
     * Get the contributions activity report from given date to given date for all contributor groups linked with this project
     *
     * @param fromDate the begin date to get results
     * @param toDate the end date to get results
     * @return the map of subgroup, contribution type and number of contributions of this type
     */
    public Map<GroupForm, Map<Integer, Long>> getProjectContributionReportByContributorGroup(Date fromDate, Date toDate){
        Map<GroupForm, Map<Integer, Long>> results = new HashMap<>();
        projet.getProjectContributionReportByContributorGroup(fromDate, toDate).forEach((k, v) -> results.put(new GroupForm(k), v));
        return results;
    }

    /**
     * Get the contributions activity report from given date to given date for project subgroups of this project
     *
     * @param fromDate the begin date to get results
     * @param toDate the end date to get results
     * @return the map of subgroup, contribution type and number of contributions of this type
     */
    public Map<ProjectSubgroupForm, Map<Integer, Long>> getProjectContributionReportByProjectSubgroup(Date fromDate, Date toDate){
        Map<ProjectSubgroupForm, Map<Integer, Long>> results = new HashMap<>();
        projet.getProjectContributionReportByProjectSubgroup(fromDate, toDate).forEach((k, v) -> results.put(new ProjectSubgroupForm(k, projet), v));
        return results;
    }

    /**
     * Get the contributions activity report for today of the project for contributor groups linked with this project
     *
     * @return the map of subgroup, contribution type and number of contributions of this type
     */
    public Map<GroupForm, Map<Integer, Long>> getProjectContributionReportByContributorGroupOfTheDay(){
        Date now = new Date();
        return getProjectContributionReportByContributorGroup(dateAsBeginOfTheDay(now), now);
    }

    /**
     * Get the contributions activity report for today of the project for project subgroups of this project
     *
     * @return the map of subgroup, contribution type and number of contributions of this type
     */
    public Map<ProjectSubgroupForm, Map<Integer, Long>> getProjectContributionReportByProjectSubgroupOfTheDay(){
        Date now = new Date();
        return getProjectContributionReportByProjectSubgroup(dateAsBeginOfTheDay(now), now);
    }

    /**
     * Get the contributions activity report from 7 days ago to now of the project for contributor groups linked with this project
     *
     * @return the map of subgroup, contribution type and number of contributions of this type
     */
    public Map<GroupForm, Map<Integer, Long>> getProjectContributionReportByContributorGroupFrom7days(){
        Date now = new Date();
        return getProjectContributionReportByContributorGroup(dateAsBeginOfTheDay(changeDayOfDate(now, -7)), now);
    }

    /**
     * Get the contributions activity report from 7 days ago to now of the project for project subgroups of this project
     *
     * @return the map of subgroup, contribution type and number of contributions of this type
     */
    public Map<ProjectSubgroupForm, Map<Integer, Long>> getProjectContributionReportByProjectSubgroupFrom7days(){
        Date now = new Date();
        return getProjectContributionReportByProjectSubgroup(dateAsBeginOfTheDay(changeDayOfDate(now, -7)), now);
    }

    /**
     * Get the contributions activity report from de begining of the project for contributor groups linked with this project
     *
     * @return the map of subgroup, contribution type and number of contributions of this type
     */
    public Map<GroupForm, Map<Integer, Long>> getProjectContributionReportByContributorGroupFromProjectBegin(){
        logger.debug(projet.getBeginDate()+"++");
        return getProjectContributionReportByContributorGroup(dateAsBeginOfTheDay(projet.getBeginDate()), new Date());
    }

    /**
     * Get the contributions activity report from de begining of the project for project subgroups of this project
     *
     * @return the map of subgroup, contribution type and number of contributions of this type
     */
    public Map<ProjectSubgroupForm, Map<Integer, Long>> getProjectContributionReportByProjectSubgroupFromProjectBegin(){
        return getProjectContributionReportByProjectSubgroup(dateAsBeginOfTheDay(projet.getBeginDate()), new Date());
    }

    /**
     * Get the given date as the beginning of the day (midnight)
     *
     * @param date the day to change
     * @return the same date at midnight
     */
    private Date dateAsBeginOfTheDay(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * Add days for a given date
     *
     * @param date the day to change
     * @param nbDays the number of days to add to given date
     * @return the date with nbDays added
     */
    private Date changeDayOfDate(Date date, int nbDays){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, nbDays);
        return calendar.getTime();
    }
}
