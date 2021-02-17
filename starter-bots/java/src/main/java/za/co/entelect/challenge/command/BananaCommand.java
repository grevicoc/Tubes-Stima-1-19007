package za.co.entelect.challenge.command;

//Untuk make kelas ini perhatiin dulu usednya paakah sudah 3x atau belum dan apakah
//id wormnya sesuai (banana butuh id 2)

public class BananaCommand implements Command{

    private final int x;
    private final int y;
    public static int used=0;

    public BananaCommand(int x, int y){
        this.x = x;
        this.y = y;
        used++;
    }

    @Override
    public String render() {
        return String.format("banana %d %d", x, y);
    }
}
