CREATE TABLE cdc_series_script
(
  series_id    INT NOT NULL AUTO_INCREMENT PRIMARY KEY, 
  series_name  VARCHAR(200),
  series_script TEXT
);

DELETE FROM cdc_series_script;

INSERT INTO cdc_series_script (series_name, series_script) VALUES ('DT Standard 5-Dose Series Business Rules', 'Series: DT Standard 5-Dose Series Business Rules
Target Disease: DTaP

---------------------------------------------------------------------
Dose: Dose 1
---------------------------------------------------------------------
Age
     Absolute Minimum Age:      6 weeks - 4 days
     Minimum Age:               6 weeks
     Earliest Recommended age:  2 months
     Latest Recommended Age:    n/a
     Maximum Age:               7 years

Interval
     Reference Dose Number:         1
     Absolute Minimum Interval:     28 days - 4 days
     Minimum Interval:              28 days
     Earliest Recommended Interval: 
     Latest Recommended Interval:   

Live Virus Conflict
     Conflicting Vaccine Type CVX: n/a
     Conflict Begin Interval:      
     Conflict End Interval:        

Preferable Vaccine Type
     Vaccine Type CVX:       DTaP (20)
     Vaccine Type Begin Age: 6 weeks - 4 days
     Vaccine Type End Age:   7 years – 1 day
     Trade Name:             n/a
     Vaccine Type MVX:       
Preferable Vaccine Type
     Vaccine Type CVX:       DTaP, 5 pertussis antigens (106)
     Vaccine Type Begin Age: 6 weeks - 4 days
     Vaccine Type End Age:   7 years – 1 day
     Trade Name:             n/a
     Vaccine Type MVX:       
Preferable Vaccine Type
     Vaccine Type CVX:       DTaP-Hep B-IPV (110)
     Vaccine Type Begin Age: 6 weeks - 4 days
     Vaccine Type End Age:   7 years – 1 day
     Trade Name:             n/a
     Vaccine Type MVX:       
Preferable Vaccine Type
     Vaccine Type CVX:       DTaP-Hib-IPV (120)
     Vaccine Type Begin Age: 6 weeks - 4 days
     Vaccine Type End Age:   5 years – 1 day
     Trade Name:             n/a
     Vaccine Type MVX:       




Allowable Vaccine Type
     Vaccine Type CVX:       DTP (1)
     Vaccine Type Begin Age: 
     Vaccine Type End Age:   
Allowable Vaccine Type
     Vaccine Type CVX:       DTP-Hib (22)
     Vaccine Type Begin Age: 
     Vaccine Type End Age:   
Allowable Vaccine Type
     Vaccine Type CVX:       DTaP-Hib (50)
     Vaccine Type Begin Age: 
     Vaccine Type End Age:   
Allowable Vaccine Type
     Vaccine Type CVX:       DTaP-IPV (130)
     Vaccine Type Begin Age: 
     Vaccine Type End Age:   




Amount of Antigen
     Amount of Antigen: 0.5 mL
     Amount Begin Age:  n/a
     Amount End Age:    
---------------------------------------------------------------------
Dose: Dose 2
---------------------------------------------------------------------
Age
     Absolute Minimum Age:      20 weeks - 4 days
     Minimum Age:               10 weeks
     Earliest Recommended age:  4 months
     Latest Recommended Age:    n/a
     Maximum Age:               7 years

Interval
     Reference Dose Number:         1
     Absolute Minimum Interval:     4 weeks - 4 days
     Minimum Interval:              4 weeks
     Earliest Recommended Interval: 2 months
     Latest Recommended Interval:   n/a

Live Virus Conflict
     Conflicting Vaccine Type CVX: n/a
     Conflict Begin Interval:      
     Conflict End Interval:        

Preferable Vaccine Type
     Vaccine Type CVX:       DTaP (20)
     Vaccine Type Begin Age: n/a
     Vaccine Type End Age:   n/a
     Trade Name:             n/a
     Vaccine Type MVX:       
Preferable Vaccine Type
     Vaccine Type CVX:       DTaP, 5 pertussis antigens (106)
     Vaccine Type Begin Age: n/a
     Vaccine Type End Age:   n/a
     Trade Name:             n/a
     Vaccine Type MVX:       
Preferable Vaccine Type
     Vaccine Type CVX:       DTaP-Hep B-IPV (110)
     Vaccine Type Begin Age: n/a
     Vaccine Type End Age:   n/a
     Trade Name:             n/a
     Vaccine Type MVX:       
Preferable Vaccine Type
     Vaccine Type CVX:       DTaP-Hib-IPV (120)
     Vaccine Type Begin Age: n/a
     Vaccine Type End Age:   n/a
     Trade Name:             n/a
     Vaccine Type MVX:       




Allowable Vaccine Type
     Vaccine Type CVX:       DTP (1)
     Vaccine Type Begin Age: 
     Vaccine Type End Age:   
Allowable Vaccine Type
     Vaccine Type CVX:       DTP-Hib (22)
     Vaccine Type Begin Age: 
     Vaccine Type End Age:   
Allowable Vaccine Type
     Vaccine Type CVX:       DTaP-Hib (50)
     Vaccine Type Begin Age: 
     Vaccine Type End Age:   
Allowable Vaccine Type
     Vaccine Type CVX:       DTaP-IPV (130)
     Vaccine Type Begin Age: 
     Vaccine Type End Age:   




Amount of Antigen
     Amount of Antigen: 0.5 mL
     Amount Begin Age:  n/a
     Amount End Age:    
');