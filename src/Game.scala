import ch.hevs.gdx2d.desktop.PortableApplication
import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.{Gdx, Input}
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.{TiledMap, TiledMapRenderer, TiledMapTile, TiledMapTileLayer, TmxMapLoader}
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2

import java.util
import scala.collection.mutable.ArrayBuffer

class Game extends PortableApplication(1080, 1080) {
  private final val TAG: String = "ISC: Fatal Error"
  private val ZOOM: Float = 0.5f
  private val HERO_LAYER: Int = 2

  private var screenHeight, screenWidth = 0
  private var spriteBatch: SpriteBatch = null
  private var tiledMap: TiledMap = null
  private var tiledMapRenderer: TiledMapRenderer = null
  private var tiledLayers: ArrayBuffer[TiledMapTileLayer] = ArrayBuffer()

  private var hero: Hero = null

  private val keyStatus: util.Map[Integer, Boolean] = new util.TreeMap[Integer, Boolean]

  override def onInit(): Unit = {
    setTitle("ISC: Section Manager")
    screenWidth = getWindowWidth
    screenHeight = getWindowHeight

    hero = new Hero(10,10)

    tiledMap = new TmxMapLoader().load("data/map_data/maps/test.tmx")
    spriteBatch = new SpriteBatch()
    tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, spriteBatch)
    for(i <- 0 until tiledMap.getLayers.getCount) {
      tiledLayers += tiledMap.getLayers.get(i).asInstanceOf[TiledMapTileLayer]
    }
  }

  override def onGraphicRender(g: GdxGraphics): Unit = {
    g.clear()

    // Manage hero movement
    manageHero()

    // zoom and camera
    g.zoom(ZOOM)
    g.moveCamera(hero.getPosition.x, hero.getPosition.y, tiledLayers(0).getWidth * tiledLayers(0).getTileWidth, tiledLayers(0).getHeight * tiledLayers(0).getTileHeight)
    tiledMapRenderer.setView(g.getCamera)

    // Start drawing
    spriteBatch.begin()
    // draw first layers
    val layers = tiledMap.getLayers
    for(i <- 0 until HERO_LAYER) {
      tiledMapRenderer.renderTileLayer(layers.get(i).asInstanceOf[TiledMapTileLayer])
    }
    // draw hero
    hero.animate(Gdx.graphics.getDeltaTime)
    hero.draw(spriteBatch)
    // draw last layers
    for(i <- HERO_LAYER until layers.getCount) {
      tiledMapRenderer.renderTileLayer(layers.get(i).asInstanceOf[TiledMapTileLayer])
    }
    // Finish drawing
    spriteBatch.end()

    g.drawFPS()
  }

  /**
   * exemple : getTile(myPosition,0,1) get the tile over myPosition
   *
   * @param position
   * The position on map (not on screen)
   * @param offsetX
   * The number of cells at right of the given position.
   * @param offsetY
   * The number of cells over the given position.
   * @return The tile around the given position | null
   */
  private def getTiles(position: Vector2, offsetX: Int, offsetY: Int): ArrayBuffer[TiledMapTile] = {
    val tiles: ArrayBuffer[TiledMapTile] = ArrayBuffer()
    for(i <- tiledLayers.indices) {
      tiles.append(try {
        val x = (position.x / tiledLayers(i).getTileWidth).toInt + offsetX
        val y = (position.y / tiledLayers(i).getTileHeight).toInt + offsetY
        tiledLayers(i).getCell(x, y).getTile
      } catch {
        case e: Exception =>
          null
      })
    }
    tiles
  }

  /**
   * Get the "walkable" property of the given tile.
   *
   * @param cell
   * The cell to know the property
   * @return true if the property is set to "true", false otherwise
   */
  private def isWalkable(cell: ArrayBuffer[TiledMapTile]): Boolean = {
    if(cell.forall(tile => tile == null)) return false
    for(tile <- cell) {
      if (tile != null) {
        val test = tile.getProperties.get("walkable")
        if(test != null && test.toString == "false") return false
      }
    }
    true
  }

  /**
   * Manage the movements of the hero using the keyboard.
   */
  private def manageHero(): Unit = {
    // Do nothing if hero is already moving
    if (!hero.isMoving) {
      // Compute direction and next cell
      var nextCell: ArrayBuffer[TiledMapTile] = ArrayBuffer()
      var goalDirection = Direction.NULL
      if (keyStatus.get(Input.Keys.D)) {
        goalDirection = Direction.RIGHT
        nextCell = getTiles(hero.getPosition, 1, 0)
      } else if (keyStatus.get(Input.Keys.A)) {
        goalDirection = Direction.LEFT
        nextCell = getTiles(hero.getPosition, -1, 0)
      } else if (keyStatus.get(Input.Keys.W)) {
        goalDirection = Direction.UP
        nextCell = getTiles(hero.getPosition, 0, 1)
      } else if (keyStatus.get(Input.Keys.S)) {
        goalDirection = Direction.DOWN
        nextCell = getTiles(hero.getPosition, 0, -1)
      }
      // Is the move valid ?
      if (isWalkable(nextCell)) {
        hero.go(goalDirection)    // Go
      } else {
        hero.turn(goalDirection)  // Face the wall
      }
    }
  }

  // Manage keyboard events
  override def onKeyUp(keycode: Int): Unit = {
    super.onKeyUp(keycode)
    keyStatus.put(keycode, false)
  }

  override def onKeyDown(keycode: Int): Unit = {
    super.onKeyDown(keycode)
    keyStatus.put(keycode, true)
  }
}

object Game {

  def main(args: Array[String]): Unit = {
    new Game
  }
}