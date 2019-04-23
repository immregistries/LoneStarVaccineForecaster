package org.immregistries.lonestar.core.api.impl;

import java.util.Date;

import org.immregistries.lonestar.core.api.model.ForecastRecommendationInterface;

public class ForecastRecommendation implements ForecastRecommendationInterface {

  private String displayLabel = "";
  private String antigenName = "";
  private String doseNumber = "";
  private Date dueDate = null;
  private Date validDate = null;
  private Date overdueDate = null;
  private Date finishedDate = null;
  private String statusDescription = "";
  private String evaluationExplanation = "";
  private String decisionProcessTextHTML = "";

  public String getDecisionProcessTextHTML() {
    return decisionProcessTextHTML;
  }

  public void setDecisionProcessTextHTML(String decisionProcessTextHTML) {
    this.decisionProcessTextHTML = decisionProcessTextHTML;
  }

  public String getAntigenName() {
    return antigenName;
  }

  public void setAntigenName(String antigenName) {
    this.antigenName = antigenName;
  }

  public String getDisplayLabel() {
    return displayLabel;
  }

  public void setDisplayLabel(String displayLabel) {
    this.displayLabel = displayLabel;
  }

  public String getEvaluationExplanation() {
    return evaluationExplanation;
  }

  public void setEvaluationExplanation(String evaluationExplanation) {
    this.evaluationExplanation = evaluationExplanation;
  }

  public String getDoseNumber() {
    return doseNumber;
  }

  public void setDoseNumber(String doseNumber) {
    this.doseNumber = doseNumber;
  }

  public Date getDueDate() {
    return dueDate;
  }

  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }

  public Date getValidDate() {
    return validDate;
  }

  public void setValidDate(Date validDate) {
    this.validDate = validDate;
  }

  public Date getOverdueDate() {
    return overdueDate;
  }

  public void setOverdueDate(Date overdueDate) {
    this.overdueDate = overdueDate;
  }

  public Date getFinishedDate() {
    return finishedDate;
  }

  public void setFinishedDate(Date finishedDate) {
    this.finishedDate = finishedDate;
  }

  public String getStatusDescription() {
    return statusDescription;
  }

  public void setStatusDescription(String statusDescription) {
    this.statusDescription = statusDescription;
  }
}
