package org.tch.forecast.core;

import java.io.PrintWriter;
import java.io.StringWriter;
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
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    if (decisionProcessFormat == DecisionProcessFormat.HTML) {
      getExplanationInHtml(printWriter);
    } else {
      getExplanationInText(printWriter);
    }
    printWriter.close();
    return stringWriter.toString();
  }
  
  public void printExplanation(PrintWriter out, DecisionProcessFormat decisionProcessFormat)
  {
    
  }

  public void getExplanationInHtml(PrintWriter out) {
    out.print("<ul>");
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
            out.print("</li>");
          }
          out.print("<li>");
          bulletPointStarted = true;
          needToStartBullet = false;
        }
        if (colorRed) {
          out.print("<font color=\"#FF0000\">");
        } else if (colorBlue) {
          out.print("<font color=\"#0000FF\">");
        }
        out.print(explanation);
        out.print(" ");
        if (colorRed || colorBlue) {
          out.print("</font>");
          colorRed = false;
          colorBlue = false;
        }
      }
    }
    if (bulletPointStarted) {
      out.print("</li>");
    }
    out.print("</ul>");
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

  public void getExplanationInText(PrintWriter out) {
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
          out.print(" + ");
          appendWrapLine(explanation, out, 69);
          out.println();
          needBullPointStarted = false;
        } else {
          out.print("   ");
          appendWrapLine(explanation, out, 69);
          out.println();
        }
      }
    }
  }

  private void appendWrapLine(String explanation, PrintWriter out, int lineLengthMax) {
    if (explanation.length() <= lineLengthMax) {
      out.print(explanation);
    } else {
      String canPrint = explanation.substring(0, lineLengthMax);
      int lastSpace = canPrint.lastIndexOf(' ');
      if (lastSpace > 0) {
        canPrint = canPrint.substring(0, lastSpace);
      }
      String leftOver = explanation.substring(canPrint.length()).trim();
      out.println(canPrint);
      out.print("   ");
      appendWrapLine(leftOver, out, lineLengthMax);
    }
  }

}
