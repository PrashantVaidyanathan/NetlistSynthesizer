/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package netsynth;

import java.io.Serializable;

/**
 *
 * @author prashantvaidyanathan
 */
public class Wire implements Serializable {
   
    
     String name; 
     public enum WireType{
         input,
         output,
         GND,
         Source,
         connector;
     }
     public WireType wtype;
     int wirestage;
     float dagstage;
     public Wire()
     {
         name = "";
         wtype = WireType.connector;  
     }
     public Wire(String wirename)
     {
        this.name = wirename;
     }
     public Wire(String wirename,WireType wType)
     {
         if(wType == WireType.input)
         {
            this.wirestage = 0;
            this.dagstage = 0;
         }
         this.name = wirename;
         this.wtype = wType;

     }
     
     
}
