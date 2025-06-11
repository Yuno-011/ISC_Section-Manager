package characters

import ch.hevs.gdx2d.components.bitmaps.Spritesheet
import ch.hevs.gdx2d.lib.GdxGraphics
import ch.hevs.gdx2d.lib.interfaces.DrawableObject
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.{Interpolation, Vector2}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Direction extends Enumeration {
  type Direction = Value
  val UP, DOWN, RIGHT, LEFT, NULL = Value
}

class MovingEntity(spritesheetPath: String, initialPos: Vector2) extends DrawableObject {

  private var textureY = 0
  private var currentFrame = 0
  private val FRAME_TIME = 0.1f // Duration of each frime

  private var dt: Double = 0
  private val nFrames = 4

  protected var SPEED: Double = 1
  protected var MAX_HEALTH: Int = 1
  private var health: Int = MAX_HEALTH
  private val ATTACK_RANGE: Int = 1

  private var lastPosition: Vector2 = initialPos
  private var newPosition: Vector2 = initialPos
  private var position: Vector2 = initialPos
  private var move = false
  private var atk = false
  private var hit = false

  private val SPRITE_WIDTH, SPRITE_HEIGHT = 48
  private var ss: Spritesheet = new Spritesheet(spritesheetPath, SPRITE_WIDTH, SPRITE_HEIGHT)

  /**
   * Getter for the hero's position (Vector2)
   * @return Vector2 - the hero's position
   */
  def getPosition: Vector2 = position

  def getLastPosition: Vector2 = lastPosition

  def getAttackRange: Int = ATTACK_RANGE

  /**
   * Getter for movement state of the hero
   * @return Boolean - true if the hero is currently moving
   */
  def isMoving: Boolean = move

  def isAttacking: Boolean = atk

  def isAlive: Boolean = health > 0

  def isInvincible: Boolean = hit

  /**
   * Animates the hero's frames and movement
   * @param elapsedTime the time that has passed since the last animate
   */
  def animate(elapsedTime: Double): Unit = {
    val frameTime = FRAME_TIME / SPEED
    position = new Vector2(lastPosition)
    if (isMoving || isAttacking) {
      dt += elapsedTime
      val alpha = (dt + frameTime * currentFrame) / (frameTime * nFrames)
      position.interpolate(newPosition, alpha.toFloat, Interpolation.linear)
    }
    else dt = 0

    if (dt > frameTime) {
      dt -= frameTime
      currentFrame = (currentFrame + 1) % nFrames
      if (currentFrame == 0) {
        if(isAttacking) textureY = 1
        move = false
        atk = false
        lastPosition = new Vector2(newPosition)
        position = new Vector2(newPosition)
      }
    }
  }

  /**
   * Do a step on the given direction
   *
   * @param direction The direction to go.
   */
  def go(direction: Direction.Direction): Unit = {
    move = true
    direction match {
      case Direction.RIGHT =>
        newPosition.add(SPRITE_WIDTH, 0)

      case Direction.LEFT =>
        newPosition.add(-SPRITE_WIDTH, 0)

      case Direction.UP =>
        newPosition.add(0, SPRITE_HEIGHT)

      case Direction.DOWN =>
        newPosition.add(0, -SPRITE_HEIGHT)

      case _ =>

    }
    turn(direction)
  }

  /**
   * Turn the hero on the given direction without do any step.
   *
   * @param direction The direction to turn.
   */
  def turn(direction: Direction.Direction): Unit = {
    direction match {
      case Direction.RIGHT =>
        textureY = 2

      case Direction.LEFT =>
        textureY = 1

      case Direction.UP =>
        textureY = 3

      case Direction.DOWN =>
        textureY = 0

      case _ =>

    }
  }

  def setSpritesheet(spritesheetPath: String): Unit =
    ss = new Spritesheet(spritesheetPath, SPRITE_WIDTH, SPRITE_HEIGHT)

  def heal(damage: Int = MAX_HEALTH): Unit =
    health = Math.min(health + damage, MAX_HEALTH)

  def takeDamage(damage: Int): Unit = {
    if(!isInvincible) {
      health = Math.max(health - damage, 0)
      hit = true
      Future {
        Thread.sleep(2000)
        removeInvicibility()
      }
    }
  }

  private def removeInvicibility(): Unit = {
    hit = false
  }

  def attack(): Unit = {
    atk = true
    textureY = 4
  }

  override def draw(g: GdxGraphics): Unit = g.draw(ss.sprites(textureY)(currentFrame), position.x, position.y)

  def draw(batch: SpriteBatch): Unit = {
    val region = ss.sprites(textureY)(currentFrame)
    if(!isInvincible) batch.draw(region, position.x, position.y)
    else batch.draw(region, position.x, position.y, region.getRegionWidth, region.getRegionHeight*2/3)
  }
}
