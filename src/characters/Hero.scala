package characters
import ch.hevs.gdx2d.components.bitmaps.Spritesheet
import ch.hevs.gdx2d.lib.GdxGraphics
import ch.hevs.gdx2d.lib.interfaces.DrawableObject
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.{Interpolation, Vector2}

class Projectile(initialPos: Vector2, dir: Direction.Direction) extends DrawableObject {

  private val textureY = dir match {
    case Direction.RIGHT => 2
    case Direction.LEFT => 1
    case Direction.UP => 3
    case Direction.DOWN => 0
    case _ => 0

  }
  private var dt: Double = 0
  private val SPEED: Double = 2

  private var lastPosition: Vector2 = initialPos
  private var newPosition: Vector2 = initialPos
  private var position: Vector2 = initialPos

  private var move = false
  private var cells: Int = 0

  private val SPRITE_WIDTH, SPRITE_HEIGHT = 48
  private val ss: Spritesheet = new Spritesheet("data/images/code-laser_sheet48.png", SPRITE_WIDTH, SPRITE_HEIGHT)

  /**
   * Getter for the projectile's position (Vector2)
   * @return Vector2 - the projectile's position
   */
  def getPosition: Vector2 = position

  def isMoving: Boolean = move

  def cellsPassed: Int = cells

  /**
   * Animates the hero's frames and movement
   * @param elapsedTime the time that has passed since the last animate
   */
  def animate(elapsedTime: Double): Unit = {
    val frameTime = 0.4f / SPEED
    position = new Vector2(lastPosition)
    if (isMoving) {
      dt += elapsedTime
      val alpha = dt / frameTime
      position.interpolate(newPosition, alpha.toFloat, Interpolation.linear)
    }
    else dt = 0

    if (dt > frameTime) {
      dt -= frameTime
      cells += 1
      move = false
      lastPosition = new Vector2(newPosition)
      position = new Vector2(newPosition)
    }
  }

  def forward(): Unit = {
    move = true
    dir match {
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
  }

  override def draw(g: GdxGraphics): Unit = g.draw(ss.sprites(textureY)(0), position.x, position.y)

  def draw(batch: SpriteBatch): Unit = {
    val region = ss.sprites(textureY)(0)
    batch.draw(region, position.x, position.y)
  }
}

class Hero(initialPos: Vector2) extends MovingEntity("data/images/mudry_sheet48.png", initialPos) {
  SPEED = 1.5
  MAX_HEALTH = 20
  ATTACK_RANGE = 10
  private var _score: Int = 0
  var codeLaser: Projectile = null

  heal()

  def this(x: Int, y: Int) = {
    this(new Vector2(x*48, y*48))
  }

  def updateScore(points: Int): Unit = {
    _score += points
  }

  def score: Int = _score

  override def attack(): Unit = {
    if(codeLaser == null) {
      if(getDirection != Direction.NULL) codeLaser = new Projectile(getPosition, getDirection)
      super.attack()
    }
  }
}
