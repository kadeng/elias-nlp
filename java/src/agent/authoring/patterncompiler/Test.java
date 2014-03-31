package elias.agent.authoring.patterncompiler;

import javax.swing.UIManager;
import java.awt.*;

public class Test {
  boolean packFrame = false;

  //Anwendung konstruieren
  public Test() {
    TestGUI frame = new TestGUI();
    //Frames validieren, die eine voreingestellte Groesse besitzen
    //Frames packen, die n�tzliche bevorzugte Infos ueber die Groesse besitzen, z.B. aus ihrem Layout
    if (packFrame)
      frame.pack();
    else
      frame.validate();
    //Fenster zentrieren
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height)
      frameSize.height = screenSize.height;
    if (frameSize.width > screenSize.width)
      frameSize.width = screenSize.width;
    frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    frame.setVisible(true);
  }

  //Main-Methode
  public static void main(String[] args) {
    try  {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch(Exception e) {
    }
    new Test();
  }
} 