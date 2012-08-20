// Copyright © 2007 Texas Children's Hospital.  All rights reserved.

package org.tch.forecast.core;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Nathan Bunker
 */
public class DateTime implements Serializable
{

  private static final long ONE_HOUR = 60 * 60 * 1000l;
  private static final long ONE_DAY = 24 * ONE_HOUR;

  public static final String FORMAT_FORMAL = "W, N O, Y";

  public static final String FORMAT_HL7 = "YMDHTS";
  public static final String FORMAT_KEY_TABLE = "Y-M-D |H:T:S";
  public static final String FORMAT_MYSQL = "Y-M-D |H:T:S";

  public static final String FORMAT_STANDARD = "M/D/Y |h:T:S A";

  private static String[] monthNamesLong =
  {
      "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
  };

  private static String[] monthNamesShort =
  {
      "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
  };
  public static final String NONE = "none";
  public static final String TODAY = "today";
  public static final String TOMORROW = "tomorrow";

  private static String[] weekNamesLong =
  {
      "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
  };
  private static String[] weekNamesShort =
  {
      "Sun", "Mon", "Tue", "Wed", "Thur", "Fri", "Sat"
  };
  private static String[] weekNamesVeryShort =
  {
      "Su", "M", "Tu", "W", "Th", "F", "Sa"
  };
  public static final String YESTERDAY = "yesterday";

  /*
   * Format codes Year Y : '2003' y : '03' Month M : '01' m : '1' N : 'January' n : 'Jan', 'Feb' Day
   * D : '09' d : '9', '10' O : '2nd', '3rd' Day of week W : 'Sunday' Long format w : 'Sun' Short
   * format I : 'Su', 'M' Shortest format i : '1', '2' i'th day in week Hour H : '12', '13' Military
   * time h : '12', '1' 12-hour clock time Minute T : '01', '02' Second S : '01', '02' AM/PM A :
   * 'AM', 'PM' a : 'am', 'pm' Use quotes to indicate string literals, use backslash to escape any
   * character. If the datetime object does not hold a date or time the date or time codes will not
   * be resolved (will print blank) (hint: you will need to use \\ in the java string literal to
   * ensure that the character \ is put in the string.) | : This special character splits the time
   * section from the date section. The date section first, the time section second. If there is no
   * date the first part will not print, if there is no time the second part will not print.
   */

  public static String format(String date, String format)
  {
    DateTime dt = new DateTime(date);
    return dt.toString(format);
  }

  // public static String formatHL7(String s) {
  // DateTime dt = new DateTime(s);
  // return dt.formatHL7();
  // }
  public static int getDaysInMonth(int year, int month)
  {
    if (month == 4 || month == 6 || month == 9 || month == 11)
    {
      return 30;
    }
    else if (month == 2)
    {
      if (year % 4 == 0)
      {
        if (year % 100 == 0 && !(year % 400 == 0))
        {
          return 28;
        }
        return 29;
      }
      return 28;
    }
    else if (month >= 1 && month <= 12)
    {
      return 31;
    }
    else
    {
      return 0;
    }

  }

  public static String getMonthLongName(int i)
  {
    i--;
    if (i >= 0 && i < monthNamesShort.length)
    {
      return monthNamesLong[i];
    }
    return "";
  }

  public static String getMonthShortName(int i)
  {
    i--;
    if (i >= 0 && i < monthNamesShort.length)
    {
      return monthNamesShort[i];
    }
    else
    {
      return "";
    }
  }

  private static String getOrdinal(int i)
  {
    int last_one_digit = i % 10;
    int last_two_digits = i % 100;
    if (last_two_digits < 4 || last_two_digits > 20)
    {
      if (last_one_digit == 1)
      {
        return i + "st";
      }
      if (last_one_digit == 2)
      {
        return i + "nd";
      }
      else if (last_one_digit == 3)
      {
        return i + "rd";
      }
      else
      {
        return i + "th";
      }
    }
    return i + "th";
  }

  public static void main(String[] argvs)
  {
    String s = "";
    if (argvs.length > 0)
    {
      s = argvs[0];
      for (int i = 1; i < argvs.length; i++)
      {
        s = s + " " + argvs[i];
      }
    }
    System.out.println();
    DateTime dt = new DateTime(s);
    if (dt.hasValidDate())
    {
      System.out.println("'" + dt.toString(FORMAT_FORMAL) + "'");
    }
    else
    {
      System.out.println("Not a recognized date");
    }
  }

  private static int parseInt(String s)
  {
    int i = 0;
    try
    {
      i = Integer.parseInt(s);
    }
    catch (NumberFormatException nfe)
    {
      i = 0;
    }
    return i;
  }

  private static String zeroPad(int i)
  {
    if (i < 10)
    {
      return "0" + i;
    }
    else
    {
      return "" + i;
    }
  }
  private int day = 0;
  private int hour = 24;
  private int minute = 0;

  private int month = 0;
  private int second = 0;

  private int year = 0;

  public DateTime()
  {
    setToday();
  }

  public DateTime(Date d)
  {
    set(d);
  }

  public DateTime(DateTime dt)
  {
    this.year = dt.year;
    this.month = dt.month;
    this.day = dt.day;
    this.hour = dt.hour;
    this.minute = dt.minute;
    this.second = dt.second;
  }

  public DateTime(int year, int month, int day)
  {
    this.year = year;
    this.month = month;
    this.day = day;
  }

  public DateTime(int year, int month, int day, int hour, int minute, int second)
  {

    this.year = year;
    this.month = month;
    this.day = day;
    this.hour = hour;
    this.minute = minute;
    this.second = second;
  }

  public DateTime(long timestamp)
  {
    set(new Date(timestamp));
  }

  public DateTime(String s)
  {
    if (s == null)
    {
      s = NONE;
    }
    set(s);
  }

  public void addDays(int i)
  {
    int d = getDaysInMonth();
    day = day + i;
    while (day > d)
    {
      month++;
      if (month > 12)
      {
        year++;
        month = 1;
      }
      day = day - d;
      d = getDaysInMonth();
    }
    while (day < 1)
    {
      month--;
      if (month < 0)
      {
        year--;
        month = 12;
      }
      day = day + getDaysInMonth();
    }
  }

  public void addHours(int i)
  {
    if (hour == 24)
    {
      hour = 0;
    }
    hour = hour + i;
    if (hour >= 24)
    {
      int d = hour / 24;
      addDays(d);
      hour = hour - d * 24;
    }
    else if (hour < 0)
    {
      int d = (24 - hour) / 24;
      addDays(-d);
      hour = hour + (d * 24);
    }
  }

  public void addMinutes(int i)
  {
    minute = minute + i;
    if (minute >= 60)
    {
      int h = minute / 60;
      addHours(h);
      minute = minute - h * 60;
    }
    else if (minute < 0)
    {
      int h = (60 - minute) / 60;
      addHours(-h);
      minute = minute + (h * 60);
    }
  }

  public void addMonths(int i)
  {
    month = month + i;
    if (month > 12)
    {
      int y = (month - 1) / 12;
      addYears(y);
      month = month - y * 12;
    }
    else if (month < 1)
    {
      int y = (12 - month) / 12;
      addYears(-y);
      month = month + (y * 12);
    }
    int d = getDaysInMonth();
    if (day > d && d > 0)
    {
      day = d;
    }
  }

  public void addSeconds(int i)
  {
    second = second + i;
    if (second >= 60)
    {
      int m = second / 60;
      addMinutes(m);
      second = second - m * 60;
    }
    else if (second < 0)
    {
      int m = (60 - second) / 60;
      addMinutes(-m);
      second = second + (m * 60);
    }
  }

  public void addYears(int i)
  {
    year = year + i;
    int d = getDaysInMonth();
    if (day > d && d > 0)
    {
      day = d;
    }
  }

  public boolean equals(Object o)
  {
    boolean eq = true;
    if (o instanceof DateTime)
    {
      DateTime dt = (DateTime) o;
      if (dt.year != this.year && (dt.year == 0 || this.year == 0))
      {
        eq = false;
      }
      else if (dt.hour != this.hour && (dt.hour == 24 || this.hour == 24))
      {
        eq = false;
      }
      else
      {
        if (this.year > 0)
        {
          eq = this.year == dt.year && this.month == dt.month && this.day == dt.day;
        }
        if (eq && this.hour < 24)
        {
          eq = this.hour == dt.hour && this.minute == dt.minute && this.second == dt.second;
        }
      }
    }
    else
    {
      eq = super.equals(o);
    }
    return eq;
  }

  private String format(char[] c, int start, int end)
  {
    String l = "YyMmNnDdOWwIiHhTSAa";
    StringBuffer sbuf = new StringBuffer();
    boolean literal = false;
    boolean escaped = false;
    for (int i = start; i < end; i++)
    {
      if (escaped)
      {
        sbuf.append(c[i]);
        escaped = false;
      }
      else if (c[i] == '\\')
      {
        escaped = true;
      }
      else if (c[i] == '\'')
      {
        literal = !literal;
      }
      else if (literal)
      {
        sbuf.append(c[i]);
      }
      else if (l.indexOf(c[i]) != -1)
      {
        if (year > 0)
        {
          switch (c[i])
          {
            case 'Y' :
              sbuf.append(year);
              break;
            case 'y' :
              sbuf.append(zeroPad(year % 1000));
              break;
            case 'M' :
              sbuf.append(zeroPad(month));
              break;
            case 'm' :
              sbuf.append(month);
              break;
            case 'N' :
              sbuf.append(monthNamesLong[month - 1]);
              break;
            case 'n' :
              sbuf.append(monthNamesShort[month - 1]);
              break;
            case 'D' :
              sbuf.append(zeroPad(day));
              break;
            case 'd' :
              sbuf.append(day);
              break;
            case 'O' :
              sbuf.append(getOrdinal(day));
              break;
            case 'W' :
              sbuf.append(weekNamesLong[getDayInWeek() - 1]);
              break;
            case 'w' :
              sbuf.append(weekNamesShort[getDayInWeek() - 1]);
              break;
            case 'I' :
              sbuf.append(weekNamesVeryShort[getDayInWeek() - 1]);
              break;
            case 'i' :
              sbuf.append(getDayInWeek());
              break;
          }
        }
        if (hour < 24)
        {
          switch (c[i])
          {
            case 'H' :
              sbuf.append(zeroPad(hour));
              break;
            case 'h' :
              sbuf.append((hour == 0) ? 12 : (hour > 12 ? hour - 12 : hour));
              break;
            case 'T' :
              sbuf.append(zeroPad(minute));
              break;
            case 'S' :
              sbuf.append(zeroPad(second));
              break;
            case 'A' :
              sbuf.append((hour < 12) ? "AM" : "PM");
              break;
            case 'a' :
              sbuf.append((hour < 12) ? "am" : "pm");
              break;
          }
        }
      }
      else
      {
        sbuf.append(c[i]);
      }
    }
    return sbuf.toString();
  }

  // public String formatFormal() {
  // StringBuffer sbuf = new StringBuffer("");
  // if (year > 0) {
  // if (month >= 1 && month <= 12) {
  // sbuf.append(monthNamesLong[month - 1]);
  // }
  // if (day > 0) {
  // sbuf.append(' ');
  // sbuf.append(getOrdinal(day));
  // sbuf.append(',');
  // }
  // sbuf.append(' ');
  // sbuf.append(year);
  // }
  // if (hour < 24) {
  // sbuf.append(year > 0 ? " " : "");
  // if (hour == 0 || hour == 12) {
  // sbuf.append("12");
  // } else {
  // sbuf.append(hour < 12 ? hour : hour - 12);
  // }
  // sbuf.append(':');
  // sbuf.append(zeroPad(minute));
  // sbuf.append(' ');
  // sbuf.append(hour < 12 ? "AM" : "PM");
  // }
  // return sbuf.toString();
  // }

  public Calendar getCalendar()
  {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month - 1);
    cal.set(Calendar.DAY_OF_MONTH, day);
    if (hour < 24)
    {
      cal.set(Calendar.HOUR_OF_DAY, hour);
      cal.set(Calendar.MINUTE, minute);
      cal.set(Calendar.SECOND, second);
    }
    else
    {
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
    }
    return cal;
  }

  public Calendar getCalendarDateOnly()
  {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month - 1);
    cal.set(Calendar.DAY_OF_MONTH, day);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    return cal;
  }

  public Date getDate()
  {
    return getCalendar().getTime();
  }

  public java.sql.Date getSQLDate()
  {
    return new java.sql.Date(getCalendar().getTimeInMillis());
  }

  public Timestamp getSQLTimestamp()
  {
    return new Timestamp(getCalendar().getTimeInMillis());
  }

  public int getDay()
  {
    return day;
  }

  public int getDayInWeek()
  {
    int century = year / 100;
    int cy = year % 100;
    int centuryStart = (3 - century % 4) * 2;
    int yearStart = centuryStart + cy + cy / 4;
    boolean leapYear = year % 400 == 0 || (year % 4 == 0 && year % 100 != 0);
    int leapYearOffset = leapYear ? -1 : 0;
    return (yearStart + getDayInYear() + leapYearOffset) % 7 + 1;
  }

  public int getDayInYear()
  {
    int d = day;
    for (int i = 1; i < month; i++)
    {
      d = d + getDaysInMonth(year, i);
    }
    return d;
  }

  public int getDaysInMonth()
  {
    return getDaysInMonth(year, month);
  }

  public int getHour()
  {
    return hour;
  }

  public int getMinute()
  {
    return minute;
  }

  public int getMonth()
  {
    return month;
  }

  public String getMonthLongName()
  {
    return getMonthLongName(month);
  }

  public String getMonthShortName()
  {
    return getMonthShortName(month);
  }

  public int getSecond()
  {
    return second;
  }

  // public String formatStandard() {
  // StringBuffer sbuf = new StringBuffer("");
  // if (year > 0) {
  // sbuf.append(zeroPad(month));
  // if (day > 0) {
  // sbuf.append('/');
  // sbuf.append(zeroPad(day));
  // }
  // sbuf.append('/');
  // sbuf.append(year);
  // }
  // if (hour < 24) {
  // sbuf.append(year > 0 ? " " : "");
  // if (hour == 0 || hour == 12) {
  // sbuf.append("12");
  // } else {
  // sbuf.append(zeroPad(hour < 12 ? hour : hour - 12));
  // }
  // sbuf.append(':');
  // sbuf.append(zeroPad(minute));
  // sbuf.append(':');
  // sbuf.append(zeroPad(second));
  // sbuf.append(' ');
  // sbuf.append(hour < 12 ? "AM" : "PM");
  // }
  // return sbuf.toString();
  // }

  public String getWeekNameLong()
  {
    String weekName = "";
    int i = getDayInWeek();
    i--;
    if (i >= 0 && i < weekNamesLong.length)
    {
      weekName = weekNamesLong[i];
    }
    return weekName;
  }

  public String getWeekNameShort()
  {
    String weekName = "";
    int i = getDayInWeek();
    i--;
    if (i >= 0 && i < weekNamesShort.length)
    {
      weekName = weekNamesLong[i];
    }
    return weekName;
  }

  public String getWeekNameVeryShort()
  {
    String weekName = "";
    int i = getDayInWeek();
    i--;
    if (i >= 0 && i < weekNamesVeryShort.length)
    {
      weekName = weekNamesLong[i];
    }
    return weekName;
  }

  public int getYear()
  {
    return year;
  }

  public boolean hasDate()
  {
    return year > 0;
  }

  public boolean hasTime()
  {
    return hour < 24;
  }

  public boolean hasValidDate()
  {
    if (year <= 0 || month <= 0 || day <= 0 || day > 31 || month > 12)
    {
      return false;
    }
    return day <= getDaysInMonth();
  }

  public boolean isGreaterThan(DateTime dt)
  {
    boolean greater = true;
    boolean finished = true;
    if (this.year != 0 && dt.year != 0)
    {
      if (this.year < dt.year)
      {
        greater = false;
      }
      else if (this.year == dt.year)
      {
        if (this.month < dt.month)
        {
          greater = false;
        }
        else if (this.month == dt.month)
        {
          if (this.day < dt.day)
          {
            greater = false;
          }
          else if (this.day == dt.day)
          {
            finished = false;
          }
        }
      }
    }
    else
    {
      finished = false;
    }
    if (!finished && this.hour < 24 && dt.hour < 24)
    {
      if (this.hour < dt.hour)
      {
        greater = false;
      }
      else if (this.hour == dt.hour)
      {
        if (this.minute < dt.minute)
        {
          greater = false;
        }
        else if (this.minute == dt.minute)
        {
          if (this.second <= dt.second)
          {
            greater = false;
          }
        }
      }
      finished = true;
    }

    return greater && finished;
  }

  public boolean isGreaterThanOrEquals(DateTime dt)
  {
    return equals(dt) || isGreaterThan(dt);
  }

  public boolean isLessThan(DateTime dt)
  {
    boolean less = true;
    boolean finished = true;
    if (this.year != 0 && dt.year != 0)
    {
      if (this.year > dt.year)
      {
        less = false;
      }
      else if (this.year == dt.year)
      {
        if (this.month > dt.month)
        {
          less = false;
        }
        else if (this.month == dt.month)
        {
          if (this.day > dt.day)
          {
            less = false;
          }
          else if (this.day == dt.day)
          {
            finished = false;
          }
        }
      }
    }
    else
    {
      finished = false;
    }
    if (!finished && this.hour < 24 && dt.hour < 24)
    {
      if (this.hour > dt.hour)
      {
        less = false;
      }
      else if (this.hour == dt.hour)
      {
        if (this.minute > dt.minute)
        {
          less = false;
        }
        else if (this.minute == dt.minute)
        {
          if (this.second >= dt.second)
          {
            less = false;
          }
        }
      }
      finished = true;
    }

    return less && finished;
  }

  public boolean isLessThanOrEquals(DateTime dt)
  {
    return equals(dt) || isLessThan(dt);
  }

  public void set(Date d)
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    year = cal.get(Calendar.YEAR);
    month = cal.get(Calendar.MONTH) + 1;
    day = cal.get(Calendar.DAY_OF_MONTH);
    hour = cal.get(Calendar.HOUR_OF_DAY);
    minute = cal.get(Calendar.MINUTE);
    second = cal.get(Calendar.SECOND);
  }

  public void set(DateTime dt)
  {
    this.year = dt.year;
    this.month = dt.month;
    this.day = dt.day;
    this.hour = dt.hour;
    this.minute = dt.minute;
    this.second = dt.second;
  }

  public void set(String s)
  {
    s = s.trim();
    if (s.equalsIgnoreCase(TODAY))
    {
      setToday();
    }
    else if (s.equalsIgnoreCase(YESTERDAY))
    {
      setToday();
      addDays(-1);
    }
    else if (s.equalsIgnoreCase(TOMORROW))
    {
      setToday();
      addDays(1);
    }
    else if (s.equalsIgnoreCase(NONE))
    {
      this.year = 0;
      this.month = 0;
      this.day = 0;
      this.hour = 24;
      this.minute = 0;
      this.second = 0;
    }
    else
    {
      s = setTimeZone(s);
      s = setTime(s);
      if (s.indexOf('/') > 0)
      {
        setDate(s, '/');
      }
      else if (s.indexOf('-') > 0)
      {
        setDate(s, '-');
      }
      else
      {
        setHL7Date(s);
      }
    }
  }

  public void setDate(int year, int month, int day)
  {
    this.year = year;
    this.month = month;
    this.day = day;
  }

  private void setDate(String s, char delim)
  {
    s = s.trim();
    int i = s.indexOf(delim);
    int j = s.length();
    int e = s.length();
    if (i == -1)
    {
      i = e;
    }
    else
    {
      j = s.indexOf(delim, i + 1);
      if (j == -1)
      {
        j = e;
      }
    }
    try
    {
      if (i != 0)
      {
        month = Integer.parseInt(s.substring(0, i));
      }
      else
      {
        month = 0;
      }
      if (i++ < e)
      {
        if (i != j)
        {
          day = Integer.parseInt(s.substring(i, j));
        }
        else
        {
          day = 0;
        }
        if (++j < e)
        {
          year = Integer.parseInt(s.substring(j, e));
        }
        else
        {
          year = 0;
        }
      }
    }
    catch (NumberFormatException nfe)
    {
      year = month = day = 0;
    }
    if (month > 1800 && (day <= 12 && day >= 1) && (year <= 31))
    {
      i = year;
      year = month;
      month = day;
      day = i;
    }
    else if (year == 0 && day > 1800 && (month <= 12 && month >= 1))
    {
      year = day;
      day = 0;
    }
  }

  public void setDay(int i)
  {
    day = i;
    if (day > 31 || day < 1)
    {
      day = 0;
    }
    else
    {
      if (year > 0)
      {
        if (day > getDaysInMonth())
        {
          day = getDaysInMonth();
        }
      }
    }
  }

  private void setHL7Date(String s)
  {
    char[] c = s.toCharArray();
    boolean looksLikeHL7 = true;
    for (int i = 0; i < c.length; i++)
    {
      if (c[i] < '0' || c[i] > '9')
      {
        looksLikeHL7 = false;
      }
    }
    if (looksLikeHL7)
    {
      if (c.length >= 4)
      {
        year = parseInt(s.substring(0, 4));
        if (year < 1232 && c.length == 8)
        {
          month = parseInt(s.substring(0, 2));
          day = parseInt(s.substring(2, 4));
          year = parseInt(s.substring(4, 8));
        }
        else if (c.length >= 6)
        {
          month = parseInt(s.substring(4, 6));
          if (c.length >= 8)
          {
            day = parseInt(s.substring(6, 8));
            if (c.length >= 10)
            {
              hour = parseInt(s.substring(8, 10));
              if (c.length >= 12)
              {
                minute = parseInt(s.substring(10, 12));
                if (c.length >= 14)
                {
                  second = parseInt(s.substring(12, 14));
                }
              }
            }
          }
        }
      }
    }
  }

  public void setHour(int i)
  {
    this.hour = i;
    if (hour > 24 || hour < 0)
    {
      hour = 0;
    }
  }

  public void setMinute(int i)
  {
    minute = i;
    if (minute >= 60 || minute < 0)
    {
      minute = 0;
    }
  }

  public void setMonth(int i)
  {
    month = i;
    if (month > 12 || month < 1)
    {
      year = day = month = 0;
    }
  }

  public void setSecond(int i)
  {
    second = i;
    if (second >= 60 || second < 0)
    {
      second = 0;
    }
  }

  public void setTime(int hour, int minute, int second)
  {
    this.hour = hour;
    this.minute = minute;
    this.second = second;
  }

  private String setTime(String s)
  {
    int i = s.indexOf(':');
    int colonCount = 0;
    if (i > 0)
    {
      i = (i > 1) ? i - 2 : 0;
      String time = s.substring(i, s.length()).trim().toUpperCase();
      s = s.substring(0, i);
      i = time.indexOf(':');
      colonCount++;
      try
      {
        hour = Integer.parseInt(time.substring(0, i));
        int j = time.indexOf(':', ++i);
        if (j < i)
        {
          j = i + 2;
          if (j >= time.length())
          {
            j = time.length();
          }
        }
        else
        {
          colonCount++;
        }
        minute = Integer.parseInt(time.substring(i, j));
        if (colonCount == 2)
        {
          i = j + 1;
          if (i < time.length())
          {
            j = i + 2;
            if (j <= time.length())
            {
              second = Integer.parseInt(time.substring(i, j));
            }
          }
        }
      }
      catch (NumberFormatException nfe)
      {
        hour = 24;
        minute = second = 0;
      }
      if (hour == 12 && time.indexOf("AM") > -1)
      {
        hour = 0;
      }
      else if (time.indexOf("PM") > -1)
      {
        if (hour != 12)
        {
          hour = hour + 12;
        }
      }
    }
    return s;
  }

  private String setTimeZone(String s)
  {
    if (s.length() > 4)
    {
      char[] sc = s.toCharArray();
      int i = sc.length - 5;
      boolean hasTimeZone = true;
      if (sc[i] != '+' && sc[i] != '-')
      {
        hasTimeZone = false;
      }
      else
      {
        i++;
        while (i < sc.length)
        {
          if (sc[i] < '0' || sc[i] > '9')
          {
            hasTimeZone = false;
            break;
          }
          i++;
        }
      }
      i = sc.length - 5;
      if (hasTimeZone && sc[i] == '-')
      {
        int dashCount = 0;
        for (int j = 0; j < sc.length; j++)
        {
          if (sc[j] == '-')
          {
            dashCount++;
          }
        }
        if (dashCount % 2 == 0)
        {
          hasTimeZone = false;
          // If there are an odd number of dashes then this
          // dash must be associated with the date, not the time zone.
          // Allows for formats such as DD-MM-YYYY
          // so they won't be interpreted as DD-MM-ZZZZ
          // This would be okay DD-MM-YYYY-ZZZZ
        }
      }
      if (hasTimeZone)
      {
        // int plus = 1;
        // if (sc[i] == '-') {
        // plus = -1;
        // }
        // String tz = s.substring(i + 1, s.length());
        s = s.substring(0, i);
        // timeZoneHour = plus * Integer.parseInt(tz.substring(0, 2));
        // timeZoneMinute = Integer.parseInt(tz.substring(2, 4));
      }
    }
    return s;
  }

  private void setToday()
  {
    set(new Date());
  }

  public void setYear(int i)
  {
    this.year = i;
    if (year == 0)
    {
      day = month = 0;
    }
  }

  public String toString()
  {
    return toString(FORMAT_STANDARD);
  }

  public String toString(String format)
  {
    char[] c = format.toCharArray();
    int start = 0;
    int end = 0;
    boolean escaped = false;
    boolean literal = false;
    for (end = 0; end < c.length && (literal || escaped || c[end] != '|'); end++)
    {
      if (escaped)
      {
        escaped = false;
      }
      else if (c[end] == '\\')
      {
        escaped = true;
      }
      else if (c[end] == '\'')
      {
        literal = !literal;
      }
    }
    if (end < c.length)
    {
      if (year > 0 && hour < 24)
      {
        return format(c, start, end) + format(c, end + 1, c.length);
      }
      else if (year > 0)
      {
        return format(c, start, end);
      }
      else if (hour < 24)
      {
        return format(c, end + 1, c.length);
      }
      else
      {
        return "";
      }
    }
    else
    {
      if (year > 0 || hour < 24)
      {
        return format(c, 0, c.length);
      }
      else
      {
        return "";
      }
    }
  }

  public int getDaysBetween(DateTime dt)
  {
    long difInSeconds = getCalendarDateOnly().getTimeInMillis() - dt.getCalendarDateOnly().getTimeInMillis();
    if (difInSeconds > 0)
    {
      return (int) ((difInSeconds + ONE_HOUR) / (float) ONE_DAY);
    }
    else
    {
      return (int) ((difInSeconds - ONE_HOUR) / (float) ONE_DAY);
    }
  }

}