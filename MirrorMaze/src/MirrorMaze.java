import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/*
 *
The Problem
You will be given a block of square rooms in an X by Y configuration,
with a door in the center of every wall.  Some rooms will have a mirror in them at a 45 degree angle.
The mirrors may reflect off both sides (2-way mirrors)
or reflect off one side and allow the beam to pass through from the other (1-way mirrors).
When the laser hits the reflective side of one of the mirrors,
the beam will reflect off at a 90 degree angle.
Your challenge is to calculate the exit point of a laser shot into one of the open doors.
You need to provide the room it will be exiting through along with the orientation.
The definition file will be provided through command line parameters.
 *
 */

public class MirrorMaze {

    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            System.out.println("please give input file.");
            return;
        }

        int mazeRow = 0;
        int mazeCol = 0;
        ArrayList<String> mirrors = new ArrayList<String>();
        int laserStartRow = -1;
        int laserStartCol = -1;
        String laserOrientation = null;
        int inputCounter = 0;

        FileReader fin = new FileReader(args[0]);
        Scanner in = new Scanner(fin);

        // read the configuration for the mirror board and the laser
        // TO-DO: to catch all kinds of input file exceptions.
        while (in.hasNextLine()) {
            String nextLine = in.nextLine().trim();
            if (nextLine.startsWith("-1")) {
                inputCounter++;
                if (in.hasNext()) nextLine = in.nextLine().trim();
            }
            // read maze size
            if (inputCounter == 0) {
                String mazeSize = nextLine;
                mazeCol = Integer.valueOf(mazeSize.trim().split(",")[0]);
                mazeRow = Integer.valueOf(mazeSize.trim().split(",")[1]);
            }
            // read mirror
            if (inputCounter == 1) {
                mirrors.add(nextLine);
            }

            // read laser
            if (inputCounter == 2) {
                String laser = nextLine;
                laserStartCol = Integer.valueOf(laser.substring(0, 1));
                laserStartRow = Integer.valueOf(laser.substring(2, 3));
                laserOrientation = new String(laser.substring(3));
            }
        }

        in.close();
        fin.close();

        Mirror[][] mirrorMaze = new Mirror[mazeCol][mazeRow];
        for (String m : mirrors) {

            Mirror tmpMirror;
            if (m.length() > 4) {
                String d = new String(m.substring(3, 4));
                String s = new String(m.substring(4));
                tmpMirror = new Mirror(d, s);
            } else {
                String d = new String(m.substring(3, 4));
                tmpMirror = new Mirror(d);
            }
            int col = Integer.valueOf(m.substring(0, 1));
            int row = Integer.valueOf((m.substring(2, 3)));
            mirrorMaze[col][row] = tmpMirror;
        }

        mazePath(mirrorMaze, laserStartCol, laserStartRow, laserOrientation);
    }

    // find the path of a laser in the maze.
    public static void mazePath(Mirror[][] board, int col, int row, String orientation) {
        // validate the input of the laser
        if (col < 0 || row < 0
                || col >= board.length
                || row >= board[0].length
                || (!orientation.equals("H") && !orientation.equals("V"))) {
            System.out.println("incorrect input");
            return;
        }

        System.out.println("the demensions of board: " + board.length + " x " + board[0].length);

        // track the path of the laser
        ArrayList<Position> path = new ArrayList<Position>();
        String direction = "+"; // "+": increase step; "-": decrease step
        path.add(new Position(col, row, orientation, direction));


        // if last position is out of board, it is finished.
        Position last = path.get(path.size() - 1);
        while ((last.col >= 0 && last.col < board.length)
                && (last.row >= 0 && last.row < board[0].length)) {
            nextPosition(board, path);
            last = path.get(path.size() - 1);
        }

        // print the path from start to exit.
        System.out.println("the path of the laser: ");
        for (int i = 0; i < path.size() - 1; i++) {
            Position p = path.get(i);
            System.out.println(p);
        }

    }

    // calculate next position of the laser
    public static void nextPosition(Mirror[][] board, ArrayList<Position> path) {
        Position prev = path.get(path.size() - 1);
        int prevCol = prev.col;
        int prevRow = prev.row;
        String prevOrient = prev.orientation;
        String prevDirection = prev.direction;
        int nextCol = -1;
        int nextRow = -1;
        String nextOrient = prevOrient;
        String nextDirection = prevDirection;

        if (prevOrient.equals("H")) {
            nextCol = prevCol + ((prevDirection.equals("+")) ? 1 : -1);
            nextRow = prevRow;
        }
        if (prevOrient.equals("V")) {
            nextRow = prevRow + ((prevDirection.equals("+")) ? 1 : -1);
            nextCol = prevCol;
        }

        if ((nextCol >= 0 && nextCol < board.length)
                && (nextRow >= 0 && nextRow < board[0].length)) {

            Mirror mirror = board[nextCol][nextRow];
            if (mirror != null) {
                if (mirror.direction.equals("R")) {

                    if (mirror.rightSide) {
                        if (prevOrient.equals("V") && prevDirection.equals("+")) {
                            nextOrient = "H";
                            nextDirection = "+";

                        }
                        if (prevOrient.equals("H") && prevDirection.equals("-")) {
                            nextOrient = "V";
                            nextDirection = "-";
                        }
                    }

                    if (mirror.leftSide) {
                        if (prevOrient.equals("V") && prevDirection.equals("-")) {
                            nextOrient = "H";
                            nextDirection = "-";

                        }
                        if (prevOrient.equals("H") && prevDirection.equals("+")) {
                            nextOrient = "V";
                            nextDirection = "+";
                        }
                    }
                }

                if (mirror.direction.equals("L")) {
                    if (mirror.rightSide) {
                        if (prevOrient.equals("V") && prevDirection.equals("-")) {
                            nextOrient = "H";
                            nextDirection = "+";

                        }
                        if (prevOrient.equals("H") && prevDirection.equals("-")) {
                            nextOrient = "V";
                            nextDirection = "+";
                        }
                    }

                    if (mirror.leftSide) {
                        if (prevOrient.equals("V") && prevDirection.equals("+")) {
                            nextOrient = "H";
                            nextDirection = "-";

                        }
                        if (prevOrient.equals("H") && prevDirection.equals("+")) {
                            nextOrient = "V";
                            nextDirection = "-";
                        }
                    }
                }
            }
        }

        Position next = new Position(nextCol, nextRow, nextOrient, nextDirection);
        for (Position p : path) {
            // check if the laser is trapped in the maze.
            // path is handy for this.
            if (p.equals(next))
                throw new RuntimeException("the laser is trapped in the maze.");
        }
        path.add(next);

    }
}
