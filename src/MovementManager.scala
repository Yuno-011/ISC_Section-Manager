import characters.{Direction, Hero, NPC}
import com.badlogic.gdx.Input

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
  }

  /**
   * Manage the movements of the NPCs using random movement.
   * @param npcs
   * @param elapsedTime
   */
  def manageNPCs(npcs: ArrayBuffer[NPC], elapsedTime: Double): Unit = {
    for(npc <- npcs) {
      if(!npc.isMoving) {
        if(npc.mustMoveNow(elapsedTime)) {
          val move = npc.nextMove()
          if(mapManager.isWalkable(mapManager.getNextCell(npc.getPosition, move))) npc.go(move)
          npc.turn(move)
        }
      }
    }
  }
}
