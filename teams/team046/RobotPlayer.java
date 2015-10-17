package team046;

import battlecode.common.*;

public class RobotPlayer {

    private static RobotController rc;
    private static int round;
    private static double power;

    // LOL
    private static int buildChannelX = randomWithRange(0, GameConstants.BROADCAST_MAX_CHANNELS);
    private static int buildChannelY = randomWithRange(0, GameConstants.BROADCAST_MAX_CHANNELS);
    private static int buildChannelZ = randomWithRange(0, GameConstants.BROADCAST_MAX_CHANNELS);
    private static int defendChannelX = randomWithRange(0, GameConstants.BROADCAST_MAX_CHANNELS);
    private static int defendChannelY = randomWithRange(0, GameConstants.BROADCAST_MAX_CHANNELS);
    private static int topSecretResearchChannel = randomWithRange(0, GameConstants.BROADCAST_MAX_CHANNELS);

	public static void run(RobotController MyJohn12LongRC) {
        rc = MyJohn12LongRC;

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
				// End turn
				rc.yield();
			}
            catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

    private static void HQ() throws GameActionException {
        if (rc.isActive()) {
            MapLocation hqLoc = rc.getLocation();
            MapLocation targetLoc;

            // Check for a build objective
            int bCZ = rc.readBroadcast(buildChannelZ);

            if (round == 0) {
                targetLoc = setBuildTarget(hqLoc);
            }
            else if (rc.readBroadcast(topSecretResearchChannel) == 1) {
                rc.broadcast(topSecretResearchChannel, 0);
                rc.researchUpgrade(Upgrade.NUKE);
                return;
            }
            else {
                if (bCZ == GameConstants.ROUND_MAX_LIMIT) {
                    targetLoc = rc.senseEnemyHQLocation();
                }
                else if (round - bCZ >= GameConstants.CAPTURE_ROUND_DELAY) {
                    rc.broadcast(buildChannelZ, 0);
                    targetLoc = setBuildTarget(hqLoc);
                }
                else {
                    // Research upgrades
                    if (!rc.hasUpgrade(Upgrade.FUSION)) {
                        rc.researchUpgrade(Upgrade.FUSION);
                        return;
                    } else if (!rc.hasUpgrade(Upgrade.DEFUSION)) {
                        rc.researchUpgrade(Upgrade.DEFUSION);
                        return;
                    }
                    else {
                        targetLoc = rc.senseEnemyHQLocation();
                    }
                }
            }

            // Find an available spawn direction
            Direction dir = hqLoc.directionTo(targetLoc);
            while (!rc.canMove(dir)) {
                dir = dir.rotateRight();
            }
            rc.spawn(dir);

            rc.broadcast(topSecretResearchChannel, 1);
        }
    }

    private static void Soldier() throws GameActionException {
        if (rc.isActive()) {
            MapLocation rLoc = rc.getLocation();
            MapLocation targetLoc;
            MapLocation nextLoc;

            // Check for build objective
            int bCZ = rc.readBroadcast(buildChannelZ);
            if (bCZ == 0 & rc.senseCaptureCost() < power) {
                targetLoc = new MapLocation(rc.readBroadcast(buildChannelX), rc.readBroadcast(buildChannelY));
                if (rLoc.equals(targetLoc)) {
                    rc.captureEncampment(RobotType.MEDBAY);
                    //rc.broadcast(defendChannelX, targetLoc.x);
                    //rc.broadcast(defendChannelY, targetLoc.y);
                    rc.broadcast(buildChannelZ, round);
                    return;
                }

            }
            else {
                //targetLoc = new MapLocation(rc.readBroadcast(defendChannelX), rc.readBroadcast(defendChannelY));
                targetLoc = rc.senseEnemyHQLocation();
            }

            // Find an available movement direction
            Direction dir = rLoc.directionTo(targetLoc);
            while (!rc.canMove(dir)) {
                dir = dir.rotateRight();
            }

            nextLoc = rLoc.add(dir);

            if (rc.senseMine(nextLoc) != null) {
                rc.defuseMine(nextLoc);
            }
            else {
                rc.move(dir);
            }
        }
    }

    private static MapLocation setBuildTarget(MapLocation hqLoc) throws GameActionException {
        MapLocation targetLocs[] = rc.senseEncampmentSquares(hqLoc, 200, Team.NEUTRAL);
        MapLocation targetLoc;

        if (targetLocs.length > 0) {
            int shortest = 1000;
            targetLoc = targetLocs[0];
            for (MapLocation l: targetLocs) {
                int distTo = hqLoc.distanceSquaredTo(l);
                if (distTo < shortest) {
                    targetLoc = l;
                    shortest = distTo;
                }
            }
            rc.broadcast(buildChannelX, targetLoc.x);
            rc.broadcast(buildChannelY, targetLoc.y);
        }
        else {
            targetLoc = rc.senseEnemyHQLocation();
            rc.broadcast(buildChannelX, 0);
            rc.broadcast(buildChannelY, 0);
            rc.broadcast(buildChannelZ, GameConstants.ROUND_MAX_LIMIT);
        }
        return targetLoc;
    }

    private static int randomWithRange(int min, int max) {
        int range = Math.abs(max - min) + 1;
        return (int)(Math.random() * range) + (min <= max ? min : max);
    }
}
