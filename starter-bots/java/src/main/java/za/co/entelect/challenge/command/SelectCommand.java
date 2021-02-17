package za.co.entelect.challenge.command;

public class SelectCommand implements Command{

    private final Command selectedCommand;          //variabel penyimpan commandnya mau apa
    private final int selectedWorm;           //variabel penyimpan worm mana yang ditunjuk
    public static int dipanggil=0;


    public SelectCommand(int worm, Command cmd){
        selectedWorm = worm;
        selectedCommand = cmd;
        dipanggil++;
    }

    //Pengecekan apakah udh melebihi batas select di luar aja (bot.java)

    @Override
    public String render() {
        return String.format("select %d;", selectedWorm) + selectedCommand.render();


    }
}
