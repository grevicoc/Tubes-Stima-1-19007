package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.CellType;
import za.co.entelect.challenge.enums.Direction;

import java.util.*;
import java.util.stream.Collectors;

import javax.swing.UIDefaults.ProxyLazyValue;

public class Bot {

    private Random random;
    private GameState gameState;
    private Opponent opponent;
    private MyWorm currentWorm;

    private Worm friendWorm1;
    private Worm friendWorm2;
    private Worm friendWorm3;

    private Cell healthPack;

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.opponent = gameState.opponents[0];
        this.currentWorm = getCurrentWorm(gameState);

        for (Worm friendWorm : gameState.myPlayer.worms){
            if (friendWorm.id==1){
                friendWorm1 = friendWorm;
            }else if(friendWorm.id==2){
                friendWorm2 = friendWorm;
            }else if(friendWorm.id==3){
                friendWorm3 = friendWorm;
            }
        }

    }

    private MyWorm getCurrentWorm(GameState gameState) {
        return Arrays.stream(gameState.myPlayer.worms)
                .filter(myWorm -> myWorm.id == gameState.currentWormId)
                .findFirst()
                .get();
    }

    public Command run() {

        // PRIO 1, cek apakah dekat worm 2 ada musuh
        Worm enemyWorm = isWorm2NearEnemy();
        if (enemyWorm!=null){
            if (SelectCommand.dipanggil<5 && BananaCommand.used<3){
                return new SelectCommand(2,new BananaCommand(enemyWorm.position.x,enemyWorm.position.y));
            }
        }else{
            if (currentWorm.id==1){
                //TODO masukkin strategi sesuai priotitas di laporan
                
            }else if (currentWorm.id==2){
                //TODO masukkin strategi sesuai priotitas di laporan
            }else if (currentWorm.id==3){
                //TODO masukkin strategi sesuai priotitas di laporan

                // Greedy by Special Weapon
                Worm enemyWorm3 = getFirstWormInRangeSpecial();
                if (enemyWorm3!=null && canSnowball(currentWorm,enemyWorm3)){
                    return new SnowballCommand(enemyWorm3.position.x,enemyWorm.position.y);
                }

                // Greedy by Enemy Position (Shot biasa)
                enemyWorm3 = getFirstWormInRange();
                if (enemyWorm3!=null){
                    Direction enemy3Direction = resolveDirection(currentWorm.position, enemyWorm3.position);
                    return new ShootCommand(enemy3Direction);
                }

                return goToNearestEnemy();

            }
        }

        return new DoNothingCommand();
    }

    private Worm getFirstWormInRange() {

        Set<String> cells = constructFireDirectionLines(currentWorm.weapon.range)
                .stream()
                .flatMap(Collection::stream)
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        for (Worm enemyWorm : opponent.worms) {
            String enemyPosition = String.format("%d_%d", enemyWorm.position.x, enemyWorm.position.y);
            if (cells.contains(enemyPosition) && enemyWorm.health > 0) {
                return enemyWorm;
            }
        }

        return null;
    }

    // Fungsi untuk mencari apakah musuh ada di dekatnya (radius 5)
    private Worm getFirstWormInRangeSpecial(){
        for (Worm enemyWorm : opponent.worms){

            // Menghitung jarak
            int distance = euclideanDistance(currentWorm.position.x,currentWorm.position.y,enemyWorm.position.x,enemyWorm.position.y);
            if (distance <= 5){
                return enemyWorm;
            }
        }
        return null;
    }

    // Fungsi untuk mencari apakah ada musuh di dekat worm 2 (radius 5)
    private Worm isWorm2NearEnemy(){
        for (Worm enemyWorm : opponent.worms){

            // Menghitung jarak
            int distance = euclideanDistance(friendWorm2.position.x,friendWorm2.position.y,enemyWorm.position.x,enemyWorm.position.y);
            if (distance <= 5){
                return enemyWorm;
            }
        }
        return null;
    }

    private List<List<Cell>> constructFireDirectionLines(int range) {
        List<List<Cell>> directionLines = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            List<Cell> directionLine = new ArrayList<>();
            for (int directionMultiplier = 1; directionMultiplier <= range; directionMultiplier++) {

                int coordinateX = currentWorm.position.x + (directionMultiplier * direction.x);
                int coordinateY = currentWorm.position.y + (directionMultiplier * direction.y);

                if (!isValidCoordinate(coordinateX, coordinateY)) {
                    break;
                }

                if (euclideanDistance(currentWorm.position.x, currentWorm.position.y, coordinateX, coordinateY) > range) {
                    break;
                }

                Cell cell = gameState.map[coordinateY][coordinateX];
                if (cell.type != CellType.AIR) {
                    break;
                }

                directionLine.add(cell);
            }
            directionLines.add(directionLine);
        }

        return directionLines;
    }

    private List<Cell> getSurroundingCells(int x, int y) {
        ArrayList<Cell> cells = new ArrayList<>();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                // Don't include the current position
                if (i != x && j != y && isValidCoordinate(i, j)) {
                    cells.add(gameState.map[j][i]);
                }
            }
        }

        return cells;
    }

    private int euclideanDistance(int aX, int aY, int bX, int bY) {
        return (int) (Math.sqrt(Math.pow(aX - bX, 2) + Math.pow(aY - bY, 2)));
    }

    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < gameState.mapSize
                && y >= 0 && y < gameState.mapSize;
    }

    private Direction resolveDirection(Position a, Position b) {
        StringBuilder builder = new StringBuilder();

        int verticalComponent = b.y - a.y;
        int horizontalComponent = b.x - a.x;

        if (verticalComponent < 0) {
            builder.append('N');
        } else if (verticalComponent > 0) {
            builder.append('S');
        }

        if (horizontalComponent < 0) {
            builder.append('W');
        } else if (horizontalComponent > 0) {
            builder.append('E');
        }

        return Direction.valueOf(builder.toString());
    }

    // Fungsi untuk bergerak(move/dig) ke tengah map
    private Command MoveToCenterCommand() {
        int pX = currentWorm.position.x;
        int pY = currentWorm.position.y;
        if (pX > 16 && pY < 16) { // kuadran 1
            pX -= 1;
            pY += 1;
        } else if (pX < 16 && pY < 16) { // kuadran 2
            pX += 1;
            pY += 1;
        } else if (pX < 16 && pY > 16) { // kuadran 3
            pX += 1;
            pY -= 1;
        } else if (pX > 16 && pY > 16) { // kuadran 4
            pX -= 1;
            pY -= 1;
        } else if (pX == 17 && pY < 16) {
            pY += 1;
        } else if (pX == 17 && pY > 16) {
            pY -= 1;
        } else if (pX > 16 && pY == 17) {
            pX -= 1;
        } else if (pX < 16 && pY == 17) {
            pY += 1;
        } else {
            pX += 1;
        }
        Cell block = gameState.map[pY][pX];
        if (block.type == CellType.AIR) {
            // return return String.format("move %d %d", x, y);
            return new MoveCommand(block.x, block.y);
        } else if (block.type == CellType.DIRT) {

            return new DigCommand(block.x, block.y);
        }
        return new DoNothingCommand();
    }
    
    // Fungsi untuk menentukan harus bergerak ke block mana tergantung arah yang diinginkan
    private Command MoveByDirCommand(String dir) {
        int pX = currentWorm.position.x;
        int pY = currentWorm.position.y;
        if (dir.equals("N")) {
            pY -= 1;
        } else if (dir.equals("NE")){
            pX += 1;
            pY -= 1;
        } else if (dir.equals("E")){
            pX += 1;
        } else if (dir.equals("SE")){
            pX += 1;
            pY += 1;
        } else if (dir.equals("S")){
            pY += 1;
        } else if (dir.equals("SW")){
            pX -= 1;
            pY += 1;
        } else if (dir.equals("W")){
            pX -= 1;
        } else if (dir.equals("NW")){
            pX -= 1;
            pY -= 1;
        }
        Cell block = gameState.map[pY][pX];
        if (block.type == CellType.AIR) {
            // return return String.format("move %d %d", x, y);
            return new MoveCommand(block.x, block.y);
        } else if (block.type == CellType.DIRT) {
            return new DigCommand(block.x, block.y);
        }
        return new DoNothingCommand();
    }

    // Fungsi untuk mengecek apakah bisa melempar snowball
    private boolean canSnowball(Worm ourWorm, Worm enemyWorm){

        if (ourWorm.id==3 && enemyWorm.roundsUntilUnfrozen==0 && SnowballCommand.used < 3){

            // Mengecek jarak lempar
            int distanceBetweenThem = euclideanDistance(ourWorm.position.x,ourWorm.position.y,enemyWorm.position.x,enemyWorm.position.y);
            if (distanceBetweenThem <= 5){
                return true;
            }
        }
        return false;
    }

    // Fungsi untuk mengecek apakah bisa melempar bananabomb
    private boolean canBananaBomb(Worm ourWorm, Worm enemyWorm){
        if (ourWorm.id==2 && BananaCommand.used<3){
            int distanceBetweenThem = euclideanDistance(ourWorm.position.x,ourWorm.position.y,enemyWorm.position.x,enemyWorm.position.y);

            // Cek dulu ini bomb bakal friendly damage ga
            int distanceBetweenbomb1 = euclideanDistance(friendWorm1.position.x,friendWorm1.position.y,enemyWorm.position.x,enemyWorm.position.y);
            int distanceBetweenbomb2 = euclideanDistance(friendWorm2.position.x,friendWorm2.position.y,enemyWorm.position.x,enemyWorm.position.y);
            int distanceBetweenbomb3 = euclideanDistance(friendWorm3.position.x,friendWorm3.position.y,enemyWorm.position.x,enemyWorm.position.y);
            if (distanceBetweenThem <= 5){
                if(distanceBetweenbomb1 >2 && distanceBetweenbomb2>2 && distanceBetweenbomb3>2){
                    return true;
                }
            }
        }
        return false;
    }

    private Command followWorm(Worm ourWorm){
        Direction followTo = resolveDirection(ourWorm.position,friendWorm1.position);
        return MoveByDirCommand(followTo.toString());
    }

    //Fungsi untuk mengecek apakah ada powerUp di 4 cell sekitar dia
    private Cell checkPowerUpAround5(){

        // Di fungsi ini dia punya "kesadaran" untuk nyari powerup kl healthnya < 25
        if (currentWorm.health<25){

            //loop mencari apakah ada power up di sekitar dia
            for (int i=currentWorm.position.x-4; i<currentWorm.position.x+4;i++){
                for (int j=currentWorm.position.y-4; i<currentWorm.position.y+4;j++){
                    if (gameState.map[i][j].powerUp.value==10){
                        return gameState.map[i][j];
                    }
                }
            }
        }
        return null;
    }

    // Fungsi untuk bergerak ke enemy paling dekat
    private Command goToNearestEnemy(){

        Worm destinationWorm = friendWorm1;             //dummy agar tidak error, nantinya akan langsung berubah
        int distanceWithNearestEnemy = 100;

        for (Worm enemyWorm : opponent.worms){
            // Menghitung jarak
            int distance = euclideanDistance(currentWorm.position.x,currentWorm.position.y,enemyWorm.position.x,enemyWorm.position.y);
            if(distance < distanceWithNearestEnemy){
                distanceWithNearestEnemy = distance;
                destinationWorm = enemyWorm;
            }
        }
        Direction destination = resolveDirection(currentWorm.position, destinationWorm.position);
        return MoveByDirCommand(destination.toString());

    }

}