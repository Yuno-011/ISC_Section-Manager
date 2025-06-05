import characters.Direction
import com.badlogic.gdx.maps.tiled.{TiledMapTile, TiledMapTileLayer}
import com.badlogic.gdx.math.Vector2

import scala.collection.mutable.ArrayBuffer

class MapManager(private var tiledLayers: ArrayBuffer[TiledMapTileLayer]) {

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

  def getNextCell(pos: Vector2, dir: Direction.Direction): ArrayBuffer[TiledMapTile] = {
    dir match {
      case Direction.UP => getTiles(pos, 0, 1)
      case Direction.RIGHT => getTiles(pos, 1, 0)
      case Direction.DOWN => getTiles(pos, 0, -1)
      case Direction.LEFT => getTiles(pos, -1, 0)
      case Direction.NULL => ArrayBuffer()
    }
  }

  def getWorldWidth: Float = tiledLayers(0).getWidth * tiledLayers(0).getTileWidth

  def getWorldHeight: Float = tiledLayers(0).getHeight * tiledLayers(0).getTileHeight

  /**
   * Get the "walkable" property of the given tile.
   *
   * @param cell
   * The cell to know the property
   * @return true if the property is set to "true", false otherwise
   */
  def isWalkable(cell: ArrayBuffer[TiledMapTile]): Boolean = {
    if(cell.forall(tile => tile == null)) return false
    if(cell.isEmpty) return false
    for(tile <- cell) {
      if (tile != null) {
        val test = tile.getProperties.get("walkable")
        if(test != null && test.toString == "false") return false
      }
    }
    true
  }
}
