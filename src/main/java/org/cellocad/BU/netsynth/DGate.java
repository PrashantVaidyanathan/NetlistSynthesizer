/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cellocad.BU.netsynth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author prashantvaidyanathan
 */

public class DGate implements Serializable{
    public String gname;
    
    public DGateType gtype;
    public List<DWire> input;
    public DWire output;
    public int gatestage;
    public int gindex;
    
    public DGate()
    {      
         input = new ArrayList<DWire>();
         output = new DWire();
    }
    public DGate(DGateType gType, List<DWire> inputWires, DWire outputWire)
    {
        input = new ArrayList<DWire>();
        this.input.addAll(inputWires);
        gtype = gType;
        output = outputWire;        
        if(!input.isEmpty())
        {
            int maxStage = input.get(0).wirestage;
            for(DWire w:input)
            {
                if(maxStage<w.wirestage)
                    maxStage = w.wirestage;
            }
            maxStage++;
            output.wirestage = maxStage;
            gatestage = maxStage;
        }
        
    }
    public DGate(DGateType gType, String gName, List<DWire> inputWires, DWire outputWire)
    {
        input = new ArrayList<DWire>();
        input.addAll(inputWires);
        gtype = gType;
        output = outputWire;        
        gname = gName;
        if(!input.isEmpty())
        {
            int maxStage = input.get(0).wirestage;
            for(DWire w:input)
            {
             
                if(maxStage<w.wirestage)
                    maxStage = w.wirestage;
            }
            maxStage++;
            output.wirestage = maxStage;
            gatestage = maxStage;
        }
        
    }
    
    public DGate(DGate gate)
    {
        gtype = gate.gtype;
        gname = gate.gname;
        gindex = gate.gindex;
        input = new ArrayList<DWire>();
        for(DWire x:gate.input)
        {
            input.add(new DWire(x));
        }
        output = new DWire(gate.output);
    }
    
    public void calculateStage()
    {
        if(!this.input.isEmpty())
        {
            int maxStage = this.input.get(0).wirestage;
            for(DWire w:this.input)
            {
                if(maxStage<w.wirestage)
                    maxStage = w.wirestage;
            }
            maxStage++;
            this.output.wirestage = maxStage;
            this.gatestage = maxStage;
        }
        
    }
}
