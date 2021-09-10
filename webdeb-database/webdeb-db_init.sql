-- MySQL dump 10.13  Distrib 8.0.16, for Win64 (x86_64)
--
-- Host: localhost    Database: webdeb
-- ------------------------------------------------------
-- Server version	5.7.26-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `t_contribution_type`
--

LOCK TABLES `t_contribution_type` WRITE;
/*!40000 ALTER TABLE `t_contribution_type` DISABLE KEYS */;
INSERT INTO `t_contribution_type` VALUES 
	(0,'ACTOR','actor','acteur','Actor'),
	(1,'DEBATE','debate','debat','Argument'),
	(2,'TEXT','text','texte','Tekst'),
	(3,'CITATION','citation','citation','citaat'),
	(4,'ARGUMENT','argument','affirmation','Argument'),
	(5,'ARGUMENT_DICTIONARY','argument dictionary','dictionaire affirmation','Argument dictionary'),
	(6,'TAG','tag','tag','tag'),
	(7,'AFFILIATION','affiliation','affiliation','Affiliatie'),
	(8,'ARG_JUSTIFICATION','argument justification','justification d\'affirmation','Argumentatieve rechtvaardiging'),
	(9,'ARG_SIMILARITY','argument simalarity','affirmation similaire','Argument gelijkenis'),
	(10,'CIT_JUSTIFICATION','citation justification','justification de citation','Citation rechtvaardiging'),
	(11,'DEB_SIMILARITY','debate simalarity','débats similaire','Debate gelijkenis'),
	(12,'DEB_HAS_TAG_DEB','debate has tag debate','debate has tag debate','debate has tag debate'),
	(13,'TAG_LINK','tag link','lien entre tags','taglink'),
	(14,'EXTERNAL_TEXT','external text','text externe','externe tekst'),
	(15,'EXTERNAL_CITATION','external citation','citation externe','externe citaat'),
	(16,'CONTEXT_HAS_CATEGORY','context has category','context has category','context has category'),
	(17,'CITATION_POSITION','citation position','citation position','citation position'),
	(18,'ACTOR_HAS_POSITION','actor has position','actor has position','actor has position');
/*!40000 ALTER TABLE `t_contribution_type` ENABLE KEYS */;
UNLOCK TABLES;

-- -----------------------------------------------------
-- Default public group (opened, members are visible globally, contributions are visible globally), anyone may add, edit contributions and view members
-- -----------------------------------------------------

INSERT INTO `contributor_group` VALUES (-1, 'webdeb public', '', 1, 0, 0, 'E97451', now());
UPDATE `contributor_group` SET id_group = 0 WHERE id_group = -1;
INSERT INTO `group_has_permission` VALUES (0, 1);
INSERT INTO `group_has_permission` VALUES (0, 2);

-- default user for WebDeb administration
INSERT INTO `contributor` values (-1, 'Admin', 'Admin', 'admin@webdeb.be', null, now(), 'F', 2016, null, null, null, null, 1, 0, 0, 0, 1, 0, 1, null, now(), now(), now(), 'WebDeb', null);
UPDATE `contributor` SET id_contributor = 0 WHERE id_contributor = -1;
INSERT INTO `contributor_has_group` values (0, 0, 3, 0, true, null, now());

--
-- Dumping data for table `permission`
--

LOCK TABLES `permission` WRITE;
/*!40000 ALTER TABLE `permission` DISABLE KEYS */;
INSERT INTO `permission` VALUES (0,'view_contribution','visualize contribution'),(1,'add_contribution','add new contribution'),(2,'edit_contribution','edit existing contribution'),(3,'del_contribution','delete contribution'),(4,'view_member','view group members and details'),(5,'add_member','add a member into a group'),(6,'edit_member','edit group member (contributor) profile'),(7,'block_member','block member from contributing into group'),(8,'merge_contribution','merge contributions from group into main database'),(9,'block_contributor','prevent contributor to contributute into webdeb'),(10,'assign_role','assign role to contributor'),(11,'create_group','create a new group'),(12,'delete_group','delete a group'),(13,'disable_annotation','disable text annotation service'),(14,'edit_contribution_ingroup','disable text annotation service');
/*!40000 ALTER TABLE `permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (0,'viewer'),(1,'contributor'),(2,'group_owner'),(3,'admin');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `role_has_permission`
--

LOCK TABLES `role_has_permission` WRITE;
/*!40000 ALTER TABLE `role_has_permission` DISABLE KEYS */;
INSERT INTO `role_has_permission` VALUES (0,0),(1,0),(1,1),(1,2),(1,4),(1,11),(2,0),(2,1),(2,2),(2,4),(2,5),(2,7),(3,0),(3,1),(3,2),(3,3),(3,4),(3,5),(3,6),(3,7),(3,8),(3,9),(3,10),(3,11),(3,12),(3,13);
/*!40000 ALTER TABLE `role_has_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_actor_type`
--

LOCK TABLES `t_actor_type` WRITE;
/*!40000 ALTER TABLE `t_actor_type` DISABLE KEYS */;
INSERT INTO `t_actor_type` VALUES (-1,'UNKNOWN','unknown','inconnu','onbekend'),(0,'PERSON','person','personne','persoon'),(1,'ORGANIZATION','organization','organisation','organisatie');
/*!40000 ALTER TABLE `t_actor_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_affiliation_actor_type`
--

LOCK TABLES `t_affiliation_actor_type` WRITE;
/*!40000 ALTER TABLE `t_affiliation_actor_type` DISABLE KEYS */;
INSERT INTO `t_affiliation_actor_type` VALUES (0,'ORGANIZATION / PERSON'),(1,'ORGANIZATION'),(2,'PERSON');
/*!40000 ALTER TABLE `t_affiliation_actor_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_affiliation_subtype`
--

LOCK TABLES `t_affiliation_subtype` WRITE;
/*!40000 ALTER TABLE `t_affiliation_subtype` DISABLE KEYS */;
INSERT INTO `t_affiliation_subtype` VALUES (-1,'ALL'),(0,'AFFILIATION'),(1,'AFFILIATED'),(2,'FILIATION');
/*!40000 ALTER TABLE `t_affiliation_subtype` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_affiliation_type`
--

LOCK TABLES `t_affiliation_type` WRITE;
/*!40000 ALTER TABLE `t_affiliation_type` DISABLE KEYS */;
INSERT INTO `t_affiliation_type` VALUES (-1,0,-1,'unset','indéfini','onbepaald'),(0,1,0,'50% or more owned or financed by','détenu ou financé à 50 % ou plus par','Voor minstens 50% in handen van of gefinancierd door'),(1,1,0,'between 25 and 49,9% owned or financed by','détenu ou financé entre 25 et 49,9 % par','Tussen 25 and 49,9% in handen van of gefinancierd door'),(2,1,0,'less than 25% owned or financed by','détenu ou financé à moins de 25 % par','Minder dan 25% in handen van of gefinancierd door'),(3,1,0,'partially owned or financed by (unknown %)','détenu ou financé par (% indéterminé)','in handen van of gefinancierd door (onbekend %)'),(4,1,0,'department of','division de','Departement van'),(5,1,0,'member of','membre de','Lid van'),(6,1,0,'produced or organized by','produit ou organisé par','geproduceerd of georganiseerd door'),(7,0,0,'awarded or labbeled by','distingué ou labellisé par','Gecertifieerd door'),(8,0,0,'participating in','participant à','Neemt deel aan'),(9,1,0,'cabinet of','cabinet de','Kabinet van'),(10,1,0,'is graduating from','est dipolomer de','studeert af'),(11,2,2,'has as parent','a comme parent','heeft als ouder'),(12,0,1,'owns or finances at least 50% of','possède ou finance au moins 50 % de','Bezit of financiert meer dan 50% van'),(13,0,1,'owns or finances between 25 and 49,9% of ','possède ou finance 25 à 49,9 % de','Bezit of financiert tussen 25 and 49,9% van'),(14,0,1,'owns or finances less than 25% of','possède ou finance moins de 25 % de','Bezit of financiert minder dan 25% van'),(15,0,1,'holds shares of or finances (unknown %)','est actionnaire ou finance (% indéterminé)','Bezit aandelen van of financiert (% onbekend)'),(16,1,1,'has as division','a pour département','Heeft als departement'),(17,1,1,'has as member','a pour membre','Heeft als lid'),(18,1,1,'produces or organizes','produit ou organise','Produceert of regelt'),(19,0,1,'has awarded or labelled','distingue ou prime','Certificeert'),(20,1,1,'has as participant','a comme participant','Heeft als deelnemers'),(21,2,1,'has as cabinet','a comme cabinet','Heeft als kabinet');
/*!40000 ALTER TABLE `t_affiliation_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_argument_shade_type`
--

LOCK TABLES `t_argument_shade_type` WRITE;
/*!40000 ALTER TABLE `t_argument_shade_type` DISABLE KEYS */;
INSERT INTO `t_argument_shade_type` (`id_shade`, `en`, `fr`, `nl`) VALUES ('-1', '[Free begin of argument]', '[Début d’argument libre]', '[Vrije begin van argument]');
INSERT INTO `t_argument_shade_type` VALUES (0,'There is no doubt that','Il est vrai que','Het is waar dat'),(1,'It is necessary to','Il faut','We moeten'),(2,'the strong and weak points','les points fort et faibles','de sterke en zwakke punten'),(3,'les conditions','les conditions','les conditions');
/*!40000 ALTER TABLE `t_argument_shade_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_argument_type`
--

LOCK TABLES `t_argument_type` WRITE;
/*!40000 ALTER TABLE `t_argument_type` DISABLE KEYS */;
INSERT INTO `t_argument_type` VALUES (0,'Simple argument','Argument simple','Simple argument'),(1,'Shaded argument','Argument avec nuance','Shaded argument');
/*!40000 ALTER TABLE `t_argument_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_business_sector`
--

LOCK TABLES `t_business_sector` WRITE;
/*!40000 ALTER TABLE `t_business_sector` DISABLE KEYS */;
INSERT INTO `t_business_sector` VALUES (0,'Agriculture and fisheries','Agriculture, sylviculture et pêche','Landbouw en visserij'),(1,'Art and culture','Arts et culture','Kunst en cultuur'),(2,'Sport and leisure','Loisirs et sports','Sport en vrijetijd'),(3,'Retail trade','Commerce de détail','Detailhandel'),(4,'Building and real estate','Construction et immobilier','Bouwen en onroerend goed'),(5,'Education and training','Education et formation','Onderwijs en opleiding'),(6,'Energy','Energie','Energie'),(7,'Environment','Environnement','Milieu'),(8,'Finance, holding, banking and insurance','Finances, holding, banques et assurances','Financiën, holding, bankieren en verzekeren'),(9,'Hospitality, catering and tourism','Hôtellerie, restauration et tourisme','Gastvrijheid, horeca en toerisme'),(10,'Industry','Industrie','Industrie'),(11,'Mining industry','Industries extractives (matières premières)','Mijnbouwindustrie'),(12,'Justice','Justice','Justitie'),(13,'Press and media','Presse et médias','Pers en media'),(14,'Research and expertise','Recherche et expertise','Onderzoek en expertise'),(15,'Health','Santé','Gezondheid'),(16,'Security','Sécurité','Veiligheid'),(17,'Information and Communications Technology','Technologies de l\'information et de la communication','Informatie en communicatietechnologie'),(18,'Transport and logistics','Transports et logistique','Transport en logistiek'),(19,'Other services to individuals or organisations','Autres services aux particuliers et organisations','Andere diensten voor particulieren en organisaties'),(20,'Cross-sectoral','Transversale aux secteurs','Sectoroverschrijdende');
/*!40000 ALTER TABLE `t_business_sector` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_contribution_visibility`
--

LOCK TABLES `t_contribution_visibility` WRITE;
/*!40000 ALTER TABLE `t_contribution_visibility` DISABLE KEYS */;
INSERT INTO `t_contribution_visibility` VALUES (0,'contributions are publicly visible'),(1,'contributions are visible in group only'),(2,'contributions are only visible by contributor and group owner');
/*!40000 ALTER TABLE `t_contribution_visibility` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_contributor_picture_source`
--

LOCK TABLES `t_contributor_picture_source` WRITE;
/*!40000 ALTER TABLE `t_contributor_picture_source` DISABLE KEYS */;
INSERT INTO `t_contributor_picture_source` VALUES (-1,'Unknown','Inconnue','Unknown'),(0,'Contributor','Contributeur','Contributor'),(1,'Picture bank','Banque d image','Picture bank'),(2,'Other open source','Autre source libre','Other open source');
/*!40000 ALTER TABLE `t_contributor_picture_source` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_copyrightfree_source`
--

LOCK TABLES `t_copyrightfree_source` WRITE;
/*!40000 ALTER TABLE `t_copyrightfree_source` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_copyrightfree_source` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_country`
--

LOCK TABLES `t_country` WRITE;
/*!40000 ALTER TABLE `t_country` DISABLE KEYS */;
INSERT INTO `t_country` VALUES ('ad','Andorra','Andorre','Andorra'),('ae','United Arab Emirates','Émirats Arabes Unis','Verenigde Arabische Emiraten'),('af','Afghanistan','Afghanistan','Afghanistan'),('ag','Antigua and Barbuda','Antigua-et-Barbuda','Antigua en Barbuda'),('ai','Anguilla','Anguilla','Anguilla'),('al','Albania','Albanie','Albanië'),('am','Armenia','Arménie','Armenië'),('an','Netherlands Antilles','Antilles Néerlandaises','Nederlandse Antillen'),('ao','Angola','Angola','Angola'),('aq','Antarctica','Antartique','Antarctica'),('ar','Argentina','Argentine','Argentinië'),('as','American Samoa','Samoa américaines','Amerikaans Samoa'),('at','Austria','Autriche','Oostenrijk'),('au','Australia','Australie','Australië'),('aw','Aruba','Aruba','Aruba'),('ax','Åland Islands','Åland (Islande)','Ålandseilanden'),('az','Azerbaijan','Azerbaïdjan','Azerbeidzjan'),('ba','Bosnia and Herzegovina','Bosnie-Herzégovine','Bosnië-Herzegovina'),('bb','Barbados','Barbade','Barbados'),('bd','Bangladesh','Bangladesh','Bangladesh'),('be','Belgium','Belgique','België'),('bf','Burkina Faso','Burkina Faso','Burkina Faso'),('bg','Bulgaria','Bulgarie','Bulgarije'),('bh','Bahrain','Bahreïn','Bahrein'),('bi','Burundi','Burundi','Burundi'),('bj','Benin','Bénin','Benin'),('bl','Saint-Barthélemy','Saint-Barthélemy','Saint-Barthélemy'),('bm','Bermuda','Bermude','Bermuda'),('bn','Brunei Darussalam','Brunéi Darussalam','Brunei Darussalam'),('bo','Bolivia','Bolivie','Bolivia'),('br','Brazil','Brésil','Brazilië'),('bs','Bahamas','Bahamas','Bahamaës'),('bt','Bhutan','Bhoutan','Bhutan'),('bv','Bouvet Island','Île Bouvet','Bouveteiland'),('bw','Botswana','Botswana','Botswana'),('by','Belarus','Belarus','Wit-Rusland'),('bz','Belize','Belize','Belize'),('ca','Canada','Canada','Canada'),('cc','Cocos (Keeling) Islands','Îles Cocos','Cocoseilanden'),('cd','Congo (Dem.Rep.)','Congo (Rép.Dém.)','Congo (Dem. Rep)'),('cf','Central African Republic','République centraficaine','Centraal Afrikaanse Republiek'),('cg','Congo (Brazzaville)','Congo (Brazzaville)','Congo (Brazzaville)'),('ch','Switzerland','Suisse','Zwitserland'),('ci','Côte d’Ivoire','Côte d’Ivoire','Ivoorkust'),('ck','Cook Islands','Îles Cook','Cook Eilanden'),('cl','Chile','Chili','Chili'),('cm','Cameroon','Cameroun','Kameroen'),('cn','China','Chine','China'),('co','Colombia','Colombie','Colombia'),('cr','Costa Rica','Costa Rica','Costa Rica'),('ct','Canton and Enderbury Islands','Canton et Enderbury (Îles)','Canton en Enderbury'),('cu','Cuba','Cuba','Cuba'),('cv','Cape Verde','Cap-Vert','Kaapverdië'),('cx','Christmas Island','Île Christmas','Kersteiland'),('cy','Cyprus','Chypre','Cyprus'),('cz','Czech Republic','République Tchèque','Tsjechische Republiek'),('de','Germany','Allemagne','Duitsland'),('dj','Djibouti','Djibouti','Djibouti'),('dk','Denmark','Danemark','Denemarken'),('dm','Dominica','Dominique','Dominica'),('do','Dominican Republic','République Dominicaine','Dominicaanse Republiek'),('dz','Algeria','Algérie','Algerije'),('ec','Ecuador','Équateur','Ecuador'),('ee','Estonia','Estonie','Estland'),('eg','Egypt','Égypte','Egypte'),('eh','Western Sahara','Sahara occidental','Westelijke Sahara'),('er','Eritrea','Érythrée','Eritrea'),('es','Spain','Espagne','Spanje'),('et','Ethiopia','Éthiopie','Ethiopië'),('fi','Finland','Finlande','Finland'),('fj','Fiji','Fidji','Fiji'),('fk','Falkland Islands','Îles Malouines','Falkland Eilanden'),('fm','Micronesia','Micronésie','Micronesië'),('fo','Faroe Islands','Îles Féroé','Faro‘r Eilanden'),('fr','France','France','Frankrijk'),('ga','Gabon','Gabon','Gabon'),('gb','United Kingdom','Grande Bretagne','Verenigd Koninkrijk'),('gd','Grenada','Grenade','Grenada'),('ge','Georgia','Géorgie','Georgië'),('gf','French Guiana','Guyane Française','Frans Guyana'),('gg','Guernsey','Guernesey','Guernsey'),('gh','Ghana','Ghana','Ghana'),('gi','Gibraltar','Gibraltar','Gibraltar'),('gl','Greenland','Groenland','Groenland'),('gm','Gambia','Gambie','Gambia'),('gn','Guinea','Guinée','Guinea'),('gp','Guadeloupe','Guadeloupe','Guadeloupe'),('gq','Equatorial Guinea','Guinée Équatoriale','Equatoriaal-Guinea'),('gr','Greece','Grèce','Griekenland'),('gs','South Georgia and the South Sandwich Islands','Géorgie du Sud-et-les Îles Sandwich du Sud','Zuid-Georgia en de Zuidelijke Sandwicheilanden'),('gt','Guatemala','Guatemala','Guatemala'),('gu','Guam','Guam','Guam'),('gw','Guinea-Bissau','Guinée-Bissau','Guinea-Bissau'),('gy','Guyana','Guyane','Guyana'),('hk','Hong Kong (SAR China)','Hong Kong (RAS Chine)','Hong Kong (SAR China)'),('hm','Heard Island and McDonald Islands','Îles Heard-et-MacDonald','Heard en MacDonald Eilanden'),('hn','Honduras','Honduras','Honduras'),('hr','Croatia','Croatie','Kroatië'),('ht','Haiti','Haïti','Haïti'),('hu','Hungary','Hongrie','Hongarije'),('id','Indonesia','Indonésie','Indonesië'),('ie','Ireland','Irlande','Ierland'),('il','Israel','Israël','Israel'),('im','Isle of Man','Île de Man','Man Eiland'),('in','India','Inde','India'),('io','British Indian Ocean Territory','Territoire britannique de l\'océan Indien','Brits Indische Oceaanterritorium'),('iq','Iraq','Iraq','Irak'),('ir','Iran (Islam.Rep.)','Iran (Rep.Islam.)','Iran (Islam.Rep.)'),('is','Iceland','Islande','Ijsland'),('it','Italy','Italie','Italië'),('je','Jersey','Jersey','Jersey'),('jm','Jamaica','Jamaïque','Jamaica'),('jo','Jordan','Jordanie','Jordanië'),('jp','Japan','Japon','Japan'),('jt','Johnston Island','Atoll Johnston','Jonsthon Atoll'),('ke','Kenya','Kénya','Kenya'),('kg','Kyrgyzstan','Kirghizistan','Kirgizië'),('kh','Cambodia','Cambodge','Cambodja'),('ki','Kiribati','Kiribati','Kiribati'),('km','Comoros','Comores','Comoren'),('kn','Saint Kitts and Nevis','Saint-Christophe-et-Niévès','Saint Kitts en Nevis'),('kp','Korea (Dem.Peo.Rep)','Corée (Rép.Pop.Dém.','Korea (Dem. Volksrep.)'),('kr','Korea (Rep.)','Corée (Rép.)','Korea (Rep.)'),('kw','Kuwait','Koweït','Koeweit'),('ky','Cayman Islands','Îles Caïmans','Kaaimaneilanden'),('kz','Kazakhstan','Kazakhstan','Kazachstan'),('la','Lao (Peo.Dem.Rep)','Laos (Rép.Dém.Pop.)','Laos (Dem. Volksrep.)'),('lb','Lebanon','Liban','Libanon'),('lc','Saint Lucia','Sainte-Lucie','Saint Lucia'),('li','Liechtenstein','Liechtenstein','Liechtenstein'),('lk','Sri Lanka','Sri Lanka','Sri Lanka'),('lr','Liberia','Liberia','Liberia'),('ls','Lesotho','Lesotho','Lesotho'),('lt','Lithuania','Lituanie','Litouwen'),('lu','Luxembourg','Luxembourg','Luxemburg'),('lv','Latvia','Lettonie','Letland'),('ly','Libya','Lybie','Libië'),('ma','Morocco','Maroc','Marokko'),('mc','Monaco','Monaco','Monaco'),('md','Moldova','Moldavie','Moldavië'),('me','Montenegro','Montenegro','Montenegro'),('mf','Saint Martin','Saint-Martin','Sint Maarten'),('mg','Madagascar','Madagascar','Madagascar'),('mh','Marshall Islands','Îles Marshall','Marshall Eilanden'),('mk','Macedonia (Rep.)','Macédoine (Rép.)','Macedonië'),('ml','Mali','Mali','Mali'),('mm','Myanmar','Myanmar','Myanmar'),('mn','Mongolia','Mongolie','Mongolië'),('mo','Macau (SAR China)','Macao (RAS Chine)','Macau (SAR China)'),('mp','Northern Mariana Islands','Îles Mariannes du Nord','Noordelijke Marianen'),('mq','Martinique','Martinique','Martinique'),('mr','Mauritania','Mauritanie','Mauretanië'),('ms','Montserrat','Montserrat','Montserrat'),('mt','Malta','Malte','Malta'),('mu','Mauritius','Maurice','Mauritius'),('mv','Maldives','Maldives','Malediven'),('mw','Malawi','Malawi','Malawi'),('mx','Mexico','Mexique','Mexico'),('my','Malaysia','Malaisie','Maleisië'),('mz','Mozambique','Mozambique','Mozambique'),('na','Namibia','Namibie','Namibië'),('nc','New Caledonia','Nouvelle Calédonie','Nieuw-Caledonië'),('ne','Niger','Niger','Niger'),('nf','Norfolk Island','Île Norfolk','Norfolk Eiland'),('ng','Nigeria','Nigeria','Nigeria'),('ni','Nicaragua','Nicaragua','Nicaragua'),('nl','Netherlands','Pays-Bas','Nederland '),('no','Norway','Norvège','Noorwegen'),('np','Nepal','Népal','Nepal'),('nq','Dronning Maud Land','Terre de la Reine-Maud','Koningin Maudland'),('nr','Nauru','Nauru','Nauru'),('nu','Niue','Niue','Niue'),('nz','New Zealand','Nouvelle Zélande','Nieuw Zeeland'),('om','Oman','Oman','Oman'),('pa','Panama','Panama','Panama'),('pe','Peru','Pérou','Peru'),('pf','French Polynesia','Polynésie Française','Frans Polynesië'),('pg','Papua New Guinea','Papouasie-Nouvelle-Guinée','Papua Nieuw Guinea'),('ph','Philippines','Philippines','Filippijnen'),('pk','Pakistan','Pakistan','Pakistan'),('pl','Poland','Pologne','Polen'),('pm','Saint Pierre and Miquelon','Saint-Pierre-et-Miquelon','Saint Pierre en Miquelon'),('pn','Pitcairn','Îles Pitcairn','Pitcairn Eilanden'),('pr','Puerto Rico','Puerto Rico','Puerto Rico'),('ps','Palestinian Territory','Territoires palestiniens','Palestijnse Gebieden'),('pt','Portugal','Portugal','Portugal'),('pw','Palau','Palaos','Palau'),('py','Paraguay','Paraguay','Paraguay'),('qa','Qatar','Qatar','Qatar'),('re','Réunion','Rénion','RŽunion'),('ro','Romania','Roumanie','Roemenië'),('rs','Serbia','Serbie','Servië'),('ru','Russia (Fed.)','Russie (Féd.)','Rusland (Fed.)'),('rw','Rwanda','Rwanda','Rwanda'),('sa','Saudi Arabia','Arabie saoudite','Saudi Arabië'),('sb','Solomon Islands','Salomon','Salomon Eilanden'),('sc','Seychelles','Seychelles','Seychellen'),('sd','Sudan','Soudan','Sudan'),('se','Sweden','Suède','Zweden'),('sg','Singapore','Singapour','Singapore'),('sh','Saint Helena','Sainte-Hélène','Sint Helena'),('si','Slovenia','Slovénie','Sloveni‘'),('sj','Svalbard and Jan Mayen Islands','Svalbard et Jan Mayen (Îles)','Spitsbergen en Jan Mayen'),('sk','Slovakia','Slovaquie','Slovakije'),('sl','Sierra Leone','Sierra Leone','Sierra Leone'),('sm','San Marino','Saint Marin','San Marino'),('sn','Senegal','Sénégal','Senegal'),('so','Somalia','Somalie','Somalië'),('sr','Suriname','Suriname','Suriname'),('st','Sao Tome and Príncipe','Sao Tomé-et-Principe','Sao Tomé en Principe'),('sv','El Salvador','El Salvador','El Salvador'),('sy','Syria (Arab Rep.)','Syrie (Rép.Arabe)','Syrië (Arab.Rep.)'),('sz','Swaziland','Swaziland','Swaziland'),('tc','Turks and Caicos Islands','Îles Turques-et-Caïques','Turks en Caicoseilanden'),('td','Chad','Tchad','Tsjaad'),('tf','French Southern Territories','Terres australes et antarctiques françaises','Franse zuidelijke gebieden'),('tg','Togo','Togo','Togo'),('th','Thailand','Thaïlande','Thailand'),('tj','Tajikistan','Tadjikistan','Tadzjikistan'),('tk','Tokelau','Tokelau','Tokelau'),('tl','Timor-Leste','Timor oriental','Oost-Timor'),('tm','Turkmenistan','Turkménistan','Turkmenistan'),('tn','Tunisia','Tunisie','Tunesiè'),('to','Tonga','Tonga','Tonga'),('tr','Turkey','Turquie','Turkije'),('tt','Trinidad and Tobago','Trinité-et-Tobago','Trinidad en Tobago'),('tv','Tuvalu','Tuvalu','Tuvalu'),('tw','Taiwan (Rep.China)','Taïwan (Rép.Chine)','Taiwan (Rep.China)'),('tz','Tanzania (U.Rep.)','Tanzanie (Rép.U.)','Tanzania (U.Rep.)'),('ua','Ukraine','Ukraine','Oekraine'),('ug','Uganda','Ouganda','Uganda'),('um','United States Minor Outlying Islands','Îles mineures éloignées des États-Unis','Kleine afgelegen eilanden van de US'),('us','United States of America','États-Unis d\'Amérique','Verenigde Staten van Amerika'),('uy','Uruguay','Uruguay','Uruguay'),('uz','Uzbekistan','Ouzbékistan','Oezbekistan'),('va','Vatican City','Vatican (Cité du)','Vaticaanstad'),('vc','Saint Vincent and Grenadines','Saint-Vincent-et-les-Grenadines','Saint Vincent en de Grenadines'),('ve','Venezuela','Venezuela','Venezuela'),('vg','British Virgin Islands','Îles Vierges britanniques','Britse Maagdeneilanden'),('vi','Virgin Islands (US)','Îles Vierges (EU)','Maagdeneilanden (VS)'),('vn','Viet Nam','Viêt Nam','Vietnam'),('vu','Vanuatu','Vanuatu','Vanuatu'),('wf','Wallis and Futuna Islands','Wallis et Futuna','Wallis en Futuna'),('ws','Samoa','Samoa','Samoa'),('ye','Yemen','Yémen','Jemen'),('yt','Mayotte','Mayotte','Mayotte'),('za','South Africa','Afrique du Sud','Zuid-Afrika'),('zm','Zambia','Zambie','Zambia'),('zw','Zimbabwe','Zimbabwe','Zimbabwe');
/*!40000 ALTER TABLE `t_country` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_debate_shade_type`
--

LOCK TABLES `t_debate_shade_type` WRITE;
/*!40000 ALTER TABLE `t_debate_shade_type` DISABLE KEYS */;
INSERT INTO `t_debate_shade_type` (`id_shade`, `en`, `fr`, `nl`) VALUES ('-1', '[Free begin of debate]', '[Début de débat libre]', '[Vrije begin van debate]');
INSERT INTO `t_debate_shade_type` VALUES (0,'Is it true that','Est-il vrai que','Is het waar dat'),(1,'Should we','Faut-il','Moeten we'),(2,'What are the strong and weak points','Quels sont les points forts et faibles','Wat zijn de sterke en zwakke punten'),(3,'A quelles conditions','A quelles conditions','A quelles conditions');
/*!40000 ALTER TABLE `t_debate_shade_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_external_source_name`
--

LOCK TABLES `t_external_source_name` WRITE;
/*!40000 ALTER TABLE `t_external_source_name` DISABLE KEYS */;
INSERT INTO `t_external_source_name` VALUES (0,'Twitter'),(1,'Chrome Extension'),(2,'Firefox Extension');
/*!40000 ALTER TABLE `t_external_source_name` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_gender`
--

LOCK TABLES `t_gender` WRITE;
/*!40000 ALTER TABLE `t_gender` DISABLE KEYS */;
INSERT INTO `t_gender` VALUES ('F','Female','Femme','Vrouw'),('M','Male','Homme','Man'),('X','Neutral','Neutre','Neutraal');
/*!40000 ALTER TABLE `t_gender` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_justification_link_shade_type`
--

LOCK TABLES `t_justification_link_shade_type` WRITE;
/*!40000 ALTER TABLE `t_justification_link_shade_type` DISABLE KEYS */;
INSERT INTO `t_justification_link_shade_type` VALUES (0,'supports','soutient/justifie','ondersteunt'),(1,'rejects','rejette','verwerpt');
/*!40000 ALTER TABLE `t_justification_link_shade_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_language`
--

LOCK TABLES `t_language` WRITE;
/*!40000 ALTER TABLE `t_language` DISABLE KEYS */;
INSERT INTO `t_language` VALUES ('aa','Afar','afar','Afaraf','Afar'),('ab','Abkhaz','abkhaze','аҧсуа бызшәа, аҧсшәа','Abchazisch'),('ae','Avestan','avestique','avesta','Avesta'),('af','Afrikaans','afrikaans','Afrikaans','Afrikaans'),('ak','Akan','akan','Akan','Akan'),('am','Amharic','amharique','አማርኛ','Amhaars'),('an','Aragonese','aragonois','aragonés','Aragonees'),('ar','Arabic','arabe','العربية ','Arabisch'),('as','Assamese','assamais','অসমীয়া','Assamees'),('av','Avaric','avar','авар мацӀ, магӀарул мацӀ','Avaars'),('ay','Aymara','aymara','aymar aru','Aymara'),('az','Azerbaijani','azeri','azərbaycan dili','Azerbeidzjaans'),('ba','Bashkir','bachkir','башҡорт теле','Bashkir'),('be','Belarusian','biélorusse','беларуская мова','Wit Russisch'),('bg','Bulgarian','bulgare','български език','Bulgaars'),('bh','Bihari','berbères','भोजपुरी','Bihari'),('bi','Bislama','bichlamar','Bislama','Bislama'),('bm','Bambara','bambara','bamanankan','Bambarra'),('bn','Bengali','bengali','বাংলা','Bengaals'),('bo','Tibetan','tibétain','བོད་ཡིག','Tibetaans'),('br','Breton','breton','brezhoneg','Bretoens'),('bs','Bosnian','bosniaque','bosanski jezik','Bosnisch'),('ca','Catalan','catalan','català','Catalaans'),('ce','Chechen','tchétchène','нохчийн мотт','Tsjetsjeens'),('ch','Chamorro','chamorro','Chamoru','Chamorro'),('co','Corsican','corse','corsu, lingua corsa','Corsicaans'),('cr','Cree','cree','ᓀᐦᐃᔭᐍᐏᐣ','Cree'),('cs','Czech','tchèque','čeština, český jazyk','Tsjechisch'),('cv','Chuvash','tchouvache','чӑваш чӗлхи','Tsjoevasjisch'),('cy','Welsh','gallois','Cymraeg','Welsh'),('da','Danish','danois','dansk','Deens'),('de','German','allemand','Deutsch','Duits'),('dv','Divehi, Dhivehi, Maldivian','maldivien','ދިވެހި ','Divehi'),('dz','Dzongkha','dzongkha','རྫོང་ཁ','Dzongkha'),('ee','Ewe','éwé','Eʋegbe','Ewe'),('el','Greek (modern)','grec (moderne)','ελληνικά','Grieks'),('en','English','anglais','English','Engels'),('eo','Esperanto','espéranto','Esperanto','Esperanto'),('es','Spanish','espagnol','español','Spaans'),('et','Estonian','estonien','eesti, eesti keel','Ests'),('eu','Basque','basque','euskara, euskera','Baskisch'),('fa','Persian','persan','فارسی ','Perzisch (Farsi)'),('ff','Fulah','peul','Fulfulde, Pulaar, Pular','Fula'),('fi','Finnish','finnois','suomi, suomen kieli','Fins'),('fj','Fijian','fidjien','vosa Vakaviti','Fijisch'),('fo','Faroese','féroïen','føroyskt','Faeršers'),('fr','French','français','français','Frans'),('fy','Western Frisian','frison occidental','Frysk','Westerlauwers Fries'),('ga','Irish','irlandais','Gaeilge','Iers'),('gd','Scottish Gaelic, Gaelic','gaélique écossais, gaélique','Gàidhlig','Schots-Gaelisch'),('gl','Galician','galicien','galego','Galicisch'),('gn','Guarani','guarani','Avañe\'ẽ','Guarani'),('gu','Gujarati','gudjrati','ગુજરાતી','Gujarati'),('gv','Manx','manx, mannois','Gaelg, Gailck','Manx-Gaelisch'),('ha','Hausa','haoussa','(Hausa) هَوُسَ ','Hausa'),('he','Hebrew','hébreu','עברית ','Hebreeuws'),('hi','Hindi','hindi','हिन्दी','Hindi'),('ho','Hiri Motu','hiri motu','Hiri Motu','Hiri Motu'),('hr','Croatian','croate','hrvatski jezik','Kroatisch'),('ht','Haitian, Haitian Creole','haïtien, créole haïtien','Kreyòl ayisyen','Haitiaans Creools'),('hu','Hungarian','hongrois','magyar','Hongaars'),('hy','Armenian','arménien','Հայերեն','Armeens'),('hz','Herero','herero','Otjiherero','Herero'),('id','Indonesian','indonésien','Bahasa Indonesia','Indonesisch'),('ig','Igbo','igbo','Asụsụ Igbo','Igbo'),('ii','Nuosu','yi de Sichuan','Nuosuhxop','Yi'),('ik','Inupiaq','inupiaq','Iñupiaq, Iñupiatun','Inupiak'),('io','Ido','ido','Ido','Ido'),('is','Icelandic','Islandais','Íslenska','IJslands'),('it','Italian','italien','italiano','Italiaans'),('iu','Inuktitut','inuktitut','ᐃᓄᒃᑎᑐᑦ','Inuktitut'),('ja','Japanese','japonais','日本語 (にほんご)','Japans'),('jv','Javanese','javanais','basa Jawa','Javaans'),('ka','Georgian','géorgien','ქართული','Georgisch'),('kg','Kongo','kongo','Kikongo','Kongo'),('ki','Kikuyu, Gikuyu','kikuyu','Gĩkũyũ','Gikuyu'),('kj','Kwanyama, Kuanyama','kwanyama, kuanyama','Kuanyama','Kwanyama'),('kk','Kazakh','kazakh','қазақ тілі','Kazachs'),('kl','Kalaallisut, Greenlandic','groenlandais','kalaallisut, kalaallit oqaasii','Groenlands'),('km','Khmer','khmer','ខ្មែរ, ខេមរភាសា, ភាសាខ្មែរ','Khmer, Cambodjaans'),('kn','Kannada','kannada','ಕನ್ನಡ','Kannada (Kanarees, Kanara)'),('ko','Korean','coréen','한국어, 조선어','Koreaans'),('kr','Kanuri','kanouri','Kanuri','Kanuri'),('ks','Kashmiri','kashmiri','कश्मीरी, كشميري‎','Kasjmiri'),('ku','Kurdish','kurde','Kurdî, كوردی‎','Koerdisch'),('kv','Komi','kom','коми кыв','Zurjeens (Komi)'),('kw','Cornish','cornique','Kernewek','Cornisch'),('ky','Kyrgyz','kirgiz','Кыргызча, Кыргыз тили','Kirgizisch'),('la','Latin','latin','latine, lingua latina','Latijn'),('lb','Luxembourgish, Letzeburgesch','luxembourgeois','Lëtzebuergesch','Luxemburgs'),('lg','Ganda','ganda','Luganda','Luganda'),('ln','Lingala','lingala','Lingála','Lingala'),('lo','Lao','lao','ພາສາລາວ','Laotiaans'),('lt','Lithuanian','lithuanien','lietuvių kalba','Lithouws'),('lu','Luba-Katanga','luba-katanga','Tshiluba','Luba-Katanga'),('lv','Latvian','letton','latviešu valoda','Lets'),('mg','Malagasy','malgache','fiteny malagasy','Plateaumalagasi'),('mh','Marshallese','marshall','Kajin M̧ajeļ','Marshallees'),('mi','Māori','maori','te reo Māori','Maori'),('mk','Macedonian','macédonien','македонски јазик','Macedonisch'),('ml','Malayalam','malayalam','മലയാളം','Malayalam'),('mn','Mongolian','mongole','Монгол хэл','Mongools'),('mr','Marathi (Marāṭhī)','marathe','मराठी','Marathi'),('ms','Malay','malais','bahasa Melayu, بهاس ملايو‎','Maleis'),('mt','Maltese','maltais','Malti','Maltees'),('my','Burmese','birman','ဗမာစာ','Birmaans, Birmees'),('na','Nauru','nauruan','Ekakairũ Naoero','Nauruaans'),('nb','Norwegian Bokmål','norvégien bokmål','Norsk bokmål','Bokmål-Noors'),('nd','Northern Ndebele','ndébélé du Nord','isiNdebele','Noord-Ndebele'),('ne','Nepali','népalais','नेपाली','Nepalees'),('ng','Ndonga','ndonga','Owambo','Ndonga'),('nl','Dutch','néerlandais','Nederlands','Nederlands'),('nn','Norwegian','norvégien','Norsk','Nynorsk (Nieuw-Noors)'),('nr','Southern Ndebele','ndébéle du sud','isiNdebele','Zuid-Ndebele'),('nv','Navajo, Navaho','navaho','Diné bizaad','Navajo'),('oc','Occitan','occitan','occitan, lenga d\'òc','Occitaans'),('oj','Ojibwa','ojibwa','ᐊᓂᔑᓈᐯᒧᐎᓐ','Ojibweg'),('om','Oromo','galla','Afaan Oromoo','Afaan Oromo'),('or','Oriya','oriya','ଓଡ଼ିଆ','Odia (Oriya)'),('os','Ossetian, Ossetic','ossète','ирон æвзаг','Ossetisch'),('pa','Panjabi, Punjabi','pendjabi','ਪੰਜਾਬੀ, پنجابی‎','Punjabi'),('pi','Pāli','pāli','पाऴि','Pali'),('pl','Polish','polonais','język polski, polszczyzna','Pools'),('ps','Pashto, Pushto','pachto','پښتو ','Pasjtoe'),('pt','Portuguese','portugais','português','Portugees'),('qu','Quechua','quechua','Runa Simi, Kichwa','Quechua'),('rm','Romansh','romanche','rumantsch grischun','Reto-Romaans'),('ro','Romanian','roumain','limba română','Roemeens'),('ru','Russian','russe','Русский','Russisch'),('rw','Kinyarwanda','rwanda','Ikinyarwanda','Kinyarwanda'),('sa','Sanskrit','sanskrit','संस्कृतम्','Sanskriet'),('sc','Sardinian','sarde','sardu','Sardijns'),('sd','Sindhi','sindhi','सिन्धी, سنڌي، سندھی‎','Sindhi'),('se','Northern Sami','sami du nord','Davvisámegiella','Noord-Samisch (Noord-Laps)'),('sg','Sango','sango','yângâ tî sängö','Sango'),('si','Sinhala, Sinhalese','singhalais','සිංහල','Singalees'),('sk','Slovak','slovaque','slovenčina, slovenský jazyk','Slovaaks'),('sl','Slovene','slovène','slovenski jezik, slovenščina','Sloveens'),('sm','Samoan','samoan','gagana fa\'a Samoa','Samoaans'),('sn','Shona','shona','Shona','Shona'),('so','Somali','somali','Soomaaliga, af Soomaali','Somalisch'),('sq','Albanian','albanais','Shqip','Albanees'),('sr','Serbian','serbe','српски језик','Servisch'),('ss','Swati','swati','SiSwati','Swazi'),('st','Southern Sotho','sotho du sud','Sesotho','Zuid-Sotho'),('su','Sundanese','soudanais','Basa Sunda','Soendanees'),('sv','Swedish','suédois','svenska','Zweeds'),('sw','Swahili','swahili','Kiswahili','Swahili'),('ta','Tamil','tamoul','தமிழ்','Tamil'),('te','Telugu','télougou','తెలుగు','Telugu, Teloegoe'),('tg','Tajik','tadjik','тоҷикӣ, toçikī, تاجیکی‎','Tadzjieks'),('th','Thai','thaï','ไทย','Thai'),('ti','Tigrinya','tigrigna','ትግርኛ','Tigrinya'),('tk','Turkmen','turkmène','Türkmen, Түркмен','Turkmeens'),('tl','Tagalog','tagalog','Wikang Tagalog','Tagalog'),('tn','Tswana','tswana','Setswana','Tswana'),('to','Tonga','tonga','faka Tonga','Tongaans, Tonga'),('tr','Turkish','turque','Türkçe','Turks'),('ts','Tsonga','tsonga','Xitsonga','Tsonga'),('tt','Tatar','tatar','татар теле, tatar tele','Tataars'),('tw','Twi','twi','Twi','Twi'),('ty','Tahitian','tahïtien','Reo Tahiti','Tahitiaans'),('ug','Uyghur','ouïgour','ئۇيغۇرچە‎, Uyghurche','Oeigoers'),('uk','Ukrainian','ukrainien','українська мова','Oekraëens'),('ur','Urdu','ourdu','اردو ','Urdu'),('uz','Uzbek','ouszbek','Oʻzbek, Ўзбек, أۇزبېك‎','Oezbeeks'),('ve','Venda','venda','Tshivenḓa','Venda'),('vi','Vietnamese','vietnamien','Việt Nam','Vietnamees'),('vo','Volapük','volapük','Volapük','VolapŸk, Volapuk'),('wo','Wolof','wolof','Wollof','Wolof'),('xh','Xhosa','xhosa','isiXhosa','Xhosa'),('yi','Yiddish','yiddish','ייִדיש ','Jiddisch'),('yo','Yoruba','yoruba','Yorùbá','Yoruba'),('za','Zhuang, Chuang','zhuang, chuang','Saɯ cueŋƅ, Saw cuengh','Zhuang, Tsjoeang'),('zh','Chinese','chinois','中文 (Zhōngwén), 汉语, 漢語','Chinees'),('zu','Zulu','zoulou','isiZulu','Zoeloe');
/*!40000 ALTER TABLE `t_language` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_legal_status`
--

LOCK TABLES `t_legal_status` WRITE;
/*!40000 ALTER TABLE `t_legal_status` DISABLE KEYS */;
INSERT INTO `t_legal_status` VALUES (0,'Company','Entreprise','Bedrijf'),(1,'Political party','Parti politique','Politieke Partij'),(2,'Public administration, public authorities, political or consultative decision instance','Administration publique, pouvoir public, instance de décision politique ou instance d’avis','Openbaar bestuur, overheidsinstanties, politieke of adviesbeslissingsinstantie'),(3,'Union, organisational federation, lobby, social movement','Syndicat, fédération d’organisations, lobby ou mouvement social','Unie, organisatorische federatie, lobby, sociale beweging'),(4,'Any other kind of non profit organization','Toute autre type d\'organisation non lucrative','Een ander soort non-profit organisatie'),(5,'Project or event','Projet ou événement','Project of evenement'),(6,'Prize, distinction, election, competition','Prix, distinction, élection, compétition','Prijs, onderscheid, verkiezing, competitie'),(7,'Product, brand','Produit, marque (y compris programme d’étude)','Product, merk'),(8,'Label','Label','Label');
/*!40000 ALTER TABLE `t_legal_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_member_visibility`
--

LOCK TABLES `t_member_visibility` WRITE;
/*!40000 ALTER TABLE `t_member_visibility` DISABLE KEYS */;
INSERT INTO `t_member_visibility` VALUES (0,'details of members are publicly visible and as being part of related group'),(1,'details of members are only visible in related group'),(2,'details of members are not visible, except by group owner');
/*!40000 ALTER TABLE `t_member_visibility` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_modification_status`
--

LOCK TABLES `t_modification_status` WRITE;
/*!40000 ALTER TABLE `t_modification_status` DISABLE KEYS */;
INSERT INTO `t_modification_status` VALUES (0,'CREATE','create','creation','Creëren'),(1,'UPDATE','update','mise à jour','Bijwerken'),(2,'DELETE','delete','suppression','Verwijderen'),(3,'MERGE','merge','fusion','Samenvoegen'),(4,'GROUP_REMOVAL','remove from group','supprimer du groupe','Verwijderen uit de groep');
/*!40000 ALTER TABLE `t_modification_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_picture_licence_type`
--

LOCK TABLES `t_picture_licence_type` WRITE;
/*!40000 ALTER TABLE `t_picture_licence_type` DISABLE KEYS */;
INSERT INTO `t_picture_licence_type` VALUES (-1,'','No licence','Pas de licence','No licence'),(0,'https://creativecommons.org/licenses/by/4.0/','CC BY','CC BY','CC BY'),(1,'https://creativecommons.org/licenses/by-sa/4.0/','CC BY-SA','CC BY-SA','CC BY-SA'),(2,'https://creativecommons.org/licenses/by-nd/4.0/','CC BY-ND','CC BY-ND','CC BY-ND'),(3,'https://creativecommons.org/licenses/by-nc/4.0/','CC BY-NC','CC BY-NC','CC BY-NC'),(4,'https://creativecommons.org/licenses/by-nc-sa/4.0/','CC BY-NC-SA','CC BY-NC-SA','CC BY-NC-SA'),(5,'https://creativecommons.org/licenses/by-nc-nd/4.0/','CC BY-NC-ND','CC BY-NC-ND','CC BY-NC-ND');
/*!40000 ALTER TABLE `t_picture_licence_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_place_type`
--

LOCK TABLES `t_place_type` WRITE;
/*!40000 ALTER TABLE `t_place_type` DISABLE KEYS */;
INSERT INTO `t_place_type` VALUES (0,'Continent','Continent','Continent'),(1,'World region, union of countries','Region du monde, regroupement de pays','Wereldregio, landenunie'),(2,'Country','Pays','Land'),(3,'Region, state','Region, état','Regio, Staat'),(4,'Province, sub-region','Province, sous-région','Provincie, Sub-region'),(5,'City, place','Ville, lieu','Stad, plaats');
/*!40000 ALTER TABLE `t_place_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_position_link_shade_type`
--

LOCK TABLES `t_position_link_shade_type` WRITE;
/*!40000 ALTER TABLE `t_position_link_shade_type` DISABLE KEYS */;
INSERT INTO `t_position_link_shade_type` VALUES (0,'High approbation','Forte approbation',''),(1,'Approbation','approbation',''),(2,'Indecision','Indécision',''),(3,'Disapproval','Désapprobation',''),(4,'High disapproval','Forte désapprobation','');
/*!40000 ALTER TABLE `t_position_link_shade_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_precision_date_type`
--

LOCK TABLES `t_precision_date_type` WRITE;
/*!40000 ALTER TABLE `t_precision_date_type` DISABLE KEYS */;
INSERT INTO `t_precision_date_type` VALUES (0,1,'Exactly since','exactement depuis','Precies sinds'),(1,1,'At least since','au moins depuis','Ten minste sinds'),(2,0,'Exactly until','exactement jusqu’à','Precies tot'),(3,0,'At least until','au moins jusqu’à','Ten minste tot'),(4,0,'Expected until','prévu jusqu’à','Gepland tot'),(5,0,'Ongoing','en cours','Onderweg');
/*!40000 ALTER TABLE `t_precision_date_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_profession_subtype`
--

LOCK TABLES `t_profession_subtype` WRITE;
/*!40000 ALTER TABLE `t_profession_subtype` DISABLE KEYS */;
INSERT INTO `t_profession_subtype` VALUES (0,'HIERARCHY'),(1,'TYPE');
/*!40000 ALTER TABLE `t_profession_subtype` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_profession_type`
--

LOCK TABLES `t_profession_type` WRITE;
/*!40000 ALTER TABLE `t_profession_type` DISABLE KEYS */;
INSERT INTO `t_profession_type` VALUES (0,0,'other','',''),(1,1,'formation','','');
/*!40000 ALTER TABLE `t_profession_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_similarity_link_shade_type`
--

LOCK TABLES `t_similarity_link_shade_type` WRITE;
/*!40000 ALTER TABLE `t_similarity_link_shade_type` DISABLE KEYS */;
INSERT INTO `t_similarity_link_shade_type` VALUES (0,'is similar to','est similaire à','is vergelijkbaar met'),(1,'opposes','s\'oppose à','staat tegenover'),(2,'translates','traduit','vertaalde');
/*!40000 ALTER TABLE `t_similarity_link_shade_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_tag_type`
--

LOCK TABLES `t_tag_type` WRITE;
/*!40000 ALTER TABLE `t_tag_type` DISABLE KEYS */;
INSERT INTO `t_tag_type` VALUES (0,'Simple folder','Dossier normal','Normaal map'),(1,'Category tag','Catégorie','Category tag'),(2,'Sub debate tag','Sub debate','Sub debate tag'),(3,'Social object','Objet social','Social object');
/*!40000 ALTER TABLE `t_tag_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_text_source_type`
--

LOCK TABLES `t_text_source_type` WRITE;
/*!40000 ALTER TABLE `t_text_source_type` DISABLE KEYS */;
INSERT INTO `t_text_source_type` VALUES (0,'Internet / web','Internet / web','Internet / web'),(1,'PDF','PDF','PDF'),(2,'Other','Other','Other');
/*!40000 ALTER TABLE `t_text_source_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_text_type`
--

LOCK TABLES `t_text_type` WRITE;
/*!40000 ALTER TABLE `t_text_type` DISABLE KEYS */;
INSERT INTO `t_text_type` VALUES (0,'Discussion or debate transcript','Entretien ou retranscription de débats','Discussie of transcript van debat'),(1,'Artistic (novel, poem, script,...)','Artistique (roman, poème, scénario,...)','Artistiek (roman, gedicht, script)'),(2,'Informative (statistical or progress report, encyclopaedia, schoolbook,...)','Informatif (rapport statistique ou d\'activité, encyclopédie, manuel scolaire,...)','Informatief (statistisch of voortgangsrapport, encyclopedie, schoolboek)'),(3,'Journalistic (news article, press report,...)','Journalistique (article de presse, reportage,...)','Journalistiek (nieuwsartikel, persbericht)'),(4,'Normative (law, rule, treaty, standard,...)','Normatif (loi, règlement, traité, norme, standard,...)','Normatief (wet, regel, verdrag, standaard)'),(5,'Opinion (defence, editorial, essay, artistic review, critique,...)','Opinion (plaidoyer, éditorial, essai, critique artistique,...)','Opinie (pleidooi, editoriaal, essay, artistieke review, kritiek)'),(6,'Prospective (project, plan, programme,...)','Prospectif (projet, plan, programme,...)','Prospectief (project, plan, programma)'),(7,'Practical (technical manual, tourist guide,...)','Pratique (manuel technique ou pratique, guide touristique,...)','Praktisch (technische handleiding, toeristische gids)'),(8,'Advertising','Publicitaire','Reclame'),(9,'Scientific (scientific article, book or report, Master or Ph.D thesis, market research,...)','Scientifique (article/livre/rapport scientifique, thèse, mémoire, étude de marché,...)','Wetenschappelijk (artikel/boek/wetenschappelijk rapport, thesis, verhandeling, marktonderzoek)'),(10,'Other','Autre','Andere');
/*!40000 ALTER TABLE `t_text_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_text_visibility`
--

LOCK TABLES `t_text_visibility` WRITE;
/*!40000 ALTER TABLE `t_text_visibility` DISABLE KEYS */;
INSERT INTO `t_text_visibility` VALUES (0,'publicly visible','visible publiquement','Openbaar zichtbaar'),(1,'visible for pedagogic purpose only','visible pour un usage pédagogique uniquement','Zichtbaar enkel voor pedagogische doeleinden'),(2,'visible for private usage','visible pour un usage privé','Zichtbaar voor privaat gebruik');
/*!40000 ALTER TABLE `t_text_visibility` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_validation_state`
--

LOCK TABLES `t_validation_state` WRITE;
/*!40000 ALTER TABLE `t_validation_state` DISABLE KEYS */;
INSERT INTO `t_validation_state` VALUES (0,'to validate','à valider','valideren'),(1,'validate','valider','bevestigen'),(2,'not validate','non validé','niet gevalideerd');
/*!40000 ALTER TABLE `t_validation_state` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_warned_word`
--

LOCK TABLES `t_warned_word` WRITE;
/*!40000 ALTER TABLE `t_warned_word` DISABLE KEYS */;
INSERT INTO `t_warned_word` VALUES (0,0,0,'fr','ex-'),(1,0,0,'fr','ancien'),(2,1,0,'fr','honoraire'),(3,1,0,'fr','émérite'),(4,1,0,'fr','fédéral'),(5,1,2,'fr','je'),(6,1,2,'fr','tu'),(7,1,2,'fr','il'),(8,1,2,'fr','elle'),(9,1,2,'fr','nous'),(10,1,2,'fr','vous'),(11,1,2,'fr','ils'),(12,1,2,'fr','elles'),(13,1,2,'fr','on'),(14,1,2,'fr','lui'),(15,0,2,'fr','estime'),(16,0,2,'fr','estimons'),(17,0,2,'fr','pense'),(18,0,2,'fr','pensons'),(19,3,0,'fr','la '),(20,3,0,'fr','le '),(21,3,0,'fr','les '),(22,3,0,'fr','l\''),(1000,0,0,'en','ex-'),(1001,0,0,'en','former'),(1002,1,0,'en','honorary'),(1003,1,0,'en','emeritus'),(1004,1,0,'en','federal'),(1005,1,2,'en','i'),(1006,1,2,'en','you'),(1007,1,2,'en','he'),(1008,1,2,'en','she'),(1009,1,2,'en','we'),(1010,1,2,'en','they'),(1011,1,2,'en','it'),(1012,1,2,'en','him'),(1013,1,2,'en','her'),(1014,0,2,'en','think'),(2000,0,0,'nl','ex-'),(2001,0,0,'nl','voormalige'),(2002,1,0,'nl','ere-'),(2003,1,0,'nl','emeritus'),(2004,1,0,'nl','federaal'),(2005,1,2,'nl','ik'),(2006,1,2,'nl','je'),(2007,1,2,'nl','hij'),(2008,1,2,'nl','ze'),(2009,1,2,'nl','we'),(2010,1,2,'nl','jullie'),(2011,1,2,'nl','u'),(2012,1,2,'nl','het'),(2013,1,2,'nl','hen'),(2014,1,2,'nl','hun'),(2015,0,2,'nl','denk'),(2016,0,2,'nl','denken');
/*!40000 ALTER TABLE `t_warned_word` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_warned_word_context_type`
--

LOCK TABLES `t_warned_word_context_type` WRITE;
/*!40000 ALTER TABLE `t_warned_word_context_type` DISABLE KEYS */;
INSERT INTO `t_warned_word_context_type` VALUES (0,'profession','profession','beroep'),(1,'argument','affirmation','argument'),(2,'text','texte','tekst'),(3,'tag','tag','tag');
/*!40000 ALTER TABLE `t_warned_word_context_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_warned_word_type`
--

LOCK TABLES `t_warned_word_type` WRITE;
/*!40000 ALTER TABLE `t_warned_word_type` DISABLE KEYS */;
INSERT INTO `t_warned_word_type` VALUES (0,'begin','début','begin'),(1,'end','fin','end'),(2,'all','tout','all');
/*!40000 ALTER TABLE `t_warned_word_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_word_gender`
--

LOCK TABLES `t_word_gender` WRITE;
/*!40000 ALTER TABLE `t_word_gender` DISABLE KEYS */;
INSERT INTO `t_word_gender` VALUES ('F','feminine','féminin','vrouwelijk'),('M','masculine','masculin','mannelijk'),('N','neuter','neutre','neutraal');
/*!40000 ALTER TABLE `t_word_gender` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-04-14  9:57:32
