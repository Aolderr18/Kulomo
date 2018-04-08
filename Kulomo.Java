package com.bignerdranch.android.kulomo;

import java.util.Collections;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Created by adamolderr on 3/3/18.
 */

public class Kulomo {

    private static final String TAG = Kulomo.class.getSimpleName();

    private char[][] gridCode;
    private Disc[] discs;
    private int score;
    boolean active;
    private int cycle;
    private boolean victorious;
    private boolean collision;
    private boolean cycleEnabled;
    private int freeCollisions;

    public Kulomo() {
        score = 0;
        active = true;
        cycle = 0;
        collision = false;
        cycleEnabled = false;
        freeCollisions = 0;
        boolean[][] discGrid = new boolean[8][8];
        int i, j;
        for (i = 0; i < 8; i++)
            for (j = 0; j < 8; j++)
                discGrid[i][j] = i % 2 == j % 2;
        final int[] iIndices = MainActivity.randomIntegerIndices(8);
        boolean temp;
        for (j = 0; j < 8; j++)
            for (i = 0; i < 8; i++) {
                temp = discGrid[i][j];
                discGrid[i][j] = discGrid[iIndices[i]][j];
                discGrid[iIndices[i]][j] = temp;
            }
        final int[] jIndices = MainActivity.randomIntegerIndices(8);
        for (i = 0; i < 8; i++)
            for (j = 0; j < 8; j++) {
                temp = discGrid[i][j];
                discGrid[i][j] = discGrid[i][jIndices[j]];
                discGrid[i][jIndices[j]] = temp;
            }
        gridCode = new char[8][8];
        for (i = 0; i < 8; i++)
            for (j = 0; j < 8; j++)
                if (discGrid[i][j])
                    gridCode[i][j] = ((int) (Math.random() * 2)) == 1 ? 'L' : 'R';
                else
                    gridCode[i][j] = 'O';
        discs = new Disc[4];
        int[] colorIndices = MainActivity.randomIntegerIndices(8);
        DiscColor[] colorsUsed = new DiscColor[2];
        int s;
        for (s = 0; s < 2; s++)
            switch (colorIndices[s]) {
                case 0:
                    colorsUsed[s] = DiscColor.CYAN;
                    break;
                case 1:
                    colorsUsed[s] = DiscColor.GREEN;
                    break;
                case 2:
                    colorsUsed[s] = DiscColor.ORANGE;
                    break;
                case 3:
                    colorsUsed[s] = DiscColor.PURPLE;
                    break;
                case 4:
                    colorsUsed[s] = DiscColor.RED;
                    break;
                case 5:
                    colorsUsed[s] = DiscColor.YELLOW;
                    break;
                case 6:
                    colorsUsed[s] = DiscColor.BLUE;
                    break;
                case 7:
                    colorsUsed[s] = DiscColor.PINK;
            }
        int moveIndex;
        boolean overlappingDestinations, notEveryDiscCollides;
        do {
            overlappingDestinations = false;
            notEveryDiscCollides = false;
            try {
                Disc pointOfCollision;
                do
                    pointOfCollision = new Disc((int) (Math.random() * 4), (int) (Math.random() * 4), "nswe".charAt((int) (Math.random() * 4)), true, DiscColor.unused);
                while ("LR".indexOf(gridCode[pointOfCollision.getX()][pointOfCollision.getY()]) > -1);
                Disc virtualDisc;
                virtualDisc = pointOfCollision.clone();
                virtualDisc.setDirection('n');
                moveIndex = 0;
                while (moveIndex < 8) {
                    virtualDisc.collide(gridCode[virtualDisc.getX()][virtualDisc.getY()]);
                    virtualDisc.move();
                    moveIndex++;
                }
                discs[0] = new Disc(virtualDisc.getX(), virtualDisc.getY(), virtualDisc.getDirection(), true, colorsUsed[0]);
                virtualDisc = pointOfCollision.clone();
                virtualDisc.setDirection('s');
                moveIndex = 0;
                while (moveIndex < 8) {
                    virtualDisc.collide(gridCode[virtualDisc.getX()][virtualDisc.getY()]);
                    virtualDisc.move();
                    moveIndex++;
                }
                discs[1] = new Disc(virtualDisc.getX(), virtualDisc.getY(), virtualDisc.getDirection(), true, colorsUsed[1]);
                virtualDisc = pointOfCollision.clone();
                virtualDisc.setDirection('w');
                moveIndex = 0;
                while (moveIndex < 8) {
                    virtualDisc.collide(gridCode[virtualDisc.getX()][virtualDisc.getY()]);
                    virtualDisc.move();
                    moveIndex++;
                }
                discs[2] = new Disc(virtualDisc.getX(), virtualDisc.getY(), virtualDisc.getDirection(), true, colorsUsed[0]);
                virtualDisc = pointOfCollision.clone();
                virtualDisc.setDirection('e');
                moveIndex = 0;
                while (moveIndex < 8) {
                    virtualDisc.collide(gridCode[virtualDisc.getX()][virtualDisc.getY()]);
                    virtualDisc.move();
                    moveIndex++;
                }
                discs[3] = new Disc(virtualDisc.getX(), virtualDisc.getY(), virtualDisc.getDirection(), true, colorsUsed[1]);
                for (i = 0; i < 4; i++)
                    for (s = i + 1; s < 4; s++)
                        if (Disc.overlapping(discs[i], discs[s])) {
                            overlappingDestinations = true;
                            i = 4;
                            break;
                        }
                if (!computeDiscPath(discs[0], discs[1], null, null, true)) {
                    notEveryDiscCollides = true;
                    continue;
                }
                if (!computeDiscPath(discs[0], discs[2], null, null, true)) {
                    notEveryDiscCollides = true;
                    continue;
                }
                if (!computeDiscPath(discs[0], discs[3], null, null, true)) {
                    notEveryDiscCollides = true;
                    continue;
                }
                if (!computeDiscPath(discs[1], discs[2], null, null, true)) {
                    notEveryDiscCollides = true;
                    continue;
                }
                if (!computeDiscPath(discs[1], discs[3], null, null, true)) {
                    notEveryDiscCollides = true;
                    continue;
                }
                if (!computeDiscPath(discs[2], discs[3], null, null, true))
                    notEveryDiscCollides = true;
            } catch (CloneNotSupportedException cnse) {

            }
        } while (overlappingDestinations || notEveryDiscCollides);
        makeAvoid(discs[0], discs[2]);
    }

    public boolean move() {
        int d;
        short numActive = 0;
        for (d = 0; d < 4; d++) {
            if (discs[d].isActive())
                ++numActive;
            else
                continue;
            discs[d].move();
            discs[d].collide(gridCode[discs[d].getX()][discs[d].getY()]);
        }
        if (collision) {
            if (++freeCollisions == 2) {
                active = false;
                victorious = false;
            } else
                collision = false;
        }
        if (numActive == 4) {
            if (Disc.colliding(discs[0], discs[1], gridCode) || Disc.colliding(discs[2], discs[3], gridCode))
                collision = true;
        }
        if (Disc.colliding(discs[0], discs[2], gridCode)) {
            discs[0].deactivate();
            discs[2].deactivate();
            cycleEnabled = true;
        }
        if (Disc.colliding(discs[1], discs[3], gridCode)) {
            discs[1].deactivate();
            discs[3].deactivate();
            cycleEnabled = true;
        }
        if (numActive == 0 && !collision) {
            active = false;
            victorious = true;
            return false;
        }
        if (cycleEnabled)
            cycle++;
        else
            cycle = 0;
        if (cycle >= 5) {
            cycle = 0;
            if (numActive == 2) {
                if (discs[0].isActive())
                    makeAvoid(discs[0], discs[2]);
                else
                    makeAvoid(discs[1], discs[3]);
            } else if (((int) Math.random() * 2) == 1)
                computeDiscPath(discs[0], discs[1], discs[0], discs[2], false);
            else
                computeDiscPath(discs[2], discs[3], discs[0], discs[2], false);
            cycleEnabled = false;
        }
        return true;
    }

    private void randomizeReflectors() {
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                if (((int) (Math.random() * 2)) == 1)
                    invertReflector(i, j, false);
    }

    private boolean computeDiscPath(Disc A, Disc B, Disc C, Disc D, boolean testing) {
        if (!testing)
            randomizeReflectors();
        int[][] a = new int[8][8];
        int[][] b = new int[8][8];
        int i, j;
        for (i = 0; i < 8; i++)
            for (j = 0; j < 8; j++) {
                a[i][j] = 0;
                b[i][j] = 0;
            }
        int stepIndex;
        int u, v;
        Stack<Velocity2D> velocity2Ds = new Stack<Velocity2D>();
        char directionCode;
        // Compute information regarding A's path.
        stepIndex = 0;
        u = A.getX();
        v = A.getY();
        switch (A.getDirection()) {
            case 'n':
                do {
                    v = (v + 7) % 8;
                    a[u][v] = ++stepIndex;
                } while ("LR".indexOf(gridCode[u][v]) == -1);
                break;
            case 's':
                do {
                    v = (v + 1) % 8;
                    a[u][v] = ++stepIndex;
                } while ("LR".indexOf(gridCode[u][v]) == -1);
                break;
            case 'w':
                do {
                    u = (u + 7) % 8;
                    a[u][v] = ++stepIndex;
                } while ("LR".indexOf(gridCode[u][v]) == -1);
                break;
            case 'e':
                do {
                    u = (u + 1) % 8;
                    a[u][v] = ++stepIndex;
                } while ("LR".indexOf(gridCode[u][v]) == -1);
        }
        directionCode = "ns".indexOf(A.getDirection()) > -1 ? 'v' : 'h';
        do {
            final char originalDirectionCode = directionCode;
            stepIndex = a[u][v] + 1;
            if ("LR".indexOf(gridCode[u][v]) > -1)
                directionCode = directionCode == 'v' ? 'h' : 'v';
            switch (directionCode) {
                case 'v':
                    if ((a[u][(v + 7) % 8] == 0 || a[u][(v + 7) % 8] > stepIndex)
                            && !(u == A.getX() && (v + 7) % 8 == A.getY())) {
                        velocity2Ds.push(new Velocity2D(u, v, originalDirectionCode));
                        v = (v + 7) % 8;
                        a[u][v] = stepIndex;
                    } else if ((a[u][(v + 1) % 8] == 0 || a[u][(v + 1) % 8] > stepIndex)
                            && !(u == A.getX() && (v + 1) % 8 == A.getY())) {
                        velocity2Ds.push(new Velocity2D(u, v, originalDirectionCode));
                        v = (v + 1) % 8;
                        a[u][v] = stepIndex;
                    } else {
                        try {
                            final Velocity2D previousVelocity = velocity2Ds.pop();
                            u = previousVelocity.getX();
                            v = previousVelocity.getY();
                            directionCode = previousVelocity.getDirection();
                        } catch (EmptyStackException ese) {

                        }
                    }
                    break;
                case 'h':
                    if ((a[(u + 7) % 8][v] == 0 || a[(u + 7) % 8][v] > stepIndex)
                            && !((u + 7) % 8 == A.getX() && v == A.getY())) {
                        velocity2Ds.push(new Velocity2D(u, v, originalDirectionCode));
                        u = (u + 7) % 8;
                        a[u][v] = stepIndex;
                    } else if ((a[(u + 1) % 8][v] == 0 || a[(u + 1) % 8][v] > stepIndex)
                            && !((u + 1) % 8 == A.getX() && v == A.getY())) {
                        velocity2Ds.push(new Velocity2D(u, v, originalDirectionCode));
                        u = (u + 1) % 8;
                        a[u][v] = stepIndex;
                    } else {
                        try {
                            final Velocity2D previousVelocity = velocity2Ds.pop();
                            u = previousVelocity.getX();
                            v = previousVelocity.getY();
                            directionCode = previousVelocity.getDirection();
                        } catch (EmptyStackException ese) {

                        }
                    }
            }
        } while (!velocity2Ds.isEmpty());
        assert velocity2Ds.isEmpty();
        // Compute information regarding A's path.
        stepIndex = 0;
        u = B.getX();
        v = B.getY();
        switch (B.getDirection()) {
            case 'n':
                do {
                    v = (v + 7) % 8;
                    b[u][v] = ++stepIndex;
                } while ("LR".indexOf(gridCode[u][v]) == -1);
                break;
            case 's':
                do {
                    v = (v + 1) % 8;
                    b[u][v] = ++stepIndex;
                } while ("LR".indexOf(gridCode[u][v]) == -1);
                break;
            case 'w':
                do {
                    u = (u + 7) % 8;
                    b[u][v] = ++stepIndex;
                } while ("LR".indexOf(gridCode[u][v]) == -1);
                break;
            case 'e':
                do {
                    u = (u + 1) % 8;
                    b[u][v] = ++stepIndex;
                } while ("LR".indexOf(gridCode[u][v]) == -1);
        }
        directionCode = "ns".indexOf(B.getDirection()) > -1 ? 'v' : 'h';
        do {
            final char originalDirectionCode = directionCode;
            stepIndex = b[u][v] + 1;
            if ("LR".indexOf(gridCode[u][v]) > -1)
                directionCode = directionCode == 'v' ? 'h' : 'v';
            switch (directionCode) {
                case 'v':
                    if ((b[u][(v + 7) % 8] == 0 || b[u][(v + 7) % 8] > stepIndex)
                            && !(u == B.getX() && (v + 7) % 8 == B.getY())) {
                        velocity2Ds.push(new Velocity2D(u, v, originalDirectionCode));
                        v = (v + 7) % 8;
                        b[u][v] = stepIndex;
                    } else if ((b[u][(v + 1) % 8] == 0 || b[u][(v + 1) % 8] > stepIndex)
                            && !(u == B.getX() && (v + 1) % 8 == B.getY())) {
                        velocity2Ds.push(new Velocity2D(u, v, originalDirectionCode));
                        v = (v + 1) % 8;
                        b[u][v] = stepIndex;
                    } else {
                        try {
                            final Velocity2D previousVelocity = velocity2Ds.pop();
                            u = previousVelocity.getX();
                            v = previousVelocity.getY();
                            directionCode = previousVelocity.getDirection();
                        } catch (EmptyStackException ese) {

                        }
                    }
                    break;
                case 'h':
                    if ((b[(u + 7) % 8][v] == 0 || b[(u + 7) % 8][v] > stepIndex)
                            && !((u + 7) % 8 == B.getX() && v == B.getY())) {
                        velocity2Ds.push(new Velocity2D(u, v, originalDirectionCode));
                        u = (u + 7) % 8;
                        b[u][v] = stepIndex;
                    } else if ((b[(u + 1) % 8][v] == 0 || b[(u + 1) % 8][v] > stepIndex)
                            && !((u + 1) % 8 == B.getX() && v == B.getY())) {
                        velocity2Ds.push(new Velocity2D(u, v, originalDirectionCode));
                        u = (u + 1) % 8;
                        b[u][v] = stepIndex;
                    } else {
                        try {
                            final Velocity2D previousVelocity = velocity2Ds.pop();
                            u = previousVelocity.getX();
                            v = previousVelocity.getY();
                            directionCode = previousVelocity.getDirection();
                        } catch (EmptyStackException ese) {

                        }
                    }
            }
        } while (!velocity2Ds.isEmpty());
        assert velocity2Ds.isEmpty();
        int adjI, adjJ;
        for (i = 0; i < 8; i++)
            for (j = 0; j < 8; j++)
                for (adjI = i - 1; adjI <= i + 1; adjI++)
                    for (adjJ = j - 1; adjJ <= j + 1; adjJ++) {
                        if (!(i == adjI || j == adjJ))
                            continue;
                        if (a[i][j] == b[(adjI + 8) % 8][(adjJ + 8) % 8] && a[i][j] > 0) {
                            boolean valid = true;
                            char directionToNext = 0, previousDirection = 0;
                            char reflectorMovedFrom = 0;
                            boolean pathConstructionStarted = false;
                            LinkedList<Point2D> testDiscPathA = new LinkedList<Point2D>();
                            LinkedList<Point2D> testDiscPathB = new LinkedList<Point2D>();
                            // Construct path A;
                            u = i;
                            v = j;
                            testDiscPathA.add(new Point2D(u, v));
                            while (u != A.getX() || v != A.getY()) {
                                reflectorMovedFrom = gridCode[u][v];
                                if (a[u][v] == 1 && (u + 7) % 8 == A.getX() && v == A.getY()) {
                                    directionToNext = 'w';
                                    u = (u + 7) % 8;
                                } else if (a[u][v] == 1 && (u + 1) % 8 == A.getX() && v == A.getY()) {
                                    directionToNext = 'e';
                                    u = (u + 1) % 8;
                                } else if (a[u][v] == 1 && u == A.getX() && (v + 7) % 8 == A.getY()) {
                                    directionToNext = 'n';
                                    v = (v + 7) % 8;
                                } else if (a[u][v] == 1 && u == A.getX() && (v + 1) % 8 == A.getY()) {
                                    directionToNext = 's';
                                    v = (v + 1) % 8;
                                } else if (a[(u + 7) % 8][v] == a[u][v] - 1) {
                                    directionToNext = 'w';
                                    u = (u + 7) % 8;
                                } else if (a[(u + 1) % 8][v] == a[u][v] - 1) {
                                    directionToNext = 'e';
                                    u = (u + 1) % 8;
                                } else if (a[u][(v + 7) % 8] == a[u][v] - 1) {
                                    directionToNext = 'n';
                                    v = (v + 7) % 8;
                                } else if (a[u][(v + 1) % 8] == a[u][v] - 1) {
                                    directionToNext = 's';
                                    v = (v + 1) % 8;
                                } else {
                                    valid = false;
                                    break;
                                }
                                testDiscPathA.add(new Point2D(u, v));
                                if (pathConstructionStarted) {
                                    if (previousDirection != directionToNext && "LR".indexOf(reflectorMovedFrom) == -1) {
                                        valid = false;
                                        break;
                                    } else if (previousDirection == directionToNext && "LR".indexOf(reflectorMovedFrom) > -1) {
                                        valid = false;
                                        break;
                                    }
                                }
                                previousDirection = directionToNext;
                                pathConstructionStarted = true;
                            }
                            if (!valid)
                                continue;
                            // Construct path B;
                            pathConstructionStarted = false;
                            u = (adjI + 8) % 8;
                            v = (adjJ + 8) % 8;
                            testDiscPathB.add(new Point2D(u, v));
                            while (u != B.getX() || v != B.getY()) {
                                reflectorMovedFrom = gridCode[u][v];
                                if (b[u][v] == 1 && (u + 7) % 8 == B.getX() && v == B.getY()) {
                                    directionToNext = 'w';
                                    u = (u + 7) % 8;
                                } else if (b[u][v] == 1 && (u + 1) % 8 == B.getX() && v == B.getY()) {
                                    directionToNext = 'e';
                                    u = (u + 1) % 8;
                                } else if (b[u][v] == 1 && u == B.getX() && (v + 7) % 8 == B.getY()) {
                                    directionToNext = 'n';
                                    v = (v + 7) % 8;
                                } else if (b[u][v] == 1 && u == B.getX() && (v + 1) % 8 == B.getY()) {
                                    directionToNext = 's';
                                    v = (v + 1) % 8;
                                } else if (b[(u + 7) % 8][v] == b[u][v] - 1) {
                                    directionToNext = 'w';
                                    u = (u + 7) % 8;
                                } else if (b[(u + 1) % 8][v] == b[u][v] - 1) {
                                    directionToNext = 'e';
                                    u = (u + 1) % 8;
                                } else if (b[u][(v + 7) % 8] == b[u][v] - 1) {
                                    directionToNext = 'n';
                                    v = (v + 7) % 8;
                                } else if (b[u][(v + 1) % 8] == b[u][v] - 1) {
                                    directionToNext = 's';
                                    v = (v + 1) % 8;
                                } else {
                                    valid = false;
                                    break;
                                }
                                testDiscPathB.add(new Point2D(u, v));
                                if (pathConstructionStarted) {
                                    if (previousDirection != directionToNext && "LR".indexOf(reflectorMovedFrom) == -1) {
                                        valid = false;
                                        break;
                                    } else if (previousDirection == directionToNext && "LR".indexOf(reflectorMovedFrom) > -1) {
                                        valid = false;
                                        break;
                                    }
                                }
                                previousDirection = directionToNext;
                                pathConstructionStarted = true;
                            }
                            if (!valid)
                                continue;
                            Collections.reverse(testDiscPathA);
                            Collections.reverse(testDiscPathB);
                            try {
                                final Point2DPath pathA = new Point2DPath(testDiscPathA, gridCode, A.getDirection());
                                final Point2DPath pathB = new Point2DPath(testDiscPathB, gridCode, B.getDirection());
                                if (!pathA.contradicts(pathB)) {
                                    final Point2D a_last = pathA.finalPoint();
                                    final Point2D b_last = pathB.finalPoint();
                                    final Disc A_after_path = new Disc(a_last.getX(), a_last.getY(), pathA.finalDirection(), true, DiscColor.unused);
                                    final Disc B_after_path = new Disc(b_last.getX(), b_last.getY(), pathB.finalDirection(), true, DiscColor.unused);
                                    if (Disc.colliding(A_after_path, B_after_path, gridCode)) {
                                        if (testing)
                                            return true;
                                        makeAvoid(C, D, pathA, pathB);
                                        final int n = a[i][j];
                                        final LinkedList<Character> reqA = pathA.getRequiredPostionsAtPoints();
                                        final LinkedList<Character> reqB = pathB.getRequiredPostionsAtPoints();
                                        int s;
                                        for (s = 0; s < n; s++) {
                                            final Point2D a_p = testDiscPathA.get(s);
                                            final Point2D b_p = testDiscPathB.get(s);
                                            final char r_a = reqA.get(s);
                                            final char r_b = reqB.get(s);
                                            if (r_a != 'I')
                                                gridCode[a_p.getX()][a_p.getY()] = r_a;
                                            if (r_b != 'I')
                                                gridCode[b_p.getX()][b_p.getY()] = r_b;
                                        }
                                        return true;
                                    }
                                }
                            } catch (BrokenPathException bpe) {

                            }
                        }
                    }
        return false;
    }

    public boolean invertReflector(int x, int y, boolean userAction) throws ArrayIndexOutOfBoundsException {
        if (x < 0 || x > 7 || y < 0 || y > 7)
            throw new ArrayIndexOutOfBoundsException("Out of the boundaries of the grid");
        gridCode[x][y] = gridCode[x][y] == 'L' ? 'R' : gridCode[x][y] == 'R' ? 'L' : 'O';
        if (userAction)
            cycleEnabled = true;
        return "LR".indexOf(gridCode[x][y]) > -1;
    }

    private boolean makeAvoid(Disc A, Disc B) {
        short trial = 0;
        while (collisionCourseExists(A, B) && trial++ < 16)
            randomizeReflectors();
        if (collisionCourseExists(A, B))
            return false;
        return true;
    }

    private boolean makeAvoid(Disc A, Disc B, Point2DPath C, Point2DPath D) {
        short trial = 0;
        while (collisionCourseExists(A, B) && trial++ < 16)
            randomizeReflectorsWithRestrictions(C, D);
        if (collisionCourseExists(A, B))
            return false;
        return true;
    }

    private void randomizeReflectorsWithRestrictions(Point2DPath C, Point2DPath D) {
        boolean[][] canBeInverted = new boolean[8][8];
        int i, j;
        for (i = 0; i < 8; i++)
            for (j = 0; j < 8; j++)
                canBeInverted[i][j] = true;
        final LinkedList<Point2D> cPoints = C.getPoint2Ds();
        for (Point2D point2D : cPoints)
            canBeInverted[point2D.getX()][point2D.getY()] = false;
        final LinkedList<Point2D> dPoints = D.getPoint2Ds();
        for (Point2D point2D : dPoints)
            canBeInverted[point2D.getX()][point2D.getY()] = false;
        for (i = 0; i < 8; i++)
            for (j = 0; j < 8; j++)
                if (canBeInverted[i][j])
                    if ((int) (Math.random() * 2) == 1)
                        invertReflector(i, j, false);
    }

    private boolean collisionCourseExists(Disc A, Disc B) {
        int i, j;
        char direction;
        List<Velocity2D> pathA = new LinkedList<Velocity2D>();
        pathA.add(new Velocity2D(A.getX(), A.getY(), A.getDirection()));
        i = A.getX();
        j = A.getY();
        direction = A.getDirection();
        do {
            switch (direction) {
                case 'n':
                    j = (j + 7) % 8;
                    break;
                case 's':
                    j = (j + 1) % 8;
                    break;
                case 'w':
                    i = (i + 7) % 8;
                    break;
                case 'e':
                    i = (i + 1) % 8;
            }
            switch (gridCode[i][j]) {
                case 'L':
                    switch (direction) {
                        case 'n':
                            direction = 'w';
                            break;
                        case 's':
                            direction = 'e';
                            break;
                        case 'w':
                            direction = 'n';
                            break;
                        case 'e':
                            direction = 's';
                    }
                    break;
                case 'R':
                    switch (direction) {
                        case 'n':
                            direction = 'e';
                            break;
                        case 's':
                            direction = 'w';
                            break;
                        case 'w':
                            direction = 's';
                            break;
                        case 'e':
                            direction = 'n';
                    }
            }
            pathA.add(new Velocity2D(i, j, direction));
        } while (!pathA.get(pathA.size() - 1).coincides(pathA.get(0)));
        pathA.remove(pathA.size() - 1);

        List<Velocity2D> pathB = new LinkedList<Velocity2D>();
        pathB.add(new Velocity2D(B.getX(), B.getY(), B.getDirection()));
        i = B.getX();
        j = B.getY();
        direction = B.getDirection();
        do {
            switch (direction) {
                case 'n':
                    j = (j + 7) % 8;
                    break;
                case 's':
                    j = (j + 1) % 8;
                    break;
                case 'w':
                    i = (i + 7) % 8;
                    break;
                case 'e':
                    i = (i + 1) % 8;
            }
            switch (gridCode[i][j]) {
                case 'L':
                    switch (direction) {
                        case 'n':
                            direction = 'w';
                            break;
                        case 's':
                            direction = 'e';
                            break;
                        case 'w':
                            direction = 'n';
                            break;
                        case 'e':
                            direction = 's';
                    }
                    break;
                case 'R':
                    switch (direction) {
                        case 'n':
                            direction = 'e';
                            break;
                        case 's':
                            direction = 'w';
                            break;
                        case 'w':
                            direction = 's';
                            break;
                        case 'e':
                            direction = 'n';
                    }
            }
            pathB.add(new Velocity2D(i, j, direction));
        } while (!pathB.get(pathB.size() - 1).coincides(pathB.get(0)));
        pathB.remove(pathB.size() - 1);
        int cycleIndex;
        int t;
        if (pathA.size() > pathB.size()) {
            cycleIndex = 0;
            do {
                for (t = cycleIndex; t < cycleIndex + pathB.size(); t++) {
                    final Velocity2D velA = pathA.get(t % pathA.size());
                    final Velocity2D velB = pathB.get(t - cycleIndex);
                    if (Disc.colliding(new Disc(velA.getX(), velA.getY(), velA.getDirection(), true, DiscColor.unused),
                            new Disc(velB.getX(), velB.getY(), velB.getDirection(), true, DiscColor.unused), gridCode))
                        return true;
                }
                cycleIndex += pathB.size();
                cycleIndex %= pathA.size();
            } while (cycleIndex > 0);
        } else {
            cycleIndex = 0;
            do {
                for (t = cycleIndex; t < cycleIndex + pathA.size(); t++) {
                    final Velocity2D velB = pathB.get(t % pathB.size());
                    final Velocity2D velA = pathA.get(t - cycleIndex);
                    if (Disc.colliding(new Disc(velB.getX(), velB.getY(), velB.getDirection(), true, DiscColor.unused),
                            new Disc(velA.getX(), velA.getY(), velA.getDirection(), true, DiscColor.unused), gridCode))
                        return true;
                }
                cycleIndex += pathA.size();
                cycleIndex %= pathB.size();
            } while (cycleIndex > 0);
        }
        return false;
    }

    public char[][] getGridCode() {
        return gridCode;
    }

    public Disc[] getDiscs() {
        return discs;
    }

    public boolean isActive() {
        return active;
    }

    public int getCycle() {
        return cycle;
    }

    public boolean isVictorious() {
        return victorious;
    }

}

final class Velocity2D {

    private final Point2D mPoint2D;
    private final char direction;

    public Velocity2D(int X, int Y, char direction) {
        mPoint2D = new Point2D(X, Y);
        this.direction = direction;
    }

    public boolean coincides(Velocity2D velocity2D) {
        return getX() == velocity2D.getX() && getY() == velocity2D.getY()
                && direction == velocity2D.getDirection();
    }

    public int getX() {
        return mPoint2D.getX();
    }

    public int getY() {
        return mPoint2D.getY();
    }

    public char getDirection() {
        return direction;
    }
}

final class Point2D {

    private final int X;
    private final int Y;

    public Point2D(int X, int Y) {
        this.X = X;
        this.Y = Y;
    }

    public static char relativeToAdjacent(Point2D A, Point2D B) throws NonAdjacentRelativityComparisonException {
        if (!(A.getX() == B.getX() || A.getY() == B.getY()))
            throw new NonAdjacentRelativityComparisonException("They are not comparable.");
        if (A.getX() == B.getX()) {
            if (A.getY() == (B.getY() + 7) % 8)
                return 'n';
            else if (A.getY() == (B.getY() + 1) % 8)
                return 's';
            else
                throw new NonAdjacentRelativityComparisonException("They are not adjacent in a two dimensional grid.");
        } else {
            if (A.getX() == (B.getX() + 7) % 8)
                return 'w';
            else if (A.getX() == (B.getX() + 1) % 8)
                return 'e';
            else
                throw new NonAdjacentRelativityComparisonException("They are not adjacent in a two dimensional grid.");
        }
    }

    public static boolean overlapping(Point2D A, Point2D B) {
        return A.getX() == B.getX() && A.getY() == B.getY();
    }

    public int getX() {
        return X;
    }

    public int getY() {
        return Y;
    }

}

final class Point2DPath {

    private static final String TAG = Point2D.class.getSimpleName();

    private LinkedList<Character> directionsUsed;
    protected final LinkedList<Point2D> mPoint2Ds;
    private final char[][] gridCode;
    private LinkedList<Character> requiredPostionsAtPoints;

    public Point2DPath(LinkedList<Point2D> mPoint2Ds, char[][] gridCode, char initialDirection) throws BrokenPathException {
        this.mPoint2Ds = mPoint2Ds;
        final int n = mPoint2Ds.size();
        this.gridCode = gridCode;
        directionsUsed = new LinkedList<Character>();
        int p;
        for (p = 0; p < n - 1; p++)
            try {
                directionsUsed.add(Point2D.relativeToAdjacent(mPoint2Ds.get(p + 1), mPoint2Ds.get(p)));
            } catch (NonAdjacentRelativityComparisonException narce) {
                throw new BrokenPathException("The path is broken.");
            }
        requiredPostionsAtPoints = new LinkedList<Character>();
        requiredPostionsAtPoints.add('I');
        char previousDirection = initialDirection, currentDirection;
        for (p = 1; p < n - 1; p++) {
            currentDirection = directionsUsed.get(p);
            if (currentDirection != previousDirection) {
                requiredPostionsAtPoints.add(Disc.requiredPositionForDirectionChange(previousDirection, currentDirection));
                previousDirection = currentDirection;
            } else
                requiredPostionsAtPoints.add('I');
        }
    }

    public char finalDirection() {
        final char d = directionsUsed.getLast();
        switch (gridCode[mPoint2Ds.getLast().getX()][mPoint2Ds.getLast().getY()]) {
            case 'R':
                switch (d) {
                    case 'n':
                        return 'e';
                    case 's':
                        return 'w';
                    case 'w':
                        return 's';
                    case 'e':
                        return 'n';
                }
                break;
            case 'L':
                switch (d) {
                    case 'n':
                        return 'w';
                    case 's':
                        return 'e';
                    case 'w':
                        return 'n';
                    case 'e':
                        return 's';
                }
                break;
        }
        return d;
    }

    public boolean contradicts(Point2DPath P2DP) {
        final List<Character> bReq = P2DP.getRequiredPostionsAtPoints();
        assert requiredPostionsAtPoints.size() == bReq.size();
        final int n = mPoint2Ds.size();
        int p, w;
        for (p = 0; p < n - 1; p++)
            for (w = p + 1; w < n - 1; w++) {
                final Point2D A = mPoint2Ds.get(p);
                final Point2D B = mPoint2Ds.get(w);
                if (Point2D.overlapping(A, B)) {
                    if (!(bReq.get(w) == requiredPostionsAtPoints.get(p)) && requiredPostionsAtPoints.get(p) != 'I')
                        return true;
                }
            }
        return false;
    }

    public LinkedList<Character> getDirectionsUsed() {
        return directionsUsed;
    }

    public Point2D finalPoint() {
        return mPoint2Ds.getLast();
    }

    public LinkedList<Character> getRequiredPostionsAtPoints() {
        return requiredPostionsAtPoints;
    }

    public LinkedList<Point2D> getPoint2Ds() {
        return mPoint2Ds;
    }
}

final class NonAdjacentRelativityComparisonException extends ArithmeticException {

    public NonAdjacentRelativityComparisonException(String narce) {
        super(narce);
    }
}

final class BrokenPathException extends Exception {

    public BrokenPathException(String bpe) {
        super(bpe);
    }
}
