/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mvcmysql.model;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
/**
 *
 * @author profe
 */
public class Model {
    
    private static ObjectContainer connexio=null;  
    private static ObjectSet resultSet = null;
        
    public Model() {
        File f=null;
        try{
            f = new File("database.yap");
            crearConnexio(f.getCanonicalPath());
        } catch (Exception ex) {
            System.err.println("No s'ha pogut establir la connexió a la BD...");
            System.exit(0);
        }
    }

    public void finalize() throws Throwable {
        if(connexio!=null) connexio.close();
        System.out.println("Tancant la connexió a la BD...");
        super.finalize(); //To change body of generated methods, choose Tools | Templates.
    }


    private void crearConnexio(String fitxer){
        try {
                connexio = Db4oEmbedded.openFile(fitxer);
                System.out.println("Connectant a la BD...");
        } catch (Exception ex) {
            System.err.println("No s'ha pogut establir la connexió a la BD...");
            System.exit(0);
        }

    }
    
    public void insertarActor(String first_name, String last_name){
        int id=1;
        ObjectSet result=connexio.query(
                new Predicate<TaulaActors>(){
                    @Override
                     public boolean match(TaulaActors et) {
                        return true; //To change body of generated methods, choose Tools | Templates.
                    }
                }
                , ((Comparator)new TaulaActors(0,null,null)).reversed());
        if(result.size()!=0) id=((TaulaActors)result.next()).get1_actor_id()+1;
        try{
            connexio.store(new TaulaActors(id, first_name, last_name));
        } catch (Exception ex) {
            System.err.println("Error a l'insertar l'actor!!");
        }  
    
    }
    
    public void borrarActor(int actor_id){
            
        TaulaActors exemple=new TaulaActors(actor_id, null, null);
        ObjectSet result=connexio.queryByExample(exemple);
        if(result.size()==1) {
            connexio.delete(result.next());
            System.err.println("Actor borrat correctament!!");
        } else {
            System.err.println("Error al borrar l'actor!!");
        }  
    
    }
    
    public void modificarActor(int actor_id, String first_name, String last_name){

        TaulaActors exemple=new TaulaActors(actor_id, null, null);
        ObjectSet<TaulaActors> result=connexio.queryByExample(exemple);
        if(result.size()==1) {
            exemple=result.next();
            exemple.set2_first_name(first_name);
            exemple.set3_last_name(last_name);
            connexio.store(exemple);
            System.err.println("Actor modificat correctament!!");
        } else {
            System.err.println("Error al modificar l'actor!!");
        }  

//        String sql = "UPDATE actor SET first_name=?, last_name=? "+
//                "WHERE actor_id=?";
//        try(PreparedStatement sentenciaPr=connexio.prepareStatement(sql)) {
//            sentenciaPr.setString(1, first_name);
//            sentenciaPr.setString(2, last_name);
//            sentenciaPr.setInt(3, actor_id);
//            sentenciaPr.executeUpdate();
//        } catch (SQLException ex) {
//            System.err.println("Error al modificar l'actor!!");
//        }  
//    
    }
    
    public ArrayList<TaulaActors> llistarActors(){
        ArrayList llista=new ArrayList();
        ObjectSet result=connexio.queryByExample(TaulaActors.class);
        
            
        if(result.size()!=0){
            
                while(result.hasNext()){
                    llista.add(result.next());                
                }
            
            
            
        } else {
            System.err.println("Error al llistar els actors!!");
        }  
        return llista;    
    }

    public static ObjectContainer getConnexio() {
        return connexio;
    }
    
    
    
}
