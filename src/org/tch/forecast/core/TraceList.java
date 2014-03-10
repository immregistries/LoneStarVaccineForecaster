package org.tch.forecast.core;

import java.util.ArrayList;
import java.util.List;

public class TraceList extends ArrayList<Trace>
{
  private static final String BULLET_POINT_START = "+";
  private static final String RED = "[RED]";
  private static final String BLUE = "[BLUE]";

  private List<String> explanationList = null;
  private String statusDescription = "";
  private String forecastName = "";
  private String forecastLabel = "";

  public TraceList() {
    this.explanationList = new ArrayList<String>();
  }

  public TraceList(TraceList traceList) {
    this.explanationList = new ArrayList<String>(traceList.explanationList);
    this.statusDescription = traceList.statusDescription;
    this.forecastName = traceList.forecastName;
    this.forecastLabel = traceList.forecastLabel;
  }

  public String getForecastName() {
    return forecastName;
  }

  public void setForecastName(String forecastName) {
    this.forecastName = forecastName;
  }

  public String getForecastLabel() {
    return forecastLabel;
  }

  public void setForecastLabel(String forecastLabel) {
    this.forecastLabel = forecastLabel;
  }

  public String getStatusDescription() {
    return statusDescription;
  }

  public void setStatusDescription(String statusDescription) {
    this.statusDescription = statusDescription;
  }

  @Deprecated
  public String getExplanation() {
    return getExplanation(DecisionProcessFormat.HTML);
  }

  public String getExplanation(DecisionProcessFormat decisionProcessFormat) {
    StringBuffer stringBuffer = new StringBuffer();
    if (decisionProcessFormat == DecisionProcessFormat.HTML) {
      getExplanationInHtml(stringBuffer);
    } else {
      getExplanationInText(stringBuffer);
    }
    return stringBuffer.toString();
  }

  public void getExplanationInHtml(StringBuffer stringBuffer) {
    stringBuffer.append("<ul>");
    boolean bulletPointStarted = false;
    boolean colorRed = false;
    boolean colorBlue = false;
    boolean needToStartBullet = true;
    for (String explanation : explanationList) {
      if (explanation.equals(BULLET_POINT_START)) {
        needToStartBullet = true;
      } else if (explanation.equals(RED)) {
        colorRed = true;
      } else if (explanation.equals(BLUE)) {
        colorBlue = true;
      } else {
        if (needToStartBullet) {
          if (bulletPointStarted) {
            stringBuffer.append("</li>");
          }
          stringBuffer.append("<li>");
          bulletPointStarted = true;
          needToStartBullet = false;
        }
        if (colorRed) {
          stringBuffer.append("<font color=\"#FF0000\">");
        } else if (colorBlue) {
          stringBuffer.append("<font color=\"#0000FF\">");
        }
        stringBuffer.append(explanation);
        stringBuffer.append(" ");
        if (colorRed || colorBlue) {
          stringBuffer.append("</font>");
          colorRed = false;
          colorBlue = false;
        }
      }
    }
    if (bulletPointStarted) {
      stringBuffer.append("</li>");
    }
    stringBuffer.append("</ul>");
  }

  public void setExplanationRed() {
    explanationList.add(RED);
  }

  public void setExplanationBlue() {
    explanationList.add(BLUE);
  }

  public void setExplanationBulletPointStart() {
    explanationList.add(BULLET_POINT_START);
  }

  public void addExplanation(String explanation) {
    if (explanation != null) {
      explanation = explanation.trim();
      if (!explanation.equals("") && !explanation.equals(".")) {
        if (!explanation.endsWith(".")) {
          explanation = explanation + ".";
        }
        explanationList.add(explanation);
      }
    }
  }

  public void getExplanationInText(StringBuffer stringBuffer) {
    boolean needBullPointStarted = true;
    for (String explanation : explanationList) {
      if (explanation.equals(BULLET_POINT_START)) {
        needBullPointStarted = true;
      } else if (explanation.equals(RED)) {
        // do nothing
      } else if (explanation.equals(BLUE)) {
        // do nothing
      } else {
        if (needBullPointStarted) {
          stringBuffer.append(" + ");
          appendWrapLine(explanation, stringBuffer, 69);
          stringBuffer.append(" \r");
          needBullPointStarted = false;
        } else {
          stringBuffer.append("   ");
          appendWrapLine(explanation, stringBuffer, 69);
          stringBuffer.append(" \r");
        }
      }
    }
  }

  private void appendWrapLine(String explanation, StringBuffer stringBuffer, int lineLengthMax) {
    if (explanation.length() <= lineLengthMax) {
      stringBuffer.append(explanation);
    } else {
      String canPrint = explanation.substring(0, lineLengthMax);
      int lastSpace = canPrint.lastIndexOf(' ');
      if (lastSpace > 0) {
        canPrint = canPrint.substring(0, lastSpace);
      }
      String leftOver = explanation.substring(canPrint.length()).trim();
      stringBuffer.append(canPrint);
      stringBuffer.append("\r   ");
      appendWrapLine(leftOver, stringBuffer, lineLengthMax);
    }
  }

}
