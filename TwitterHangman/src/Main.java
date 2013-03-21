import me.reckter.bot.Bot;
import me.reckter.misc.Console;

public class Main {

    public static void main(String[] args)
    {
        Bot b = new Bot();
        while(true)
        {
            b.tick();
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e)
            {
                Console.c_log("twitter", "main clock", e.toString());
            }
        }
    }
}
