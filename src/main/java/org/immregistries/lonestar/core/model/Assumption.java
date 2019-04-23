package org.immregistries.lonestar.core.model;

public class Assumption
{
  private String description = "";
  
  public Assumption(String description)
  {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
