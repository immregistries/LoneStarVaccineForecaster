package org.tch.forecast.core;

import java.util.ArrayList;

public class TraceList extends ArrayList<Trace>
{
  private StringBuffer explanation = new StringBuffer();
  private String statusDescription = "";
  private String forecastName = "";
  private String forecastLabel = "";
  
  public TraceList()
  {
    // default
  }
  
  public TraceList(TraceList traceList)
  {
    this.explanation = new StringBuffer(traceList.explanation);
    this.statusDescription = traceList.statusDescription;
    this.forecastName = traceList.forecastName;
    this.forecastLabel = traceList.forecastLabel;
  }
  
  public String getForecastName()
  {
    return forecastName;
  }
  public void setForecastName(String forecastName)
  {
    this.forecastName = forecastName;
  }
  public String getForecastLabel()
  {
    return forecastLabel;
  }
  public void setForecastLabel(String forecastLabel)
  {
    this.forecastLabel = forecastLabel;
  }
  public String getStatusDescription()
  {
    return statusDescription;
  }
  public void setStatusDescription(String statusDescription)
  {
    this.statusDescription = statusDescription;
  }
  public StringBuffer getExplanation()
   {
     return explanation;
   }
   public void setExplanation(StringBuffer explanation)
   {
     this.explanation = explanation;
   }
   
   public void append(String s)
   {
     explanation.append(s);
   }

}
