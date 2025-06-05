import ch.hevs.gdx2d.desktop.PortableApplication
import ch.hevs.gdx2d.lib.GdxGraphics
import characters.{Hero, NPC}
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.{TiledMap, TiledMapRenderer, TiledMapTile, TiledMapTileLayer, TmxMapLoader}
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2

import java.util
import scala.collection.mutable.ArrayBuffer

class Game extends PortableApplication(1080, 1080) {
  private final val TAG: String = "ISC: Section Manager"
  private val ZOOM: Float = 0.5f
  private val HERO_LAYER: Int = 2

  private var screenHeight, screenWidth = 0
  private var spriteBatch: SpriteBatch = null
  private var tiledMap: TiledMap = null
  private var tiledMapRenderer: TiledMapRenderer = null

  private var mapManager: MapManager = null
  private var movManager: MovementManager = null

  private var hero: Hero = null
  private val npcs: ArrayBuffer[NPC] = ArrayBuffer()

  private val keyStatus: util.Map[Integer, Boolean] = new util.TreeMap[Integer, Boolean]

  override def onInit(): Unit = {
    setTitle("ISC: Section Manager")
    screenWidth = getWindowWidth
    screenHeight = getWindowHeight

    hero = new Hero(19,13)
    for(_ <- 0 until 5) {
      npcs += new NPC(19, 13)
    }

    tiledMap = new TmxMapLoader().load("data/map_data/maps/classroom.tmx")
    spriteBatch = new SpriteBatch()
    tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, spriteBatch)

    val tiledLayers: ArrayBuffer[TiledMapTileLayer] = ArrayBuffer()
    for(i <- 0 until tiledMap.getLayers.getCount) {
      tiledLayers += tiledMap.getLayers.get(i).asInstanceOf[TiledMapTileLayer]
    }
    mapManager = new MapManager(tiledLayers)
    movManager = new MovementManager(mapManager)
  }

  override def onGraphicRender(g: GdxGraphics): Unit = {
    g.clear()

    // Manage hero movement
    movManager.manageHero(hero, keyStatus)
    movManager.manageNPCs(npcs, Gdx.graphics.getDeltaTime)

    // zoom and camera
    g.zoom(ZOOM)
    g.moveCamera(hero.getPosition.x, hero.getPosition.y, mapManager.getWorldWidth, mapManager.getWorldHeight)
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
    // draw NPCs
    for(npc <- npcs) {
      npc.animate(Gdx.graphics.getDeltaTime)
      npc.draw(spriteBatch)
    }
    // draw last layers
    for(i <- HERO_LAYER until layers.getCount) {
      tiledMapRenderer.renderTileLayer(layers.get(i).asInstanceOf[TiledMapTileLayer])
    }
    // Finish drawing
    spriteBatch.end()
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