package org.tch.forecast.core.server;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tch.forecast.core.DateTime;
import org.tch.forecast.core.DecisionProcessFormat;
import org.tch.forecast.core.ImmunizationForecastDataBean;
import org.tch.forecast.core.ImmunizationInterface;
import org.tch.forecast.core.SoftwareVersion;
import org.tch.forecast.core.TimePeriod;
import org.tch.forecast.core.VaccinationDoseDataBean;
import org.tch.forecast.core.api.impl.CvxCode;
import org.tch.forecast.core.api.impl.CvxCodes;
import org.tch.forecast.core.api.impl.VaccineForecastManager;
import org.tch.forecast.core.model.Immunization;
import org.tch.forecast.core.model.ImmunizationMDA;

public class CaretForecaster
{

  private static final String SECTION_SEPARATOR = "~~~";
  private static final String DOSE_SEPARATOR = "|||";
  private static final String DOSE_OVERRIDE_EXCLUDED = "2";
  private static final String DOSE_OVERRIDE_INCLUDED = "1";
  private static final String DOSE_OVERRIDE_DEFAULT = "0";

  private static final String FORECASTING_MODE_ACCEPTABLE = "1";
  private static final String FORECASTING_MODE_RECOMMENDED = "0";

  private static final String HL7_CODE_ERROR_CODE_NONE = "0";
  private static final String HL7_CODE_ERROR_CODE_UNRECOGNIZED = "1";
  private static final String HL7_CODE_ERROR_CODE_UNSUPPORTED = "2";

  private static final String DOSE_OVERRIDE_FORCE_VALID = "1";
  private static final String DOSE_OVERRIDE_FORCE_INVALID = "2";

  private static final boolean USE_EARLY_DUE_AND_OVERDUE = true;

  //  1 Date used for forecast
  private static final int FIELD_IN_CASE_DETAIL_01_DATE_USED_FOR_FORECAST = 1;
  //  2 Forecasting Mode
  private static final int FIELD_IN_CASE_DETAIL_02_FORECASTING_MODE = 2;
  //  3 Version
  private static final int FIELD_IN_CASE_DETAIL_03_USE_4_DAY_GRACE_PERIOD = 3;
  //  4 Use 4-day Grace Period
  private static final int FIELD_IN_CASE_DETAIL_04_RESERVED_FOR_FUTURE_USE = 4;
  //  5 Reserved for future use
  private static final int FIELD_IN_CASE_DETAIL_05_RESERVED_FOR_FUTURE_USE = 5;
  //  6 Personal ID - Chart#
  private static final int FIELD_IN_CASE_DETAIL_06_PERSONAL_ID = 6;
  //  7 User Note (Patient IEN)
  private static final int FIELD_IN_CASE_DETAIL_07_USER_NOTE = 7;
  //  8 Date of Birth
  private static final int FIELD_IN_CASE_DETAIL_08_DATE_OF_BIRTH = 8;
  //  9 Gender
  private static final int FIELD_IN_CASE_DETAIL_09_GENDER = 9;
  //  10  Mother HBsAg Status
  private static final int FIELD_IN_CASE_DETAIL_10_MOTHER_HBSAG_STATUS = 10; // NOT USED YET
  //  11  Pertussis Contraindicated Indication
  private static final int FIELD_IN_CASE_DETAIL_11_PERTUSSIS_CONTRAINDICATED_INDICATION = 11;
  //  12  Diphtheria Contraindicated Indication
  private static final int FIELD_IN_CASE_DETAIL_12_DIPHTHERIA_CONTRAINDICATED_INDICATION = 12;
  //  13  Tetanus Contraindicated Indication
  private static final int FIELD_IN_CASE_DETAIL_13_TETANUS_CONTRAINDICATED_INDICATION = 13;
  //  14  Hib Contraindicated Indication
  private static final int FIELD_IN_CASE_DETAIL_14_HIB_CONTRAINDICATED_INDICATION = 14;
  //  15  HBIG Contraindicated Indication
  private static final int FIELD_IN_CASE_DETAIL_15_HBIG_CONTRAINDICATED_INDICATION = 15; // NOT USED YET
  //  16  HepB Contraindicated Indication
  private static final int FIELD_IN_CASE_DETAIL_16_HEPB_CONTRAINDICATED_INDICATION = 16;
  //  17  OPV Contraindicated Indication
  private static final int FIELD_IN_CASE_DETAIL_17_OPV_CONTRAINDICATED_INDICATION = 17; // NOT USED YET
  //  18  IPV Contraindicated Indication
  private static final int FIELD_IN_CASE_DETAIL_18_IPV_CONTRAINDICATED_INDICATION = 18;
  //  19  Measles Contraindicated Indication
  private static final int FIELD_IN_CASE_DETAIL_19_MEASLES_CONTRAINDICATED_INDICATION = 19;
  //  20  Mumps Contraindicated Indication
  private static final int FIELD_IN_CASE_DETAIL_20_MUMPS_CONTRAINDICATED_INDICATION = 20;
  //  21  Rubella Contraindicated Indication
  private static final int FIELD_IN_CASE_DETAIL_21_RUBELLA_CONTRAINDICATED_INDICATION = 21;
  //  22  Varicella Contraindicated Indication
  private static final int FIELD_IN_CASE_DETAIL_22_VARICELLA_CONTRAINDICATED_INDICATION = 22;
  //  23  HepA Contraindicated Indication
  private static final int FIELD_IN_CASE_DETAIL_23_HEPA_CONTRAINDICATED_INDICATION = 23;
  //  24  Rv Contraindicated Indication
  private static final int FIELD_IN_CASE_DETAIL_24_RV_CONTRAINDICATED_INDICATION = 24;
  //  25  S-Pn Contraindicated Indication
  private static final int FIELD_IN_CASE_DETAIL_25_S_PN_CONTRAINDICATED_INDICATION = 25;
  //  26  Influenza Contraindicated Indication
  private static final int FIELD_IN_CASE_DETAIL_26_INFLUENZA_CONTRAINDICATED_INDICATION = 26;
  //  27  Meningococcal Contraindicated Indication
  private static final int FIELD_IN_CASE_DETAIL_27_MENINGOCOCCAL_CONTRAINDICATED_INDICATION = 27;
  //  28  HPV Contraindicated Indication
  private static final int FIELD_IN_CASE_DETAIL_28_HPV_CONTRAINDICATED_INDICATION = 28;
  //  29  H1N1 Contraindicated Indication
  private static final int FIELD_IN_CASE_DETAIL_29_H1N1_CONTRAINDICATED_INDICATION = 29; // NOT USED YET

  private static final int FIELD_IN_CASE_DETAIL_30_ZOSTER_CONTRAINDICATION = 30;

  private static final String FIELD_IN_CASE_DETAIL_SEP = SECTION_SEPARATOR;

  //  1 Dose Note
  private static final int FIELD_IN_INPUT_DOSE_1_DOSE_NOTE = 1;
  //  2 Dose HL7 Code
  private static final int FIELD_IN_INPUT_DOSE_2_DOSE_HL7_CODE = 2;
  //  3 Date of Dose Administration
  private static final int FIELD_IN_INPUT_DOSE_3_DATE_OF_DOSE_ADMINISTRATION = 3;
  //  4 Dose Override
  private static final int FIELD_IN_INPUT_DOSE_4_DOSE_OVERRIDE = 4;
  //  5 Reserved for future use
  private static final int FIELD_IN_INPUT_DOSE_5_RESERVED_FOR_FUTURE_USE = 5;
  //  6 Reserved for future use
  private static final int FIELD_IN_INPUT_DOSE_6_RESERVED_FOR_FUTURE_USE = 6;

  private static final String FIELD_IN_INPUT_DOSE_SEP = "|||";

  // ++ Out fields +++++++++++++++++++++++++++++++++++++
  private static final int FIELD_OUT_00_ERROR = 0;

  //  1 Copyright Notice
  private static final int FIELD_OUT_CASE_DATA_01_COPYRIGHT_NOTICE = 1;
  //  2 Run Date and Time
  private static final int FIELD_OUT_CASE_DATA_02_RUN_DATE_AND_TIME = 2;
  //  3 Date Used for Forecast 
  private static final int FIELD_OUT_CASE_DATA_03_DATE_USED_FOR_FORECAST = 3;
  //  4 Forecasting Mode
  private static final int FIELD_OUT_CASE_DATA_04_FORECASTING_MODE = 4;
  //  5 Version
  private static final int FIELD_OUT_CASE_DATA_05_VERSION = 5;
  //  6 Reserved for future use
  private static final int FIELD_OUT_CASE_DATA_06_FUTURE_USE = 6;
  //  7 Reserved for future use
  private static final int FIELD_OUT_CASE_DATA_07_FUTURE_USE = 7;
  //  8 Reserved for future use
  private static final int FIELD_OUT_CASE_DATA_08_FUTURE_USE = 8;
  //  9 Personal ID - Chart#
  private static final int FIELD_OUT_CASE_DATA_09_PERSONAL_ID = 9;
  //  10  User Note (Patient IEN)
  private static final int FIELD_OUT_CASE_DATA_10_USER_NOTE = 10;
  //  11  Date of Birth
  private static final int FIELD_OUT_CASE_DATA_11_DATE_OF_BIRTH = 11;
  //  12  Gender
  private static final int FIELD_OUT_CASE_DATA_12_GENDER = 12;

  //  1 Dose Note
  private static final int FIELD_OUT_INPUT_DOSE_01_DOSE_NOTE = 1;
  //  2 Dose Input HL7 Code
  private static final int FIELD_OUT_INPUT_DOSE_02_DOSE_INPUT_HL7_CODE = 2;
  //  3 Dose Input HL7 Code Error Code
  private static final int FIELD_OUT_INPUT_DOSE_03_DOSE_INPUT_HL7_CODE_ERROR_CODE = 3;
  //  4 Date of Dose Administration
  private static final int FIELD_OUT_INPUT_DOSE_04_DATE_OF_DOSE_ADMINISTRATION = 4;
  //  5 Dose Override
  private static final int FIELD_OUT_INPUT_DOSE_05_DOSE_OVERRIDE = 5;
  //  6 Reserved for future use
  private static final int FIELD_OUT_INPUT_DOSE_06_INVLIAD_DOSE_AND_REASON = 6;
  //  7  Reserved for future use
  private static final int FIELD_OUT_INPUT_DOSE_07_RESERVED_FOR_FUTURE_USE = 7;

  //  1 Dose Due IMM/Serve Series Code
  private static final int FIELD_OUT_DOSE_DUE_1_DOSE_DUE_IMM_SERVE_SERIES_CODE = 1;
  //  2 Dose Due Dose Number
  private static final int FIELD_OUT_DOSE_DUE_02_DOSE_DUE_DOSE_NUMBER = 2;
  //  3 Dose Due Past Due Indicator
  private static final int FIELD_OUT_DOSE_DUE_03_DOSE_DUE_PAST_DUE_INDICATOR = 3;
  //  4 Dose Due Minimum Date
  private static final int FIELD_OUT_DOSE_DUE_04_DOSE_DUE_MINIMUM_DATE = 4;
  //  5 Dose Due Recommended Date
  private static final int FIELD_OUT_DOSE_DUE_05_DOSE_DUE_RECOMMENDED_DATE = 5;
  //  6 Dose Due Exceeds Date
  private static final int FIELD_OUT_DOSE_DUE_06_DOSE_DUE_EXCEEDS_DATE = 6;
  //  7 Reserved for future use
  private static final int FIELD_OUT_DOSE_DUE_07_RESERVED_FOR_FUTURE_USE = 7;
  //  8 Reserved for future use
  private static final int FIELD_OUT_DOSE_DUE_08_RESERVED_FOR_FUTURE_USE = 8;

  //  1 Dose Due Next IMM/Serve Series Code
  private static final int FIELD_OUT_DOSE_DUE_NEXT_1_DOSE_DUE_NEXT_IMM_SERVE_SERIES_CODE = 1;
  //  2 Reserved for future use
  private static final int FIELD_OUT_DOSE_DUE_NEXT_2_RESERVED_FOR_FUTURE_USE = 2;
  //  3 Dose due Next Dose Number
  private static final int FIELD_OUT_DOSE_DUE_NEXT_3_DOSE_DUE_NEXT_DOSE_NUMBER = 3;
  //  4 Dose Due Next Dependent Dose Index
  private static final int FIELD_OUT_DOSE_DUE_NEXT_4_DOSE_DUE_NEXT_DEPENDENT_DOSE_INDEX = 4;
  //  5 Dose due Next Acceptable Administration Date 
  private static final int FIELD_OUT_DOSE_DUE_NEXT_5_DOSE_DUE_NEXT_ACCEPTABLE_ADMINISTRATION_DATE_ = 5;
  //  6 Dose due Next Recommended Administration Date
  private static final int FIELD_OUT_DOSE_DUE_NEXT_6_DOSE_DUE_NEXT_RECOMMENDED_ADMINISTRATION_DATE = 6;
  //  7 Dose due Next Exceeds Date 
  private static final int FIELD_OUT_DOSE_DUE_NEXT_7_DOSE_DUE_NEXT_EXCEEDS_DATE = 7;
  //  8 Reserved for future use
  private static final int FIELD_OUT_DOSE_DUE_NEXT_8_RESERVED_FOR_FUTURE_USE = 8;

  //  1 Hib Series Completed Indicator
  private static final int FIELD_OUT_SERIES_01_HIB_SERIES_COMPLETED_INDICATOR = 1;
  //  2 Hepatitis A Series Completed Indicator
  private static final int FIELD_OUT_SERIES_02_HEPATITIS_A_SERIES_COMPLETED_INDICATOR = 2;
  //  3 Hepatitis B Series Completed Indicator
  private static final int FIELD_OUT_SERIES_03_HEPATITIS_B_SERIES_COMPLETED_INDICATOR = 3;
  //  4 Primary DTP Series Completed Indicator
  private static final int FIELD_OUT_SERIES_04_PRIMARY_DTP_SERIES_COMPLETED_INDICATOR = 4;
  //  5 Polio Vaccine Series Completed Indicator
  private static final int FIELD_OUT_SERIES_05_POLIO_VACCINE_SERIES_COMPLETED_INDICATOR = 5;
  //  6 MMR Series Completed Indicator
  private static final int FIELD_OUT_SERIES_06_MMR_SERIES_COMPLETED_INDICATOR = 6;
  //  7 Varicella Series Completed Indicator
  private static final int FIELD_OUT_SERIES_07_VARICELLA_SERIES_COMPLETED_INDICATOR = 7;
  //  8 Rv Series Completed Indicator
  private static final int FIELD_OUT_SERIES_08_RV_SERIES_COMPLETED_INDICATOR = 8;
  //  9 Strep-Pneumococcal Series Completed Indicator
  private static final int FIELD_OUT_SERIES_09_STREP_PNEUMOCOCCAL_SERIES_COMPLETED_INDICATOR = 9;
  //  10  Meningococcal Series Completed Indicator
  private static final int FIELD_OUT_SERIES_10_MENINGOCOCCAL_SERIES_COMPLETED_INDICATOR = 10;
  //  11  HPV Series Completed Indicator
  private static final int FIELD_OUT_SERIES_11_HPV_SERIES_COMPLETED_INDICATOR = 11;
  //  12  Reserved for future use
  private static final int FIELD_OUT_SERIES_12_ZOSTER_SERIES_COMPLETED_INDICATOR = 12;
  //  13  Reserved for future use
  private static final int FIELD_OUT_SERIES_13_RESERVED_FOR_FUTURE_USE = 13;

  private static final int FIELD_OUT_HUMAN_READABLE_LOG = 0;

  private int runCode = 0;
  private String runProblem = "";
  private Throwable exception = null;

  private String request = "";
  private StringBuilder response = new StringBuilder();
  private int currentPosition = 1;

  private static HashMap<String, String> doseDueOutHash = new HashMap<String, String>();
  static {
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"1"); // DTP
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"2"); // OPV
    doseDueOutHash.put(ImmunizationForecastDataBean.MMR, "3"); // MMR
    doseDueOutHash.put(ImmunizationForecastDataBean.MEASLES, "5"); // Measles
    doseDueOutHash.put(ImmunizationForecastDataBean.MUMPS, "07"); // Mumps
    doseDueOutHash.put(ImmunizationForecastDataBean.RUBELLA, "06"); // Rubella
    doseDueOutHash.put(ImmunizationForecastDataBean.HEPB, "8"); // Hep B adolescent or pediatric
    doseDueOutHash.put(ImmunizationForecastDataBean.TD, "9"); // Td adult
    doseDueOutHash.put(ImmunizationForecastDataBean.TDAP, "115"); // Tdap
    doseDueOutHash.put(ImmunizationForecastDataBean.HIB, "17"); // Hib  unspecified
    doseDueOutHash.put(ImmunizationForecastDataBean.DIPHTHERIA, "20"); // DTaP
    doseDueOutHash.put(ImmunizationForecastDataBean.DTAP, "20"); // DTaP
    doseDueOutHash.put(ImmunizationForecastDataBean.VARICELLA, "21"); // Varicella
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"28"); // DT pediatric
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"30"); // HBIG
    doseDueOutHash.put(ImmunizationForecastDataBean.HEPA, "85"); // Hep A  pediatric
    doseDueOutHash.put(ImmunizationForecastDataBean.PCV13, "133"); // Strep  Pneumococcal (polysacchoride)
    doseDueOutHash.put(ImmunizationForecastDataBean.PNEUMO, "133"); // Strep  Pneumococcal (polysacchoride)
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"49"); // Hib PRP-OMP
    doseDueOutHash.put(ImmunizationForecastDataBean.HPV, "137"); // HPV,  quadrivalent
    doseDueOutHash.put(ImmunizationForecastDataBean.POLIO, "89"); // Unspecified Polio
    doseDueOutHash.put(ImmunizationForecastDataBean.MCV4, "147"); // Meningococcal (MCV4)
    doseDueOutHash.put(ImmunizationForecastDataBean.MENING, "147"); // Meningococcal (MCV4)
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"116"); // Rotavirus, pentavalent
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"119"); // Rotavirus,monovalent
    doseDueOutHash.put(ImmunizationForecastDataBean.ROTAVIRUS, "122"); // Rotavirus, NOS
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"128"); // H1N1-09, NOS
    doseDueOutHash.put(ImmunizationForecastDataBean.PPSV, "33"); // Pneumococcal,  PCV13
    doseDueOutHash.put(ImmunizationForecastDataBean.INFLUENZA, "88"); // Influenza, seasonal, injectable
    doseDueOutHash.put(ImmunizationForecastDataBean.INFLUENZA_IIV, "141"); // Influenza, seasonal, injectable
    doseDueOutHash.put(ImmunizationForecastDataBean.INFLUENZA_LAIV, "151"); // Influenza, seasonal, injectable
    doseDueOutHash.put(ImmunizationForecastDataBean.ZOSTER, "121"); // Influenza, seasonal, injectable

    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"-10"); // Td Adult  Booster
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"-12"); // Unspecified D/T
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"-13"); // Tdap Adult Booster

  }

  private List<String> caseDetailFieldList;
  private List<List<String>> inputDoseFieldListList;

  public CaretForecaster(String request) throws Exception {
    this.request = request;

    String caseDetailRequest;
    int pos = request.indexOf(SECTION_SEPARATOR);
    if (pos == -1) {
      caseDetailRequest = request;
      request = "";
    } else {
      caseDetailRequest = request.substring(0, pos);
      request = chopRequest(request, pos);
    }

    caseDetailFieldList = new ArrayList<String>();
    parseSection(caseDetailFieldList, caseDetailRequest);

    if (caseDetailFieldList.size() < FIELD_IN_CASE_DETAIL_29_H1N1_CONTRAINDICATED_INDICATION) {
      runCode = -19;
      runProblem = "Input is too short expecting at least " + FIELD_IN_CASE_DETAIL_29_H1N1_CONTRAINDICATED_INDICATION
          + " fields";
    }

    inputDoseFieldListList = new ArrayList<List<String>>();

    pos = request.indexOf("|||");
    while (pos != -1) {
      String inputDoseRequest = request.substring(0, pos);
      request = chopRequest(request, pos);
      List<String> inputDoseFieldList = new ArrayList<String>();
      inputDoseFieldListList.add(inputDoseFieldList);
      parseSection(inputDoseFieldList, inputDoseRequest);
      pos = request.indexOf("|||");
    }
    if (request.length() > 0) {
      List<String> inputDoseFieldList = new ArrayList<String>();
      inputDoseFieldListList.add(inputDoseFieldList);
      parseSection(inputDoseFieldList, request);
    }

  }

  public String chopRequest(String request, int i) {
    i = i + 3;
    if (i < request.length()) {
      request = request.substring(i);
    } else {
      request = "";
    }
    return request;
  }

  public void parseSection(List<String> fieldList, String request) {
    StringBuilder sb = new StringBuilder();
    for (char c : request.toCharArray()) {
      if (c == '^') {
        fieldList.add(sb.toString());
        sb.setLength(0);
      } else {
        sb.append(c);
      }
    }
    fieldList.add(sb.toString());

    for (int i = 0; i < fieldList.size(); i++) {
      if (fieldList.get(i).equals("NULL")) {
        fieldList.set(i, "");
      }
    }

  }

  public String forecast(VaccineForecastManager vaccineForecastManager, Map<String, Integer> cvxToVaccineIdMap)
      throws Exception {

    String errorCode = "";
    String description = "";
    try {
      if (runCode < 0) {
        throw new Exception("Unable to process input because: " + runProblem);
      }

      Date forecastDate = readDate(caseDetailFieldList, FIELD_IN_CASE_DETAIL_01_DATE_USED_FOR_FORECAST);
      Date dateOfBirth = readDate(caseDetailFieldList, FIELD_IN_CASE_DETAIL_08_DATE_OF_BIRTH);
      String gender = readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_09_GENDER);
      boolean use4DayGracePeriod = readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_03_USE_4_DAY_GRACE_PERIOD)
          .equals("1");

      Set<String> filterSet = new HashSet<String>();
      if (readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_11_PERTUSSIS_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.PERTUSSIS);
      }
      if (readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_12_DIPHTHERIA_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.DIPHTHERIA);
      }
      if (readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_13_TETANUS_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.DIPHTHERIA);
      }
      if (readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_14_HIB_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.HIB);
      }

      if (readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_16_HEPB_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.HEPB);
      }

      if (readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_18_IPV_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.POLIO);
      }

      if (readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_19_MEASLES_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.MEASLES);
      }

      if (readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_20_MUMPS_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.MUMPS);
      }

      if (readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_21_RUBELLA_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.RUBELLA);
      }

      if (readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_22_VARICELLA_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.VARICELLA);
      }

      if (readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_23_HEPA_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.HEPA);
      }

      if (readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_24_RV_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.ROTAVIRUS);
      }

      if (readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_25_S_PN_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.PNEUMO);
        filterSet.add(ImmunizationForecastDataBean.PCV13);
        filterSet.add(ImmunizationForecastDataBean.PPSV);
      }

      if (readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_26_INFLUENZA_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.INFLUENZA);
      }

      if (readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_27_MENINGOCOCCAL_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.MENING);
      }

      if (readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_28_HPV_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.HPV);
      }

      if (readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_30_ZOSTER_CONTRAINDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.ZOSTER);
      }

      ForecastRunner forecastRunner = new ForecastRunner(vaccineForecastManager);
      forecastRunner.getForecastOptions().setFluSeasonDue(new TimePeriod("1 month"));
      forecastRunner.getForecastOptions().setFluSeasonEnd(new TimePeriod("6 months"));
      forecastRunner.getForecastOptions().setFluSeasonOverdue(new TimePeriod("4 months"));
      forecastRunner.getForecastOptions().setFluSeasonStart(new TimePeriod("0 months"));
      // forecastRunner.getForecastOptions().setFluSeasonFinished(new TimePeriod("9 months"));
      forecastRunner.getForecastOptions().setIgnoreFourDayGrace(!use4DayGracePeriod);
      forecastRunner.getForecastOptions().setUseEarlyOverdue(true);
      forecastRunner.getForecastOptions().setUseEarlyDue(true);
      forecastRunner.getForecastOptions().setDecisionProcessFormat(DecisionProcessFormat.FORMATTED_TEXT);
      forecastRunner.getPatient().setDob(new DateTime(dateOfBirth));
      forecastRunner.getPatient().setSex(gender.toUpperCase().startsWith("M") ? "M" : "F");
      forecastRunner.setForecastDate(forecastDate);
      List<ImmunizationInterface> imms = forecastRunner.getImms();

      int i = 0;
      for (List<String> inputDoseFieldList : inputDoseFieldListList) {
        i++;

        String doseNote = readField(inputDoseFieldList, FIELD_IN_INPUT_DOSE_1_DOSE_NOTE);
        String cvxCode = readField(inputDoseFieldList, FIELD_IN_INPUT_DOSE_2_DOSE_HL7_CODE);
        // TODO mapping code to cvx
        // for now, tack on 0 for single digit codes
        if (cvxCode.length() == 0) {
          continue;
        } else if (cvxCode.length() == 1) {
          cvxCode = "0" + cvxCode;
        }
        Integer vaccineId = null;

        if (cvxToVaccineIdMap.containsKey(cvxCode)) {
          vaccineId = cvxToVaccineIdMap.get(cvxCode);
        }

        if (readField(inputDoseFieldList, FIELD_IN_INPUT_DOSE_3_DATE_OF_DOSE_ADMINISTRATION).equals("")) {
          continue;
        }
        Date doseAdminDate = readDate(inputDoseFieldList, FIELD_IN_INPUT_DOSE_3_DATE_OF_DOSE_ADMINISTRATION);

        ImmunizationMDA imm = new ImmunizationMDA();
        String doseOveride = readField(inputDoseFieldList, FIELD_IN_INPUT_DOSE_4_DOSE_OVERRIDE);
        imm.setDoseOverride(doseOveride);
        if (doseOveride.equals(DOSE_OVERRIDE_FORCE_INVALID)) {
          imm.setSubPotent(true);
        } else if (doseOveride.equals(DOSE_OVERRIDE_FORCE_VALID)) {
          imm.setForceValid(true);
        }

        imm.setVaccinationId("" + i);
        imm.setCvx(cvxCode);
        imm.setDateOfShot(doseAdminDate);
        imm.setDoseNote(doseNote);
        if (vaccineId == null) {
          imm.setHl7CodeErrorCode(HL7_CODE_ERROR_CODE_UNRECOGNIZED);
          imm.setVaccineId(0);
        } else {
          imm.setHl7CodeErrorCode(HL7_CODE_ERROR_CODE_NONE);
          imm.setVaccineId(vaccineId);
        }
        imms.add(imm);
      }

      {
        setAssumeParam(forecastRunner, "Adult assumed to have completed DTaP series.",
            Immunization.ASSUME_DTAP_SERIES_COMPLETE);
        setAssumeParam(forecastRunner, "Adult assumed to have completed MMR series.", Immunization.ASSUME_MMR_COMPLETE);
        setAssumeParam(forecastRunner, "Adult assumed to have completed Varicella series.",
            Immunization.ASSUME_VAR_COMPLETE);
        forecastRunner.getForecastOptions().setAssumeCompleteScheduleName("HepA");
        forecastRunner.getForecastOptions().setAssumeCompleteScheduleName("HepB");
      }

      String forecastingMode = readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_02_FORECASTING_MODE);
      if (forecastingMode.equals(FORECASTING_MODE_ACCEPTABLE)) {
        forecastRunner.getForecastOptions().setRecommendWhenValid(true);
      } else {
        forecastingMode = FORECASTING_MODE_RECOMMENDED;
        forecastRunner.getForecastOptions().setRecommendWhenValid(false);
      }

      // Run Forecast
      forecastRunner.forecast();

      DateTime today = new DateTime();

      // Put together response
      addValue("TCH Forecaster version " + SoftwareVersion.VERSION, FIELD_OUT_CASE_DATA_01_COPYRIGHT_NOTICE);
      addValue(today.toString("YMDHTS"), FIELD_OUT_CASE_DATA_02_RUN_DATE_AND_TIME);
      addValue((new DateTime(forecastDate)).toString("YMD"), FIELD_OUT_CASE_DATA_03_DATE_USED_FOR_FORECAST);
      addValue(forecastingMode, FIELD_OUT_CASE_DATA_04_FORECASTING_MODE);
      addValue("", FIELD_OUT_CASE_DATA_05_VERSION);

      // addValue(SoftwareVersion.VERSION, FIELD_OUT_06_RULE_SET_MAJOR_VERSION);
      // addValue(forecastRunner.getForecasterScheduleName(), FIELD_OUT_07_RULE_SET_MINOR_VERSION);
      // addValue(SoftwareVersion.VERSION_RELEASE, FIELD_OUT_08_RULE_SET_RELEASE_DATE);
      addValue(readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_06_PERSONAL_ID), FIELD_OUT_CASE_DATA_09_PERSONAL_ID);
      addValue(readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_07_USER_NOTE), FIELD_OUT_CASE_DATA_10_USER_NOTE);
      addValue(readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_08_DATE_OF_BIRTH),
          FIELD_OUT_CASE_DATA_11_DATE_OF_BIRTH);
      addValue(readField(caseDetailFieldList, FIELD_IN_CASE_DETAIL_09_GENDER), FIELD_OUT_CASE_DATA_12_GENDER);

      // #2 History Segment
      response.append(SECTION_SEPARATOR);
      {
        boolean first = true;
        i = 0;
        for (ImmunizationInterface immInterface : imms) {
          if (immInterface instanceof ImmunizationMDA) {
            ImmunizationMDA imm = (ImmunizationMDA) immInterface;
            if (!first) {
              response.append(DOSE_SEPARATOR);
            }
            first = false;
            currentPosition = 1;
            addValue(imm.getDoseNote(), FIELD_OUT_INPUT_DOSE_01_DOSE_NOTE);
            addValue(imm.getCvx(), FIELD_OUT_INPUT_DOSE_02_DOSE_INPUT_HL7_CODE);
            addValue(imm.getHl7CodeErrorCode(), FIELD_OUT_INPUT_DOSE_03_DOSE_INPUT_HL7_CODE_ERROR_CODE);
            addValue(new DateTime(imm.getDateOfShot()).toString("YMD"),
                FIELD_OUT_INPUT_DOSE_04_DATE_OF_DOSE_ADMINISTRATION);
            addValue(imm.getDoseOverride(), FIELD_OUT_INPUT_DOSE_05_DOSE_OVERRIDE);

            String invalidReason = "";
            for (VaccinationDoseDataBean dose : forecastRunner.getDoseList()) {
              if (dose.getVaccinationId() == imm.getVaccinationId()) {
                if (dose.getStatusCode().equals(VaccinationDoseDataBean.STATUS_INVALID)) {
                  // adding special rule to skip marking a Hep B as invalid for third dose that is
                  // pediatric
                  if (dose.getForecastCode().equals(ImmunizationForecastDataBean.HEPB)
                      && dose.getDoseCode().equals("3") && imm.getCvx().equals("110")) {
                    continue;
                  }

                  invalidReason = dose.getReason();
                  if (invalidReason == null) {
                    invalidReason = "";
                  } else {
                    invalidReason = invalidReason.trim();
                  }
                  if (invalidReason.length() > 70) {
                    invalidReason = invalidReason.substring(0, 70);
                  }
                  invalidReason = "1:" + invalidReason;
                  break;
                }
              }
            }
            addValue(invalidReason, FIELD_OUT_INPUT_DOSE_06_INVLIAD_DOSE_AND_REASON);
            i++;
          }
        }
        response.append(SECTION_SEPARATOR);
      }

      boolean inInfluenzaSuppressRange = false;
      // special suppress flu forecast for first dose between 04/01 and 07/01 of this year
      DateTime startSuppressDate = new DateTime(forecastDate);
      startSuppressDate.setMonth(4);
      startSuppressDate.setDay(1);
      DateTime endSuppressDate = new DateTime(forecastDate);
      endSuppressDate.setMonth(8);
      endSuppressDate.setDay(1);
      DateTime suppressDate = new DateTime(forecastDate);
      if (suppressDate.isGreaterThanOrEquals(startSuppressDate) && suppressDate.isLessThan(endSuppressDate)) {
        inInfluenzaSuppressRange = true;
      }

      // #3 Doses Due Segment
      Set<String> nc = new HashSet<String>();
      {

        List<ImmunizationForecastDataBean> forecastListDueToday = new ArrayList<ImmunizationForecastDataBean>();
        List<ImmunizationForecastDataBean> forecastListDueTodayAdd = new ArrayList<ImmunizationForecastDataBean>(
            forecastRunner.getForecastListDueToday());
        for (Iterator<ImmunizationForecastDataBean> it = forecastListDueTodayAdd.iterator(); it.hasNext();) {
          ImmunizationForecastDataBean forecastResult = it.next();
          nc.add(forecastResult.getForecastName());
          if (!doseDueOutHash.containsKey(forecastResult.getForecastName())) {
            it.remove();
          } else {
            filter(filterSet, forecastListDueTodayAdd, it, forecastResult);
          }
        }
        forecastListDueToday.addAll(forecastListDueTodayAdd);
        {
          boolean first = true;
          for (ImmunizationForecastDataBean forecastResult : forecastListDueToday) {
            if (inInfluenzaSuppressRange
                && (forecastResult.getForecastName().equals(ImmunizationForecastDataBean.INFLUENZA) || forecastResult
                    .getForecastName().equals(ImmunizationForecastDataBean.INFLUENZA_IIV))
                && forecastResult.getDose().equals("1")) {
              continue;
            }
            if (!first) {
              response.append(DOSE_SEPARATOR);
            }
            first = false;
            currentPosition = 1;
            String doseDueCode = doseDueOutHash.get(forecastResult.getForecastName());
            //        String doseHL7Code = doseDueOutHash.get(forecastResult.getForecastName());
            //        if (doseHL7Code == null) {
            //          doseHL7Code = "";
            //        }
            addValue(doseDueCode, FIELD_OUT_DOSE_DUE_1_DOSE_DUE_IMM_SERVE_SERIES_CODE);
            // addValue(doseHL7Code, FIELD_OUT_140_DOSE_DUE_DOSE_HL7_CODE + base);
            // addValue(forecastResult.getForecastLabel(), FIELD_OUT_141_DOSE_DUE_HL7_CODE_PRINT_STRING + base);
            addValue(forecastResult.getDose(), FIELD_OUT_DOSE_DUE_02_DOSE_DUE_DOSE_NUMBER);
            boolean overdue = forecastResult.getStatusDescription().equals("overdue");
            addValue(overdue ? "1" : "0", FIELD_OUT_DOSE_DUE_03_DOSE_DUE_PAST_DUE_INDICATOR);
            addValue(d(forecastResult.getValid()), FIELD_OUT_DOSE_DUE_04_DOSE_DUE_MINIMUM_DATE);
            if (forecastingMode.equals(FORECASTING_MODE_ACCEPTABLE)) {
              addValue(d(forecastResult.getValid()), FIELD_OUT_DOSE_DUE_05_DOSE_DUE_RECOMMENDED_DATE);
            } else {
              addValue(d(forecastResult.getDue()), FIELD_OUT_DOSE_DUE_05_DOSE_DUE_RECOMMENDED_DATE);
            }
            addValue(d(forecastResult.getOverdue()), FIELD_OUT_DOSE_DUE_06_DOSE_DUE_EXCEEDS_DATE);
            //        addValue(d(forecastResult.getOverdue()), FIELD_OUT_147_DOSE_DUE_MINIMUM_REMINDER_DATE + base);
            //        addValue(d(forecastResult.getOverdue()), FIELD_OUT_148_DOSE_DUE_RECOMMENDED_REMINDER_DATE + base);
            //        addValue(overdue ? "1" : "0", FIELD_OUT_149_DOSE_DUE_EXCEEDS_REMINDER_DATE + base);
            //        addValue("", FIELD_OUT_150_DOSE_DUE_VFC_PAYMENT_INDICATOR + base);
            addValue("", FIELD_OUT_DOSE_DUE_08_RESERVED_FOR_FUTURE_USE);

          }
          response.append(SECTION_SEPARATOR);
        }
        List<ImmunizationForecastDataBean> forecastListDueLater = new ArrayList<ImmunizationForecastDataBean>(
            forecastRunner.getForecastListDueLater());
        List<ImmunizationForecastDataBean> forecastListDueLaterAdd = new ArrayList<ImmunizationForecastDataBean>();
        for (Iterator<ImmunizationForecastDataBean> it = forecastListDueLater.iterator(); it.hasNext();) {
          ImmunizationForecastDataBean forecastResult = it.next();
          nc.add(forecastResult.getForecastName());
          if (!doseDueOutHash.containsKey(forecastResult.getForecastName())) {
            it.remove();
          }
          filter(filterSet, forecastListDueLaterAdd, it, forecastResult);
        }
        forecastListDueLater.addAll(forecastListDueLaterAdd);
        {
          boolean first = true;
          for (ImmunizationForecastDataBean forecastResult : forecastListDueLater) {
            if (!first) {
              response.append(DOSE_SEPARATOR);
            }
            currentPosition = 1;
            String doseDueCode = doseDueOutHash.get(forecastResult.getForecastName());
            //        String doseHL7Code = doseDueOutHash.get(forecastResult.getForecastName());
            //        if (doseHL7Code == null) {
            //          doseHL7Code = "";
            //        }
            addValue(doseDueCode, FIELD_OUT_DOSE_DUE_NEXT_1_DOSE_DUE_NEXT_IMM_SERVE_SERIES_CODE);
            //        addValue(doseHL7Code, FIELD_OUT_153_DOSE_DUE_NEXT_DOSE_HL7_CODE + base);
            //        addValue(forecastResult.getForecastLabel(), FIELD_OUT_154_DOSE_DUE_NEXT_HL7_CODE_PRINT_STRING + base);
            addValue(forecastResult.getDose(), FIELD_OUT_DOSE_DUE_NEXT_3_DOSE_DUE_NEXT_DOSE_NUMBER);
            addValue("", FIELD_OUT_DOSE_DUE_NEXT_4_DOSE_DUE_NEXT_DEPENDENT_DOSE_INDEX);
            addValue(d(forecastResult.getValid()),
                FIELD_OUT_DOSE_DUE_NEXT_5_DOSE_DUE_NEXT_ACCEPTABLE_ADMINISTRATION_DATE_);
            addValue(d(forecastResult.getDue()),
                FIELD_OUT_DOSE_DUE_NEXT_6_DOSE_DUE_NEXT_RECOMMENDED_ADMINISTRATION_DATE);
            addValue(d(forecastResult.getOverdue()), FIELD_OUT_DOSE_DUE_NEXT_7_DOSE_DUE_NEXT_EXCEEDS_DATE);
            //        addValue("", FIELD_OUT_160_DOSE_DUE_NEXT_VFC_PAYMENT_INDICATOR + base);
            addValue("", FIELD_OUT_DOSE_DUE_NEXT_8_RESERVED_FOR_FUTURE_USE);
            response.append(DOSE_SEPARATOR);
          }
          response.append(SECTION_SEPARATOR);
        }
      }
      currentPosition = 1;
      addValue(c(nc, ImmunizationForecastDataBean.HIB), FIELD_OUT_SERIES_01_HIB_SERIES_COMPLETED_INDICATOR);
      addValue(c(nc, ImmunizationForecastDataBean.HEPA), FIELD_OUT_SERIES_02_HEPATITIS_A_SERIES_COMPLETED_INDICATOR);
      addValue(c(nc, ImmunizationForecastDataBean.HEPB), FIELD_OUT_SERIES_03_HEPATITIS_B_SERIES_COMPLETED_INDICATOR);
      addValue(c(nc, ImmunizationForecastDataBean.DTAP), FIELD_OUT_SERIES_04_PRIMARY_DTP_SERIES_COMPLETED_INDICATOR);
      addValue(c(nc, ImmunizationForecastDataBean.POLIO), FIELD_OUT_SERIES_05_POLIO_VACCINE_SERIES_COMPLETED_INDICATOR);
      addValue(c(nc, ImmunizationForecastDataBean.MMR), FIELD_OUT_SERIES_06_MMR_SERIES_COMPLETED_INDICATOR);
      addValue(c(nc, ImmunizationForecastDataBean.VARICELLA), FIELD_OUT_SERIES_07_VARICELLA_SERIES_COMPLETED_INDICATOR);
      addValue(c(nc, ImmunizationForecastDataBean.ROTAVIRUS), FIELD_OUT_SERIES_08_RV_SERIES_COMPLETED_INDICATOR);
      addValue(c(nc, ImmunizationForecastDataBean.PNEUMO),
          FIELD_OUT_SERIES_09_STREP_PNEUMOCOCCAL_SERIES_COMPLETED_INDICATOR);
      addValue(c(nc, ImmunizationForecastDataBean.MENING), FIELD_OUT_SERIES_10_MENINGOCOCCAL_SERIES_COMPLETED_INDICATOR);
      addValue(c(nc, ImmunizationForecastDataBean.HPV), FIELD_OUT_SERIES_11_HPV_SERIES_COMPLETED_INDICATOR);
      addValue(c(nc, ImmunizationForecastDataBean.ZOSTER), FIELD_OUT_SERIES_12_ZOSTER_SERIES_COMPLETED_INDICATOR);
      addValue("", FIELD_OUT_SERIES_13_RESERVED_FOR_FUTURE_USE);

      StringWriter stringWriter = new StringWriter();
      PrintWriter out = new RpmsWriter(stringWriter);
      forecastRunner.printTextReport(out);
      description = stringWriter.toString();

    } catch (Throwable t) {
      t.printStackTrace();
      errorCode = "Unable to Forecast, unexpected exeption occurred: " + t.getMessage();
    }
    return errorCode + "&&&" + response.toString() + "&&&" + description;
  }

  public void setAssumeParam(ForecastRunner forecastRunner, String label, int vaccineId) {
    TimePeriod assumeSeriesCompleteAtAge = new TimePeriod("18 years");

    if (assumeSeriesCompleteAtAge != null) {
      DateTime assumptionAge = assumeSeriesCompleteAtAge.getDateTimeFrom(forecastRunner.getPatient().getDob());
      if (forecastRunner.getForecastDate().after(assumptionAge.getDate())) {
        DateTime assumptionDate = new DateTime(assumptionAge);
        Immunization imm = new Immunization();
        imm.setDateOfShot(assumptionDate.getDate());
        imm.setVaccineId(vaccineId);
        imm.setLabel(label);
        imm.setAssumption(true);
        forecastRunner.getImms().add(imm);
      }
    }
  }

  public boolean filter(Set<String> filterSet, List<ImmunizationForecastDataBean> forecastListDueTodayAdd,
      Iterator<ImmunizationForecastDataBean> it, ImmunizationForecastDataBean forecastResult) {
    if (filterSet.contains(forecastResult.getForecastName())) {
      it.remove();
      return true;
    } else if (forecastResult.getForecastName().equals(ImmunizationForecastDataBean.MMR)
        && (filterSet.contains(ImmunizationForecastDataBean.MEASLES)
            || filterSet.contains(ImmunizationForecastDataBean.MUMPS) || filterSet
              .contains(ImmunizationForecastDataBean.RUBELLA))) {
      it.remove();
      if (!filterSet.contains(ImmunizationForecastDataBean.MEASLES)) {
        ImmunizationForecastDataBean forecastResultAdd = createForecastCopy(forecastResult);
        forecastResultAdd.setForecastName(ImmunizationForecastDataBean.MEASLES);
        forecastResultAdd.setForecastLabel(ImmunizationForecastDataBean.MEASLES);
        forecastListDueTodayAdd.add(forecastResultAdd);
      }
      if (!filterSet.contains(ImmunizationForecastDataBean.MUMPS)) {
        ImmunizationForecastDataBean forecastResultAdd = createForecastCopy(forecastResult);
        forecastResultAdd.setForecastName(ImmunizationForecastDataBean.MUMPS);
        forecastResultAdd.setForecastLabel(ImmunizationForecastDataBean.MUMPS);
        forecastListDueTodayAdd.add(forecastResultAdd);
      }
      if (!filterSet.contains(ImmunizationForecastDataBean.RUBELLA)) {
        ImmunizationForecastDataBean forecastResultAdd = createForecastCopy(forecastResult);
        forecastResultAdd.setForecastName(ImmunizationForecastDataBean.RUBELLA);
        forecastResultAdd.setForecastLabel(ImmunizationForecastDataBean.RUBELLA);
        forecastListDueTodayAdd.add(forecastResultAdd);
      }
      return true;
    } else if (forecastResult.getForecastName().equals(ImmunizationForecastDataBean.DIPHTHERIA)
        || forecastResult.getForecastName().equals(ImmunizationForecastDataBean.DTAP)
        || forecastResult.getForecastName().equals(ImmunizationForecastDataBean.TD)
        || forecastResult.getForecastName().equals(ImmunizationForecastDataBean.TDAP)) {
      if (filterSet.contains(ImmunizationForecastDataBean.DIPHTHERIA)) {
        // if Diphtheria is contraindicated no combination is valid
        it.remove();
        return true;
      } else if (filterSet.contains(ImmunizationForecastDataBean.PERTUSSIS)) {
        // if Pertussis is contraindicated then only DT or TD can be given
        if (forecastResult.getForecastName().equals(ImmunizationForecastDataBean.DTAP)) {
          forecastResult.setForecastName(ImmunizationForecastDataBean.DT);
          forecastResult.setForecastLabel(ImmunizationForecastDataBean.DT);
        } else if (forecastResult.getForecastName().equals(ImmunizationForecastDataBean.TDAP)) {
          forecastResult.setForecastName(ImmunizationForecastDataBean.TD);
          forecastResult.setForecastLabel(ImmunizationForecastDataBean.TD);
        } else {
          // good to go
        }
      }
    }
    return false;
  }

  public ImmunizationForecastDataBean createForecastCopy(ImmunizationForecastDataBean forecastResult) {
    ImmunizationForecastDataBean forecastResultAdd = new ImmunizationForecastDataBean();
    forecastResultAdd.setComment(forecastResult.getComment());
    forecastResultAdd.setDateDue(forecastResult.getDateDue());
    forecastResultAdd.setDose(forecastResult.getDose());
    forecastResultAdd.setFinished(forecastResult.getFinished());
    forecastResultAdd.setForecastLabel(forecastResult.getForecastLabel());
    forecastResultAdd.setForecastName(forecastResult.getForecastName());
    forecastResultAdd.setForecastNameOriginal(forecastResult.getForecastNameOriginal());
    forecastResultAdd.setImmregid(forecastResult.getImmregid());
    forecastResultAdd.setOverdue(forecastResult.getOverdue());
    forecastResultAdd.setSchedule(forecastResult.getSchedule());
    forecastResultAdd.setSeasonEnd(forecastResult.getSeasonEnd());
    forecastResultAdd.setSeasonStart(forecastResult.getSeasonStart());
    forecastResultAdd.setSortOrder(forecastResult.getSortOrder());
    forecastResultAdd.setStatusDescription(forecastResult.getStatusDescription());
    forecastResultAdd.setTraceList(forecastResult.getTraceList());
    forecastResultAdd.setValid(forecastResult.getValid());
    return forecastResultAdd;
  }

  public String c(Set<String> notCompleted, String forecastName) {
    return notCompleted.contains(forecastName) ? "0" : "1";
  }

  private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

  private int readInt(List<String> fieldList, int position) throws Exception {
    String value = readField(fieldList, position);
    if (value.equals("")) {
      return 0;
    }
    return Integer.parseInt(value);
  }

  private String d(Date d) {
    return new DateTime(d).toString("YMD");
  }

  private Date readDate(List<String> fieldList, int position) throws Exception {
    String value = readField(fieldList, position);
    if (value.equals("")) {
      value = "07041776";
    }
    return sdf.parse(value);
  }

  private String readField(List<String> fieldList, int position) {
    position--;
    if (position < fieldList.size()) {
      String value = fieldList.get(position);
      if (value.equalsIgnoreCase("NULL")) {
        return "";
      }
      return value.trim();
    }
    return "";
  }

  private void addValue(String value, int position) {
    if (position < currentPosition) {
      throw new IllegalArgumentException("Unable to add value to output in position " + position
          + ", already at position " + currentPosition);
    }
    while (currentPosition < position) {
      response.append("^");
      currentPosition++;
    }
    response.append(value);
    response.append("^");
    currentPosition++;
  }

  // java -classpath deploy/tch-forecaster.jar org.tch.forecast.core.server.CaretForecaster

  // java -classpath deploy/tch-forecaster.jar org.tch.forecast.core.server.CaretForecaster "20131118^R^IHS_6m26^0^0^FURRAST,JOHN DELBERT  Chart#: 00-00-55^55^19571122^Male^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^~~~2272^20^20080118^0^0^0|||2273^20^20080122^0^0^0|||2271^21^20080118^0^0^0|||2663^111^20081212^0^0^0|||
  // java -classpath deploy/tch-forecaster.jar org.tch.forecast.core.server.CaretForecaster "20140201^R^IHS_6m26^0^0^^55^19481128^Male^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^~~~55079^9^19990706^0^0^0|||180404^115^20110504^0^0^0|||55078^45^19990706^0^0^0|||183899^33^20060101^0^0^0"
  // java -classpath deploy/tch-forecaster.jar org.tch.forecast.core.server.CaretForecaster "20140306^0^1^0^0^BERLASA,ERIN GEORGE  Chart#: 00-00-25^25^19881225^Female^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^~~~3367^21^20120415^0^0^0|||3366^141^20131001^0^0^0|||"
  // java -classpath deploy/tch-forecaster.jar org.tch.forecast.core.server.CaretForecaster "20140310^0^1^0^0^UVARECKAR,ROSE ANNA  Chart#: 00-01-04^104^20131104^Female^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^~~~"
  // java -classpath deploy/tch-forecaster.jar org.tch.forecast.core.server.CaretForecaster "20140328^0^1^0^0^UVARECKAR,TEST^104^20100307^Male^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^~~~^20^20100601^0^0^|||^110^20100801^0^0^|||^20^20110901^0^0^|||^20^20120101^0^0^"
  // java -classpath deploy/tch-forecaster.jar org.tch.forecast.core.server.CaretForecaster "20140328^0^1^0^0^UVARECKAR,TEST 2^104^20000307^Male^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^~~~^20^20000601^0^0^|||^110^20000801^0^0^|||^20^20010901^0^0^|||^20^20120001^0^0^"
  // java -classpath deploy/tch-forecaster.jar org.tch.forecast.core.server.CaretForecaster "20140328^0^0^0^0^HUEMS,SHEILA LYNN  Chart#: 174226^27654^19620922^Female^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^"
  // java -classpath deploy/tch-forecaster.jar org.tch.forecast.core.server.CaretForecaster "20140328^0^0^0^0^HUEMS,TEST 2  Chart#: 174226^27654^20020701^Male^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^~~~^138^20140320^0^0^"
  // java -classpath deploy/tch-forecaster.jar org.tch.forecast.core.server.CaretForecaster "20140401^0^1^0^0^ORR,ALBERT JOSEPH  Chart#: 105237^1745^19490331^Male^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^1^0^0^0^0^0^~~~43177^9^19970424^0^0^0|||137440^115^20080820^0^0^0|||183911^121^19980331^1^0^0|||57611^88^19991116^0^0^0|||183909^33^20070410^0^0^0"
  // java -classpath deploy/tch-forecaster.jar org.tch.forecast.core.server.CaretForecaster "20140401^0^1^0^0^ORR,ALBERT JOSEPH  Chart#: 105237^1745^19490331^Male^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^~~~43177^9^19970424^0^0^0|||137440^115^20080820^0^0^0|||183911^121^19980331^5^0^0|||57611^88^19991116^0^0^0|||183909^33^20070410^0^0^0"
  // java -classpath deploy/tch-forecaster.jar org.tch.forecast.core.server.CaretForecaster "20140402^0^1^0^0^DEMO, BABYMALE  Chart#: 105237^1745^20140219^Male^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^1^0^0^0^0^0^"
  // java -classpath deploy/tch-forecaster.jar org.tch.forecast.core.server.CaretForecaster "20140402^1^1^0^0^DEMO, BABYMALE  Chart#: 105237^1745^20140219^Male^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^1^0^0^0^0^0^"
  // java -classpath deploy/tch-forecaster.jar org.tch.forecast.core.server.CaretForecaster "20100527^1^1^0^0^DEMO, PATIENT BILL^1745^20100527^Male^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^1^0^0^0^0^0^~~~^8^20100527^0^0^|||^110^20100714^0^0^|||^110^20100914^0^0^"
  // java -classpath deploy/tch-forecaster.jar org.tch.forecast.core.server.CaretForecaster "20140421^0^0^0^0^CREYG,ALLISON RAE  Chart#: 00-00-30^30^19840216^Female^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^~~~3477^21^20140328^0^0^0|||3478^149^20140404^0^0^0|||"

  public static void main(String[] args) throws Exception {
    String request = ForecastServer.TEST[0];
    if (args.length > 0) {
      request = args[0];
    }
    CaretForecaster cf = new CaretForecaster(request);
    VaccineForecastManager vaccineForecastManager = new VaccineForecastManager();
    Map<String, CvxCode> cvxToCvxCodeMap = CvxCodes.getCvxToCvxCodeMap();
    Map<String, Integer> cvxToVaccineIdMap = new HashMap<String, Integer>();
    for (CvxCode cvxCode : cvxToCvxCodeMap.values()) {
      cvxToVaccineIdMap.put(cvxCode.getCvxCode(), cvxCode.getVaccineId());
    }
    String response = cf.forecast(vaccineForecastManager, cvxToVaccineIdMap);
    System.out.println();
    System.out.println(response);
    System.out.println();
    int pos;
    while ((pos = response.indexOf("|||")) != -1) {
      System.out.println(response.substring(0, pos));
      response = response.substring(pos + 3);
    }
    System.out.println(response);

  }

  private static class RpmsWriter extends PrintWriter
  {

    public RpmsWriter(Writer out) {
      super(out);
    }

    @Override
    public void println() {
      super.print("|||");
    }

    @Override
    public void println(String x) {
      super.print(x);
      println();
    }

  }
}
