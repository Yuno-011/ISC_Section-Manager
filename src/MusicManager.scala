import ch.hevs.gdx2d.components.audio.MusicPlayer

import java.io.File
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

class MusicManager {
  private var songsToPlay: ArrayBuffer[String] = ArrayBuffer()
  private var mp: MusicPlayer = null

  def play(soundPath: String): Unit = {
    songsToPlay += soundPath
    if(mp == null || !mp.isPlaying) playNextAudioNow()
    else waitUntilSoundFinished()
  }

  def getRandomKillPhrase: String = {
    var filename: String = ""
    val dir = new File("data/sounds/mudry")
    if (dir.exists && dir.isDirectory) {
      val files = dir.listFiles().filter(_.isFile)
      if (files.nonEmpty) filename = s"data/sounds/mudry/" + files(Random.nextInt(files.length)).getName
    }
    filename
  }

  private def waitUntilSoundFinished(): Unit = {
    Future {
      Thread.sleep(500)
      if(mp.isPlaying) waitUntilSoundFinished()
      else playNextAudioNow()
    }
  }

  private def playNextAudioNow(): Unit = {
    if(songsToPlay.nonEmpty) {
      mp = new MusicPlayer(songsToPlay.head)
      mp.play()
      songsToPlay = songsToPlay.tail
    }
  }
}
