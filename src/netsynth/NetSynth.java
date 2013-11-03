/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package netsynth;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import netsynth.Gate.GateType;
import netsynth.Wire.WireType;


/**
 *
 * @author prashantvaidyanathan
 */
public class NetSynth {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Global.wirecount = 0;
        Global.espinp =0;
        Global.espout =0;
        //testnetlistmodule();
        testEspresso();
        
            
        
    }
    
    
    public static void testEspresso()
    {
        
        List<String> espressoOut = new ArrayList<String>();
        espressoOut = runEspresso();
        
        List<Gate> SOPgates = new ArrayList<Gate>();
        SOPgates = parseEspressoOutput(espressoOut);
        for(Gate g:SOPgates)
        {
            String gateString = netlist(g);
            System.out.println(gateString);
        }
    }
    
    
   
    
    public static List<String> runEspresso() {
    
        
        List<String> espressoOutput = new ArrayList<String>();
        String x = System.getProperty("os.name");
        StringBuilder commandBuilder = null;
        //if("Linux".equals(x))
        //{
            commandBuilder = new StringBuilder("./src/resources/espresso.linux src/resources/A0.txt");
        //}
        
        //System.out.println(commandBuilder);
        String command = commandBuilder.toString();
        //System.out.println("So this is what is happening: "+command);
        
        Runtime runtime = Runtime.getRuntime();
        Process proc = null;
        try {
            proc = runtime.exec(command);
        } catch (IOException ex) {
            Logger.getLogger(NetSynth.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            String filestring = "";
            filestring += "src/resources/write";
            filestring += Global.espout;
            filestring += ".txt";
            File fbool = new File(filestring);
            Writer output = new BufferedWriter(new FileWriter(fbool));
            InputStream in = proc.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = null;
            
            while((line = br.readLine())!= null)
            {  
                espressoOutput.add(line);
                line += "\n";
                output.write(line);
            }
            output.close();
            fbool.deleteOnExit();
        } catch (IOException ex) {
            Logger.getLogger(NetSynth.class.getName()).log(Level.SEVERE, null, ex);
        }
         
        return espressoOutput;
    }
    
    
    public static void testnetlistmodule()
    {
        Gate and = new Gate();
        Wire w1 = new Wire("A",WireType.input);
        Wire w2 = new Wire("B",WireType.input);
        Wire outp = new Wire("outP",WireType.output);
        List<Wire> inputWires = new ArrayList<Wire>();
        inputWires.add(w1);
        inputWires.add(w2);
        
        GateType testgateType;
        testgateType = GateType.XNOR2;
        Gate gtest = new Gate(testgateType,inputWires,outp);
        
        List<Gate> test = new ArrayList<Gate>();
        test = GatetoNORNOT(gtest);

        for(Gate gout:test)
        {
            String netbuilder = "";
            netbuilder = netlist(gout);
            System.out.println(netbuilder);
        
        }
         
        
    }
    
    
    
    public static List<Gate> parseEspressoOutput(List<String> espinp)
    {
        List<Gate> sopexp = new ArrayList<Gate>();
        String inpNames = (espinp.get(2).substring(5));
        List<Wire> wireInputs = new ArrayList<Wire>();
        List<Wire> wireOutputs = new ArrayList<Wire>();
        List<Gate> inpInv = new ArrayList<Gate>();
        
        for(String splitInp:inpNames.split(" "))
        {
            wireInputs.add(new Wire(splitInp,WireType.input));
        }
        inpInv = notGates(wireInputs);
        
        String outNames = (espinp.get(3).substring(4));
        for(String splitInp:outNames.split(" "))
        {
            wireOutputs.add(new Wire(splitInp,WireType.output));
        }
        int numberOfMinterms = Integer.parseInt(espinp.get(4).substring(3));
        List<Wire> minTemp = new ArrayList<Wire>();
        List<Wire> orWires = new ArrayList<Wire>();
        
        List<Gate> prodGates;
        for(int i=5;i<(5+numberOfMinterms);i++)
        {
            
            //List<Wire> minTemp = new ArrayList<Wire>();
            String minT = espinp.get(i).substring(0, (wireInputs.size()));
            prodGates = new ArrayList<Gate>();
            minTemp = new ArrayList<Wire>();
            
            for(int j=0;j<wireInputs.size();j++)
            {
                
                if(minT.charAt(j)=='-')
                    continue;
                else if(minT.charAt(j) == '0')
                {
                    if(!sopexp.contains(inpInv.get(j)))
                    {
                        sopexp.add(inpInv.get(j));
                        
                        //System.out.println(i);
                    }
                    minTemp.add(inpInv.get(j).output);
                }
                else if(minT.charAt(j) == '1')
                    minTemp.add(wireInputs.get(j));
                
            }
            prodGates = AndORGates(minTemp,GateType.AND2);
         
            orWires.add(prodGates.get(prodGates.size()-1).output);
            sopexp.addAll(prodGates);
            //System.out.println(minT);
        }
        prodGates = new ArrayList<Gate>();
        prodGates = AndORGates(orWires,GateType.OR2);
        sopexp.addAll(prodGates);
        //System.out.println(netlist(sopexp.get(0)));
        return sopexp;
    }
    
    public static List<Gate> notGates(List<Wire> andWires)
    {
        List<Gate> notInp = new ArrayList<Gate>();
        for(Wire xWire:andWires)
        {
            String Wirename = "Wire" + Global.wirecount++;
            Wire aout = new Wire(Wirename);
            List<Wire> inpNot = new ArrayList<Wire>();
            inpNot.add(xWire);
            notInp.add(new Gate(GateType.NOT,inpNot,aout));
        }
        return notInp;
    }
    
    public static List<Gate> AndORGates(List<Wire> andWires,GateType gOrAnd)
    {
        if (andWires.isEmpty())
        {    return null;}
        List<Gate> minterm = new ArrayList<Gate>();
        List<Wire> nextLevelWires = new ArrayList<Wire>();
        List<Wire> temp = new ArrayList<Wire>();
        
        int wireCount,indx;
        nextLevelWires.addAll(andWires);
        wireCount = andWires.size();
        
        while(wireCount > 1)
        {
            temp = new ArrayList<Wire>();
            temp.addAll(nextLevelWires);
            nextLevelWires = new ArrayList<Wire>();
            indx = 0;
            while((indx+2)<= (wireCount))
            {
                
                List<Wire> ainp = new ArrayList<Wire>();
                ainp.add(temp.get(indx));
                ainp.add(temp.get(indx+1));
                String Wirename = "Wire" + Global.wirecount++;
                
                Wire aout = new Wire(Wirename);
                nextLevelWires.add(aout);
                Gate andG = new Gate(gOrAnd,ainp,aout);
                minterm.add(andG);
                indx+=2;
            
            }
            if(temp.size() %2 != 0)
            {
                nextLevelWires.add(temp.get(wireCount-1));
            }
            wireCount = nextLevelWires.size();
            if(wireCount == 2)
            {
                List<Wire> ainp = new ArrayList<Wire>();
                ainp.add(nextLevelWires.get(0));
                ainp.add(nextLevelWires.get(1));
                String Wirename = "Wire" + Global.wirecount++;
                Wire aout = new Wire(Wirename);
                Gate andG = new Gate(gOrAnd,ainp,aout);
                minterm.add(andG);
                break;
            }
        }
        /*for(Gate gmin:minterm)
        {
            String x = netlist(gmin);
            System.out.println(x);
            //System.out.println(gmin.gatestage);
        }*/
        return minterm;
    }
    //public static List<Gate> createandgates(List<String> gatenames)
    //{
        
    //}
    
    public static List<Gate> GatetoNOR(Gate g)
    {
       List<Gate> nor_eq = new ArrayList<Gate>();
       
       if(g.gtype == GateType.NOT)
       {
           Gate nor1 = new Gate();
           nor1.gtype = GateType.NOR2;
           
           nor1.input.add(g.input.get(0));
           nor1.input.add(g.input.get(0));
           
           nor1.output = g.output;
           nor1.calculateStage();
           nor_eq.add(nor1);
           
       }
       
       else if(g.gtype == GateType.AND2)
       {
           Gate nor1 = new Gate();
           nor1.gtype = GateType.NOR2;
           nor1.input.add(g.input.get(0));
           nor1.input.add(g.input.get(0));
           Wire outp1 = new Wire();
           outp1.name = "Wire" + Global.wirecount++;
           nor1.output = outp1;
           
           Gate nor2 = new Gate();
           nor2.gtype = GateType.NOR2;
           nor2.input.add(g.input.get(1));
           nor2.input.add(g.input.get(1));
           Wire outp2 = new Wire();
           outp2.name = "Wire" + Global.wirecount++;
           nor2.output = outp2;
           
           Gate nor3 = new Gate();
           nor3.gtype = GateType.NOR2;
           nor3.input.add(outp1);
           nor3.input.add(outp2);
           nor3.output = g.output;
           
           nor1.calculateStage();
           nor2.calculateStage();
           nor3.calculateStage();
           
           nor_eq.add(nor1);
           nor_eq.add(nor2);
           nor_eq.add(nor3);
       
       }
       
       
       else if(g.gtype == GateType.NAND2)
       {
           Gate nor1 = new Gate();
           nor1.gtype = GateType.NOR2;
           nor1.input.add(g.input.get(0));
           nor1.input.add(g.input.get(0));
           Wire outp1 = new Wire();
           outp1.name = "Wire" + Global.wirecount++;
           nor1.output = outp1;
           
           Gate nor2 = new Gate();
           nor2.gtype = GateType.NOR2;
           nor2.input.add(g.input.get(1));
           nor2.input.add(g.input.get(1));
           Wire outp2 = new Wire();
           outp2.name = "Wire" + Global.wirecount++;
           nor2.output = outp2;
           
           Gate nor3 = new Gate();
           nor3.gtype = GateType.NOR2;
           nor3.input.add(outp1);
           nor3.input.add(outp2);
           Wire outp3 = new Wire();
           outp3.name = "Wire" + Global.wirecount++;
           nor3.output = outp3;
           
           Gate nor4 = new Gate();
           nor4.gtype = GateType.NOR2;
           nor4.input.add(outp3);
           nor4.input.add(outp3);
           nor4.output = g.output;
           
           nor1.calculateStage();
           nor2.calculateStage();
           nor3.calculateStage();
           nor4.calculateStage();
           
           nor_eq.add(nor1);
           nor_eq.add(nor2);
           nor_eq.add(nor3);
           nor_eq.add(nor4);
           
       }
       
       
       else if(g.gtype == GateType.OR2)
       {
           Gate nor1  = new Gate();
           nor1.gtype = GateType.NOR2;
           nor1.input.add(g.input.get(0));
           nor1.input.add(g.input.get(1));
           Wire outp1 = new Wire();
           outp1.name = "Wire" + Global.wirecount++;
           nor1.output = outp1;
           
           Gate nor2 = new Gate();
           nor2.gtype = GateType.NOR2;
           nor2.input.add(outp1);
           nor2.input.add(outp1);
           nor2.output = g.output;
          
           nor1.calculateStage();
           nor2.calculateStage();
           
           nor_eq.add(nor1);
           nor_eq.add(nor2);
           
       }
       
       else if(g.gtype == GateType.NOR2)
       {
           g.calculateStage();
           nor_eq.add(g);
       }
       
       else if(g.gtype == GateType.XOR2)
       {
           Gate nor1 = new Gate();
           nor1.gtype = GateType.NOR2;
           nor1.input.add(g.input.get(0));
           nor1.input.add(g.input.get(0));
           Wire outp1 = new Wire();
           outp1.name = "Wire" + Global.wirecount++;
           nor1.output = outp1;
           
           Gate nor2 = new Gate();
           nor2.gtype = GateType.NOR2;
           nor2.input.add(g.input.get(1));
           nor2.input.add(g.input.get(1));
           Wire outp2 = new Wire();
           outp2.name = "Wire" + Global.wirecount++;
           nor2.output = outp2;
           
           Gate nor3 = new Gate();
           nor3.gtype = GateType.NOR2;
           nor3.input.add(g.input.get(0));
           nor3.input.add(g.input.get(1));
           Wire outp3 = new Wire();
           outp3.name = "Wire" + Global.wirecount++;
           nor3.output = outp3;
           
           Gate nor4 = new Gate();
           nor4.gtype = GateType.NOR2;
           nor4.input.add(outp1);
           nor4.input.add(outp2);
           Wire outp4 = new Wire();
           outp4.name = "Wire" + Global.wirecount++;
           nor4.output = outp4;
           
           Gate nor5 = new Gate();
           nor5.gtype = GateType.NOR2;
           nor5.input.add(outp3);
           nor5.input.add(outp4);
           nor5.output = g.output;
           
           nor1.calculateStage();
           nor2.calculateStage();
           nor3.calculateStage();
           nor4.calculateStage();
           nor5.calculateStage();
           
           nor_eq.add(nor1);
           nor_eq.add(nor2);
           nor_eq.add(nor3);
           nor_eq.add(nor4);
           nor_eq.add(nor5);
                                                       
       }
       else if(g.gtype == GateType.XNOR2)
       {
           Gate nor1 = new Gate();
           nor1.gtype = GateType.NOR2;
           nor1.input.add(g.input.get(0));
           nor1.input.add(g.input.get(0));
           Wire outp1 = new Wire();
           outp1.name = "Wire" + Global.wirecount++;
           nor1.output = outp1;
           
           Gate nor2 = new Gate();
           nor2.gtype = GateType.NOR2;
           nor2.input.add(g.input.get(1));
           nor2.input.add(g.input.get(1));
           Wire outp2 = new Wire();
           outp2.name = "Wire" + Global.wirecount++;
           nor2.output = outp2;
           
           Gate nor3 = new Gate();
           nor3.gtype = GateType.NOR2;
           nor3.input.add(outp1);
           nor3.input.add(g.input.get(1));
           Wire outp3 = new Wire();
           outp3.name = "Wire" + Global.wirecount++;
           nor3.output = outp3;
           
           Gate nor4 = new Gate();
           nor4.gtype = GateType.NOR2;
           nor4.input.add(g.input.get(0));
           nor4.input.add(outp2);
           Wire outp4 = new Wire();
           outp4.name = "Wire" + Global.wirecount++;
           nor4.output = outp4;
           
           Gate nor5 = new Gate();
           nor5.gtype = GateType.NOR2;
           nor5.input.add(outp3);
           nor5.input.add(outp4);
           nor5.output = g.output;

           nor1.calculateStage();
           nor2.calculateStage();
           nor3.calculateStage();
           nor4.calculateStage();
           nor5.calculateStage();
           
           nor_eq.add(nor1);
           nor_eq.add(nor2);
           nor_eq.add(nor3);
           nor_eq.add(nor4);
           nor_eq.add(nor5);
       }
       
       return nor_eq;
    }  
     
    public static List<Gate> GatetoNORNOT(Gate g)
    {
       List<Gate> nor_eq = new ArrayList<Gate>();
       
       if(g.gtype == GateType.NOT)
       {
           g.calculateStage();
           nor_eq.add(g);
       }
       
       else if(g.gtype == GateType.AND2)
       {
           Gate not1 = new Gate();
           not1.gtype = GateType.NOT;
           not1.input.add(g.input.get(0));
           Wire outp1 = new Wire();
           outp1.name = "Wire" + Global.wirecount++;
           not1.output = outp1;
           
           Gate not2 = new Gate();
           not2.gtype = GateType.NOT;
           not2.input.add(g.input.get(1));
           Wire outp2 = new Wire();
           outp2.name = "Wire" + Global.wirecount++;
           not2.output = outp2;
           
           Gate nor1 = new Gate();
           nor1.gtype = GateType.NOR2;
           nor1.input.add(outp1);
           nor1.input.add(outp2);
           nor1.output = g.output;
           
           not1.calculateStage();
           not2.calculateStage();
           nor1.calculateStage();
           
           nor_eq.add(not1);
           nor_eq.add(not2);
           nor_eq.add(nor1);
       }
       
       
       else if(g.gtype == GateType.NAND2)
       {
           Gate not1 = new Gate();
           not1.gtype = GateType.NOT;
           not1.input.add(g.input.get(0));
           Wire outp1 = new Wire();
           outp1.name = "Wire" + Global.wirecount++;
           not1.output = outp1;
           
           Gate not2 = new Gate();
           not2.gtype = GateType.NOT;
           not2.input.add(g.input.get(1));
           Wire outp2 = new Wire();
           outp2.name = "Wire" + Global.wirecount++;
           not2.output = outp2;
           
           Gate nor1 = new Gate();
           nor1.gtype = GateType.NOR2;
           nor1.input.add(outp1);
           nor1.input.add(outp2);
           Wire outp3 = new Wire();
           outp3.name = "Wire" + Global.wirecount++;
           nor1.output = outp3;
           
           Gate not3 = new Gate();
           not3.gtype = GateType.NOT;
           not3.input.add(outp3);
           not3.output = g.output;
           
           not1.calculateStage();
           not2.calculateStage();
           nor1.calculateStage();
           not3.calculateStage();
           
           nor_eq.add(not1);
           nor_eq.add(not2);
           nor_eq.add(nor1);
           nor_eq.add(not3);
           
       }
       
       
       else if(g.gtype == GateType.OR2)
       {
           Gate nor1  = new Gate();
           nor1.gtype = GateType.NOR2;
           nor1.input.add(g.input.get(0));
           nor1.input.add(g.input.get(1));
           Wire outp1 = new Wire();
           outp1.name = "Wire" + Global.wirecount++;
           nor1.output = outp1;
           
           Gate not1 = new Gate();
           not1.gtype = GateType.NOT;
           not1.input.add(outp1);
           not1.output = g.output;
          
           nor1.calculateStage();
           not1.calculateStage();
           
           nor_eq.add(nor1);
           nor_eq.add(not1);
           
       }
       
       else if(g.gtype == GateType.NOR2)
       {
           g.calculateStage();
           nor_eq.add(g);
       }
       
       else if(g.gtype == GateType.XOR2)
       {
           Gate not1 = new Gate();
           not1.gtype = GateType.NOT;
           not1.input.add(g.input.get(0));
           Wire outp1 = new Wire();
           outp1.name = "Wire" + Global.wirecount++;
           not1.output = outp1;
           
           Gate not2 = new Gate();
           not2.gtype = GateType.NOT;
           not2.input.add(g.input.get(1));
           Wire outp2 = new Wire();
           outp2.name = "Wire" + Global.wirecount++;
           not2.output = outp2;
           
           Gate nor1 = new Gate();
           nor1.gtype = GateType.NOR2;
           nor1.input.add(g.input.get(0));
           nor1.input.add(g.input.get(1));
           Wire outp3 = new Wire();
           outp3.name = "Wire" + Global.wirecount++;
           nor1.output = outp3;
           
           Gate nor2 = new Gate();
           nor2.gtype = GateType.NOR2;
           nor2.input.add(outp1);
           nor2.input.add(outp2);
           Wire outp4 = new Wire();
           outp4.name = "Wire" + Global.wirecount++;
           nor2.output = outp4;
           
           Gate nor3 = new Gate();
           nor3.gtype = GateType.NOR2;
           nor3.input.add(outp3);
           nor3.input.add(outp4);
           nor3.output = g.output;
           
           not1.calculateStage();
           not2.calculateStage();
           nor1.calculateStage();
           nor2.calculateStage();
           nor3.calculateStage();
           
           nor_eq.add(not1);
           nor_eq.add(not2);
           nor_eq.add(nor1);
           nor_eq.add(nor2);
           nor_eq.add(nor3);
                                                       
       }
       else if(g.gtype == GateType.XNOR2)
       {
           Gate not1 = new Gate();
           not1.gtype = GateType.NOT;
           not1.input.add(g.input.get(0));
           Wire outp1 = new Wire();
           outp1.name = "Wire" + Global.wirecount++;
           not1.output = outp1;
           
           Gate not2 = new Gate();
           not2.gtype = GateType.NOT;
           not2.input.add(g.input.get(1));
           Wire outp2 = new Wire();
           outp2.name = "Wire" + Global.wirecount++;
           not2.output = outp2;
           
           Gate nor1 = new Gate();
           nor1.gtype = GateType.NOR2;
           nor1.input.add(outp1);
           nor1.input.add(g.input.get(1));
           Wire outp3 = new Wire();
           outp3.name = "Wire" + Global.wirecount++;
           nor1.output = outp3;
           
           Gate nor2 = new Gate();
           nor2.gtype = GateType.NOR2;
           nor2.input.add(g.input.get(0));
           nor2.input.add(outp2);
           Wire outp4 = new Wire();
           outp4.name = "Wire" + Global.wirecount++;
           nor2.output = outp4;
           
           Gate nor3 = new Gate();
           nor3.gtype = GateType.NOR2;
           nor3.input.add(outp3);
           nor3.input.add(outp4);
           nor3.output = g.output;

           not1.calculateStage();
           not2.calculateStage();
           nor1.calculateStage();
           nor2.calculateStage();
           nor3.calculateStage();

           
           nor_eq.add(not1);
           nor_eq.add(not2);
           nor_eq.add(nor1);
           nor_eq.add(nor2);
           nor_eq.add(nor3);
       }
       return nor_eq;
    }  
    
    
    public static String netlist(Gate g)
    {
        String netbuilder="";
        netbuilder += g.gtype;
            netbuilder += "(";
            netbuilder += g.output.name;
            
            for(Wire x:g.input)
            {
                netbuilder += ",";
                netbuilder += x.name;
            }
            netbuilder += ")";
            //netbuilder += "  Stage:";
            //netbuilder += g.gatestage;
        return netbuilder;
    }
    
}