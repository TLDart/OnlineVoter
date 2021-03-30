import java.util.ArrayList;
public class Info {
   String name;
   String cc; 
   ArrayList<Election> validElections;

   Info(String name, String cc, ArrayList<Election> validElections){
       this.name = name; 
       this.cc = cc;
       this.validElections = validElections;
   }
}
