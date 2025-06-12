import characters.{Direction, Evil, Hero, NPC}
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector2

import java.util
import scala.collection.mutable.ArrayBuffer

class MovementManager(private val mapManager: MapManager) {
  /**
   * Manage the movements of the hero using the keyboard.
   * @param hero
   * @param keyStatus
   */
  def manageHero(hero: Hero, keyStatus: util.Map[Integer, Boolean]): Unit = {
    // Do nothing if hero is already moving
    if (!hero.isMoving) {
      // Compute direction
      var goalDirection = Direction.NULL
      if (keyStatus.get(Input.Keys.D) || keyStatus.get(Input.Keys.RIGHT)) goalDirection = Direction.RIGHT
      else if (keyStatus.get(Input.Keys.A)|| keyStatus.get(Input.Keys.LEFT)) goalDirection = Direction.LEFT
      else if (keyStatus.get(Input.Keys.W)|| keyStatus.get(Input.Keys.UP)) goalDirection = Direction.UP
      else if (keyStatus.get(Input.Keys.S)|| keyStatus.get(Input.Keys.DOWN)) goalDirection = Direction.DOWN
      // Is the move valid ?
      if (mapManager.isWalkable(mapManager.getNextCell(hero.getPosition, goalDirection))) {
        hero.go(goalDirection)    // Go
      } else {
        hero.turn(goalDirection)  // Face the wall
      }
    }
    if(hero.codeLaser != null && !hero.codeLaser.isMoving) {
      if(hero.codeLaser.cellsPassed < hero.getAttackRange) hero.codeLaser.forward()
      else hero.codeLaser = null
    }
  }

  /**
   * Manage the movements of the NPCs using random movement.
   * @param npcs
   * @param elapsedTime
   */
  def manageNPCs(npcs: ArrayBuffer[NPC], hero: Hero, elapsedTime: Double): Unit = {
    for(npc <- npcs) {
      if(!npc.isMoving && !npc.isInvincible && npc.isAlive) {
        if(npc.mustMoveNow(elapsedTime)) {
          if(npc.isInstanceOf[Evil]) findFirstMoveToHero(npc.asInstanceOf[NPC with Evil], hero)
          val move = npc.nextMove()
          if(mapManager.isWalkable(mapManager.getNextCell(npc.getPosition, move))) npc.go(move)
          npc.turn(move)
        }
      }
    }
  }

  private def findFirstMoveToHero(npc: NPC with Evil, hero: Hero): Unit = {
    if(Math.abs(npc.getPosition.x - hero.getPosition.x) > 48
      || Math.abs(npc.getPosition.y - hero.getPosition.y) > 48) {

      if (Math.abs(npc.getPosition.x - hero.getPosition.x) <= npc.visionDistance * 48
        && Math.abs(npc.getPosition.y - hero.getPosition.y) <= npc.visionDistance * 48) {

        val mapValues: Array[Array[Int]] = Array.fill((mapManager.getWorldWidth / 48).toInt)(Array.fill((mapManager.getWorldHeight / 48).toInt)(-2)) // -2 equals no value yet
        mapValues((hero.getPosition.x / 48).toInt)((hero.getPosition.y / 48).toInt) = 0 // 0 for the player place
        val npcX: Int = (npc.getPosition.x / 48).toInt
        val npcY: Int = (npc.getPosition.y / 48).toInt
        mapValues(npcX)(npcY) = -1 // -1 for the npc place

        var foundNpc: Boolean = false

        def testAndAddValueToCell(x: Int, y: Int, current: Int): Unit = {
          if (mapValues.isDefinedAt(x)) if (mapValues(x).isDefinedAt(y)) {
            if (mapValues(x)(y) == -2) {
              mapValues(x)(y) = current + 1
            } else if (mapValues(x)(y) == -1) foundNpc = true
          }
        }

        // Fill the Array with Int values.
        // hero is 0 and then all the surrounding cells are 1, then all the surronding cells of the 1s are 2, ...
        var current: Int = 0
        do {
          for (x <- mapValues.indices) {
            for (y <- mapValues(x).indices) {
              if (mapValues(x)(y) == current) {
                for (dir <- Direction.values.filter(dir => dir != Direction.NULL)) {
                  if (mapManager.isWalkable(mapManager.getNextCell(new Vector2(x * 48, y * 48), dir))) {
                    dir match {
                      case Direction.RIGHT => testAndAddValueToCell(x + 1, y, current)
                      case Direction.LEFT => testAndAddValueToCell(x - 1, y, current)
                      case Direction.UP => testAndAddValueToCell(x, y + 1, current)
                      case Direction.DOWN => testAndAddValueToCell(x, y - 1, current)
                      case _ => // do nothing
                    }
                  }
                }
              }
            }
          }
          current += 1

        } while (!foundNpc)

        // Get all the values around the NPC
        val dirValues: ArrayBuffer[(Direction.Direction, Int)] = ArrayBuffer()
        for (dir <- Direction.values.filter(dir => dir != Direction.NULL)) {
          dir match {
            case Direction.RIGHT => if (mapValues.isDefinedAt(npcX + 1)) if (mapValues(npcX + 1).isDefinedAt(npcY)) dirValues += ((dir, mapValues(npcX + 1)(npcY)))
            case Direction.LEFT => if (mapValues.isDefinedAt(npcX - 1)) if (mapValues(npcX - 1).isDefinedAt(npcY)) dirValues += ((dir, mapValues(npcX - 1)(npcY)))
            case Direction.UP => if (mapValues.isDefinedAt(npcX)) if (mapValues(npcX).isDefinedAt(npcY + 1)) dirValues += ((dir, mapValues(npcX)(npcY + 1)))
            case Direction.DOWN => if (mapValues.isDefinedAt(npcX)) if (mapValues(npcX).isDefinedAt(npcY - 1)) dirValues += ((dir, mapValues(npcX)(npcY - 1)))
            case _ => // do nothing
          }
        }

        // get the first value that is equal to the current (npc placement value) -1
        val npcDir = dirValues.find(dirVal => dirVal._2 == current - 1)
        if (npcDir.isDefined) npc.addMoveToPlayer(npcDir.get._1) // add the direction to the NPC's moveList
      }
    }
  }
}
