
// phred 1.2
// (C) 1999-2004 Robby Glen Garner and Paco Xander Nathan
// Released under the GNU Public License

import com.jfred.*;
import com.oroinc.text.regex.*;
import java.awt.*;
import java.applet.*;
import java.net.*;
import java.io.*;
import java.awt.datatransfer.*;

public class phred extends Applet implements Runnable, ClipboardOwner {

  Clipboard clipboard = getToolkit().getSystemClipboard();
  public String person = "Unknown Host";
  InetAddress thisAddress= null;
  String displayString=null;

  // get ready for freddy
  private long curtime, endtime;
  private static Grammar grammar = null;
  private static Fred fred = null;
  private static Context context = null;
  private static Cart cart = null;
  Config config = new Config(new File(System.getProperty("user.dir"), "JFRED.cfg"));

  // data contained in dialog box

  private final static boolean debug = false;

  public boolean inApplet = true;
  public xFrame frame = new xFrame();  // xFrame handles destroy properly.

  public TextField input = new TextField("");
  public TextArea record = new TextArea();

  // Initialize data structures

  public void init (String[] args) {
    String dir = System.getProperty("user.dir");

    // setup rules

    grammar = new Grammar();
    fred = new Fred(grammar, true,"/Users/robby/JFRED/out/production/JFRED/jfred.dat");

    // setup cart, loading an existing cart, if given

    String cartName = "dirt";

    if (args.length > 1)
      cartName = args[1];

    cart = new Cart(dir, cartName, false);
    cart.put("render", "html2");

    context = new Context(cart, fred);
    context.loadFile();

    fred.setVerbose(false);

    // determine whether we're running as an applet

    try {
      boolean useParam = true;
      AppletContext a = getAppletContext();

//      Person = getParameter("person");
//      RulezNamez = getParameter("ruleset");
//      AudioInitFile = getParameter("announce");

    }
    catch (NullPointerException npe) {
      inApplet = false;
    }

    inApplet = false;
	 
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
      System.err.println("Is your windowing system running?");
      System.exit(1);
    }

    // initialize frame, if needed

    if (inApplet) {
      show();
    }
    else {
      frame.setTitle("phred console");
      frame.add("Center", this);
      frame.pack();	
      frame.setSize(600, 300);
      frame.show();
    }


    try{
        thisAddress = InetAddress.getLocalHost();
        person=new String(thisAddress.getHostName());
    }
        catch(UnknownHostException e) {
           person="unknown host";
    }
    
    displayString = new String("Operating.\n\n" );
    record.setText(displayString);
    input.setText("");
    input.requestFocus();

   //Open Gman = new Open("http://www.robitron.com");

  }

  // Catch the ENTER keys for input

  public boolean action (Event event, Object what) {
    if (event.target == input) {
      record.append("You Say : " + input.getText() + "\n");
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
            "what is a ",
            "what is the ",
            "tell me about ",
            "define ",
            "have you seen ",
            "did you see ",
            "did you like ",
            "what happened in ",
            "what's a ",
            "is that a ",
            "is this a ",
            "tell me ",
            "have you ever heard of ",
            "do you know of ",
            "do you know about ",
            "do you know ",
            "what do you think about ",
            "have you studied ",
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
//                 try {
//                     url = new URL(queryString);
//                     getAppletContext().showDocument(url, "_blank");
//                 } catch (MalformedURLException e) {
//                     System.out.println("URL not reachable");
//                 }

        }
  }



  String callFRED(String stimulus) {

       // perform initial parsing

       Phrase phrase = new Phrase(stimulus, grammar.breaks);

       grammar.removeCants(phrase); 
       grammar.removeFluff(phrase);

       phrase.stashExpand();
	
       // formulate a response

       String response = fred.formReply(phrase, context, true);

       // save the current context

       cart.saveFile();
       context.saveFile();

       return response;
  }


  //render a url from in an applet

  public void browser_spawn(boolean inAnApplet, String com) {
     URL url = null;
     if (inApplet) {
        try {
              url = new URL(com);
            } catch (MalformedURLException e) {
              System.out.println("URL not reachable");
            }

        getAppletContext().showDocument(url, "_blank");
     }
     else {
            try {
			   Open Gman = new Open(com);
               //Process p = Runtime.getRuntime().exec("iexplore " + com);
            }
            catch (Exception e) {
               System.err.println(e);
               e.printStackTrace();
            }
     }
  }

  // Send a line of text to the FRED

  public void sendLine () {
    String links[] = null;
    String response = "";
    boolean found = false;

    String stimulus = input.getText();

    //    formQuest(stimulus.toLowerCase());


    input.setText("");

    response = callFRED(stimulus);

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

    //copy to clipboard
    StringSelection contents = new StringSelection(line);
    clipboard.setContents(contents, this);

    //set delay

    endtime = curtime + 3000 + (line.length()/10)*1000;


    //take a single threaded siesta

//    do {
//         curtime = System.currentTimeMillis();
//    } while (curtime < endtime);


    if (line.length() <= 60) {
       processed_line = line;
    }
    else {
           for (int k=0; (k < (line.length())); k++) {
               if ((line.charAt(k)==' ') && (dis>60)) {
                   dis=0;
                   processed_line=processed_line.concat(sub + "\r\n");
                   sub="";
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

    record.append("Bot Says: " + processed_line);
    record.append("\n\n");
    text = record.getText();
    record.setSelectionStart(text.length());
    record.setSelectionEnd(text.length());
  }

  
  public void run () {
  }


  public void start () {
    input.requestFocus();
    super.start();
  }


  public void stop () {
    super.stop();
  }

  public void lostOwnership(Clipboard clipboard, Transferable contents) {
         //System.out.println("Speak.");
  }


  // Console Operation

  public static void main (String[] args) {
    phred baru = new phred();
    baru.init(args);
    baru.start();
  }
}
