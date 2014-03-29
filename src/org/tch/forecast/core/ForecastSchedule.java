package org.tch.forecast.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ForecastSchedule
{
  private List<VaccineForecastDataBean> vaccineForecastList = new ArrayList<VaccineForecastDataBean>();
  private String scheduleName = "";

  public List<VaccineForecastDataBean> getVaccineForecastList()
  {
    return vaccineForecastList;
  }

  public void setVaccineForecastList(List<VaccineForecastDataBean> vaccineForecastList)
  {
    this.vaccineForecastList = vaccineForecastList;
  }

  public String getScheduleName()
  {
    return scheduleName;
  }

  public void setScheduleName(String scheduleName)
  {
    this.scheduleName = scheduleName;
  }
  
  public ForecastSchedule()
  {
    // default
  }
  
  public ForecastSchedule initFromText(String text, VaccineForecastManagerInterface forecastManager) throws Exception
  {
    InputStream is = new ByteArrayInputStream(text.getBytes());
    try {
      init(is, forecastManager);
    } catch (Exception exception)
    {
      throw new Exception("Unable to read XML definition ", exception);
    }
    return this;
  }
  
  public ForecastSchedule initFromResource(String resource, VaccineForecastManagerInterface forecastManager) throws Exception
  {
    InputStream is = this.getClass().getClassLoader().getResourceAsStream(resource);
    if (is == null)
    {
      throw new Exception("Unable to find XML definition " + resource + " that is required to run forecaster");
    }
    
    try {
      init(is, forecastManager);
    } catch (Exception exception)
    {
      throw new Exception("Unable to read XML definition " + resource, exception);
    }
    return this;
  }

  public void init(InputStream is, VaccineForecastManagerInterface forecastManager) throws ParserConfigurationException, Exception, SAXException, IOException
  {
    DocumentBuilderFactory factory;
    DocumentBuilder builder;
    factory = DocumentBuilderFactory.newInstance();
    factory.setIgnoringComments(true);
    factory.setIgnoringElementContentWhitespace(true);
    factory.setNamespaceAware(true);
    builder = factory.newDocumentBuilder();
    processDocument(builder.parse(new InputSource(is)), forecastManager);
  }

  protected Object processDocument(Document node, VaccineForecastManagerInterface forecastManager) throws Exception
  {
    Node n = node.getFirstChild();
    if (n != null)
    {
      processNode(n, forecastManager);
    }
    return null;
  }

  private void processNode(Node n, VaccineForecastManagerInterface forecastManager) throws Exception
  {
    String name = n.getNodeName();
    if (!name.equals("schedule"))
    {
      throw new Exception("Root node in definition xml should be 'forecast', instead found '" + name + "'");
    }
    scheduleName = DomUtils.getAttributeValue(n, "scheduleName");
    if (scheduleName == null )
    {
      scheduleName = "";
    }
    NodeList l = n.getChildNodes();
    for (int i = 0, icount = l.getLength(); i < icount; i++)
    {
      n = l.item(i);
      name = n.getNodeName();
      if (name.equals("forecast")) 
      {
        VaccineForecastDataBean vaccineForecast = new VaccineForecastDataBean();
        vaccineForecastList.add(vaccineForecast);
        vaccineForecast.processNode(n, forecastManager);
      }
    }
  }



}
