package bd;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private  String puerto="3306";
    private  String nomservidor="localhost";
    private  String db="huellas";
    private  String user="root";
    private  String pass="";
    Connection conn=null;

    public Connection conectar(){
        try{
            String ruta="jdbc:mysql://";
            String servidor=nomservidor+":"+puerto+"/";
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(ruta+servidor+db,user,pass);

            if (conn!=null){
                System.out.println("Conección a base de datos listo...");
            }
            else {
                throw new SQLException();
            }
        }catch(SQLException e){
            JOptionPane.showMessageDialog(null, e.getMessage());
        }catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Se produjo el siguiente error: "+e.getMessage());
        }catch (NullPointerException e){
            JOptionPane.showMessageDialog(null, "Se produjo el siguiente error: "+e.getMessage());
        }finally{
            return conn;
        }
    }
    public void desconectar(){
        conn = null;
        System.out.println("Desconexion a base de datos listo...");
    }
}
