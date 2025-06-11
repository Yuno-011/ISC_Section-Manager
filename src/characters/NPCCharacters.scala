package characters

import com.badlogic.gdx.math.Vector2

class Teacher(initialPos: Vector2) extends NPC("data/images/jaquemet_sheet48.png", initialPos) {
  POINT_VALUE = -5

  def this(x: Int, y: Int) = {
    this(new Vector2(48*x, 48*y))
  }
}

class Student(initialPos: Vector2) extends NPC("data/images/student_sheet48.png", initialPos) {

  def this(x: Int, y: Int) = {
    this(new Vector2(48*x, 48*y))
  }
}

trait Evil extends NPC {

  private val VISION_DISTANCE = 4

  POINT_VALUE = -POINT_VALUE
  if(isInstanceOf[Teacher]) {
    MAX_HEALTH = 20
    setSpritesheet("data/images/evil_jaquemet_sheet48.png")
  }
  else if (isInstanceOf[Student]) {
    MAX_HEALTH = 10
    setSpritesheet("data/images/evil_student_sheet48.png")
  }
  heal()

  def visionDistance: Int = VISION_DISTANCE

  override def mustMoveNow(elapsedTime: Double): Boolean = true

  def addMoveToPlayer(move: Direction.Direction): Unit = {
    if(move != Direction.NULL) movesToDo += move
  }
}