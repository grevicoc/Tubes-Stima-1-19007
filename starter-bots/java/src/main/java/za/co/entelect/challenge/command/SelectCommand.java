package za.co.entelect.challenge.command;

public class SelectCommand implements Command{

    private final Command selectedCommand;          //variabel penyimpan commandnya mau apa
    private final int selectedWorm;           //variabel penyimpan worm mana yang ditunjuk
    private static int dipanggil;
    static {
        dipanggil=0;
    }

    public SelectCommand(int worm, Command cmd){
        selectedWorm = worm;
        selectedCommand = cmd;
        dipanggil++;
    }

    @Override
    public String render() {
        if (dipanggil<=5){
            return String.format("select %d;", selectedWorm) + selectedCommand.render();
        }else{
            return selectedCommand.render();
        }

    }
}
