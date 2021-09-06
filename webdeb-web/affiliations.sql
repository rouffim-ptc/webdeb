SELECT ac.id_contribution as 'id_actor', ac.sortkey as 'name', concat(a.id_contribution, ap.extension) as 'image', p.gender FROM actor_has_affiliation aha
left join contribution ac on ac.id_contribution = aha.id_actor
left join actor a on a.id_contribution = ac.id_contribution
left join person p on p.id_contribution = ac.id_contribution
left join contributor_picture ap on ap.id_picture = a.id_picture
where aha.id_actor_as_affiliation = 123996 and a.id_type = 0
group by aha.id_actor

SELECT aha.id_aha as 'link_id', aha.start_date, aha.end_date, aha.start_date_type, start_date_type.fr, aha.end_date_type, end_date_type.fr,

@f1 := (select spelling from profession_i18names where lang = 'fr' and gender = p.gender and profession = aha.function) as 'gendered_function',
if(@f1 is null, @f2 := (select spelling from profession_i18names where lang = 'fr' and profession = aha.function group by profession), "") as 'neutral_function1',
if(@f2 is null, (select spelling from profession_i18names where profession = aha.function group by profession), "") as 'neutral_function2',
@f3 := (select spelling from profession_i18names where lang = 'fr' and (gender = 'M' or gender = 'N') and profession = phl.id_profession_to group by profession) as 'generic_function1',
if(@f3 is null, @f4 := (select spelling from profession_i18names where profession = phl.id_profession_to group by profession), "") as 'generic_function2'

FROM actor_has_affiliation aha
inner join person p on p.id_contribution = aha.id_actor
left join t_precision_date_type start_date_type on start_date_type.id_type = aha.start_date_type
left join t_precision_date_type end_date_type on end_date_type.id_type = aha.end_date_type
left join profession_has_link phl on phl.id_profession_from = aha.function 
where aha.id_actor_as_affiliation = 123996
group by aha.id_aha



SELECT ac.id_contribution as 'id_actor', ac.sortkey as 'name', concat(a.id_contribution, ap.extension) as 'image', status.fr as 'status' FROM actor_has_affiliation aha
left join contribution ac on ac.id_contribution = aha.id_actor
left join actor a on a.id_contribution = ac.id_contribution
left join contributor_picture ap on ap.id_picture = a.id_picture
inner join organization o on o.id_contribution = ac.id_contribution
inner join t_legal_status status on status.id_type = o.legal_status
where aha.id_actor_as_affiliation = 123996
group by aha.id_actor

SELECT aha.id_aha as 'link_id', aha.start_date, aha.end_date, aha.start_date_type, start_date_type.fr, aha.end_date_type, end_date_type.fr, aha.type as 'type' FROM actor_has_affiliation aha
left join actor a on a.id_contribution = aha.id_actor
left join t_precision_date_type start_date_type on start_date_type.id_type = aha.start_date_type
left join t_precision_date_type end_date_type on end_date_type.id_type = aha.end_date_type
where aha.id_actor_as_affiliation = 123996 and a.id_type = 1



SELECT ac.id_contribution as 'id_actor', ac.sortkey as 'name', concat(a.id_contribution, ap.extension) as 'image' FROM actor_has_affiliation aha
left join contribution ac on ac.id_contribution = aha.id_actor_as_affiliation
left join actor a on a.id_contribution = ac.id_contribution
left join contributor_picture ap on ap.id_picture = a.id_picture
where aha.id_actor = 123996
group by aha.id_actor

SELECT aha.id_aha as 'link_id', aha.start_date, aha.end_date, aha.start_date_type, start_date_type.fr, aha.end_date_type, end_date_type.fr, aha.type as 'type' FROM actor_has_affiliation aha
left join actor a on a.id_contribution = aha.id_actor_as_affiliation
left join t_precision_date_type start_date_type on start_date_type.id_type = aha.start_date_type
left join t_precision_date_type end_date_type on end_date_type.id_type = aha.end_date_type
where aha.id_actor = 123996