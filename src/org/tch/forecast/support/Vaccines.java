package org.tch.forecast.support;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.tch.forecast.validator.db.DatabasePool;

public class Vaccines
{
  
  static Vaccine getByID(int id) throws Exception
  {
    Connection conn = null;
    try
    {
      conn = DatabasePool.getConnection();
      PreparedStatement pstmt = null;
      String sql = "SELECT vaccine_label FROM vaccine_tch WHERE vaccine_id = ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, id);
      ResultSet rset = pstmt.executeQuery();
      if (rset.next())
      {
        Vaccine vaccine = new Vaccine();
        vaccine.setVaccineID(id);
        vaccine.setDisplayName(rset.getString(1));
        return vaccine;
      }
    }
    finally
    {
      DatabasePool.close(conn);
    }

    return null;
  }
}
