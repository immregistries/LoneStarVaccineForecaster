package org.immregistries.lonestar.core.logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.immregistries.lonestar.core.ImmunizationInterface;

public class Event
{
  protected Date eventDate = null;
  protected boolean hasEvent = false;
  protected List<ImmunizationInterface> immList = new ArrayList<ImmunizationInterface>();
  
  public Date getEventDate()
  {
    return eventDate;
  }
  public List<ImmunizationInterface> getImmList()
  {
    return immList;
  }
  public boolean isHasEvent()
  {
    return hasEvent;
  }
  public void setEventDate(Date eventDate)
  {
    this.eventDate = eventDate;
  }
  public void setHasEvent(boolean hasEvent)
  {
    this.hasEvent = hasEvent;
  }
  public void setImmList(List<ImmunizationInterface> immList)
  {
    this.immList = immList;
  }
}
