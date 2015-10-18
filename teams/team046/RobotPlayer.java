package team046;

import battlecode.common.*;

import java.util.Arrays;

public class RobotPlayer {

    private static RobotController rc;
    private static int round;
    private static double power;
    private static MapLocation rallyPoint;
    private static int zergRushChannel = randomWithRange(0, GameConstants.BROADCAST_MAX_CHANNELS);

	public static void run(RobotController MyJohn12LongRC) {
        rc = MyJohn12LongRC;

        // Set rally point
        if (rallyPoint == null) {
            MapLocation goodHQ = rc.senseHQLocation();
            MapLocation badHQ = rc.senseEnemyHQLocation();
            MapLocation midPoint = new MapLocation((badHQ.x + goodHQ.x) / 2, (badHQ.y + goodHQ.y) / 2);
            MapLocation qPoint = new MapLocation((midPoint.x + goodHQ.x) / 2, (midPoint.y + goodHQ.y) / 2);
            rallyPoint = new MapLocation((qPoint.x + goodHQ.x)/2, (qPoint.y + goodHQ.y)/2);
        }

		while (true) {
            try {
                round = Clock.getRoundNum();
                power = rc.getTeamPower();

				if (rc.getType() == RobotType.HQ) {
					HQ();
				}
                else if (rc.getType() == RobotType.SOLDIER) {
                    Soldier();
				}

				rc.yield();
			}
            catch (Exception e) {
				e.printStackTrace();
                break;
			}
		}
	}

    private static void HQ() throws GameActionException {
        if (rc.isActive()) {


            //Robot[] friendlyRobots = rc.senseNearbyGameObjects(Robot.class, rallyPoint, 100, rc.getTeam());
            //System.out.println(Arrays.deepToString(friendlyRobots));
            //System.out.println(friendlyRobots.length);
            if (rc.senseEnemyNukeHalfDone()) {
                rc.broadcast(zergRushChannel, 1);
            }
           /* else if (friendlyRobots.length >= 15) {
                rc.researchUpgrade(Upgrade.NUKE);
                return;
            }*/
            else if (round > 750  && !rc.hasUpgrade(Upgrade.FUSION)) {
                rc.researchUpgrade(Upgrade.FUSION);
                return;
            }
            else if (round > 1000  && !rc.hasUpgrade(Upgrade.DEFUSION)) {
                rc.researchUpgrade(Upgrade.DEFUSION);
                return;
            }
            else if (round > 1250 && !rc.hasUpgrade(Upgrade.VISION)) {
                rc.researchUpgrade(Upgrade.VISION);
                return;
            }

            // Find an available spawn direction
            for (Direction dir : Direction.values()) {
                if (dir != Direction.NONE && dir != Direction.OMNI && rc.canMove(dir)) {
                    rc.spawn(dir);
                    break;
                }
            }
        }
    }

    private static void Soldier() throws GameActionException {
        if (rc.isActive()) {
            MapLocation rLoc = rc.getLocation();

            // Set default rally point
            MapLocation targetLoc = rallyPoint;

            // Check for build objective
            if (power > GameConstants.BROADCAST_READ_COST) {
                if (rc.readBroadcast(zergRushChannel) == 1) {
                    targetLoc = rc.senseEnemyHQLocation();
                }
                else {
                    // Get scared
                    Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 3, rc.getTeam().opponent());
                    if (nearbyEnemies.length > 0) {
                        return;
                    }
                }
            }

            // Find an available movement direction
            Direction dir = rLoc.directionTo(targetLoc);
            if (dir == Direction.NONE) {
                return;
            }
            else if (dir == Direction.OMNI) {
                dir = Direction.EAST;
            }
            while (!rc.canMove(dir)) {
                dir = dir.rotateRight();
            }

            MapLocation nextLoc = rLoc.add(dir);

            if (rc.senseMine(nextLoc) != null) {
                rc.defuseMine(nextLoc);
            }
            else {
                rc.move(dir);
            }
        }
    }

    private static int randomWithRange(int min, int max) {
        int range = Math.abs(max - min) + 1;
        return (int)(Math.random() * range) + (min <= max ? min : max);
    }
}
