import java.io.Serializable;
import java.sql.Date;

import java.util.Calendar;

public class Person implements Serializable{
   private long uid;
   private String name;
   private String password;
   private String dep;
   private String address;
   private int phoneNumber;
   private int ccNr;
   private Calendar ccValidity;

   Person(long uid, String name, String password, String dep, String address, int phoneNumber, int ccNr, Calendar ccValidity){
       super();
       this.uid = uid;
       this.name = name;
       this.password = password;
       this.dep = dep;
       this.address = address;
       this.phoneNumber = phoneNumber;
       this.ccNr = ccNr;
       this.ccValidity = ccValidity;
   }
}
