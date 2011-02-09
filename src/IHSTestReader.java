import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;


public class IHSTestReader
{
  
  private static char READY = 'R';
  private static char LOCK = 'L';
  private static char CLICK = 'C';
  public static void main(String[] args) throws Exception
  {
    File fileIn = new File(args[0]);
    File fileOut = new File(args[1]);
    PrintWriter out = new PrintWriter(new FileWriter(fileOut));
    BufferedReader in = new BufferedReader(new FileReader(fileIn));
    String line;
    char state = READY;
    String description = "";
    while ((line = in.readLine()) != null)
    {
      if (line.startsWith("Case:"))
      {
        description = line.substring(5).trim();
      }
      if (line.startsWith("Contraindicated vaccines: None indicated"))
      {
        state = LOCK;
        continue;
      }
      if (state == LOCK)
      {
        if (line.startsWith("----------"))
        {
          state = CLICK;
          out.println("-----------------------------------------");
          out.println(description);
        }
        continue;
      }
      if (state == CLICK)
      {
        if (line.startsWith("----------"))
        {
          state = READY;
          continue;
        }
        out.println(line);
      }
    }
    out.close();
  }
}
