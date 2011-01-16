package org.tch.forecast.db;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
/*
 * Author: Parth Sanaja
 * */

public class StmtHelper
{
	private static final String sql_delim = ", ";
    private static final char sql_delim_char = ',';
    private static final String sql_sp = " ";
    private static final String sql_openb = " (";
    private static final String sql_closeb = ") ";
    private static final String sql_eqpar = " = ?";
    private static final String sql_par = " ?";

    public static PreparedStatement genInsert( Connection con, String table, Object model ,String [] exceptionColumns )
    throws SQLException{
        PreparedStatement stmt = null;
        StringBuffer buffer = new StringBuffer( "INSERT INTO " );
        StringBuffer buffer2 = new StringBuffer( " VALUES (" );
        buffer.append( table).append(sql_openb );
        Field[] fieldDefs = model.getClass().getFields();
        int size = fieldDefs.length;
        try{
            for (int i = 0; i < size; i++){
				if(isNameExist(fieldDefs[i].getName(),exceptionColumns)){
					continue;
				}
                if (fieldDefs[i].get( model ) != null){
                    buffer.append( fieldDefs[i].getName() );
                    buffer.append( sql_delim );
                    buffer2.append( sql_par );
                    buffer2.append( sql_delim );
                }
            }
            if (buffer.charAt( buffer.length() - 2 ) == sql_delim_char){
                buffer.replace( buffer.length() - 2, buffer.length(), sql_closeb );
                buffer2.replace( buffer2.length() - 2, buffer2.length(), sql_closeb );
            }else{
                buffer.append( sql_closeb );
                buffer2.append( sql_closeb );
            }
            buffer.append( buffer2);
            stmt = con.prepareStatement( buffer.toString() );
            fillStmt( stmt, model, exceptionColumns );
	    }
        catch (IllegalAccessException iae){
        	iae.printStackTrace();
        }
        System.out.println(buffer);
        return stmt;
    }

    
    public static void fillStmt( PreparedStatement stmt, Object model,String [] exceptionColumns )
    throws SQLException{
        Field[] fieldDefs = model.getClass().getFields();
        int idx = 0;
        try{
            int size = fieldDefs.length;
            for (int i = 0; i < size; i++){
				if(isNameExist(fieldDefs[i].getName(),exceptionColumns)){
					continue;
				}
                if (fieldDefs[i].get( model ) != null){
                    updateStmt( stmt, fieldDefs[i], fieldDefs[i].get( model ), ++idx);
                }
            }
        }catch (IllegalAccessException iae){
        	iae.printStackTrace();
        }
    }
    
    public static void updateStmt(PreparedStatement stmt, Field field, Object value, int idx )
    throws SQLException {
  		updateStmt(stmt, field.getType(), value, idx);
    }

    public static void updateStmt(PreparedStatement stmt, Class type, Object value, int idx)
    throws SQLException
  	{
  		 if (type == java.lang.String.class) {
  			 stmt.setString(idx,(String)value);
  		 } else if (type == java.lang.Integer.class) {
  			 if(value == null)
  				stmt.setNull(idx,Types.NUMERIC);
  			 else
  				stmt.setInt(idx,((Integer)value).intValue());
  		 } else if (type == java.lang.Double.class) {
  			 if(value == null)
  				stmt.setNull(idx,Types.NUMERIC);
  			 else
  				stmt.setDouble(idx,((Double)value).doubleValue());
           } else if (type == java.lang.Float.class) {
               if(value == null)
                  stmt.setNull(idx,Types.NUMERIC);
               else
                  stmt.setFloat(idx,((Float)value).floatValue());
  		 } else if (type == java.sql.Date.class) {
  			 if(value == null)
  				stmt.setNull(idx,Types.DATE);
  			 else
  				stmt.setDate(idx,(java.sql.Date)value);
  		 } else if (type == java.sql.Timestamp.class) {
  			 if(value == null)
  				stmt.setNull(idx,Types.TIMESTAMP);
  			 else
  				stmt.setTimestamp(idx,(java.sql.Timestamp)value);
  		 } else if (type.getName() == "int") {
  			 if(value == null)
  				stmt.setNull(idx,Types.NUMERIC);
  			 else
  				stmt.setInt(idx,((Integer)value).intValue());
  		 } else if (type.getName() == "double") {
  			 if(value == null)
  				stmt.setNull(idx,Types.NUMERIC);
  			 else
  			   stmt.setDouble(idx,((Double)value).doubleValue());
           } else if (type.getName() == "float") {
               if(value == null)
                  stmt.setNull(idx,Types.NUMERIC);
               else
                 stmt.setFloat(idx,((Float)value).floatValue());
  		 }
  	}
    
    public static PreparedStatement genUpdate( Connection con, String table, Object model, ArrayList whereList ) throws SQLException {
		return genUpdate( con, table, model, whereList, null);
	}

    public static PreparedStatement genUpdate( Connection con, String table, Object model, ArrayList whereList, String[] exceptionColumns )
    throws SQLException{
        PreparedStatement stmt = null;
        StringBuffer buffer = new StringBuffer( "UPDATE " + table + " SET " );
        Field[] fieldDefs = model.getClass().getFields();
        StmtField dbf = null;
        int size = fieldDefs.length;

            for (int i = 0; i < size; i++){
				if(isNameExist(fieldDefs[i].getName(),exceptionColumns)){
					continue;
				}


                    buffer.append( fieldDefs[i].getName() );
                    buffer.append( sql_eqpar );
                    buffer.append( sql_delim );

            }
            if (buffer.charAt( buffer.length() - 2 ) == sql_delim_char){
                buffer.replace( buffer.length() - 2, buffer.length(), sql_sp );
            }else{
                buffer.append( sql_sp );
            }
            if (whereList != null && whereList.size() > 0){
                buffer.append( " WHERE " );
                for (int i = 0; i < whereList.size(); i++){
                    dbf = (StmtField) whereList.get( i );

                        buffer.append( dbf.fieldname );
                        buffer.append( dbf.oper );
                        buffer.append( sql_par );
                        buffer.append( " AND " );

                }
                int index = buffer.toString().lastIndexOf( " AND " );
                buffer.replace( index, buffer.length(), sql_sp );
            }

            stmt = con.prepareStatement( buffer.toString() );
            int idx = fillStmtUpdate( stmt, model, exceptionColumns );
            fillWhere( stmt, model, whereList, idx);

        return stmt;
    }

    public static int fillStmtUpdate( PreparedStatement stmt, Object model,String[] exceptionColumns )
    throws SQLException{
        Field[] fieldDefs = model.getClass().getFields();
        int idx = 0;
        try{
            int size = fieldDefs.length;
            for (int i = 0; i < size; i++){

				if(isNameExist(fieldDefs[i].getName(),exceptionColumns)){
					continue;
				}

                    updateStmt( stmt, fieldDefs[i], fieldDefs[i].get( model ), ++idx );

            }
        }catch (IllegalAccessException iae){
        	iae.printStackTrace();
        }
        return idx;
    }
    
    public static void fillWhere( PreparedStatement stmt, Object model, ArrayList whereList, int idx )
    throws SQLException
    {
        Field field = null;
        StmtField psf = null;
        try{
          if (whereList != null) {
            int size = whereList.size();
            for (int i = 0; i < size; i++) {
              psf = (StmtField) whereList.get(i);
              if (model != null) {
                field = model.getClass().getField(psf.fieldname);
                updateStmt(stmt, field, psf.value, ++idx);
              }
              else {
                updateStmt(stmt, psf.value.getClass(), psf.value, ++idx);
              }
            }
          }
        }catch (NoSuchFieldException nsfe){
        	nsfe.printStackTrace();
        }
    }
    
	private static boolean isNameExist(String name, String values[]) {
		if(values!=null) {
			for (int index = 0; index < values.length; index++) {
				if(name.equals(values[index])) {
					return true;
				}
			}
		}
		return false;
	}

}