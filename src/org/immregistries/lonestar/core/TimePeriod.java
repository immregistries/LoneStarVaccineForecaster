package org.immregistries.lonestar.core;

import java.util.Date;

import org.immregistries.lonestar.core.DateTime;

public class TimePeriod
{
  public static final String MONTH = "M";
  public static final String DAY = "D";

  private String period = "";
  private int amount = 0;
  private TimePeriod addPeriod = null;
  private boolean even = false;

  public String toString()
  {
    if (period.equals(""))
    {
      return "";
    }
    int displayAmount = even ? amount / (period.equals(MONTH) ? 12 : 7) : amount;
    return displayAmount + " " + getPeriodDescription() + (addPeriod == null ? "" : " " + addPeriod);
  }

  private String getPeriodDescription()
  {
    if (period.equals(MONTH))
    {
      if (even)
      {
        return amount == 12 ? "year" : "years";
      }
      return amount == 1 ? "month" : "months";
    } else if (period.equals(DAY))
    {
      if (even)
      {
        return amount == 7 ? "week" : "weeks";
      }
      return amount == 1 ? "day" : "days";
    }
    return "";
  }

  public int getAmount()
  {
    return amount;
  }
  
  public boolean isFourDay()
  {
    return isDay() && getAmount() == 4 && addPeriod == null;
  }

  public boolean isMonth()
  {
    return period.equals(MONTH);
  }

  public boolean isDay()
  {
    return period.equals(DAY);
  }

  public TimePeriod() {
    period = DAY;
  }

  public boolean isEmpty()
  {
    return period.equals("");
  }

  public TimePeriod(String s) {
    if (s != null && s.length() > 0)
    {
      String[] parts = s.trim().split("\\s++");
      if (parts.length % 2 == 1 || parts.length == 1)
      {
        throw new IllegalArgumentException("Unable to parse, unrecognized format '" + s + "'");
      }
      init(parts, 0);
    }
  }

  private TimePeriod(String[] parts, int i) {
    init(parts, i);
  }

  private void init(String[] parts, int i)
  {
    String part1 = parts[i++];
    String part2 = parts[i++];
    amount = Integer.parseInt(part1);
    if (part2.equalsIgnoreCase("months") || part2.equalsIgnoreCase("month") || part2.equalsIgnoreCase("m"))
    {
      period = MONTH;
      even = amount % 12 == 0;
    } else if (part2.equalsIgnoreCase("years") || part2.equalsIgnoreCase("year") || part2.equalsIgnoreCase("y"))
    {
      period = MONTH;
      amount = amount * 12;
      even = true;
    } else if (part2.equalsIgnoreCase("days") || part2.equalsIgnoreCase("day") || part2.equalsIgnoreCase("d"))
    {
      period = DAY;
      even = amount % 7 == 0;
    } else if (part2.equalsIgnoreCase("weeks") || part2.equalsIgnoreCase("week") || part2.equalsIgnoreCase("w"))
    {
      period = DAY;
      amount = amount * 7;
      even = true;
    } else
    {
      throw new IllegalArgumentException("Unable to parse, unrecognized period '" + part2 + "'");
    }
    if (i < parts.length)
    {
      addPeriod = new TimePeriod(parts, i);
    }
  }

  public DateTime getDateTimeFrom(Date date)
  {
    DateTime dt = new DateTime(date);
    add(dt);
    return dt;
  }

  public DateTime getDateTimeFrom(DateTime dt)
  {
    dt = new DateTime(dt);
    add(dt);
    return dt;
  }
  
  public DateTime getDateTimeBefore(DateTime dt)
  {
    dt = new DateTime(dt);
    sub(dt);
    return dt;
  }

  private void add(DateTime dt)
  {
    if (!isEmpty())
    {
      if (isMonth())
      {
        dt.addMonths(amount);
      } else
      {
        dt.addDays(amount);
      }
    }
    if (addPeriod != null)
    {
      addPeriod.add(dt);
    }
  }
  
  private void sub(DateTime dt)
  {
    if (!isEmpty())
    {
      if (isMonth())
      {
        dt.addMonths(-amount);
      } else
      {
        dt.addDays(-amount);
      }
    }
    if (addPeriod != null)
    {
      addPeriod.sub(dt);
    }
  }
}
