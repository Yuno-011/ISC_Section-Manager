import ch.hevs.gdx2d.components.bitmaps.BitmapImage
import ch.hevs.gdx2d.desktop.PortableApplication
import ch.hevs.gdx2d.lib.GdxGraphics
import characters.{Evil, Hero, NPC, Student, Teacher}
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.{TiledMap, TiledMapRenderer, TiledMapTileLayer, TmxMapLoader}
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.{Vector2, Vector3}

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
  private var atkManager: AttackManager = null
  private var musManager: MusicManager = null

  private var hero: Hero = null
  private var npcs: ArrayBuffer[NPC] = ArrayBuffer()

  private val keyStatus: util.Map[Integer, Boolean] = new util.TreeMap[Integer, Boolean]
  private var gameOver: Boolean = false

  override def onInit(): Unit = {
    setTitle("ISC: Section Manager")
    screenWidth = getWindowWidth
    screenHeight = getWindowHeight

    tiledMap = new TmxMapLoader().load("data/map_data/maps/classroom.tmx")
    spriteBatch = new SpriteBatch()
    tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, spriteBatch)

    val tiledLayers: ArrayBuffer[TiledMapTileLayer] = ArrayBuffer()
    for(i <- 0 until tiledMap.getLayers.getCount) {
      tiledLayers += tiledMap.getLayers.get(i).asInstanceOf[TiledMapTileLayer]
    }
    musManager = new MusicManager()
    mapManager = new MapManager(tiledLayers)
    movManager = new MovementManager(mapManager)

    hero = new Hero(19,13)
    if(Math.random() >= 0.5) npcs += new Teacher(10, 14)
    else npcs += new Teacher(10, 14) with Evil
    for(i <- 0 until 10) {
      val pos: Vector2 = mapManager.getRandomPos
      if(i >= 5) npcs += new Student(pos) with Evil
      else npcs += new Student(pos)
    }

    atkManager = new AttackManager(hero)
    musManager.playMusic("data/music/background.mp3")
  }

  override def onGraphicRender(g: GdxGraphics): Unit = {
    g.clear()

    if(!gameOver) {
      // Manage hero movement
      movManager.manageHero(hero, keyStatus)
      movManager.manageNPCs(npcs, hero, Gdx.graphics.getDeltaTime)

      // Manage attacks
      atkManager.handleHeroAttack(npcs, keyStatus)
      gameOver = atkManager.handleNPCAttack(npcs)

      // Remove dead NPCs and add make them respawn
      npcs = npcs.filter(npc => npc.isAlive)
      if(npcs.length < 11) musManager.playSound(musManager.getRandomKillPhrase)
      while(npcs.length < 11) {
        if(!npcs.exists(npc => npc.isInstanceOf[Teacher])) {
          if(Math.random() >= 0.5) npcs += new Teacher(10, 14)
          else npcs += new Teacher(10, 14) with Evil
        } else {
          val pos: Vector2 = mapManager.getRandomPos
          if(npcs.count(n => n.isInstanceOf[Evil]) < 5) npcs += new Student(pos) with Evil
          else npcs += new Student(pos)
        }
      }
    }

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
    // draw projectile
    if(hero.codeLaser != null) {
      hero.codeLaser.animate(Gdx.graphics.getDeltaTime)
      hero.codeLaser.draw(spriteBatch)
    }
    // Finish drawing
    spriteBatch.end()
    g.drawString(screenWidth/2-90, screenHeight-220, s"Score: ${hero.score}")
    // Draw screen-fixed UI text in top-left
    val screenPos = new Vector3(0, 0, 0)  // camera based in the top left corner
    val worldPos = g.getCamera.unproject(screenPos)
    g.drawTransformedPicture(worldPos.x+100, worldPos.y-23, 0, 100, 23, new BitmapImage("data/images/hp-bar.png"))
    val width: Float = hero.getHealthPercent*153
    g.drawFilledRectangle(worldPos.x+41.5f+width/2, worldPos.y-10.5f-25/2, width, 25, 0, Color.SCARLET)

    if(gameOver) {
      g.drawString(worldPos.x+220, worldPos.y-220, "GAME OVER")
      g.drawString(worldPos.x+230, worldPos.y-250, s"Score: ${hero.score}")
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

  override def onDispose(): Unit = {
    super.onDispose()
    musManager.dispose()
  }
}

object Game {

  def main(args: Array[String]): Unit = {
    new Game
  }
}