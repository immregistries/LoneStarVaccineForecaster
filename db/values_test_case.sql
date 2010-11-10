REPLACE INTO test_case (case_id, case_label, case_description, case_source, group_code, patient_last, patient_first, patient_dob, patient_sex) VALUES 
(2, 'Hep B Test 1', 'Hep B # 1 given at birth = valid.  Next due in 1-2 months', 'CT', 'HepB', 'Adams', 'Abigail', '2006-04-01', 'F'),
(3, 'Hep B Test 2', 'Hep B # 2 administered 24 days after # 1 (4 day grace period applies) = valid.  ', 'CT', 'HepB', 'Boxer', 'Ben', '2006-02-01', 'M'),

(5, 'Hep B Test 3', 'Hep B # 3 administered at birth, at one month and at 164 days = valid.  ', 'CT', 'HepB', 'Carr', 'Caleb', '2005-12-01', 'M'),


(8, 'Hep B Test 4', 'Hep B # 2 given less than 24 days after # 1 = invalid', 'CT', 'HepB', 'Drury', 'David', '2005-08-01', 'M'),

(10, 'Hep B Test 5', 'Hep B # 3 administered less than 8 weeks after # 2 = invalid ', 'CT', 'HepB', 'Eakins', 'Emily', '2005-06-01', 'F'),


(13, 'Hep B Test 6', 'Hep B # 3 given at less than 164 days of age is invalid', 'CT', 'HepB', 'Frannie', 'Fox', '2006-01-01', 'F'),


(16, 'Hep B Test 7', 'Minimum interval between Hep 1 and Hep 3 is l6 weeks', 'CT', 'HepB', 'Sacheti', 'Julius', '2004-07-01', 'M'),


(19, 'Hep B Test 8', 'Using the two dose hep dosage and schedule for adolescents 11-15, dose # 2 should be recommended 4 months after #1', 'CT', 'HepB', 'Corden', 'David', '1992-05-05', 'M'),
(20, 'Hep B Test 9', '2 dose hep administered correctly to adolescent 11-15 results in "series complete"', 'CT', 'HepB', 'Foss', 'Jonathan', '1993-04-07', 'M'),

(22, 'Hep B Test 10', 'If no minimum 4 mo. between doses 1 and  2 in the two dose adoles Hep schedule, there needs to be l6 wk interval between dose 1 and next valid dose ', 'CT', 'HepB', 'Malone', 'Patrick', '1992-05-01', 'M'),

(24, 'Hep B Test 11', 'Recombivax administered to adolescent 11-15 without minimum 4 month interval; unspecified Hep administered less than 8 wks later.  Needs one more.', 'CT', 'HepB', 'Malone', 'Seamus', '1992-05-01', 'M'),


(27, 'Hep B Test 12', '2 doses of Rcmbvx adult product were given to a l7 year old.  Needs one more pediatric formulation.', 'CT', 'HepB', 'Foss', 'Samuel', '1998-09-16', 'M'),

(29, 'Hep B Test 13', 'Hep at birth. Comvax at 6 weeks and 6 months = Hep B series complete', 'CT', 'HepB', 'Wilcox', 'Emma', '2004-02-04', 'F'),


(32, 'Hep B Test 14', 'Comvax at 2,4, and 12 months old = series complete for Hep B', 'CT', 'HepB', 'Gilfond', 'George', '2002-05-15', 'M'),


(35, 'Hep B Test 15', 'Less than a l6 week interval between # 1 and # 3 =invalid #3', 'CT', 'HepB', 'Myers', 'Mickey', '2002-05-15', 'M'),


(38, 'Hep B Test 16', 'Hep # 3 must be administered on or after 24 weeks of age.  Hep at birth, Comvax at 6 weeks and 3 months results in need for another Hep', 'CT', 'HepB', 'Huston', 'Ryan', '2005-01-01', 'M'),


(41, 'VZ Test 1', 'Vz can be administered at one year of age (minus 4 days)', 'CT', 'Var', 'Sacco', 'Gabriella', '2004-07-01', 'F'),
(42, 'VZ Test 2', 'Minimum age for administration of Vz is one year. Vaccine administered greater than 4 days before 1st birthday is invalid.', 'CT', 'Var', 'Taggart', 'Merritt', '2003-11-10', 'F'),
(43, 'VZ Test 3', 'If MMR and VZ are not administered on the same day, the minimum interval is 28 days between doses, nothing less', 'CT', 'Var', 'Sullivan', 'Molly', '2005-03-17', 'F'),
(44, 'VZ Test 4', 'If there is less than a 28 day interval between an MMR and VZ, the second is invalid and must be repeated.  No grace period applies.', 'CT', 'Var', 'Sullivan', 'Colleen', '2005-03-17', 'F'),

(46, 'VZ Test 5', 'Children 13 years of age or older should receive 2 doses of Vz at least 4 weeks apart, 4 day grace period can apply', 'CT', 'Var', 'Englehart', 'Elizabeth', '1992-01-01', 'F'),
(47, 'VZ Test 6', 'Children ages 13 or older should receive 2 doses of VZ', 'CT', 'Var', 'Englehart', 'Sarah', '1992-01-01', 'F'),
(48, 'VZ Test 7', 'Minimum interval between dose 1 and dose 2 of VZ in children age 13 or older is 4 weeks, 4 day grace period can apply', 'CT', 'Var', 'Englehart', 'Hannah', '1992-01-01', 'F'),

(50, 'VZ Test 8', 'An interval of less than 4 weeks between dose 1 and dose 2 of Vz in adolescents renders the second dose invalid', 'CT', 'Var', 'Mulroy', 'Molly', '1992-01-01', 'F'),

(52, 'VZ Test 9', 'Live vaccines, MMR and VZ, administered on the same day are both valid if minimum age is met', 'CT', 'Var', 'Newt', 'Kathryn', '2004-08-08', 'F'),

(54, 'VZ Test 10', 'For children under 13, the minimum interval between two doses of VZ is 12 weeks but second not re-given if at least 28 days after first', 'CT', 'Var', 'Lagay', 'Karen', '2002-11-11', 'F'),

(56, 'VZ Test 11', 'MMR and VZ administered less than 4 weeks apart renders the second invalid', 'CT', 'Var', 'Lagay', 'Grace', '2005-07-07', 'F'),

(58, 'VZ Test 12', 'Hx of Vz should be considered "series complete" or "exempt by hx of disease."  No vaccine recommendation should appear.', 'CT', 'Var', 'Grey', 'Haley', '2004-05-01', 'F'),
(59, 'VZ Test 13', 'Child with a titer for VZ should be considered "series immune" and should not have recommendations for any vz vaccination', 'CT', 'Var', 'Grey', 'Shelby', '2005-06-01', 'F'),
(60, 'VZ Test 14', 'Forecast for child whose parent refuses VZ for child will remain active; child will continue to show as needing the vaccine', 'CT', 'Var', 'Payne', 'Mamaisa', '2004-03-18', 'F'),

(62, '3 Dose HIB Test 1', 'The earliest age for administration of HIB is 6 weeks old.  Before that is invalid.', 'CT', '3Hib', 'West', 'Chantel', '2004-01-01', 'F'),

(64, '3 Dose HIB Test 2', 'HIB administered at 6 weeks and 6 months are valid; another due on/after first birthday', 'CT', '3Hib', 'Emma', 'Wilcox', '2004-02-04', 'F'),


(67, '3 Dose HIB Test 3', 'HIB (Comvax) at 2,4,12 months is a complete series', 'CT', '3Hib', 'Gilfond', 'George', '2004-05-15', 'M'),


(70, '3 Dose HIB Test 4', 'Dose 3 of Comvax must be administered on or after 12 months of age; must be at least 8 weeks between dose 2 and 3', 'CT', '3Hib', 'Vickers', 'Victoria', '2004-05-15', 'F'),


(73, '3 Dose HIB Test 5', '3 Comvax required for series completion with 3rd on or after 12 months of age at least 8 weeks after # 2', 'CT', '3Hib', 'Huston', 'Ryan', '2005-01-01', 'M'),


(76, '3 Dose HIB Test 6', 'Less than 4 weeks between dose 1 and 2 = dose 2 is invalid', 'CT', '3Hib', 'King', 'Gavin', '2004-10-10', 'M'),

(78, '3 Dose HIB Test 7', '2 doses of HIB required if first is administered at 12-14 months', 'CT', '3Hib', 'Hines', 'Frannie', '2005-06-01', 'F'),
(79, '3 Dose HIB Test 8', 'If first HIB is given at 12-14 months of age, series is completed with a booster administered 8 weeks later', 'CT', '3Hib', 'Stick', 'Fletcher', '2005-01-11', 'M'),

(81, '3 Dose HIB Test 9', 'If first HIB is administered at 12-14 months of age, the interval to the next dose is 8 weeks', 'CT', '3Hib', 'Hurley', 'Heather', '2005-01-01', 'F'),

(83, '3 Dose HIB Test 10', '1 dose of any HIB product administered on or after 15 months of age = series complete', 'CT', '3Hib', 'Isaakson', 'Iona', '2005-01-11', 'F'),
(84, '3 Dose HIB Test 11', 'Hib administered before 6 weeks of age is invalid', 'CT', '3Hib', 'Hunter', 'Harold', '2004-01-01', 'M'),
(85, '3 Dose HIB Test 12', 'HIB administered at 6 weeks and 6 months old are valid.  Third HIB needed on or after 12 months of age', 'CT', '3Hib', 'Eakins', 'Eamon', '2004-02-04', 'M'),

(87, '3 Dose HIB Test 13', 'Pedvax at 2, 4, and 12 months = series complete', 'CT', '3Hib', 'Kuric', 'Katie', '2004-05-15', 'F'),


(90, '3 Dose HIB Test 14', '3rd dose of Pedvax must be on/after 12 months of age and at least 8 weeks after #2 ', 'CT', '3Hib', 'Potsdam', 'Phillip', '2004-05-15', 'M'),


(93, '3 Dose HIB Test 15', 'Less than 4 weeks between dose 1 and dose 2 renders dose 2 invalid', 'CT', '3Hib', 'Stephan', 'King', '2004-10-10', 'M'),

(95, '3 Dose HIB Test 16', 'If the first HIB of any product is administered at 12-14 months of age, a booster is needed in two months', 'CT', '3Hib', 'Lakes', 'Lauren', '2005-06-01', 'F'),
(96, '3 Dose HIB Test 17', 'If first dose of any HIB product is administered at 12-14 months and is followed by a booster in 2 months, series is complete', 'CT', '3Hib', 'Gurley', 'George', '2005-01-10', 'M'),

(98, '3 Dose HIB Test 18', 'If first dose of any HIB product is administered at 12-14 months old, the interval to # 2 is 8 weeks. Less than 8 wks=invalid', 'CT', '3Hib', 'Lynch', 'Heather', '2005-01-10', 'F'),

(100, '3 Dose HIB Test 19', '1 dose of any HIB product administered on or after 15 months of age results in HIB series completion', 'CT', '3Hib', 'Merrill', 'Mary', '2005-01-11', 'F'),
(101, '3 Dose HIB Test 20', '1 dose of any HIB product administered on or after 15 months of age results in HIB series completion', 'CT', '3Hib', 'Moffett', 'Martha', '2005-02-01', 'F'),
(102, '3 Dose HIB Test 21', 'Hib does not display as recommended for child over age 5 even if child did not have the # of doses recommended when under 5 ', 'CT', '3Hib', 'Moffett', 'Molly', '2003-02-01', 'F'),
(103, '4 Dose HIB Test 1', 'Hib 1 given at 6 weeks of age is valid.  Next due in 4-8 weeks as long as child is 10 weeks old', 'CT', '4Hib', 'Adams', 'Abigail', '2006-04-01', 'F'),
(104, '4 Dose HIB Test 2', 'Dose # 2 administered 4 weeks after dose 1 = valid.', 'CT', '4Hib', 'Boxer', 'Ben', '2006-02-01', 'M'),

(106, '4 Dose HIB Test 3', 'HIB # 3 given 4 weeks after # 2 is valid if child is at least 14 weeks old', 'CT', '4Hib', 'Carr', 'Caleb', '2005-12-01', 'M'),


(109, '4 Dose HIB Test 4', 'If a primary HIB dose is administered to a child 2-6 mo old, the child will need 3 primary doses with a booster at 12-15 months old.', 'CT', '4Hib', 'Drury', 'David', '2005-08-01', 'M'),


(112, '4 Dose HIB Test 5', 'HIB # 4 given at 12 months of age and at least 8 weeks after #3=valid and "series complete"', 'CT', '4Hib', 'Eakins', 'Emilie', '2005-06-01', 'F'),



(116, '4 Dose HIB Test 6', 'If HIB dose #1 is given at 7 months of age, accelerated and recommended is in  4 weeks per catch up schedule', 'CT', '4Hib', 'Fox', 'Frannie', '2005-11-10', 'F'),
(117, '4 Dose HIB Test 7', 'If first two doses of HIB are given at 7 and 9 months of age,  # 3 should be no sooner than 12 months of age ', 'CT', '4Hib', 'Garth', 'Garrett', '2005-09-10', 'M'),

(119, '4 Dose HIB Test 8', 'A 12 month old child with 1 HIB prior to 12 months and one on/after 12 months needs one more HIB. Minimal interval 8 weeks.', 'CT', '4Hib', 'Hunt', 'Heather', '2005-06-01', 'F'),

(121, '4 Dose HIB Test 9', 'If HIB #1 is given at 12 mos, accelerated and recommendation should be the same for dose 2', 'CT', '4Hib', 'Iverson', 'Isaac', '2005-06-01', 'F'),
(122, '4 Dose HIB Test 10', 'HibTiter of ActHIB #1 given at 12 months of age.  Forecast for #2 is for 8  weeks later', 'CT', '4Hib', 'Johnson', 'Jared', '2005-06-01', 'M'),
(123, '4 Dose HIB Test 11', 'HibTiter or Act Hib at 12 and 14 months = "series complete"', 'CT', '4Hib', 'Kelly', 'Kari', '2005-04-01', 'F'),

(125, '4 Dose HIB Test 12', 'Hib # 1 administered before 6 weeks of age is invalid', 'CT', '4Hib', 'Peterson', 'Paul', '2006-05-01', 'M'),
(126, '4 Dose HIB Test 13', '#2 administered less than 4 weeks after #1=invalid.  ', 'CT', '4Hib', 'Quest', 'Quiero', '2006-03-04', 'M'),

(128, '4 Dose HIB Test 14', '#2 administered at younger than 10 weeks of age = invalid', 'CT', '4Hib', 'Rikert', 'Rachel', '2006-02-26', 'F'),

(130, '4 Dose HIB Test 15', 'Dose #3 administered at less than 14 weeks of age=invalid', 'CT', '4Hib', 'Shepherd', 'Sam', '2006-01-06', 'M'),


(133, '4 Dose HIB Test 16', '#3 given less than 4 weeks after #2=invalid.  Forecast for valid #3 should be 4 weeks after invalid #3', 'CT', '4Hib', 'Tierny', 'Thomas', '2006-01-06', 'M'),


(136, '4 Dose HIB Test 17', 'Dose #4 administered before 12 months of age=invalid', 'CT', '4Hib', 'Vieux', 'Victoria', '2005-04-04', 'F'),



(140, '4 Dose HIB Test 18', '#1 at 13 months and #2 at 14 months.  Forecast should call for one more in 8 weeks from #2 which was invalid', 'CT', '4Hib', 'Windsor', 'William', '2005-02-01', 'M'),

(142, '4 Dose HIB Test 19', '#4 given less than 8 weeks after #3 = invalid.  Forecast for valid #4 should be 8 weeks after invalid #4 and not before 12 months of age', 'CT', '4Hib', 'Ulin', 'Ulysses', '2005-04-07', 'M'),



(146, '4 Dose HIB Test 20', 'Mixed HIBs require 4 doses', 'CT', '4Hib', 'Jackson', 'Jeanette', '2005-01-12', 'F'),


(149, '4 Dose HIB Test 21', 'AAP Lapsed Series Rule: if current age is 7-11 mo with one prior HIB, child needs 1 now and 1 in 2 months if the child is at least 12 months old', 'CT', '4Hib', 'Watts', 'Jesse', '2007-05-17', 'M'),

(151, '4 Dose HIB Test 22', 'If current age of child is 7-11 months and child has had 2 doses of HIB, child needs dose in 4 weeks and dose 8 wks later', 'CT', '4Hib', 'Watts', 'Jennifer', '2006-04-27', 'F'),

(153, '4 Dose HIB Test 23', 'Hib no longer displays as required for child now over 5 who did not have number of recommended HIBS for a child under age 5. ', 'CT', '4Hib', 'Higgens', 'Henry', '2000-02-11', 'M'),













(167, 'DTP Product Test 1', '#1 given at 6 weeks of age is valid.  Next due in 4-6 weeks.', 'CT', 'DTaP', 'Abigail', 'Adams', '2006-04-01', 'F'),
(168, 'DTP Product Test 2', 'Dose # 2 given 28 days after #1 is valid.  Next due in 4-8 weeks', 'CT', 'DTaP', 'Boxer', 'Ben', '2006-02-01', 'M'),

(170, 'DTP Product Test 3', 'Dose #3 given 4 weeks after #2 is valid if child''s age is at least 14 weeks.  Next due on/after 12 months of age', 'CT', 'DTaP', 'Carr', 'Caleb', '2005-12-01', 'M'),


(173, 'DTP Product Test 4', 'Dose 4 given at 12 months of age and at least 6 months after # 3 is valid ', 'CT', 'DTaP', 'Eakins', 'Emily', '2005-06-01', 'F'),



(177, 'DTP Product Test 5', 'Dose 4 given at 12 months of age and 4 months after #3 need not be re-administered', 'CT', 'DTaP', 'Fielding', 'Fiona', '2004-11-10', 'F'),



(181, 'DTP Product Test 6', '5 year old with 4 DTaPs before age 4 needs a booster', 'CT', 'DTaP', 'Gallo', 'Gabriella', '2001-04-04', 'F'),



(185, 'DTP Product Test 7', '5 year old with 3 DTaPs before age 4 needs only 1 booster before adolescence', 'CT', 'DTaP', 'Hankins', 'Hannah', '2001-05-06', 'F'),



(189, 'DTP Product Test 8', '5 year old child with no previous DTaPs needs 4 DTaPs.  Intervals of 4 wks, 4 wks, 6 mos and 6 mos complete child until adolescence', 'CT', 'DTaP', 'Ireland', 'Ian', '2000-02-25', 'M'),



(193, 'DTP Product Test 9', 'Child inadvertently administered TriHibit as a primary dose.  DTaP=valid but HIB is invalid.', 'CT', 'DTaP', 'Jensen', 'Johanna', '2005-06-01', 'F'),

(195, 'DTP Product Test 10', 'Dose 1 given before 6 weeks of age=invalid', 'CT', 'DTaP', 'Peterson', 'Paul', '2006-05-01', 'M'),
(196, 'DTP Product Test 11', 'Minimum interval between # 1 and # 2 is 28 days. Administration before the minimum is invalid (4 day grace can apply)', 'CT', 'DTaP', 'Quest', 'Quiero', '2006-03-04', 'M'),

(198, 'DTP Product Test 12', 'Dose 2 given at less than 10 weeks of age=invalid', 'CT', 'DTaP', 'Rikert', 'Rachel', '2006-02-26', 'F'),

(200, 'DTP Product Test 13', '# 3 given at less than 14 weeks of age = invalid', 'CT', 'DTaP', 'Shepherd', 'Sam', '2006-01-06', 'M'),


(203, 'DTP Product Test 14', 'Dose #4 administered before 12 months of age=invalid', 'CT', 'DTaP', 'Vieux', 'Victoria', '2005-04-04', 'F'),



(207, 'DTP Product Test 15', 'Child administered DT not DTaP for encephalopathy.  Pertussis recorded as not adminstered. Nothing in this product line should be recommended for 5 years.', 'CT', 'DTaP', 'Durr', 'David', '2001-02-02', 'M'),




(212, 'DTP Product Test 16', 'Child now 7 had one DTaP before 12 months of age.  Needs 3 more Td.  Interval between doses 1 and 2 of Td is 4 weeks.', 'CT', 'DTaP', 'Foss', 'Harriet', '1999-09-16', 'F'),

(214, 'DTP Product Test 17', 'Child now 7 had first DTaP before 12 mos of age.  Needs 3 more Td.  Between dose 3  and booster, the interval is 6 months.', 'CT', 'DTaP', 'Abbott', 'Hannah', '1999-03-16', 'F'),


(217, 'DTP Product Test 18 ', 'Child now 7 had  DTaP # 1 after 12 months of age.  Needs  2 Td to complete primary series.  Needs 6 mo. between doses 2 and 3', 'CT', 'DTaP', 'Kelsey', 'Ken', '1999-05-13', 'M'),


(220, 'DTP Product Test 19', 'Previously unvaccinated child age 7 or older needs 3 primary doses of Td with 6 months between dose 2 and dose 3', 'CT', 'DTaP', 'Lucas', 'Lulu', '1998-09-09', 'F'),


(223, 'DPT Product Test 20', 'Td #2 administered 4 weeks after #1=valid', 'CT', 'DTaP', 'Milktoast', 'Martin', '1998-07-07', 'M'),

(225, 'DTP Product Test 21', 'Td # 2 administered less than 4 weeks after #1 = invalid', 'CT', 'DTaP', 'Milktoast', 'Margaret', '1998-07-07', 'F'),

(227, 'DTP Product Test 22', 'Td # 3 administered 6 months after # 2 = valid. Tdap recommended in 5 years', 'CT', 'DTaP', 'Nerd', 'Nancy', '1997-05-05', 'F'),


(230, 'DTP Test Product 23', 'Td # 3 administered less than 6 months after # 2 is invalid as final dose in primary series', 'CT', 'DTaP', 'Oliver', 'Owen', '1997-07-24', 'M'),


(233, 'DTP Product Test 24', 'Recommendation for Tdap for adolescents 11-12 years old:  5 years replacing 10 year interval', 'CT', 'DTaP', 'Christison', 'Kate', '1988-04-04', 'F'),




(238, 'DTP Product Test 25', 'Tetramune constituted valid DTP and HIB vaccinations if spaced properly.  ', 'CT', 'DTaP', 'Carroll', 'Leslie', '1995-05-05', 'F'),



(242, 'DTP Product Test 26', 'Tetramune constituted valid DTP and HIB vaccinations if administered properly.  ', 'CT', 'DTaP', 'Robertson', 'Marc', '1999-12-12', 'M'),




(247, 'DTP Product Test 27', 'Boostrix is correctly administered to children ages 10-18 if 5 years have passed since last DTaP vaccination', 'CT', 'DTaP', 'Jericho', 'Joshua 2', '1993-03-03', 'M'),





(253, 'DTP Product Test 28', 'Boostrix is correctly administered to children ages 10-18 if 5 years have passed since last DTP/ product', 'CT', 'DTaP', 'Bullfrog', 'Jeremiah', '1993-03-03', 'M'),




(258, 'DTP Product Test 29', 'Boostrix is correctly administered to children ages 10-18 if 5 years have passed since last DTP product', 'CT', 'DTaP', 'Chariot', 'Elijah', '1996-03-03', 'M'),




(263, 'DTP Product Test 30', 'Minimum age for Boostrix is 10 years old', 'CT', 'DTaP', 'Wheeler', 'Ezechial', '1996-08-08', 'M'),





(269, 'DTP Product Test 31', 'Minimum age for Boostrix is 10 years old', 'CT', 'DTaP', 'Suffering', 'Sarah', '1996-08-08', 'F'),





(275, 'DTP Product Test 32', 'Maximum age for Boostrix is 18', 'CT', 'DTaP', 'Follows', 'Rachel', '1987-04-07', 'F'),





(281, 'DTP Product Test 33', 'Adacel is correctly administered to ages 11-65 if 5 years have passed since last DTaP vaccination', 'CT', 'DTaP', 'Hankins', 'Henry', '1995-03-05', 'M'),





(287, 'DTP Product Test 34', 'Adacel is correctly administered to ages 11-64 if 5 years have passed since last DTaP vaccination', 'CT', 'DTaP', 'Hankins', 'Hallie', '1995-03-05', 'F'),




(292, 'DTP Product Test 35', 'Adacel is correctly administered to ages 11-64 if 5 years have passed since last DTaP/ vaccination: test 5 year interval', 'CT', 'DTaP', 'Slewfoot', 'Sally', '1995-03-05', 'F'),





(298, 'DTP Product Test 36', 'There is a minimum 5 year interval between last DTP/DTaP product and Adacel', 'CT', 'DTaP', 'Hunter', 'Harry', '1995-03-05', 'M'),





(304, 'DTP Product Test 37', 'Minimum age for Adacel is 11', 'CT', 'DTaP', 'Tee', 'Mikey', '1995-03-05', 'M'),





(310, 'DTP Product Test 38', '4 month minimum between DTaP 3 and DTaP 4', 'CT', 'DTaP', 'McCook', 'Mallory', '2004-02-01', 'M'),



(314, 'Pediarix Test 1', 'The minimum age for dose 1 is 6 weeks ', 'CT', 'Ped', 'Adams', 'Andrew', '2006-04-04', 'M'),


(317, 'Pediarix Test 2', 'Mimimum interval between 1 and 2 is 4 weeks (24 days). If given before 6 months of age, Hep is not valid as #3 ', 'CT', 'Ped', 'Buckshorn', 'Bonnie', '2006-02-01', 'F'),


(320, 'Pediarix Test 3', 'Pediarix dose # 3 given at 6 months of age (164 days) is valid for all components if intervals have been valid.', 'CT', 'Ped', 'Christison', 'Caron', '2006-05-13', 'F'),



(324, 'Pediarix Test 4', 'Minimum age for dose # 1 is 6 weeks (38 days) old. If given before 6 weeks, Hep B may count', 'CT', 'Ped', 'Drake', 'Dottie', '2006-05-06', 'F'),


(327, 'Pediarix Test 5', 'Minimum age for dose 2 is 10 weeks old.  If given before 10 weeks, DTaP and IPV not valid; Hep may be depending on previous doses', 'CT', 'Ped', 'Windsor', 'Charles', '2006-10-10', 'M'),


(330, 'Pediarix Test 6', 'Minimum interval between doses 1 and 2 is 4 weeks (24 days).  If less than that, is invalid for all components.', 'CT', 'Ped', 'Windsor', 'Anne', '2005-11-01', 'F'),


(333, 'Pediarix Test 7', 'Minimum age for dose 3 for DTaP and IPV components is 14 weeks of age.', 'CT', 'Ped', 'Scott', 'Mary', '2005-06-01', 'F'),


(336, 'Pediarix Test 8', 'Minimum interval between doses 2 and 3 is 4 weeks (24 days).  Less than that is invalid.', 'CT', 'Ped', 'Windsor', 'Edward', '2005-04-01', 'M'),


(339, 'Pediarix Test 9', 'A dose of Pediarix inadvertently administered as the 4th dose of DTaP need not be readministered', 'CT', 'Ped', 'Erickson', 'Eric', '2005-03-15', 'M'),



(343, 'Pediarix Test 10', 'A dose of Pediarix inadvertently administered as the 5th dose of DTaP/4th dose of IPV need not be readministered', 'CT', 'Ped', 'Flop', 'Fabien', '2002-02-02', 'M'),




(348, 'Trihibit Test 1', 'Trihibit is a valid 4th dose of DTaP and HIB if first three doses of both are valid', 'CT', 'Tri', 'Somers', 'Suzanne', '2004-02-02', 'F'),






(355, 'Trihibit Test 2', 'Trihibit is not valid as the final or first  dose in a child with no prior HIB vaccinations', 'CT', 'Tri', 'Trainor', 'Tamara', '2004-08-06', 'F'),

(357, 'Trihibit Test 3', 'Trihibit can be HIB booster at 12-15 months for child with 2 previous Comvax', 'CT', 'Tri', 'Underwood', 'Ula', '2003-11-15', 'F'),



(361, 'Trihibit Test 4', 'Trihibit can be HIB booster at 12-15 months for child with 2 previous Pedvax', 'CT', 'Tri', 'Viceroy', 'Vicky', '2004-10-14', 'F'),


(364, 'Trihibit Test 5', 'TriHibit can be booster at 12-15 months for chid with 3 previous HibTiter or Act Hib', 'CT', 'Tri', 'Wilcox', 'Wilhemina', '2004-08-22', 'F'),






(371, 'Trihibit Test 6', 'TriHibit not approved as 5th dose of DTaP = invalid. Per communication with NIP/Dr Atkinson 3/16/07 a repeat dose is needed ', 'CT', 'Tri', 'Daggart', 'Emelis', '2001-10-08', 'F'),




(376, 'Trihibit Test 7', 'Trihibit can be used as the booster @ 15 months old if child had a HIB at least 2 months earlier', 'CT', 'Tri', 'Gallo', 'Antonio', '2004-07-02', 'M'),

(378, 'Hep A Test 1', 'Minimum age for dose one is 12 months of age.  Dose administered before 12 months of age is invalid.', 'CT', 'HepA', 'Carr', 'Caleb', '2005-12-01', 'M'),
(379, 'Hep A Test 2', 'Minimum age for dose one is 12 months of age.  Dose administered at or after 12 months of age is valid.', 'CT', 'HepA', 'Eakins', 'Emily', '2005-06-01', 'F'),
(380, 'Hep A Test 3', 'There is a minimum 6 month interval between dose 1 and dose 2.  Less than that is invalid', 'CT', 'HepA', 'Fielding', 'Fiona', '2004-11-10', 'F'),

(382, 'Hep A Test 4', 'There is a minimum 6 month interval between dose 1 and dose 2.  ', 'CT', 'HepA', 'Gallo', 'Gabriella', '2001-04-04', 'F'),

(384, 'Flu Test 1', 'A young child should have a second dose of influenza vaccine the first year he/she receives the vaccination', 'CT', 'Flu', 'Loren', 'Gabrielle', '2005-09-01', 'F'),
(385, 'Flu Test 2', 'Child under 2 received 2 doses the first flu season.  One should be recommended for next year.', 'CT', 'Flu', 'Taggart', 'Ashley', '2005-05-10', 'F'),

(387, 'Flu Test 3', 'Child received only one dose her first flu season.  Should receive only one in subsequent years', 'CT', 'Flu', 'Martin', 'Mary', '2004-04-04', 'F'),

(389, 'Flu Test 4', 'There is a one month interval between dose 1 and dose 2', 'CT', 'Flu', 'Cox', 'Rose', '2005-05-01', 'F'),

(391, 'Polio Test 1', 'Minimum age for dose #1 is 6 weeks (38 days with grace period applied).', 'CT', 'IPV', 'Adams', 'Abigail', '2006-04-01', 'F'),
(392, 'Polio Test 2', 'Minimum interval between dose # 1 and # 2 is 4 weeks and dose # 2 must not be given before 10 weeks of age.  4 day grace can apply', 'CT', 'IPV', 'Boxer', 'Ben', '2006-02-01', 'M'),

(394, 'Polio Test 3', '#3 given 4 weeks after # 2 is valid if not before 14 weeks of age.  Four day grace can apply', 'CT', 'IPV', 'Carr', 'Caleb', '2005-12-01', 'M'),


(397, 'Polio Test 4', 'A dose of polio still required in CT at 4 years of age for series to be complete.', 'CT', 'IPV', 'Duncan', 'David', '1995-06-01', 'M'),



(401, 'Polio Test 5', 'Dose #4 given at 4 years of age = valid and "series complete"', 'CT', 'IPV', 'Edington', 'Emma', '2001-06-01', 'F'),



(405, 'Polio Test 6', 'Dose # 1 given at less than 6 weeks (38 days) of age is invalid', 'CT', 'IPV', 'Fox', 'Frannie', '2005-11-10', 'F'),
(406, 'Polio Test 7', '# 2 given at less than 10 weeks of age is invalid', 'CT', 'IPV', 'Garth', 'Garrett', '2005-09-10', 'M'),

(408, 'Polio Test 8', '# 3 given at less than 14 weeks of age is invalid', 'CT', 'IPV', 'Iverson', 'Isaac', '2005-06-01', 'M'),


(411, 'Polio Test 9', 'Dose # 2 given less than 4 weeks (24 days) after # 1 is invalid', 'CT', 'IPV', 'Johnson', 'Jared', '2005-06-01', 'M'),

(413, 'Polio Test 10', 'Dose # 3 given less than 4 weeks after # 2 is invalid. Four day grace can apply.', 'CT', 'IPV', 'Kelly', 'Kari', '2005-04-01', 'F'),


(416, 'Polio Test 11', 'Child over age 7 with 3 IPV''s before age 4 needs one more IPV', 'CT', 'IPV', 'Adams', 'Angie', '1999-06-23', 'F'),


(419, 'Polio Test 12', 'Child over 4 had IPV and OPV mixed with dose #3 after age 4.  Still needs 4th dose if under 18 years of age.', 'CT', 'IPV', 'Castaneda', 'Carmen', '1994-10-16', 'F'),


(422, 'Polio Test 13', 'IPV dose # 4 not needed if # 3 is given on or after the 4th birthday.  Should be "series complete"', 'CT', 'IPV', 'Dowle', 'David', '1997-03-18', 'M'),


(425, 'Polio Test 14', 'Four mixed vaccines with at least one on or after age 4 is "series complete"', 'CT', 'IPV', 'Earling', 'Erica', '1999-04-13', 'F'),



(429, 'Polio Test 15', 'Unspecified polios administered age appropriately must be counted as valid vaccines', 'CT', 'IPV', 'Carrington', 'Connor', '1995-07-10', 'M'),




(434, 'MNCL Test 1', 'MPSV 4 is not licensed for children under 2.  ', 'CT', 'Meni', 'Pringle', 'Sophia', '2005-02-05', 'F'),
(435, 'MNCL Test 2', 'An MPSV administered to younger adolescents may require an additional meningococcal vaccine', 'CT', 'Meni', 'Evan', 'Katie', '1994-04-09', 'F'),
(436, 'MNCL Test 3', 'The MCV is licensed for ages 11 and older.  Administration at younger ages results in an invalid dose. ', 'CT', 'Meni', 'Evan', 'Lucy', '1996-09-06', 'F'),
(437, 'MNCL Test 4', 'An MCV administered at age 11 or older may not require a subsequent dose of any meningococcal vaccine', 'CT', 'Meni', 'Lane', 'Maggie', '1995-08-22', 'F'),
(438, 'MMR Test 1', 'The minimum age for dose 1 is 12 months old.  #1 on or after first birthday is valid', 'CT', 'MMR', 'Sacco', 'Gabriella', '2004-07-01', 'F'),
(439, 'MMR Test 2', 'The minimum age for dose 1 of MMR is 12 months old. If administered before 12 months of age (minus 4 day grace), is invalid', 'CT', 'MMR', 'Taggart', 'Merritt', '2003-11-10', 'F'),
(440, 'MMR Test 3', 'The minimum interval between dose 1 and dose 2 is 28 days.', 'CT', 'MMR', 'Cox', 'Rose', '2004-05-01', 'F'),

(442, 'MMR/Vz Test 4', 'Live vaccines administered on the same day are both valid', 'CT', 'MMR', 'Newt', 'Kathryn', '2004-08-08', 'F'),

(444, 'MMR Test 5', 'There is a 4 week minimum interval between MMR 1 and MMR 2.  Four day grace can apply so interval of less than 23 days is invalid.', 'CT', 'MMR', 'Gayne', 'Pamela', '2004-04-13', 'F'),

(446, 'MMR Test 6', 'The minimum interval between doses 1 and 2 of MMR is 4 weeks.  With application of 4 day grace, is 24 days.', 'CT', 'MMR', 'Taggart', 'Allison', '2004-09-06', 'F'),

(448, 'MMRV Test 7', 'Minimum age for MMRV is 12 months of age', 'CT', 'MMR', 'Salaski', 'Ellie', '2005-07-01', 'F'),

(450, 'MMRV Test 8', 'Minimum age for administration of MMRV is 12 months of age.  Before 361 days (grace applied) is invalid.', 'CT', 'MMR', 'Taggart', 'Charlotte', '2005-11-10', 'F'),

(452, 'MMRV Test 9', 'Minimal interval between MMRV and MMRV for children under age 13 is 12 weeks, nothing less. Child must be 15 mo. for 2nd VZ to be valid.', 'CT', 'MMR', 'Cox', 'Shelby', '2004-05-01', 'F'),

(454, 'MMRV Test 10', 'Minimal interval between dose 1 and dose 2 of MMRV is 12 weeks, nothing else in a child under 13.  Four day grace does not apply.', 'CT', 'MMR', 'Taggart', 'Ida Two', '2004-09-06', 'F'),

(456, 'MMR/MMRV Mix Test 11', 'An MMRV administered 4 weeks after an MMR is valid as a second MMR and a first Vz', 'CT', 'MMR', 'Caron', 'Amanda', '2005-02-02', 'F'),

(458, 'MMR/MMRV Mix Test 12', 'The interval between MMR and Varicella is 4 weeks nothing less; however in using MMRV, MMR may be valid when VZ isn''t', 'CT', 'MMR', 'Caron', 'Gilbert', '2005-02-02', 'M'),

(460, 'MMR/MMRV Mix Test 13', 'Minimal interval between MMRV and a subsequent MMR is 28 days nothing less', 'CT', 'MMR', 'Caron', 'Pamela', '2005-02-02', 'F'),

(462, 'MMR/MMRV Mix Test 14', 'Minimal interval between MMR and subsequent MMRV is 4 weeks, nothing less', 'CT', 'MMR', 'Caron', 'Rosemarie', '2005-02-02', 'F'),

(464, 'MMRV Test 15', 'The minimal interval between 2 doses of MMRV is 12 weeks', 'CT', 'MMR', 'Caron', 'Delima', '2005-02-02', 'F'),

(466, 'MMR/Vz Mixed Test 16', 'The minimal interval between Vz in any vaccine is 12 weeks for children under age 13.  Four day grace does not apply.', 'CT', 'MMR', 'Christison', 'Hugh', '2005-05-22', 'M'),

(468, 'MMR/V Mixed Test 17', 'MMRV properly spaced after invalid Vz results in valid MMR and Vz', 'CT', 'MMR', 'Christison', 'Louise', '2005-05-22', 'F'),

(470, 'MMR/V Mixed Test 18', 'MMRV properly spaced after valid MMR and invalid Vz results in MMR 2 and Vz dose 1', 'CT', 'MMR', 'Christison', 'John', '2005-05-22', 'M'),


(473, 'MMR/V Mixed Test 19', 'MMRV properly spaced after invalid MMR and valid VZ results in MMR dose 1 and Vz dose 2', 'CT', 'MMR', 'Christison', 'David', '2001-05-22', 'M'),


(476, 'MMR/V Mixed Test 20', 'Invalid MMRV after invalid MMR results in no valid doses', 'CT', 'MMR', 'Hines', 'Frances', '2005-07-16', 'F'),

(478, 'MMR/V Mixed Test 21', 'Invalid MMRV (less than 4 weeks) after invalid Vz results in no valid Vz', 'CT', 'MMR', 'Hines', 'William', '2005-07-16', 'M'),

(480, 'MMR Test 22', 'Parent refused Measles only antigen. Child not up-to-date', 'CT', 'MMR', 'Spott', 'Red', '2004-09-09', 'M'),











(492, 'MMR Test 23', 'Single antigen vaccines appropriately spaced and all administered  will be considered as a  valid dose of the combination vaccine.', 'CT', 'MMR', 'Spott', 'Harry', '2004-09-09', 'M'),



(496, 'MMR Test 24', 'Correctly administered and complete single antigen vaccines followed by a valid MMRV shall result in "series complete" for MMR', 'CT', 'MMR', 'Spott', 'Jane Sally', '2004-09-09', 'F'),




(501, 'MMR Test 25', 'Two rounds of correctly administered and complete single antigen vaccines shall result in "series complete" for MMR', 'CT', 'MMR', 'Whitehouse', 'Merritt', '2004-04-01', 'M'),





(507, 'MMR Test 26', 'Refused MMR results in child appearing late for vaccination', 'CT', 'MMR', 'Allerton', 'Isaac', '2006-01-01', 'M'),
(508, 'PCV 7 Test 1', 'Minimum age for dose 1 is 6 weeks (38 days).', 'CT', 'Pneu', 'Adams', 'Abigail', '2006-04-01', 'F'),
(509, 'PCV-7 Test 2', 'Minimum interval between doses 1 and 2 is 4 weeks (4 day grace can apply).  Child must be at least 10 weeks old for dose 2', 'CT', 'Pneu', 'Boxer', 'Ben', '2006-02-01', 'M'),

(511, 'PCV-7 Test 3', 'Minimum interval between doses 2 and 3 is 4 weeks.  Child must be at least 14 weeks old for dose 3.', 'CT', 'Pneu', 'Carr', 'Caleb', '2005-12-01', 'M'),


(514, 'PCV-7 Test 4', 'Dose 4 given at 12 months of age and at least 8 weeks after dose 3 is valid and the series is complete', 'CT', 'Pneu', 'Duncan', 'David', '2005-06-01', 'M'),



(518, 'PCV-7 Test 5', 'Minimum age for dose 1 is 6 weeks (38 days) old.  Administration before that is invalid.', 'CT', 'Pneu', 'Fox', 'Frannie', '2005-11-10', 'F'),
(519, 'PCV-7 Test 6', 'Minimum age for administration of dose 2 is 10 weeks with a minimum of 4 weeks since dose 1.  Less than that is invalid.', 'CT', 'Pneu', 'Garth', 'Garrett', '2005-09-10', 'M'),

(521, 'PCV-7 Test 7', 'The minimum interval between dose 1 and dose 2 is 4 weeks (4 day grace can apply).  Less than that is invalid.', 'CT', 'Pneu', 'Johnson', 'Jared', '2005-06-01', 'M'),

(523, 'PCV-7 Test 8', 'Minimum interval between dose 2 and 3 is 4 weeks (4 day grace can apply).  Less than that is invalid.', 'CT', 'Pneu', 'Kelly', 'Kari', '2005-04-01', 'F'),


(526, 'PCV-7 Test 9', 'Minimum age for administration of dose 3 is 14 weeks with at least 4 weeks since dose 2.  Less than that is invalid.', 'CT', 'Pneu', 'Iverson', 'Isaac', '2005-06-01', 'M'),


(529, 'PCV-7 Test 10', 'Minimum age for dose 4 is 12 months (4 day grace can apply)', 'CT', 'Pneu', 'Johnson', 'Jeremy', '2005-06-01', 'M'),



(533, 'PCV-7 Test 11', 'The minimum interval between dose 3 and 4 is 8 weeks.  Less than that is invalid.', 'CT', 'Pneu', 'Kelly', 'Caroline', '2005-04-01', 'F'),



(537, 'PCV-7 Test 12', 'If first dose is administered at 7 months or older, child needs only a second dose in 4 weeks and a booster at 12-15 months with an 8 wk interval.', 'CT', 'Pneu', 'Lakes', 'Lauren', '2005-06-01', 'F'),


(540, 'PCV-7 Test 13', 'If first dose is administered at 12 months of age or older, a second and final dose is needed in 8 weeks.', 'CT', 'Pneu', 'Bolduc', 'Mick', '2005-01-01', 'M'),

(542, 'PCV-7 Test 14', 'If first dose is administered at 12 months of age or older, a second and final dose is needed in 8 weeks.', 'CT', 'Pneu', 'Olsen', 'Olivia', '2005-03-18', 'F'),

(544, 'PCV-7 Test 15', 'A healthy child who receives a first dose on or after 24 months of age, does not require additional doses', 'CT', 'Pneu', 'Nickelson', 'Nick', '2004-03-01', 'M'),
(545, 'RV Test 1b', 'Minimum age for dose 1 is 6 weeks (38 days) old=valid', 'CT', 'Rota', 'Adams', 'Abbie', '2008-04-01', 'F'),
(546, 'RV Test 2', 'Dose #2 given 4 weeks (24 days) after # 1 is valid if the child is at least l0 weeks old', 'CT', 'Rota', 'Boxer', 'Ben', '2006-02-01', 'M'),

(548, 'RV Test 3', 'Dose # 3 given 4 weeks after #2 is valid if the child is at least 14 weeks old', 'CT', 'Rota', 'Carr', 'Caleb', '2005-12-01', 'M'),


(551, 'RV Test 4', 'Dose # 1 given before 6 weeks of age (38 days) is invalid', 'CT', 'Rota', 'Fox', 'Frannie', '2005-11-10', 'F'),
(552, 'RV Test 5', 'The minimum age for dose # 2 is 10 weeks of age.  Administration before 10 weeks (66 days) is invalid.', 'CT', 'Rota', 'Garth', 'Garrett', '2005-09-10', 'M'),

(554, 'RV Test 6', 'The minimum age between doses # 1 and # 2 is 4 weeks (4 day grace can apply).  Administration before that is invalid.', 'CT', 'Rota', 'Johnson', 'Jared', '2005-06-01', 'M'),

(556, 'RV Test 7', 'The minimum age between doses # 2 and # 3 is 4 weeks (4 day grace can apply).  Administration before that is invalid.', 'CT', 'Rota', 'Kelly', 'Kari', '2005-04-01', 'F'),


(559, 'RV Test 8', 'The minimum age for dose # 3 is 14 weeks of age.  Administration before 10 weeks (66 days) is invalid.', 'CT', 'Rota', 'Iverson', 'Isaac', '2005-06-01', 'M'),


(562, 'RV Test 9', 'Rotavirus vaccine is not to be administered after 32 weeks of age regardless of the number of doses that have been administered  ', 'CT', 'Rota', 'Prince', 'Edward', '2006-04-07', 'M'),

(564, 'RV Test 10', 'If rotavirus dose 1 is inadvertently administered at greater than 13 weeks, the series should be completed as per the schedule', 'CT', 'Rota', 'Windsor', 'Elizabeth', '2006-05-01', 'F'),
(565, 'HPV Test 1', 'Gardisil can be administered to females 9-26.  Is invalid if administered to females under age 9', 'CT', 'HPV', 'Contrary', 'Mary', '1997-10-10', 'F'),
(566, 'HPV Test 2', 'Gardisil is licensed for females 9-26. ', 'CT', 'HPV', 'Safe', 'Ima', '1997-12-08', 'F'),
(567, 'HPV Test 3', 'The minimal interval between dose #1 and dose # 2 is 4 weeks', 'CT', 'HPV', 'Christmas', 'Eva', '1997-04-04', 'F'),

(569, 'HPV Test 4', 'Earliest administration of # 3 after # 1 is 12 weeks ', 'CT', 'HPV', 'Newyer', 'Happi', '1997-01-01', 'F'),


(572, 'HPV Test 5', 'The minimal interval between dose 1 and dose 2 is 4 weeks.  If less than 4 weeks (4 day grace can apply), vaccination is invalid', 'CT', 'HPV', 'Green', 'Holly', '1996-12-25', 'F'),

(574, 'HPV Test 6', 'HPV is recommended for females 11 and older', 'CT', 'HPV', 'Berry', 'Hollie', '1996-12-25', 'F'),
(575, 'HPV Test 7', 'HPV  vaccine is currently licensed for females only.  Should not show as a recommendation for males', 'CT', 'HPV', 'Springer', 'Fynn', '1996-04-04', 'M'),
(576, 'HPV Test 8', 'Cervarix HPV is licensed for females ages 10-25 at months 0, 1 and 6.  If given to female less than 10, is invalid', 'CT', 'HPV', 'White', 'Noelle', '1997-12-25', 'F'),
(577, 'HPV Test 9', 'Cervarix is licensed for females 10-25 ', 'CT', 'HPV', 'Winter', 'Solstice', '1995-12-21', 'F'),
(578, 'HPV Test 10', 'The minimum interval between dose 1 and dose 2 is 4 weeks', 'CT', 'HPV', 'Gold', 'Autumn', '1995-10-21', 'F'),

(580, 'HPV Test 11', 'Earliest administration of # 3 after # 1 is 104 days (24 days + 80 days) if spacing has been adequate in each interval', 'CT', 'HPV', 'Flowers', 'Spring', '1996-03-21', 'F'),


(583, 'HPV Test 12', 'The minimum interval between dose 1 and dose 2 is 4 weeks.  Less than this interval results in an invalid dose.', 'CT', 'HPV', 'Waters', 'Summer', '1995-07-21', 'F'),

(585, 'HPV Test 13', 'The minimum interval between dose 1 and dose 3 is 104 days.  Less than this results in an invalid dose', 'CT', 'HPV', 'Cleere', 'Brooke', '1996-03-21', 'F'),


(588, 'HPV Test 14', 'Cervarix is currently licensed for females only.  Should not show as a recommendation for a male', 'CT', 'HPV', 'Springer', 'Simon', '1996-04-04', 'M');
