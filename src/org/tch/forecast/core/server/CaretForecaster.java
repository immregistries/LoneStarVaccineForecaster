package org.tch.forecast.core.server;

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
import org.tch.forecast.core.ImmunizationForecastDataBean;
import org.tch.forecast.core.ImmunizationInterface;
import org.tch.forecast.core.SoftwareVersion;
import org.tch.forecast.core.api.impl.CvxCodes;
import org.tch.forecast.core.api.impl.VaccineForecastManager;
import org.tch.forecast.core.model.ImmunizationMDA;

public class CaretForecaster
{

  private static final String DOSE_OVERRIDE_EXCLUDED = "2";
  private static final String DOSE_OVERRIDE_INCLUDED = "1";
  private static final String DOSE_OVERRIDE_DEFAULT = "0";

  private static final String FORECASTING_MODE_ACCEPTABLE = "A";
  private static final String FORECASTING_MODE_RECOMMENDED = "R";

  private static final String HL7_CODE_ERROR_CODE_NONE = "0";
  private static final String HL7_CODE_ERROR_CODE_UNRECOGNIZED = "1";
  private static final String HL7_CODE_ERROR_CODE_UNSUPPORTED = "2";

  private static final int FIELD_IN_01_DATE_USED_FOR_FORECAST = 1;
  private static final int FIELD_IN_02_FORECASTING_MODE = 2;
  private static final int FIELD_IN_03_VERSION = 3;
  private static final int FIELD_IN_04_PERSONAL_ID = 4;
  private static final int FIELD_IN_05_USER_NOTE = 5;
  private static final int FIELD_IN_06_DATE_OF_BIRTH = 6;
  private static final int FIELD_IN_07_GENDER = 7;

  private static final int FIELD_IN_08_MOTHER_HBSAG_STATUS = 8; // NOT USED YET
  private static final int FIELD_IN_09_PERTUSSIS_CONTRAINDICATED_INDICATION = 9;
  private static final int FIELD_IN_10_DIPHTHERIA_CONTRAINDICATED_INDICATION = 10;
  private static final int FIELD_IN_11_TETANUS_CONTRAINDICATED_INDICATION = 11;
  private static final int FIELD_IN_12_HIB_CONTRAINDICATED_INDICATION = 12;
  private static final int FIELD_IN_13_HBIG_CONTRAINDICATED_INDICATION = 13; // NOT USED YET
  private static final int FIELD_IN_14_HEPB_CONTRAINDICATED_INDICATION = 14;
  private static final int FIELD_IN_15_OPV_CONTRAINDICATED_INDICATION = 15; // NOT USED YET
  private static final int FIELD_IN_16_IPV_CONTRAINDICATED_INDICATION = 16;
  private static final int FIELD_IN_17_MEASLES_CONTRAINDICATED_INDICATION = 17;
  private static final int FIELD_IN_18_MUMPS_CONTRAINDICATED_INDICATION = 18;
  private static final int FIELD_IN_19_RUBELLA_CONTRAINDICATED_INDICATION = 19;
  private static final int FIELD_IN_20_VARICELLA_CONTRAINDICATED_INDICATION = 20;
  private static final int FIELD_IN_21_HEPA_CONTRAINDICATED_INDICATION = 21;
  private static final int FIELD_IN_22_RV_CONTRAINDICATED_INDICATION = 22;
  private static final int FIELD_IN_23_S_PN_CONTRAINDICATED_INDICATION = 23;
  private static final int FIELD_IN_24_INFLUENZA_CONTRAINDICATED_INDICATION = 24;
  private static final int FIELD_IN_25_MENINGOCOCCAL_CONTRAINDICATED_INDICATION = 25;
  private static final int FIELD_IN_26_HPV_CONTRAINDICATED_INDICATION = 26;
  private static final int FIELD_IN_27_H1N1_CONTRAINDICATED_INDICATION = 27; // NOT USED YET

  private static final int FIELD_IN_28_NUMBER_OF_INPUT_DOSES = 28;

  private static final int FIELD_IN_29_DOSE_NOTE = 1;
  private static final int FIELD_IN_30_DOSE_HL7_CODE = 2;
  private static final int FIELD_IN_31_DATE_OF_DOSE_ADMINISTRATION = 3;
  private static final int FIELD_IN_32_DOSE_OVERRIDE = 4;
  private static final int FIELD_IN_33_RESERVED_FOR_FUTURE_USE = 5;
  private static final int FIELD_IN_34_RESERVED_FOR_FUTURE_USE = 6;

  // ++ Out fields +++++++++++++++++++++++++++++++++++++

  private static final int FIELD_OUT_00_ERROR = 0;
  private static final int FIELD_OUT_01_COPYRIGHT_NOTICE = 1;
  private static final int FIELD_OUT_02_RUN_DATE_AND_TIME = 2;
  private static final int FIELD_OUT_03_DATE_USED_FOR_FORECAST = 3;
  private static final int FIELD_OUT_04_FORECASTING_MODE = 4;
  private static final int FIELD_OUT_05_VERSION = 5;
  private static final int FIELD_OUT_06_RULE_SET_MAJOR_VERSION = 6;
  private static final int FIELD_OUT_07_RULE_SET_MINOR_VERSION = 7;
  private static final int FIELD_OUT_08_RULE_SET_RELEASE_DATE = 8;
  private static final int FIELD_OUT_09_PERSONAL_ID = 9;
  private static final int FIELD_OUT_10_USER_NOTE = 10;
  private static final int FIELD_OUT_11_DATE_OF_BIRTH = 11;
  private static final int FIELD_OUT_68_GENDER = 12;
  private static final int FIELD_OUT_13_NUMBER_OF_INPUT_DOSES = 13;

  private static final int FIELD_OUT_DOSE_BASE = 13;
  private static final int FIELD_OUT_DOSE_BASE_ADD = 13;
  private static final int FIELD_OUT_14_DOSE_NOTE = 1;
  private static final int FIELD_OUT_15_DOSE_INPUT_HL7_CODE = 2;
  private static final int FIELD_OUT_16_DOSE_INPUT_HL7_CODE_ERROR_CODE = 3;
  private static final int FIELD_OUT_17_DOSE_INPUT_PRINT_STRING = 4;
  private static final int FIELD_OUT_18_DOSE_HL7_CODE_ = 5;
  private static final int FIELD_OUT_19_DOSE_HL7_CODE_PRINT_STRING = 6;
  private static final int FIELD_OUT_20_DATE_OF_DOSE_ADMINISTRATION = 11;
  private static final int FIELD_OUT_21_DOSE_OVERRIDE = 13;

  private static final int FIELD_OUT_22_NUMBER_OF_DOSES_DUE_ON_THE_DATE_USED_FOR_FORECAST = 14;
  private static final int FIELD_OUT_DOSE_DUE_BASE_ADD = 6;
  private static final int FIELD_OUT_23_DOSE_DUE_IMM_SERVE_SERIES_CODE = 1;
  private static final int FIELD_OUT_24_DOSE_DUE_DOSE_NUMBER = 2;
  private static final int FIELD_OUT_25_DOSE_DUE_PAST_DUE_INDICATOR = 3;
  private static final int FIELD_OUT_26_DOSE_DUE_MINIMUM_DATE = 4;
  private static final int FIELD_OUT_27_DOSE_DUE_RECOMMENDED_DATE = 5;
  private static final int FIELD_OUT_28_DOSE_DUE_EXCEEDS_DATE = 6;

  private static final int FIELD_OUT_29_NUMBER_OF_DOSES_DUE_NEXT = 29;
  private static final int FIELD_OUT_DOSE_DUE_NEXT_BASE_ADD = 6;
  private static final int FIELD_OUT_30_DOSE_DUE_NEXT_IMM_SERVE_SERIES_CODE = 1;
  private static final int FIELD_OUT_31_DOSE_DUE_NEXT_DOSE_NUMBER = 2;
  private static final int FIELD_OUT_32_DOSE_DUE_NEXT_DEPENDENT_DOSE_INDEX = 3;
  private static final int FIELD_OUT_33_DOSE_DUE_NEXT_ACCEPTABLE_ADMINISTRATION_DATE_ = 4;
  private static final int FIELD_OUT_34_DOSE_DUE_NEXT_RECOMMENDED_ADMINISTRATION_DATE = 5;
  private static final int FIELD_OUT_35_DOSE_DUE_NEXT_EXCEEDS_DATE_ = 6;

  private static final int FIELD_OUT_36_HIB_SERIES_COMPLETED_INDICATOR = 7;
  private static final int FIELD_OUT_37_HEPATITIS_A_SERIES_COMPLETED_INDICATOR = 8;
  private static final int FIELD_OUT_38_HEPATITIS_B_SERIES_COMPLETED_INDICATOR = 9;
  private static final int FIELD_OUT_39_PRIMARY_DTP_SERIES_COMPLETED_INDICATOR = 10;
  private static final int FIELD_OUT_40_POLIO_VACCINE_SERIES_COMPLETED_INDICATOR = 11;
  private static final int FIELD_OUT_41_MMR_SERIES_COMPLETED_INDICATOR = 12;
  private static final int FIELD_OUT_42_VARICELLA_SERIES_COMPLETED_INDICATOR = 13;
  private static final int FIELD_OUT_43_RV_SERIES_COMPLETED_INDICATOR = 14;
  private static final int FIELD_OUT_44_STREP_PNEUMOCOCCAL_SERIES_COMPLETED_INDICATOR = 15;
  private static final int FIELD_OUT_45_MENINGOCOCCAL_SERIES_COMPLETED_INDICATOR = 16;
  private static final int FIELD_OUT_46_HPV_SERIES_COMPLETED_INDICATOR = 17;

  private static final int FIELD_OUT_HUMAN_READABLE_LOG = 0; //TODO 

  private int runCode = 0;
  private String runProblem = "";

  private String request = "";
  private String[] fields = null;
  private StringBuilder response = new StringBuilder();
  private int currentPosition = 1;

  private static HashMap<String, String> seriesOutHash = new HashMap<String, String>();
  static {
    seriesOutHash.put(ImmunizationForecastDataBean.DTAP, "101"); // D/T Series
    seriesOutHash.put(ImmunizationForecastDataBean.DIPHTHERIA, "101"); // D/T Series
    seriesOutHash.put(ImmunizationForecastDataBean.TDAP, "101"); // D/T Series
    seriesOutHash.put(ImmunizationForecastDataBean.HEPA, "102"); // Hepatitis A Series
    seriesOutHash.put(ImmunizationForecastDataBean.HEPB, "103"); // Hepatitis B Series
    seriesOutHash.put(ImmunizationForecastDataBean.HIB, "104"); // Hib Series
    seriesOutHash.put(ImmunizationForecastDataBean.MMR, "105"); // MMR Series
    seriesOutHash.put(ImmunizationForecastDataBean.MEASLES, "105"); // MMR Series
    seriesOutHash.put(ImmunizationForecastDataBean.MUMPS, "105"); // MMR Series
    seriesOutHash.put(ImmunizationForecastDataBean.RUBELLA, "105"); // MMR  Series
    seriesOutHash.put(ImmunizationForecastDataBean.POLIO, "106"); // IPV/OPV  Series
    seriesOutHash.put(ImmunizationForecastDataBean.VARICELLA, "107"); // Varicella
    // seriesOutHash.put("", "108"); // HBIG -- IMM/Serve treats HBIG as a standalone series
    seriesOutHash.put(ImmunizationForecastDataBean.TD, "109"); // D/T Series
    seriesOutHash.put(ImmunizationForecastDataBean.ROTAVIRUS, "110"); // Rotavirus Series
    seriesOutHash.put(ImmunizationForecastDataBean.PNEUMO, "111"); // Strep-Pneumococcal Series
    seriesOutHash.put(ImmunizationForecastDataBean.INFLUENZA, "112"); // Influenza Series
    seriesOutHash.put(ImmunizationForecastDataBean.MENING, "113"); // Meningococcal Series
    seriesOutHash.put(ImmunizationForecastDataBean.HPV, "114"); // HPV Series
    // seriesOutHash.put("", "115"); // H1N1Flu Series
  }
  private static HashMap<String, String> doseDueOutHash = new HashMap<String, String>();
  static {
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"1"); // DTP
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"2"); // OPV
    doseDueOutHash.put(ImmunizationForecastDataBean.MMR, "3"); // MMR
    doseDueOutHash.put(ImmunizationForecastDataBean.MEASLES, "5"); // Measles
    doseDueOutHash.put(ImmunizationForecastDataBean.HEPB, "8"); // Hep B adolescent or pediatric
    doseDueOutHash.put(ImmunizationForecastDataBean.TD, "9"); // Td adult IPV
    doseDueOutHash.put(ImmunizationForecastDataBean.HIB, "17"); // Hib  unspecified
    doseDueOutHash.put(ImmunizationForecastDataBean.DTAP, "20"); // DTaP
    doseDueOutHash.put(ImmunizationForecastDataBean.VARICELLA, "21"); // Varicella
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"28"); // DT pediatric
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"30"); // HBIG
    doseDueOutHash.put(ImmunizationForecastDataBean.HEPA, "31"); // Hep A  pediatric
    doseDueOutHash.put(ImmunizationForecastDataBean.PNEUMO, "33"); // Strep  Pneumococcal (polysacchoride)
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"49"); // Hib PRP-OMP
    doseDueOutHash.put(ImmunizationForecastDataBean.HPV, "62"); // HPV,  quadrivalent
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"89"); // Unspecified Polio
    doseDueOutHash.put(ImmunizationForecastDataBean.MENING, "114"); // Meningococcal (MCV4)
    doseDueOutHash.put(ImmunizationForecastDataBean.TDAP, "115"); // Tdap
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"116"); // Rotavirus, pentavalent
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"119"); // Rotavirus,monovalent
    doseDueOutHash.put(ImmunizationForecastDataBean.ROTAVIRUS, "122"); // Rotavirus, NOS
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"128"); // H1N1-09, NOS
    doseDueOutHash.put(ImmunizationForecastDataBean.PNEUMO, "133"); // Pneumococcal,  PCV13
    doseDueOutHash.put(ImmunizationForecastDataBean.INFLUENZA, "141"); // Influenza, seasonal, injectable
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"-10"); // Td Adult  Booster
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"-12"); // Unspecified D/T
    // doseDueOutHash.put(ImmunizationForecastDataBean. ,"-13"); // Tdap Adult Booster

  }

  public CaretForecaster(String request) throws Exception {
    this.request = request;

    List<String> fieldList = new ArrayList<String>();
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
    fields = (String[]) fieldList.toArray(new String[] {});

    if (fields.length < FIELD_IN_28_NUMBER_OF_INPUT_DOSES) {
      runCode = -19;
      runProblem = "Input is too short expecting at least 28 fields";
    }

    // Set all fields to empty
    for (int i = 0; i < fields.length; i++) {
      if (fields[i] == null || fields[i].equals("NULL")) {
        fields[i] = "";
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

      Date forecastDate = readDate(FIELD_IN_01_DATE_USED_FOR_FORECAST);
      Date dateOfBirth = readDate(FIELD_IN_06_DATE_OF_BIRTH);
      String gender = readField(FIELD_IN_07_GENDER);

      Set<String> filterSet = new HashSet<String>();
      if (readField(FIELD_IN_09_PERTUSSIS_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.PERTUSSIS);
      }
      if (readField(FIELD_IN_10_DIPHTHERIA_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.DIPHTHERIA);
      }
      if (readField(FIELD_IN_11_TETANUS_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.DIPHTHERIA);
      }
      if (readField(FIELD_IN_12_HIB_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.HIB);
      }

      if (readField(FIELD_IN_14_HEPB_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.HEPB);
      }

      if (readField(FIELD_IN_16_IPV_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.POLIO);
      }

      if (readField(FIELD_IN_17_MEASLES_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.MEASLES);
      }

      if (readField(FIELD_IN_18_MUMPS_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.MUMPS);
      }

      if (readField(FIELD_IN_19_RUBELLA_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.RUBELLA);
      }

      if (readField(FIELD_IN_20_VARICELLA_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.VARICELLA);
      }

      if (readField(FIELD_IN_21_HEPA_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.HEPA);
      }

      if (readField(FIELD_IN_22_RV_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.ROTAVIRUS);
      }

      if (readField(FIELD_IN_23_S_PN_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.PNEUMO);
      }

      if (readField(FIELD_IN_24_INFLUENZA_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.INFLUENZA);
      }

      if (readField(FIELD_IN_25_MENINGOCOCCAL_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.MENING);
      }

      if (readField(FIELD_IN_26_HPV_CONTRAINDICATED_INDICATION).equals("1")) {
        filterSet.add(ImmunizationForecastDataBean.HPV);
      }

      ForecastRunner forecastRunner = new ForecastRunner(vaccineForecastManager);
      forecastRunner.getPatient().setDob(new DateTime(dateOfBirth));
      forecastRunner.getPatient().setSex(gender.toUpperCase().startsWith("M") ? "M" : "F");
      forecastRunner.setForecastDate(forecastDate);
      List<ImmunizationInterface> imms = forecastRunner.getImms();

      int numberOfDoses = readInt(FIELD_IN_28_NUMBER_OF_INPUT_DOSES);
      for (int i = 0; i < numberOfDoses; i++) {
        int offset = FIELD_IN_28_NUMBER_OF_INPUT_DOSES + (i * 6);
        String doseNote = readField(FIELD_IN_29_DOSE_NOTE + offset);
        String cvxCode = readField(FIELD_IN_30_DOSE_HL7_CODE + offset);
        // TODO mapping code to cvx
        // for now, tack on 0 for single digit codes
        if (cvxCode.length() == 1) {
          cvxCode = "0" + cvxCode;
        }
        Integer vaccineId = null;

        if (cvxToVaccineIdMap.containsKey(cvxCode)) {
          vaccineId = cvxToVaccineIdMap.get(cvxCode);
        }

        Date doseAdminDate = readDate(FIELD_IN_31_DATE_OF_DOSE_ADMINISTRATION + offset);

        String doseOveride = readField(FIELD_IN_32_DOSE_OVERRIDE + offset);
        if (doseOveride.equals(DOSE_OVERRIDE_EXCLUDED)) {
          // don't add to list because dose was excluded
          continue;
        }

        ImmunizationMDA imm = new ImmunizationMDA();
        imm.setVaccinationId("" + (i + 1));
        imm.setCvx(cvxCode);
        imm.setDateOfShot(doseAdminDate);
        imm.setDoseNote(doseNote);
        if (vaccineId == null) {
          imm.setHl7CodeErrorCode(HL7_CODE_ERROR_CODE_UNSUPPORTED);
          imm.setVaccineId(0);
        } else {
          imm.setHl7CodeErrorCode("0");
          imm.setVaccineId(vaccineId);
        }
        imms.add(imm);
      }

      // Run Forecast
      forecastRunner.forecast();

      DateTime today = new DateTime();

      String forecastingMode = readField(FIELD_IN_02_FORECASTING_MODE);
      if (!forecastingMode.equals(FORECASTING_MODE_ACCEPTABLE)) {
        forecastingMode = FORECASTING_MODE_RECOMMENDED;
      }

      // Put together response
      addValue("TCH Forecaster version " + SoftwareVersion.VERSION, FIELD_OUT_01_COPYRIGHT_NOTICE);
      addValue(today.toString("YMDHTS"), FIELD_OUT_02_RUN_DATE_AND_TIME);
      addValue((new DateTime(forecastDate)).toString("YMD"), FIELD_OUT_03_DATE_USED_FOR_FORECAST);
      addValue(forecastingMode, FIELD_OUT_04_FORECASTING_MODE);
      addValue(readField(FIELD_IN_03_VERSION), FIELD_OUT_05_VERSION);
      addValue(SoftwareVersion.VERSION, FIELD_OUT_06_RULE_SET_MAJOR_VERSION);
      addValue(forecastRunner.getForecasterScheduleName(), FIELD_OUT_07_RULE_SET_MINOR_VERSION);
      addValue(SoftwareVersion.VERSION_RELEASE, FIELD_OUT_08_RULE_SET_RELEASE_DATE);
      addValue(readField(FIELD_IN_04_PERSONAL_ID), FIELD_OUT_09_PERSONAL_ID);
      addValue(readField(FIELD_IN_05_USER_NOTE), FIELD_OUT_10_USER_NOTE);
      addValue(readField(FIELD_IN_06_DATE_OF_BIRTH), FIELD_OUT_11_DATE_OF_BIRTH);
      addValue(readField(FIELD_IN_07_GENDER), FIELD_OUT_68_GENDER);
      addValue(readField(FIELD_IN_28_NUMBER_OF_INPUT_DOSES), FIELD_OUT_13_NUMBER_OF_INPUT_DOSES);

      int base = FIELD_OUT_DOSE_BASE;
      for (int i = 0; i < imms.size(); i++) {
        ImmunizationMDA imm = (ImmunizationMDA) imms.get(i);
        addValue(imm.getDoseNote(), FIELD_OUT_14_DOSE_NOTE + base);
        addValue(imm.getCvx(), FIELD_OUT_15_DOSE_INPUT_HL7_CODE + base);
        addValue(imm.getDoseNote(), FIELD_OUT_16_DOSE_INPUT_HL7_CODE_ERROR_CODE + base);
        addValue(imm.getCvx(), FIELD_OUT_17_DOSE_INPUT_PRINT_STRING + base);
        addValue(imm.getCvx(), FIELD_OUT_18_DOSE_HL7_CODE_ + base);
        if (imm.getVaccineId() != 0) {
          addValue(imm.getCvx(), FIELD_OUT_19_DOSE_HL7_CODE_PRINT_STRING + base);
        }
        addValue(new DateTime(imm.getDateOfShot()).toString("YMD"), FIELD_OUT_20_DATE_OF_DOSE_ADMINISTRATION + base);
        base += FIELD_OUT_DOSE_BASE_ADD;
      }

      Set<String> nc = new HashSet<String>();

      List<ImmunizationForecastDataBean> forecastListDueToday = forecastRunner.getForecastListDueToday();
      List<ImmunizationForecastDataBean> forecastListDueTodayAdd = new ArrayList<ImmunizationForecastDataBean>();
      for (Iterator<ImmunizationForecastDataBean> it = forecastListDueToday.iterator(); it.hasNext();) {
        ImmunizationForecastDataBean forecastResult = it.next();
        nc.add(forecastResult.getForecastName());
        if (!seriesOutHash.containsKey(forecastResult.getForecastName())) {
          it.remove();
        }
        filter(filterSet, forecastListDueTodayAdd, it, forecastResult);
      }
      forecastListDueToday.addAll(forecastListDueTodayAdd);
      base++;
      addValue(String.valueOf(forecastListDueToday.size()), base);
      for (ImmunizationForecastDataBean forecastResult : forecastListDueToday) {
        String doseDueCode = seriesOutHash.get(forecastResult.getForecastName());
        //        String doseHL7Code = doseDueOutHash.get(forecastResult.getForecastName());
        //        if (doseHL7Code == null) {
        //          doseHL7Code = "";
        //        }
        addValue(doseDueCode, FIELD_OUT_23_DOSE_DUE_IMM_SERVE_SERIES_CODE + base);
        // addValue(doseHL7Code, FIELD_OUT_140_DOSE_DUE_DOSE_HL7_CODE + base);
        // addValue(forecastResult.getForecastLabel(), FIELD_OUT_141_DOSE_DUE_HL7_CODE_PRINT_STRING + base);
        addValue(forecastResult.getDose(), FIELD_OUT_24_DOSE_DUE_DOSE_NUMBER + base);
        boolean overdue = forecastResult.getStatusDescription().equals("overdue");
        addValue(overdue ? "1" : "0", FIELD_OUT_25_DOSE_DUE_PAST_DUE_INDICATOR + base);
        if (forecastingMode.equals(FORECASTING_MODE_ACCEPTABLE)) {
          addValue(d(forecastResult.getDue()), FIELD_OUT_26_DOSE_DUE_MINIMUM_DATE + base);
        } else {
          addValue(d(forecastResult.getValid()), FIELD_OUT_26_DOSE_DUE_MINIMUM_DATE + base);
        }
        addValue(d(forecastResult.getDue()), FIELD_OUT_27_DOSE_DUE_RECOMMENDED_DATE + base);
        addValue(d(forecastResult.getOverdue()), FIELD_OUT_28_DOSE_DUE_EXCEEDS_DATE + base);
        //        addValue(d(forecastResult.getOverdue()), FIELD_OUT_147_DOSE_DUE_MINIMUM_REMINDER_DATE + base);
        //        addValue(d(forecastResult.getOverdue()), FIELD_OUT_148_DOSE_DUE_RECOMMENDED_REMINDER_DATE + base);
        //        addValue(overdue ? "1" : "0", FIELD_OUT_149_DOSE_DUE_EXCEEDS_REMINDER_DATE + base);
        //        addValue("", FIELD_OUT_150_DOSE_DUE_VFC_PAYMENT_INDICATOR + base);
        base += FIELD_OUT_DOSE_DUE_BASE_ADD;
      }
      List<ImmunizationForecastDataBean> forecastListDueLater = forecastRunner.getForecastListDueLater();
      List<ImmunizationForecastDataBean> forecastListDueLaterAdd = new ArrayList<ImmunizationForecastDataBean>();
      for (Iterator<ImmunizationForecastDataBean> it = forecastListDueLater.iterator(); it.hasNext();) {
        ImmunizationForecastDataBean forecastResult = it.next();
        nc.add(forecastResult.getForecastName());
        if (!seriesOutHash.containsKey(forecastResult.getForecastName())) {
          it.remove();
        }
        filter(filterSet, forecastListDueLaterAdd, it, forecastResult);
      }
      forecastListDueLater.addAll(forecastListDueLaterAdd);
      base++;
      addValue(String.valueOf(forecastListDueLater.size()), base);
      for (ImmunizationForecastDataBean forecastResult : forecastListDueLater) {
        String doseDueCode = seriesOutHash.get(forecastResult.getForecastName());
        //        String doseHL7Code = doseDueOutHash.get(forecastResult.getForecastName());
        //        if (doseHL7Code == null) {
        //          doseHL7Code = "";
        //        }
        addValue(doseDueCode, FIELD_OUT_30_DOSE_DUE_NEXT_IMM_SERVE_SERIES_CODE + base);
        //        addValue(doseHL7Code, FIELD_OUT_153_DOSE_DUE_NEXT_DOSE_HL7_CODE + base);
        //        addValue(forecastResult.getForecastLabel(), FIELD_OUT_154_DOSE_DUE_NEXT_HL7_CODE_PRINT_STRING + base);
        addValue(forecastResult.getDose(), FIELD_OUT_31_DOSE_DUE_NEXT_DOSE_NUMBER + base);
        addValue("", FIELD_OUT_32_DOSE_DUE_NEXT_DEPENDENT_DOSE_INDEX + base);
        addValue(d(forecastResult.getValid()), FIELD_OUT_33_DOSE_DUE_NEXT_ACCEPTABLE_ADMINISTRATION_DATE_ + base);
        addValue(d(forecastResult.getDue()), FIELD_OUT_34_DOSE_DUE_NEXT_RECOMMENDED_ADMINISTRATION_DATE + base);
        addValue(d(forecastResult.getOverdue()), FIELD_OUT_35_DOSE_DUE_NEXT_EXCEEDS_DATE_ + base);
        //        addValue("", FIELD_OUT_160_DOSE_DUE_NEXT_VFC_PAYMENT_INDICATOR + base);
        base += FIELD_OUT_DOSE_DUE_NEXT_BASE_ADD;
      }
      addValue(c(nc, ImmunizationForecastDataBean.HIB), FIELD_OUT_36_HIB_SERIES_COMPLETED_INDICATOR + base);
      addValue(c(nc, ImmunizationForecastDataBean.HEPA), FIELD_OUT_37_HEPATITIS_A_SERIES_COMPLETED_INDICATOR + base);
      addValue(c(nc, ImmunizationForecastDataBean.HEPB), FIELD_OUT_38_HEPATITIS_B_SERIES_COMPLETED_INDICATOR + base);
      addValue(c(nc, ImmunizationForecastDataBean.DTAP), FIELD_OUT_39_PRIMARY_DTP_SERIES_COMPLETED_INDICATOR + base);
      addValue(c(nc, ImmunizationForecastDataBean.POLIO), FIELD_OUT_40_POLIO_VACCINE_SERIES_COMPLETED_INDICATOR + base);
      addValue(c(nc, ImmunizationForecastDataBean.MMR), FIELD_OUT_41_MMR_SERIES_COMPLETED_INDICATOR + base);
      addValue(c(nc, ImmunizationForecastDataBean.VARICELLA), FIELD_OUT_42_VARICELLA_SERIES_COMPLETED_INDICATOR + base);
      addValue(c(nc, ImmunizationForecastDataBean.ROTAVIRUS), FIELD_OUT_43_RV_SERIES_COMPLETED_INDICATOR + base);
      addValue(c(nc, ImmunizationForecastDataBean.PNEUMO), FIELD_OUT_44_STREP_PNEUMOCOCCAL_SERIES_COMPLETED_INDICATOR
          + base);
      addValue(c(nc, ImmunizationForecastDataBean.MENING), FIELD_OUT_45_MENINGOCOCCAL_SERIES_COMPLETED_INDICATOR + base);
      addValue(c(nc, ImmunizationForecastDataBean.HPV), FIELD_OUT_46_HPV_SERIES_COMPLETED_INDICATOR + base);

      description = forecastRunner.getTextReport(forecastingMode.equals(FORECASTING_MODE_ACCEPTABLE));

    } catch (Throwable t) {
      t.printStackTrace();
      errorCode = "Unable to Forecast, unexpected exeption occurred: " + t.getMessage();
    }
    return errorCode + "&&&" + response.toString() + "&&&" + description;
  }

  public void filter(Set<String> filterSet, List<ImmunizationForecastDataBean> forecastListDueTodayAdd,
      Iterator<ImmunizationForecastDataBean> it, ImmunizationForecastDataBean forecastResult) {
    if (filterSet.contains(forecastResult.getForecastName())) {
      it.remove();
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
    } else if (forecastResult.getForecastName().equals(ImmunizationForecastDataBean.DIPHTHERIA)
        || forecastResult.getForecastName().equals(ImmunizationForecastDataBean.DTAP)
        || forecastResult.getForecastName().equals(ImmunizationForecastDataBean.TD)
        || forecastResult.getForecastName().equals(ImmunizationForecastDataBean.TDAP)) {
      if (filterSet.contains(ImmunizationForecastDataBean.DIPHTHERIA)) {
        // if Diphtheria is contraindicated no combination is valid
        it.remove();
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
  }

  public ImmunizationForecastDataBean createForecastCopy(ImmunizationForecastDataBean forecastResult) {
    ImmunizationForecastDataBean forecastResultAdd = new ImmunizationForecastDataBean();
    forecastResultAdd.setComment(forecastResult.getComment());
    forecastResultAdd.setDateDue(forecastResult.getDateDue());
    forecastResultAdd.setDose(forecastResult.getDose());
    forecastResultAdd.setEarly(forecastResult.getEarly());
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

  private SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy");

  private int readInt(int position) throws Exception {
    String value = readField(position);
    if (value.equals("")) {
      return 0;
    }
    return Integer.parseInt(value);
  }

  private String d(Date d) {
    return new DateTime(d).toString("YMD");
  }

  private Date readDate(int position) throws Exception {
    String value = readField(position);
    if (value.equals("")) {
      value = "07041776";
    }
    return sdf.parse(value);
  }

  private String readField(int position) {
    position--;
    if (position < fields.length) {
      String value = fields[position];
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

  // java -classpath tch-forecaster.jar org.tch.forecast.core.server.CaretForecaster

  public static void main(String[] args) throws Exception {
    String request = ForecastServer.TEST_1;
    if (args.length > 0) {
      request = args[0];
    }
    CaretForecaster cf = new CaretForecaster(request);
    VaccineForecastManager vaccineForecastManager = new VaccineForecastManager();
    Map<String, Integer> cvxToVaccineIdMap = CvxCodes.getCvxToVaccineIdMap();
    String response = cf.forecast(vaccineForecastManager, cvxToVaccineIdMap);
    System.out.println(response);
  }
}
