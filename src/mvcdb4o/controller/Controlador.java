/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mvcdb4o.controller;

import com.db4o.ObjectContainer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import mvcdb4o.model.Model;
import mvcdb4o.model.TaulaActors;
import mvcdb4o.view.VistaActors;

/**
 *
 * @author profe
 */
public class Controlador {

    private Model odb;
    private VistaActors vista;
    private int filasel = -1;
    private String nom = "";
    private String cognom = "";
    private int id = -1;

    public Controlador(Model odb, VistaActors jf) {
        this.odb = odb;
        this.vista = jf;
        ArrayList<TaulaActors> llista=odb.llistarActors();
        carregaTaula(llista, TaulaActors.class);
        vista.getjComboBox1().setModel(new DefaultComboBoxModel(llista.toArray()));
        vista.getjButton1().setEnabled(false);
        vista.getjButton2().setEnabled(!llista.isEmpty());
        borrarCamps();
        vista.setVisible(true);
        control();
    }

    public void borrarCamps() {
        //Posem en blanc el nom i cognom de l'actor
        vista.getjTextField1().setText("");
        vista.getjTextField2().setText("");
        vista.getjList1().setModel(new DefaultListModel());
        nom = cognom = "";
    }

    public void carregaTaula(ArrayList resultSet, Class<?> classe) {
        // TODO add your handling code here:
        //Quan tornem a carregar la taula perdem la selecció que haviem fet i posem filasel a -1
        filasel = -1;

        Vector columnNames = new Vector();
        Vector data = new Vector();
        ModelCanvisBD model;

        //Anotem el nº de camps de la classe
        Field[] camps = classe.getDeclaredFields();
        //Ordenem els camps alfabèticament
        Arrays.sort(camps, new OrdenarCampClasseAlfabeticament());
        int ncamps = camps.length;
        //Recorrem els camps de la classe i posem els seus noms com a columnes de la taula
        //Com hem hagut de posar _numero_ davant el nom dels camps, mostrem el nom a partir de la 4ª lletra 
        for (Field f : camps) {
            columnNames.addElement(f.getName().substring(3));
        }
        //Afegixo al model de la taula una columna on guardaré l'objecte mostrat a cada fila (amago la columna al final per a que no aparegue a la vista)
        columnNames.addElement("objecte");
        //Si hi ha algun element a l'arraylist omplim la taula
        if (resultSet.size() != 0) {

            //Guardem els descriptors de mètode que ens interessen (els getters), més una columna per guardar l'objecte sencer
            Vector<Method> methods = new Vector(ncamps + 1);
            try {

                PropertyDescriptor[] descriptors = Introspector.getBeanInfo(classe).getPropertyDescriptors();
                Arrays.sort(descriptors, new OrdenarMetodeClasseAlfabeticament());
                for (PropertyDescriptor pD : descriptors) {
                    Method m = pD.getReadMethod();
                    if (m != null & !m.getName().equals("getClass")) {
                        methods.addElement(m);
                    }
                }

            } catch (IntrospectionException ex) {
                Logger.getLogger(VistaActors.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (Object m : resultSet) {
                Vector row = new Vector(ncamps + 1);

                for (Method mD : methods) {
                    try {
                        row.addElement(mD.invoke(m));
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(VistaActors.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(VistaActors.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InvocationTargetException ex) {
                        Logger.getLogger(VistaActors.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                //Aquí guardo l'objecte sencer a la darrera columna
                row.addElement(m);
                //Finalment afegixo la fila a les dades
                data.addElement(row);
            }
        }

        //Utilitzem el model que permet actualitzar la BD des de la taula
        model = new ModelCanvisBD(data, columnNames, Model.getConnexio(), columnNames.size() - 1);
        vista.getjTable2().setModel(model);

        //Amago la darrera columna per a que no aparegue a la vista
        TableColumnModel tcm = vista.getjTable2().getColumnModel();
        tcm.removeColumn(tcm.getColumn(tcm.getColumnCount() - 1));

        //Fixo l'amplada de les columnes que sí es mostren
        TableColumn column;
        for (int i = 0; i < vista.getjTable2().getColumnCount(); i++) {
            column = vista.getjTable2().getColumnModel().getColumn(i);
            column.setMaxWidth(250);
        }

    }

    // Classe "niada" (nested class, clase anidada) usada per ordenar els camps de les classes alfabèticament
    public static class OrdenarCampClasseAlfabeticament implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            return (int) (((Field) o1).getName().compareToIgnoreCase(((Field) o2).getName()));
        }
    }

    // Classe "niada" (nested class, clase anidada) usada per ordenar els mètodes de les classes alfabèticament
    public static class OrdenarMetodeClasseAlfabeticament implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {

            Method mo1 = ((PropertyDescriptor) o1).getReadMethod();
            Method mo2 = ((PropertyDescriptor) o2).getReadMethod();

            if (mo1 != null && mo2 != null) {
                return (int) mo1.getName().compareToIgnoreCase(mo2.getName());
            }
            if (mo1 == null) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    public static boolean hasElement(Object searched, JList list) {
      for (int a = 0; a < list.getModel().getSize(); a++) {
         Object element = list.getModel().getElementAt(a);
         if (element.equals(searched))  {
            return true;
         }
      }
      return false;
    }
    
    public void control() {

        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getSource().equals(vista.getjButton6())) {
                    if (filasel != -1) {
                        odb.borrarActor(id);
                        borrarCamps();
                        ArrayList<TaulaActors> llista=odb.llistarActors();
                        carregaTaula(llista, TaulaActors.class);
                        vista.getjComboBox1().setModel(new DefaultComboBoxModel(llista.toArray()));
                        vista.getjButton2().setEnabled(!llista.isEmpty());
                    } else {
                        JOptionPane.showMessageDialog(null, "Per borrar un actor primer l'has de seleccionar!!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    if (actionEvent.getSource().equals(vista.getjButton5())) {
                        if (!nom.equals("") || !cognom.equals("")) {
                            ArrayList<TaulaActors> afins=new ArrayList<>();
                            if(vista.getjList1().getModel().getSize()!=0){
                                DefaultListModel lm=(DefaultListModel)vista.getjList1().getModel();
                                for (int i = 0; i < lm.getSize(); i++) {
                                    TaulaActors element = (TaulaActors)lm.getElementAt(i);
                                    afins.add(element);    
                                }
                            }
                            
                            odb.insertarActor(nom, cognom, afins);
                            borrarCamps();
                            ArrayList<TaulaActors> llista=odb.llistarActors();
                            carregaTaula(llista, TaulaActors.class);
                            vista.getjComboBox1().setModel(new DefaultComboBoxModel(llista.toArray()));
                            //Si acabo d'insertar un actor puc activar el botó d'afegir actors afins
                            vista.getjButton2().setEnabled(true);
                        } else {
                            JOptionPane.showMessageDialog(null, "No pots introduir un actor sense nom ni cognom!!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        if (actionEvent.getSource().equals(vista.getjButton7())) {
                            if (filasel != -1 && (!nom.equals("") || !cognom.equals(""))) {
                                ArrayList<TaulaActors> afins=new ArrayList<>();
                                if(vista.getjList1().getModel().getSize()!=0){
                                    DefaultListModel lm=(DefaultListModel)vista.getjList1().getModel();
                                    for (int i = 0; i < lm.getSize(); i++) {
                                        TaulaActors element = (TaulaActors)lm.getElementAt(i);
                                        afins.add(element);    
                                    }
                                }
                                odb.modificarActor(id, nom, cognom, afins);
                                borrarCamps();
                                ArrayList<TaulaActors> llista=odb.llistarActors();
                                carregaTaula(llista, TaulaActors.class);
                                vista.getjComboBox1().setModel(new DefaultComboBoxModel(llista.toArray()));
                            } else {
                                JOptionPane.showMessageDialog(null, "Per modificar un actor primer l'has de seleccionar i posar algun valor al nom i/o cognoms!!", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            //Botó d'afegir actor a la llista d'afins
                            if (actionEvent.getSource().equals(vista.getjButton2())) {
                                if (!hasElement(vista.getjComboBox1().getSelectedItem(), vista.getjList1())) {
                                    DefaultListModel lm=(DefaultListModel)vista.getjList1().getModel();
                                    lm.addElement(vista.getjComboBox1().getSelectedItem());
                                    vista.getjList1().setModel(lm);
                                } else {
                                    JOptionPane.showMessageDialog(null, "L'actor ja està a la llista d'afins!!", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            } else {
                                //Botó d'eliminar actors de la llista d'afins
                                if (actionEvent.getSource().equals(vista.getjButton1())) {
                                    DefaultListModel lm=(DefaultListModel)vista.getjList1().getModel();
                                    lm.removeElement(lm.getElementAt(vista.getjList1().getSelectedIndex()));
                                    vista.getjList1().setModel(lm);
                                    vista.getjButton1().setEnabled(false);
                                }
                                else
                                    //Botó de sortir de l'aplicació
                                    if(actionEvent.getSource().equals(vista.getjButton4())){
                                    try {
                                        odb.finalize();
                                    } catch (Throwable ex) {
                                        System.out.println("Error tancant la base de dades!!");
                                    }
                                    System.exit(0);
                                }
                            }
                        }
                    }
                }
            }
        };
        vista.getjButton1().addActionListener(actionListener);
        vista.getjButton2().addActionListener(actionListener);
        vista.getjButton4().addActionListener(actionListener);
        vista.getjButton5().addActionListener(actionListener);
        vista.getjButton6().addActionListener(actionListener);
        vista.getjButton7().addActionListener(actionListener);

        MouseAdapter mouseAdapterJTable = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e); //To change body of generated methods, choose Tools | Templates.
                try {
                    filasel = vista.getjTable2().getSelectedRow();
                    if (filasel != -1) {
                        //if(jTable2.getValueAt(filasel, 0) instanceof String ) 
                        id = Integer.parseInt(vista.getjTable2().getValueAt(filasel, 0).toString());
                        //else id=(int)jTable2.getValueAt(filasel, 0);
                        nom = (String) vista.getjTable2().getValueAt(filasel, 1);
                        vista.getjTextField1().setText(nom);
                        cognom = (String) vista.getjTable2().getValueAt(filasel, 2);
                        vista.getjTextField2().setText(cognom);
                        
                        //Omplim la llista d'actors afins
                        vista.getjList1().setModel(new DefaultListModel());
                        DefaultListModel lm=(DefaultListModel)vista.getjList1().getModel();
                        for (TaulaActors t:(ArrayList<TaulaActors>)vista.getjTable2().getValueAt(filasel, 3)) {
                                    lm.addElement(t);    
                        }
                    } else {
                        borrarCamps();
                    }
                } catch (NumberFormatException ex) {
                }
            }

        };
        vista.getjTable2().addMouseListener(mouseAdapterJTable);

        MouseAdapter mouseAdapterJList = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e); //To change body of generated methods, choose Tools | Templates.
                int filasel =vista.getjList1().getSelectedIndex();
                vista.getjButton1().setEnabled(filasel != -1);
            }

        };
        vista.getjList1().addMouseListener(mouseAdapterJList);

        FocusAdapter focusAdapter = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e); //To change body of generated methods, choose Tools | Templates.
                if (e.getSource().equals(vista.getjTextField1())) {
                    nom = vista.getjTextField1().getText().trim();
                }
                if (e.getSource().equals(vista.getjTextField2())) {
                    cognom = vista.getjTextField2().getText().trim();
                }
            }

        };

        vista.getjTextField1().addFocusListener(focusAdapter);
        vista.getjTextField2().addFocusListener(focusAdapter);

        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e); //To change body of generated methods, choose Tools | Templates.
                if (e.getKeyChar() == '\n') {

                    try {
                        filasel = vista.getjTable2().getSelectedRow();
                        if (filasel != -1) {
                            //if(jTable2.getValueAt(filasel, 0) instanceof String ) 
                            id = Integer.parseInt(vista.getjTable2().getValueAt(filasel, 0).toString());
                            //else id=(int)jTable2.getValueAt(filasel, 0);
                            nom = (String) vista.getjTable2().getValueAt(filasel, 1);
                            vista.getjTextField1().setText(nom);
                            cognom = (String) vista.getjTable2().getValueAt(filasel, 2);
                            vista.getjTextField2().setText(cognom);
                        } else {
                            borrarCamps();
                        }
                    } catch (NumberFormatException ex) {
                    }
                }
            }
        };

        vista.getjTable2().addKeyListener(keyAdapter);

        WindowAdapter windowAdapter = new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent evt) {
                //Mateix codi que quan paretem el botó de sortir del programa
                try {
                    odb.finalize();
                } catch (Throwable ex) {
                    System.out.println("Error tancant la base de dades!!");
                }
                System.exit(0);
            }
        };

        vista.addWindowListener(windowAdapter);
    }

}

//Classse filla de DefaultTableModel que conté un Listener per automàticament actualitzar a la BD els canvis fets a una jTable
class ModelCanvisBD extends DefaultTableModel {

    private ObjectContainer resultSet = null;
    private int columnaID;

    //El paràmetre colID indica quina columna del DefaultTableModel conté l'objecte mostrat a la fila de la taula
    public ModelCanvisBD(Vector data, Vector columnNames, ObjectContainer rs, int colID) {
        super(data, columnNames);
        resultSet = rs;
        columnaID = colID;
        this.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                TableModel model = (TableModel) e.getSource();
                Object data = model.getValueAt(row, column);
                
                //Guardem el descriptor del mètode setter que ens interessa (el de la casella que modifiquem)
                Method method = null;
                try {

                    PropertyDescriptor[] descriptors = Introspector.getBeanInfo(model.getValueAt(row, columnaID).getClass()).getPropertyDescriptors();
                    Arrays.sort(descriptors, new Controlador.OrdenarMetodeClasseAlfabeticament());
                    int cont = 0;
                    for (PropertyDescriptor pD : descriptors) {
                        Method m = pD.getWriteMethod();
                        //Només agafem el setter de la columna modificada
                        if (m != null && cont == column) {
                            method = m;
                        }
                        cont++;
                    }

                } catch (IntrospectionException ex) {
                    Logger.getLogger(VistaActors.class.getName()).log(Level.SEVERE, null, ex);
                }
                //La primera instrucció del try actualitza les dades modificades de l'objecte (és com aplicar-li el setter corresponent)
                //La segona guarda l'objecte modificat a la BD
                try {
                    method.invoke(model.getValueAt(row, columnaID), new Object[]{data});
                    resultSet.store(model.getValueAt(row, columnaID));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Canvi de dada incorrecte!!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        );

    }

    @Override
    public boolean isCellEditable(int row, int column) {
        //permitim editar des de la taula totes les columnes excepte la que conté una col·lecció
        return column!=3; //To change body of generated methods, choose Tools | Templates.
    }
}
