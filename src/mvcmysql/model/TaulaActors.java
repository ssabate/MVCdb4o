/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mvcmysql.model;

import java.util.Comparator;

/**
 *
 * @author profe
 */
public class TaulaActors implements Comparator<TaulaActors> {
    
    private int _1_actor_id;
    private String _2_first_name;
    private String _3_last_name;

    public TaulaActors(int actor_id, String first_name, String last_name) {
        this._1_actor_id = actor_id;
        this._2_first_name = first_name;
        this._3_last_name = last_name;
    }

    

    public TaulaActors(String first_name, String last_name) {
        this._2_first_name = first_name;
        this._3_last_name = last_name;
    }

    public String get3_last_name() {
        return _3_last_name;
    }

    public void set3_last_name(String _3_last_name) {
        this._3_last_name = _3_last_name;
    }
 
    public String get2_first_name() {
        return _2_first_name;
    }

    public void set2_first_name(String _2_first_name) {
        this._2_first_name = _2_first_name;
    }

    @Override
    public int compare(TaulaActors o1, TaulaActors o2) {
        return o1.get1_actor_id()-o2.get1_actor_id(); //To change body of generated methods, choose Tools | Templates.
    }

    public int get1_actor_id() {
        return _1_actor_id;
    }

    public void set1_actor_id(int _1_actor_id) {
        this._1_actor_id = _1_actor_id;
    }
    
}
