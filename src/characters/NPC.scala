package characters

import com.badlogic.gdx.math.Vector2

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class NPC(spritesheetPath: String, initialPos: Vector2) extends MovingEntity(spritesheetPath, initialPos) {

  private var timeSinceLastMove: Double = 0.0
  private var timeNextMove: Double = -1
  private val moveTimeInterval: (Double, Double) = (1.0, 3.0) // seconds
  private val moveDistInterval: (Int, Int) = (1, 3)
  protected val movesToDo: ArrayBuffer[Direction.Direction] = ArrayBuffer()

  protected var POINT_VALUE: Int = -1

  /**
   * Defines the next moves of the NPC
   * @param elapsedTime - the time that has passed since the last call
   * @return true if the NPC must move now
   */
  def mustMoveNow(elapsedTime: Double): Boolean = {
    if(isAlive) {
      if (movesToDo.nonEmpty) return true
      // Timer for random movement
      if(timeNextMove == -1) timeNextMove = moveTimeInterval._1 + (moveTimeInterval._2 - moveTimeInterval._1) * new Random().nextDouble()
      timeSinceLastMove += elapsedTime
      if (timeSinceLastMove >= timeNextMove) {
        timeNextMove = -1
        val rdmDist: Int = moveDistInterval._1 + new Random().nextInt((moveDistInterval._2 - moveDistInterval._1) + 1)
        val directions = Direction.values.filter(_ != Direction.NULL).toIndexedSeq
        val rdmDir: Direction.Direction = directions(Random.nextInt(directions.length))
        for(_ <- 0 until rdmDist) movesToDo += rdmDir
        return true
      }
      return false
    }
    false
  }

  def nextMove(): Direction.Direction = {
    var move = Direction.NULL
    if(movesToDo.nonEmpty) move = movesToDo.remove(0)
    if(movesToDo.isEmpty) timeSinceLastMove = 0.0
    move
  }

  def killPoints: Int = POINT_VALUE

  def this(spritesheetPath: String, x: Int, y: Int) = {
    this(spritesheetPath, new Vector2(x*48, y*48))
  }
}
