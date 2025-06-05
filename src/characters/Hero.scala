package characters
import com.badlogic.gdx.math.Vector2

class Hero(initialPos: Vector2) extends MovingEntity("data/images/mudry_sheet48.png", initialPos) {
  SPEED = 1.5

  def this(x: Int, y: Int) = {
    this(new Vector2(x*48, y*48))
  }
}
