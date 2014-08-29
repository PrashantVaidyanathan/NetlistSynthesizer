/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Test;

import BU.CelloGraph.DAGW;
import BU.ParseVerilog.Blif;
import BU.ParseVerilog.CircuitDetails;
import BU.ParseVerilog.Convert;
import BU.ParseVerilog.Espresso;
import BU.ParseVerilog.Parser;
import BU.ParseVerilog.parseVerilogFile;
import BU.booleanLogic.BooleanSimulator;
import BU.netsynth.DGate;
import BU.netsynth.DGateType;
import BU.netsynth.DWire;
import BU.netsynth.DWireType;
import BU.netsynth.Global;
import BU.netsynth.NetSynth;
import static BU.netsynth.NetSynth.Filepath;
import static BU.netsynth.NetSynth.parseEspressoToNORNAND;
import static BU.netsynth.NetSynth.printGate;
import static BU.netsynth.NetSynth.runEspresso;
import BU.netsynth.NetSynthSwitches;
import BU.netsynth.NetlistConversionFunctions;
import BU.precomputation.PreCompute;
import BU.precomputation.genVerilogFile;
import MIT.dnacompiler.Gate;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author prashantvaidyanathan
 */
public class TestSynthesis {
    
    /**
     * 
     * 
     * 
     * 
     * 
     */
    
    
    public static void test3norconversion()
    {
        List<DGate> inp = new ArrayList<DGate>();
        
        DGate g1 = new DGate();
        DGate g2 = new DGate();
        DGate g3 = new DGate();
        DGate g4 = new DGate();
        DGate g5 = new DGate();
        
        
        
        DWire w1 = new DWire("in1",DWireType.input);
        DWire w2 = new DWire("in2",DWireType.input);
        DWire w3 = new DWire("in3",DWireType.input);
        DWire w4 = new DWire("w1",DWireType.connector);
        DWire w5 = new DWire("w2",DWireType.connector);
        DWire w6= new DWire("out1",DWireType.output);
        DWire w7= new DWire("out2",DWireType.output);
        DWire w8= new DWire("out3",DWireType.output);
        
        g1.gtype = DGateType.NOR;
        g2.gtype = DGateType.NOT;
        g3.gtype = DGateType.NOR;
        g4.gtype = DGateType.NOT;
        g5.gtype = DGateType.NOT;
        
        g1.input.add(w1);
        g1.input.add(w2);
        g1.output = w4;
        
        g2.input.add(w4);
        g2.output = w5;
        
        g3.input.add(w3);
        g3.input.add(w5);
        g3.output = w6;
        
        g4.input.add(w4);
        g4.output = w7;
        
        g5.input.add(w5);
        g5.output = w8;
        
        inp.add(g1);
        inp.add(g2);
        inp.add(g3);
        inp.add(g4);
        inp.add(g5);
        
        System.out.println("Input Netlist");
        NetSynth.printNetlist(inp);
        
        inp = NetSynth.convertToNOR3(inp);
        
        System.out.println("\nOutput Netlist");
        NetSynth.printNetlist(inp);
        
        
        
        
        
    }
    
    public static void testSpecInputVerilog(int n,int tt)
    {
       
            List<String> verilogFileLines = new ArrayList<String>();
            verilogFileLines = genVerilogFile.createSingleOutpVerilogFile(n, tt);
            String filepath = NetSynth.create_VerilogFile(verilogFileLines, "TestNinput");
            DAGW newdag = new DAGW();
            //newdag = NetSynth.runNetSynth(filepath);
            newdag = NetSynth.runNetSynth(filepath, NetSynthSwitches.defaultmode,  NetSynthSwitches.defaultmode,NetSynthSwitches.defaultmode,NetSynthSwitches.defaultmode, NetSynthSwitches.defaultmode);
            System.out.println("\nDAGW Gates");
            for(Gate xgate:newdag.Gates)
            {
                System.out.println("Gate Type: " + xgate.Type + " : Gate Name: " + xgate.Name);
            }
            
            System.out.println("\n\nPrint Graph: \n"+newdag.printGraph());
    }
    
    
    public static void testAllnInputVerilog(int n) // Also perform check to make sure the truthtable produced is correct
    {
        int maxpow = (int) Math.pow(2,Math.pow(2,n));  
        int count =0;
        for(int i=0;i<maxpow;i++)
        {
            List<String> verilogFileLines = new ArrayList<String>();
            verilogFileLines = genVerilogFile.createSingleOutpVerilogFile(n, i);
            String filepath = NetSynth.create_VerilogFile(verilogFileLines, "TestNinput");
            DAGW newdag = new DAGW();
            //newdag = NetSynth.runNetSynth(filepath);
            newdag = NetSynth.runNetSynth(filepath, NetSynthSwitches.defaultmode,  NetSynthSwitches.defaultmode,NetSynthSwitches.defaultmode,NetSynthSwitches.defaultmode, NetSynthSwitches.defaultmode);
            
            /*if(newdag.Gates.size() == 4)
            {
                System.out.println(i);
                
                for(Gate xgate:newdag.Gates)
                    System.out.println(xgate.Type + ":" + xgate.Name );
            }
            */
            String hex = Convert.InttoHex(i);
            System.out.println(hex + ","+(newdag.Gates.size()));
            count += newdag.Gates.size();
        }
        System.out.println("Total : "+ count);
    }
    
    public static void testVerilogrunNetSynth()
    {
        
        String path = Filepath;
        Filepath = parseVerilogFile.class.getClassLoader().getResource(".").getPath();
         
        if(Filepath.contains("prashant"))
        {
            if(Filepath.contains("build/classes/"))
                Filepath = Filepath.substring(0,Filepath.lastIndexOf("build/classes/")); 
            else if(Filepath.contains("src"))
                Filepath = Filepath.substring(0,Filepath.lastIndexOf("src/"));
            Filepath += "src/BU/ParseVerilog/Verilog.v";
           //Filepath += "src/BU/resources/TestNinput.v";
            path = Filepath;
        }
        //System.out.println("Netlist:");
        //NetSynth.printNetlist(NetSynth.getNetlist(path));
        DAGW resdag = new DAGW();
        resdag = NetSynth.runNetSynth(path,NetSynthSwitches.originalstructural,  NetSynthSwitches.defaultmode,NetSynthSwitches.defaultmode,NetSynthSwitches.defaultmode, NetSynthSwitches.defaultmode);
        for(Gate xgate:resdag.Gates)
        {
            System.out.println("Type: " + xgate.Type + " ::: Name: "+ xgate.Name);
        }
        System.out.println("Number of Gates in the DAGW : " + resdag.Gates.size());
        
        System.out.println(resdag.printGraph());
    }
    
    
    public static void histogram()
    {
        String filestring ="";
          if(Filepath.contains("prashant"))
          {
              filestring += Filepath+ "src/BU/resources/Histogram";
          }
          else
          {
              filestring += Filepath+ "BU/resources/Histogram";
          }
        
          
            //filestring += Global.espout++ ;
            filestring += ".csv";
            File fespinp = new File(filestring);
        try 
        {
            Writer output = new BufferedWriter(new FileWriter(fespinp));
            String Line = "SrNo,PreComputer,Espresspo\n";
            output.write(Line);
            List<List<DGate>> precomp;
            precomp = PreCompute.parseNetlistFile();
            CircuitDetails circ = new CircuitDetails();
            circ.inputNames.add("A");
            circ.inputNames.add("B");
            circ.inputNames.add("C");
            circ.outputNames.add("O");
            for(int i=0;i<256;i++)
            {
                Line = "";
                Line += (i + ",");
                if(i==0 || i==255)
                    Line += ("1,"); 
                else
                    Line += (precomp.get(i-1).size() + ",");
                
                
                circ.truthTable.add(Convert.dectoBin(i,circ.inputNames.size() ));
                List<String> eslines = new ArrayList<String>();
                eslines = Espresso.createFile(circ);
                String filestring2 = "";
                if(Filepath.contains("prashant"))
                {
                    filestring2 += Filepath+ "src/BU/resources/espresso";
                }
                else
                {
                    filestring2 += Filepath+ "BU/resources/espresso";
                }
                filestring2 += Global.espout++ ;
                filestring2 += ".txt";
                File fespinp2 = new File(filestring2);
                try 
                {
                    Writer output2 = new BufferedWriter(new FileWriter(fespinp2));
                    for(String xline:eslines)
                    {
                        String newl = (xline + "\n");
                        output2.write(newl);
                    }
                    output2.close();
                    List<String> espout2 = new ArrayList<String>();
                    espout2 = runEspresso(filestring2);
                    List<DGate> espoutput2 = new ArrayList<DGate>();
                    espoutput2 = parseEspressoToNORNAND(espout2);
                    String xf ="";
                    xf = BooleanSimulator.bpermute(espoutput2);
                    
                    //if(i==2)
                    //if(i==253 || i == 254 || i==251 || i==239)
                        //System.out.println(xf);
                    Line += espoutput2.size();
                    fespinp2.deleteOnExit();
              
                } 
                catch (IOException ex) 
                {
                    Logger.getLogger(NetSynth.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                Line += "\n";
                output.write(Line);
            }
            output.close();
        
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(NetSynth.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    public static void testParseVerilogFileFunctions()
    {
        String Filepath;
        String filestring ="";
        Filepath = TestSynthesis.class.getClassLoader().getResource(".").getPath();
        if(Filepath.contains("build/classes/"))
            Filepath = Filepath.substring(0,Filepath.lastIndexOf("build/classes/")); 
        else if(Filepath.contains("src"))
            Filepath = Filepath.substring(0,Filepath.lastIndexOf("src/"));
        if (Filepath.contains("prashant")) 
        {
            filestring += Filepath + "src/BU/ParseVerilog/Verilog.v";
        } 
        else 
        {
            filestring += Filepath + "BU/ParseVerilog/Verilog.v";
        }
        NetSynth.runNetSynth(filestring);
        /*String alllines = parseVerilogFile.verilogFileLines(filestring);
        boolean isStructural = parseVerilogFile.isStructural(alllines);
        if(isStructural)
            System.out.println("Structural Verilog Code!");
        else
        {
            boolean hasCaseStatements = parseVerilogFile.hasCaseStatements(alllines);
            if(hasCaseStatements)
                System.out.println("Has Case Statements!");
            else
                System.out.println("No case statements");
        }*/
    }
    
    
    public static void testespressotoabc()
    {
        String Filepath;
        String filestring ="";
        Filepath = TestSynthesis.class.getClassLoader().getResource(".").getPath();
        if(Filepath.contains("build/classes/"))
            Filepath = Filepath.substring(0,Filepath.lastIndexOf("build/classes/")); 
        else if(Filepath.contains("src"))
            Filepath = Filepath.substring(0,Filepath.lastIndexOf("src/"));
        if (Filepath.contains("prashant")) 
        {
            filestring += Filepath + "src/BU/resources/testespfile.txt";
        } 
        else 
        {
            filestring += Filepath + "BU/resources/testespfile.txt";
        }
        
        List<DGate> netlistoutp = new ArrayList<DGate>();
        netlistoutp = NetSynth.parseEspressoFileToABC(filestring);
        NetSynth.printNetlist(netlistoutp);
    }
      
    public static void test2notstonor()
    {
        List<DGate> netlistn = new ArrayList<DGate>();
        DWire inp1 = new DWire("a",DWireType.input);
        DWire inp2 = new DWire("b",DWireType.input);
        
        DWire wire1 = new DWire("w1",DWireType.connector);
        DWire wire2 = new DWire("w2",DWireType.connector);
        DWire out1 = new DWire("out1",DWireType.output);
        DWire out2 = new DWire("out2",DWireType.output);
        DWire w3 = new DWire("w3",DWireType.connector);
        DWire out3 = new DWire("out3",DWireType.output);
        
        DGate not1 = new DGate();
        DGate not2 = new DGate();
        not1.gtype = DGateType.NOT;
        not1.input.add(inp1);
        not1.output = wire1;
        
        not2.gtype = DGateType.NOT;
        not2.input.add(inp2);
        not2.output = wire2;
        
        
        DGate nor1 = new DGate();
        DGate nor2 = new DGate();
        DGate nor3 = new DGate();
        
        nor1.gtype = DGateType.NOR;
        nor1.input.add(inp1);
        nor1.input.add(wire2);
        nor1.output = out1;
        
        nor2.gtype = DGateType.NOR;
        nor2.input.add(inp2);
        nor2.input.add(wire1);
        nor2.output = out2;
        
        nor3.gtype = DGateType.NOR;
        nor3.input.add(inp1);
        nor3.input.add(inp2);
        nor3.output = w3;
        
        DGate not3 = new DGate();
        not3.gtype = DGateType.NOT;
        not3.input.add(w3);
        not3.output = out3;
        
        
        
        netlistn.add(not1);
        netlistn.add(not2); 
        netlistn.add(nor1);
        
        netlistn.add(nor2);
        netlistn.add(nor3);
        netlistn.add(not3);
        
        
        NetSynth.convert2NOTsToNOR(netlistn);
        
    }
    
    public static void testNORConversion(DGateType gtype)
    {
        DGate inputgate = new DGate();
        inputgate.gtype = gtype;
        for(int i=0;i<2;i++)
        {
            String wirename = "InpWire" + Global.wirecount++;
            DWire inpW = new DWire(wirename,DWireType.input);
            inputgate.input.add(inpW);
        }
        inputgate.output = new DWire("OutW",DWireType.output);
        
        System.out.println("----------------------------------\n Input Gate :");
        System.out.println(NetSynth.printGate(inputgate));
        
        System.out.println("-----------------------------------\n Converted to NOR Gate");
        List<DGate> convertednetlist = new ArrayList<DGate>();
        convertednetlist = NetlistConversionFunctions.GatetoNORNOT(inputgate);
        for(int i=0;i<convertednetlist.size();i++)
        {    
            System.out.println(NetSynth.printGate(convertednetlist.get(i)));
        }
        
    }
    
    public static void testprintNetsynth()
    {
        List<DGate> samplenetlist = new ArrayList<DGate>();
        //samplenetlist = NetSynth.parseStructuralVtoNORNOT("");
        NetSynth.printNetlist(samplenetlist);
        BooleanSimulator.printTruthTable(samplenetlist);
    }
    
    public static void testGetTT()
    {
        List<DGate> samplenetlist = new ArrayList<DGate>();
        //samplenetlist = NetSynth.parseStructuralVtoNORNOT("");
        List<String> ttvals = new ArrayList<String>();
        
        ttvals = BooleanSimulator.getTruthTable(samplenetlist);
        
        System.out.println("Truth Table Values :");
        for(String xtt:ttvals)
        {
            System.out.println(xtt);
        }
    }
    
    public static void testReducedFanin(DGateType gtype, int inputno)
    {
        DGate inputgate = new DGate();
        inputgate.gtype = gtype;
        for(int i=0;i<inputno;i++)
        {
            String wirename = "InpWire" + Global.wirecount++;
            DWire inpW = new DWire(wirename,DWireType.input);
            inputgate.input.add(inpW);
        }
        inputgate.output = new DWire("OutW",DWireType.output);
        
        System.out.println("----------------------------------\n Input Gate :");
        System.out.println(NetSynth.printGate(inputgate));
        
        System.out.println("-----------------------------------\n Reduced Fanin Gates");
        List<DGate> convertednetlist = new ArrayList<DGate>();
        convertednetlist = NetlistConversionFunctions.ConvertToFanin2(inputgate);
        for(int i=0;i<convertednetlist.size();i++)
        {    
            System.out.println(NetSynth.printGate(convertednetlist.get(i)));
        }
    }
    
    
    public static void vtestfunc()
    {
        String line = "module and3(output out, input wire in2,in6, bryan, output wire on1, input in1, in3, in4);";
        Parser.testfunction(line);
    }
    
    public static void verifyinverse()
    {
        String filestring ="";
        if(Filepath.contains("prashant"))
        {
            filestring += Filepath+ "src/BU/resources/Inverse";
        }
        else
        {
            filestring += Filepath+ "BU/resources/Inverse";
        }
          
            filestring += ".csv";
            File fespinp = new File(filestring);
        try {
            List<List<DGate>> precomp;
            precomp = PreCompute.parseNetlistFile();
            Writer output = new BufferedWriter(new FileWriter(fespinp));
            String Line;
            Line = "SrNo,Actual,Inverse\n";
            output.write(Line);
            for(int i=0;i<256;i++)
            {
                 Line ="";
                if(i==0||i==255)
                   Line += i + "\n";
                else
                {
                    int x = precomp.get(i-1).size();
                    int y = precomp.get(253- i + 1).size();
                    //int y=0;
                    Line = i+ ","+ x + "," + (y+1) + "\n";
                }
                output.write(Line);
            }
            output.close();
            
            
        } catch (IOException ex) {
            Logger.getLogger(NetSynth.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
    }
    
    public static void verifyprecomute()
    {
         List<List<DGate>> precomp;
         precomp = PreCompute.parseNetlistFile();
         int flag =0;
         for(int i=0;i<256;i++)
         {
             if(i==0 || i==255)
             {
                 flag =0;
             } 
             else
             {
                 String truthfunc ="";
                 truthfunc = BooleanSimulator.bpermutePreComp(precomp.get(i-1));
                 //System.out.println(truthfunc);
                 int k = Convert.bintoDec(truthfunc);
                 if(k!=i)
                 {
                     System.out.println(truthfunc);
                     System.out.println(i-1);
                     //flag =1;
                     //break;
                 }
             }
         }
         if(flag == 1)
         {
             System.out.println("ERROR!!!");
         }
    }
    
    
    public static void testespressogen()
    {
        List<String> eslines = new ArrayList<String>();
        CircuitDetails circ = new CircuitDetails();
        circ.inputNames.add("inp1");
        circ.inputNames.add("inp2");
        circ.inputNames.add("inp3");
        circ.outputNames.add("out1");
        circ.outputNames.add("out2");
        circ.truthTable.add(Convert.dectoBin(69, 3));
        circ.truthTable.add(Convert.dectoBin(96, 3));
        List<String> espoutcirc = new ArrayList<String>();
        espoutcirc = Espresso.createFile(circ);
        List<String> blifoutcirc = new ArrayList<String>();
        blifoutcirc = Blif.createFile(circ);
        for(String xesp:espoutcirc)
        {
            System.out.println(xesp);
        }
        String filestring = "";
        if (Filepath.contains("prashant")) 
        {
            filestring += Filepath + "src/BU/resources/espresso";
        } 
        else 
        {
            filestring += Filepath + "BU/resources/espresso";
        }
        filestring += Global.espout++;
        filestring += ".txt";
          File fespinp = new File(filestring);
        //Writer output;
        try 
        {
            Writer output = new BufferedWriter(new FileWriter(fespinp));
            //output = new BufferedWriter(new FileWriter(fespinp));
             for (String xline : espoutcirc) 
             {
                String newl = (xline + "\n");
                output.write(newl);
             }
             output.close();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(NetSynth.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        List<String> espout = new ArrayList<String>();
        espout = runEspresso(filestring);
        List<DGate> espoutput = new ArrayList<DGate>();
        espoutput = parseEspressoToNORNAND(espout);
    }
    
    
    /**Function*************************************************************
    Synopsis    []
    Description []
    SideEffects []
    SeeAlso     []
    ***********************************************************************/
    public static void testnetlistmodule()
    {
        DGate and = new DGate();
        DWire w1 = new DWire("A",DWireType.input);
        DWire w2 = new DWire("B",DWireType.input);
        DWire outp = new DWire("outP",DWireType.output);
        List<DWire> inputWires = new ArrayList<DWire>();
        inputWires.add(w1);
        inputWires.add(w2);
        
        DGateType testgateType;
        testgateType = DGateType.NOR;
        DGate gtest = new DGate(testgateType,inputWires,outp);
        
        List<DGate> test = new ArrayList<DGate>();
        test = NetlistConversionFunctions.GatetoNORNOT(gtest);

        for(DGate gout:test)
        {
            
            String netbuilder = "";
            netbuilder = printGate(gout);
            System.out.println(netbuilder);
        
        }   
    }
    
    
}
