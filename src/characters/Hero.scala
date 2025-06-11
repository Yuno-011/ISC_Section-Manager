package characters
import com.badlogic.gdx.math.Vector2

class Hero(initialPos: Vector2) extends MovingEntity("data/images/mudry_sheet48.png", initialPos) {
  SPEED = 1.5
  MAX_HEALTH = 20
  ATTACK_RANGE = 2
  private var _score: Int = 0

  heal()

  def this(x: Int, y: Int) = {
    this(new Vector2(x*48, y*48))
  }

  def updateScore(points: Int): Unit = {
    _score += points
  }

  def score: Int = _score
}
