
// Phred P2 Applet. 
// (C) 1999 Robby Glen Garner and Paco Xander Nathan

import com.jfred.*;
import com.oroinc.text.regex.*;
import java.awt.*;
import java.applet.*;
import java.net.*;
import java.io.*;

public class p2 extends Applet {
  public static String Person = "Phred";
  public static String Foreigner = "Unknown Host";
  public static String RulezNamez = "jfred.dat";
  public static String AudioInitFile = "working.au";

  //special funky stuff
  AudioClip announce;
  InetAddress thisAddress= null;
  String displayString=null;

  // get ready for freddy
  private long curtime, endtime;
  public static Grammar grammar = null;
  private static Fred fred = null;
  private static Context context = null;

 // data contained in dialog box

  private final static boolean debug = false;

  public boolean inApplet = true;
  public xFrame frame = new xFrame();  // xFrame handles destroy right

  public TextField input = new TextField("");
  public TextArea record = new TextArea();

  // Initialize data structures

  public void init () {

    // calling the validate method here can help applets;
    // unnecessary when this program runs as an application

    validate();

    // determine whether we're running as an applet

    try {
      boolean useParam = true;
      AppletContext a = getAppletContext();

      Person = getParameter("person");
      RulezNamez = getParameter("ruleset");
      AudioInitFile = getParameter("announce");

    }
    catch (NullPointerException npe) {
      inApplet = false;
    }

    //questions.DumpLog();

    try {
      setLayout(new BorderLayout());

      record.setEditable(false);
      record.setForeground(Color.green);
      record.setBackground(Color.black);

      input.setForeground(Color.yellow);
      input.setBackground(Color.blue);

      add("Center", record);
      add("South", input);
    }
    catch (Exception e) {
      System.err.println(Person + ": is your windowing system running?");
      System.exit(1);
    }


    // initialize frame, if needed

    if (inApplet) {
      show();
    }
    else {
      frame.setTitle(Person + " Console");
      frame.add("Center", this);
      frame.pack();	
      frame.show();
    }

    try{
        thisAddress = InetAddress.getLocalHost();
        Foreigner=new String(thisAddress.getHostName());
    }
        catch(UnknownHostException e) {
           Foreigner="unknown host";
    }
    
    displayString = new String("Initializing.\nP2 v.1.0 (C) 1999 Robby Glen Garner and Paco Xander Nathan.\n\nSensors detecting " + Foreigner + "\n\n" + Person + " is now operational.\n\n" );
    record.setText(displayString);
    input.setText("");
    input.requestFocus();

    //announce = getAudioClip(getCodeBase(), AudioInitFile);
    //announce.play();

    grammar = new Grammar();

    fred = new Fred(grammar, true, RulezNamez);

    Cart cart = new Cart("", "", false);

    context = new Context(cart, fred);

    fred.setVerbose(false);

  }

  // Catch the ENTER keys for input

  public boolean action (Event event, Object what) {
    if (event.target == input) {
      record.appendText(">" + input.getText() + "\n");
      curtime = System.currentTimeMillis();
      sendLine();
    }
    return true;
  }

  public void formQuest(String inputLine)
  {
       String topic = "";
       URL url = null;
       String queryString = "";
       String query[]= {
            "define ",
            "search for ",
            "help me find ",
            "end-of-list"
        };    
 
        int i, gotAt;
        for (i=0; !query[i].equals("end-of-list"); i++) {
           gotAt=inputLine.indexOf(query[i]);
           if (gotAt>=0) {
              topic=inputLine.substring(gotAt + query[i].length());
           }
        }

        if (topic.length()>0) {
           
           queryString = "http://www.altavista.digital.com/cgi-bin/query?pg=q&what=web&kl=XX&q=" + topic.replace(' ','+');
           // http://www.lycos.com/cgi-bin/pursuit?matchmode=and&cat=lycos&query=" + topic;
           // System.out.println(queryString);
                 try {
                     url = new URL(queryString);
                     getAppletContext().showDocument(url, "_blank");
                 } catch (MalformedURLException e) {
                     System.out.println("URL not reachable");
                 }

        }
  }



  String callFRED(String stimulus) {

     String response = null;

        // perform initial parsing

        Phrase phrase = new Phrase(stimulus, grammar.breaks);

        grammar.removeCants(phrase); 
        grammar.removeFluff(phrase);

        phrase.stashExpand();
	
        // formulate a response

       response = fred.formReply(phrase, context, true);

       return response;
  }


  //render a url from in an applet

  public void browser_spawn(boolean inAnApplet, String com) {
     URL url = null;

     try {
           url = new URL(com);
         } catch (MalformedURLException e) {
           System.out.println("URL not reachable");
         }

     getAppletContext().showDocument(url, "_blank");
  }

  // Send a line of text to the FRED

  public void sendLine () {
    String links[] = null;
    String response = "";
    boolean found = false;

    String stimulus = input.getText();

    formQuest(stimulus.toLowerCase());

    input.setText("");

    response = callFRED(stimulus);

       // email the response chosen
       // [insert here]

    if (!response.equals("")) {
       recordLine(response);
    }

    // go display the URL's

    links = context.getLinks();

    if (links != null) {
       for (int i = 0; i < links.length; i++) {
           browser_spawn(inApplet, links[i]);
       }
    }
  }


  // Record a line of text from the FRED

  public void recordLine (String line) {
    String text = record.getText();
    String processed_line = "";
    String sub = "";
    int dis = 0;

    //set delay

    endtime = curtime + 3000 + (line.length()/10)*1000;

    //take a single threaded siesta

    do {
         curtime = System.currentTimeMillis();
    } while (curtime < endtime);


    if (line.length() <= 65) {
       processed_line = line;
    }
    else {
           for (int k=0; (k < (line.length())); k++) {
               if ((line.charAt(k)==' ') && (dis>65)) {
                   dis=0;
                   processed_line=processed_line.concat(sub + "\r\n");
                   sub="";
               }
               else if (line.charAt(k) == '\\') {
                        dis = 0;
                        k++;
                        processed_line=processed_line.concat(sub + "\r\n");
                        sub = "";
               }
               else {
                       dis++;
                       if (line.charAt(k)=='\n') {
                           dis = 0;
                       }
                       sub=sub.concat(line.substring(k,k+1));
               }
           } 
           processed_line=processed_line.concat(sub + "\r\n");
    }

    record.appendText(processed_line);
    record.appendText("\n\n");
    record.select(text.length() + 1, text.length() + 1);

  }

  
  public void start () {
    input.requestFocus();
    super.start();
  }


  public void stop () {
    super.stop();
  }


  // Console Operation

  public static void main (String[] args) {
    p2 rulez = new p2();
    rulez.init();
    rulez.start();
  }
}
